package icgtest;

import static org.junit.Assert.*;

import java.util.HashMap;

import icg.PhysicalQuantity;
import icg.PhysicalQuantity.QuantityElements;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class PhysicalQuantityTest {

	@RunWith(Theories.class)
	public static class initializeTest{
		@DataPoints
		public static Object[][] param = {
			//正常な入力
			{"1 kg m", 1.0, 1, 1, 0, 0},
			{" 1 kg m2", 1.0, 2, 1, 0, 0},
			{"1", 1.0, 0, 0, 0, 0},
		};
		
		@Test
		@Theory
		public void testConstructor(Object[] p){
			PhysicalQuantity pq = new PhysicalQuantity((String)p[0]);
			HashMap<QuantityElements,Number> map = pq.getCharacterizeMap();
			assertEquals(map.get(QuantityElements.Number),p[1]);
			assertEquals(map.get(QuantityElements.m), p[2]);
			assertEquals(map.get(QuantityElements.kg), p[3]);
			assertEquals(map.get(QuantityElements.s), p[4]);
			assertEquals(map.get(QuantityElements.A), p[5]);
			
		}
		
	}
	
	@RunWith(Theories.class)
	public static class toStringTest{
		@DataPoints
		public static Object[][] param = {
			//正常な入力
			{"1 kg m", "1.0 kg m"}
		};
		
		@Test
		@Theory
		public void testToString(Object[] p){
			PhysicalQuantity pq = new PhysicalQuantity((String) p[0]);
			assertEquals(pq.toString(),p[1]);
		}
	}
	
	@RunWith(Theories.class)
	public static class compareTest{
		@DataPoints
		public static Object[][] param = {
			{new PhysicalQuantity("1 kg"), new PhysicalQuantity(" 5 kg"), false}, 
		};
		
		@Test
		@Theory
		public void testCompare(Object[] p){
			assertEquals(((PhysicalQuantity)p[0]).isLargerThan((PhysicalQuantity)p[1]), p[2]);
		}
	}
}
