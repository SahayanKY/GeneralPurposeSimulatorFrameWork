package simulation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import icg.frame.DataInputFrame;
import simulation.param.Parameter;

public abstract class Simulater extends SwingWorker<Object,String>{
	private DataInputFrame inputFrame;
	private BufferedWriter resultWriter;
	private ProgressMonitor monitor;
	private LocalDateTime simulationStartTime;

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
	public void execute(File resultSaveDirectory) throws IOException {
		try {
			File storeFile = new File(resultSaveDirectory.toString()+"\\"+getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日HH時mm分ss.SSS秒"))+"result.csv");
			if(storeFile.exists()) {
				//同名のファイルが存在する場合
				throw new IOException("同名のファイルが存在");
			}

			this.resultWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(storeFile),"UTF-8"));
			execute();
		}catch(IOException e) {
			try {
				if(resultWriter != null) {
					resultWriter.close();
				}
			} catch (IOException e1) {
			}
			throw e;
		}
	}

	@Override
	/*
	 * シミュレーションの計算実行開始メソッド。
	 * */
	protected Object doInBackground() {
		executeSimulation();
		monitor.close();

		return null;
	}

	/*
	 * シミュレーション本体の実装部分。Simulaterの子クラスはこのメソッドをオーバーライドし、
	 * 計算を行うようにすること。また、計算の各段階で適切にupdateProgress(double)とpublish(String)を使うこと。
	 * */
	protected abstract void executeSimulation();

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
	protected void process(List<String> list) {
		for(String strline:list) {
			try {
				this.resultWriter.write(strline);
				this.resultWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public abstract void setSystemInputParameterValue();

	public abstract void createParameters();

	public abstract ArrayList<Parameter> getParameterList();

	public final void openInputFrame() {
		inputFrame = new DataInputFrame(this);
		monitor = new ProgressMonitor(inputFrame, "メッセージ", "ノート", 0, 100);
	}

	@Override
	protected void done() {
		inputFrame.dispose();
		System.out.println("done()");
		try {
			this.resultWriter.flush();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}finally {
			try {
				this.resultWriter.close();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}

}
