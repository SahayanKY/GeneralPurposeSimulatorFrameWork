package icg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Properties;

/*
 * Propertiesの順番通りに出力してくれない、パス入力の面倒な仕様、既存のコメント削除仕様の改善
 * */
public class ExProperties extends Properties{
	private File file;

	public static void main(String args[] ) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("D:\\ゆうき\\test.txt"));
			bw.write("aaaaa\naaaaaaaa");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	ExProperties(File file){
		this.file = file;
	}

	/*
	 * コンストラクタで指定したファイルをベースにプロパティを更新する
	 * */
	public void postscript() throws IOException{
		//複製を作り、それを見ながらパラメータをセットしていく
		File copyFile = new File(file.toString() +"copy");
		Files.copy(Paths.get(file.toString()), Paths.get(copyFile.toString()));
		try(
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			BufferedReader reader = new BufferedReader(new FileReader(copyFile));
		){
			String lineStr;
			Hashtable<Object,Object> tab = (Hashtable<Object,Object>)clone();
			while((lineStr = reader.readLine())!=null) {
				if(lineStr.matches("[#!]{1}.*") || !lineStr.matches(".+[=]{1}.*")) {
					//プロパティを記述している文で無かった場合
					writer.write(lineStr);
					writer.newLine();
					continue;
				}
				String key,oldValue,newValue;
				String sublineStr[] = lineStr.split("=", 2);
				key = sublineStr[0];
				oldValue = sublineStr[1];
				newValue = (String)tab.get(key);


				if(newValue == null) {
					//パラメータが更新されなかった場合同じ内容を書き込む
					writer.write(lineStr);
				}else {
					//更新された場合はその値を書き込む
					//同時に書き終わったものを消しておく
					writer.write(key+"="+newValue);
					tab.remove(key);
				}

				writer.newLine();
			}
			for(Object key : tab.keySet()) {
				writer.write((String)key + "=" + tab.get(key));
				writer.newLine();
			}
			writer.flush();
		}catch(IOException e) {
			System.out.println(e);
		}
		copyFile.delete();
	}
}
