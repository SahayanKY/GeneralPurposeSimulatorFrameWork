package simulation.function.nurbs;

import java.util.ArrayList;

/**NURBS基底関数の組を表すクラス
 * NURBS基底関数の構成要素である、ノットベクトル、各基底関数の次数、コントロールポイントの
 * 重みを保持し、それらにより構成される基底関数の組を表します。
 * */
public class NURBSProperty {

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

	/**インデックスの変換計算に使う
	 * i=0,1,...,m-1についてはn{i}*n{i+1}*...*n{m-1}
	 * i=mについては1
	 * */
	protected int[] Pi_n;

	/**コントロールポイントの重み*/
	protected double[] weight;

	/**変数の数*/
	protected final int parameterNum;

	/**registerNURBSFunction(NURBSFunction)で登録されたNURBSFunction*/
	private final ArrayList<NURBSFunction> nurbslist = new ArrayList<>();

	/**このプロパティが示す基底関数組が特にBスプライン基底関数である場合、true*/
	public final boolean isBSpline;

	/**
	 * NURBSの基底関数組を保有するNURBSPropertyをインスタンス化します。
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
	public NURBSProperty(double[][] knot, int[] p, double[] weight){
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

			//各ノットベクトルについて
			for(int j=0;j<knot[i].length-1;j++) {
				//オープンノットベクトルになっているか
				//前後ろの要素がp+1個重なっているか
				//0,1,...,pが同じ、n,...,n+p-1,n+pが同じ(n+p+1 == knot[i].length)
				if(j<p[i] //前のノットについて調べるとき
						||
					knot[i].length-p[i]-1 <= j //後ろのノットについて調べるとき
				) {
					if(knot[i][j]!=knot[i][j+1]) {
						throw new IllegalArgumentException("ノットベクトルknot["+i+"]がオープンノットベクトルでありません");
					}
				}

				//各変数のノットベクトルは単調増加列になっているのか
				if(knot[i][j]>knot[i][j+1]) {
					throw new IllegalArgumentException("ノットベクトルが単調増加列でありません:knot["+i+"]["+j+"]>knot["+i+"]["+j+1+"]");
				}
			}

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
	 * 定義域外であれば例外がスローされます。
	 * @param t 調べる変数値
	 * */
	public void checkVariableIsValid(double... t) throws IllegalArgumentException{
		//指定された変数の数は想定している変数の数に一致しているか
		if(t.length != parameterNum) {
			throw new IllegalArgumentException("変数の数が要求される数"+parameterNum+"に合いません:"+t.length);
		}

		//tはNURBS関数の定義域に反していないか
		for(int i=0;i<parameterNum;i++) {
			//各変数について対応のノットベクトルの範囲の中にあるかを調べる
			if(t[i] < knot[i][0] || t[i] > knot[i][knot[i].length-1]) {
				throw new IllegalArgumentException("指定された変数値t["+i+"]はノットベクトルの範囲を超えています");
			}
		}
	}

	/**
	 * NURBSFunctionインスタンスをこのインスタンスに登録します。
	 * k法によって精細化を行う際、NURBSFunctionの状態を変える必要があり、
	 * それを一括で行うためのものです。なお、これはNURBSFunction内部から
	 * 自動的に呼び出されるので、NURBSFunctionインスタンスを利用する際に
	 * 明示的に呼び出してはなりません。
	 * @param function 登録されるNURBSFunctionインスタンス
	 */
	protected void registerNURBSFunction(NURBSFunction function) {
		this.nurbslist.add(function);
	}

	/**
	 * k法によるノットの挿入、及び登録されているNURBSFunctionのコントロールポイントの
	 * 追加を行います。
	 * */
	public void k_method(){}
}
