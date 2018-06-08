package simulation.param.checker;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icg.UnitEditor;

public class DefaultParameterChecker implements ParameterChecker {

	/*
	 * デフォルトのパラメータのチェック。
	 * @param input 入力値のString表現
	 * @return 0の場合は異常なし、1の場合は警告、2の場合はエラーで計算続行不可
	 */
	@Override
	public int checkFormatOf(String input, String maxValue, String minValue) {
		try {
			int message=0;

			if(maxValue != null && UnitEditor.isLargerA_thanB(input,maxValue)) {
				//最大値の設定があるが、それを上回っていた場合
				message = 1;
			}
			if(minValue != null && UnitEditor.isLargerA_thanB(minValue,input)) {
				message = 1;
			}
			if(maxValue == null && minValue == null) {
				if(UnitEditor.isPhysicalQuantity(input) == -1) {
					//物理量でも無次元量でもなかった場合
					message = 2;
				}else {
					Pattern p = Pattern.compile("^( *-?[0-9]*\\.?[0-9]+(E-?[0-9]+)?)(.*)");
					Matcher m = p.matcher(input);
					if(!m.find()) {
						message = 2;
					}else {
						String inputUnits = m.group(3);
						HashMap<String,Integer> map = UnitEditor.moldUnit(inputUnits);
						if(map.get("m") == null && map.get("kg") == null
								&& map.get("s") == null && map.get("A") == null) {
							//単位計算の結果無次元量
							message = 0;
						}else {
							//最大最小の指定が無い物理量の場合
							message = 1;
						}
					}
				}
			}

			return message;
		}catch(IllegalArgumentException e) {
			//物理量入力のフォーマットに従っていない
			return 2;
		}
	}

}
