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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import simulation.frame.DataInputFrame;
import simulation.param.Parameter;
import simulation.param.ParameterManager;
import simulation.param.checker.WhiteSpaceChecker;

public abstract class Simulator extends SwingWorker<Object,String>{
	private DataInputFrame inputFrame;
	private Map<String,BufferedWriter> writermap = new HashMap<>();
	private ProgressMonitor monitor;
	private LocalDateTime simulationStartTime;
	protected final ParameterManager paraMana = new ParameterManager(this);
	protected File resultStoreDirectory;
	private double startTime,currentProgressRate;
	protected final ArrayList<Supplier<String[]>> parameterSetterFuncList = new ArrayList<>();

	public static final String
		STREAM_CREATE = "1",
		STREAM_CLOSE = "2",
		STREAM_LOG = "3";

	public Simulator(){
		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				ProgressMonitor monitor = Simulator.this.monitor;
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

	//getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日HH時mm分ss.SSS秒"))
	public final LocalDateTime getSimulationStartTime() {
		if(simulationStartTime == null) {
			simulationStartTime = LocalDateTime.now();
		}
		return this.simulationStartTime;
	}

	/*
	 * 計算結果の出力先のディレクトリを指定し、シミュレーションを開始する。
	 * 同名のexecute()では正しく起動しない実装をしているため、こちらを呼び起動させる。
	 * @param resultStoreDirectory ユーザーが指定したシミュレーション結果を保存させるディレクトリ
	 * このディレクトリの下の階層に各種結果ファイルを保存する
	 * */
	public final void execute(File resultStoreDirectory) throws IOException{
		//シミュレーション日時の決定
		setSimulationStartTime();
		this.resultStoreDirectory
			= new File(resultStoreDirectory.toString()
				+"\\"+
				getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日HH時mm分ss.SSS秒"))
				+"シミュレーション結果"
			);
		//ディレクトリを作らないとFileNotFoundExceptionになる
		this.resultStoreDirectory.mkdir();
		execute();
	}

	protected final void addParameterSetterFunc(Supplier<String[]> sup) {
		this.parameterSetterFuncList.add(sup);
	}

	public final String[][] setParameter(){
		int n = this.parameterSetterFuncList.size();
		String[][] result = new String[n][];
		for(int i=0;i<n;i++) {
			//シミュレーションのパラメータ値をメンバ変数に代入し、
			//プロパティファイルに出力したい文字列をresultに与える
			result[i] = this.parameterSetterFuncList.get(i).get();
		}
		return result;
	}


	@Override
	/*
	 * シミュレーションの計算実行開始メソッド。
	 * */
	protected Object doInBackground() {
		try {
			//シミュレーションに必要なパラメータをセットする
			String result[][] = this.setParameter();

			//ディレクトリを指定して、パラメータやその合成量をファイルとして記録させる
			paraMana.writePropertyOn(resultStoreDirectory,result);
			executeSimulation();

		}catch(Exception e) {

			e.printStackTrace();
		}

		monitor.close();

		return null;
	}

	/*
	 * シミュレーション本体の実装部分。Simulatorの子クラスはこのメソッドをオーバーライドし、
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

	/*
	 * シミュレーション実行結果を外部ファイルへ出力する。このメソッドは直接呼ぶものではない。
	 * 必ずpublish(String)を介して呼ぶこと。また、publish(String)で指定する文字列は以下に従うこと。
	 *　・STREAM_CREATE+":xxx.yyy"でxxx.yyyへの出力ストリームを生成する
	 *	・STREAM_CLOSE+":xxx.yyy"でxxx.yyyへの出力をやめる
	 * 	・STREAM_LOG+":xxx.yyy:...."でxxx.yyyへ"...."を入力し、改行する
	 * */
	@Override
	protected void process(List<String> list) {
		for(String strline:list) {
			if(strline.startsWith(STREAM_CREATE+":")) {
				//writerを生成する分岐

				try {
					String filename = strline.substring(STREAM_CREATE.length()+1);
					File storeFile = new File(resultStoreDirectory.toString()+"\\"+filename);
					if(writermap.containsKey(filename)) {
						//既に指定されたファイルに対応したwriterが存在する場合、
						//例外をトレースした後、そのwriterをマッピングから外す
						//フラッシュし、クローズし、新しいwriterをマッピングする
						new IOException("指定されたファイルは既に存在し、結果は上書きされています:ファイル名:"+filename).printStackTrace();
						BufferedWriter writer = writermap.remove(filename);
						try {
							writer.flush();
						}catch(IOException e) {
							e.printStackTrace();
						}finally {
							try {
								writer.close();
							}catch(IOException e) {
								e.printStackTrace();
							}
						}
					}

					//writerを生成する
					//前に対応するwriterが存在した場合、それは上で既に解法済みなのでNoProblem
					writermap.put(filename,
							new BufferedWriter(new OutputStreamWriter(new FileOutputStream(storeFile),"UTF-8"))
					);
				}catch(Exception e) {
					e.printStackTrace();
				}

			}else if(strline.startsWith(STREAM_CLOSE+":")){
				//ファイルへの出力を停止する分岐

				final String filename = strline.substring(STREAM_CLOSE.length()+1);

				if(!writermap.containsKey(filename)) {
					//対応したwriterが存在しない場合
					continue;
				}
				BufferedWriter writer = writermap.get(filename);


				//writerをフラッシュし、クローズする
				//その後、マッピングから外す
				try {
					writer.flush();
				}catch(IOException e) {
					e.printStackTrace();
				}finally {
					try {
						writer.close();
					}catch(IOException e) {
						e.printStackTrace();
					}finally {
						writermap.remove(filename);
					}
				}

			}else if(strline.startsWith(STREAM_LOG+":")) {
				//ファイルへの出力をする分岐

				//ファイル名を取得し、writerを取得
				String strarray[] = strline.split(":",3);

				//[0]=log
				//[1]=xxx.yyy
				//[2]=...... //内容
				if(!writermap.containsKey(strarray[1])) {
					//対応したwriterが存在しない場合
					continue;
				}
				BufferedWriter writer = writermap.get(strarray[1]);

				try {
					//内容を出力する
					writer.write(strarray[2]);
					writer.newLine();
				}catch(IOException e) {
					e.printStackTrace();
				}

			}else {
				//想定外の処理
				new IllegalArgumentException("想定されていないコマンドです:"+strline).printStackTrace();
			}

		}
	}

	/*
	 * このシミュレーションが使用するParameterをParameterManagerに登録する。
	 * オーバーライドするときはSimulatorクラスのcreateParameter()を一番最初に呼び出すようにしてください。
	 * */
	public void createParameters(){
		final Parameter
			シミュ実行者 = new Parameter("一般", "シミュレーション実行者", "シミュレーション実行者", null, null, new WhiteSpaceChecker());

		paraMana.addParameter(シミュ実行者);

		this.parameterSetterFuncList.add(()->{
			return new String[] {
					"シミュレーション年月日時分秒="+this.getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日_HH:mm:ss.SSS")),
					"シミュレータ="+this.getThisName(),
					"バージョン="+this.getThisVersion()
			};
		});
	}

	/*
	 * このSimulatorインスタンスが保持するParameterManagerを返す。
	 * */
	public ParameterManager getParameterManager() {
		return this.paraMana;
	}

	/*
	 * このSimulatorがシミュレーションを実行するのに必要とするパラメータを入力するためのDataInputFrameを展開する。
	 * @param
	 * width 展開するDataInputFrameの幅
	 * height 展開するDataInputFrameの縦幅
	 * */
	public void openDataInputFrame(int width, int height) {
		inputFrame = new DataInputFrame(this,width,height);
		monitor = new ProgressMonitor(inputFrame, "メッセージ", "ノート", 0, 100);
	}

	/*
	 * このSimulatorがシミュレーション実行終了後に行う処理を規定する。
	 * */
	@Override
	protected void done() {
		inputFrame.dispose();
		for(BufferedWriter writer:writermap.values()) {
			if(writer == null) {
				continue;
			}
			try {
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					writer.close();
				}catch(IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		writermap.clear();
	}

	/*
	 * このシミュレータを表す名前を返す
	 * */
	public abstract String getThisName();

	/*
	 * このシミュレータの開発バージョンを返す
	 */
	public abstract String getThisVersion();

}
