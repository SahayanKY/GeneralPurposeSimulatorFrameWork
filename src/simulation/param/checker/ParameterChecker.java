package simulation.param.checker;

public interface ParameterChecker {
	
	/*
	 * 指定した文字列によって入力値が正常であるかどうかを判断する。
	 * @param input 入力値の文字列表現
	 * @param maxValue,minValue 入力値の最大値、最小値が存在する場合は指定する
	 * @return 0の場合は異常なし、1の場合は要検証(計算の続行は可能)、2の場合は続行のできない異常を意味する。
	 * */
	public int checkFormatOf(String input,String maxValue, String minValue);
}
