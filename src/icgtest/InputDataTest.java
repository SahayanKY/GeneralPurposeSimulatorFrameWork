package icgtest;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.junit.Test;

public class InputDataTest {

	@Test
	public void testFinNumber () {
		String inputString = "3";
		try {
			int n;
			if(!((n = Integer.parseInt(inputString)) == 3 | n==4)) {
				throw new NumberFormatException();
			}
		}catch(NumberFormatException e) {
			fail();
		}
	}

	@Test
	public void testYearMonthFormat() {
		String inputString = "2001/12";
		//if(!inputString.matches("20[0-9]{2}0[1-9]") & !inputString.matches("20[0-9]{2}1[012]")) {
		if(!inputString.matches("(19|20)[0-9]{2}/(0[1-9]|1[012])")) {
			fail();
		}
	}

	@Test
	public void testProperties() {
		Properties p = new Properties();
		try(Reader reader = new FileReader(System.getProperty("user.dir")+"\\bin\\icg\\入力データフォーマット.properties")){
			p.load(reader);
			System.out.println(p.getProperty("使用燃焼データ年月XXXX/YY"));
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		System.out.println(p.getProperty("使用燃焼データ年月XXXX/YY"));

	}
		/*Sample sample = new Sample();
Method method = Sample.class.getDeclaredMethod("<メソッド名>", 引数の型1, 引数の型2...);
method.setAccessible(true);
int actual = (戻り値の型)method.invoke(<インスタンス>,引数1,引数2...);
*/

}
