package simulation.param.checker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import simulation.param.Parameter;

public class IntegerChecker implements ParameterChecker {
	private final int[] forgiveIntegers;

	public IntegerChecker(int... forgiveIntegers){
		this.forgiveIntegers = new int[forgiveIntegers.length];
		for(int i=0;i<forgiveIntegers.length;i++){
			this.forgiveIntegers[i] = forgiveIntegers[i];
		}
	}


	/*
	 * 入力値がコンストラクタで指定した整数のいずれかであるかをチェックする。
	 * @param input 入力値の文字列表現
	 * @param maxValue,minValue 何を指定しても無視される
	 * @return 入力値が指定の整数値であれば0、整数値でもなく、指定の値でもなければ2が返る。
	 * */
	@Override
	public int checkFormatOf(Parameter parameter) {
		String input = parameter.getValue();
		int n,message = Parameter.inputformat_Error;

		Pattern p = Pattern.compile("^ *([0-9]+) *$");
		Matcher m = p.matcher(input);

		if(m.find()) {
			n = Integer.parseInt(m.group(1));
			for(int forgiveN:forgiveIntegers){
				if(n==forgiveN){
					message = Parameter.inputformat_NoProblem;
					break;
				}
			}
		}

		return message;
	}

}
