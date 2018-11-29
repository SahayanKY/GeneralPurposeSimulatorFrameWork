package simulation.function.nurbs;

/**
 * k法によりNURBSを精細化するクラス。
 * */
public class DirectKRefiner implements KRefiner {


	/**
	 * {@inheritDoc}
	 * <p>
	 * この実装では新しいコントロールポイントを計算するのに、単純に連立方程式を
	 * 解いています。そのため、他の実装に比べて計算時間がかかる可能性があります。
	 * </p>
	 *
	 * @param basis NURBS基底関数
	 * @param q 増やしたい次数
	 * @param addedKnot 追加したいノット
	 * @throws IllegalArgumentException addedKnotを追加することによって関数の連続性が保持されない場合
	 * */
	@Override
	public void kRefinement(NURBSBasisFunction basis, int q, double[][] addedKnot) {
		double[][] newKnot = new double[basis.parameterNum][];
		for(int i=0;i<basis.parameterNum;i++) {

		}

	}

}
