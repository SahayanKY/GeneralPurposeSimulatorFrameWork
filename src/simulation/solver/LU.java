package simulation.solver;

public class LU extends LinearEquationSolver {

	@Override
	public double[] solve(double[][] a, double[] b) {
		if(!matrixIsNormal(a,b)) {
			throw new IllegalArgumentException("指定された配列は行数、列数が一致していません");
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

			//LU小行列に分解
			for(int i=j+1;i<a.length;i++) {
				a[i][j] = a[i][j]/a[j][j];
			}
			for(int i=j+1;i<a.length;i++) {
				for(int jj=j+1;jj<a.length;jj++) {
					a[i][jj] = a[i][jj] -a[i][j]*a[j][jj];
				}
			}
		}

		double[] x = b;

		//Ly=bをまず解く
		for(int i=0;i<a.length;i++) {
			for(int j=0;j<i;j++) {
				x[i] -= a[i][j]*x[j];
			}
			//Lの対角は1なので最後割る必要がない
		}

		//Ux=yを解く
		for(int i=a.length-1;i>=0;i--) {
			for(int j=a.length-1;j>i;j--) {
				x[i] -= a[i][j]*x[j];
			}
			x[i] /= a[i][i];
			if(Double.isInfinite(x[i])) {
				throw new IllegalArgumentException("指定された係数行列は正則ではありません");
			}
		}

		return x;
	}

	public static void main(String args[]) {
		LU solver = new LU();
		double[][] a = {
				{1,2,4,5,17},
				{8,2,-4,-14,5},
				{5,6,3,8,6},
				{6,7,8,2,12},
				{3,-4,-7,-22,-1}
		};
		double[] b = {
				4,-5,0,1,5
		};

		double[] x = solver.solve(a,b);
		for(double r:x) {
			System.out.println(r);
		}
	}

}
