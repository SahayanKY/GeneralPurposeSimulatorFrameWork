package simulation.function.nurbs;

public class NURBSRefiner {

	/**
	 * 指定されたNURBS基底関数に対して、ノットを挿入します。
	 * 返されるインスタンスは指定されたものとは全く異なる参照をもちます。
	 * また、返されるNURBSFunctionインスタンスの順番は、指定された順番と対応しています。
	 * また、指定されたNURBS関数の状態は変化しません。
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

		NURBSBasisFunction basis = group.basis;
		NURBSFunction[] funcs = group.funcs;

		//元のコントロールポイント数
		int n_All = group.basis.Pi_n[0];
		//新しいコントロールポイントの数
		int Newn_All = 1;
		for(int varNum=0;varNum<basis.parameterNum;varNum++) {
			//X[varNum].length:ある変数方向のポイントの増加数
			Newn_All *= (basis.n[varNum]+X[varNum].length);
		}

		//---------------------------------------------------------------------

		//これから操作するポイントの総座標数を計算する
		int sumDimension=1;
		for(NURBSFunction f:funcs) {
			sumDimension += f.dimension;
		}

		//---------------------------------------------------------------------

		//ctrl:今のポイントの値をまとめたもの
		//NewCtrl:新しいポイントを保持するもの
		//[][0]:重み
		//[][1]:func[0]のポイントの1つめの座標...
		double[][] ctrl = new double[n_All][sumDimension];
		double[][] NewCtrl = new double[Newn_All][sumDimension];

		for(int i=0;i<n_All;i++) {
			ctrl[i][0] = basis.weight[i];
		}
		for(int i_func=0, j=1;i_func<funcs.length;i_func++) {
			for(int d=0;d<funcs[i_func].dimension;d++,j++) {
				for(int i=0;i<n_All;i++) {
					ctrl[i][j] = funcs[i_func].ctrl[i][d];
				}
			}
		}

		//---------------------------------------------------------------------

		//NewKnot:新しいノットを保持するもの
		double[][] NewKnot = new double[basis.parameterNum][];
		for(int varNum=0;varNum<basis.parameterNum;varNum++) {
			NewKnot[varNum] = new double[basis.knot[varNum].length +X[varNum].length];
		}

		//---------------------------------------------------------------------

		//新しいポイントとノットを計算する
		refineKnot(ctrl,NewCtrl,basis.knot,X,NewKnot,basis.p);

		//---------------------------------------------------------------------

		//計算結果から、新しいNURBSBasisFunctionとNURBSFunctionを生成するために、
		//それらのコンストラクタの引数に必要なものを用意する
		int[] Newp = basis.p.clone();
		double[] NewWeight = new double[Newn_All];

		//各NURBSFunctionのポイント毎にまとめたもの
		double[][][] NewCtrlEachFunc = new double[funcs.length][Newn_All][];
		for(int i=0;i<Newn_All;i++) {
			NewWeight[i] = NewCtrl[i][0];
		}
		for(int i_func=0, j=1;i_func<funcs.length;i_func++) {
			for(int d=0;d<funcs[i_func].dimension;d++,j++) {
				for(int i=0;i<n_All;i++) {
					NewCtrlEachFunc[i_func][i][d] = NewCtrl[i][j];

					//ctrl[i][j] = funcs[i_func].ctrl[i][d];
				}
			}
		}

		//---------------------------------------------------------------------
		//NURBSBasisFunctionとNURBSFunctionをインスタンス化し、Groupにまとめて返す
		NURBSBasisFunction NewBasis = new NURBSBasisFunction(NewKnot, Newp, NewWeight);
		NURBSFunction[] NewFuncs = new NURBSFunction[funcs.length];
		for(int i_func=0;i_func<funcs.length;i_func++) {
			NewFuncs[i_func] = new NURBSFunction(NewCtrlEachFunc[i_func], NewBasis);
		}

		return new NURBSFunctionGroup(NewBasis, NewFuncs);
	}


	/**
	 * ノットを精細化します。
	 * ctrlとknotとX、そしてpの状態は変えません。
	 * また、NewCtrlとNewKnotは全て初期化しておいてください。
	 *
	 * @param ctrl 基底関数の重み、各NURBS関数のコントロールポイントをまとめたもの
	 * @param NewCtrl 新しいポイントを保存する配列
	 * @param knot 基底関数のノットベクトル
	 * @param X 挿入するノット
	 * @param NewKnot 新しいノットベクトルを保存する配列
	 * @param p 基底関数の次数
	 *
	 * @throws IllegalArgumentException ノットを挿入した事により関数の不連続化が起こる場合
	 * @throws NullPointerException NewCtrl[*]やNewKnot[*]がnullの場合
	 * */
	private void refineKnot(double[][] ctrl, double[][] NewCtrl, double[][] knot, double[][] X, double[][] NewKnot, int[] p) {
		//変数の数
		final int parameterNum = knot.length;

		for(int l=0;l<parameterNum;l++) {

			double[] Ul = knot[l];
			double[] Xl = X[l];
			double[] bUl = NewKnot[l];

			//X[l]をknot[l]に挿入した結果をNewKnot[l]に代入する
			//NewKnot[l]に挿入結果を作りながら、順次新しいポイントを計算していく
			int k_bef = -1,k_now;
			int i_Ul = 0, i_bUl = 0;
			int n_Xl = Xl.length, n_Ul = Ul.length;
			for(int i_Xl = 0;i_Xl < n_Xl ; i_Xl++) {
				//x_iXまでbUlを完成させる
				for(; Ul[i_Ul] <= Xl[i_Xl] ; i_Ul++,i_bUl++) {
					bUl[i_bUl] = Ul[i_Ul];
				}
				bUl[i_bUl] = Xl[i_Xl];
				k_now = i_bUl -1;
				i_bUl++;

				//---------------------------------------------------------------------

				//x_iXを挿入した事による新しいポイントを計算するのに必要な分だけ、
				//QiにPiを代入する

				for(int j = k_bef+1 ; j <= k_now ;j++) {
					//変数l以外の方向についてループさせる
					//代入
					//未実装
				}

				//---------------------------------------------------------------------

				//新しいポイントを内分計算

				for(int j = k_now ; j >= k_now -p[l]+1 ; j--) {
					double α = (Xl[i_Xl] -bUl[j])/(Ul[j-k_now+i_Ul+p[l]-1] -bUl[j]);

					//変数l以外の方向についてループさせる
					//内分計算
					//未実装

				}

				//---------------------------------------------------------------------

				k_bef = k_now;

			}

			//X[l]を全て代入し終わった

			//-------------------------------------------------------------------------

			//後方の変化しなかったポイントを代入する
			for(int i=k_bef+1 ; i < n_Ul+n_Xl ; i++) {

			}

			//-------------------------------------------------------------------------

			//残った元のノットを追加する
			for(;i_Ul < n_Ul ; i_Ul++, i_bUl++) {
				bUl[i_bUl] = Ul[i_Ul];
			}

		}

		//全てのノットを挿入し終わり、ポイントも計算し終わった
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
