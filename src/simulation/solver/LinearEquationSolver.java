package simulation.solver;

public abstract class LinearEquationSolver {
	/*trueの場合配列を変化させる*/
	protected boolean changeArray = true;

	/*
	 * このソルバーが計算の際に指定された配列の改変を許すかどうかを指定します。
	 * この指定によっては、配列変更を許した場合指定された配列上で計算処理を行うため、
	 * メモリの節約となるためです。デフォルト値はtrueです。
	 * trueを指定した場合、計算に指定した配列の要素を後から変更するとソルバーによっては
	 * 以降正しく機能しない場合があります。その可能性のあるソルバーについては
	 * 各ソルバー実装クラスのsolve(double[][],double[])のjavadocに記載しています。
	 * @param change trueの場合、配列は変化します。falseの場合、配列は変化しません。
	 * */
	public final void changeArray(boolean change) {
		this.changeArray = change;
	}

	/*
	 * このソルバーが入力された配列を変化させるのかを示す値を返します。
	 * @return trueのとき、配列を変化させます。
	 * */
	public final boolean isToChangeArray() {
		return this.changeArray;
	}

	/*
	 * 引数に指定された行列、右辺項ベクトルを持つ線形方程式を解きます。
	 * 解く線形方程式は解が一意に求まるものに限られます。
	 * 解法実装クラスによっては、解が一意に決定しなかった場合でもエラーなしに
	 * 解が返される可能性があります。その点については各子クラスのjavadocを参照
	 * してください。
	 * @throws IllegalArgumentException 必要な係数行列や右辺項ベクトルが与えら
	 * れていなかったり、それらの行数が一致していない場合。また、計算の途中で
	 * 解が破綻した場合にスローされます。
	 * */
	public abstract double[] solve(double[][] A, double[] B);

	/*
	 * 解析に指定された係数行列と右辺項ベクトルの行数、列数が一致しているかを
	 * チェックします。
	 * 指定された係数行列が正則かどうかを調べるものではありません。
	 * @param a 係数行列
	 * @param b 右辺項ベクトル
	 * @return 行数、列数が一致していない場合、falseを返します。
	 * */
	protected boolean matrixIsNormal(double[][] a, double[] b) {
		boolean result = true;
		int n = a.length;
		for(double[] ai:a) {
			if(ai.length != n) {
				result = false;
				break;
			}
		}
		if(b.length != n) {
			result = false;
		}

		return result;
	}

}
