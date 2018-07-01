package simulation.param.checker;

import simulation.param.Parameter;

public class DateFormatChecker implements ParameterChecker {

	/*
	 * 年月の入力フォーマットに即していない場合エラーとなる。入力値は"2018/05"のように年月の表記をもつかをチェックされる。
	 * @param input 入力値の文字列表現
	 * @param maxValue,minValue 何を指定しても無視される
	 * @return
	 * */
	@Override
	public int checkFormatOf(Parameter parameter) {
		String input = parameter.getValue();
		if(!input.matches("(19|20)[0-9]{2}/(0[1-9]|1[012])")) {
			return Parameter.inputformat_Error;
		}else {
			return Parameter.inputformat_NoProblem;
		}
	}

}
