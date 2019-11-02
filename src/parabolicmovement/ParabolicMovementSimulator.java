package parabolicmovement;

import java.io.File;
import java.util.function.Function;

import icg.PhysicalQuantity;
import simulation.Simulator;
import simulation.io.CSVWriter;
import simulation.param.Parameter;
import simulation.param.checker.DefaultParameterChecker;
import simulation.param.checker.ParameterChecker;

public class ParabolicMovementSimulator extends Simulator{

	private StaticParameters staparams;
	private DynamicParametersCombinations dynparamscomb;


	public static void main(String args[]) {
		ParabolicMovementSimulator pms = new ParabolicMovementSimulator();
		pms.createParameters();
		pms.openDataInputFrame(340,490);
	}


	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected Runnable createNextConditionSolver() {
		DynamicParameters dynparams;
		if((dynparams = dynparamscomb.getNextDynamicParameters()) == null) {
			return null;
		} else {
			Runnable runnable =
					new Runnable() {
						/**計算条件*/
						private DynamicParameters dynparams;

						@Override
						public void run() {
							ParabolicMovementSolver solver = new ParabolicMovementSolver(staparams,dynparams);
							Result result = solver.solve();
							File saveFile = new File(resultStoreDirectory.toString()+"\\"+getLogFileName(dynparams));
							new CSVWriter().writeNext(result, saveFile);
						}

						public Runnable setDynParams(DynamicParameters dynparams){
							this.dynparams = dynparams;
							return this;
						}
					}.setDynParams(dynparams);
			return runnable;
		}
	}


	/**
	 * {@inheritDoc}
	 * */
	@Override
	protected int getAllConditionNumber() {
		return dynparamscomb.allCombiNum;
	}


	@Override
	public String getThisName() {
		return "放物運動シミュレーション";
	}

	@Override
	public String getThisVersion() {
		return "0.2-20191028";
	}

	public String getLogFileName(DynamicParameters dynparam) {
		return "log-射角"+dynparam.theta0+".csv";
	}

	/**
	 * このシミュレーションが使用するParameterをParameterManagerに登録する。
	 * */
	@Override
	public void createParameters(){
		super.createParameters();

		final ParameterChecker defchecker = new DefaultParameterChecker();

		final Parameter
			重さ = new Parameter("質点諸元", "重さ", "重さ", "0 kg", null, defchecker),
			初期位置x = new Parameter("質点諸元", "初期位置x", "初期位置x", null, null, defchecker),
			初期位置y = new Parameter("質点諸元", "初期位置y", "初期位置y", null, null, defchecker),
			初期速度u = new Parameter("質点諸元", "初期速度(ノルム)u", "初期速度(ノルム)u", null, null, defchecker),
			進行時間 = new Parameter("一般", "進行時間", "進行時間", "0 s", "1000 s", defchecker),
			時間差分 = new Parameter("一般", "時間差分dt", "時間差分dt", "0 s", "10 s", defchecker),
			射角分割数 = new Parameter("質点諸元", "射角分割数", "射角分割数", "1", null, defchecker);

		paraMana.addParameter(重さ);
		paraMana.addParameter(初期位置x);
		paraMana.addParameter(初期位置y);
		paraMana.addParameter(初期速度u);
		paraMana.addParameter(進行時間);
		paraMana.addParameter(時間差分);
		paraMana.addParameter(射角分割数);


		this.parameterSetterFuncList.add(()->{
			Function<Parameter,Double> getDoubleValue = (parameter) -> new PhysicalQuantity(parameter.getValue()).Number;

			double
				m,	//質量
				x0,	//初期位置x
				y0, //初期位置y
				u0, //初期速度u(ノルム)
				tn,	//進行時間
				dt; //時間差分
			int
				N_theta0; //射角分割数

			m = getDoubleValue.apply(重さ);
			x0 = getDoubleValue.apply(初期位置x);
			y0 = getDoubleValue.apply(初期位置y);
			u0 = getDoubleValue.apply(初期速度u);
			tn = getDoubleValue.apply(進行時間);
			dt = getDoubleValue.apply(時間差分);

			this.staparams = new StaticParameters(m, x0, y0, u0, tn, dt);


			//----------------------------------------------------------------
			N_theta0 = Integer.valueOf(射角分割数.getValue());
			double theta0[] = new double[N_theta0];
			for(int i=1;i<=N_theta0;i++) {
				theta0[i-1] = 360.0/N_theta0 *i;
			}

			this.dynparamscomb = new DynamicParametersCombinations(theta0);

			//----------------------------------------------------------------

			return new String[] {};
		});
	}



















}
