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
			PhysicalQuantity inputQ = new PhysicalQuantity(input);
			PhysicalQuantity maxQ;
			PhysicalQuantity minQ;
			try {
				maxQ = new PhysicalQuantity(maxValue);
				minQ = new PhysicalQuantity(minValue);
			}catch(IllegalArgumentException | NullPointerException e) {
				System.out.println("DefaultParameterChecker.checkFormatOf(String,String,String)"+e);
				return 0;
			}

			if(inputQ.isLargerThan(maxQ) || minQ.isLargerThan(inputQ)) {
				return 1;
			}else {
				return 0;
			}
		}catch(IllegalArgumentException e) {
			//物理量入力のフォーマットに従っていない
			return 2;
		}
	}

}
