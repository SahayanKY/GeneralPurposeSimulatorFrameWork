package simulation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.time.ZoneId;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import simulation.frame.DataInputFrame;
import simulation.lock.SimulationProgressLock;

public class SimulatorGUIInterfacer extends SwingWorker<Object,String>{
	private final Simulator simulator;
	private final SimulatorLauncher launcher;
	private final DataInputFrame inputFrame;
	private ProgressMonitor monitor;
	private final SimulationProgressLock progresslock = new SimulationProgressLock();

	private int completedTaskNum = 0;

	/**
	 * GUIアプリ側とSimulator側の境界をとるクラスのインスタンスを生成します。
	 * @param
	 * 	simulator	起動する(使用する)Simulatorのクラスインスタンス
	 * @param
	 * 	inputFrame	入力画面
	 * */
	public SimulatorGUIInterfacer(Simulator simulator, DataInputFrame inputFrame) {
		this.simulator = simulator;
		this.launcher = new SimulatorLauncher(simulator,this);
		this.inputFrame = inputFrame;

		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				ProgressMonitor monitor = SimulatorGUIInterfacer.this.monitor;
				if(monitor == null) {
					return;
				}
				if(monitor.isCanceled()) {
					System.out.println("monitor is canceled");
					launcher.cancel();
					return;
				}
				if("progress".equals(e.getPropertyName())) {
					String restTimeStr;
					double startTime = simulator.getSimulationStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
					double currentProgressRate = (double)completedTaskNum/simulator.getAllConditionNumber();
					if(currentProgressRate == 0.0) {
						restTimeStr = "";
					}else {
						double elapsedTime = System.currentTimeMillis() - startTime;
						double restTime = elapsedTime * (1-currentProgressRate)/currentProgressRate;
						int restM = (int)(restTime/60000);
						restTimeStr = "約"+((restM==0)?"":restM+"分")+(int)(restTime/1000-restM*60)+"秒";
					}
					monitor.setNote("現在:"+e.getNewValue()+"%　:完了まで"+restTimeStr);
					monitor.setProgress((Integer)e.getNewValue()+1);
				}
			}
		});

	}


	/*
	 * 計算結果の出力先のディレクトリを指定し、シミュレーションを開始する。
	 * 同名のexecute()では正しく起動しない実装をしているため、こちらを呼び起動させる。
	 * @param resultStoreDirectory ユーザーが指定したシミュレーション結果を保存させるディレクトリ
	 * このディレクトリの下の階層に各種結果ファイルを保存する
	 * */
	public void executeSimulator(File resultStoreDirectory) throws IOException{
		//シミュレーション日時の決定
		simulator.setSimulationStartTime();

		simulator.setResultDirectory(resultStoreDirectory);


		//SwingWorker
		execute();// -> doInBackground()
	}


	@Override
	protected Object doInBackground(){
		//TODO executeSimulatorを介さなかった場合の例外処理
		monitor = new ProgressMonitor(inputFrame, "メッセージ", "ノート", 0, 100);
		updateProgress(false);

		//シミュレーションに必要なパラメータをセットする
		simulator.setParameter();

		//シミュレータの起動
		launcher.launch();

		//ランチャーが起動したシミュレーションが完了するまで待機
		launcher.join();
		System.out.println("joined");

		updateProgress(true);
		monitor.close();

		return null;
	}

	/*
	 * このSimulatorがシミュレーション実行終了後に行う処理を規定する。
	 * */
	@Override
	protected void done() {
		inputFrame.dispose();

		//TODO この後の処理どうするか

	}


	/**
	 * シミュレーションの計算進捗率を1タスク分更新する。
	 * この値の変化を読み取ってMonitorに反映されます。
	 * */
	protected void updateProgress() {
		synchronized(progresslock) {
			this.completedTaskNum++;
			double currentProgressRate = (double)completedTaskNum/simulator.getAllConditionNumber();
			int n = (int)(currentProgressRate*100);
			if(n < 0) {
				n = 0;
			}else if(n > 100) {
				n = 100;
			}
			setProgress(n);
		}
	}

	/**
	 * シミュレーションの計算進捗率を0%または100%に設定します。
	 * このメソッドは進捗率設定を的確に行うための冗長的なメソッドです。
	 * @param iscompleted
	 * 終了していない場合はfalse、終了した場合はtrue
	 * */
	protected void updateProgress(boolean iscompleted) {
		synchronized(progresslock) {
			double currentProgressRate = (iscompleted)? 1:0;
			setProgress((int)(currentProgressRate*100));
			System.out.println("progress:"+iscompleted+","+currentProgressRate);
		}
	}

}
