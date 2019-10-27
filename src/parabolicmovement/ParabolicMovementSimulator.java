package parabolicmovement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import analysis.GalerkinConvectionDiffusion;
import icg.PhysicalQuantity;
import simulation.Simulator;
import simulation.param.Parameter;
import simulation.param.checker.DefaultParameterChecker;
import simulation.param.checker.ParameterChecker;
import simulation.system.SystemInfo;

public class ParabolicMovementSimulator extends Simulator{

	private StaticParameters staparams;
	private DynamicParametersCombinations dynparamscomb;




	@Override
	protected void executeSimulation() {
		ParabolicMovementSolver solver;
		DynamicParameters dynparams;

		//スレッドプールの立上げ
		ExecutorService exec = Executors.
		while((dynparams = dynparamscomb.getNextDynamicParameters())!= null) {
			solver = new ParabolicMovementSolver(staparams,dynparams);
/**
 *
 * 		//スレッドプールの立上げ
//		ExecutorService exec = Executors.newWorkStealingPool();
		ExecutorService exec = Executors.newSingleThreadExecutor();
		List<Future<?>> list = new ArrayList<>();

		for(int i=0; i<narray.length;i++) {
			for(int j=0;j<karray.length;j++) {
				Future<String> f =
					exec.submit(new Callable<String>() {
						int n;
						double k;

						@Override
						public String call() {
							new GalerkinConvectionDiffusion(n,k).analyze();
							return "";
						}

						public Callable<String> setParam(int n,double k) {
							this.n = n;
							this.k = k;
							return this;
						}
					}.setParam(narray[i],karray[j]));
				list.add(f);
			}
		}

		//タスク受け取りの終了
		exec.shutdown();

		//タスク完了の待機
		for(Future<?> f:list) {
			try {
				f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
 * */

		}
	}

	@Override
	public String getThisName() {
		return "放物運動シミュレーション";
	}

	@Override
	public String getThisVersion() {
		return "0.1-20191027";
	}

	/**
	 * このシミュレーションが使用するParameterをParameterManagerに登録する。
	 * */
	@Override
	public void createParameters(){
		super.createParameters();

		final ParameterChecker defchecker = new DefaultParameterChecker();

		String mincore = "0", maxcore = SystemInfo.CPU_CORE_NUM+"";

		final Parameter
			重さ = new Parameter("質点諸元", "重さ", "重さ", "0 kg", null, defchecker),
			初期位置x = new Parameter("質点諸元", "初期位置x", "初期位置x", null, null, defchecker),
			初期位置y = new Parameter("質点諸元", "初期位置y", "初期位置y", null, null, defchecker),
			初期速度u = new Parameter("質点諸元", "初期速度(ノルム)u", "初期速度(ノルム)u", null, null, defchecker),
			進行時間 = new Parameter("一般", "進行時間", "進行時間", "0 s", "1000 s", defchecker),
			時間差分 = new Parameter("一般", "時間差分dt", "時間差分dt", "0 s", "10 s", defchecker),
			射角分割数 = new Parameter("質点諸元", "射角分割数", "射角分割数", "1", null, defchecker),
			並列数 = new Parameter("一般", "並列数", "並列数", mincore, maxcore, defchecker);

		paraMana.addParameter(重さ);
		paraMana.addParameter(初期位置x);
		paraMana.addParameter(初期位置y);
		paraMana.addParameter(初期速度u);
		paraMana.addParameter(進行時間);
		paraMana.addParameter(時間差分);
		paraMana.addParameter(並列数);


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
				parallelnum, //並列数
				N_theta0; //射角分割数

			m = getDoubleValue.apply(重さ);
			x0 = getDoubleValue.apply(初期位置x);
			y0 = getDoubleValue.apply(初期位置y);
			u0 = getDoubleValue.apply(初期速度u);
			tn = getDoubleValue.apply(進行時間);
			dt = getDoubleValue.apply(時間差分);
			parallelnum = Integer.valueOf(並列数.getValue());

			this.staparams = new StaticParameters(m, x0, y0, u0, tn, dt,parallelnum);


			//----------------------------------------------------------------
			N_theta0 = Integer.valueOf(射角分割数.getValue());
			double theta0[] = new double[N_theta0];
			for(int i=1;i<=720;i++) {
				theta0[i-1] = 360.0/N_theta0 *i;
			}

			this.dynparamscomb = new DynamicParametersCombinations(theta0);

			//----------------------------------------------------------------

			return new String[] {};
		});
	}


















}
