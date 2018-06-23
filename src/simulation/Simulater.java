package icg;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

public abstract class Simulater extends SwingWorker<Object,Object>{
	private String[] simuResultLabels;
	private BufferedWriter resultWriter;
	private ProgressMonitor monitor = new ProgressMonitor(null, "メッセージ", "ノート", 0, 500);

	public Simulater(String[] simuResultLabels, File resultSaveDirectory){
		this.simuResultLabels = simuResultLabels;
		try {
			this.resultWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(resultSaveDirectory.toString()+"\\result.txt")),"UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}

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

	/*
	 * for (int i = min; i < max; i++) {
			// 終了判定
			if (pm.isCanceled()) {
				pm.close();
				break;
			}

			pm.setNote("現在：" + i);

			//何かの処理

			pm.setProgress(i + 1); //プログレスバーに現在値をセット
		}
	 *
	 * */

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
