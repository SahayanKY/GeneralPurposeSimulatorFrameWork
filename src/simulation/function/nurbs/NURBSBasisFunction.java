package simulation.function.nurbs;

import simulation.function.nurbs.assertion.NURBSAsserter;

/**
 * <p>NURBS基底関数の組を表すクラス
 * <p>NURBS基底関数の構成要素である、ノットベクトル、各基底関数の次数、コントロールポイントの
 * 重みを保持し、それらにより構成される基底関数の組を表します。
 *
 * <p>このオブジェクトは基本immutableであり、全ての変数、配列変数の要素も不変です。
 * ただし、shallowコピーメソッドを利用し、それで得た配列要素に対して
 * 操作した場合、変化する可能性があります。
 * @version 2019/02/23 17:58
 * */
public class NURBSBasisFunction {

	/**
	 * 各変数の基底関数のノットベクトル<br>
	 * 第1インデックスは0からm-1(mは変数の数)まであり、<br>
	 * 第2インデックスは不定です。
	 *
	 * @version 2019/02/22 21:37
	 * */
	private final double[][] knot;
	/**
	 * ノットベクトルを返します。<br>
	 * 配列の複製を渡します。
	 *
	 * @return ノットベクトル
	 * @see #giveKnotVector_Shallow()
	 * @version 2019/02/23 13:43
	 * */
	public double[][] giveKnotVector_Deep(){
		double[][] copy_knot = new double[this.parameterNum][];
		for(int i=0;i<this.parameterNum;i++) {
			copy_knot[i] = new double[this.knot[i].length];
			for(int j=0;j<copy_knot[i].length;j++) {
				copy_knot[i][j] = this.knot[i][j];
			}
		}

		return copy_knot;
	}
	/**
	 * <p>ノットベクトルを返します。
	 * <p>配列の参照を渡します。そのため、得た配列要素を変更すると
	 * インスタンスの状態が変化します。それはこのインスタンスの想定された
	 * 利用ではないので注意してください。
	 * <p>このメソッドは、配列要素を変化させないコンテキストの中で、
	 * 複製をするとメモリを圧迫する可能性を考慮したものです。
	 *
	 * @return ノットベクトル
	 * @see #giveKnotVector_Deep()
	 * @version 2019/02/23 13:42
	 * */
	public double[][] giveKnotVector_Shallow(){
		return this.knot;
	}



	/**
	 * 各変数の基底関数の次数<br>
	 * インデックスは0からm-1(mは変数の数)まであります。
	 *
	 * @version 2019/02/22 21:38
	 * */
	private final int[] p;
	/**
	 * 次数の配列を返します。
	 * 配列は複製を渡します。
	 *
	 * @return 次数の配列
	 * @version 2019/02/22 21:38
	 * */
	public int[] giveDegreeArray() {
		int[] copy_p = new int[this.parameterNum];
		for(int i=0;i<this.parameterNum;i++) {
			copy_p[i] = this.p[i];
		}
		return copy_p;
	}


	/**インデックスの変換計算に使う<br>
	 * i=0,1,...,m-1については(p{i}+1)(p{i+1}+1)...(p{m-1}+1)
	 * i=mについては1
	 *
	 * @version 2019/02/22 21:38
	 * */
	@Deprecated
	protected final int[] Pi_p;
	/**
	 * Pi_p配列を計算し、返します。<br>
	 * Pi_p[i] = (p{i}+1)(p{i+1}+1)...(p{m-1}+1)
	 * (mは変数の数、pは次数)
	 * です。(ただし、i=mでは1)<br>
	 * インデックスは0からmまであります。
	 *
	 * @version 2019/02/22 21:38
	 * */
	public int[] givePi_p() {
		int[] Pi_p = new int[this.parameterNum+1];
		Pi_p[this.parameterNum] = 1;
		for(int i=this.parameterNum-1;i>=0;i--) {
			Pi_p[i] = (p[i]+1)*Pi_p[i+1];
		}
		return Pi_p;
	}

