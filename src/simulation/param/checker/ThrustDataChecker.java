package simulation.param.checker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ThrustDataChecker implements ParameterChecker {

	//データファイルを読み込み、それが燃焼データのフォーマットに即していない場合はエラー
	/*
	 * データファイルを読み込み、それが燃焼データのフォーマットに即していない場合はエラー
	 * @param input 読み込むファイルのパスの文字列表現
	 * @param maxValue,minValue 何を指定しても無視される
	 * @return 0の場合は燃焼データファイルであることを意味する。2の場合は燃焼データではないことを意味する。
	 * */
	@Override
	public int checkFormatOf(String input, String maxValue, String minValue) {
		if(isThrustDataFile(input)) {
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
				if(pastTime == -1 && Double.parseDouble(dataArray[0]) != 0) {
					return false;
				}
				//2つ目のデータも数値に変換できるか
				Double.parseDouble(dataArray[1]);
				pastTime = Double.parseDouble(dataArray[0]);
			}
			return true;
		} catch (IOException | NumberFormatException e) {
			System.out.println(e);
			//IOException ファイルが存在しない場合false
			//NumberFormatException ファイル内の文字列が数値に変換不可能であればfalse
			return false;
		}
	}
}
