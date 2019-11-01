package simulation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import simulation.frame.DataInputFrame;
import simulation.param.Parameter;
import simulation.param.ParameterManager;
import simulation.param.checker.DefaultParameterChecker;
import simulation.param.checker.ParameterChecker;
import simulation.param.checker.WhiteSpaceChecker;
import simulation.system.SystemInfo;

public abstract class Simulator extends SwingWorker<Object,String>{
	private DataInputFrame inputFrame;
	private Map<String,BufferedWriter> writermap = new HashMap<>();
	private ProgressMonitor monitor;
	private LocalDateTime simulationStartTime;
	protected final ParameterManager paraMana = new ParameterManager(this);
	protected File resultStoreDirectory;
	private double startTime,currentProgressRate;
	protected int parallelNum;

	/**入力値をSolverに必要なParameterにセットし、
	 * さらにその数値を処理してpropertiesファイルに出力する値の文字列を返す*/
	protected final ArrayList<Supplier<String[]>> parameterSetterFuncList = new ArrayList<>();

	public Simulator(){
		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				ProgressMonitor monitor = Simulator.this.monitor;
				if(monitor == null) {
					return;
				}
				if(monitor.isCanceled()) {
					System.out.println("iscancled");
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

	public final void setParameter(){
		int n = this.parameterSetterFuncList.size();
		String[][] result = new String[n][];
		for(int i=0;i<n;i++) {
			//シミュレーションのパラメータ値をメンバ変数に代入し、
			//プロパティファイルに出力したい文字列をresultに与える
			result[i] = this.parameterSetterFuncList.get(i).get();
		}
		//ディレクトリを指定して、パラメータやその合成量をファイルとして記録させる
		try {
			paraMana.writePropertyOn(resultStoreDirectory,result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	/*
	 * シミュレーションの計算実行開始メソッド。
	 * */
	protected Object doInBackground() {
		monitor = new ProgressMonitor(inputFrame, "メッセージ", "ノート", 0, 100);
		updateProgress(0);

		//シミュレーションに必要なパラメータをセットする
		this.setParameter();

		//final ExecutorService es_each = Executors.newFixedThreadPool(parallelNum);
		//final ExecutorService es_all = E

		double[] array;
		for(int i=0;i<1000000;i++) {
			array = new double[50];
		}


		updateProgress(1);

		monitor.close();

		return null;
	}

	/**
	 * (計算されていない、またはSubmitされていない)次の計算条件の元で計算を行うSolverインスタンスを返す。
	 *
	 * */
	protected abstract Runnable createNextConditionSolver();


	/**
	 * 条件の数
	 * */
	protected abstract int getAllConditionNumber();


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
	protected void updateProgress(double progressRate) {
		this.currentProgressRate = progressRate;
		int n = (int)(progressRate*100);
		if(n < 0) {
			n = 0;
		}else if(n > 100) {
			n = 100;
		}
		setProgress(n);
	}




	/*
	 * このシミュレーションが使用するParameterをParameterManagerに登録する。
	 * オーバーライドするときはSimulatorクラスのcreateParameter()を一番最初に呼び出すようにしてください。
	 * */
	public void createParameters(){
		final ParameterChecker defchecker = new DefaultParameterChecker();
		String mincore = "1", maxcore = String.valueOf(SystemInfo.CPU_CORE_NUM);


		final Parameter
			シミュ実行者 = new Parameter("一般", "シミュレーション実行者", "シミュレーション実行者", null, null, new WhiteSpaceChecker()),
			並列数 = new Parameter("一般", "並列数", "並列数", mincore, maxcore, defchecker);

		paraMana.addParameter(シミュ実行者);
		paraMana.addParameter(並列数);

		this.parameterSetterFuncList.add(()->{
			parallelNum = Integer.valueOf(並列数.getValue());

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
