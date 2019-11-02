package simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class SimulatorLauncher {
	private Simulator simulator;
	private CompletableFuture<Void> allcf;
	private SimulatorGUIInterfacer interfacer;


	SimulatorLauncher(Simulator simulator, SimulatorGUIInterfacer interfacer) {
		this.simulator = simulator;
		this.interfacer = interfacer;
	}

	public void cancel() {
		this.allcf.cancel(true);
	}

	public void join() {
		this.allcf.join();
	}


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
