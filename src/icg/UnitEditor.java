package icg;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitEditor {
	private static String doubleRegex = "-?[0-9]*\\.?[0-9]+(E-?[0-9]+)?";

	/*
	 * 一番目の引数に指定された物理量が二番目の引数に指定された物理量よりも大きい場合true、
	 * 小さい場合falseを返す。引数が物理量でなかった場合や、比較のできない物理量のペアであった場合、
	 * IllegalArgumentExceptionがスローされる。
	 * @param value1,value2　比較する物理量
	 * @return boolean trueの時、1番目の引数の方が大きい
	 * */
	public static boolean isLargerA_thanB(String value1, String value2) throws IllegalArgumentException{
		if(value1 == null || value2 == null || isPhysicalQuantity(value1) == -1 || isPhysicalQuantity(value2) == -1){
			//指定が物理量でも無次元量でもない場合
			throw new IllegalArgumentException();
		}
		Pattern p = Pattern.compile("(-?[0-9]*\\.?[0-9]+(E-?[0-9]+)?)([μmcdhkMdagsA/\\-0-9 ]*)");
		Matcher mValue1 = p.matcher(value1);
		Matcher mValue2 = p.matcher(value2);

		mValue1.find();mValue2.find();

		Double value1Num = Double.parseDouble(mValue1.group(1));
		Double value2Num = Double.parseDouble(mValue2.group(1));

		String value1Unit = mValue1.group(3);
		String value2Unit = mValue2.group(3);

		//単位をMKSAのマップに変換する
		HashMap<String,Integer> value1UnitMap = moldUnit(value1Unit);
		HashMap<String,Integer> value2UnitMap = moldUnit(value2Unit);
		if(value1UnitMap == null || value2UnitMap == null) {
			throw new IllegalArgumentException();
		}

		for(String unit:new String[]{"m","kg","s","A"}){
			if(!value1UnitMap.getOrDefault(unit,0).equals(value2UnitMap.getOrDefault(unit,0))){
				throw new IllegalArgumentException();
			}
		}

		if(value1Num*Math.pow(10,value1UnitMap.getOrDefault("none",0))
				> value2Num*Math.pow(10, value2UnitMap.getOrDefault("none",0))){
			return true;
		}else{
			return false;
		}
	}

	/*
	 * 1番目の引数に指定された物理量を2番目の引数に指定された単位に変換し、そのString
	 * 表現を返す。このメソッドの戻り値は単位つきのStringであることに注意する。
	 * 変換の前後で次元が違う場合や物理量としてのフォーマットを満たない場合はnull値を、
	 * 無次元量の場合はafterUnitに何を指定してもOriginValueに接頭辞を加味したものが返る。
	 * 例えば、10 km/m -> 10000
	 * @param OriginValue 変換したい物理量を単位付きで指定
	 * @param afterUnit 変換後の単位を指定
	 * */
	public static String convert_from_toWithUnits(String OriginValue,String afterUnit) {
		Double Number;
		if(OriginValue == null || afterUnit == null) {
			return null;
		}
		switch(isPhysicalQuantity(OriginValue)) {
			case -1:
				//物理量でも無次元量でもない
				return null;
			case 0:
				//計算するまでもなく無次元量
				return OriginValue;
			case 1:
				//物理量（もしかしたら無次元量）
		}

		String UnisStr = OriginValue.split(" *"+doubleRegex+" +",2)[1];
		String NumStr = OriginValue.split(UnisStr)[0];
		Number = Double.parseDouble(NumStr);

		HashMap<String,Integer> beforeUnitMap = moldUnit(UnisStr);
		if(beforeUnitMap == null) {
			return null;
		}
		if(beforeUnitMap.get("m") == null && beforeUnitMap.get("kg") == null
				&& beforeUnitMap.get("s") == null && beforeUnitMap.get("A") == null) {
			//単位の計算の結果、無次元量となっていた場合
			return Double.toString(Number * Math.pow(10, beforeUnitMap.getOrDefault("none",0)));
		}

		//こっから先はOriginValueは物理量
		if(!isUnit(afterUnit)) {
			//変換先の指定が適切な単位で無かった場合
			return null;
		}

		HashMap<String,Integer> afterUnitMap = moldUnit(afterUnit);
		if(afterUnitMap == null) {
			return null;
		}

		for(String unit:new String[] {"m","kg","s","A"}) {
			if(!beforeUnitMap.getOrDefault(unit,0).equals(afterUnitMap.getOrDefault(unit,0))) {
				//次元が違う場合変換不可能なのでnull
				return null;
			}
		}
		Number *= Math.pow(10, beforeUnitMap.getOrDefault("none",0)-afterUnitMap.getOrDefault("none",0));


		return Number +" "+ afterUnit;
	}

	/*
	 * 指定された文字列が物理量であるかどうかを調べる。物理量であれば1を、無次元量であれば0を、
	 * どちらでもないものであれば-1を返す。なお、先頭と後端の余分な半角スペースがあってもよい。
	 * @param target 調べる文字列
	 * @int 1:物理量(単位の計算はしないため無次元量である可能性もある),
	 * 		 0：無次元量,
	 * 		-1：どちらでもない文字列
	 * */
	public static int isPhysicalQuantity(String target){
		if(target.matches(" *"+doubleRegex+" *")){
			//無次元量の場合
			return 0;
		}else{
			if(!target.matches(" *"+doubleRegex+" +"+"[μmcdhkMdagsA/\\-0-9 ]*")) {
				//"468.46 cm 4684.453 ks"等の数字の2回以上の入力を無効にする
				//"fs468.38 cm"等の数字の前に文字が来るのを無効にする
				//単位の部分の不確かさは後で各単体単位毎に判定を行う
				return -1;
			}
			String unitStr = target.split(" *"+doubleRegex+" +")[1];

			if(isUnit(unitStr)) {
				return 1;
			}else {
				return -1;
			}
		}
	}

	/*
	 * 指定された文字列が適切な組立単位かどうかを判定する。
	 * @param target 調べる文字列
	 * @return true:単位と判定,　false：単位ではないと判定
	 * */
	private static boolean isUnit(String target){
		if(target.split("/").length > 2) {
			//"/"を使いすぎている
			return false;
		}
		//単体単位(km,ms2,kA-2など)の配列に変換し、単体単位かどうかを調べる
		String[] units = target.split(" |/");
		for(String unit:units){
			if(unit.equals("")){
				continue;
			}
			if(unit.matches(" *([μmcdhkM]|da)?[mgsA](-?[0-9]+)? *")){
				continue;
			}else{
				return false;
			}
		}
		return true;
	}

	/*
	 * 単位(組立単位)を受け取り、m,kg,s,Aの次数と10^-3などの接頭辞による係数を
	 * もったマップを返す。接頭辞による係数のキーは"none"
	 * 指定する単位は適切な組立単位であることが前提である。
	 * */
	public static HashMap<String,Integer> moldUnit(String targetUnit){
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
