package simulation.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import parabolicmovement.Result;

/**
 * 与えられたインスタンスをCSVに出力します。
 * */
public class CSVWriter {

	/**
	 * 与えられたResultインスタンスをCSVに出力します。
	 * CSVのヘッダーにはResultインスタンスが保持しているヘッダーの文字列を与えます。
	 * @param result
	 * 出力する計算結果
	 * @param storeFile
	 * 出力するファイルのインスタンス。ファイル名と拡張子を含む。
	 * */
	public void write(Result result,File storeFile) {
		try(
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(storeFile),"UTF-8"));
		){
			writer.write(result.headerString());
			writer.newLine();
			int lineNum = result.lineNum;
			for(int i=0;i<lineNum;i++) {
				String linestr = result.lineString(i);
				writer.write(linestr);
				writer.newLine();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
