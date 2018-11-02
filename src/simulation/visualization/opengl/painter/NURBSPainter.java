package simulation.visualization.opengl.painter;

import com.jogamp.opengl.GL2;

import simulation.function.nurbs.NURBSFunction;

public class NURBSPainter {
	/*
	 * パラメータ空間から実空間への写像を通して関数を与えている場合
	 * こちらのpaint()を利用する。
	 * @param gl2 GL2インスタンス
	 * @param realSpaceFunc パラメータ空間から実空間への写像
	 * @param valueFunc パラメータ空間から目的関数値への写像
	*/
	public void paint(GL2 gl2, NURBSFunction realSpaceFunc, NURBSFunction valueFunc){
	}

	/*
	 * パラメータ空間に対する関数値を描画したい場合、こちらのpaint()を利用する。
	 * @param valueFunc 描画したい関数
	*/
	public void paint(GL2 gl2, NURBSFunction valueFunc){
	}

	/*
	 * 1変数
	 * →1（次元上の数直線）、2次元上の曲線上で定義されたスカラー関数
	 * →2次元上の曲線上で定義されたR2ベクトル関数
	 *
	 * 2変数
	 * →2次元の面、3次元上の曲面上で定義されたスカラー関数
	 * →2次元の面で定義されたR2ベクトル関数
	 * →3次元上の曲面で定義されたR3ベクトル関数
	 *
	 * 3変数
	 * →3次元の空間上で定義されたスカラー関数
	 * →3次元の空間上で定義されたR3ベクトル関数
	 *
	 *
	 * スカラー関数とベクトル関数の描画オプション
	 * ・スカラー関数
	 * 		・等値曲面を描画するのか、その間隔は？
	 * 		・色遣いは？
	 * ・ベクトル関数
	 * 		・流線形？格子上でのベクトル描画？
	 *		・3次元上の曲面上のベクトル関数描画等には利用できない
	 * */
}
