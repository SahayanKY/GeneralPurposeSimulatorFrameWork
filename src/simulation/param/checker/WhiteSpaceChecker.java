package simulation.param.checker;

public class WhiteSpaceChecker implements ParameterChecker {

	/*
	 * 未入力、または空白文字のみの場合エラー
	 * @param input 入力値の文字列表現
	 * @param maxValue,minValue 何を指定しても無視される
	 * @return 0の場合異常なし、2の場合異常あり
	 * */
	@Override
	public int checkFormatOf(String input, String maxValue, String minValue) {
		if(input.matches("[ 　]+") | input.equals("")) {
			return 2;
		}else {
			return 0;
		}
	}

}
