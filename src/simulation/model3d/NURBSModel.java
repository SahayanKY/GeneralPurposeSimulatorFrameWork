package simulation.model3d;

public class NURBSModel extends Model{
	public final double[] uknot, vknot;
	public final double[][][] ctrl;
	public final int p,q;

	/*
	 * ctrlsの1番目のインデックスはu方向、2番目のインデックスはv方向の
	 * ノットに対応する。
	 * p,qは多項式の次数
	 * コントロールポイントは(重み、x座標、y座標、z座標)の順に指定する
	 * */
	public NURBSModel(int p, double[] uknot, int q, double[] vknot, double[][][] ctrl){
		//次数が負数になっていないか
		if(p < 0 || q < 0) {
			throw new IllegalArgumentException("次数が不正です");
		}
		//ノットベクトルが単調増加になっているか
		for(int i=0;i<uknot.length-1;i++) {
			if(uknot[i] > uknot[i+1]) {
				throw new IllegalArgumentException("ノットベクトルが単調増加でありません");
			}
		}
		for(int j=0;j<vknot.length-1;j++) {
			if(vknot[j] > vknot[j+1]) {
				throw new IllegalArgumentException("ノットベクトルが単調増加でありません");
			}
		}
		//ノットの数と次数とポイントの数のつじつまがあっているか
		if(uknot.length != ctrl.length +p+1 || vknot.length != ctrl[0].length +q+1) {
			throw new IllegalArgumentException("コントロールポイント数、ノット数、次数のつじつまが合っていません");
		}

		/*
		 * v方向のポイント数は一定になっているか
		 * ctrlsの各要素は重みと3次元の計4要素の配列になっているか
		 * また、座標値には先に重みをかけておく
		 */
		for(int i=0;i<ctrl.length;i++) {
			if(ctrl[i].length != ctrl[0].length) {
				throw new IllegalArgumentException("v方向のコントロールポイントの数が一定でありません");
			}
			for(int j=0;j<ctrl[i].length;j++) {
				if(ctrl[i][j].length != 4) {
					throw new IllegalArgumentException("コントロールポイントの値が4要素になっていません");
				}
				for(int k=1;k<4;k++) {
					ctrl[i][j][k] *= ctrl[i][j][0];
				}
			}
		}

		this.p = p;
		this.q = q;
		this.uknot = uknot;
		this.vknot = vknot;
		this.ctrl = ctrl;
	}

	public double[] func(double u, double v) {
		if(u < uknot[0] || uknot[uknot.length-1] < u || v < vknot[0] || vknot[vknot.length-1] < v) {
			throw new IllegalArgumentException("u,vの指定がノットの範囲に対して異常です:(u,v)=("+u+","+v+")");
		}

		int k=0,h=0; //u_k <= u < u_k+1 , v_h <= v < v_h+1

		if(u == uknot[uknot.length-1]) {
			//uがノットの最後端に等しい時、（値が違う）一つ前のノットを指定する
			k = uknot.length -p -2;
		}else {
			for(int i=0; i<uknot.length;i++) {
				if(u < uknot[i]) {
					k = i-1; //直前の位置がk
					break;
				}
			}
		}

		//vについても同様にしてhを求める
		if(v == vknot[vknot.length -1]) {
			h = vknot.length -q -2;
		}else {
			for(int i=0; i<vknot.length;i++) {
				if(v < vknot[i]) {
					h = i-1;
					break;
				}
			}
		}

		//先にv方向について和を取り、次にu方向について和を取る
		//tempC2D[][q][]にu方向について和を取る時に使う結果を保存する
		double[][][] tempC2D = new double[p+1][q+1][4];
		for(int i=0;i<=p;i++) {
			for(int j=0;j<=q;j++) {
				for(int d=0;d<4;d++) {
					tempC2D[i][j][d] = ctrl[i+k-p][j+h-q][d];
				}
			}
		}

		for(int r=1; r<=q;r++) {
			for(int j=q;j>=r;j--) {
				double a = (v -vknot[j+h-q])/(vknot[j+1+h-r] -vknot[j+h-q]);
				for(int i=0;i<=p;i++) {
					for(int d=0;d<4;d++) {
						tempC2D[i][j][d] = (1-a)*tempC2D[i][j-1][d] +a*tempC2D[i][j][d];
					}
				}
			}
		}

		//こっから先は曲線のdeBoorとほぼ同じ
		//tempC2D[][q][]は曲線のtempCtrls[][]に対応する
		for(int r=1;r<=p;r++) {
			for(int j=p;j>=r;j--) {
				double a = (u -uknot[j+k-p])/(uknot[j+1+k-r] -uknot[j+k-p]);
				for(int d=0;d<4;d++) {
					tempC2D[j][q][d] = (1-a)*tempC2D[j-1][q][d] +a*tempC2D[j][q][d];
				}
			}
		}

		//重みの分を処理して提出
		double result[] = new double[3];
		for(int d=0;d<3;d++) {
			result[d] = tempC2D[p][q][d+1]/tempC2D[p][q][0];
		}


		/*


		double[][] tempCtrls = new double[p+1][dimension+1];

		for(int j=0 ; j<=p ; j++) {
			for(int d=0;d<=dimension; d++) {
				tempCtrls[j][d] = ctrls[j+k-p][d];
			}
		}

		for(int r=1; r<=p ; r++) {
			for(int j=p ; j>=r ; j--) {
				double a = (t -knots[j+k-p])/(knots[j+1+k-r] -knots[j+k-p]);
				for(int d=0; d<=dimension ; d++) {
					tempCtrls[j][d] = (1-a)*tempCtrls[j-1][d] +a*tempCtrls[j][d];
				}
			}
		}

		double result[] = new double[dimension];
		for(int i=0;i<dimension;i++) {
			result[i] = tempCtrls[p][i+1]/tempCtrls[p][0];
		}

		return result;
		 * */

		return result;

	}

	public static void main(String args[]) {
		double[] uknot = {0,0,1,1},vknot = {0,0,1,1};
		int p = 1,q = 1;
		double[][][] ctrl=
			{
					{{1,0,0,0},{1,10,0,0}},
					{{1,0,10,0},{1,10,10,0}}
			};

		NURBSModel model = new NURBSModel(p, uknot, q, vknot, ctrl);

		for(int i=0;i<=10;i++) {
			for(int j=0;j<=10;j++) {
				double u = 1.0/10*i, v = 1.0/10*j;
				double result[] = model.func(u, v);
				System.out.println("("+u+","+v+")→("+result[0]+","+result[1]+","+result[2]+")");
			}
		}

	}

}
