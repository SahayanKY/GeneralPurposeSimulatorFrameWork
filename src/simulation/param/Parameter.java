package simulation.param;

import simulation.param.checker.ParameterChecker;

public class Parameter {
	public final String parentLabel, childLabel, propertyLabel, maxValue, minValue;
	public final ParameterChecker checker;

	public static final int
		inputformat_NoProblem = 0,
		inputformat_Warning = 1,
		inputformat_Error = 2;


	private boolean needFileChooser = false;

	private String valueStr;

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
	}

	/*
	 * パラメータのチェック。同時にその値をvalueに格納する。
	 * @param input 入力値のString表現
	 * @return クラス定数
	 * inputformat_NoProblemの場合は異常なし、
	 * inputformat_Warningの場合は警告
	 * inputformat_Errorの場合はエラーで計算続行不可を意味する。
	 */
	public int setAndCheckFormatOf(String input) {
		valueStr = input;
		return checker.checkFormatOf(this);
	}

	/*
	 * このパラメータのもつ値を返す。
	 * @return このパラメータのもつvalueの値
	 * */
	public String getValue() {
		return this.valueStr;
	}


	/*
	 * このパラメータの値をセットする。
	 * @param
	 * inputValue セットする値のString表現
	 * */
	public void setValue(String inputValue) {
		this.valueStr = inputValue;
	}


	/*
	 * このパラメータの値のセットにファイルチューザーを使用したい場合trueを指定する。
	 * デフォルト値はfalse。
	 * @param need ボタンを使う場合はtrue
	 * */
	public void setNeedFileChooser(boolean need) {
		this.needFileChooser = need;
	}

	/*
	 * このパラメータが値のセットにボタンを要するものかをどうかを返す。
	 * @return このパラメータがボタンを使うものであればtrue,そうでなければfalse
	 * */
	public boolean isNeedFileChooser() {
		return this.needFileChooser;
	}

}

