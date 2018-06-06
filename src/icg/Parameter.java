package icg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.StringJoiner;

/*
 * 入力データの保持及び保存を担うクラス
 * */
public enum Parameter{
	機体バージョン("一般","機体バージョン"){
		@Override
		public int checkFormatOf(String input) {
			//未入力、または空白文字のみの場合エラー
			if(input.matches("[ 　]+") | input.equals("")) {
				return 2;
			}else {
				super.valueStr = input;
				return 0;
			}
		}
	},
	燃焼データ年月("一般","使用燃焼データ年月XXXX/YY"){
		@Override
		public int checkFormatOf(String input) {
			//年月の入力フォーマットに即していない場合エラー
			if(!input.matches("(19|20)[0-9]{2}/(0[1-9]|1[012])")) {
				return 2;
			}else {
				super.valueStr = input;
				return 0;
			}
		}
	},
	燃焼データファイル("一般","燃焼データファイル"){
		@Override
		public int checkFormatOf(String input) {
			//データファイルを読み込み、それが燃焼データのフォーマットに即していない場合はエラー
			if(isThrustDataFile(input)) {
				super.valueStr = input;
				return 0;
			}else {
				return 2;
			}
		}

		/*
		 * 入力されたパスが燃焼データなのかをチェックする
		 * @param filePath 入力されたパス
		 * @return falseの場合はデータに異常
		 * */
		private boolean isThrustDataFile(String filePath) {
			try (BufferedReader dataFileReader = new BufferedReader(new FileReader(filePath));){
				String dataLineStr;
				double pastTime=-1;
				while((dataLineStr = dataFileReader.readLine()) != null) {
					//改行だけの行は跳ばす
					if(dataLineStr.equals("")) {
						continue;
					}
					//半角スペース、タブ文字、一回の","区切りであるか。また、1行のデータが時間、推力の2つであるか。
					String[] dataArray = dataLineStr.split(" +|	+|,{1}");
					if(dataArray.length != 2) {
						return false;
					}
					//double型に変換できるか
					//時間は単調増加になっているか(time)
					if(pastTime >= Double.parseDouble(dataArray[0])) {
						return false;
					}
					//timeの初期値は0に合わせる
					//最初の代入前に確認
					if(pastTime == -1 & Double.parseDouble(dataArray[0]) != 0) {
						return false;
					}
					//2つ目のデータも数値に変換できるか
					Double.parseDouble(dataArray[1]);
					pastTime = Double.parseDouble(dataArray[0]);
				}
				return true;
			} catch (IOException | NumberFormatException e) {
				//IOException ファイルが存在しない場合false
				//NumberFormatException ファイル内の文字列が数値に変換不可能であればfalse
				return false;
			}
		}
	},
	空気密度("一般","空気密度/[kg/m^3]"),
	最大飛行速度("一般","最大飛行速度/[m/s]"),
	比推力("一般","比推力/[s]"),
	ランチャー長さ("一般","ランチャー長さ/[m]"),
	風速計高さ("一般","風速計高さ/[m]"),


	ロケット外径("ロケット全体","ロケット外径/[mm]"),
	ロケット内径("ロケット全体","ロケット内径/[mm]"),
	全体圧力中心位置("ロケット全体","全体圧力中心位置/[mm]"),
	全体重心位置("ロケット全体","全体重心位置/[mm]"){
		@Override
		public int checkFormatOf(String input) {
			//入力値が"a"で終わる場合燃焼後を、"b"で終わる場合燃焼前を意味する。
			if(!(input.endsWith("a") || input.endsWith("b"))) {
				return 2;
			}
			String subInput = input.substring(0, input.length()-1);
			int messageNum = super.checkFormatOf(subInput);
			super.valueStr = input;
			return messageNum;

			//input = "4564a"
			//→value = 4564, valueStr = "4564a"
		}
	},
	ロケット全長("ロケット全体","ロケット全長/[mm]"),
	ロケット質量("ロケット全体","ロケット質量/[g]"){
		@Override
		public int checkFormatOf(String input) {
			//入力値が"a"で終わる場合燃焼後を、"b"で終わる場合燃焼前を意味する。
			if(!(input.endsWith("a") || input.endsWith("b"))) {
				return 2;
			}
			String subInput = input.substring(0, input.length()-1);
			int messageNum = super.checkFormatOf(subInput);
			super.valueStr = input;
			return messageNum;

			//input = "4564a"
			//→value = 4564, valueStr = "4564a"
		}
	},
	抗力係数("ロケット全体","抗力係数/[-]"),


