package icg;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import icg.frame.DataInputFrame;
import simulation.Simulater;
import simulation.param.Parameter;
import simulation.param.checker.BeforeAfterParamChecker;
import simulation.param.checker.DateFormatChecker;
import simulation.param.checker.DefaultParameterChecker;
import simulation.param.checker.IntegerChecker;
import simulation.param.checker.ParameterChecker;
import simulation.param.checker.ThrustDataChecker;
import simulation.param.checker.WhiteSpaceChecker;

public class ICG extends Simulater{
	private ArrayList<Parameter> paramList = new ArrayList<>();

	public ICG() {
		//new DataInputFrame(this);
	}

	public static void main(String args[]) {
		ICG icg = new ICG();
		icg.createParameters();
		icg.openInputFrame();
	}


	@Override
	protected void process(List<Object> list) {
		// TODO 自動生成されたメソッド・スタブ

	}


	@Override
	public void setSystemInputParameterValue() {
		//シミュレーション年月日時分秒
		paramList.get(0).setValue(this.getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss.SSS")));
	}

	public void openInputFrame() {
		DataInputFrame inputFrame = new DataInputFrame(this);
	}

	@Override
	public ArrayList<Parameter> getParameterList(){
		return this.paramList;
	}

	@Override
	public void createParameters() {
		ParameterChecker def = new DefaultParameterChecker(), befAft = new BeforeAfterParamChecker();
		String
			一般 = "一般",
			ロケット全体 = "ロケット全体",
			ノーズコーン = "ノーズコーン",
			分離機構 = "分離機構",
			ボディーチューブ = "ボディーチューブ",
			エンジンを抑えるプレート = "エンジンを抑えるプレート",
			グレイン = "グレイン",
			インジェクターベル = "インジェクターベル",
			酸化剤タンク = "酸化剤タンク",
			フィン = "フィン";

		paramList.add(new Parameter(一般, "日時", "シミュレーション年月日時分秒"));
		paramList.add(new Parameter(一般, "機体aバージョン", "機体バージョン", null, null, new WhiteSpaceChecker()));
		paramList.add(new Parameter(一般, "燃焼データ燃月20XX/YY", "使用燃焼データ年月20XX/YY", null, null, new DateFormatChecker()));
		Parameter thrustFileParam = new Parameter(一般, "燃焼データファイル", "燃焼データファイル", null, null, new ThrustDataChecker());
		thrustFileParam.setNeedInputButtonParameter();
		paramList.add(thrustFileParam);
		paramList.add(new Parameter(一般, "空気密度", "空気密度", "0 kg/m3", "10 kg/m3", def));
		paramList.add(new Parameter(一般, "最大飛行速度", "最大飛行速度", "0 m/s", "200 m/s", def));
		paramList.add(new Parameter(一般, "比推力", "比推力", "0 s", "150 s", def));
		paramList.add(new Parameter(一般, "ランチャー長さ", "ランチャー長さ", "0 m", "10 m", def));
		paramList.add(new Parameter(一般, "風速計高さ", "風速計高さ", "0 m", "10 m", def));


		paramList.add(new Parameter(ロケット全体, "外径", "ロケット外径", "0 mm", "200 mm", def));
		paramList.add(new Parameter(ロケット全体, "内径", "ロケット内径", "0 mm", "200 mm", def));
		paramList.add(new Parameter(ロケット全体, "圧力中心位置", "全体圧力中心位置", "0 mm", "2000 mm", def));
		paramList.add(new Parameter(ロケット全体, "重心位置", "全体重心位置", "0 mm", "2000 mm", befAft));
		paramList.add(new Parameter(ロケット全体, "全長", "ロケット全長", "0 m", "2000 mm", def));
		paramList.add(new Parameter(ロケット全体, "質量", "ロケット質量", "0 g", "5000 g", befAft));
		paramList.add(new Parameter(ロケット全体, "抗力係数", "ロケット抗力係数", null, null, def));


		paramList.add(new Parameter(ノーズコーン, "長さ", "ノーズコーン長さ", "0 mm", "400 m", def));
		paramList.add(new Parameter(ノーズコーン, "質量", "ノーズコーン質量", "0 g", "200 g", def));
		paramList.add(new Parameter(ノーズコーン, "重心位置", "ノーズコーン重心位置", "0 mm", "200 mm", def));
		paramList.add(new Parameter(ノーズコーン, "圧力中心位置", "ノーズコーン圧力中心位置", "0 mm", "200 mm", def));
		paramList.add(new Parameter(ノーズコーン, "法線力係数", "ノーズコーン法線力係数", null, null, def));



		paramList.add(new Parameter(分離機構, "長さ", "分離機構長さ", "0 mm", "100 mm", def));
		paramList.add(new Parameter(分離機構, "質量", "分離機構質量", "0 g", "200 g", def));
		paramList.add(new Parameter(分離機構, "先端位置", "分離機構先端位置", "0 mm", "200 mm", def));
		paramList.add(new Parameter(分離機構, "外径", "分離機構外径", "0 mm", "110 mm", def));


		paramList.add(new Parameter(ボディーチューブ, "チューブ1長さ", "チューブ1長さ", "0 mm", "500 mm", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ2長さ", "チューブ2長さ", "0 mm", "500 mm", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ3長さ", "チューブ3長さ", "0 mm", "500 mm", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ1質量", "チューブ1質量", "0 g", "200 g", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ2質量", "チューブ2質量", "0 g", "200 g", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ3質量", "チューブ3質量", "0 g", "200 g", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ1重心位置", "チューブ1重心位置", "0 mm", "500 mm", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ2重心位置", "チューブ2重心位置", "0 mm", "500 mm", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ3重心位置", "チューブ3重心位置", "0 mm", "500 mm", def));


		paramList.add(new Parameter(エンジンを抑えるプレート, "質量", "エンジンを抑えるプレート質量", "0 g", "10 g", def));
		paramList.add(new Parameter(エンジンを抑えるプレート, "先端位置", "エンジンを抑えるプレート先端位置", "0 mm", "10 mm", def));
		paramList.add(new Parameter(エンジンを抑えるプレート, "長さ", "エンジンを抑えるプレート長さ", "0 mm", "10 mm", def));


		paramList.add(new Parameter(グレイン, "燃焼前質量", "燃焼前グレイン質量", "0 g", "10 g", def));
		paramList.add(new Parameter(グレイン, "燃焼後質量", "燃焼後グレイン質量", "0 g", "10 g", def));
		paramList.add(new Parameter(グレイン, "先端位置", "グレイン先端位置", "0 mm", "10 mm", def));
		paramList.add(new Parameter(グレイン, "長さ", "グレイン長さ", "0 mm", "10 mm", def));
		paramList.add(new Parameter(グレイン, "外径", "グレイン外径", "0 mm", "10 mm", def));


		paramList.add(new Parameter(インジェクターベル, "質量", "インジェクターベル質量", "0 g", "10 g", def));
		paramList.add(new Parameter(インジェクターベル, "先端位置", "インジェクターベル先端位置", "0 mm", "10 mm", def));
		paramList.add(new Parameter(インジェクターベル, "長さ", "インジェクターベル長さ", "0 mm", "10 mm", def));
		paramList.add(new Parameter(インジェクターベル, "外径", "インジェクターベル外径", "0 mm", "10 mm", def));


		paramList.add(new Parameter(酸化剤タンク, "燃焼前質量", "燃焼前酸化剤タンク質量", "0 g", "10 g", def));
		paramList.add(new Parameter(酸化剤タンク, "燃焼後質量", "燃焼後酸化剤タンク質量", "0 g", "10 g", def));
		paramList.add(new Parameter(酸化剤タンク, "先端位置", "酸化剤タンク先端位置", "0 mm", "10 mm", def));
		paramList.add(new Parameter(酸化剤タンク, "長さ", "酸化剤タンク長さ", "0 mm", "10 mm", def));
		paramList.add(new Parameter(酸化剤タンク, "外径", "酸化剤タンク外径", "0 mm", "10 mm", def));


		paramList.add(new Parameter(フィン, "枚数", "フィン枚数", null, null, new IntegerChecker(3,4)));
		paramList.add(new Parameter(フィン, "高さ", "フィン高さ", "0 mm", "10 mm", def));
		paramList.add(new Parameter(フィン, "根本長さ", "フィン根本長さ", "0 mm", "10 mm", def));
		paramList.add(new Parameter(フィン, "端部長さ", "フィン端部長さ", "0 mm", "10 mm", def));
		paramList.add(new Parameter(フィン, "後退長さ", "フィン後退長さ", "0 mm", "10 mm", def));
		paramList.add(new Parameter(フィン, "先端位置", "フィン先端位置", "0 mm", "10 mm", def));
		paramList.add(new Parameter(フィン, "重心位置", "フィン重心位置", "0 mm", "10 mm", def));
		paramList.add(new Parameter(フィン, "質量", "フィン質量", "0 g", "10 g", def));
		paramList.add(new Parameter(フィン, "圧力中心位置", "フィン圧力中心位置", "0 mm", "10 mm", def));
		paramList.add(new Parameter(フィン, "法線力係数", "フィン法線力係数", null, null, def));

		/*
		 * propertyLabelのかぶりがないかの確認テスト
		HashMap<String, Integer> map = new HashMap<>();
		for(int i=0;i< paramList.size();i++) {
			if(map.put(paramList.get(i).propertyLabel, i) != null) {
				Parameter p = paramList.get(i);
				System.out.println(p.parentLabel+":"+p.childLabel+":"+p.propertyLabel);
			}
		}*/


	}
}