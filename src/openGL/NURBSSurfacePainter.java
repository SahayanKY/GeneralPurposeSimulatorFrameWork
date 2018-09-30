package openGL;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.*;

import com.jogamp.opengl.GL2;

import simulation.model3d.Model;
import simulation.model3d.NURBSModel;

public class NURBSSurfacePainter implements ModelPainter {
	int uN = 100,vN = 100; //u方向、v方向の分割数


	public void paint(GL2 gl2, Model model) {
		NURBSModel m = null;
		if(model instanceof NURBSModel) {
			m = (NURBSModel) model;
		}else {
			throw new IllegalArgumentException("指定されたインスタンスはNURBSModelインスタンスでありません");
		}

		//色の設定
		gl2.glMaterialfv(GL_FRONT, GL_DIFFUSE, new float[] {0.2f,0.8f,0,1}, 0);
		gl2.glMaterialfv(GL_FRONT, GL_SPECULAR, new float[] {0.2f,0.8f,0,1},0);
		gl2.glMaterialfv(GL_FRONT, GL_AMBIENT, new float[] {0.0215f, 0.0245f, 0.0215f,1}, 0);
		gl2.glMaterialfv(GL_FRONT, GL_SHININESS, new float[] {76.8f},0);


		double uMin = m.uknot[0],
				uMax = m.uknot[m.uknot.length-1],
				vMin = m.vknot[0],
				vMax = m.vknot[m.vknot.length-1];

		float memory[][] = new float[vN+1][];


		gl2.glDisable(GL_CULL_FACE);

		//(u,v)の頂点に対して、
		//(0,0),(1,0),(0,1),(1,1),...,(1,0),(2,0),(1,1),(2,1)と進めていく
		//重複する頂点はmemoryに保持しておく
		for(int i=0;i<uN;i++) {
			gl2.glBegin(GL_TRIANGLE_STRIP);
			gl2.glNormal3fv(new float[] {0,0,-1},0);

			for(int j=0;j<=vN;j++) {
				if(i == 0) {
					//一番最初のループでは(0,v)の点の座標を先に計算する必要がある
					//v0は上の例で言うところの1,3,5...番目の頂点
					double[] v0 = m.func(uMin, vMin +(vMax -vMin)/vN*j);
					memory[j] = new float[]{(float)v0[0],(float)v0[1],(float)v0[2]};
				}

				//以降のループでは前回既に計算済みの頂点座標を使うので、memoryを参照する
				//memory[j]がv0に対応する
				gl2.glVertex3fv(memory[j],0);


				//v1は2,4,6,...番目の頂点に対応する
				double[] v1 = m.func(uMin +(uMax -uMin)/uN*(i+1), vMin +(vMax -vMin)/vN*j);
				float[] fv1 = {(float)v1[0],(float)v1[1],(float)v1[2]};
				gl2.glVertex3fv(fv1,0);
				memory[j] = fv1;
			}
			gl2.glEnd();
		}

		gl2.glEnable(GL_CULL_FACE);

	}
}
