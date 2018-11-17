package simulation.function.nurbs;

public class NURBSFunction {
	private double[][] ctrl;
	private final NURBSProperty property;

	/**
	 * NURBS関数をインスタンス化させます。
	 * コントロールポイントは多変数NURBSの場合注意が必要です。
	 * mを変数の数、Pをコントロールポイント、n_iを変数i方向のポイントの数としたとき、
	 * P_{0,0,...,0,0}はctrl[0]に格納される。
	 * P_{0,0,...,0,n_{m-1}-1}はctrl[n_{m-1}-1]に格納される。
	 * P_{0,0,...,1,0}はctrl[n_{m-1}]に格納される。
	 * P_{i_0,i_1,...,i_{m-2},i_{m-1}}はctrl[i_{m-1}+i_{m-2}*n_{m-1}+...+i_0*n_1*...*n_{m-1}]に格納される。
	 * これを前提として計算を行います。ctrlの第2のインデックスはコントロールポイント
	 * がベクトル値だった場合にその成分をもつのに利用します。
	 *
	 * @param ctrl コントロールポイントを指定する。
	 * @param ctrlNum 各方向のコントロールポイントの数を指定する。
	 * @param property このNURBS関数が必要とするノットベクトルを表すNURBSProperty
	 */
	public NURBSFunction(double[][] ctrl, int[] ctrlNum, NURBSProperty property) {
		//次数は設定させないのか？
		//ctrlの要素数はproperty.knotとつじつまがあうのか
		this.ctrl = ctrl;
		this.property = property;
	}

	/**
	 * 変数値を引数で指定し、その点でのNURBS関数の値を計算します。
	 * @param t 変数値
	 * @return 関数値
	 */
	public double[] value(double... t){
		//t.lengthはproperty.parameterNumと一致するのか
		return null;
	}

	/**
	 * k法によるコントロールポイントの変更を行います。
	 * ただし、このメソッドは登録してあるNURBSPropertyインスタンスから呼び出される
	 * ものであり、利用者は明示的に呼び出さないでください。
	 */
	protected void kmethod() {}
}
