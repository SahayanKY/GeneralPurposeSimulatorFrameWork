package simulation.function.nurbs;

/**NURBS基底関数の組を表すクラス
 * NURBS基底関数の構成要素である、ノットベクトル、各基底関数の次数、コントロールポイントの
 * 重みを保持し、それらにより構成される基底関数の組を表します。
 * */
public class NURBSBasisFunction {

	/**各変数の基底関数のノットベクトル*/
	protected double[][] knot;

	/**各変数の基底関数の次数*/
	protected int[] p;

	/**インデックスの変換計算に使う
	 * i=0,1,...,m-1については(p{i}+1)(p{i+1}+1)...(p{m-1}+1)
	 * i=mについては1
	 * */
	protected int[] Pi_p;

	/**関数値計算時に必要となるコントロールポイント数*/
	protected int effCtrlNum=1;

	/**インデックスの変換計算等に使う<br>
	 * n{i}を変数t{i}方向のポイントの数として、
	 * i=0,1,...,m-1についてはn{i}*n{i+1}*...*n{m-1}、
	 * i=mについては1。<br>
	 * 総コントロールポイント数はPi_n[0]に等しい。
	 * */
	protected int[] Pi_n;

	/**コントロールポイントの重み*/
	protected double[] weight;

	/**変数の数*/
	protected final int parameterNum;

	/**このプロパティが示す基底関数組が特にBスプライン基底関数である場合、true*/
	public final boolean isBSpline;

	/**
	 * NURBSの基底関数組を保有するNURBSBasisFunctionをインスタンス化します。
	 * 指定するノットベクトルはオープンノットベクトルであることを前提とします。
	 *
	 * 重みについては正の数を必ず指定してください。
	 * ここで全て1を指定した場合、Bスプラインに対応します。
	 *
	 * @param knot ノットベクトルを指定する。1変数NURBSの場合、knot[0]にノットベクトルを
	 * 与え、knot.lengthは1であること。2変数の場合、knot[0]とknot[1]にそれぞれのノットベクトル
	 * を与え、knot.lengthは2であること。以下同様である。
	 * @param p 各変数の基底関数の次数
	 * @param weight 各コントロールポイントの重み
	 */
	public NURBSBasisFunction(double[][] knot, int[] p, double[] weight){
		if(knot == null) {
			throw new IllegalArgumentException("引数knotが指定されていません");
		}else if(p == null) {
			throw new IllegalArgumentException("引数pが指定されていません");
		}else if(weight == null) {
			throw new IllegalArgumentException("引数weightが指定されていません");
		}

		if(knot.length != p.length) {
			//変数の数が一致していない場合
			throw new IllegalArgumentException("knotとpが示す変数の数が一致していません");
		}

		boolean isBSpline=true;

		for(int i=0;i<weight.length;i++) {
			//重みが全て正の数かをチェック
			if(weight[i]<=0) {
				throw new IllegalArgumentException("重みweight["+i+"]が正の数でありません");
			}

			//重みが全て1だった場合、Bスプラインである
			if(isBSpline && weight[i]!=1) {
				isBSpline = false;
			}
		}
		this.isBSpline = isBSpline;

		this.parameterNum = knot.length;
		this.Pi_p = new int[this.parameterNum+1];
		this.Pi_p[this.parameterNum] = 1;
		this.Pi_n = new int[this.parameterNum+1];
		this.Pi_n[this.parameterNum] = 1;

		/*ノットベクトルと次数から予想されるコントロールポイント数を計算
		 * 1変数に対して(コントロールポイントの数)=(ノット要素数)-(次数)-1
		 * 2変数以上ではそれらの総積
		 * P_{0,0,0}からP_{2,5,4}まで存在する場合、それぞれの変数に対して
		 * 3,6,5個のポイントがあるので、3*6*5が総コントロールポイント数になる
		*/
		for(int i=parameterNum-1;i>=0;i--) {
			//各変数の基底関数の次数は1以上になっているのか
			if(p[i]<1) {
				throw new IllegalArgumentException("次数p["+i+"]が1以上でありません");
			}

			assertArrayIsOpenKnotVector(true, knot[i], p[i]);

			this.Pi_p[i] = this.Pi_p[i+1]*(p[i]+1);
			this.Pi_n[i] = this.Pi_n[i+1]*(knot[i].length-p[i]-1);
		}


		if(this.Pi_n[0] != weight.length) {
			//Pi_n[0]は総コントロールポイント数に等しい
			throw new IllegalArgumentException(
					"コントロールポイントの数とノットベクトルの要素数と次数のつじつまが合いません");
		}


		this.effCtrlNum = Pi_p[0];
		this.knot = knot;
		this.p = p;
		this.weight = weight;
	}

