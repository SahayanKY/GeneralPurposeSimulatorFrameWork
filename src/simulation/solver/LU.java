package simulation.solver;

public class LU extends LinearEquationSolver {
	private boolean isreuse=false;
	/*isreuseがtrueのとき、nullかどうかは保証されないが、
	falseのときはnullであることが保証される。*/
	private double[][] LU;
	/*LU分解過程の行入れ替えの結果を保持しておく。
	 *LU==nullのときはnullである
	 *i番目の入れ替え時に、i行目とlineorder[i]行目を入れ替えた
	 **/
	private int[] lineorder;

	@Override
	/*
	 * 連立方程式をLU分解を利用して計算します。
	 * 指定された係数行列が正則でなく、解が一意に求まらない場合、
	 * IllegalArgumentExceptionがスローされます。
	 * また、isToReuseLU(boolean)でtrueを指定した場合、
	 * 以前計算されたLU分解結果を利用して方程式が解かれます。
	 * このため、以前した係数行列とこれから指定する係数行列が
	 * 異なる場合、正しい結果を返しません。その場合、resetLU()を利用してください。
	 * また、LU分解結果を再度用いて計算する際に、Aにnullを指定しても構いません。
	 * @param A 連立方程式の係数行列
	 * @param B 連立方程式の右辺項ベクトル
	 * */
	public double[] solve(double[][] A, double[] B) {
		double[][] a;
		double[] b;
		if((LU == null && !matrixIsNormal(A,B)) || (LU != null && LU.length != B.length)) {
			//LUがnullのとき
			//→A,Bはどちらも正しく指定されてなければならない
			//LUがnullでないとき
			//→LUと指定されたBの行数が合ってなければならない
			throw new IllegalArgumentException("指定された配列は行数、列数が一致していません");
		}

		if(LU!=null) {
			//LU結果があって、それを用いる場合
			a = LU;
			if(this.changeArray) {
				//bを変化させてもいい場合
				b = B;
			}else {
				b = new double[LU.length];
				//初期化
				for(int i=0;i<LU.length;i++) {
					b[i] = B[i];
				}
			}

			//bの行入れ替えを行う
			for(int i=0;i<LU.length;i++) {
				//今i番目にある値を保管する
				double bi = b[i];

				//pivot番目=lineorder[i]番目にある値をb[i]に移動させる
				b[i] = b[lineorder[i]];
				b[lineorder[i]] = bi;
			}


		}else {
			//LU結果が存在しない場合

			//a,bの初期化
			if(this.changeArray) {
				//配列を変えていい場合
				a = A;
				b = B;
			}else {
				//仮引数の配列を変化させない設定の場合
				a = new double[A.length][A.length];
				b = new double[A.length];
				//初期化
				for(int i=0;i<a.length;i++) {
					for(int j=0;j<a.length;j++) {
						a[i][j] = A[i][j];
					}
					b[i] = B[i];
				}
			}

			//既存のLU結果を用いないため、LU分解を行う
			//→aに分解結果を保存
			//	→this.isreuseならばLUにaを保存
			//→this.isreuseならばlineorderに行入れ替えの結果を保持する

			if(this.isreuse) {
				//lineorderを初期化する
				lineorder = new int[a.length];
				for(int i=0;i<a.length;i++) {
					//並び替えが全くない状態に初期化
					lineorder[i] = i;
				}
			}

			//Lの対角成分が全て1のLUに分解する
			for(int j=0;j<a.length;j++) {
				//ピボット位置の取得
				int pivot=j;
				//絶対値の最も大きい位置をpivotに取得
				double pivotValue=0;
				for(int i=j;i<a.length;i++) {
					if(pivotValue < Math.abs(a[i][j])) {
						pivotValue = Math.abs(a[i][j]);
						pivot = i;
					}
				}

				//入れ替え
				//ピボット位置を一時的に入れる
				double[] tempAi = a[pivot];
				double tempb= b[pivot];

				a[pivot] = a[j];
				b[pivot] = b[j];
				a[j] = tempAi;
				b[j] = tempb;

				if(this.isreuse) {
					//行入れ替えを記録
					lineorder[j] = pivot;
				}

				//LU小行列に分解
				for(int i=j+1;i<a.length;i++) {
					a[i][j] = a[i][j]/a[j][j];
					if(Double.isNaN(a[i][j])) {
						throw new IllegalArgumentException("指定された係数行列は正則ではない可能性があります");
					}
				}
				for(int i=j+1;i<a.length;i++) {
					for(int jj=j+1;jj<a.length;jj++) {
						a[i][jj] = a[i][jj] -a[i][j]*a[j][jj];
					}
				}
			}

			//分解終了後
			if(this.isreuse) {
				LU = a;
			}
		}

		//以降aはLU(Lの対角は1)に分解された結果
		//以降bは一度使った要素を二度と使わないため、解を保存するメモリとして使っていく
		double[] x = b;

		//Ly=bをまず解く
		for(int i=0;i<a.length;i++) {
			for(int j=0;j<i;j++) {
				x[i] -= a[i][j]*x[j];
			}
			//Lの対角は1なので最後割る必要がない
		}

		//y（今配列xが参照しているやつ）も、一度使った要素を二度と使わないため、
		//上書きする形で解を求めていく
		//Ux=yを解く
		for(int i=a.length-1;i>=0;i--) {
			for(int j=a.length-1;j>i;j--) {
				x[i] -= a[i][j]*x[j];
			}
			x[i] /= a[i][i];
			if(Double.isInfinite(x[i])) {
				throw new IllegalArgumentException("指定された係数行列は正則ではない可能性があります");
			}
		}

		return x;
	}

	/*
	 * このソルバーが一度計算したLU分解結果を繰り返し使うかどうかを設定します。
	 * これでtrueを指定した場合、前に計算したLU分解の結果を利用して
	 * 方程式を解くため、非常に速くなります。
	 * falseを指定し、既にLU分解結果を保持していた場合、内部でLU分解結果はクリアされます。
	 * デフォルトはfalseです。
	 * @param reuse trueの時、LU分解結果を利用する。
	 * */
	public void isToReuseLU(boolean isreuse) {
		this.isreuse = isreuse;
		if(!this.isreuse) {
			this.resetLU();
		}
	}

	/*
	 * 以前計算されたLU分解結果をクリアします。
	 * 以前計算した連立方程式とこれから計算する連立方程式の係数行列が
	 * 異なる場合、これを利用してください。
	 * */
	public void resetLU() {
		this.LU = null;
		this.lineorder = null;
	}

	public static void main(String args[]) {
		LU solver = new LU();
		double[][] a = {
				{1,2,4,5,17},
				{8,2,-4,-14,5},
				{5,6,3,8,6},
				{6,7,8,2,12},
				{3,-4,-7,-22,0}
		};
		double[] b = {
				4,-5,0,1,5
		};

		solver.isToReuseLU(true);
		solver.changeArray(true);
		double[] x = solver.solve(a,b);
		for(double r:x) {
			System.out.println(r);
		}

		x = solver.solve(null,new double[] {4,-5,0,1,5});
		for(double r:x) {
			System.out.println(r);
		}

		x = solver.solve(null,new double[] {4,-5,0,1,5});
		for(double r:x) {
			System.out.println(r);
		}

	}

}
