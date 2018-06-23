package simulation.param;

import java.util.ArrayList;

import simulation.param.checker.ParameterChecker;

public class Parameter {
	public final String parentLabel, childLabel, propertyLabel, maxValue, minValue;
	public final ParameterChecker checker;
	public final boolean isSystemInputParameter;

	private static ArrayList<Parameter> needInputButtonParams = new ArrayList<>();

	private String value;

	//ユーザーの入力するパラメータについてのコンストラクタ
	public Parameter(String parentLabel, String childLabel, String propertyLabel, String minValue, String maxValue, ParameterChecker checker) {
		if(parentLabel == null || childLabel == null || propertyLabel == null || checker == null) {
			throw new IllegalArgumentException();
		}
		this.parentLabel = parentLabel;
		this.childLabel = childLabel;
		this.propertyLabel = propertyLabel;
		this.maxValue = maxValue;
		this.minValue = minValue;
		this.checker = checker;
		this.isSystemInputParameter = false;
	}

	//プログラムが入力するパラメータについてのコンストラクタ
	public Parameter(String parentLabel, String childLabel, String propertyLabel) {
		if(parentLabel == null || childLabel == null || propertyLabel == null) {
			throw new IllegalArgumentException();
		}
		this.parentLabel = parentLabel;
		this.childLabel = childLabel;
		this.propertyLabel = propertyLabel;
		this.maxValue = null;
		this.minValue = null;
		this.checker = null;
		this.isSystemInputParameter = true;
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
	 * このパラメータの値をセットする。
	 * @param
	 * inputValue セットする値のString表現
	 * */
	public void setValue(String inputValue) {
		this.value = inputValue;
	}


	/*
	 * このパラメータの値のセットにボタンを使用したい場合用いる。
	 * */
	public void setNeedInputButtonParameter() {
		needInputButtonParams.add(this);
	}

	/*
	 * このパラメータが値のセットにボタンを使うものかどうかを判断する。
	 * @return このパラメータがボタンを使うものであればtrue,そうでなければfalse
	 * */
	public boolean isNeedInputButtonParameter() {
		boolean isNeed = false;
		for(Parameter p:needInputButtonParams) {
			isNeed= (this == p);
			if(isNeed) {
				break;
			}
		}
		return isNeed;
	}

}