	/**
	 * 指定された値が定義域内であるかどうかを判断します。
	 *
	 * @param assertion trueを指定した場合、例外がスローされます。
	 * @param t 調べる変数値
	 * @return 定義域内だった場合true
	 * @throws IllegalArgumentException 定義域外であった場合
	 * */
	public boolean assertVariableIsValid(boolean assertion, double... t){
		boolean result = true;

		//指定された変数の数は想定している変数の数に一致しているか
		if(t.length != parameterNum) {
			if(assertion) {
				throw new IllegalArgumentException("変数の数が要求される数"+parameterNum+"に合いません:"+t.length);
			}
			result = false;
		}

		//tはNURBS関数の定義域に反していないか
		for(int i=0;i<parameterNum;i++) {
			//各変数について対応のノットベクトルの範囲の中にあるかを調べる
			if(t[i] < knot[i][0] || t[i] > knot[i][knot[i].length-1]) {
				if(assertion) {
					throw new IllegalArgumentException("指定された変数値t["+i+"]はノットベクトルの範囲を超えています");
				}
				result = false;
			}
		}
		return result;
	}

	/**
	 * 指定された配列がオープンノットベクトルかどうかを調べます。
	 *
	 * @param assertion trueを指定した場合、例外がスローされます。
	 * @param knot 調べる配列
	 * @param p 対応する次数
	 * @return オープンノットベクトルだった場合true
	 * @throws IllegalArgumentException オープンノットベクトルで無い場合
	 * */
	public static boolean assertArrayIsOpenKnotVector(boolean assertion, double[] knot, int p) {
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

	/**
	 * 基底関数の値を返します。
	 * @param indexs 各変数のBスプライン基底関数のインデックスを指定します。
	 * @param t 変数値
	 * */
	public double value(int[] indexs,double[] t) {
		if(indexs == null) {
			throw new IllegalArgumentException("indexsが指定されていません");
		}else if(t == null) {
			throw new IllegalArgumentException("tが指定されていません");
		}

		//変数値が定義域に即しているか
		assertVariableIsValid(true,t);

		if(indexs.length != t.length) {
			throw new IllegalArgumentException("基底関数のインデックス組の数と変数値の数が一致していません");
		}


		double result = 1;
		//各Bスプライン基底関数を掛け合わせていく
		for(int i=0;i<indexs.length;i++) {
			//0になる基底関数をかけることになるときはその場で0をreturnするようにする
			double f = BSplineBasisFunctionValue(i,indexs[i],t[i]);
			if(f==0) {
				return 0;
			}else {
				result *= f;
			}
		}
		/*
		 * result==N{i}N{j}..N{k}
		 * */


		if(this.isBSpline) {
			//重みが全て1なので、後の計算結果loopResult[0]やweight[weightIndex]は1になるので省略
			return result;
		}


		//indexsでの重みを取得する
		int weightIndex = 0;
		//i0,i1,...,i{m-1}というインデックスを1つの数に置き換える
		for(int i=0;i<this.parameterNum;i++) {
			weightIndex += indexs[i] *Pi_n[i+1];
		}
		result *= weight[weightIndex];
		/*
		 * result==w{ij..k}N{i}N{j}..N{k}
		 * */


		//重みだけでdeBoorを実行し、それでresultを割る
		//各変数についてt_k <= t < t_k+1となるようなkをさがす
		int[] k = NURBSCalculater.searchVariablesPosition_InKnotVectors(this, t);

		//以降deBoorアルゴリズムの通り
		//Q[][0]:計算に必要な重みのみに制限した配列
		double Q[][] = NURBSCalculater.restrictControlPoint(k, this, null);

		//4つの入れ子ループ部分へ
		//loopResult[0]:重みの足し合わせ結果
		double[] loopResult = NURBSCalculater.deBoorsLoop(t, k, Q, this);
		/*
		 * loopResult[0]==sum{a}sum{b}..sum{c} w{ab..c}N{a}N{b}..N{c}
		 * */

		return result/loopResult[0];
		/*
		 * return==w{ij..k}N{i}N{j}..N{k}/(sum{a}sum{b}..sum{c} w{ab..c}N{a}N{b}..N{c})
		 * */
	}


	/**
	 * 1変数Bスプライン基底関数を計算する
	 * @param ivar 変数値配列のインデックス
	 * @param iN 基底関数のインデックス
	 * @param t 変数値
	 * */
	private double BSplineBasisFunctionValue(int ivar,int iN,double t) {
		double[] knot = this.knot[ivar];
		int p = this.p[ivar];

		//t_k <= t < t_k+1
		int k = NURBSCalculater.searchVariablePosition_InKnotVector(knot, p, t);

		int h = k-iN;
		if(h < 0 || iN < k-p) {
			return 0;
		}


		double[] result;


		if(p-h >= h) {
			result = new double[h+1];
			result[0] = 1;
			for(int i=p;i>p-h;i--) {
				for(int j=p-i+1;j>=0;j--) {
					if(j==p-i+1) {
						result[j] = result[j-1]*(knot[iN+p+1]-t)/(knot[iN+p+1]-knot[iN+p-i+1]);
					}else if(j==0) {
						result[0] = result[0]*(t-knot[iN])/(knot[iN+i]-knot[iN]);
					}else {
						double a = (t-knot[iN+j])/(knot[iN+j+i]-knot[iN+j]);
						result[j] = a*result[j] +(1-a)*result[j-1];
					}
				}
			}
			for(int i=p-h;i>h;i--) {
				for(int j=h;j>=0;j--) {
					if(j==0) {
						result[0] = result[0]*(t-knot[iN])/(knot[iN+i]-knot[iN]);
					}else {
						double a = (t-knot[iN+j])/(knot[iN+j+i]-knot[iN+j]);
						result[j] = a*result[j] +(1-a)*result[j-1];
					}
				}
			}
			for(int i=h;i>0;i--) {
				for(int j=h;j>=h-i+1;j--) {
					double a = (t-knot[iN+j])/(knot[iN+j+i]-knot[iN+j]);
					result[j] = a*result[j] +(1-a)*result[j-1];
				}
			}

		}else {
			result = new double[p-h+1];
			result[0] = 1;

			for(int i=p;i>h;i--) {
				for(int j=p-i+1;j>=0;j--) {
					if(j==p-i+1) {
						result[p-i+1] = result[p-i]*(knot[iN+j+i]-t)/(knot[iN+j+i]-knot[iN+j]);
					}else if(j==0) {
						result[0] = result[0]*(t-knot[iN])/(knot[iN+i]-knot[iN]);
					}else {
						double a = (t-knot[iN+j])/(knot[iN+j+i]-knot[iN+j]);
						result[j] = a*result[j] +(1-a)*result[j-1];
					}
				}
			}
			for(int i=h;i>p-h;i--) {
				for(int j=0;j<=p-h;j++) {
					if(j==p-h) {
						result[j] = result[j]*(knot[iN+p+1]-t)/(knot[iN+p+1]-knot[iN+p-i+1]);
					}else {
						double a = (t-knot[iN+h-i+j+1])/(knot[iN+h+j+1]-knot[iN+h-i+j+1]);
						result[j] = (1-a)*result[j] +a*result[j+1];
					}
				}
			}
			for(int i=p-h;i>0;i--) {
				for(int j=h;j>=h-i+1;j--) {
					double a = (t-knot[iN+j])/(knot[iN+j+i]-knot[iN+j]);
					result[j] = a*result[j] +(1-a)*result[j-1];
				}
			}
		}

		return result[result.length-1];
	}

}
