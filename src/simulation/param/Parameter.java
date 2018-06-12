package simulation.param;

import java.util.ArrayList;

import simulation.param.checker.ParameterChecker;

public abstract class Parameter {
	protected static ArrayList<? extends Parameter> paramList = new ArrayList<>();

	public final String parentLabel, childLabel, maxValue, minValue;
	public final ParameterChecker checker;

	protected String value;

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
	public abstract int checkFormatOf(String input) ;

	/*
	 * このパラメータのもつ値を返す。
	 * @return このパラメータのもつvalueの値
	 * */
	public String getValue() {
		return this.value;
	}

	/*
	 * パラメータの配列を返す。
	 * @return このパラメータ(列挙子)をすべて持つ配列
	 * */
	public static Parameter[] values() {
		Parameter[] params = new Parameter[paramList.size()];
		for(int i=0;i<params.length;i++) {
			params[i] = paramList.get(i);
		}
		return params;
	}


	/*
	 * このパラメータが「燃焼データ」を表すものかどうかを判断する。
	 * */
	public abstract boolean isThrustDataParameter();
}

