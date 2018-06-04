package icg;

import java.util.HashMap;

public class UnitEditor {
	public static void main(String args[]) {
		HashMap<String,Integer> map = new UnitEditor().editUnit("dg km mA cs/ ms  kg dm cA");
		if(map == null) {
			System.out.println("不正な入力値");
		}else {
			for(String key : map.keySet()) {
				System.out.println(key +":"+ map.get(key));
			}
		}
	}

	public void method() {
		String value = "120 cm";
		String convertedValue = convert_from_to(value, "km");
	}

	public String convert_from_to(String OriginValue,String afterUnit) {
		Double Number;
		String[] sts = OriginValue.split(" ",2);
		if(sts.length != 2 || sts[1].equals("")) {
			//無次元量の場合
			//変換は不可能なのでnull
			return null;
		}
		try {
			Number = Double.parseDouble(sts[0]);
		}catch(NumberFormatException e) {
			return null;
		}
		editUnit(sts[1]);


		return OriginValue;
	}

	/*
	 * 単位(組立単位)を受け取り、m,kg,s,Aの次数と10^-3などの接頭辞による係数を
	 * もったマップを返す。接頭辞による係数のキーは"none"
	 * */
	private HashMap<String,Integer> editUnit(String unit){
		HashMap<String,Integer> unitMap = new HashMap<>();
		String[][] units;
		String[] positiveUnits = unit.split("/");
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
		int g_degree = unitMap.remove("g");
		unitMap.put("kg", g_degree);
		unitMap.put("none", unitMap.getOrDefault("none", 0) - 3*g_degree);

		for(String key:new String[]{"m","kg","s","A"}) {
			//mapのkeySet()を使うとremoveしたときに例外が発生してしまうので
			//次数が0のものは消しておく
			if(unitMap.getOrDefault(key,0) == 0) {
				unitMap.remove(key);
			}
		}

		return unitMap;
	}


	private int convertPrefix(String prefix) {
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
