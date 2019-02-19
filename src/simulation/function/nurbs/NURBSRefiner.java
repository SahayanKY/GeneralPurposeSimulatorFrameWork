package simulation.function.nurbs;

import java.util.ArrayList;

public class NURBSRefiner {

	/**
	 * 指定されたNURBS基底関数に対して、ノットを挿入します。
	 * 返されるインスタンスは指定されたものとは異なる参照をもちます。
	 * また、返されるNURBSFunctionインスタンスの順番は、指定された順番と対応しています。
	 *
	 * @param group NURBS基底関数とコントロールポイントの組
	 * @param X 各変数方向のノットベクトルに挿入するノット配列
	 *
	 * @throws IllegalArgumentException
	 * <ul>
	 * 		<li>Xに含まれる値が、基底関数の定義域外であった場合
	 * 		<li>Xを追加することによって関数の連続性が保持されない場合
	 * 		<li>Xが単調増加列の配列の配列で無かった場合
	 * 		<li>Xの要素数がbasisの変数の数に一致しない場合。
	 * </ul>
	 * */
	public NURBSFunctionGroup refineKnot(NURBSFunctionGroup group, double[][] X) {
		//Xが正常かどうかを調べる
		assertInsertedKnotVectorIsValid(group.basis,X);


		//最後に新しくNURBSFunctionインスタンスを作る時に必要となる
		//各Functionのポイントの次元を把握しておく
		ArrayList<Integer> pointDimension = new ArrayList<>();
		pointDimension.add(1);
		for(NURBSFunction f:group.func) {
			pointDimension.add(f.dimension);
		}





		return null;
	}

	/**
	 * ノットを精細化します。
	 *
	 * @param ctrl 基底関数の重み、各NURBS関数のコントロールポイントをまとめたもの
	 * @param NewCtrl 新しいポイントを保存する配列
	 * @param knot 基底関数のノットベクトル
	 * @param X 挿入するノット
	 * @param NewKnot 新しいノットベクトルを保存する配列
	 * @param p 基底関数の次数
	 * */
	private void refineKnot(double[][] ctrl, double[][] NewCtrl, double[][] knot, double[][] X, double[][] NewKnot, int[] p) {

	}


	/**
	 * 挿入するノットとして指定された配列が条件を満たしているかを調べます。
	 * Xを挿入する事による関数の不連続化の可能性については調べません。
	 *
	 * @throws IllegalArgumentException
	 * <ul>
	 * 		<li>Xに含まれる値が、基底関数の定義域外であった場合
	 * 		<li>Xが単調増加列の配列の配列で無かった場合
	 * 		<li>Xの要素数がbasisの変数の数に一致しない場合。
	 * </ul>
	 * */
	private void assertInsertedKnotVectorIsValid(NURBSBasisFunction basis, double[][] X) {
		if(X.length != basis.parameterNum) {
			throw new IllegalArgumentException("基底関数の数と挿入するベクトルの数が合いません");
		}

		for(int varNum=0;varNum<basis.parameterNum;varNum++) {
			double[] parentKnot = basis.knot[varNum];
			double[] childKnot = X[varNum];

			if(X[varNum].length == 0) {
				continue;
			}

			if(childKnot[0] <= parentKnot[0] ||
					parentKnot[parentKnot.length-1] <= childKnot[childKnot.length-1]) {
				throw new IllegalArgumentException("挿入するベクトルの一部が基底関数の定義域外です");
			}

		}
	}


}
