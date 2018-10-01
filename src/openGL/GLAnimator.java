package openGL;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.*;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.*;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import simulation.model3d.AMFModel;
import simulation.model3d.Model;
import simulation.model3d.NURBSSurfaceModel;


public class GLAnimator implements GLEventListener {
	private final GLU glu = new GLU();
	private float r=0;
	private FPSAnimator animator;
	private ArrayList<Model> modelList = null;
	private ArrayList<ModelPainter> painterList = null;

	public void setAnimationConfigure(GLAutoDrawable drawable, int fps, boolean scheduleAtFixedRate) {
		animator = new FPSAnimator(drawable, fps, scheduleAtFixedRate);
	}

	public void startAnimation() {
		if(animator == null) {
			return;
		}else {
			animator.start();
		}
	}

	public void changeStateAnimation() {
		if(animator == null) {
			return;
		}else {
			if(animator.isPaused()) {
				animator.resume();
			}else {
				animator.pause();
			}
		}
	}

	public void setModelList(ArrayList<Model> modelList){
		//this.modelList = modelList;

		double[] uknot = {0,0,1,1},vknot = {0,0,0,1,1,2,2,3,3,4,4,4};
		int p = 1,q = 2;
		double a = Math.sqrt(0.5);
		double[][][] ctrl=
			{
				{
					{1,20,0,-30},
					{a,20,30,-30},
					{1,20,30,0},
					{a,20,30,30},
					{1,20,0,30},
					{a,20,-30,30},
					{1,20,-30,0},
					{a,20,-30,-30},
					{1,20,0,-30}
				},
				{
					{1,-5,0,-30},
					{a,-5,30,-30},
					{1,-5,30,0},
					{a,-5,30,30},
					{1,-5,0,30},
					{a,-5,-30,30},
					{1,-5,-30,0},
					{a,-5,-30,-30},
					{1,-5,0,-30}
				}
			};
		try {
			NURBSSurfaceModel nmodel = new NURBSSurfaceModel(p, uknot, q, vknot, ctrl);
			this.modelList = new ArrayList<>();
			this.modelList.add(nmodel);
		}catch(Exception e) {
			e.printStackTrace();
		}



		this.painterList = new ArrayList<>();
		for(Model model:this.modelList) {
			if(model instanceof AMFModel) {
				painterList.add(new AMFPainter());
			}else if(model instanceof NURBSSurfaceModel) {
				painterList.add(new NURBSSurfacePainter());
			}
		}
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl2 = drawable.getGL().getGL2();
		gl2.glClearColor(0.8f, 0.8f, 0.8f, 1);
		gl2.glEnable(GL_DEPTH_TEST);
		gl2.glEnable(GL_CULL_FACE);
		gl2.glCullFace(GL_BACK);

		//ライトの設定
		gl2.glEnable(GL_LIGHTING);
		gl2.glEnable(GL_LIGHT0);
		//色の設定
		gl2.glLightfv(GL_LIGHT0, GL_DIFFUSE, new float[] {0.9f, 0.9f, 0.9f, 1},0);
		gl2.glLightfv(GL_LIGHT0, GL_SPECULAR, new float[] {0.9f, 0.9f, 0.9f, 1},0);
		gl2.glLightfv(GL_LIGHT0, GL_AMBIENT, new float[] {0.9f, 0.9f, 0.9f, 1},0);
		//位置の設定
		gl2.glLightfv(GL_LIGHT0, GL_POSITION, new float[] {0,300,300,0}, 0);

	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl2 = drawable.getGL().getGL2();
		gl2.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		gl2.glLoadIdentity();

		//視点の位置、向き
		glu.gluLookAt(120, 120, 120, 0, 0, 0, 0, 1, 0);
		gl2.glRotatef(-0.5f*r,0,1,0);


		gl2.glDisable(GL_LIGHTING);
		gl2.glBegin(GL_LINES);
			gl2.glColor3f(1.0f, 0, 0);
			gl2.glVertex3fv(new float[]{-5,0,0},0);
			gl2.glVertex3fv(new float[] {25,0,0},0);
		gl2.glEnd();

		gl2.glBegin(GL_LINES);
			gl2.glColor3f(0, 1.0f, 0);
			gl2.glVertex3fv(new float[]{0,-5,0},0);
			gl2.glVertex3fv(new float[] {0,25,0},0);
		gl2.glEnd();

		gl2.glBegin(GL_LINES);
			gl2.glColor3f(0, 0, 1.0f);
			gl2.glVertex3fv(new float[]{0,0,-5},0);
			gl2.glVertex3fv(new float[] {0,0,25},0);
		gl2.glEnd();

		gl2.glBegin(GL_LINES);
			gl2.glColor3f(1.0f, 1.0f, 1.0f);
			gl2.glVertex3fv(new float[]{-30,-30,-3},0);
			gl2.glVertex3fv(new float[] {30,30,3},0);
		gl2.glEnd();
		gl2.glEnable(GL_LIGHTING);


		if(modelList == null) {
			return;
		}else {

			// 図形の回転
			//gl2.glRotatef(r, 1.0f, 1.0f, 0.3f);

			int max = modelList.size();

			for(int i=0;i<max;i++) {
				painterList.get(i).paint(gl2, modelList.get(i));
			}

			if(r++ >= 720.0f) {
				r = 0;
			}
		}

		gl2.glDisable(GL_LIGHTING);
		gl2.glBegin(GL_LINES);
			gl2.glColor3f(1.0f,1.0f,0.0f);
			gl2.glVertex3fv(new float[] {0,0,20},0);
			gl2.glVertex3fv(new float[] {20,20,-30},0);
		gl2.glEnd();
		gl2.glEnable(GL_LIGHTING);

	}


	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl2 = drawable.getGL().getGL2();

		//gl2.glViewport(x, y, width, height);
		//JOGL内部で実行済み

		gl2.glMatrixMode(GL_PROJECTION);
		//透視変換行列を指定

		gl2.glLoadIdentity();
		//透視変換行列を単位行列にする

		//gl2.glOrthof(-1.0f*width/300, 1.0f*width/300, -1.0f*height/300, 1.0f*height/300, -1.0f, 1.0f);

		glu.gluPerspective(30.0, (double)width / (double)height, 1.0, 300.0);

		gl2.glMatrixMode(GL_MODELVIEW);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		if(animator != null) {
			animator.stop();
		}
	}


}
