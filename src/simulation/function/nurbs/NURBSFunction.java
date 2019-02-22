package simulation.function.nurbs;

import simulation.function.nurbs.assertion.NURBSAsserter;

/**
 * <p>NURBSによる関数。</p>
 * <p>
 * NURBSBasisFunctionインスタンスをNURBS基底関数とする関数インスタンスを表す。
 * 具体的には、コントロールポイントを保持しています。
 * </p>
 * */
public class NURBSFunction {

	/**
	 * 重み付きのコントロールポイント。<br>
	 * w_{i_0,i_1,...,i_{m-2},i_{m-1}} P_{i_0,i_1,...,i_{m-2},i_{m-1}}は
	 * ctrl[i_{m-1}+i_{m-2}*n_{m-1}+...+i_0*n_1*...*n_{m-1}]に格納されており、
	 * i_{k}=0,1,...,n_{k}-1が有効です。(n_{k}は変数k方向のポイント数)
	 *
	 * @version 2019/02/23 0:22
	 * */
	private final double[][] ctrl;

	/**
	 * この関数インスタンスの基底関数組
	 * @version 2019/02/23 0:35
	 * */
	private final NURBSBasisFunction basis;
	public NURBSBasisFunction getBasisFunction() {
		return this.basis;
	}

	/**
	 * このインスタンスが扱う関数値の次元数。
	 * コントロールポイントの次元に等しい。
	 * @version 2019/02/23 0:36
	 * */
	public final int dimension;

	/**
	 * <p>
	 * TODO ctrlをディープコピーさせる
	 * </p>
	 *
	 * NURBS関数をインスタンス化させます。
	 * コントロールポイントは多変数NURBSの場合注意が必要です。
	 * mを変数の数、Pをコントロールポイント、n_iを変数x_i方向のポイントの数としたとき、
	 * <ul>
	 * 	<li>P_{0,0,...,0,0}はctrl[0]に格納される。
	 * 	<li>P_{0,0,...,0,n_{m-1}-1}はctrl[n_{m-1}-1]に格納される。
	 * 	<li>P_{0,0,...,1,0}はctrl[n_{m-1}]に格納される。
	 * 	<li>P_{i_0,i_1,...,i_{m-2},i_{m-1}}はctrl[i_{m-1}+i_{m-2}*n_{m-1}+...+i_0*n_1*...*n_{m-1}]に格納される。
	 * </ul>
	 * これを前提として計算を行います。ctrlの第2のインデックスは各コントロールポイントの座標
	 * (x座標,y座標,z座標など)を保持します。
	 *
	 * @param ctrl コントロールポイントを指定する。
	 * @param basis このNURBS関数が必要とする基底関数組
	 * @throws NullPointerException ctrlまたはbasisがnullの場合
	 * @throws IllegalArgumentException
	 * <ul>
	 * 		<li>指定されたコントロールポイントと基底関数の重みの数が一致しない場合
	 * 		<li>コントロールポイントの次元が0の場合
	 * 		<li>全てのコントロールポイントの次元が同一でない場合
	 * </ul>
	 * @version 2019/02/23 0:38
	 */
	public NURBSFunction(double[][] ctrl, NURBSBasisFunction basis) {
		//nullチェック
		if(ctrl == null) {
			throw new NullPointerException("引数ctrlがnullです");
		}else if(basis == null) {
			throw new NullPointerException("引数basisがnullです");
		}


		if(ctrl.length != basis.getNumberOfAllCtrl()) {
			throw new IllegalArgumentException("コントロールポイントの数が重みの数に一致しません");
		}

		//ctrlの各要素の成分の数は一定になっているのか、
		//d!=0か
		dimension = ctrl[0].length;
		if(dimension == 0) {
			throw new IllegalArgumentException("コントロールポイントの要素数が足りません:次元d(>0)");
		}
		this.ctrl = new double[ctrl.length][dimension];
		double[] weight = basis.getWeightArray();
		for(int i=0;i<ctrl.length;i++) {
			if(dimension != ctrl[i].length) {
				throw new IllegalArgumentException("コントロールポイントctrl["+i+"]に次元数の過不足があります");
			}

			//先に重みを各座標に掛け合わせておく
			for(int d=0;d<dimension;d++) {
				this.ctrl[i][d] = weight[i] *ctrl[i][d];
			}
		}

		this.basis = basis;
	}

	/**
	 * 変数値を引数で指定し、その点でのNURBS関数の値を計算します。
	 * @param t 変数値
	 * @return 関数値
	 */
	public double[] value(double... t){
		//定義域に反していないかをチェック
		NURBSAsserter asserter = new NURBSAsserter(true);
		asserter.assertVariableIsValid(this.basis, t);

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
	 * この関数インスタンスの基底関数が指定された基底関数と同値かどうかを返します。
	 * 同値、即ち、同じ基底関数インスタンスかどうかを比較します。
	 * @return 同値の場合、true
	 * @version 2019/02/23 1:30
	 * */
	public boolean basisFunctionIs(NURBSBasisFunction basis) {
		return this.basis == basis;
	}

}
