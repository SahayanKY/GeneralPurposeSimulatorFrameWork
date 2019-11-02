package simulation;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.function.Supplier;

import simulation.param.Parameter;
import simulation.param.ParameterManager;
import simulation.param.checker.DefaultParameterChecker;
import simulation.param.checker.ParameterChecker;
import simulation.param.checker.WhiteSpaceChecker;
import simulation.system.SystemInfo;

/**
 * 
 * 単一条件を計算するSolverを生成する
 * */
public abstract class Simulator{
	private LocalDateTime simulationStartTime;
	protected final ParameterManager paraMana = new ParameterManager(this);
	protected File caseResultDirectory;
	protected int parallelNum;

	/**入力値をSolverに必要なParameterにセットし、
	 * さらにその数値を処理してpropertiesファイルに出力する値の文字列を返す*/
	protected final ArrayList<Supplier<String[]>> parameterSetterFuncList = new ArrayList<>();


	protected final void setSimulationStartTime() {
		//TODO 2回以上このメソッドを呼び出すのを例外処理
		this.simulationStartTime = LocalDateTime.now();
	}

	public final LocalDateTime getSimulationStartTime() {
		if(this.simulationStartTime == null) {
			throw new IllegalStateException("Simulatorはまだ起動していません");
		}
		return this.simulationStartTime;
	}

	protected final void addParameterSetterFunc(Supplier<String[]> sup) {
		this.parameterSetterFuncList.add(sup);
	}

	public void setResultDirectory(File parentResultDirectory) throws IOException{
		//ユーザーに指定された保存先ディレクトリ
		String parentResultDirectoryStr = parentResultDirectory.getPath();
		if(!parentResultDirectoryStr.endsWith("/") && !parentResultDirectoryStr.endsWith("\\")) {
			parentResultDirectoryStr += "/";
		}

		//保存先ディレクトリに作る今回の計算結果保存用のディレクトリ
		String caseResultDirectoryStr = parentResultDirectoryStr +
				getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日HH時mm分ss.SSS秒"))
				+"シミュレーション結果";

		this.caseResultDirectory = new File(caseResultDirectoryStr);

		//TODO たまたま保存ディレクトリが一致していた場合の例外処理

		caseResultDirectory.mkdirs();
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
			paraMana.writePropertyOn(caseResultDirectory,result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * (計算されていない、またはSubmitされていない)次の計算条件の元で計算を行うSolverインスタンスを返す。
	 * */
	protected abstract Runnable createNextConditionSolver();


	/**
	 * 条件の数
	 * */
	protected abstract int getAllConditionNumber();

	/**
	 * このシミュレーションが使用するParameterをParameterManagerに登録する。
	 * そして、計算開始時のGUIからのパラメータのセット方法について記述する。
	 *
	 * このインスタンスを生成した後はこのメソッドを最初に必ず呼び出してください。
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

	/**
	 * このSimulatorインスタンスが保持するParameterManagerを返す。
	 * */
	public ParameterManager getParameterManager() {
		return this.paraMana;
	}

	/**
	 * このシミュレータを表す名前を返す
	 * */
	public abstract String getThisName();

	/**
	 * このシミュレータの開発バージョンを返す
	 */
	public abstract String getThisVersion();

}
