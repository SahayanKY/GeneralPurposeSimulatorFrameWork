package simulation.param;

import simulation.param.checker.DefaultParameterChecker;
import simulation.param.checker.ParameterChecker;

public class Parameter {
	public final String parentLabel, childLabel;
	public final ParameterChecker checker;

	protected final String maxValue,minValue;
	protected String value;

	private static final ParameterChecker defaultChecker = new DefaultParameterChecker();

	Parameter(String parentLabel, String childLabel, ParameterChecker checker){
		this(parentLabel, childLabel, null,null,checker);
	}

	Parameter(String parentLabel, String childLabel){
		this(parentLabel, childLabel, null, null, defaultChecker);
	}

	Parameter(String parentLabel, String childLabel, String maxValue, String minValue){
		this(parentLabel,childLabel,maxValue,minValue,defaultChecker);
	}

	Parameter(String parentLabel, String childLabel, String maxValue, String minValue, ParameterChecker checker){
		this.parentLabel = parentLabel;
		this.childLabel = childLabel;
		this.maxValue = maxValue;
		this.minValue = minValue;
		this.checker = checker;
	}

	/*
	 * パラメータのチェック。同時にその値をvalueに格納。
	 * @param input 入力値のString表現
	 * @return 0の場合は異常なし、1の場合は警告、2の場合はエラーで計算続行不可
	 */
	private final int checkFormatOf(String input) {
		int message = checker.checkFormatOf(input, maxValue, minValue);
		if(message == 0 || message == 1) {
			value = input;
		}
		return message;
	}

}

