package simulation.model3d;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class TestModelProperties {
	public static void main(String args[]) {
		try {
			ArrayList<Model> modelList;

			modelList = amfModelListGetter("D:\\アセンブリ.AMF", "amf");
			//modelList = cubeModelGetter(10,50,30);

			for(Model model:modelList) {
				if(model instanceof AMFModel) {
					AMFModel mod = (AMFModel) model;
					System.out.println("red:"+mod.red);
					System.out.println("green:"+mod.green);
					System.out.println("blue:"+mod.blue);
					System.out.println("triangle:"+mod.triangleMeshes.size());
					System.out.println("volume/m3:"+ModelProperties.calcTotalVolume(mod));
					double G[] = ModelProperties.calcGravityCenter(mod);
					System.out.println("CG:x:"+G[0]+",y:"+G[1]+",z:"+G[2]);
					double inertia[][] = ModelProperties.calcMomentOfInertia(mod);
					System.out.println("Ixx"+inertia[0][0]);
					System.out.println("Ixy"+inertia[0][1]);
					System.out.println("Iyy"+inertia[1][1]);
					System.out.println("Iyz"+inertia[1][2]);
					System.out.println("Izz"+inertia[2][2]);
					System.out.println("Izx"+inertia[2][0]);

					System.out.println("-------------------------------------");
				}

			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<Model> amfModelListGetter(String filePath, String extension) throws SAXException, IOException, ParserConfigurationException{
		return ModelHandler.loadModelFile(filePath, extension);
	}

	public static ArrayList<Model> cubeModelGetter(int a, int b, int c){
		ArrayList<Model> cubeList = new ArrayList<>();
		AMFModel cube = new AMFModel();
		cubeList.add(cube);

		float[][] v
			= {
				{0, 0, 0},
				{a, 0, 0},
				{a, b, 0},
				{0, b, 0},
				{0, 0, c},
				{a, 0, c},
				{a, b, c},
				{0, b, c},
			};

		Triangle[] triangles
			= {
				new Triangle(v[0], v[2], v[1]),
				new Triangle(v[2], v[0], v[3]),
				new Triangle(v[0], v[1], v[5]),
				new Triangle(v[0], v[5], v[4]),
				new Triangle(v[0], v[4], v[3]),
				new Triangle(v[3], v[4], v[7]),
				new Triangle(v[1], v[2], v[5]),
				new Triangle(v[2], v[6], v[5]),
				new Triangle(v[2], v[3], v[7]),
				new Triangle(v[2], v[7], v[6]),
				new Triangle(v[4], v[5], v[6]),
				new Triangle(v[4], v[6], v[7]),
			};

		for(Triangle triangle:triangles) {
			cube.addTriangle(triangle);
		}

		cube.mPrefix = 1;
		cube.specificGravity = 0.001f;

		return cubeList;
	}
}
