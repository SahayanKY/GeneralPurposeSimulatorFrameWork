package simulation.function.nurbs;

/**
 * NURBSをk法（次数を上げた後、ノットを挿入する）により精細化するインターフェースです。
 * */
public interface KRefiner {
	/**
	 * <p>
	 * 指定した基底関数、及びそれに関連するNURBS関数を精細化します。
	 * </p>
	 * <p>
	 * 具体的には基底関数の次数をqだけ増やし、元のノットベクトルにaddedKnotを追加します。
	 * </p>
	 * <p>
	 * このメソッドを実装する際にはNURBSBasisFunctionインスタンス、NURBSFunctionインスタンス
	 * 双方のメンバを正しく更新しているか注意してください。
	 * </p>
	 * @author SahayanKY
	 * @param basis NURBS基底関数
	 * @param q 増やしたい次数
	 * @param addedKnot 追加したいノット
	 * @throws IllegalArgumentException
	 * <ul>
	 * <li>addedKnotを追加することによって関数の連続性が保持されない場合。
	 * <li>addedKnotの最小値、最大値が元のノット範囲外にあるとき。
	 * <li>addedKnotが単調増加列の配列の配列で無かった場合。
	 * <li>qの各要素のうちいずれかが負数であった場合。
	 * </ul>
	 * */
	public void kRefinement(NURBSBasisFunction basis,int[] q,double[][] addedKnot);
}
