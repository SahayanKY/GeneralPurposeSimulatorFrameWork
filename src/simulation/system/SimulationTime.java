package simulation.system;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * シミュレーションを開始した時間等についてのクラス
 *
 * スレッドセーフではない(未検証)
 * */
public class SimulationTime {
	private LocalDateTime simulationStartTime;
	private String pattern = "yyyy年MM月dd日HH時mm分ss.SSS秒";


	/**
	 * 日時の文字列パターンを指定します。
	 * これは LocalDateTime に従います。
	 * デフォルトは"yyyy年MM月dd日HH時mm分ss.SSS秒"です。
	 * @param pattern
	 * 文字列パターン
	 * */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * シミュレーションが開始してからの経過時間をミリ秒単位で返します。
	 * @return
	 * 経過時間[ms]
	 * */
	public long getElapsedTime() {
		long start = simulationStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

		return now-start;
	}

	/**
	 * 開始時刻の文字列表現を返します。
	 * @return
	 * 開始時刻の文字列表現
	 * */
	public String startTimetoString() {
		return simulationStartTime.format(DateTimeFormatter.ofPattern(pattern));
	}

}
