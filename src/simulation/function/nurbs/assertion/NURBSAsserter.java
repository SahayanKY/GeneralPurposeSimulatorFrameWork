package simulation.function.nurbs.assertion;

import simulation.function.nurbs.NURBSBasisFunction;

public class NURBSAsserter {
	private final boolean assertion;

	/**
	 * NURBSに関するassertionを行うインスタンスを生成します。
	 *
	 * @param assertion trueを指定した場合、もしassertionに引っかかった時に例外をスローするようになります。
	 * @version 2019/02/23 2:57
	 * */
	public NURBSAsserter(boolean assertion) {
		this.assertion = assertion;
	}

	/**
	 * 指定された値が定義域内であるかどうかを判断します。
	 *
	 * @param t 調べる変数値
	 * @return 定義域内だった場合true
	 * @throws IllegalArgumentException
	 * <ul>
	 * 		<li>定義域外を指定した場合
	 * 		<li>変数の数が一致していない場合
	 * </ul>
	 *
	 * @version 2019/02/23 2:57
	 * */
	public boolean assertVariableIsValid(NURBSBasisFunction basis, double... t){
		boolean result = true;

		//指定された変数の数は想定している変数の数に一致しているか
		if(t.length != basis.parameterNum) {
			if(assertion) {
				throw new IllegalArgumentException("変数の数が要求される数"+basis.parameterNum+"に合いません:"+t.length);
			}
			result = false;
		}

		double[][] domain = basis.getDomain();

		//tはNURBS関数の定義域に反していないか
		for(int i=0;i<basis.parameterNum;i++) {
			//各変数について対応のノットベクトルの範囲の中にあるかを調べる
			if(t[i] < domain[i][0] || domain[i][1] < t[i]) {
				if(assertion) {
					throw new IllegalArgumentException("指定された変数値t["+i+"]はノットベクトルの範囲を超えています");
				}
				result = false;
			}
		}
		return result;
	}



	/**
	 * <p>指定された配列がオープンノットベクトルかどうかを調べます。
	 * <p>オープンノットベクトルとは、ベクトルの前後の要素が次数と
	 * 等しいものを指します。
	 *
	 * @param knot 調べる配列
	 * @param p 対応する次数
	 * @return オープンノットベクトルだった場合true
	 * @throws IllegalArgumentException
	 * <ul>
	 * 		<li>単調増加列でない場合
	 * 		<li>オープンノットベクトルで無い場合
	 * </ul>
	 *
	 * @version 2019/02/23 2:57
	 * */
	public boolean assertArrayIsOpenKnotVector(double[] knot, int p) {
		boolean result=true;

		//各ノットベクトルについて
		for(int j=0;j<knot.length-1;j++) {
			//オープンノットベクトルになっているか
			//前後ろの要素がp+1個重なっているか
			//0,1,...,pが同じ、n,...,n+p-1,n+pが同じ(n+p+1 == knot[i].length)
			if(j<p //前のノットについて調べるとき
					||
				knot.length-p-1 <= j //後ろのノットについて調べるとき
			) {
				if(knot[j]!=knot[j+1]) {
					if(assertion) {
						throw new IllegalArgumentException("ノットベクトルがオープンノットベクトルでありません");
					}
					result = false;
				}
			}

			//各変数のノットベクトルは単調増加列になっているのか
			if(knot[j]>knot[j+1]) {
				if(assertion) {
					throw new IllegalArgumentException("ノットベクトルが単調増加列でありません:knot["+j+"]>knot["+j+1+"]");
				}
				result = false;
			}
		}

		return result;
	}


}
