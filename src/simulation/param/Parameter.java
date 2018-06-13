package simulation.param;

import simulation.param.checker.ParameterChecker;

public class Parameter {
	public final String parentLabel, childLabel, maxValue, minValue;
	public final ParameterChecker checker;

	public final boolean isThrustDataParam;

	private String value;

	Parameter(String parentLabel, String childLabel, String maxValue, String minValue, boolean isThrustDataParam, ParameterChecker checker){
		this.parentLabel = parentLabel;
		this.childLabel = childLabel;
		this.maxValue = maxValue;
		this.minValue = minValue;
		this.checker = checker;
		this.isThrustDataParam = isThrustDataParam;
	}

	/*
	 * パラメータのチェック。同時にその値をvalueに格納。
	 * @param input 入力値のString表現
	 * @return 0の場合は異常なし、1の場合は警告、2の場合はエラーで計算続行不可
	 */
	public int checkFormatOf(String input) {
		int message = checker.checkFormatOf(input, maxValue, minValue);
		if(message == 0 || message == 1) {
			value = input;
		}
		return message;
	}

	/*
	 * このパラメータのもつ値を返す。
	 * @return このパラメータのもつvalueの値
	 * */
	public String getValue() {
		return this.value;
	}



	/*
	 * このパラメータが「燃焼データ」を表すものかどうかを判断する。
	 * @return このパラメータが「燃焼データ」であればtrue,そうでなければfalse
	 * */
	public boolean isThrustDataParameter() {
		return this.isThrustDataParam;
	}

	public void setValue(String inputValue) {
		this.value = inputValue;
	}
}

