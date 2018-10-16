package simulation.param;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.function.Predicate;

import icg.DoubleBackSlashReader;
import simulation.Simulator;

/*
 * 各種パラメータのリストであり、状態を一括でチェックしたり、パラメータ値を設定するクラス
 * */
public class ParameterManager {
	private final Simulator simulater;
	private ArrayList<Parameter> paramList = new ArrayList<>();
	private String warningMessage = "";

	public ParameterManager(Simulator simulater){
		this.simulater = simulater;
	}

	/* 外部(Frame)から入力値をセットする時の呼び出し用
	 * データ入力に不具合がないかをチェックし、同時にcheckFormatOf()内で各Parameterのvalueを更新する。
	 * この処理はバッチ処理ではない。
	 * @param checkMap 入力データのString型のマップ
	 * @return 不具合を検知した項目の数と一覧文字列を合わせた1つの文字列。
	 * 計算を続行できない不具合の場合"エラー"から始まる文字列を、
	 * 計算の続行は可能な不具合の場合"要検証"から始まる文字列を、
	 * 特に何も無かった場合nullを返す。
	 * */
	public String checkAllInputDataFormat(LinkedHashMap<String,LinkedHashMap<String,String>> checkMap){
		//入力値のチェック
		StringJoiner Errors = new StringJoiner(System.getProperty("line.separator")),
				Warnings = new StringJoiner(System.getProperty("line.separator")),
				WarningsForProperty = new StringJoiner(", ");
		int ErrorTime=0,WarnTime=0;
		for(Parameter param : paramList) {
			if(param.isSystemInputParameter) {
				//システムが入力するパラメータならチェックする必要なし

				continue;
			}
			String inputString = checkMap.get(param.parentLabel).get(param.childLabel);
			switch(param.setAndCheckFormatOf(inputString)) {
				case Parameter.inputformat_NoProblem:
					continue;
				case Parameter.inputformat_Warning:
					Warnings.add(param.parentLabel+"."+param.childLabel);
					WarningsForProperty.add(param.parentLabel+"."+param.childLabel);
					WarnTime++;
					continue;
				case Parameter.inputformat_Error:
					Errors.add(param.parentLabel+"."+param.childLabel);
					ErrorTime++;
					continue;
			}
		}

		if(ErrorTime>0) {
			return "エラー : "+ErrorTime +"件\n"+ Errors.toString();
		}else if(WarnTime>0) {
			warningMessage = WarningsForProperty.toString();
			return "要検証 : "+WarnTime +"件\n"+ Warnings.toString();
		}else {
			warningMessage = "";
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
	public void setData_by(File choosedFile) throws IOException{
		try (DoubleBackSlashReader reader = new DoubleBackSlashReader(new InputStreamReader(new FileInputStream(choosedFile), "UTF-8"))){
			Properties ExistingProperty = new Properties();
			ExistingProperty.load(reader);

			for(Parameter param : paramList) {
				if(param.isSystemInputParameter) {
					continue;
				}
				param.setValue(ExistingProperty.getProperty(param.propertyLabel));
			}
		} catch (IOException e) {
			throw e;
		}
	}

	/*
	 * 追加するParameterインスタンスを指定する
	 * */
	public void addParameter(Parameter parameter) {
		this.paramList.add(parameter);
	}


	/*
	 * 各パラメータがもつvalueを指定されたディレクトリに作成したプロパティファイルに書き込む
	 * @param choosedDirectory 保存先ディレクトリ
	 * @param result パラメータに関連した値を同時に書き込む際に指定する
	 * 		各文字列は"="を1つだけ含まなければならない。
	 * @throws IOException ファイル保存の際に発生した何らかの不具合を表す例外
	 * */
	public void writePropertyOn(File choosedDirectory,String propertyLine[][]) throws IOException{
		File storeFile = new File(choosedDirectory.toString()+"\\"+simulater.getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日HH時mm分ss.SSS秒")) +"シミュレーションパラメータ.properties");
		if(storeFile.exists()) {
			//既に同名のファイルが存在する場合処理を停止
			throw new IOException("同名のファイルが存在");
		}
		try(
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(storeFile),"UTF-8"));
		){
			writer.write("#シミュレーション条件記録ファイル");
			writer.newLine();
			writer.write("#各項目の想定Min値から想定Max値の間に無いものには警告が出ます");
			writer.newLine();
			writer.newLine();
			writer.write("警告="+ warningMessage);
			writer.newLine();

			//プロパティファイルのフォーマットに反していないかを判別する関数
			Predicate<String> formatMatcher =  str -> {
				boolean result;
				if(str.startsWith("#")) {
					result = true;
				}else {
					int
						index = str.indexOf("="),
						lastindex = str.lastIndexOf("=");

					if(index == -1) {
						//そもそも"="を含まない場合
						result = false;
					}else if(index != lastindex){
						//"="が複数個存在する場合
						result = false;
					}else if(index == 0 || lastindex == str.length() -1) {
						//"="で文字列が始まっていたり、終わっていたりする場合
						result = false;
					}else {
						result = true;
					}
				}
				return result;
			};


			//Simulatorクラスで希望した出力値を出力
			for(String line : propertyLine[0]) {
				if(!formatMatcher.test(line)) {
					line = "#"+ line;
				}
				writer.write(line);
				writer.newLine();
			}


			for(Parameter p:paramList) {
				writer.newLine();
				writer.write(p.propertyLabel +"="+ p.getValue());
				if(p.maxValue != null) {
					writer.newLine();
					writer.write("Max"+p.propertyLabel +"="+ p.maxValue);
				}
				if(p.minValue != null) {
					writer.newLine();
					writer.write("Min"+p.propertyLabel +"="+ p.minValue);
				}
				writer.newLine();
			}


			//Simulator子クラスで希望した出力値を出力
			for(int i=1;i<propertyLine.length;i++) {
				for(String line : propertyLine[i]) {
					if(!formatMatcher.test(line)) {
						line = "#"+ line;
					}
					writer.write(line);
					writer.newLine();
				}
			}

			writer.flush();
		}catch(IOException e) {
			throw e;
		}
	}

	/*
	 * パラメータの構造を示すマップを返す。
	 * */
	public LinkedHashMap<String,LinkedHashMap<String,Parameter>> getInputParamMap(boolean isUserParamMap){
		LinkedHashMap<String,LinkedHashMap<String,Parameter>> parentMap = new LinkedHashMap<>();
		for(Parameter param : paramList) {
			if(isUserParamMap && param.isSystemInputParameter) {
				continue;
			}
			String parentLabel = param.parentLabel;
			String childLabel = param.childLabel;
			LinkedHashMap<String,Parameter> childMap = parentMap.getOrDefault(parentLabel, new LinkedHashMap<String,Parameter>());
			childMap.put(childLabel, param);
			parentMap.put(parentLabel, childMap);
		}
		return parentMap;
	}
}
