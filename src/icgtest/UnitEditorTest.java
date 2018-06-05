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
			{"-5.145E-5 Mg", "g", "-51.45 g"}, //21

			//計算後次元消失型
			{"5 kg-2 kg /kg-1 s", "/s", "5.0 /s"}, //22
			{"5 kg g g-2", "fae", "5000.0"}, //23

	};

	@Test
	@Theory
	public void testConvert_from_toWithUnits(String[] s) {
		String result = UnitEditor.convert_from_toWithUnits(s[0], s[1]);
		assertEquals(result , s[2]);
	}

}
