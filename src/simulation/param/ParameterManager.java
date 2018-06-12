package simulation.param;

import java.io.File;
import java.io.FileInputStream;
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

import icg.DoubleBackSlashReader;
import icg.ExProperties;

public class ParameterManager {
	//private static ArrayList<Parameter> paramList = new ArrayList<>();

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
			return "要検証 : "+WarnTime +"件\n"+ Warnings.toString();
		}else {
			return null;
		}
	}

	/* 外部の既存プロパティファイルでセットする時の呼び出し用
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
				param.value = ExistingProperty.getProperty(param.childLabel);
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
		Files.copy(getFormatStream() , storeFilePath);

		ExProperties exP = new ExProperties(storeFile);
		for(Parameter param : Parameter.values()) {
			exP.setProperty(param.childLabel, param.value);
		}
		exP.postscript();

	}

	/*
	 * パラメータの構造を示すマップを返す。
	 * */
	public static LinkedHashMap<String,LinkedHashMap<String,String>> getEnumMap(){
		LinkedHashMap<String,LinkedHashMap<String,String>> parentMap = new LinkedHashMap<>();
		for(Parameter param : Parameter.values()) {
			String parentLabel = param.parentLabel;
			String childLabel = param.childLabel;
			LinkedHashMap<String,String> childMap = parentMap.getOrDefault(parentLabel, new LinkedHashMap<String,String>());
			childMap.put(childLabel, param.value);
			parentMap.put(parentLabel, childMap);
		}
		return parentMap;
	}
}
