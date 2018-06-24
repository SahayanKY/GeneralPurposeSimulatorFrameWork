package icgtest;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import icg.PhysicalQuantity;

@RunWith(Enclosed.class)
public class PhysicalQuantityTest {

	@RunWith(Theories.class)
	public static class initializeTest{
		@DataPoints
		public static Object[][] param = {
			//正常な入力
			{"1 kg m"},
			{" 1  kg m2"},
			{"1"},
			{"0.53 "},
			{"/m"},
			{"m /s"},
			{"kg"},
		};

		@Test
		@Theory
		public void testConstructor(Object[] p){
			PhysicalQuantity pq = new PhysicalQuantity((String)p[0]);
		}

	}

	@RunWith(Theories.class)
	public static class equalsTest{
		@DataPoints
		public static Object[][] param = {
			//正常な入力
			{"1 kg", "1000 g",true},
			{"1 s", "1000 ms",true},
			{"1 m", "1 m2", false},
			{"0.5 m/ms", "0.5 km/s", true},
			{"10 /km", "0.01 /m", true},
			{"13 mm3", "1.3E-8 m3", true},
			{"4 g km2/s ", "4 Mg m2 /s", true},
		};

		@Test
		@Theory
		public void testEquals(Object[] p) {
			PhysicalQuantity pq1 = new PhysicalQuantity((String)p[0]);
			PhysicalQuantity pq2 = new PhysicalQuantity((String)p[1]);

			assertEquals(pq1.equalsDimension(pq2), (boolean)p[2]);
			if((boolean)p[2]) {
				//比較する物理量の次元が等しい場合テストを続ける
				assertEquals(pq1.Number, pq2.Number, pq1.Number*Math.pow(10,-5));
			}
		}
	}

	@RunWith(Theories.class)
	public static class toStandardStringTest{
		@DataPoints
		public static Object[][] param = {
			//正常な入力
			{"1 kg m", "1.0 kg m"},
			{"0.5 g m", "5.0E-4 kg m"},
			//{"5 mm2", "5.0E-6 m2"},
		};

		@Test
		@Theory
		public void testToStandardString(Object[] p){
			PhysicalQuantity pq = new PhysicalQuantity((String) p[0]);
			assertEquals(pq.toString(),p[1]);
		}
	}



	@RunWith(Theories.class)
	public static class compareTest{
		@DataPoints
		public static Object[][] param = {
			{"1 kg", " 5 kg", false},
			{"0.5 g m", "0.001 kg m", false},

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
		public void testCompare(Object[] p){
			PhysicalQuantity pq1 = new PhysicalQuantity((String)p[0]),
					pq2 = new PhysicalQuantity((String)p[1]);
			assertEquals(pq1.isLargerThan(pq2), p[2]);
		}
	}
}
