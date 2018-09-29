package simulation.model3d;

public class NURBSModel extends Model{
	public final double[] uknots, vknots;
	public final double[][][] ctrls;
	public final int p,q;

	/*
	 * ctrlsの1番目のインデックスはu方向、2番目のインデックスはv方向の
	 * ノットに対応する。
	 * p,qは多項式の次数
	 * コントロールポイントは(重み、x座標、y座標、z座標)の順に指定する
	 * */
	NURBSModel(int p, double[] uknots, int q, double[] vknots, double[][][] ctrls){
		//次数が負数になっていないか
		if(p < 0 || q < 0) {
			throw new IllegalArgumentException("次数が不正です");
		}
		//ノットベクトルが単調増加になっているか
		for(int i=0;i<uknots.length-1;i++) {
			if(uknots[i] > uknots[i+1]) {
				throw new IllegalArgumentException("ノットベクトルが単調増加でありません");
			}
		}
		for(int j=0;j<vknots.length-1;j++) {
			if(vknots[j] > vknots[j+1]) {
				throw new IllegalArgumentException("ノットベクトルが単調増加でありません");
			}
		}
		//ノットの数と次数とポイントの数のつじつまがあっているか
		if(uknots.length != ctrls.length +p+1 || vknots.length != ctrls[0].length +q+1) {
			throw new IllegalArgumentException("コントロールポイント数、ノット数、次数のつじつまが合っていません");
		}

		 /*
		 * v方向のポイント数は一定になっているか
		 * ctrlsの各要素は重みと3次元の計4要素の配列になっているか
		 * また、座標値には先に重みをかけておく
		 */
		for(int i=0;i<ctrls.length;i++) {
			if(ctrls[i].length != ctrls[0].length) {
				throw new IllegalArgumentException("v方向のコントロールポイントの数が一定でありません");
			}
			for(int j=0;j<ctrls[i].length;j++) {
				if(ctrls[i][j].length != 4) {
					throw new IllegalArgumentException("コントロールポイントの値が4要素になっていません");
				}
				for(int k=1;k<4;k++) {
					ctrls[i][j][k] *= ctrls[i][j][0];
				}
			}
		}

		this.p = p;
		this.q = q;
		this.uknots = uknots;
		this.vknots = vknots;
		this.ctrls = ctrls;
	}



}
