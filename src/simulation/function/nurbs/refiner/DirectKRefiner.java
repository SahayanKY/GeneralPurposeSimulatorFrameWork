package simulation.function.nurbs.refiner;

import simulation.function.nurbs.NURBSBasisFunction;

/**
 * k法によりNURBSを精細化するクラス。
 * */
@Deprecated
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
	 * <li>q,addedKnotの要素数がbasisの変数の数に一致しない場合。
	 * </ul>
	 * */
	@Override
	public void kRefinement(NURBSBasisFunction basis, int[] q, double[][] addedKnot) {
		for(int ivar=0;ivar<basis.parameterNum;ivar++) {
			//1つの変数のノットベクトルに対して順にノットを挿入する
			/*
			 * //追加する次数が負数になっていないか
			if(q[i]<0) {
				throw new IllegalArgumentException("次数q["+i+"]が負数です");
			}

			//追加ノットが元のノット範囲に収まっているのか
			if(addedKnot[i][0]<=basis.knot[i][0] ||
					basis.knot[i][basis.knot[i].length-1] <= addedKnot[i][addedKnot[i].length-1]) {
				throw new IllegalArgumentException("追加するノットaddedKnot["+i+"]が元のノットの範囲外に広がっています。");
			}
			if(addedj!=addedKnot[i].length && addedKnot[i][addedj-1]>addedKnot[i][addedj]) {
						throw new IllegalArgumentException("追加ノットaddedKnot["+i+"]は単調増加列でありません");
			}

				if(multi>basis.p[i]+q[i] && tempNewKnot[totalj]!=basis.knot[i][0] && tempNewKnot[totalj]!=basis.knot[i][basis.knot[i].length-1]) {
					throw new IllegalArgumentException("ノットを挿入し過ぎです:"+tempNewKnot[totalj]);
				}
			 * */

		}
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

		NURBSBasisFunction basis = new NURBSBasisFunction(new double[][] {knot}, new int[] {2}, weight);

		basis = refineKnot(basis,X);
		for(int index=0;index<14;index++) {
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
	 * @param basis 元の基底関数
	 * */
	private static NURBSBasisFunction refineKnot(NURBSBasisFunction basis, double[] X) {
		double[] knot = basis.knot[0];
		int p = basis.p[0];

		double[] bKnot = new double[knot.length+X.length];
		double[] bWeight = new double[basis.weight.length+X.length];

		int k_bef=-1,i_U=0,i_bU=0;

		for(int i_X=0;i_X<X.length;i_X++) {
			for(;knot[i_U]<=X[i_X];i_U++,i_bU++) {
				bKnot[i_bU] = knot[i_U];
			}
			bKnot[i_bU] = X[i_X];
			int k_now = i_bU -1;
			i_bU++;

			//--------------------------------------------

			for(int j=k_bef+1 ; j<=k_now ; j++) {
				bWeight[j] = basis.weight[j-i_X];
			}

			//---------------------------------------------

			for(int j=k_now,jj=i_U+p-1 ; j>=k_now-p+1 ; j--,jj--) {
				double alpha = (X[i_X] -bKnot[j])/(knot[jj] -bKnot[j]);
				bWeight[j] = alpha*bWeight[j] +(1-alpha)*bWeight[j-1];
			}

			//---------------------------------------------

			k_bef = k_now;

		}

		for(int i=k_bef+1;i<bWeight.length;i++) {
			bWeight[i] = basis.weight[i-X.length];
		}

		for(;i_U<knot.length;i_U++,i_bU++) {
			bKnot[i_bU] = knot[i_U];
		}



		/*
		 * ノット挿入で変化させないといけないもの
		 * NURBSBasisFunction
		 * ・knot(double[][]):ノットベクトル
		 * ・weight(double[]):重み
		 * ・Pi_n(int[]):ポイント数に依存するため
		 *
		 * NURBSFunction
		 * ・ctrl(double[][]):コントロールポイント
		 * */

		/*
		 * 次数上げで変化させないといけないもの
		 * NURBSBasisFunction
		 * ・knot:ノットベクトル
		 * ・weight:重み、ノットベクトルがかわるので
		 * ・p:次数
		 * ・Pi_p:次数に依存するため
		 * ・effCtrlNum:次数に依存するため
		 * ・Pi_n:ポイント数が変わるため
		 *
		 * NURBSFunction
		 * ・ctrl:コントロールポイント
		 * */



		return null;
	}


	/*
	 * /**
	 * ノット挿入の仮実装
	 * @param X 挿入するノット、元のノットベクトルの範囲内である必要がある。
	 * @param knot 挿入先のノットベクトル
	 * @param ctrls コントロールポイントPw
	 * *//*
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
	 *
	 * */










}
