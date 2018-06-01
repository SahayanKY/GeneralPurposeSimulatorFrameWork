package icg;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class InputDataTest {

	@Test
	public void testFinNumber () {
		String inputString = "a32";
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
	public void testisThrustDataFile() {
		String path = System.getProperty("user.dir")+"\\bin\\icg\\data";
		try {
			InputData inputData = InputData.class.newInstance();
			Method testMethod = InputData.class.getDeclaredMethod("isThrustDataFile", String.class);
			testMethod.setAccessible(true);

			boolean f;
			f = (boolean) testMethod.invoke(inputData, path);

			if(f != true) {
				fail();
			}

		} catch (InstantiationException | IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		/*Sample sample = new Sample();
Method method = Sample.class.getDeclaredMethod("<メソッド名>", 引数の型1, 引数の型2...);
method.setAccessible(true);
int actual = (戻り値の型)method.invoke(<インスタンス>,引数1,引数2...);
*/
	}

}
