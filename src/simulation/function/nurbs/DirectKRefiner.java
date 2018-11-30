package simulation.function.nurbs;

/**
 * k法によりNURBSを精細化するクラス。
 * */
public class DirectKRefiner implements KRefiner {
	double[][] newKnot;

	/**
	 * {@inheritDoc}
	 * <p>
	 * この実装では新しいコントロールポイントを計算するのに、単純に連立方程式を
	 * 解いています。そのため、他の実装に比べて計算時間がかかる可能性があります。
	 * </p>
	 *
	 * @param basis NURBS基底関数
	 * @param q 増やしたい次数
	 * @param addedKnot 追加したいノット。各配列は単調増加列であるとこを前提とします。
	 * @throws IllegalArgumentException
	 * addedKnotを追加することによって関数の連続性が保持されない場合。
	 * addedKnotの最小値、最大値が元のノット範囲外にあるとき。
	 * addedKnotが単調増加列の配列の配列で無かった場合。
	 * qの各要素のうちいずれかが負数であった場合。
	 * */
	@Override
	public void kRefinement(NURBSBasisFunction basis, int[] q, double[][] addedKnot) {

		//新しいノットベクトルの生成
		double[][] newKnot = new double[basis.parameterNum][];
		for(int i=0;i<basis.parameterNum;i++) {

			//追加する次数が負数になっていないか
			if(q[i]<0) {
				throw new IllegalArgumentException("次数q["+i+"]が負数です");
			}

			//追加ノットが元のノット範囲に収まっているのか
			if(addedKnot[i][0]<=basis.knot[i][0] ||
					basis.knot[i][basis.knot[i].length-1] <= addedKnot[i][addedKnot[i].length-1]) {
				throw new IllegalArgumentException("追加するノットaddedKnot["+i+"]が元のノットの範囲外に広がっています。");
			}

			/*
			 * 新しいノットの長さを知るためには予めbasis.knot[i]のユニークなノットの数が
			 * 分からないといけないため、一時的に最大数を確保しておく。
			*/
			double[] tempNewKnot = new double[basis.knot[i].length*(q[i]+1)+addedKnot[i].length];
			int tempKnotLength=0;

			for(int basisj=0,addedj=0,totalj=0,multi=1;
					basisj<basis.knot[i].length;
					totalj++) {

				boolean addKnotDegreeTimesFlag=false;

				if(addedj<addedKnot[i].length && addedKnot[i][addedj]<basis.knot[i][basisj]) {
					//追加したいノットを追加し、addedKnotのイテレータを進める
					tempNewKnot[totalj] = addedKnot[i][addedj];
					addedj++;

					if(addedj!=addedKnot[i].length && addedKnot[i][addedj-1]>addedKnot[i][addedj]) {
						throw new IllegalArgumentException("追加ノットaddedKnot["+i+"]は単調増加列でありません");
					}

				}else {
					//元のノットベクトル由来のノットを追加

					tempNewKnot[totalj] = basis.knot[i][basisj];
					basisj++;

					if(totalj==0 || tempNewKnot[totalj] != tempNewKnot[totalj-1]) {
						addKnotDegreeTimesFlag = true;
					}
				}

				if(totalj>0 && tempNewKnot[totalj-1] == tempNewKnot[totalj]) {
					multi++;
				}else {
					multi = 1;
				}


				if(addKnotDegreeTimesFlag) {
					/*
					 * tempNewKnotの直前の値と今の値が等しくなく、
					 * "今"は元のノットのユニークな値を1回目に追加しているとき
					 * 元のノットが0,0,0,1,2,2,3,3,3だったら、0、3、4、6番目のとき
					 * なので、増やしたい次数分追加する
					 * */

					totalj++;
					for(int k=0;k<q[i];k++,totalj++) {
						tempNewKnot[totalj] = basis.knot[i][basisj-1];
						//basisjは上での操作で既にインクリメントしてしまっているので-1して
						//参照する
					}
					totalj--;
					multi += q[i];
					//次数の分多重度が増える
				}

				if(multi>basis.p[i]+q[i] && tempNewKnot[totalj]!=basis.knot[i][0] && tempNewKnot[totalj]!=basis.knot[i][basis.knot[i].length-1]) {
					throw new IllegalArgumentException("ノットを挿入し過ぎです:"+tempNewKnot[totalj]);
				}

				tempKnotLength = totalj;
			}

			newKnot[i] = new double[tempKnotLength+1];
			for(int j=0;j<=tempKnotLength;j++) {
				newKnot[i][j] = tempNewKnot[j];
			}
		}

		this.newKnot = newKnot;

		//指定されたaddedKnotやqの変数の数とかは大丈夫かをチェックするように
		//連立方程式を解く

	}

	public static void main(String args[]) {
		double[][] knot = {
				{0,0,0,1,2,2,4,5,6,6,6},
				{0,0,1,2,2},
				{-5,-5,-5,-5,-3,-3,-1,0,0,0,0}
		};

		int[] p = {
			2,1,3
		};

		double[] weight = {
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,
		};

		NURBSBasisFunction basis = new NURBSBasisFunction(knot, p, weight);
		DirectKRefiner refiner = new DirectKRefiner();

		int[] q = {
				2,2,2
		};

		/*
		 * 		{0,0,0,1,2,2,4,5,6,6,6},
				{0,0,1,2,2},
				{-5,-5,-5,-5,-3,-3,-1,0,0,0,0}
		 * */

		double[][] addedKnot = {
				{0.5,0.5,1,3,4,5},
				{0.5,0.7},
				{-4,-3,-2,-1}
		};

		refiner.kRefinement(basis, q, addedKnot);

		//-3,-3,-3,-3,-3,-3

		/*
		 * 0,0,0,0,0,
		 * 0.5,0.5,
		 * 1,1,1,1,
		 * 2,2,2,2,
		 * 3,
		 * 4,4,4,4,
		 * 5,5,5,5,
		 * 6,6,6,6,6
		 *
		 * 0,0,0,0,0.5,0.7,1,1,1,2,2,2,2
		 *
		 *
		 * */


		for(int i=0;i<basis.parameterNum;i++) {
			for(double d:refiner.newKnot[i]) {
				System.out.println(d);
			}
			System.out.println("--------------------------------------");
		}
	}













}
