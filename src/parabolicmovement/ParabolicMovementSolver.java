package parabolicmovement;


/**
 * 放物運動の計算を行うクラス
 * コンストラクタにて、計算条件を
 * */
public class ParabolicMovementSolver {
	private final StaticParameters staparams;
	private final DynamicParameters dynparams;


	/**
	 * コンストラクタ
	 * */
	public ParabolicMovementSolver(StaticParameters staparams, DynamicParameters dynparams) {
		this.staparams = staparams;
		this.dynparams = dynparams;
	}

	public Result solve() {
		final double
			m = staparams.m,	//質量
			x0 = staparams.x0,	//初期位置x
			y0 = staparams.y0, //初期位置y
			U0 = staparams.U0, //初期速度u(ノルム)
			tn = staparams.tn,	//進行時間
			dt = staparams.dt;	//時間差分
		final double theta0 = dynparams.theta0; //射角[°]

		final double u0 = U0*Math.cos(theta0/180*Math.PI); //初期速度x成分
		final double v0 = U0*Math.sin(theta0/180*Math.PI); //初期速度y成分

		final int n = (int)(tn/dt);

		double[]
			t = new double[n+1],
			x = new double[n+1],
			y = new double[n+1],
			u = new double[n+1],
			v = new double[n+1];

		t[0] = 0;
		x[0] = x0;
		y[0] = y0;
		u[0] = u0;
		v[0] = v0;

		for(int i=1;i<=n;i++) {
			v[i] = v[i-1] - 9.8*dt;
			u[i] = u[i-1];
			x[i] = x[i-1] + (u[i]+u[i-1])/2*dt;
			y[i] = y[i-1] + (v[i]+v[i-1])/2*dt;
			t[i] = dt * i;
		}

		return new Result(t, x, y, u, v);
	}


}