	ノーズコーン長さ("ノーズコーン", "ノーズコーン長さ/[mm]"),
	ノーズコーン質量("ノーズコーン", "ノーズコーン質量/[g]"),
	ノーズコーン重心位置("ノーズコーン", "ノーズコーン重心位置/[mm]"),
	ノーズコーン圧力中心位置("ノーズコーン", "ノーズコーン圧力中心位置/[mm]"),
	ノーズコーン法線力係数("ノーズコーン", "ノーズコーン法線力係数/[-]"),


	分離機構長さ("分離機構", "分離機構長さ/[mm]"),
	分離機構質量("分離機構", "分離機構質量/[g]"),
	分離機構先端位置("分離機構", "分離機構先端位置/[mm]"),
	分離機構外径("分離機構", "分離機構外径/[mm]"),


	チューブ1長さ("ボディーチューブ", "チューブ1長さ/[mm]"),
	チューブ2長さ("ボディーチューブ", "チューブ2長さ/[mm]"),
	チューブ3長さ("ボディーチューブ", "チューブ3長さ/[mm]"),
	チューブ1質量("ボディーチューブ", "チューブ1質量/[g]"),
	チューブ2質量("ボディーチューブ", "チューブ2質量/[g]"),
	チューブ3質量("ボディーチューブ", "チューブ3質量/[g]"),
	チューブ1重心位置("ボディーチューブ", "チューブ1重心位置/[mm]"),
	チューブ2重心位置("ボディーチューブ", "チューブ2重心位置/[mm]"),
	チューブ3重心位置("ボディーチューブ", "チューブ3重心位置/[mm]"),


	エンジンを抑えるプレート質量("エンジンを抑えるプレート", "エンジンを抑えるプレート質量/[g]"),
	エンジンを抑えるプレート先端位置("エンジンを抑えるプレート", "エンジンを抑えるプレート先端位置/[mm]"),
	エンジンを抑えるプレート長さ("エンジンを抑えるプレート", "エンジンを抑えるプレート長さ/[mm]"),


	燃焼前グレイン質量("グレイン", "燃焼前グレイン質量/[g]"),
	燃焼後グレイン質量("グレイン", "燃焼後グレイン質量/[g]"),
	グレイン先端位置("グレイン", "グレイン先端位置/[mm]"),
	グレイン長さ("グレイン", "グレイン長さ/[mm]"),
	グレイン外径("グレイン", "グレイン外径/[mm]"),


	インジェクターベル質量("インジェクターベル", "インジェクターベル質量/[g]"),
	インジェクターベル先端位置("インジェクターベル", "インジェクターベル先端位置/[mm]"),
	インジェクターベル長さ("インジェクターベル", "インジェクターベル長さ/[mm]"),
	インジェクターベル外径("インジェクターベル", "インジェクターベル外径/[mm]"),


	燃焼前酸化剤タンク質量("酸化剤タンク", "燃焼前酸化剤タンク質量/[g]"),
	燃焼後酸化剤タンク質量("酸化剤タンク", "燃焼後酸化剤タンク質量/[g]"),
	酸化剤タンク先端位置("酸化剤タンク", "酸化剤タンク先端位置/[mm]"),
	酸化剤タンク長さ("酸化剤タンク", "酸化剤タンク長さ/[mm]"),
	酸化剤タンク外径("酸化剤タンク", "酸化剤タンク外径/[mm]"),


