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
	 * コンストラクタ
	 * */
	public DynamicParametersCombinations(double[] theta0) {
		this.theta0 = theta0;
	}

	/**
	 * 次に計算してもらう条件を提示します。
	 * このメソッドはスレッドセーフではありません。
	 * 必ずシングルスレッドからアクセスしてください。
	 * もし、次の条件がない場合はnullが返されます。
	 * @return 次の計算条件。存在しない場合はnull。
	 * */
	public DynamicParameters getNextDynamicParameters() {
		if(nextIndex == 720) {
			return null;
		}else {
			DynamicParameters next = new DynamicParameters(theta0[nextIndex]);
			nextIndex++;
			return next;
		}
	}
}
