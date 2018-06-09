package simulation.param.checker;

import icg.PhysicalQuantity;

public class DefaultParameterChecker implements ParameterChecker {

	/*
	 * デフォルトのパラメータのチェック。
	 * @param input 入力値のString表現
	 * @return 0の場合は異常なし、1の場合は警告、2の場合はエラーで計算続行不可
	 */
	@Override
	public int checkFormatOf(String input, String maxValue, String minValue) {
		try {
			int message = 0;

			PhysicalQuantity inputQ = new PhysicalQuantity(input);
			PhysicalQuantity maxQ;
			PhysicalQuantity minQ;

			if(maxValue != null) {
				maxQ = new PhysicalQuantity(maxValue);
				if(inputQ.isLargerThan(maxQ)) {
					message = 1;
				}
			}
			if(minValue != null) {
				minQ = new PhysicalQuantity(minValue);
				if(minQ.isLargerThan(inputQ)) {
					message = 1;
				}
			}
			return message;
		}catch(IllegalArgumentException e) {
			//物理量入力のフォーマットに従っていない
			return 2;
		}
	}

}
