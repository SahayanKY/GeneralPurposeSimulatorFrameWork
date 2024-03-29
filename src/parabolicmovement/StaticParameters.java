package parabolicmovement;

/**
 * 計算条件(射角等)が変化しても、変わらないパラメータ値のクラス
 * */
public class StaticParameters {

	public final double
		m,	//質量
		x0,	//初期位置x
		y0, //初期位置y
		U0, //初期速度u(ノルム)
		tn,	//進行時間
		dt;	//時間差分


	public StaticParameters(double m, double x0, double y0, double U0, double tn, double dt) {
		this.m = m;
		this.x0 = x0;
		this.y0 = y0;
		this.U0 = U0;
		this.tn = tn;
		this.dt = dt;
	}

}
