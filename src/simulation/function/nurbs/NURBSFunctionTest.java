package simulation.function.nurbs;

public class NURBSFunctionTest {
	/*
	 * 正常動作テスト
	 * ・関数の形が既知である、ノット、コントロール、次数のセットから得られた
	 * 関数値を既知関数と比較する。
	 *
	 * 例外テスト
	 * ・オープンノットベクトルでない場合
	 * ・ノット、コントロール、次数が一致しない場合
	 * ・定義域外のとき
	 * ・null指定
	 * ・
	 * */
	public static void main(String args[]) {
		double[][] ctrl = {
				{0,0},
				{1,1},
				{0,3}
		};
		double[] weight = {
			1,1,1
		};

		double[][] knot = {
				{0,0,0,1,1,1}
		};


		int[] p = {2};

		NURBSBasisFunction basis = new NURBSBasisFunction(knot, p, weight);
		NURBSFunction func = new NURBSFunction(ctrl, basis);

		for(int i=0;i<=30;i++) {
			double t = (i==0)? knot[0][0] : (i==30)? knot[0][knot[0].length-1]: (knot[0][knot[0].length-1]-knot[0][0])*i/30.0;
			double[] result = func.value(t);
			System.out.print(t);
			for(double f:result) {
				System.out.print("	"+f);
			}
			System.out.println("");
		}

		System.out.println("-------------------------------------------------------------");

		for(int i=0;i<weight.length;i++) {
			for(int j=0;j<=30;j++) {
				double t = (j==0)? knot[0][0] : (j==30)? knot[0][knot[0].length-1]: (knot[0][knot[0].length-1]-knot[0][0])*j/30.0;
				double f = basis.value(new int[] {i}, new double[] {t});
				System.out.println(t+"	"+f);
			}
		}


	}
}
