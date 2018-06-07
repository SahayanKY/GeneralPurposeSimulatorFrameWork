package icgtest;

import icg.frame.DataInputFrame;

public class DataInputFrameTest {
	public static void main(String args[]) {
		System.setProperty("file.encoding", "UTF-8");
		new DataInputFrame();
		//new DataInputFrameTest().test();
		//new TestDataInputFrame().testB();あああ
	}

	private void test() {
		String st = "ab";
		String s[] = st.split("b");
		System.out.println(s.length);
	}

}
