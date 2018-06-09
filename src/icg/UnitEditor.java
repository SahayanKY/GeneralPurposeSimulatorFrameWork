package icg;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitEditor {
	private static Pattern physicalQuantityPattern = Pattern.compile("^ *(\\-?[0-9]*\\.?[0-9]+(?:E-?[0-9]+)?)( +(?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)?| +)+/?(?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)?| +)*| +/ *(?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)?| +)+)?$");
	private static Pattern unitsPattern = Pattern.compile("^ *((?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)? +)*(?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)?)? */? *(?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)? +)*(?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)?)) *$");
	private static Pattern oneUnitPattern = Pattern.compile("((?:[μmcdhkM]|da)?)([mgsA])((?:-?[0-9]+)?)");

	private static String doubleRegex = "-?[0-9]*\\.?[0-9]+(E-?[0-9]+)?";

	/*
	 * 一番目の引数に指定された物理量が二番目の引数に指定された物理量よりも大きい場合true、
	 * 小さい場合falseを返す。引数が物理量でなかった場合や、比較のできない物理量のペアであった場合、
	 * IllegalArgumentExceptionがスローされる。
	 * @param value1,value2　比較する物理量
	 * @return boolean trueの時、1番目の引数の方が大きい
	 * */
	/*public static boolean isLargerA_thanB(String value1, String value2) throws IllegalArgumentException{
		String convertedValue1 = convert_from_toWithUnits(value1,value2);
		

		if(value1Num*Math.pow(10,value1UnitMap.getOrDefault("none",0))
				> value2Num*Math.pow(10, value2UnitMap.getOrDefault("none",0))){
			return true;
		}else{
			return false;
		}
	}*/

	/*
	 * 1番目の引数に指定された物理量を2番目の引数に指定された単位（または物理量の単位）に変換し、そのString
	 * 表現を返す。このメソッドの戻り値は単位つきのStringであることに注意する。
	 * 変換の前後で次元が違う場合や物理量としてのフォーマットを満たない場合はnull値を、
	 * 無次元量の場合はafterUnitに何を指定してもOriginValueに接頭辞を加味したものが返る。
	 * 例えば、10 km/m -> 10000
	 * @param OriginValue 変換したい物理量を単位付きで指定
	 * @param afterUnit 変換後の単位を単位または物理量で指定
	 * */
	public static String convert_from_toWithUnits(String OriginValue,String afterUnit) {

		HashMap<String,Number> beforeValueMap = dimensionAnalysis(OriginValue);
		Double Number = (Double)beforeValueMap.get("Number");
		if(beforeValueMap.get("m") == null && beforeValueMap.get("kg") == null
				&& beforeValueMap.get("s") == null && beforeValueMap.get("A") == null) {
			//単位の計算の結果、無次元量となっていた場合
			if(Number == null){
				throw new IllegalArgumentException("変換前の指定が物理量でも無次元量でもありません");
			}else{
				return Double.toString(Number * Math.pow(10, (Double)beforeValueMap.getOrDefault("none",0)));
			}
		}

		//こっから先はOriginValueは物理量
		HashMap<String,Number> afterUnitMap = dimensionAnalysis(afterUnit);

		StringBuilder stb = new StringBuilder();
		for(String unit:new String[] {"kg","m","s","A"}) {
			if(!beforeValueMap.getOrDefault(unit,0).equals(afterUnitMap.getOrDefault(unit,0))) {
				//次元が違う場合変換不可能なのでnull
				throw new IllegalArgumentException("UnitEditor.convert_from_toWithUnits():変換の前後で単位の次元が異なります");
			}
			if(beforeValueMap.containsKey(unit)){
				stb.append(" "+unit+beforeValueMap.get(unit));
			}
		}
		
		Double AfterNumber = Number * Math.pow(10, (Double)beforeValueMap.getOrDefault("none",0)-(Double)afterUnitMap.getOrDefault("none",0));

		return AfterNumber.toString()+stb;
	}

	/*
	 * 指定された文字列が物理量(または数値)であるかどうかを調べる。物理量であればtrueを、
	 * どちらでもないものであればfalseを返す。なお、先頭と後端の余分な半角スペースがあってもよい。
	 * @param target 調べる文字列
	 * @boolean 
	 * true:物理量または数値
	 *　false：どちらでもない文字列
	 * */
	private static boolean isPhysicalQuantity(String target){
		if(target == null){
			return false;
		}
		Matcher physicalM = physicalQuantityPattern.matcher(target);
		if(physicalM.find()) {
			return true;
		}else{
			return false;
		}
	}


	/*
	 * 指定された文字列が適切な組立単位かどうかを判定する。
	 * @param target 調べる文字列
	 * @return true:単位と判定,　false：単位ではないと判定
	 * */
	private static boolean isUnit(String target){
		Matcher m = unitsPattern.matcher(target);
		if(!m.find()) {
			return false;
		}else {
			return true;
		}
	}

	/*
	 * 単位(組立単位)を受け取り、m,kg,s,Aの次数と10^-3などの接頭辞による係数を
	 * もったマップを返す。接頭辞による係数のキーは"none"
	 * */
	@Deprecated
	private static HashMap<String,? extends Number> moldUnit(String targetUnit){
		if(!isUnit(targetUnit)) {
			throw new IllegalArgumentException("指定された文字列は単位ではありません");
		}
		HashMap<String,Integer> unitMap = new HashMap<>();
		String[][] units;
		String[] positiveUnits = targetUnit.split("/");
		if(positiveUnits.length == 2) {
			String[] negativeUnits = positiveUnits[1].split(" ");
			positiveUnits = positiveUnits[0].split(" ");
			units = new String[][]{positiveUnits , negativeUnits};
		}else {
			//"/"の無い単位だった場合
			units = new String[][] {positiveUnits[0].split(" ")};
		}

		//m,g,s,A,none毎に指数をマップに記録していく
		for(int i=0;i<units.length;i++) {
			int posinega; //1か-1
			for(String u : units[i]) {
				if(u.equals("")) {
					continue;
				}
				posinega = 1-2*i; //初期化
				int prefix=0,degree=0;
				String standardUnit=null;
				String degreeStr[] = u.split("([μmcdhkM]|da)?[mgsA]");
				if(degreeStr.length == 0) {
					//次数が1で省略されている場合
					degree = 1;
				}else {
					//次数が省略されていない場合
					//数字のところを正規表現で切り離す
					degree = Integer.parseInt(degreeStr[1]);
					u = u.split("-?[0-9]+")[0];
				}
				prefix = convertPrefix(u.substring(0,u.length()-1));
				standardUnit = u.substring(u.length()-1,u.length());


				unitMap.put("none", unitMap.getOrDefault("none", 0)+prefix*degree*posinega);
				unitMap.put(standardUnit, unitMap.getOrDefault(standardUnit,0)+degree*posinega);
			}
		}

		//gをkgに直す
		if(unitMap.containsKey("g")){
			int g_degree = unitMap.remove("g");
			unitMap.put("kg", g_degree);
			unitMap.put("none", unitMap.getOrDefault("none", 0) - 3*g_degree);
		}

		for(String key:new String[]{"m","kg","s","A"}) {
			//mapのkeySet()を使うとremoveしたときに例外が発生してしまうので
			//次数が0のものは消しておく
			if(unitMap.getOrDefault(key,0) == 0) {
				unitMap.remove(key);
			}
		}

		return unitMap;
	}
	
	/*
	 * 指定された物理量、数値または単位の次元解析を行う。
	 * 返るマップには"Number"(元の文字列の数値の部分),"none"(接頭辞の解析結果),"m","kg","s","A"のキーが最大でマップされている
	 * その値が0のときはキーは除去されている
	 * */
	static HashMap<String,Number> dimensionAnalysis(String quantity){
		HashMap<String,Number> unitMap = new HashMap<String,Number>();
		Matcher matcher;
		String[] UnitSlashSplit;
		
		if(isPhysicalQuantity(quantity)){
			//物理量または無次元量、数値に関する条件分岐
			matcher = physicalQuantityPattern.matcher(quantity);
			matcher.find();
			unitMap.put("Number", Double.parseDouble(matcher.group(1)));
			
			String unitline = matcher.group(2);
			if(unitline == null){
				//単位が全くない指定だった場合
				return unitMap;
			}
			UnitSlashSplit = matcher.group(2).split("/");
		}else if(isUnit(quantity)){
			//単位単体に関する条件分岐
			matcher = unitsPattern.matcher(quantity);
			matcher.find();
			UnitSlashSplit = matcher.group(1).split("/");
		}else{
			throw new IllegalArgumentException("UnitEditor.dimensionAnalysis:指定された文字列は物理量でも数値でも単位でもありません");
		}
		
		String[][] units;
		if(UnitSlashSplit.length == 2) {
			String[] negativeUnits = UnitSlashSplit[1].split(" ");
			UnitSlashSplit = UnitSlashSplit[0].split(" ");
			units = new String[][]{UnitSlashSplit , negativeUnits};
		}else {
			//"/"の無い単位だった場合
			units = new String[][] {UnitSlashSplit[0].split(" ")};
		}
		
		
		//m,g,s,A,none毎に指数をマップに記録していく
		for(int i=0;i<units.length;i++) {
			int posinega; //1か-1
			for(String u : units[i]) {
				if(u.equals("")) {
					continue;
				}
				posinega = 1-2*i; //初期化
				
				Matcher oneUnitMatcher = oneUnitPattern.matcher(u);
				oneUnitMatcher.find();
				String prefixStr = oneUnitMatcher.group(1);
				String standardUnit = oneUnitMatcher.group(2);
				String degreeStr = oneUnitMatcher.group(3);

				int prefix = (prefixStr.equals(""))? 0: convertPrefix(prefixStr);
				int degree = (degreeStr.equals(""))? 1: Integer.parseInt(degreeStr);
				
				unitMap.put("none", (Integer)unitMap.getOrDefault("none", Integer.valueOf(0))+prefix*degree*posinega);
				unitMap.put(standardUnit, (Integer)unitMap.getOrDefault(standardUnit, Integer.valueOf(0))+degree*posinega);
			}
		}
		
		//gをkgに直す
		if(unitMap.containsKey("g")){
			Integer g_degree = (Integer) unitMap.remove("g");
			unitMap.put("kg", g_degree);
			unitMap.put("none", (Integer)unitMap.getOrDefault("none", Integer.valueOf(0)) - 3*g_degree);
		}
		
		//次数が0のものは消しておく
		for(String key:new String[]{"m","kg","s","A"}) {
			//mapのkeySet()を使うとremoveしたときに例外が発生してしまうので

			if((Integer)unitMap.getOrDefault(key,Integer.valueOf(0)) == 0) {
				unitMap.remove(key);
			}
		}

		return unitMap;
	}


	private static int convertPrefix(String prefix) {
		switch(prefix) {
			case "μ":	return -6;
			case "m":	return -3;
			case "c":	return -2;
			case "d":	return -1;
			case "da":	return 1;
			case "h":	return 2;
			case "k":	return 3;
			case "M":	return 6;
			default:	return 0;
		}
	}
}
