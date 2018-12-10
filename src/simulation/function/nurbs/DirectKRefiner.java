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
	 * @author SahayanKY
	 * @param basis NURBS基底関数
	 * @param q 増やしたい次数
	 * @param addedKnot 追加したいノット。各配列は単調増加列であるとこを前提とします。
	 * @throws IllegalArgumentException
	 * <ul>
	 * <li>addedKnotを追加することによって関数の連続性が保持されない場合。
	 * <li>addedKnotの最小値、最大値が元のノット範囲外にあるとき。
	 * <li>addedKnotが単調増加列の配列の配列で無かった場合。
	 * <li>qの各要素のうちいずれかが負数であった場合。
	 * </ul>
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
		/*
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
		 *

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
		 *


		for(int i=0;i<basis.parameterNum;i++) {
			for(double d:refiner.newKnot[i]) {
				System.out.println(d);
			}
			System.out.println("--------------------------------------");
		}
		*/

		double[] X = {1,1,3,4,9};
		double[] knot = {0,0,0,2,2,4,5,8,9,10,10,10};
		double[] weight = {1,1,1,1,1,1,1,1,1};

		NURBSBasisFunction basis = refineKnot(X,knot,weight,2);
		for(int index=6;index<14;index++) {
			for(int i=0;i<=100;i++) {
				double t = (i==0)? knot[0] : (i==100)? knot[knot.length-1]: knot[0]+(knot[knot.length-1]-knot[0])*i/100.0;
				System.out.println(basis.value(new int[] {index}, new double[] {t}));
			}
			System.out.println("");
		}
	}

	/**
	 * ノット挿入の仮実装
	 * @param X 挿入するノット、元のノットベクトルの範囲内である必要がある。
	 * @param knot 挿入先のノットベクトル
	 * @param ctrls コントロールポイントPw
	 * */
	private static NURBSBasisFunction refineKnot(double[] X, double[] knot, double[] ctrls, int p) {
		double[] bKnot = new double[knot.length+X.length];
		double[] nCtrls = new double[ctrls.length+X.length];
		for(int i=0;i<nCtrls.length;i++) {
			nCtrls[i] = -1;
		}

		if(X[0]<=knot[0] || knot[knot.length-1]<=X[X.length-1]) {
			throw new IllegalArgumentException("追加のノットが条件を満たしていません");
		}

		bKnot[0] = knot[0];

		for(int iu=1, ix=0,ibu=1;ix<X.length;ix++) {
			//X[ix]を頭から1つずつ挿入する

			while(knot[iu]<=X[ix]) {
				bKnot[ibu] = knot[iu];
				nCtrls[ibu-1] = ctrls[ibu-1-ix];
				iu++;
				ibu++;
			}
			bKnot[ibu] = X[ix];
			nCtrls[ibu-1] = ctrls[ibu-1-ix];

			ibu += 0;

			for(int i=ibu-1,j=iu+p-1;i>=ibu-p;i--,j--) {
				//bKnot[ibu-1]<=X[ix]<bKnot[ibu+1]
				//bKnot[ibu+1]以降はまだ存在しないが、
				//これ以降はまだ使っていない元のノット(knot[iu]以降)が続くかのように計算が行われる)
				//(元のノットに、一つ一つノットを追加するように計算をしていくため)

				//<<k>>==bKnot[ibu-1],<<k+1>>==X[ix],<<k+2>>==knot[iu],...

				//k+2<=i+p+1<=k+p+1より、現時点でその位置は元のノットベクトルの続きの部分なので
				//buip1:<<i+p+1>>=knot[iu],knot[iu+1],...,knot[iu+p-1]を参照する

				//k-p+1<=i<=kより、これまで作ってきた新しいノットベクトルbKnotを参照し、
				//bui:<<i>>=knot[ibu-p],knot[ibu-p+1],...,knot[ibu-1]を参照する


				double bui = bKnot[i]; //<<i>>
				double buip1 = knot[j]; //<<i+p+1>>
				double α = (X[ix] -bui)/(buip1 -bui);

				nCtrls[i] = α*(nCtrls[i] -nCtrls[i-1]) +nCtrls[i-1];
			}


			if(ix == X.length-1) {
				//最後、変形が起こらなかったコントロールポイントを追加していく
				for(int i=ibu;i<nCtrls.length;i++) {
					nCtrls[i] = ctrls[i-ix-1];
				}

				//使用されなかった元のノットベクトルも追加していく
				//<<k+2>>,...,=knot[iu],...
				for(int i=iu;i<knot.length;i++) {
					bKnot[ibu+1+i-iu] = knot[i];
				}
			}

			ibu++;

		}


		NURBSBasisFunction basis = new NURBSBasisFunction(new double[][] {bKnot}, new int[] {p}, nCtrls);
		return basis;
	}











}
