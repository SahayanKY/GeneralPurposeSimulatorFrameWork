package simulation.model3d;

import java.util.ArrayList;

public class AMFModel extends Model{
	public final ArrayList<Triangle> triangleMeshes = new ArrayList<>();

	public void addTriangle(Triangle triangle) {
		triangleMeshes.add(triangle);
	}

}
