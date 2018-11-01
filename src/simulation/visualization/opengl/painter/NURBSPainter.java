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
}
