package simulation.param.checker;

public class BeforeAfterParamChecker extends DefaultParameterChecker {
	
	/*
	 * 燃焼の前後で値が変わるパラメータのチェックを行う。入力値は"a"または"b"で終わる必要がある。この"a"は
	 * 燃焼後(After)を、"b"は燃焼前(Before)を意味する。
	 * @param input 入力値の文字列表現
	 * @param maxValue,minValue 比較対象の文字列表現。この2つは"a","b"はつけないものを指定する。
	 * すなわち、このチェックは燃焼前後によらないチェックが行われる。
	 * @return 0の場合異常なし、1の場合要検証、2の場合エラー(続行不可能)を意味する。
	 * */
	@Override
	public int checkFormatOf(String input, String maxValue, String minValue) {
		//入力値が"a"で終わる場合燃焼後を、"b"で終わる場合燃焼前を意味する。
		if(!(input.endsWith("a") || input.endsWith("b"))) {
			return 2;
		}
		String subInput = input.substring(0, input.length()-1);
		int messageNum = super.checkFormatOf(subInput, maxValue, minValue);
		return messageNum;
	}

}
