package openGL;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.*;

import com.jogamp.opengl.GL2;

import simulation.model3d.Model;
import simulation.model3d.NURBSSurfaceModel;

public class NURBSSurfacePainter implements ModelPainter {
	int uN = 100,vN = 100; //u方向、v方向の分割数


	public void paint(GL2 gl2, Model model) {
		NURBSSurfaceModel m = null;
		if(model instanceof NURBSSurfaceModel) {
			m = (NURBSSurfaceModel) model;
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
		for(int j=0;j<=vN;j++) {
			//一番最初のループでは(0,v)の点の座標を先に計算する必要がある
			//v0は上の例で言うところの1,3,5...番目の頂点

			//計算誤差によりfunc()にIllegalArgumentExceptionをスローされるため
			double v = (j == vN)? vMax : vMin +(vMax -vMin)/vN*j;

			double[] vertex0 = m.func(uMin, v);
			memory[j] = new float[]{(float)vertex0[0],(float)vertex0[1],(float)vertex0[2]};
		}

		for(int i=0;i<uN;i++) {
			gl2.glBegin(GL_TRIANGLE_STRIP);
			gl2.glNormal3fv(new float[] {0,0,-1},0);

			for(int j=0;j<=vN;j++) {
				//以降のループでは前回既に計算済みの頂点座標を使うので、memoryを参照する
				//memory[j]がv0に対応する
				gl2.glVertex3fv(memory[j],0);

				//計算誤差によりfunc()にIllegalArgumentExceptionをスローされるため
				double u = (i == uN-1)? uMax : uMin +(uMax -uMin)/uN*(i+1),
						v = (j == vN)? vMax : vMin +(vMax -vMin)/vN*j;

				//v1は2,4,6,...番目の頂点に対応する
				double[] vertex1 = m.func(u, v);
				float[] fv1 = {(float)vertex1[0],(float)vertex1[1],(float)vertex1[2]};
				gl2.glVertex3fv(fv1,0);
				memory[j] = fv1;
			}
			gl2.glEnd();
		}

		gl2.glEnable(GL_CULL_FACE);

		gl2.glDisable(GL_LIGHTING);

		gl2.glLineWidth(3.0f);
		//u方向のノットを結ぶ線を引く(u方向に垂直)
		for(int i=0;i<m.uknot.length;i++) {
			//多重ノットは処理してもしょうがないので次のループへ
			if(i > 0 && m.uknot[i] == m.uknot[i-1]) {
				continue;
			}
			gl2.glBegin(GL_LINE_STRIP);
			for(int j=0;j<=vN;j++) {
				double u = m.uknot[i],
						v = (j == vN)? vMax : vMin +(vMax-vMin)/vN*j;

				double[] vertex = m.func(u, v);
				gl2.glVertex3fv(new float[] {(float)vertex[0],(float)vertex[1],(float)vertex[2]},0);
			}
			gl2.glEnd();
		}

		//v方向のノットを結ぶ線を引く(v方向に垂直)
		for(int j=0;j<m.vknot.length;j++) {
			//多重ノットは処理してもしょうがないので次のループへ
			if(j > 0 && m.vknot[j] == m.vknot[j-1]) {
				continue;
			}
			gl2.glBegin(GL_LINE_STRIP);
			for(int i=0;i<=uN;i++) {
				double u = (i == uN)? uMax : uMin +(uMax -uMin)/uN*i,
						v = m.vknot[j];

				double[] vertex = m.func(u, v);
				gl2.glVertex3fv(new float[] {(float)vertex[0],(float)vertex[1],(float)vertex[2]},0);
			}
			gl2.glEnd();
		}

		gl2.glEnable(GL_LIGHTING);

		gl2.glLineWidth(1);

	}
}
