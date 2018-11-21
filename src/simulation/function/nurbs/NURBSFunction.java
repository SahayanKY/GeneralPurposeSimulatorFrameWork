package simulation.function.nurbs;

public class NURBSFunction {
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

		int[] ctrlNum = {
				3
		};

		int[] p = {2};

		NURBSProperty property = new NURBSProperty(knot, p, weight);
		NURBSFunction func = new NURBSFunction(ctrl, property);

		for(int i=0;i<=30;i++) {
			double t = (i==0)? knot[0][0] : (i==30)? knot[0][knot[0].length-1]: (knot[0][knot[0].length-1]-knot[0][0])*i/30;
			double[] result = func.value(t);
			System.out.print(t);
			for(double f:result) {
				System.out.print("	"+f);
			}
			System.out.println("");
		}

	}


	/**コントロールポイント。具体的な中身はコンストラクタを参照*/
	private double[][] ctrl;

	/**このインスタンスが必要とする基底関数のノットベクトルや次数*/
	private final NURBSProperty pro;

	/**このインスタンスが扱う関数値の次元数*/
	private int dimension=-1;

	/**関数値計算時に必要となるコントロールポイント数*/
	private int effCtrlNum=1;

	/**
	 * NURBS関数をインスタンス化させます。
	 * コントロールポイントは多変数NURBSの場合注意が必要です。
	 * mを変数の数、Pをコントロールポイント、n_iを変数i方向のポイントの数としたとき、
	 * <ul>
	 * 	<li>P_{0,0,...,0,0}はctrl[0]に格納される。
	 * 	<li>P_{0,0,...,0,n_{m-1}-1}はctrl[n_{m-1}-1]に格納される。
	 * 	<li>P_{0,0,...,1,0}はctrl[n_{m-1}]に格納される。
	 * 	<li>P_{i_0,i_1,...,i_{m-2},i_{m-1}}はctrl[i_{m-1}+i_{m-2}*n_{m-1}+...+i_0*n_1*...*n_{m-1}]に格納される。
	 * </ul>
	 * これを前提として計算を行います。ctrlの第2のインデックスは各コントロールポイントの座標
	 * を保持します。
	 *
	 * @param ctrl コントロールポイントを指定する。
	 * @param ctrlNum 各方向のコントロールポイントの数を指定する。
	 * @param pro このNURBS関数が必要とするノットベクトルを表すNURBSProperty
	 */
	public NURBSFunction(double[][] ctrl, NURBSProperty pro) {
		//nullチェック
		if(ctrl == null) {
			throw new IllegalArgumentException("引数ctrlがnullです");
		}else if(pro == null) {
			throw new IllegalArgumentException("引数proがnullです");
		}

		this.effCtrlNum = pro.Pi_p[0];


		if(ctrl.length != pro.weight.length) {
			throw new IllegalArgumentException("コントロールポイントの数が重みの数に一致しません");
		}

		//ctrlの各要素の成分の数は一定になっているのか、
		//d!=0か
		for(int i=0;i<ctrl.length;i++) {
			double[] monoctrl=ctrl[i];
			if(dimension==-1) {
				dimension = monoctrl.length;

				if(dimension == 0) {
					throw new IllegalArgumentException("コントロールポイントの要素数が足りません:次元d(>0)");
				}
			}

			if(dimension != monoctrl.length) {
				throw new IllegalArgumentException("コントロールポイントctrl["+i+"]に次元数の過不足があります");
			}

			//先に重みを各座標に掛け合わせておく
			for(int d=0;d<dimension;d++) {
				monoctrl[d] *= pro.weight[i];
			}
		}

		pro.registerNURBSFunction(this);

		this.ctrl = ctrl;
		this.pro = pro;
	}

	/**
	 * 変数値を引数で指定し、その点でのNURBS関数の値を計算します。
	 * @param t 変数値
	 * @return 関数値
	 */
	public double[] value(double... t){
		//指定された変数の数は想定している変数の数に一致しているか
		if(t.length != pro.parameterNum) {
			throw new IllegalArgumentException("変数の数が要求される数"+pro.parameterNum+"に合いません:"+t.length);
		}

		//tはNURBS関数の定義域に反していないか
		for(int i=0;i<pro.parameterNum;i++) {
			//各変数について対応のノットベクトルの範囲の中にあるかを調べる
			if(t[i] < pro.knot[i][0] || t[i] > pro.knot[i][pro.knot[i].length-1]) {
				throw new IllegalArgumentException("指定された変数値t["+i+"]はノットベクトルの範囲を超えています");
			}
		}

		//各変数についてt_k <= t < t_k+1となるようなkをさがす
		int[] k = new int[pro.parameterNum];
		for(int i=0;i<pro.parameterNum;i++) {
			for(int j=0; j<pro.knot[i].length; j++) {
				if(t[i] < pro.knot[i][j]) {
					k[i] = j-1; //直前の位置がk
					break;
				}
			}
			//tがノットの最後端に等しい時、（値が違う）一つ前のノットを指定する
			if(t[i] == pro.knot[i][pro.knot[i].length-1]) {
				k[i] = pro.knot[i].length-pro.p[i]-2; //==n-1
			}
		}

		//以降deBoorアルゴリズムの通り
		//Q[][0]:重み
		//Q[][1]以降:重み*座標値
		double Q[][] = new double[effCtrlNum][dimension+1];

		//元のコントロールポイントから必要なものをコピーし初期化する
		{
			int[] indexs = new int[pro.parameterNum];
			out:while(true) {
				int Qindex=0,Pindex=0;
				//i0,i1,...,i{m-1}というインデックスを1つの数に置き換える
				for(int i=0;i<pro.parameterNum;i++) {
					Qindex += indexs[i] *pro.Pi_p[i+1];
					Pindex += (k[i]-pro.p[i]+indexs[i]) *pro.Pi_n[i+1];
				}

				Q[Qindex][0] = pro.weight[Qindex];
				for(int i=1;i<dimension+1;i++) {
					Q[Qindex][i] = ctrl[Pindex][i-1];
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
		}


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
						for(int d=0;d<dimension+1;d++) {
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

		//結果を格納しているp{0},p[1],...,p{m-1}を変換し、取得する
		int resultIndex=0;
		for(int i=0;i<pro.parameterNum;i++) {
			resultIndex += pro.p[i]*pro.Pi_p[i+1];
		}

		/*Q[resultIndex]には{重みの足し合わせ、座標1*重みの足し合わせ、...}が入っているため、
		 * インデックス0で残りの要素を割り、その残りの要素を結果として出さなければならない
		 * (NURBSの特徴)*/
		double[] result = new double[dimension];
		for(int i=1;i<=dimension;i++) {
			result[i-1] = Q[resultIndex][i]/Q[resultIndex][0];
		}

		return result;
	}

	/**
	 * k法によるコントロールポイントの変更を行います。
	 * ただし、このメソッドは登録してあるNURBSPropertyインスタンスから呼び出される
	 * ものであり、利用者は明示的に呼び出さないでください。
	 */
	protected void kmethod() {}
}
