package parabolicmovement;

public class Result {
	private final double t[];
	private final double x[];
	private final double y[];
	private final double u[];
	private final double v[];
	public final int lineNum;

	public Result(double[] t, double[] x, double[] y, double[] u, double[] v) {
		this.t = t;
		this.x = x;
		this.y = y;
		this.u = u;
		this.v = v;
		lineNum = t.length;
	}

	/**
	 * 0 <= line < lineNum
	 * @return 範囲外:null,範囲内:該当する行の値の文字列
	 * */
	public String lineString(int line) {
		if(line < 0 || lineNum <= line) {
			return null;
		}
		String linestr = t[line]+","+x[line]+","+y[line]+","+u[line]+","+v[line];
		return linestr;
	}

	public String headerString() {
		return "t[s],x[m],y[m],u[m/s],v[m/s]";
	}

}
