package simulation.function.nurbs;

import java.util.ArrayList;

public class NURBSProperty {
	/**各変数の基底関数のノットベクトル*/
	protected double[][] knot;

	/**各変数の基底関数の次数*/
	protected int[] p;

	/**インデックスの変換計算に使う*/
	protected int[] Pi;

	/**変数の数*/
	protected final int parameterNum;

	/**registerNURBSFunction(NURBSFunction)で登録されたNURBSFunction*/
	private final ArrayList<NURBSFunction> nurbslist = new ArrayList<>();

	/**
	 * NURBSの基底関数組を保有するNURBSPropertyをインスタンス化します。
	 * 指定するノットベクトルはオープンノットベクトルであることを前提とします。
	 * @param knot ノットベクトルを指定する。1変数NURBSの場合、knot[0]にノットベクトルを
	 * 与え、knot.lengthは1であること。2変数の場合、knot[0]とknot[1]にそれぞれのノットベクトル
	 * を与え、knot.lengthは2であること。以下同様である。
	 * @param p 各変数の基底関数の次数
	 */
	public NURBSProperty(double[][] knot, int[] p){
		if(knot == null) {
			throw new IllegalArgumentException("引数knotが指定されていません");
		}else if(p == null) {
			throw new IllegalArgumentException("引数pが指定されていません");
		}

		if(knot.length != p.length) {
			//変数の数が一致していない場合
			throw new IllegalArgumentException("knotとpが示す変数の数が一致していません");
		}
		this.parameterNum = knot.length;
		this.Pi = new int[this.parameterNum+1];
		this.Pi[this.parameterNum] = 1;

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

			this.Pi[i] = this.Pi[i+1]*(p[i]+1);
		}

		this.knot = knot;
		this.p = p;
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
