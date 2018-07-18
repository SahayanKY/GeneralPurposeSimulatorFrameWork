package openGL;

import java.util.ArrayList;

public class Model {
	public float density=0;
	public float red=0,green=0,blue=0;
	public ArrayList<Triangle> mesh = new ArrayList<>();

	public Model() {}

	public void addTriangle(Triangle triangle) {
		mesh.add(triangle);
	}

	public void setColor(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public void setMaterialDencity(float density) {
		this.density = density;
	}
}
