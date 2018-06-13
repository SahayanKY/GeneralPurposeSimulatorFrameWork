package icg;

import java.time.LocalDateTime;

import icg.frame.DataInputFrame;
import icg.frame.ProgressInformFrame;

public class ICG {
	private String calcResultDirectoryPath;
	private DataInputFrame datainputF;
	private LocalDateTime calculateStartTime;
	public static void main(String args[]) {
		ICG icg = new ICG();
		icg.start();
	}

	private void start() {
		calculateStartTime = LocalDateTime.now();
		datainputF = new DataInputFrame(this);
	}

	public void executeCalculation(InputData ID) {
		Calculater calc = new Calculater();
		ProgressInformFrame informF = new ProgressInformFrame();
		informF.makePanel();

		calc.execute(ID);

	}

	public void restart() {

	}
}

/*
 * try {
			BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir")+"\\bin\\icg\\data"));
			String[] sts = br.readLine().split("	");
			for(String s:sts) {
				System.out.println(Double.parseDouble(s));
			}
			br.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
 * */

/*
 *
 *画面クラス
 * データ入力画面クラス
 * +-データ保持クラス
 * 計算結果表示画面クラス
 *
 *計算実行クラス
 *
 *計算結果記録クラス
 *
 *
 *
 * */