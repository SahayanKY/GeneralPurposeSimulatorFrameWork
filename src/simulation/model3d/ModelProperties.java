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

	/*
	 * MKSA系(m)でこのmodelの重心を返す
	 * */
	public static double[][] calcMomentOfInertia(Model model) {
		int triangleN = model.triangleMeshes.size();
		int arraySize = triangleN/100+1;

		double[]
			ar_int_xx_p = new double[arraySize],
			ar_int_xx_n = new double[arraySize],
			ar_int_xy_p = new double[arraySize],
			ar_int_xy_n = new double[arraySize],
			ar_int_yy_p = new double[arraySize],
			ar_int_yy_n = new double[arraySize],
			ar_int_yz_p = new double[arraySize],
			ar_int_yz_n = new double[arraySize],
			ar_int_zz_p = new double[arraySize],
			ar_int_zz_n = new double[arraySize],
			ar_int_zx_p = new double[arraySize],
			ar_int_zx_n = new double[arraySize];

		int resultSetIndex = 0;
		for(int triIndex=0;triIndex<triangleN;triIndex++) {
			float[]	vertex0 = model.triangleMeshes.get(triIndex).vertexs[0],
					vertex1 = model.triangleMeshes.get(triIndex).vertexs[1],
					vertex2 = model.triangleMeshes.get(triIndex).vertexs[2];

			float 	x0 = vertex0[0], x1 = vertex1[0], x2 = vertex2[0],
					y0 = vertex0[1], y1 = vertex1[1], y2 = vertex2[1],
					z0 = vertex0[2], z1 = vertex1[2], z2 = vertex2[2];

			double Sxp,Sxn,Syp,Syn,Szp,Szn,int_x3,int_y3,int_z3,int_xyz;

			Sxp = y1 *z2 +y0 *z1 +y2 *z0;
			Sxn = -y1 *z0 -y0 *z2 -y2 *z1;
			Syp = z1 *x2 +z0 *x1 +z2 *x0;
			Syn = -z1 *x0 -z0 *x2 -z2 *x1;
			Szp = x1 *y2 +x0 *y1 +x2 *y0;
			Szn = -x1 *y0 -x0 *y2 -x2 *y1;

			int_x3 = x1*x1*(x1 +x2 +x0) +x1 *(x2*x2 +x2*x0 +x0*x0)
					+ x2*x2*(x2 +x0) +x0*x0*(x2 +x0);
			int_y3 = y1*y1*(y1 +y2 +y0) +y1 *(y2*y2 +y2*y0 +y0*y0)
					+ y2*y2*(y2 +y0) +y0*y0*(y2 +y0);
			int_z3 = z1*z1*(z1 +z2 +z0) +z1 *(z2*z2 +z2*z0 +z0*z0)
					+ z2*z2*(z2 +z0) +z0*z0*(z2 +z0);
			int_xyz = 6*x0*y0*z0 +2*x0*y0*z1 +2*x0*y0*z2
					+2*x0*y1*z0 +2*x0*y1*z1 +x0*y1*z2
					+2*x0*y2*z0 +x0*y2*z1 +2*x0*y2*z2
					+2*x1*y0*z0 +2*x1*y0*z1 +x1*y0*z2
					+2*x1*y1*z0 +6*x1*y1*z1 +2*x1*y1*z2
					+x1*y2*z0 +2*x1*y2*z1 +2*x1*y2*z2
					+2*x2*y0*z0 +x2*y0*z1 +2*x2*y0*z2
					+x2*y1*z0 +2*x2*y1*z1 +2*x2*y1*z2
					+2*x2*y2*z0 +2*x2*y2*z1 +6*x2*y2*z2;

			ar_int_xx_p[resultSetIndex] += Sxp *int_x3;
			ar_int_xx_n[resultSetIndex] += Sxn *int_x3;
			ar_int_xy_p[resultSetIndex] += Szp *int_xyz;
			ar_int_xy_n[resultSetIndex] += Szn *int_xyz;
			ar_int_yy_p[resultSetIndex] += Syp *int_y3;
			ar_int_yy_n[resultSetIndex] += Syn *int_y3;
			ar_int_yz_p[resultSetIndex] += Sxp *int_xyz;
			ar_int_yz_n[resultSetIndex] += Sxn *int_xyz;
			ar_int_zz_p[resultSetIndex] += Szp *int_z3;
			ar_int_zz_n[resultSetIndex] += Szn *int_z3;
			ar_int_zx_p[resultSetIndex] += Syp *int_xyz;
			ar_int_zx_n[resultSetIndex] += Syn *int_xyz;

			resultSetIndex++;
			if(resultSetIndex == arraySize) {
				resultSetIndex = 0;
			}
		}

		double
			int_xx_p=0,	int_xx_n=0,
			int_xy_p=0,	int_xy_n=0,
			int_yy_p=0,int_yy_n=0,
			int_yz_p=0,int_yz_n=0,
			int_zz_p=0,int_zz_n=0,
			int_zx_p=0,int_zx_n=0,
			int_xx,int_xy,int_yy,int_yz,int_zz,int_zx;


		for(int i=0;i<arraySize;i++) {
			int_xx_p += ar_int_xx_p[i];
			int_xx_n += ar_int_xx_n[i];
			int_xy_p += ar_int_xy_p[i];
			int_xy_n += ar_int_xy_n[i];
			int_yy_p += ar_int_yy_p[i];
			int_yy_n += ar_int_yy_n[i];
			int_yz_p += ar_int_yz_p[i];
			int_yz_n += ar_int_yz_n[i];
			int_zz_p += ar_int_zz_p[i];
			int_zz_n += ar_int_zz_n[i];
			int_zx_p += ar_int_zx_p[i];
			int_zx_n += ar_int_zx_n[i];
		}

		int_xx = (int_xx_p +int_xx_n)/60.0;
		int_xy = (int_xy_p +int_xy_n)/120.0;
		int_yy = (int_yy_p +int_yy_n)/60.0;
		int_yz = (int_yz_p +int_yz_n)/120.0;
		int_zz = (int_zz_p +int_zz_n)/60.0;
		int_zx = (int_zx_p +int_zx_n)/120.0;

		double[][] moment = new double[3][3];
		//比重:specificGravity：g/cm3,
		//int_xx...: mPrefix^5
		//単位をMKSAに直す

		//g/cm3 = 10^3 kg/m3

		moment[0][0] = 1000*Math.pow(model.mPrefix,5) *model.specificGravity*(int_yy +int_zz);
		moment[0][1] = moment[1][0] = -1000*Math.pow(model.mPrefix,5) *model.specificGravity *int_xy;
		moment[0][2] = moment[2][0] = -1000*Math.pow(model.mPrefix,5) *model.specificGravity *int_zx;
		moment[1][1] = 1000*Math.pow(model.mPrefix,5) *model.specificGravity *(int_xx +int_zz);
		moment[1][2] = moment[2][1] = -1000*Math.pow(model.mPrefix,5) *model.specificGravity *int_yz;
		moment[2][2] = 1000*Math.pow(model.mPrefix,5) *model.specificGravity *(int_xx +int_yy);

		return moment;
	}
}
