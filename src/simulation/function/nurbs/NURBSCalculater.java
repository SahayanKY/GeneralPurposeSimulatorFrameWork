package simulation.function.nurbs;

public abstract class NURBSCalculater {

	/**
	 * 各変数についてt_k <= t < t_k+1となるようなkを探します。
	 * @param pro プロパティ
	 * @param t 変数値
	 * */
	protected static int[] searchVariablesPosition_InKnotVectors(NURBSProperty pro, double[] t) {
		int[] k = new int[pro.parameterNum];
		for(int i=0;i<pro.parameterNum;i++) {
			k[i] = searchVariablePosition_InKnotVector(pro.knot[i],pro.p[i],t[i]);
		}
		return k;
	}

	/**
	 * 1変数について、t_k <= t < t_k+1となるようなkを探します。
	 * @param knot 変数に対応するノットベクトル
	 * @param p 変数に対応する次数
	 * @param t 変数値
	 * */
	protected static int searchVariablePosition_InKnotVector(double[] knot, int p,double t) {
		int k=-1;
		if(t == knot[knot.length-1]) {
			//tがノットの最後端に等しい時、（値が違う）一つ前のノットを指定する
			k = knot.length-p-2; //==n-1
		}else {
			for(int j=0; j<knot.length; j++) {
				if(t < knot[j]) {
					k = j-1; //直前の位置がk
					break;
				}
			}
		}
		if(k == -1) {
			throw new IllegalArgumentException("変数tはノットベクトルの範囲にありません");
		}
		return k;
	}


	/**
	 * 計算に有効なコントロールポイントを返します。
	 * 返される配列の具体的な意味は、
	 * <ul>
	 * 	<li>第1インデックスはコントロールポイントの指定
	 * 	<li>第2インデックスはコントロールポイントの重み、重み*座標1、...
	 * </ul>
	 * です。funcにnullを指定した場合、第2インデックスは重みだけが返されます。
	 * この配列の長さはpro.effCtrlNumです。
	 *
	 * @param k ノットの有効範囲パラメータ
	 * @param pro プロパティ
	 * @param func NURBSFunctionインスタンス
	 * */
	protected static double[][] restrictControlPoint(int[] k, NURBSProperty pro, NURBSFunction func){
		int dimension;
		if(func == null) {
			dimension = 0;
		}else {
			dimension = func.ctrl[0].length;
		}

		double[][] Q = new double[pro.effCtrlNum][dimension+1];

		//元のコントロールポイントから必要なものをコピーし初期化する
		int[] indexs = new int[pro.parameterNum];
		out:while(true) {
			int Qindex=0,Pindex=0;
			//i0,i1,...,i{m-1}というインデックスを1つの数に置き換える
			for(int i=0;i<pro.parameterNum;i++) {
				Qindex += indexs[i] *pro.Pi_p[i+1];
				Pindex += (k[i]-pro.p[i]+indexs[i]) *pro.Pi_n[i+1];
			}

			Q[Qindex][0] = pro.weight[Pindex];
			for(int i=1;i<dimension+1;i++) {
				Q[Qindex][i] = func.ctrl[Pindex][i-1];
			}

			//繰り上がり処理
			for(int i=indexs.length-1;i>=0;i--) {
				indexs[i]++;
				if(indexs[i]<=pro.p[i]) {
					break;
				}else {
					indexs[i]=0;
					if(i==0) {
						//全ての組み合わせについて終了
						break out;
					}else {
						continue;
					}
				}
			}
		}

		return Q;
	}


	/**
	 * deBoorのアルゴリズムのループ部分です。
	 *
	 * これにより、f{i,j,..,k}N{i,p}N{j,q}...N{k,r}を計算したことになります。
	 *
	 * ノット範囲の限定、及びそれに基づくコントロールポイントの限定を行ってから、
	 * このメソッドを呼び出してください。また、引数Qに上書きしながら計算を行うため、
	 * Qの各要素の値は呼び出し前に対して変わっています。
	 *
	 * @param t 変数値
	 * @param k ノット範囲の限定パラメータ。
	 * @param Q 限定後のコントロールポイント。BスプラインのdeBoorアルゴリズムを
	 * 作用させるものを指定する。
	 * @param pro プロパティ
	 * */
	protected static double[] deBoorsLoop(double[] t, int[] k, double[][] Q, NURBSProperty pro) {
		//4つループの入れ子
		for(int l=pro.parameterNum-1;l>=0;l--) {
			for(int r=0;r<=pro.p[l]-1;r++) {
				for(int i=pro.p[l];i>=r+1;i--) {
					double alpha
						= (t[l] -pro.knot[l][i+k[l]-pro.p[l]])
							/(pro.knot[l][i+k[l]-r] -pro.knot[l][i+k[l]-pro.p[l]]);


					//0,0,...,0からp0,p1,...,p{l-1}まで繰り返す
					int[] indexs = new int[pro.parameterNum];
					//indexsのインデックスl+1からm-1までは次数pで固定であり、
					//インデックスlは対象外（他の意味によって指定される）
					for(int j=l+1;j<pro.parameterNum;j++) {
						indexs[j] = pro.p[j];
					}
					indexs[l] = i;

					out:while(true) {
						//i{0},i{1},...,i{l-1},i{l},p{l+1},...,p{m-1}を変換したものを格納
						int convertIndex = 0;
						for(int j=0;j<pro.parameterNum;j++) {
							convertIndex += indexs[j]*pro.Pi_p[j+1];
						}

						//deBoorの計算Q = (1-a)Q +aQの部分
						for(int d=0;d<Q[0].length;d++) {
							//コントロールポイントの各成分毎に計算
							Q[convertIndex][d] =
								(1-alpha)*Q[convertIndex-pro.Pi_p[l+1]][d]
									+
								alpha*Q[convertIndex][d];
						}


						//繰り上がり処理
						//i0,i1,...,i{l-1}までを弄るのでl-1始まり
						for(int j=l-1;j>=0;j--) {
							indexs[j]++;
							if(indexs[j]<=pro.p[j]) {
								break;
							}else {
								indexs[j]=0;
								if(j==0) {
									//全ての組み合わせについて終了
									break out;
								}else {
									continue;
								}
							}
						}

						if(l==0) {
							break out;
						}
					}
				}
			}
		}

		//結果を格納しているp{0},p[1],...,p{m-1}はQ.length-1に等しい
		return Q[Q.length-1];
	}

	/**
	 * 重み*座標値となっている値を座標値単体に変換します。
	 * 引数pointの第1インデックスには重みを、第2インデックス以降には
	 * 重み*座標値を指定してください。また、戻り値は
	 * 座標値1、座標値2、...となり、配列の長さは引数pointの長さの-1になります。
	 * @param point 重み、重み*座標値1、...となっているポイント
	 * @throws IllegalArgumentException pointの長さが1以下で処理できない場合。
	 * */
	protected static double[] processWeight(double[] point) {
		if(point.length <= 1) {
			throw new IllegalArgumentException("指定された配列の長さは1以下で処理できません");
		}
		int dimension = point.length -1;
		double[] result = new double[dimension];
		for(int i=0;i<dimension;i++) {
			result[i] = point[i+1]/point[0];
		}
		return result;
	}

}
