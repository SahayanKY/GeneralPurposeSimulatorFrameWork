package parabolicmovement;

/**
 * 計算条件(射角)の組み合わせを保持、提案するクラス。
 * */
public class DynamicParametersCombinations {
	/**
	 * 全計算条件
	 * */
	private final double[] theta0;

	/**
	 * 次の計算条件を表すインデックス
	 * */
	private int nextIndex = 0;

	/**
	 * 全計算条件の数
	 * */
	public final int allCombiNum;

	/**
	 * コンストラクタ
	 * */
	public DynamicParametersCombinations(double[] theta0) {
		this.theta0 = theta0;
		this.allCombiNum = theta0.length;
	}

	/**
	 * 次に計算してもらう条件を提示します。
	 * このメソッドはスレッドセーフではありません。
	 * 必ずシングルスレッドからアクセスしてください。
	 * もし、次の条件がない場合はnullが返されます。
	 * @return 次の計算条件。存在しない場合はnull。
	 * */
	public DynamicParameters getNextDynamicParameters() {
		if(nextIndex == allCombiNum) {
			return null;
		}else {
			DynamicParameters next = new DynamicParameters(theta0[nextIndex]);
			nextIndex++;
			return next;
		}
	}
}