	/**
	 * 関数値計算時に必要となるコントロールポイント数
	 * @version 2019/02/22 21:40
	 * */
	@Deprecated
	public final int effCtrlNum;
	/**
	 * 関数値計算時等に必要となるコントロールポイント数を返します。
	 * @return 計算に必要なポイント数
	 * @version 2019/02/23 0:52
	 * */
	public int giveEffectiveCtrlNum() {
		int effCtrlNum = 1;
		for(int i=0;i<this.parameterNum;i++) {
			effCtrlNum *= (this.p[i]+1);
		}
		return effCtrlNum;
	}


	/**
	 * 各変数方向のコントロールポイントの数<br>
	 * インデックスは0からm-1まであります。(mは変数の数)
	 * @version 2019/02/22 21:40
	 * */
	private final int[] n;
	/**
	 * コントロールポイントの数の配列を返します。<br>
	 * 配列は複製を渡します。
	 *
	 * @return 各変数についてのポイント数の配列
	 * @version 2019/02/22 21:40
	 * */
	public int[] giveNumberArrayOfCtrl() {
		int[] copy_n = new int[this.parameterNum];
		for(int i=0;i<this.parameterNum;i++) {
			copy_n[i] = this.n[i];
		}
		return copy_n;
	}
	/**
	 * 全コントロールポイントの数を返します。
	 * @return 全ポイント数
	 * @version 2019/02/23 0:55
	 * */
	public int giveNumberOfAllCtrl() {
		return this.weight.length;
	}
	/**
	 * インデックスの変換計算等に使う<br>
	 * n{i}を変数t{i}方向のポイントの数として、
	 * i=0,1,...,m-1についてはn{i}*n{i+1}*...*n{m-1}、
	 * i=mについては1。<br>
	 * 総コントロールポイント数はPi_n[0]に等しい。
	 * @version 2019/02/22 21:40
	 * */
	@Deprecated
	protected final int[] Pi_n;
	/**
	 * Pi_n配列を計算し、返します。<br>
	 * Pi_n[i] = n{i}*n{i+1}*...*n{m-1}
	 * (mは変数の数、nはコントロールポイントの数)
	 * です。(ただし、i=mでは1)<br>
	 * インデックスは0からm(mは変数の数)まであります。<br>
	 *
	 * @return Pi_n配列。具体的な中身はメソッドの説明参照。
	 * @version 2019/02/22 21:40
	 * */
	public int[] givePi_n() {
		int[] Pi_n = new int[this.parameterNum+1];
		Pi_n[this.parameterNum] = 1;
		for(int i=this.parameterNum-1;i>=0;i++) {
			Pi_n[i] = this.n[i]*Pi_n[i+1];
		}

		return Pi_n;
	}


	/**
	 * コントロールポイントの重みの配列<br>
	 * インデックスは0からn-1まであります。(nはコントロールポイントの数)
	 * @version 2019/02/22 21:41
	 * */
	private final double[] weight;
	/**
	 * コントロールポイントの重みの配列を返します。<br>
	 * 配列は複製を返します。
	 *
	 * @return コントロールポイントの重みの配列
	 * @version 2019/02/22 21:41
	 * */
	public double[] giveWeightArray_Deep() {
		double[] copy_weight = new double[this.weight.length];
		for(int i=0;i<copy_weight.length;i++) {
			copy_weight[i] = this.weight[i];
		}

		return copy_weight;
	}
	/**
	 * <p>コントロールポイントの重みの配列を返します。
	 * <p>配列の参照を渡します。そのため、得た配列要素を変更すると
	 * インスタンスの状態が変化します。それはこのインスタンスの想定された
	 * 利用ではないので注意してください。
	 * <p>このメソッドは、配列要素を変化させないコンテキストの中で、
	 * 複製をするとメモリを圧迫する可能性を考慮したものです。
	 *
	 * @return コントロールポイントの重みの配列
	 * @see #giveWeightArray_Deep()
	 * @version 2019/02/23 13:48
	 * */
	public double[] giveWeightArray_Shallow() {
		return this.weight;
	}


	/**
	 * 変数の数
	 * @version 2019/02/22 21:41
	 * */
	public final int parameterNum;

	/**
	 * このインスタンスが示す基底関数が特にBスプライン基底関数である場合、true
	 * @version 2019/02/22 21:42
	 * */
	public final boolean isBSpline;

