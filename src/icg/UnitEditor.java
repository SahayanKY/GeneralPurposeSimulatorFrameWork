package icg;

import java.util.HashMap;

public class UnitEditor {

	/*
	 * 1番目の引数に指定された物理量を2番目の引数に指定された単位に変換し、そのString
	 * 表現を返す。このメソッドの戻り値は単位つきのStringであることに注意する。
	 * 変換の前後で次元が違う場合や物理量としてのフォーマットを満たない場合はnull値を、
	 * 無次元量の場合はafterUnitに何を指定してもOriginValueが返る。
	 * @param OriginValue 変換したい物理量を単位付きで指定
	 * @param afterUnit 変換後の単位を指定
	 * */
	public static String convert_from_toWithUnits(String OriginValue,String afterUnit) {
		Double Number;
		if(OriginValue.matches(" *[0-9]*\\.?[0-9]+ *")){
			//無次元量の場合
			//変換はできないのでそのまま返す
			return OriginValue;
		}else if(!OriginValue.matches(" *[0-9]*\\.?[0-9]+ [mgsAμcdahkM/]* *")){
			//物理量でない場合
			//問答無用
			return null;
		}
		
		String[] sts = OriginValue.split(" ",2);
		try {
			Number = Double.parseDouble(sts[0]);
		}catch(NumberFormatException e) {
			//数値に変換できないものは論外
			return null;
		}
		HashMap<String,Integer> beforeUnitMap = moldUnit(sts[1]);
		HashMap<String,Integer> afterUnitMap = moldUnit(afterUnit);
		for(String unit:new String[] {"m","kg","s","A"}) {
			if(!beforeUnitMap.getOrDefault(unit,0).equals(afterUnitMap.getOrDefault(unit,0))) {
				//次元が違う場合変換不可能なのでnull
				return null;
			}
		}
		Number *= Math.pow(10, beforeUnitMap.get("none")-afterUnitMap.get("none"));

		return Number +" "+ afterUnit;
	}
	
	/*
	 * 指定された文字列が物理量であるかどうかを調べる。物理量であれば1を、無次元量であれば0を、
	 * どちらでもないものであれば-1を返す。なお、先頭と後端の余分な半角スペースがあってもよい。
	 * @param target 調べる文字列
	 * @int 1:物理量,0：無次元量,-1：どちらでもない文字列
	 * */
	private static int isPhysicalQuantity(String target){
		if(target.matches(" *[0-9]*\\.?[0-9]+ *")){
			//無次元量の場合
			return 0;
		}else{
			String unitStr = target.split(" *[0-9]*\\.?[0-9]+")[1];
			if(unitStr.split("/").length > 2){
				//"/"を使いすぎている
				return -1;
			}
			//単体単位(km,ms2,kA-2など)の配列に変換する
			String[] units = unitStr.split(" |/");
			for(String unit:units){
				if(unit.equals("")){
					continue;
				}
				if(!isOneUnit(unit)){
					return -1;
				}
			}
		}
			
		return 1;
	}
	
	/*
	 * 指定された文字列が1つのトークンからなる単位(単体単位)かどうかを判定する。
	 * 例えば、"mm2"や"  ks-2  "は単位と判定し、1を返す。
	 * "cm A"などの入力は-1となって、単位と判定されないため注意。
	 * @param target 調べる文字列
	 * @return true:単位と判定,　false：単位ではないと判定
	 * */
	private static boolean isOneUnit(String target){
		if(target.matches(" *([μmcdhkM]{0,1}|da)[mgsA]-?[0-9]* *")){
			return true;
		}else{
			return false;
		}
	}

	/*
	 * 単位(組立単位)を受け取り、m,kg,s,Aの次数と10^-3などの接頭辞による係数を
	 * もったマップを返す。接頭辞による係数のキーは"none"
	 * */
	private static HashMap<String,Integer> moldUnit(String originUnit){
		HashMap<String,Integer> unitMap = new HashMap<>();
		String[][] units;
		String[] positiveUnits = originUnit.split("/");
		if(positiveUnits.length > 2 || positiveUnits.length == 0) {
			//"/"は一度だけなので
			return null;
		}else if(positiveUnits.length == 2) {
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
				if(u.matches("([μmcdhkM]{0,1}|da)[mgsA]")) {
					//次数が1で省略されている場合
					degree = 1;
					prefix = convertPrefix(u.substring(0,u.length()-1));
					standardUnit = u.substring(u.length()-1,u.length());
				}else if(u.matches("([μmcdhkM]{0,1}|da)[mgsA]-{0,1}[0-9]+")){
					//次数が1でない場合
					degree = Integer.parseInt(u.split("([μmcdhkM]{0,1}|da)[mgsA]")[1]);
					u = u.split("-{0,1}[0-9]+")[0];
					//ここから先は次数が1の場合と同じ
					prefix = convertPrefix(u.substring(0,u.length()-1));
					standardUnit = u.substring(u.length()-1,u.length());
				}else {
					return null;
				}
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
