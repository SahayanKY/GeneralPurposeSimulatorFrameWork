package simulation.model3d;

public abstract class ModelProperties {
	/*
	 * MKSA系(kg)でこのmodelの質量を返す
	 * */
	public static double calcTotalMass(Model model) {
		return model.specificGravity *1000 *calcTotalVolume(model);
	}

	/*
	 * MKSA系(m3)でこのmodelの体積を返す
	 * */
	public static double calcTotalVolume(Model model) {
		//Gaussの定理を利用しているのは下と同じ
		//正になるものと負になるものを分けて足し合わせていき、最後にその2つを足す

		double result = 0,
				posiResultSet[] = new double[model.triangleMeshes.size()/100+1],
				negaResultSet[] = new double[model.triangleMeshes.size()/100+1];
		int i=0;
		for(Triangle tri:model.triangleMeshes) {
			float[]	vertex0 = tri.vertexs[0],
					vertex1 = tri.vertexs[1],
					vertex2 = tri.vertexs[2];
			double posiRes,negaRes;
			posiRes = (vertex0[1] *vertex1[2] +vertex1[1] *vertex2[2] +vertex2[1] *vertex0[2])*(vertex0[0] +vertex1[0] +vertex2[0]);
			negaRes = -1*(vertex0[1] *vertex2[2] +vertex1[1] *vertex0[2] +vertex2[1] *vertex1[2])*(vertex0[0] +vertex1[0] +vertex2[0]);

			if(posiRes > 0) {
				posiResultSet[i] += posiRes;
			}else {
				negaResultSet[i] += posiRes;
			}

			if(negaRes > 0) {
				posiResultSet[i] += negaRes;
			}else {
				negaResultSet[i] += negaRes;
			}

			i++;
			if(i == posiResultSet.length) {
				i=0;
			}
		}

		for(int j=1;j<posiResultSet.length;j++) {
			posiResultSet[0] += posiResultSet[j];
			negaResultSet[0] += negaResultSet[j];
		}

		result = (posiResultSet[0] +negaResultSet[0])/6.0;

/*
		//Gaussの定理から体積積分問題を表面積分問題に変換する
		//丸め誤差を回避するため、100毎に分割して足していく
		double[] resultSet=new double[model.triangleMeshes.size()/100+1];
		int i=0;

		for(Triangle tri:model.triangleMeshes) {
			float[][] vertexs = tri.vertexs;
			//外積から面の法線ベクトル*面積を計算する
			double Sx = (double)vertexs[0][1]*vertexs[1][2] +(double)vertexs[1][1]*vertexs[2][2] +(double)vertexs[2][1]*vertexs[0][2] -(double)vertexs[0][1]*vertexs[2][2] -(double)vertexs[1][1]*vertexs[0][2] -(double)vertexs[2][1]*vertexs[1][2];
			double Sy = (double)vertexs[0][2]*vertexs[1][0] +(double)vertexs[1][2]*vertexs[2][0] +(double)vertexs[2][2]*vertexs[0][0] -(double)vertexs[0][2]*vertexs[2][0] -(double)vertexs[1][2]*vertexs[0][0] -(double)vertexs[2][2]*vertexs[1][0];
			double Sz = (double)vertexs[0][0]*vertexs[1][1] +(double)vertexs[1][0]*vertexs[2][1] +(double)vertexs[2][0]*vertexs[0][1] -(double)vertexs[0][0]*vertexs[2][1] -(double)vertexs[1][0]*vertexs[0][1] -(double)vertexs[2][0]*vertexs[1][1];

			resultSet[i] += (vertexs[0][0]*Sx +vertexs[0][1]*Sy +vertexs[0][2]*Sz);
			i++;
			if(i==resultSet.length) {
				i=0;
			}
		}

		double result=0;
		for(double dresult:resultSet) {
			result += dresult;
		}
		result /= 6.0;
*/

		//単位変換
		result *= Math.pow(model.mPrefix,3);

		return result;
	}

	/*
	 * MKSA系(m)でこのmodelの重心を返す
	 * */
	public static double[] calcGravityCenter(Model model) {
		double volume = calcTotalVolume(model);
		double result[] = new double[3];

		for(int i=0;i<3;i++) {
			int triangleN = model.triangleMeshes.size();
			double[] posiResultSet = new double[triangleN/100+1],
					negaResultSet = new double[triangleN/100+1];

			int resultSetIndex = 0;
			for(int triIndex=0;triIndex<triangleN;triIndex++) {
				float[]	vertex0 = model.triangleMeshes.get(triIndex).vertexs[0],
						vertex1 = model.triangleMeshes.get(triIndex).vertexs[1],
						vertex2 = model.triangleMeshes.get(triIndex).vertexs[2];
				double posiRes,negaRes,common;
				common = vertex0[i] *vertex0[i] +vertex1[i] *vertex1[i] +vertex2[i] *vertex2[i]
						+vertex0[i] *vertex1[i] +vertex1[i] *vertex2[i] +vertex2[i] *vertex0[i];
				posiRes = (vertex0[(i+1)%3] *vertex1[(i+2)%3] +vertex1[(i+1)%3] *vertex2[(i+2)%3] +vertex2[(i+1)%3] *vertex0[(i+2)%3])*common;
				negaRes = -1*(vertex0[(i+1)%3] *vertex2[(i+2)%3] +vertex1[(i+1)%3] *vertex0[(i+2)%3] +vertex2[(i+1)%3] *vertex1[(i+2)%3])*common;

				if(posiRes > 0) {
					posiResultSet[resultSetIndex] += posiRes;
				}else {
					negaResultSet[resultSetIndex] += posiRes;
				}

				if(negaRes > 0) {
					posiResultSet[resultSetIndex] += negaRes;
				}else {
					negaResultSet[resultSetIndex] += negaRes;
				}

				resultSetIndex++;
				if(resultSetIndex == posiResultSet.length) {
					resultSetIndex = 0;
				}
			}

			//インデックス1に結果の数値を足し合わせていく
			for(int index=1;index<posiResultSet.length;index++) {
				posiResultSet[0] += posiResultSet[index];
				negaResultSet[0] += negaResultSet[index];
			}

			//単位変換と係数をかける
			result[i] = (posiResultSet[0]+negaResultSet[0])*Math.pow(model.mPrefix,4)/24.0/volume;
		}

		return result;
	}
}
