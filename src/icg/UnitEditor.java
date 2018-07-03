package icg;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitEditor {
	public enum Prefixs{
		μ(-6),m(-3),c(-2),d(-1),da(1),h(2),k(3),M(6),none(0);
		int n;
		Prefixs(int n){
			this.n = n;
		}

		/*
		 * 列挙子に対応する整数を返す。指定された文字列に対応する列挙子が存在しない場合、
		 * 0が返る。
		 * @param 変換したい接頭辞の文字列表現
		 * @return 対応する整数値
		 * */
		public static int convertPrefix(String prefix) {
			for(Prefixs p:Prefixs.values()) {
				if(p.name().equals(prefix)) {
					return p.n;
				}
			}
			return 0;
		}
	}
//	private static Pattern physicalQuantityPattern = Pattern.compile("^ *(\\-?[0-9]*\\.?[0-9]+(?:E-?[0-9]+)?)( +(?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)?| +)+/?(?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)?| +)*| +/ *(?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)?| +)+)?$");

	private static Pattern physicalQuantityPattern = Pattern.compile("^ *(\\-?[0-9]*\\.?[0-9]+(?:E-?[0-9]+)?)((?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)?| +|/)+)$");
	private static Pattern numberPattern = Pattern.compile("^ *(\\-?[0-9]*\\.?[0-9]+(?:E-?[0-9]+)?) *$");
	private static Pattern unitsPattern = Pattern.compile("^((?:(?:[μmcdhkM]|da)?[mgsA](?:-?[0-9]+)?| +|/)+)$");
	private static Pattern oneUnitPattern = Pattern.compile("^((?:[μmcdhkM]|da)?)([mgsA])((?:-?[0-9]+)?)$");

	private static String doubleRegex = "-?[0-9]*\\.?[0-9]+(E-?[0-9]+)?";


	/*
	 * 指定された文字列が物理量(または数値)であるかどうかを調べる。物理量であればtrueを、
	 * どちらでもないものであればfalseを返す。なお、先頭と後端の余分な半角スペースがあってもよい。
	 * @param target 調べる文字列
	 * @boolean
	 * true:物理量または数値
	 * false：どちらでもない文字列
	 * */
	/*private static boolean isPhysicalQuantity_OrNumberOnly(String target){
		Matcher physicalM = physicalQuantityOrNumberPattern.matcher(target);
		if(physicalM.find()) {
			return true;
		}else{
			return false;
		}
	}*/


	/*
	 * 指定された文字列が適切な組立単位かどうかを判定する。
	 * @param target 調べる文字列
	 * @return
	 * true:単位,
	 * false:単位ではない
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
	 * 指定された物理量、数値または単位の次元解析を行う。
	 * 返るマップには"Number"(元の文字列の数値の部分),"none"(接頭辞の解析結果),"m","kg","s","A"のキーが最大でマップされている
	 * なお、その値が0のときはキーは除去されている。
	 * @param 次元解析を行う文字列
	 * @return その結果を表すマップ
	 * */
	public static HashMap<String,Number> dimensionAnalysis(String quantity){
		if(quantity == null) {
			throw new NullPointerException();
		}
		HashMap<String,Number> unitMap = new HashMap<String,Number>();
		Matcher matcher;
		String[] UnitLineSlashSplit = null;

		String message = null;

		matcher = numberPattern.matcher(quantity);
		if(matcher.find()) {
			//次元解析するまでもなくただの数値だった場合
			unitMap.put("Number", Double.parseDouble(matcher.group(1)));
			return unitMap;
		}else {
			matcher = unitsPattern.matcher(quantity);
			if(matcher.find()) {
				//単位単体だった場合
				UnitLineSlashSplit = matcher.group(1).split("/");
			}else {
				matcher = physicalQuantityPattern.matcher(quantity);
				if(matcher.find()) {
					unitMap.put("Number", Double.parseDouble(matcher.group(1)));
					UnitLineSlashSplit = matcher.group(2).split("/");
				}else {
					message = "指定された文字列は数値でも単位でも物理量でもありません。";
				}
			}
		}


		if(message == null && UnitLineSlashSplit.length > 2) {
			message = "指定された文字列は過剰に\"/\"を含んでいます。";
		}

		if(message != null) {
			throw new IllegalArgumentException(message);
		}


		String[][] units;
		if(UnitLineSlashSplit.length == 2) {
			String[] negativeUnits = UnitLineSlashSplit[1].split(" ");
			UnitLineSlashSplit = UnitLineSlashSplit[0].split(" ");
			units = new String[][]{UnitLineSlashSplit , negativeUnits};
		}else {
			//"/"の無い単位だった場合
			units = new String[][] {UnitLineSlashSplit[0].split(" ")};
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
				if(!oneUnitMatcher.find()) {
					throw new IllegalArgumentException("指定された文字列の単位を認識できません。");
				}

				String prefixStr = oneUnitMatcher.group(1);
				String standardUnit = oneUnitMatcher.group(2);
				String degreeStr = oneUnitMatcher.group(3);

				int prefix = (prefixStr.equals(""))? 0: Prefixs.convertPrefix(prefixStr);
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
}
