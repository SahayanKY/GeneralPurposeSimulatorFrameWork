package simulation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import icg.frame.DataInputFrame;
import simulation.param.Parameter;
import simulation.param.ParameterManager;

public abstract class Simulater extends SwingWorker<Object,String>{
	private DataInputFrame inputFrame;
	protected BufferedWriter resultWriter;
	private ProgressMonitor monitor;
	private LocalDateTime simulationStartTime;
	private ParameterManager paraMan;
	protected File resultStoreDirectory;
	private double startTime,currentProgressRate;


	public Simulater(){
		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				ProgressMonitor monitor = Simulater.this.monitor;
				if(monitor == null) {
					return;
				}
				if(monitor.isCanceled()) {
					return;
				}
				if("progress".equals(e.getPropertyName())) {
					String restTimeStr;
					if(startTime == 0.0) {
						startTime = simulationStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
					}
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

	public final void setSimulationStartTime() {
		this.simulationStartTime = LocalDateTime.now();
	}

	public final LocalDateTime getSimulationStartTime() {
		if(simulationStartTime == null) {
			simulationStartTime = LocalDateTime.now();
		}
		return this.simulationStartTime;
	}

	/*
	 * 計算結果の出力先のディレクトリを指定し、シミュレーションを開始する。
	 * 同名のexecute()では正しく起動しない仕様であるので注意。
	 * */
	public void execute(File resultStoreDirectory) throws IOException{
		//シミュレーション日時の決定
		setSimulationStartTime();
		this.resultStoreDirectory = resultStoreDirectory;
		execute();
	}

	@Override
	/*
	 * シミュレーションの計算実行開始メソッド。
	 * */
	protected Object doInBackground() {
		try {
			//指定したディレクトリにパラメータを保存させる
			paraMan.writeProperty_on(resultStoreDirectory);
			executeSimulation(paraMan.getInputParamMap(false));

		}catch(Exception e) {
			try {
				if(resultWriter != null) {
					resultWriter.close();
				}
			} catch (IOException e1) {
			}
			e.printStackTrace();
		}

		monitor.close();

		return null;
	}

	/*
	 * シミュレーション本体の実装部分。Simulaterの子クラスはこのメソッドをオーバーライドし、
	 * 計算を行うようにすること。また、計算の各段階で適切にupdateProgress(double)とpublish(String)を使うこと。
	 * */
	protected abstract void executeSimulation(LinkedHashMap<String,LinkedHashMap<String,Parameter>> map);

	/*
	 * シミュレーションの計算進捗率を指定する。もし、シミュレーションを中断する
	 * ようユーザーから指示が来ていた場合、falseが返る。
	 * @param
	 * progressRate 計算の進捗を表すdouble型の数値。0から1までの数値が有効。
	 * それ以外の数値は自動的に0または1に変換される。
	 * @return
	 * シミュレーションを中断するようProgressMonitorを介して
	 * 指示されていた場合、false
	 * */
	protected boolean updateProgress(double progressRate) {
		if(monitor.isCanceled()) {
			return false;
		}
		this.currentProgressRate = progressRate;
		int n = (int)(progressRate*100);
		if(n < 0) {
			n = 0;
		}else if(n > 100) {
			n = 100;
		}
		setProgress(n);
		return true;
	}

	@Override
	protected abstract void process(List<String> list);

	public abstract void calculateAndSetParameterValue(LinkedHashMap<String,LinkedHashMap<String,Parameter>> map);

	public abstract void createParameters();

	public abstract ArrayList<Parameter> getParameterList();

	public ParameterManager getParameterManager() {
		return this.paraMan;
	}

	public final void openInputFrame(int width, int height) {
		paraMan = new ParameterManager(this);
		inputFrame = new DataInputFrame(this,width,height);
		monitor = new ProgressMonitor(inputFrame, "メッセージ", "ノート", 0, 100);
	}

	@Override
	protected void done() {
		inputFrame.dispose();
		System.out.println("done()");
		System.out.println("done():currentProgressRate:"+currentProgressRate);
		try {
			this.resultWriter.flush();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}finally {
			try {
				if(resultWriter != null) {
					this.resultWriter.close();
				}
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}

}
