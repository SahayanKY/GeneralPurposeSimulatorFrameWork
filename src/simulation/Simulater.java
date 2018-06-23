package simulation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import simulation.param.Parameter;

public abstract class Simulater extends SwingWorker<Object,Object>{
	private BufferedWriter resultWriter;
	private ProgressMonitor monitor = new ProgressMonitor(null, "メッセージ", "ノート", 0, 500);
	private LocalDateTime simulationStartTime;

	public Simulater(){
		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if("progress".equals(e.getPropertyName())) {
					ProgressMonitor monitor = Simulater.this.monitor;
					if(monitor.isCanceled()) {
						monitor.close();
					}
					monitor.setNote("現在:"+e.getNewValue());
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

	public void execute(File resultSaveDirectory) throws IOException {
		try {
			File storeFile = new File(resultSaveDirectory.toString()+"\\"+getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日HH時mm分ss.SSS秒"))+"result.txt");
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
	 * シミュレーションの計算実行を行う。
	 * */
	protected Object doInBackground() {
		for(int i=0; i<100; i++) {
			setProgress(i);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	protected abstract void process(List<Object> list);

	public abstract void setSystemInputParameterValue();

	public abstract void createParameters();

	public abstract ArrayList<Parameter> getParameterList();

	@Override
	protected void done() {
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
