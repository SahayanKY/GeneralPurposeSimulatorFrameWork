package icgtest;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import icg.UnitEditor;

@RunWith(Enclosed.class)
public class UnitEditorTest {

	@RunWith(Theories.class)
	public static class convertUnitTest{
		@DataPoints
		public static String[][] param = {
			//正常な入力
			{"1 kg", "g", "1000.0 g"}, //0
			{"1 km", "m", "1000.0 m"}, //1
			{"10 km/s", "hm/s", "100.0 hm/s"}, //2

			//変換前後一致型
			{"-1.05 kg", "kg", "-1.05 kg"}, //3
			{"5.0888 das", "das", "5.0888 das"}, //4
			{".5 Mg", "Mg", "0.5 Mg"}, //5

			//無次元量シリーズ
			{"1513","afefw", "1513"}, //6
			{"10.15", "afae", "10.15"}, //7
			{".15", "grge", ".15"}, //8
			{"  10.15   ", "a ea", "  10.15   "}, //9

			//空白多め
			{"  1 kg", "g", "1000.0 g"}, //10
			{"10  km /  s", "hm/s", "100.0 hm/s"}, //11

			//空白少なめ
			{"7kg", "g", null}, //12

			//負符号
			{"-1 m", "km", "-0.001 km"}, //13
			{"-1.05 m", "mm", "-1050.0 mm"}, //14

			//負の次元
			{"5 m-2", "/m2", "5.0 /m2"}, //15
			{"5 m-1 g", "g/m", "5.0 g/m"}, //16
			{"5 m-2 g", "g/m2", "5.0 g/m2"}, //17
			{"5 m s1 m2", "s mm3", "5.0E9 s mm3"}, //18

			//指数表記の入力
			{"5.145E5 mm ks-1", "m/s", "0.5145 m/s"}, //19
			{"-5.145E5 mm ks-1", "m s-1", "-0.5145 m s-1"}, //20
			//{"-5.145E-5 Mg", "g", "-51.45 g"},

			//計算後次元消失型
			{"5 kg-2 kg /kg-1 s", "/s", "5.0 /s"}, //21
			{"5 kg g g-2", "fae", "5000.0"}, //22

			//"/"多量
			{"0.1 kg/m/s2", "kg/m s2", null}, //23

			//指数部が十何乗
			//{"5.0E23 mm4/kg", "m4/g", "5.0E8 m4/g"},
			//{"5.0 m012/mm112 kg100" , "/m100 g100", "5.0E36 /m100 g100"}
		};

		@Test
		@Theory
		public void testConvert_from_toWithUnits(String[] s) {
			String result = UnitEditor.convert_from_toWithUnits(s[0], s[1]);
			assertEquals(result , s[2]);
		}
		
	}
	
	@RunWith(Theories.class)
	public static class compareValueTest{
		@DataPoints
		public static Object[][] param = {
			//正常な入力
			{"1 kg", "100 g", true}, //0
			{"1 km", "1 m", true}, //1
			{"10 m/ms", "1 km/s", true}, //2
			{"-1.05 kg", "0 kg", false}, //3
			{"-22 cm", "-10 /cm-1", false}, //4
			
			//変換前後一致型
			{"5.0888 das", "1.0 das", true}, //5
			{"-0.2 Mg-10 s2", ".5 s2 Mg-10 ", false}, //6
			
			//無次元量シリーズ
			{"1513 mm/m","10 m/m", false}, //7
			{"10.15", "10 m/m", true}, //8
			{".15 kg/g", "160", false}, //9
			{"  10.15  g/mg ", "10151", false}, //10
			{"15.4", "48", false}, //11
			
			//指数表記の入力
			{"5.145E5 mm ks-1", "0.52 /m-1 s", false}, //12
			{"-5.145E5 mm ks-1", "-0.1 m s-1", false}, //13
			{"-5.145E-5 Mg-1", "-5.0E-11 g-1", false}, //14
		};

		@Test
		@Theory
		public void testIsLargeA_thanB(Object[] s) {
			boolean result = UnitEditor.isLargerA_thanB((String)s[0], (String)s[1]);
			assertEquals(result , (boolean)s[2]);
		}
		
	}

}
