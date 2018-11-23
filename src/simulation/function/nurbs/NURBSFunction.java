package simulation.function.nurbs;

public class NURBSFunction {

	/**コントロールポイント。具体的な中身はコンストラクタを参照*/
	protected final double[][] ctrl;

	/**このインスタンスが必要とするノットベクトルや次数からなる基底関数組*/
	private final NURBSBasisFunction basis;

	/**このインスタンスが扱う関数値の次元数*/
	private final int dimension;



	/**
	 * NURBS関数をインスタンス化させます。
	 * コントロールポイントは多変数NURBSの場合注意が必要です。
	 * mを変数の数、Pをコントロールポイント、n_iを変数i方向のポイントの数としたとき、
	 * <ul>
	 * 	<li>P_{0,0,...,0,0}はctrl[0]に格納される。
	 * 	<li>P_{0,0,...,0,n_{m-1}-1}はctrl[n_{m-1}-1]に格納される。
	 * 	<li>P_{0,0,...,1,0}はctrl[n_{m-1}]に格納される。
	 * 	<li>P_{i_0,i_1,...,i_{m-2},i_{m-1}}はctrl[i_{m-1}+i_{m-2}*n_{m-1}+...+i_0*n_1*...*n_{m-1}]に格納される。
	 * </ul>
	 * これを前提として計算を行います。ctrlの第2のインデックスは各コントロールポイントの座標
	 * を保持します。
	 *
	 * @param ctrl コントロールポイントを指定する。
	 * @param ctrlNum 各方向のコントロールポイントの数を指定する。
	 * @param basis このNURBS関数が必要とする基底関数組
	 */
	public NURBSFunction(double[][] ctrl, NURBSBasisFunction basis) {
		//nullチェック
		if(ctrl == null) {
			throw new IllegalArgumentException("引数ctrlがnullです");
		}else if(basis == null) {
			throw new IllegalArgumentException("引数basisがnullです");
		}


		if(ctrl.length != basis.weight.length) {
			throw new IllegalArgumentException("コントロールポイントの数が重みの数に一致しません");
		}

		//ctrlの各要素の成分の数は一定になっているのか、
		//d!=0か
		dimension = ctrl[0].length;
		if(dimension == 0) {
			throw new IllegalArgumentException("コントロールポイントの要素数が足りません:次元d(>0)");
		}
		for(int i=0;i<ctrl.length;i++) {
			double[] monoctrl=ctrl[i];
			if(dimension != monoctrl.length) {
				throw new IllegalArgumentException("コントロールポイントctrl["+i+"]に次元数の過不足があります");
			}

			//先に重みを各座標に掛け合わせておく
			for(int d=0;d<dimension;d++) {
				monoctrl[d] *= basis.weight[i];
			}
		}

		basis.registerNURBSFunction(this);

		this.ctrl = ctrl;
		this.basis = basis;
	}

	/**
	 * 変数値を引数で指定し、その点でのNURBS関数の値を計算します。
	 * @param t 変数値
	 * @return 関数値
	 */
	public double[] value(double... t){
		//定義域に反していないかをチェック
		basis.assertVariableIsValid(true,t);

		//各変数についてt_k <= t < t_k+1となるようなkをさがす
		int[] k = NURBSCalculater.searchVariablesPosition_InKnotVectors(basis, t);

		//以降deBoorアルゴリズムの通り
		//Q[][0]:重み
		//Q[][1]以降:重み*座標値
		double Q[][] = NURBSCalculater.restrictControlPoint(k, basis, this);

		//4つの入れ子ループ部分へ
		//loopResult[0]:重みの足し合わせ結果
		//loopResult[1]以降:重み*座標値の足し合わせ結果
		double[] loopResult = NURBSCalculater.deBoorsLoop(t, k, Q, basis);

		/*loopResultには{重みの足し合わせ、座標1*重みの足し合わせ、...}が入っているため、
		 * loopResult[0]で残りの要素を割り、その残りの要素を結果として出さなければならない
		 * (NURBSの特徴)
		 * */
		return NURBSCalculater.processWeight(loopResult);
	}

	/**
	 * k法によるコントロールポイントの変更を行います。
	 * ただし、このメソッドは登録してあるNURBSBasisFunctionインスタンスから呼び出される
	 * ものであり、利用者は明示的に呼び出さないでください。
	 */
	protected void kmethod() {}
}
