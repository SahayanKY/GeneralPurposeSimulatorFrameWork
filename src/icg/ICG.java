package icg;

import java.io.File;
import java.util.List;

import icg.frame.DataInputFrame;

public class ICG extends Simulater{


	public ICG(File resultSaveDirectory) {
		super(null, resultSaveDirectory);
	}

	Thread thread;
	//private String calcResultDirectoryPath;
	private DataInputFrame datainputF;
	//private LocalDateTime calculateStartTime;
	/*public static void main(String args[]) {
		ICG icg = new ICG();
		icg.simulationStart();
	}*/


	@Override
	protected void process(List<Object> list) {
		// TODO 自動生成されたメソッド・スタブ

	}
}