	フィン枚数("フィン", "フィン枚数"){
		@Override
		public int checkFormatOf(String input) {
			//整数値でない場合や想定の整数値でない場合はエラー
			try {
				int n;
				//3,4枚を想定
				if(!((n = Integer.parseInt(input)) == 3 | n==4)) {
					throw new NumberFormatException();
				}
				value = (double) n;
				valueStr = input;
				return 0;
			}catch(NumberFormatException e) {
				return 2;
			}
		}
	},
	フィン高さ("フィン", "フィン高さ/[mm]"),
	フィン根本長さ("フィン", "フィン根本長さ/[mm]"),
	フィン端部長さ("フィン", "フィン端部長さ/[mm]"),
	フィン後退長さ("フィン", "フィン後退長さ/[mm]"),
	フィン先端位置("フィン", "フィン先端位置/[mm]"),
	フィン重心位置("フィン", "フィン重心位置/[mm]"),
	フィン質量("フィン", "フィン質量/[g]"),
	フィン圧力中心位置("フィン", "フィン圧力中心位置/[mm]"),
	フィン法線力係数("フィン", "フィン法線力係数/[-]");

	public final String parentLabel;
	public final String childLabel;

	protected String valueStr;
	protected double value;

	private final static Properties FormatProperty = new Properties();
	private final static String FormatPropertyPath = System.getProperty("user.dir")+"\\bin\\icg\\入力データフォーマット.properties";

