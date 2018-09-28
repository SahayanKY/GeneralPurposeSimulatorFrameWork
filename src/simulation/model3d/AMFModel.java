package simulation.model3d;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.*;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;

public class AMFModel extends Model{
	public final ArrayList<Triangle> triangleMeshes = new ArrayList<>();

	public void addTriangle(Triangle triangle) {
		triangleMeshes.add(triangle);
	}

	@Override
	public void paint(GL2 gl2) {
		// 図形の描画
		gl2.glMaterialfv(GL_FRONT, GL_DIFFUSE, new float[] {this.red,this.green,this.blue,1}, 0);
		gl2.glMaterialfv(GL_FRONT, GL_SPECULAR, new float[] {this.red,this.green,this.blue,1},0);
		gl2.glMaterialfv(GL_FRONT, GL_AMBIENT, new float[] {0.0215f, 0.0245f, 0.0215f,1}, 0);
		gl2.glMaterialfv(GL_FRONT, GL_SHININESS, new float[] {76.8f},0);

		gl2.glBegin(GL_TRIANGLES);
		for(Triangle triangle:this.triangleMeshes) {
			gl2.glNormal3fv(triangle.normal,0);
			for(int nodeIndex=0;nodeIndex<3;nodeIndex++) {
				gl2.glVertex3fv(triangle.vertexs[nodeIndex],0);
			}
		}
		gl2.glEnd();
	}
}
