package icgtest;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import icg.UnitEditor;

@RunWith(Theories.class)
public class UnitEditorTest {

	@DataPoints
	public static String[][] param = {
			//正常な入力
			{"1 kg", "g", "1000.0 g"},
			{"1 km", "m", "1000.0 m"},
			{"10 km/s", "hm/s", "100.0 hm/s"},
			{"10  km /  s", "hm/s", "100.0 hm/s"},

			//無次元量シリーズ
			{"1513","afefw", "1513"},
			{"10.15", "afae", "10.15"},
			{".15", "grge", ".15"},
			{"  10.15   ", "a ea", "  10.15   "},

			//空白多め
			{"  1 kg", "g", "1000.0 g"},

			//空白少なめ
			{"7kg", "g", null}
			};

	@Test
	@Theory
	public void testConvert_from_toWithUnits(String[] s) {
		String result = UnitEditor.convert_from_toWithUnits(s[0], s[1]);
		assertEquals(result , s[2]);
	}

}
