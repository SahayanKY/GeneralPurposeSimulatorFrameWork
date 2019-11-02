package simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * シミュレーションを非同期に立ち上げ、実行させるクラス。
 * */
public class SimulatorLauncher {

	/**実行するシミュレーションのクラス*/
	private Simulator simulator;
	/**立ち上げたマルチスレッド全部の完了をもって完了するCompletableFuture*/
	private CompletableFuture<Void> allcf;
	/**interfacer*/
	private SimulatorGUIInterfacer interfacer;

	/**
	 * コンストラクタ。
	 * @param simulator
	 * 起動させるSimulatorクラスインスタンス
	 * @param interfacer
	 * GUI側とやり取りするSimulatorGUIInterfacerクラスインスタンス
	 * */
	SimulatorLauncher(Simulator simulator, SimulatorGUIInterfacer interfacer) {
		this.simulator = simulator;
		this.interfacer = interfacer;
	}

	/**
	 * 呼び出すことによって、シミュレーションを完全に停止させます。
	 * 途中でシミュレーションをやめる場合に利用してください。
	 * これを呼び出した際に、ある単一条件のシミュレーションが実行中であった場合、
	 * その条件が完了しファイル出力も完了した後に、
	 * 次の条件に移行せずに停止します。
	 * */
	public void cancel() {
		this.allcf.cancel(true);
	}

	/**
	 * すべての単一条件が完了するまで、このメソッドを呼び出したスレッドを停止します。
	 * 完了後は実行を再開します。
	 * */
	public void join() {
		this.allcf.join();
	}


	/**
	 * Simulatorを起動させます。
	 * 並列数1であっても、これを呼び出したスレッドとは異なるスレッド上でシミュレーションは実行されます。
	 * これを呼び出す前にsimulatorのパラメータの設定等は済ませておいてください。
	 * */
	public void launch() {
		final ExecutorService es_each = Executors.newFixedThreadPool(simulator.parallelNum);

		//スレッド数だけCompletaleFutureを作り、非同期に実行させる。
		//それらCompletableFutureのリストを受け取る
		List<CompletableFuture<Void>> cflist = this.executeMultithread(es_each);

		//全部のCompletableFutureを統合するCompletableFutureを作る。
		//それは、キャンセルすれば、全てのCompletableFutureをキャンセル、Taskを停止させ、
		//全てのCompletableFutureが完了すればシャットダウンする
		this.allcf = this.getIntegratedCompletableFuture(cflist, es_each);
	}

	/**
	 * すべてのTaskが完了したのをもって完了とするCompletableFutureを返します。
	 * */
	private CompletableFuture<Void> getIntegratedCompletableFuture(List<CompletableFuture<Void>> cflist, ExecutorService es) {
		CompletableFuture<Void> allcf = CompletableFuture.allOf(
					cflist.toArray(new CompletableFuture[cflist.size()])
		);
		allcf.whenComplete((ret,ex) -> {
			if(ex == null) {
				//異常終了なし
			}else if(ex instanceof CancellationException){
				//アプリ側からの停止命令
				//それぞれのCompletableFutureに対してcancelをかけ、FutureTaskを停止させる
				cflist.stream().forEach(cf -> {
					cf.cancel(true);
				});
				System.out.println(ex+":アプリからの停止命令により終了しました");
			}
			es.shutdown();
		});
		return allcf;
	}

	/**
	 * 指定されていた並列数の数だけ、
	 * 単一条件の計算を次々と実行していくCompletableFutureを生成します。
	 * */
	private List<CompletableFuture<Void>> executeMultithread(ExecutorService es) {
		List<CompletableFuture<Void>> cflist = new ArrayList<>();

		for(int i=1;i<=simulator.parallelNum;i++) {
			CompletableFuture<Void> cf = new CompletableFuture<>();

			//仕事をもらえる限り仕事をし続けるTaskインスタンスの生成
			FutureTask<Void> task = new FutureTask<Void>(() -> {
				try {
					Runnable runnable;
					while((runnable = simulator.createNextConditionSolver()) != null) {
						//受け取ったrunnableを同期的に(このスレッド上で)実行
						runnable.run();
						interfacer.updateProgress();

						if(Thread.currentThread().isInterrupted()) {
							//中断を受けた場合は停止する
							throw new InterruptedException("次の計算条件に移る前に中断されました. ThreadName:"+Thread.currentThread().getName());
						}
					}

					//runnable == null
					//Completed
					return null;

				}catch(InterruptedException e) {
					System.out.println(e);
					return null;
				}
			});

			es.execute(() -> {
				try {
					task.run();
					cf.complete(task.get());
				}catch(ExecutionException ex) {
					cf.completeExceptionally(ex.getCause());
				}catch(Throwable ex) {
					cf.completeExceptionally(ex);
				}
			});


			//cf.cancel(true)で、CompletableFutureを停止するが、実行中の動作は停止されない
			//そこで、cancelで異常終了し、下の処理が回るので、そこでTaskをcancelする
			cf.whenComplete((ret,ex) -> {
				if(ex instanceof CancellationException) {
					//CompletableFutureがキャンセルによって完了している場合
					if(!task.isDone()) {
						//FutureTaskがまだ実行中で、割り込みをかける
						task.cancel(true);
					}
				}

			});

			cflist.add(cf);

		}

		return cflist;
	}

}
