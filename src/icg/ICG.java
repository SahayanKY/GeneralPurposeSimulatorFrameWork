package icg;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import simulation.Simulater;
import simulation.param.Parameter;
import simulation.param.ParameterManager;
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
	protected void executeSimulation() {
		LinkedHashMap<String,LinkedHashMap<String,Parameter>> paramMap = (new ParameterManager(this)).getUserInputParamMap();
		double paramMap.get("")
		publish("時間,z,vz");
		double progressRate = 0;
		double m=0.5, t=0, z=0, g=-9.8, vz=50, step = 0.3;
		String format = "%f,%f,%f";
		publish(String.format(format, t,z,vz));
		System.out.println(progressRate);
		for(int i=0;updateProgress(progressRate) && z>=0;i++) {



/*			double vz2 = g*step + vz;
			double z2 = vz*step +z;
			double t2 = step*(i+1);
			publish(String.format(format, t2,z2,vz2));
			vz = vz2;
			z = z2;
			progressRate = Math.abs(g*step*(i+1)/(2*Math.sqrt(vz*vz-2*g*z)));
			System.out.println(progressRate);
*/
			try {
				Thread.sleep((long)(step*1000));
			}catch(InterruptedException e) {
			}
		}


	}

	@Override
	protected void process(List<String> list) {
		super.process(list);
	}



	@Override
	public void setSystemInputParameterValue(LinkedHashMap<String,LinkedHashMap<String,Parameter>> map) throws NullPointerException{
		BiFunction<String,String,Double> getValue = (parent,child) -> new PhysicalQuantity(map.get(parent).get(child).getValue()).Number;
		BiConsumer<String[],String> setValue = (label,value) -> map.get(label[0]).get(label[1]).setValue(value);

		String  一般="一般",
			ロケット全体 = "ロケット全体",
			ノーズコーン = "ノーズコーン",
			分離機構 = "分離機構",
			ボディーチューブ = "ボディーチューブ",
			エンジンを抑えるプレート = "エンジンを抑えるプレート",
			グレイン = "グレイン",
			インジェクターベル = "インジェクターベル",
			酸化剤タンク = "酸化剤タンク",
			フィン = "フィン",
			合成パラメータ = "合成パラメータ";//シミュレーション年月日時分秒

		setValue.accept(new String[] {一般,"日時"}, this.getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss.SSS")));
		//map.get(一般).get("日時").setValue(this.getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss.SSS")));


		double ρ = getValue.apply(一般,"空気密度"),
				Vmax = getValue.apply(一般,"最大飛行速度"),
				lsp = getValue.apply(一般,"比推力"),
				tb = getValue.apply(一般,"燃焼持続時間"),
				laucherL = getValue.apply(一般,"ランチャー長さ"),
				anemometerH = getValue.apply(一般,"風速計高さ"),

				rocketOuterDiameter = getValue.apply(ロケット全体,"外径"),
				rocketInnerDiameter = getValue.apply(ロケット全体,"内径"),
				rocketCP = getValue.apply(ロケット全体,"圧力中心位置"),
				rocketL = getValue.apply(ロケット全体,"全長"),
				rocketCD = getValue.apply(ロケット全体,"抗力係数"),

				noseL = getValue.apply(ノーズコーン,"長さ"),
				noseM = getValue.apply(ノーズコーン,"質量"),
				noseCG = getValue.apply(ノーズコーン,"重心位置"),
				noseCP = getValue.apply(ノーズコーン,"圧力中心位置"),
				noseCNα = getValue.apply(ノーズコーン,"法線力係数"),

				separaterL = getValue.apply(分離機構,"長さ"),
				separaterM = getValue.apply(分離機構,"質量"),
				separaterTop = getValue.apply(分離機構,"先端位置"),
				separaterOuterDiameter = getValue.apply(分離機構,"外径"),

				tubeL = getValue.apply(ボディーチューブ,"チューブ1長さ")
						+getValue.apply(ボディーチューブ,"チューブ2長さ")
						+getValue.apply(ボディーチューブ,"チューブ3長さ"),
				tube1M = getValue.apply(ボディーチューブ,"チューブ1質量"),
				tube2M = getValue.apply(ボディーチューブ,"チューブ2質量"),
				tube3M = getValue.apply(ボディーチューブ,"チューブ3質量"),
				tubeM = tube1M + tube2M + tube3M,
				tubeCG =(getValue.apply(ボディーチューブ,"チューブ1重心位置")*tube1M
						+getValue.apply(ボディーチューブ,"チューブ2重心位置")*tube2M
						+getValue.apply(ボディーチューブ,"チューブ3重心位置")*tube3M)
						/tubeM,

				aluminumPlateM = getValue.apply(エンジンを抑えるプレート,"質量"),
				aluminumPlateTop = getValue.apply(エンジンを抑えるプレート,"先端位置"),
				aluminumPlateL = getValue.apply(エンジンを抑えるプレート,"長さ"),

				grainBefM = getValue.apply(グレイン,"燃焼前質量"),
				grainAftM = getValue.apply(グレイン,"燃焼後質量"),
				grainTop = getValue.apply(グレイン,"先端位置"),
				grainL = getValue.apply(グレイン,"長さ"),
				grainOuterDiameter = getValue.apply(グレイン,"外径"),

				injectorM = getValue.apply(インジェクターベル,"質量"),
				injectorTop = getValue.apply(インジェクターベル,"先端位置"),
				injectorL = getValue.apply(インジェクターベル,"長さ"),
				injectorOuterDiameter = getValue.apply(インジェクターベル,"外径"),

				tankBefM = getValue.apply(酸化剤タンク,"燃焼前質量"),
				tankAftM = getValue.apply(酸化剤タンク,"燃焼後質量"),
				tankTop = getValue.apply(酸化剤タンク,"先端位置"),
				tankL = getValue.apply(酸化剤タンク,"長さ"),
				tankOuterDiameter = getValue.apply(酸化剤タンク,"外径"),

				finNumber = getValue.apply(フィン,"枚数"),
				finH = getValue.apply(フィン,"高さ"),
				finRootL = getValue.apply(フィン,"根本長さ"),
				finEdgeL = getValue.apply(フィン,"端部長さ"),
				finBackL = getValue.apply(フィン,"後退長さ"),
				finTop = getValue.apply(フィン, "先端位置"),
				finCG = getValue.apply(フィン, "重心位置"),
				finM = getValue.apply(フィン, "質量"),
				finCP = getValue.apply(フィン, "圧力中心位置"),
				finCNα = getValue.apply(フィン, "法線力係数");


		/*

		paramList.add(new Parameter(ロケット全体, "重心位置", "全体重心位置", "0 mm", "2000 mm", befAft));
		paramList.add(new Parameter(ロケット全体, "質量", "ロケット質量", "0 g", "5000 g", befAft));

		 *
		 *


		paramList.add(new Parameter(合成パラメータ, "干渉係数", "干渉係数Kfb[-]"));
		=IF(OR(フィン枚数=3,フィン枚数=4),1+(ロケット外径/2*0.5)/(フィン高さS+ロケット外径/2),1/0)

		paramList.add(new Parameter(合成パラメータ, "干渉込みフィン法線力係数", "干渉込みフィン法線力係数CNαfb[-]"));
		=フィン抗力係数(C_Nα)ｆ*干渉係数Kfb


		paramList.add(new Parameter(合成パラメータ, "Xn","Xn[mm]"));
		=0.466*ノーズコーン長さLn

		paramList.add(new Parameter(合成パラメータ, "合計法線力係数", "合計法線力係数CNα[-]"));
		=ノーズコーン抗力係数(C_Nα)n+N4

		paramList.add(new Parameter(合成パラメータ, "空力減衰モーメント", "空力減衰モーメント[kg m2/s]"));
		=空気密度ρ*(ロケット外径/2)^2*最大飛行速度V*(ノーズコーン抗力係数(C_Nα)n*(N5-全体重心位置G(乾燥時))^2+フィン抗力係数(C_Nα)ｆ*(フィン先端からの圧力中心位置-全体重心位置G(乾燥時))^2)*10^(-12)

		paramList.add(new Parameter(合成パラメータ, "ジェット減衰モーメント", "ジェット減衰モーメント[kg m2/s]"));
		=(グレイン質量(燃焼前)-グレイン質量(燃焼後))*(ロケット全長Lr-全体重心位置G(乾燥時))^2/燃焼持続時間tb*10^(-9)

		paramList.add(new Parameter(合成パラメータ, "減衰モーメント係数", "減衰モーメント係数[kg m2/s]"));
		=N7+N8

		paramList.add(new Parameter(合成パラメータ, "慣性モーメント(ピッチ・ヨー)", "慣性モーメント(ピッチ・ヨー)"));
		=ノーズ質量*(ノーズ先端からの重心位置-全体重心位置G(乾燥時))^2
=分離機構質量*((分離機構先端からの位置+分離機構長さ/2-全体重心位置G(乾燥時))^2+分離機構外径^2/16+分離機構長さ^2/12)
=N10*((N11-全体重心位置G(乾燥時))^2+(ロケット内径^2+ロケット外径^2)/16+N12^2/12)
=エンジンの上質量*((エンジンの上先端からの位置+エンジンの上長さ/2-全体重心位置G(乾燥時))^2+ロケット内径^2/16+エンジンの上長さ^2/12)
=グレイン質量(燃焼後)*((グレイン先端からの位置+グレイン長さ/2-全体重心位置G(乾燥時))^2+グレイン外径^2/16+グレイン長さ^2/12)
=いんじぇく質量*((いんじぇく先端からの位置+いんじぇく長さ/2-全体重心位置G(乾燥時))^2+いんじぇく外径^2/16+いんじぇく長さ^2/12)
=タンク質量(燃焼後)*((タンク先端からの位置+タンク長さ/2-全体重心位置G(乾燥時))^2+タンク外径^2/16+タンク長さ^2/12)
=フィン質量*(フィン先端からの重心位置-全体重心位置G(乾燥時))^2
=SUM(N14:N21)*10^(-9)

		paramList.add(new Parameter(合成パラメータ, "慣性モーメント(ロール)", "慣性モーメント(ロール)"));
		=ノーズ質量*ロケット外径^2/16
=分離機構質量*分離機構外径^2/4
=N10*(ロケット内径^2+ロケット外径^2)/4
=エンジンの上質量*ロケット内径^2/4
=(グレイン質量(燃焼後)+いんじぇく質量+タンク質量(燃焼後))*インじぇく外径^2/4
=フィン質量*((ロケット外径/2+フィン高さS)^3-ロケット外径^3/8)/フィン高さS
=SUM(N24:N29)*10^(-9)


		 * */
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
			フィン = "フィン",
			合成パラメータ = "合成パラメータ";

		paramList.add(new Parameter(一般, "日時", "シミュレーション年月日時分秒"));
		paramList.add(new Parameter(一般, "機体バージョン", "機体バージョン", null, null, new WhiteSpaceChecker()));
		paramList.add(new Parameter(一般, "燃焼データ燃月20XX/YY", "使用燃焼データ年月20XX/YY", null, null, new DateFormatChecker()));
		Parameter thrustFileParam = new Parameter(一般, "燃焼データファイル", "燃焼データファイル", null, null, new ThrustDataChecker());
		thrustFileParam.setNeedInputButtonParameter();
		paramList.add(thrustFileParam);
		paramList.add(new Parameter(一般, "空気密度", "空気密度", "0 kg/m3", "10 kg/m3", def));
		paramList.add(new Parameter(一般, "最大飛行速度", "最大飛行速度", "0 m/s", "200 m/s", def));
		paramList.add(new Parameter(一般, "比推力", "比推力", "0 s", "150 s", def));
		paramList.add(new Parameter(一般, "燃焼持続時間", "燃焼持続時間", "0 s", "100 s", def));
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

		paramList.add(new Parameter(合成パラメータ, "干渉係数", "干渉係数Kfb[-]"));
		paramList.add(new Parameter(合成パラメータ, "干渉込みフィン法線力係数", "干渉込みフィン法線力係数CNαfb[-]"));
		paramList.add(new Parameter(合成パラメータ, "Xn","Xn[mm]"));
		paramList.add(new Parameter(合成パラメータ, "合計法線力係数", "合計法線力係数CNα[-]"));
		paramList.add(new Parameter(合成パラメータ, "空力減衰モーメント", "空力減衰モーメント[kg m2/s]"));
		paramList.add(new Parameter(合成パラメータ, "ジェット減衰モーメント", "ジェット減衰モーメント[kg m2/s]"));
		paramList.add(new Parameter(合成パラメータ, "減衰モーメント係数", "減衰モーメント係数[kg m2/s]"));
		paramList.add(new Parameter(合成パラメータ, "慣性モーメント(ピッチ・ヨー)", "慣性モーメント(ピッチ・ヨー)[kg m2]"));
		paramList.add(new Parameter(合成パラメータ, "慣性モーメント(ロール)", "慣性モーメント(ロール)[kg m2]"));

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