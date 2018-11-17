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
	 * これを前提として計算を行います。ctrlの第2のインデックスは各コントロールポイントの重みと座標
	 * を保持します。ただしこのうち、1番目の要素は重みであり、正の数を必ず指定してください。
	 * ここで全て1を指定した場合、Bスプラインに対応します。2番目以降がポイントの座標になります。
	 * 最低でも2番目の要素を指定してください。
	 *
	 * @param ctrl コントロールポイントを指定する。
	 * @param ctrlNum 各方向のコントロールポイントの数を指定する。
	 * @param pro このNURBS関数が必要とするノットベクトルを表すNURBSProperty
	 */
	public NURBSFunction(double[][] ctrl, int[] ctrlNum, NURBSProperty pro) {
		//ctrlの要素数はpro.knotとpro.pとつじつまがあうのか
		for(int i=0;i<pro.parameterNum;i++) {
			if(pro.knot[i].length != ctrlNum[i]+pro.p[i]+1) {
				throw new IllegalArgumentException(
					"コントロールポイントの数とノットベクトルの要素数と次数のつじつまが合いません"+
					":pro.knot["+i+"].length != ctrlNum["+i+"]+pro.p["+i+"]+1"
				);
			}
		}

		//ctrlの各要素の成分の数は一定になっているのか、
		//重み1+次元dになっているのか
		//d!=0ではないか
		//重みw>0か


		this.ctrl = ctrl;
		this.property = pro;
	}

	/**
	 * 変数値を引数で指定し、その点でのNURBS関数の値を計算します。
	 * @param t 変数値
	 * @return 関数値
	 */
	public double[] value(double... t){
		//t.lengthはproperty.parameterNumと一致するのか
		//tはNURBS関数の定義域に反していないか
		//tの要素の内、それに対応するノットの最後端に等しかった場合の特別な対応
		return null;
	}

	/**
	 * k法によるコントロールポイントの変更を行います。
	 * ただし、このメソッドは登録してあるNURBSPropertyインスタンスから呼び出される
	 * ものであり、利用者は明示的に呼び出さないでください。
	 */
	protected void kmethod() {}
}
