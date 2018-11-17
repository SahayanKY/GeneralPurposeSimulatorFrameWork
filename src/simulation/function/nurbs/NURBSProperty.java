package simulation.function.nurbs;

import java.util.ArrayList;

public class NURBSProperty {
	/**各変数の基底関数のノットベクトル*/
	protected double[][] knot;
	/**各変数の基底関数の次数*/
	protected int[] p;
	//パラメータの数
	protected final int parameterNum;
	private final ArrayList<NURBSFunction> nurbslist = new ArrayList<>();

	/**
	 * NURBSのノットベクトルを保有するNURBSPropertyをインスタンス化します。
	 * @param knot ノットベクトルを指定する。1変数NURBSの場合、knot[0]にノットベクトルを
	 * 与え、knot.lengthは1であること。2変数の場合、knot[0]とknot[1]にそれぞれのノットベクトル
	 * を与え、knot.lengthは2であること。以下同様である。
	 */
	public NURBSProperty(double[][] knot, int[] p){
		//例外処理、パラメータチェック
		if(knot.length != p.length) {
			//変数の数が一致していない場合
			throw new IllegalArgumentException("knotとpが示す変数の数が一致していません");
		}
		this.parameterNum = knot.length;

		for(int i=0;i<parameterNum;i++) {
			//各変数のノットベクトルは単調増加列になっているのか
			for(int j=0;j<knot[i].length-1;j++) {
				if(knot[i][j]>knot[i][j+1]) {
					throw new IllegalArgumentException("ノットベクトルが単調増加列でありません:knot["+i+"]["+j+"]>knot["+i+"]["+j+1+"]");
				}
			}
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
	 * @param function
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
