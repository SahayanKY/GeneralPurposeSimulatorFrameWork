package simulation.model3d;

import java.util.ArrayList;

public class Model {
	public float specificGravity=0;
	public float red=0,green=0,blue=0;
	public float mPrefix=0;
	public final ArrayList<Triangle> triangleMeshes = new ArrayList<>();

	public Model() {}

	public void addTriangle(Triangle triangle) {
		triangleMeshes.add(triangle);
	}

	public void setColor(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public void setMaterialDencity(float density) {
		this.specificGravity = density;
	}
}
