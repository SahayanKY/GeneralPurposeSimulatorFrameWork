package openGL;

import com.jogamp.opengl.GL2;

import simulation.model3d.Model;

public interface ModelPainter {
	public void paint(GL2 gl2, Model model);
}
