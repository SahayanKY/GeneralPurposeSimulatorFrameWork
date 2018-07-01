package simulation.param.checker;

import simulation.param.Parameter;

public class BeforeAfterParamChecker extends DefaultParameterChecker {

	/*
	 * 燃焼の前後で値が変わるパラメータのチェックを行う。入力値は"a"または"b"で終わる必要がある。この"a"は
	 * 燃焼後(After)を、"b"は燃焼前(Before)を意味する。
	 * @param parameter 入力値を検証するParameterインスタンス
	 * @return 0の場合異常なし、1の場合要検証、2の場合エラー(続行不可能)を意味する。
	 * */
	@Override
	public int checkFormatOf(Parameter parameter) {
		String input = parameter.getValue(),
				maxValue = parameter.maxValue,
				minValue = parameter.minValue;

		//入力値が"a"で終わる場合燃焼後を、"b"で終わる場合燃焼前を意味する。
		if(!(input.endsWith("a") || input.endsWith("b"))) {
			return Parameter.inputformat_Error;
		}
		String subInput = input.substring(0, input.length()-1);
		return checkInputSize(subInput, maxValue, minValue);
	}

}
