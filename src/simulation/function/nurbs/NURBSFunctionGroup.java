package simulation.function.nurbs;

public class NURBSFunctionGroup {
	/**
	 * 基底関数
	 * @version 2019/02/22 20:26
	 * */
	public final NURBSBasisFunction basis;
	/**
	 * NURBS関数
	 * @version 2019/02/22 20:26
	 * */
	public final NURBSFunction[] funcs;

	/**
	 * NURBSBasisFunctionとNURBSFunctionの組を作ります。
	 *
	 * @param basis NURBSBasisFunctionインスタンス
	 * @param func basisを基底関数とするNURBSFunctionインスタンス
	 *
	 * @throws IllegalArgumentException NURBSBasisFunctionインスタンスが一致しない場合
	 * @version 2019/02/22 20:26
	 * */
	public NURBSFunctionGroup(NURBSBasisFunction basis, NURBSFunction[] func) {
		this.basis = basis;
		for(NURBSFunction f:func) {
			if(!f.basisFunctionIs(basis)) {
				throw new IllegalArgumentException("指定されたNURBSFunctionの基底関数は指定された基底関数と同じではありません");
			}
		}
		this.funcs = func;
	}

}
