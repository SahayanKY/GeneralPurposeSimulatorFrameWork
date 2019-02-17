package openGL.drawable;

/**
 * 1変数、1次元のパラメータ表示のスカラー関数
 *
 * この関数は1変数1次元のパラメータ関数x(t)と
 * 1変数1次元の関数f(x)からなるf(x(t))という関数を表している。
 * */
public abstract class DrawableFunction111 implements DrawableFunction {

	/**
	 * パラメータに対する関数値fを返す。
	 * @param t パラメータ
	 * @return 関数値f(t)
	 * */
	public abstract float f_param(float t);


	/**
	 * パラメータに対する実空間の値xと関数値fを配列にして返す。
	 * @param t パラメータ
	 * @return {x(t),f(x(t))}という配列
	 * */
	public abstract float[] f_real(float t);
}
