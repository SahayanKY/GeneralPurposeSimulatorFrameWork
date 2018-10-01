package openGL;

import com.jogamp.opengl.GL2;

public class TwoDScalarFunctionPainter {
	public boolean
		paint_contourline = true,
		paint_color = false,
		paint_3Dgraph = false;

	public int uN = 100, vN = 100;

	public void paint(GL2 gl2) {
		//2次元面、等高線付き、色なしのグラフ
		double f[][][] = new double[uN+1][vN+1][];

		double uMin = 0,
				uMax = 10,
				vMin = 0,
				vMax = 10;

		for(int i=0;i<=uN;i++) {
			for(int j=0;j<=vN;j++) {
				double u = (i == uN)? uN : uMin +(uMax -uMin)/uN*i,
						v = (j == vN)? vN : vMin +(vMax -vMin)/vN*j;

				f[i][j] = func(u,v);
				//f[i][j][0]:x成分,
				//f[i][j][1]:y成分,
				//f[i][j][2]:z成分
				//z成分を高さとするようなグラフを描画する
			}
		}

	}

	private double[] func(double u, double v) {
		return null;
	}
}