	static {
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(FormatPropertyPath), "UTF-8")){
			FormatProperty.load(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	Parameter(String parentLabel, String childLabel){
		this.parentLabel = parentLabel;
		this.childLabel = childLabel;
	}


	public String getParameterStringValue() {return this.valueStr;	}
	public double getParameterDoubleValue() {return this.value;}


	/*
	 * パラメータのチェック。同時にその値をvalueに格納。
	 * @param input 入力値のString表現
	 * @return 0の場合は異常なし、1の場合は警告、2の場合はエラーで計算続行不可
	 */
	public int checkFormatOf(String input) {
		try {
			String maxValueStr = FormatProperty.getProperty("Max"+childLabel);
			String minValueStr = FormatProperty.getProperty("Min"+childLabel);

			int message=0;

			if(maxValueStr != null && UnitEditor.isLargerA_thanB(input,maxValueStr)) {
				//最大値の設定があるが、それを上回っていた場合
				message = 1;
			}
			if(minValueStr != null && UnitEditor.isLargerA_thanB(minValueStr,input)) {
				message = 1;
			}

			valueStr = input;
			return message;
		}catch(IllegalArgumentException e) {
			//物理量入力のフォーマットに従っていない
			return 2;
		}
	}


	/* 検索用。
	 * 指定された名前(子ラベル)をもつ列挙子を返す。頭から検索をかけるため、連続して使うのは非推奨。
	 * @param 子ラベルのString表現
	 * @return 対応する列挙子。無い場合はnull。
	 * */
	public static Parameter valueWhoseChildLabelIs(String childLabel) {
		for(Parameter parameter:Parameter.values()) {
			if(parameter.childLabel.equals(childLabel)) {
				return parameter;
			}
		}
		return null;
	}

	/* マップ形式の自身の出力。
	 * この列挙型がもつ列挙子のマップを、列挙子の持つ値とともに返す。
	 * @return LinkedHashMap<"列挙子の親ラベル",LinkedHashMap<"列挙子の子ラベル","列挙子の持つ値のString表現">>
	 * */
	public static LinkedHashMap<String,LinkedHashMap<String,String>> getEnumValueMap(){
		LinkedHashMap<String,LinkedHashMap<String,String>> parentMap = new LinkedHashMap<>();
		for(Parameter param : Parameter.values()) {
			String parentLabel = param.parentLabel;
			String childLabel = param.childLabel;
			LinkedHashMap<String,String> childMap = parentMap.getOrDefault(parentLabel, new LinkedHashMap<String,String>());
			childMap.put(childLabel, param.valueStr);
			parentMap.put(parentLabel, childMap);
		}
		return parentMap;
	}

	/* 外部(Frame)から入力値をセットする時の呼び出し用
	 * データ入力に不具合がないかをチェックし、同時にcheckFormatOf()内で各Parameterのvalueを更新する。
	 * この処理はバッチ処理ではない。
	 * @param checkMap 入力データのString型のマップ
	 * @return 不具合を検知した項目の数と一覧文字列を合わせた1つの文字列。
	 * 計算を続行できない不具合の場合"エラー"から始まる文字列を、
	 * 計算の続行は可能な不具合の場合"要検証"から始まる文字列を、
	 * 特に何も無かった場合nullを返す。
	 * @exception 引数より受けたデータに直ちに停止すべき不具合がある場合(入力値が空文字や数値でない、またはnull)
	 * */
	public static String checkAllInputDataFormat(LinkedHashMap<String,LinkedHashMap<String,String>> checkMap){
		//入力値のチェック
		StringJoiner Errors = new StringJoiner("\n"), Warnings = new StringJoiner("\n");
		int ErrorTime=0,WarnTime=0;
		for(Parameter param : Parameter.values()) {
			String inputString = checkMap.get(param.parentLabel).get(param.childLabel);
			switch(param.checkFormatOf(inputString)) {
				case 0:
					continue;
				case 1:
					Warnings.add(param.childLabel);
					WarnTime++;
					continue;
				case 2:
					Errors.add(param.childLabel);
					ErrorTime++;
					continue;
			}
		}
		if(ErrorTime>0) {
			return "エラー : "+ErrorTime +"件\n"+ Errors.toString();
		}else if(WarnTime>0) {
			return "要検証 : "+WarnTime +"件\n "+ Warnings.toString();
		}else {
			return null;
		}
	}

	/* 外部から既存プロパティファイルをセットする時の呼び出し用
	 * 指定されたプロパティファイルを読み込み、その値を各列挙子にセットする。
	 * このファイルがプロパティファイルでなかった場合、処理はされない。
	 * また、対応しないプロパティに関しては変化しない。
	 * @param choosedFile 指定するプロパティファイル
	 * @throws IOException 指定されたファイルの操作の際に発生した何らかの不具合
	 * */
	public static void setData_by(File choosedFile) throws IOException{
		try (DoubleBackSlashReader reader = new DoubleBackSlashReader(new InputStreamReader(new FileInputStream(choosedFile), "UTF-8"))){
			Properties ExistingProperty = new Properties();
			ExistingProperty.load(reader);

			for(Parameter param : Parameter.values()) {
				param.valueStr = ExistingProperty.getProperty(param.childLabel);
			}
		} catch (IOException e) {
			throw e;
		}
	}


	/*
	 * 各列挙子がもつvalueStrを指定されたプロパティファイルに書き込む
	 * @param choosedDirectory 保存先ディレクトリ
	 * @return エラーが起きた場合、その理由を示すStringを返す。
	 * @throws IOException ファイル保存の際に発生した何らかの不具合
	 * */
	public static void writeProperty_on(File choosedDirectory) throws IOException{
		Path storeFilePath = Paths.get(choosedDirectory.getPath() +"\\"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) +"シミュレーション.properties");
		File storeFile = storeFilePath.toFile();
		if(storeFile.exists()) {
			//既に同名のファイルが存在する場合処理を停止
			throw new IOException("同名のファイルが存在");
		}
		Files.copy(Paths.get(FormatPropertyPath), storeFilePath);
		ExProperties exP = new ExProperties(storeFile);
		for(Parameter param : Parameter.values()) {
			exP.setProperty(param.childLabel, param.valueStr);
		}
		exP.postscript();
	}
}
