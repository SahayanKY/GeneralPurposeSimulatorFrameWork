package icg;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class DataInputFrameTest {
	public static void main(String args[]) {
		new DataInputFrame();
		//new TestDataInputFrame().test();
		//new TestDataInputFrame().testB();
	}

	private void testB() {
		StringBuilder stb = new StringBuilder();
		System.out.println(stb.toString().equals(""));
	}

	private void test() {
		Reader reader;
		try {
			reader = new FileReader(System.getProperty("user.dir")+"\\bin\\icg\\入力データフォーマット.properties");
			Properties p = new Properties();
			p.load(reader);
			System.out.println(p.getProperty("機体バージョン")+"bbbb");
			System.out.println(p.getProperty("機体バージョン") == null);
			System.out.println(p.getProperty("機体バージョン").equals(""));
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}


	}

}
