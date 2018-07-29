package simulation.model3d;

public class Triangle {
	public float vertexs[][];
	public float normal[] = new float[3];

	public Triangle(float[] v1, float[] v2, float[] v3) {
		if(v1.length != 3 || v2.length != 3 || v3.length != 3) {
			throw new IllegalArgumentException("Triangle.Triangle(float[],float[],float[]):引数の頂点座標が異常です");
		}
		vertexs = new float[3][];

		vertexs[0] = v1;
		vertexs[1] = v2;
		vertexs[2] = v3;

		calcurateNormal();
	}

	public Triangle(float[][] vertexs) {
		if(vertexs.length != 3 || vertexs[0].length != 3 || vertexs[1].length != 3 || vertexs[2].length != 3) {
			throw new IllegalArgumentException("Triangle.Triangle(float[][]):引数の頂点座標が異常です");
		}
		this.vertexs = vertexs;

		calcurateNormal();
	}

	private void calcurateNormal() {
		//外積から面の法線ベクトルを計算する
		normal[0] = vertexs[0][1]*vertexs[1][2] +vertexs[1][1]*vertexs[2][2] +vertexs[2][1]*vertexs[0][2] -vertexs[0][1]*vertexs[2][2] -vertexs[1][1]*vertexs[0][2] -vertexs[2][1]*vertexs[1][2];
		normal[1] = vertexs[0][2]*vertexs[1][0] +vertexs[1][2]*vertexs[2][0] +vertexs[2][2]*vertexs[0][0] -vertexs[0][2]*vertexs[2][0] -vertexs[1][2]*vertexs[0][0] -vertexs[2][2]*vertexs[1][0];
		normal[2] = vertexs[0][0]*vertexs[1][1] +vertexs[1][0]*vertexs[2][1] +vertexs[2][0]*vertexs[0][1] -vertexs[0][0]*vertexs[2][1] -vertexs[1][0]*vertexs[0][1] -vertexs[2][0]*vertexs[1][1];
		float normalNorm = (float) Math.sqrt(normal[0]*normal[0] +normal[1]*normal[1] +normal[2]*normal[2]);
		//正規化
		normal[0] /= normalNorm;
		normal[1] /= normalNorm;
		normal[2] /= normalNorm;
	}

}
