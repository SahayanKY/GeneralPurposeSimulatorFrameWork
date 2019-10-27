package simulation.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import parabolicmovement.Result;

public class CSVWriter {

	public void writeNext(Result result,File storeFile) {
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