	/**
	 * NURBSの基底関数組を表すNURBSBasisFunctionをインスタンス化します。
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
	 *
	 * @throws NullPointerException
	 * <ul>
	 * 		<li>knot,p,weightがnullの場合
	 * </ul>
	 * @throws IllegalArgumentException
	 * <ul>
	 * 		<li>ノットベクトルの数と次数の数が一致しない場合
	 * 		<li>重みとして与えられた数が0または負数の場合
	 * 		<li>次数として与えられた数が1未満の場合
	 * 		<li>コントロールポイントの数、ノットベクトルの要素の数、次数のつじつまが合わない場合
	 * 		<li>ノットベクトルがオープンノットベクトルでない場合
	 * 		<li>ノットベクトルが単調増加列でない場合
	 * </ul>
	 * @version 2019/02/22 21:42
	 */
	public NURBSBasisFunction(double[][] knot, int[] p, double[] weight){
		if(knot == null) {
			throw new NullPointerException("引数knotが指定されていません");
		}else if(p == null) {
			throw new NullPointerException("引数pが指定されていません");
		}else if(weight == null) {
			throw new NullPointerException("引数weightが指定されていません");
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
		this.n = new int[this.parameterNum];
		NURBSAsserter asserter = new NURBSAsserter(true);

		//ポイントの総数、ノットと次数から推算
		int AllCtrlNum = 0;
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

			asserter.assertArrayIsOpenKnotVector(knot[i], p[i]);

			this.n[i] = knot[i].length-p[i]-1;
			AllCtrlNum += n[i];
		}

		if(AllCtrlNum != weight.length) {
			//総コントロールポイント数を比較
			//重みの数と、ノットベクトルと次数から推算したポイントの数は等しいはず
			throw new IllegalArgumentException(
					"コントロールポイントの数とノットベクトルの要素数と次数のつじつまが合いません");
		}

		this.knot = new double[parameterNum][];
		this.p = new int[parameterNum];
		for(int i=0;i<parameterNum;i++) {
			this.p[i] = p[i];
			this.knot[i] = new double[knot[i].length];
			for(int j=0;j<knot[i].length;j++) {
				this.knot[i][j] = knot[i][j];
			}
		}

		this.weight = new double[weight.length];
		for(int i=0;i<weight.length;i++) {
			this.weight[i] = weight[i];
		}
	}

	/**
	 * <p>この基底関数の定義域を返します。</p>
	 *
	 * <p>
	 * 第1インデックスは変数x{i}を指定し、
	 * 第2インデックスの1つ目の要素は最小値、2つ目の要素は最大値を表しています。
	 * </p>
	 *
	 * @return 定義域の配列
	 * @version 2019/02/23 2:40
	 * */
	public double[][] giveDomain(){
		double[][] domain = new double[this.parameterNum][2];
		for(int i=0;i<this.parameterNum;i++) {
			domain[i][0] = this.knot[i][0];
			domain[i][1] = this.knot[i][this.knot[i].length-1];
		}

		return domain;
	}


	/**
	 * <p>
	 * 基底関数の値を返します。
	 * </p>
	 * <p>
	 * 基底関数w{i,j,..k}N{i,p}N{j,q}...N{k,r}/(sum{a}sum{b}..sum{c} w{a,b,..c}N{a,p}N{b,q}..N{c,r})の値を計算します。
	 * </p>
	 *
	 * @param indexs 各変数のBスプライン基底関数のインデックス{i,j,..k}を指定します。
	 * @param t 変数値
	 * @version 2019/02/22 22:03
	 * */
	public double value(int[] indexs,double[] t) {
		if(indexs == null) {
			throw new IllegalArgumentException("indexsが指定されていません");
		}else if(t == null) {
			throw new IllegalArgumentException("tが指定されていません");
		}

		NURBSAsserter asserter = new NURBSAsserter(true);

		//変数値が定義域に即しているか
		asserter.assertVariableIsValid(this, t);

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
		int[] Pi_n = this.givePi_n();
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
	 * 1変数Bスプライン基底関数N{i,p}を計算する
	 * @param ivar 変数値配列のインデックス。どの変数かを指定する。
	 * @param iN 基底関数のインデックスi
	 * @param t 変数値
	 * @version 2019/02/22 22:03
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
