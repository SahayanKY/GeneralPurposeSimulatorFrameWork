package simulation.function.nurbs.assertion;

import simulation.function.nurbs.NURBSBasisFunction;

/**
 * NURBS関係のassertionを行うクラス。
 * @version 2019/02/23 18:01
 * */
public class NURBSAsserter {
	/**
	 * 異常が検知された場合に、例外をスローするかどうかを定めます。
	 * trueの場合、スローします。
	 * @version 2019/02/23 18:02
	 * */
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
	 * @param basis 基底関数
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
		//指定された変数の数は想定している変数の数に一致しているか
		if(t.length != basis.parameterNum) {
			if(assertion) {
				throw new IllegalArgumentException("変数の数が要求される数"+basis.parameterNum+"に合いません:"+t.length);
			}
			return false;
		}

		double[][] domain = basis.giveDomain();

		//tはNURBS関数の定義域に反していないか
		for(int i=0;i<basis.parameterNum;i++) {
			//各変数について対応のノットベクトルの範囲の中にあるかを調べる
			if(t[i] < domain[i][0] || domain[i][1] < t[i]) {
				if(assertion) {
					throw new IllegalArgumentException("指定された変数値t["+i+"]はノットベクトルの範囲を超えています");
				}
				return false;
			}
		}
		return true;
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
					return false;
				}
			}

			//各変数のノットベクトルは単調増加列になっているのか
			if(knot[j]>knot[j+1]) {
				if(assertion) {
					throw new IllegalArgumentException("ノットベクトルが単調増加列でありません:knot["+j+"]>knot["+j+1+"]");
				}
				return false;
			}
		}

		return true;
	}



	/**
	 * 挿入するノットとして指定された配列が条件を満たしているかを調べます。
	 * Xを挿入する事による関数の不連続化の可能性については調べません。
	 *
	 * @param basis 基底関数
	 * @param X 挿入したいノットベクトル
	 * @return 挿入するノットとして適切な場合true
	 * @throws IllegalArgumentException
	 * <ul>
	 * 		<li>Xに含まれる値が、基底関数の定義域外であった場合
	 * 		<li>Xが単調増加列の配列の配列で無かった場合
	 * 		<li>Xの要素数がbasisの変数の数に一致しない場合。
	 * </ul>
	 * @version 2019/02/23 10:58
	 * */
	public boolean assertInsertedKnotVectorIsValid(NURBSBasisFunction basis, double[][] X) {
		if(X.length != basis.parameterNum) {
			if(assertion) {
				throw new IllegalArgumentException("基底関数の数と挿入するベクトルの数が合いません");
			}
			return false;
		}

		double[][] domain = basis.giveDomain();

		for(int varNum=0;varNum<basis.parameterNum;varNum++) {
			if(X[varNum].length == 0) {
				continue;
			}

			if(X[varNum][0] <= domain[varNum][0] ||
					domain[varNum][1] <= X[varNum][X[varNum].length-1]) {
				if(assertion) {
					throw new IllegalArgumentException("挿入するベクトルの一部が基底関数の定義域外です");
				}
				return false;
			}

			for(int j=1; j<X[varNum].length; j++) {
				if(X[varNum][j-1] > X[varNum][j]) {
					if(assertion) {
						throw new IllegalArgumentException("単調増加列でありません");
					}
					return false;
				}
			}


		}

		return true;

	}


}
