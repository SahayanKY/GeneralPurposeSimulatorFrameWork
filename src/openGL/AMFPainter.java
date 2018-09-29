package openGL;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.*;

import com.jogamp.opengl.GL2;

import simulation.model3d.AMFModel;
import simulation.model3d.Model;
import simulation.model3d.Triangle;

public class AMFPainter implements ModelPainter{
	public void paint(GL2 gl2, Model model) {
		AMFModel m = null;
		if(model instanceof AMFModel) {
			m = (AMFModel) model;
		}else {
			throw new IllegalArgumentException("指定されたインスタンスはAMFModelインスタンスでありません");
		}
		// 図形の描画
		gl2.glMaterialfv(GL_FRONT, GL_DIFFUSE, new float[] {m.red,m.green,m.blue,1}, 0);
		gl2.glMaterialfv(GL_FRONT, GL_SPECULAR, new float[] {m.red,m.green,m.blue,1},0);
		gl2.glMaterialfv(GL_FRONT, GL_AMBIENT, new float[] {0.0215f, 0.0245f, 0.0215f,1}, 0);
		gl2.glMaterialfv(GL_FRONT, GL_SHININESS, new float[] {76.8f},0);

		gl2.glBegin(GL_TRIANGLES);
		for(Triangle triangle:m.triangleMeshes) {
			//法線を指定することで光の反射などによる実際の色の計算が行われる
			gl2.glNormal3fv(triangle.normal,0);

			//このコマンドは実際の結果に影響しなかった
			//→上でglMaterialfv()で指定しているのがきいているのか？
			//gl2.glColor3fv(new float[] {0.8f,0.8f,0.8f}, 0);
			for(int nodeIndex=0;nodeIndex<3;nodeIndex++) {
				gl2.glVertex3fv(triangle.vertexs[nodeIndex],0);
			}
		}
		gl2.glEnd();
	}
}
