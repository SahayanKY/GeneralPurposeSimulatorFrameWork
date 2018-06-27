package icg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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
	private File thrustFile;
	private double
		ρ,
		Vmax,
		lsp,
		tb,
		launcherL,
		anemometerH,

		rocketOuterDiameter,
		rocketInnerDiameter,
		rocketCP,
		rocketL,
		rocketCD,

		noseL,
		noseM,
		noseCG,
		noseCP,
		noseCNα,

		separaterL,
		separaterM,
		separaterTop,
		separaterOuterDiameter,
		tubeL,
		tube1M,
		tube2M,
		tube3M,
		tubeM,
		tubeCG,

		aluminumPlateM,
		aluminumPlateTop,
		aluminumPlateL,

		grainBefM,
		grainAftM,
		grainContentsM,
		grainTop,
		grainL,
		grainCG,
		grainOuterDiameter,

		injectorM,
		injectorTop,
		injectorL,
		injectorOuterDiameter,

		tankBefM,
		tankAftM,
		tankContentsM,
		tankTop,
		tankL,
		tankCG,
		tankOuterDiameter,

		finNumber,
		finH,
		finRootL,
		finEdgeL,
		finBackL,
		finTop,
		finCG,
		finM,
		finCP,
		finCNα,

		rocketAftM,
		rocketAftCG,
		Kfb,
		finCNαb,
		CA,
		CR,
		rollI,
		pitchyawI;
	private Double launcherAzimuth;
	private Double fireAngle;
	private double rocketCNα;

	public ICG() {
		//new DataInputFrame(this);
	}

	public static void main(String args[]) {
		ICG icg = new ICG();
		icg.createParameters();
		icg.openInputFrame();
	}

	@Override
	protected void executeSimulation(LinkedHashMap<String,LinkedHashMap<String,Parameter>> map) {
		try(BufferedReader reader = new BufferedReader(new FileReader(thrustFile));) {
			publish("start");

			ArrayList<double[]> thrustList = new ArrayList<>();
			String st;
			for(int i=0;(st = reader.readLine()) != null;i++) {
				String[] dataSet = st.split(" +|	+|,{1}");
				thrustList.add(new double[] {Double.parseDouble(dataSet[0]),Double.parseDouble(dataSet[1])});
			}
			double[][] thrustArray = (double[][]) thrustList.toArray();
			thrustList = null;
			double progressRate = 0;
			double time=0,
					dt=0,
					g=9.8,
					N_grainContentsM = grainContentsM,
					N_tankContentsM = tankContentsM,
					N_RocketM = rocketAftM+N_grainContentsM+N_tankContentsM,
					N_RocketCG = (rocketAftM*rocketAftCG+grainContentsM*grainCG+tankContentsM*tankCG)/(rocketAftM+grainContentsM+tankContentsM),
					N_CD = rocketCD,
					atomosP = 1013, //hPa単位なので注意
					temperature = 20, //℃単位なので注意
					N_ρ = atomosP/(2.87*(temperature+273.15)),//H2=Q2/(2.87*(R2+273.15))
					crossA = rocketOuterDiameter*rocketOuterDiameter/4*Math.PI,
					anemometerV = 1,
					windSpeed = 0, //風速!$D$4*(Xn-1/風速!$D$5)^(1/風速!$B$8)
					windAngle = 0,
					attackAngle = 0,
					diffCGCP = rocketCP-N_RocketCG,
					Vx = 0,
					Vz = 0,
					XCG = 0,
					ZCG = 0,
					relativeVelocityToAir = 0,
					normalForce = 0,
					drag = 0,
					ω = 0,
					θ = Math.toRadians(fireAngle),
					staticMoment = 0,
					dampingMoment = 0,
					dampingMomentCoefficient = 0;
			publish("時間/s,推力/N,質量/kg,重心/m,抗力係数,空気密度/kg m-3,風速/m s-1,風方向角/rad,迎え角/rad,CP-CG/m,気圧/hPa,気温/℃,重心Vx/m s-1,重心Vz/m s-1,重心X,重心Z,対気流速度/m s-1,法線力/N,抗力/N,ω/rad s-1,θ/rad,");
			/*
			double m=0.5, t=0, z=0, g=-9.8, vz=50, step = 0.3;
			String format = "%f,%f,%f";
			publish(String.format(format, t,z,vz));
			System.out.println(progressRate);
			*/
			String format = "%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f";
			publish(String.format(format, time, thrustArray[0][1], N_RocketM, N_RocketCG, N_CD, N_ρ, windSpeed, windAngle, attackAngle, diffCGCP, atomosP, temperature, Vx, Vz, XCG, ZCG, relativeVelocityToAir, normalForce, drag, ω, θ));

			for(int j=0;updateProgress(progressRate) && ZCG>=0 ;j++) {
				double ω2,θ2,CD2,Vx2,Vz2,XCG2,ZCG2,thrust,Velocity;
				if(j < thrustArray.length) {
					if(thrustArray[j][1]<=0) {
						continue;
					}
					//推力が出ていた場合(Bn>0)
					N_grainContentsM = grainContentsM*(thrustArray[thrustArray.length-1][0]-thrustArray[j][0])/(thrustArray[thrustArray.length-1][0]-thrustArray[0][0]);
					N_tankContentsM = tankContentsM*(thrustArray[thrustArray.length-1][0]-thrustArray[j][0])/(thrustArray[thrustArray.length-1][0]-thrustArray[0][0]);
					N_RocketM = rocketAftM +N_grainContentsM +N_tankContentsM;
					N_RocketCG = (rocketAftM*rocketAftCG+N_grainContentsM*grainCG+N_tankContentsM*tankCG)/(rocketAftM+N_grainContentsM+N_tankContentsM);

					if(j<thrustArray.length-1) {
						dt = thrustArray[j+1][0] -thrustArray[j][0];
						thrust = thrustArray[j+1][1];
					}else {
						thrust = 0;
					}

				}else {
					thrust = 0;
				}
				ω2 = ω - (staticMoment +dampingMoment)/pitchyawI*dt;
				windAngle = Math.atan2(Vz, Vx+windSpeed);
				θ2 = θ + (ω+ω2)/2*dt;
				attackAngle = (ZCG < Math.sin(Math.toRadians(fireAngle))*launcherL && Vz >=0)? 0:θ2-windAngle;
				CD2 = rocketCD *((Math.abs(Math.toDegrees(attackAngle)) < 15)? (0.012*Math.pow(Math.toDegrees(attackAngle),2)+1):5);
				N_ρ = atomosP/(2.87*(temperature+273.15));
				drag = (ZCG < Math.sin(Math.toRadians(fireAngle)) && Vz >=0)? 0: CD2*N_ρ*crossA*relativeVelocityToAir*relativeVelocityToAir/2;
				diffCGCP = rocketCP -N_RocketCG;
				windSpeed = anemometerV*Math.pow(ZCG/anemometerH,1/6.0);
				Vx2 = Vx +(Math.cos(θ2)*thrust -Math.cos(windAngle)*drag)/N_RocketM *dt;
				Vz2 = Vz +((Math.sin(θ2)*thrust-Math.sin(windAngle)*drag)/N_RocketM -g) *dt;
				Velocity = Math.sqrt(Vx2*Vx2+Vz2*Vz2);
				relativeVelocityToAir = Math.sqrt(Math.pow(Vx2+windSpeed,2)+Vz2*Vz2);
				XCG2 = XCG +(Vx+Vx2)/2*dt;
				ZCG2 = ZCG +(Vz+Vz2)/2*dt;
				temperature = 20 -0.0065*ZCG2;
				atomosP = 1013 *Math.pow((1-(0.0065*ZCG2)/(20+273.15)),0.5257);
				normalForce = (ZCG2<Math.sin(Math.toRadians(fireAngle)) && Vz2>0)? 0:rocketCNα *N_ρ *relativeVelocityToAir*relativeVelocityToAir *attackAngle *crossA/2;
				staticMoment = normalForce *diffCGCP;
				dampingMoment = N_ρ *crossA *Velocity/2 *(noseCNα*Math.pow(noseCP -rocketAftCG,2) +finCNαb *Math.pow(finCP -rocketAftCG,2))*ω;
				//readedByte += st.getBytes("UTF-8").length;

				//progressRate = (double)readedByte/size;

/*
			double vz2 = g*step + vz;
			double z2 = vz*step +z;
			double t2 = step*(i+1);
			publish(String.format(format, t2,z2,vz2));
			vz = vz2;
			z = z2;
			progressRate = Math.abs(g*step*(i+1)/(2*Math.sqrt(vz*vz-2*g*z)));
			System.out.println(progressRate);
*/
				time += dt;
				ω = ω2;
				publish(String.format(format, time, thrustArray[0][1], N_RocketM, N_RocketCG, N_CD, N_ρ, windSpeed, windAngle, attackAngle, diffCGCP, atomosP, temperature, Vx, Vz, XCG, ZCG, relativeVelocityToAir, normalForce, drag, ω, θ));


				try {
					Thread.sleep(100);
				}catch(InterruptedException e) {
				}

			}
		} catch (IOException e) {
		}

	}

	@Override
	protected void process(List<String> list) {
		for(String strline:list) {
			if(strline.equals("restart") || strline.equals("start")) {
				for(int i=0;i<5;i++) {
					if(strline.equals("restart")) {
						this.setSimulationStartTime();
					}
					File storeFile = new File(resultStoreDirectory.toString()+"\\"+getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日HH時mm分ss.SSS秒"))+"result.csv");
					if(storeFile.exists()) {
						//同名のファイルが存在する場合
						continue;
					}
					try {
						if(this.resultWriter != null) {
							this.resultWriter.close();
						}
						this.resultWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(storeFile),"UTF-8"));
						break;
					} catch (IOException e) {
					}
					if(i==4) {
						throw new Error("保存ファイル作成に5回失敗しました。");
					}
				}
				continue;
			}
			try {
				this.resultWriter.write(strline);
				this.resultWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	@Override
	public void calculateAndSetParameterValue(LinkedHashMap<String,LinkedHashMap<String,Parameter>> map) throws NullPointerException,IllegalArgumentException{
		BiFunction<String,String,Double> getValue = (parent,child) -> new PhysicalQuantity(map.get(parent).get(child).getValue()).Number;
		BiConsumer<String[],String> setStringValue = (label,value) -> map.get(label[0]).get(label[1]).setValue(value);


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

		setStringValue.accept(new String[] {一般,"日時"}, this.getSimulationStartTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss.SSS")));

		thrustFile = new File(map.get(一般).get("燃焼データファイル").getValue());

		ρ = getValue.apply(一般,"空気密度");
		Vmax = getValue.apply(一般,"最大飛行速度");
		lsp = getValue.apply(一般,"比推力");
		tb = getValue.apply(一般,"燃焼持続時間");
		launcherL = getValue.apply(一般,"ランチャー長さ");
		launcherAzimuth = getValue.apply(一般, "打上げ方位角[°]");
		fireAngle = getValue.apply(一般, "射角[°]");
		anemometerH = getValue.apply(一般,"風速計高さ");

		rocketOuterDiameter = getValue.apply(ロケット全体,"外径");
		rocketInnerDiameter = getValue.apply(ロケット全体,"内径");
		rocketCP = getValue.apply(ロケット全体,"圧力中心位置");
		rocketL = getValue.apply(ロケット全体,"全長");
		rocketCD = getValue.apply(ロケット全体,"抗力係数");

		noseL = getValue.apply(ノーズコーン,"長さ");
		noseM = getValue.apply(ノーズコーン,"質量");
		noseCG = getValue.apply(ノーズコーン,"重心位置");
		noseCP = getValue.apply(ノーズコーン,"圧力中心位置");
		noseCNα = getValue.apply(ノーズコーン,"法線力係数");

		separaterL = getValue.apply(分離機構,"長さ");
		separaterM = getValue.apply(分離機構,"質量");
		separaterTop = getValue.apply(分離機構,"先端位置");
		separaterOuterDiameter = getValue.apply(分離機構,"外径");

		tubeL = getValue.apply(ボディーチューブ,"チューブ1長さ")
				+getValue.apply(ボディーチューブ,"チューブ2長さ")
				+getValue.apply(ボディーチューブ,"チューブ3長さ");
		tube1M = getValue.apply(ボディーチューブ,"チューブ1質量");
		tube2M = getValue.apply(ボディーチューブ,"チューブ2質量");
		tube3M = getValue.apply(ボディーチューブ,"チューブ3質量");
		tubeM = tube1M + tube2M + tube3M;
		tubeCG =(getValue.apply(ボディーチューブ,"チューブ1重心位置")*tube1M
				+getValue.apply(ボディーチューブ,"チューブ2重心位置")*tube2M
				+getValue.apply(ボディーチューブ,"チューブ3重心位置")*tube3M)
				/tubeM;

		aluminumPlateM = getValue.apply(エンジンを抑えるプレート,"質量");
		aluminumPlateTop = getValue.apply(エンジンを抑えるプレート,"先端位置");
		aluminumPlateL = getValue.apply(エンジンを抑えるプレート,"長さ");

		grainBefM = getValue.apply(グレイン,"燃焼前質量");
		grainAftM = getValue.apply(グレイン,"燃焼後質量");
		grainContentsM = grainBefM -grainAftM;
		grainTop = getValue.apply(グレイン,"先端位置");
		grainL = getValue.apply(グレイン,"長さ");
		grainCG = grainTop +grainL/2;
		grainOuterDiameter = getValue.apply(グレイン,"外径");

		injectorM = getValue.apply(インジェクターベル,"質量");
		injectorTop = getValue.apply(インジェクターベル,"先端位置");
		injectorL = getValue.apply(インジェクターベル,"長さ");
		injectorOuterDiameter = getValue.apply(インジェクターベル,"外径");

		tankBefM = getValue.apply(酸化剤タンク,"燃焼前質量");
		tankAftM = getValue.apply(酸化剤タンク,"燃焼後質量");
		tankContentsM = tankBefM -tankAftM;
		tankTop = getValue.apply(酸化剤タンク,"先端位置");
		tankL = getValue.apply(酸化剤タンク,"長さ");
		tankCG = tankTop +tankL/2;
		tankOuterDiameter = getValue.apply(酸化剤タンク,"外径");

		finNumber = getValue.apply(フィン,"枚数");
		finH = getValue.apply(フィン,"高さ");
		finRootL = getValue.apply(フィン,"根本長さ");
		finEdgeL = getValue.apply(フィン,"端部長さ");
		finBackL = getValue.apply(フィン,"後退長さ");
		finTop = getValue.apply(フィン, "先端位置");
		finCG = getValue.apply(フィン, "重心位置");
		finM = getValue.apply(フィン, "質量");
		finCP = getValue.apply(フィン, "圧力中心位置");
		finCNα = getValue.apply(フィン, "法線力係数");

		String st;
		st = map.get(ロケット全体).get("質量").getValue();
		rocketAftM = new PhysicalQuantity(st.substring(0,st.length()-1)).Number;
		if(st.endsWith("b")) {
			//燃焼後(乾燥時)の質量に直す
			rocketAftM -= (tankContentsM +grainContentsM);
		}

		st = map.get(ロケット全体).get("重心位置").getValue();
		rocketAftCG = new PhysicalQuantity(st.substring(0,st.length()-1)).Number;
		if(st.endsWith("b")) {
			//同様
			//右辺のrocketAftCGは燃焼前重心
			rocketAftCG = (1+(grainContentsM+tankContentsM)/rocketAftM)*rocketAftCG -(grainContentsM*grainCG+tankContentsM*tankCG)/rocketAftM;
		}

		Kfb = 1+rocketOuterDiameter/(2*finH+rocketOuterDiameter);
		finCNαb = Kfb*finCNα;
		rocketCNα = noseCNα +finCNαb;
		setStringValue.accept(new String[] {合成パラメータ,"干渉係数"}, String.valueOf(Kfb));
		setStringValue.accept(new String[] {合成パラメータ,"干渉込みフィン法線力係数"}, String.valueOf(finCNαb));
		setStringValue.accept(new String[] {合成パラメータ,"合計法線力係数"}, String.valueOf(rocketCNα));

		CA = ρ*Vmax*rocketOuterDiameter*rocketOuterDiameter/4*(noseCNα*Math.pow(noseCP-rocketAftCG,2)+finCNαb*Math.pow(finCP-rocketAftCG,2));
		CR = grainContentsM*Math.pow(rocketL-rocketAftCG,2)/tb;

		setStringValue.accept(new String[] {合成パラメータ,"空力減衰モーメント"}, String.valueOf(CA)+" kg m2/s");
	//一時的に機体の重心を燃焼後(乾燥)で出している
	//ジェット減衰モーメントは燃焼前の状態で出しているのだが
	//後で要確認
		setStringValue.accept(new String[] {合成パラメータ,"ジェット減衰モーメント"}, String.valueOf(CR)+" kg m2/s");
		setStringValue.accept(new String[] {合成パラメータ,"減衰モーメント係数"}, String.valueOf(CA+CR)+" kg m2/s");
		setStringValue.accept(new String[] {合成パラメータ,"復元モーメント係数"}, String.valueOf(ρ*(noseCNα+finCNαb)*Math.pow(rocketOuterDiameter/2*Vmax,2)*(rocketCP-rocketAftCG))+" kg m2/s2");

		pitchyawI
			=noseM*Math.pow(noseCG-rocketAftCG,2)
			+separaterM*(Math.pow(separaterTop+separaterL/2-rocketAftCG,2)+separaterOuterDiameter*separaterOuterDiameter/16+separaterL*separaterL/12)
			+tubeM*(Math.pow(tubeCG-rocketAftCG,2)+(rocketInnerDiameter*rocketInnerDiameter+rocketOuterDiameter*rocketOuterDiameter)/16+tubeL*tubeL/12)
			+aluminumPlateM*(Math.pow(aluminumPlateTop+aluminumPlateL/2-rocketAftCG,2)+rocketInnerDiameter*rocketInnerDiameter/16+aluminumPlateL*aluminumPlateL/12)
			+grainAftM*(Math.pow(grainTop+grainL/2-rocketAftCG,2)+grainOuterDiameter*grainOuterDiameter/16+grainL*grainL/12)
			+injectorM*(Math.pow(injectorTop+injectorL/2-rocketAftCG,2) +injectorOuterDiameter*injectorOuterDiameter/16 +injectorL*injectorL/12)
			+tankAftM*(Math.pow(tankTop+tankL/2-rocketAftCG,2)+tankOuterDiameter*tankOuterDiameter/16+tankL*tankL/12)
			+finM*Math.pow(finCG-rocketAftCG,2);

		rollI
			=noseM*rocketOuterDiameter*rocketOuterDiameter/16
			+separaterM*separaterOuterDiameter*separaterOuterDiameter/4
			+tubeM*(rocketInnerDiameter*rocketInnerDiameter+rocketOuterDiameter*rocketOuterDiameter)/4
			+aluminumPlateM*rocketInnerDiameter*rocketInnerDiameter/4
			+(grainAftM+injectorM+tankAftM)*injectorOuterDiameter*injectorOuterDiameter/4
			+finM*(Math.pow(rocketOuterDiameter/2+finH,3)-Math.pow(rocketOuterDiameter, 3)/8)/finH;
		setStringValue.accept(new String[] {合成パラメータ,"慣性モーメント(ピッチ・ヨー)"}, String.valueOf(pitchyawI));
		setStringValue.accept(new String[] {合成パラメータ,"慣性モーメント(ロール)"}, String.valueOf(rollI));
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
		paramList.add(new Parameter(一般, "燃焼データ年月20XX/YY", "使用燃焼データ年月20XX/YY", null, null, new DateFormatChecker()));
		Parameter thrustFileParam = new Parameter(一般, "燃焼データファイル", "燃焼データファイル", null, null, new ThrustDataChecker());
		thrustFileParam.setNeedInputButtonParameter();
		paramList.add(thrustFileParam);
		paramList.add(new Parameter(一般, "空気密度", "空気密度", "0.5 kg/m3", "5 kg/m3", def));
		paramList.add(new Parameter(一般, "最大飛行速度", "最大飛行速度", "20 m/s", "200 m/s", def));
		paramList.add(new Parameter(一般, "比推力", "比推力", "50 s", "500 s", def));
		paramList.add(new Parameter(一般, "燃焼持続時間", "燃焼持続時間", "2 s", "30 s", def));
		paramList.add(new Parameter(一般, "ランチャー長さ", "ランチャー長さ", "3 m", "10 m", def));
		paramList.add(new Parameter(一般, "打上げ方位角[°]", "磁東からの打上げ方位角[°]", "0", "359", def));
		paramList.add(new Parameter(一般, "射角[°]", "射角[°]", "50", "90", def));
		paramList.add(new Parameter(一般, "風速計高さ", "風速計高さ", "0 m", "15 m", def));


		paramList.add(new Parameter(ロケット全体, "外径", "ロケット外径", "70 mm", "170 mm", def));
		paramList.add(new Parameter(ロケット全体, "内径", "ロケット内径", "70 mm", "170 mm", def));
		paramList.add(new Parameter(ロケット全体, "圧力中心位置", "全体圧力中心位置", "1000 mm", "2500 mm", def));
		paramList.add(new Parameter(ロケット全体, "重心位置", "全体重心位置", "700 mm", "1500 mm", befAft));
		paramList.add(new Parameter(ロケット全体, "全長", "ロケット全長", "1200 mm", "2500 mm", def));
		paramList.add(new Parameter(ロケット全体, "質量", "ロケット質量", "4 kg", "10 kg", befAft));
		paramList.add(new Parameter(ロケット全体, "抗力係数", "ロケット抗力係数CD", null, null, def));


		paramList.add(new Parameter(ノーズコーン, "長さ", "ノーズコーン長さ", "100 mm", "500 mm", def));
		paramList.add(new Parameter(ノーズコーン, "質量", "ノーズコーン質量", "100 g", "400 g", def));
		paramList.add(new Parameter(ノーズコーン, "重心位置", "ノーズコーン重心位置", "10 mm", "500 mm", def));
		paramList.add(new Parameter(ノーズコーン, "圧力中心位置", "ノーズコーン圧力中心位置", "10 mm", "500 mm", def));
		paramList.add(new Parameter(ノーズコーン, "法線力係数", "ノーズコーン法線力係数CNαn", null, null, def));



		paramList.add(new Parameter(分離機構, "長さ", "分離機構長さ", "5 mm", "300 mm", def));
		paramList.add(new Parameter(分離機構, "質量", "分離機構質量", "10 g", "500 g", def));
		paramList.add(new Parameter(分離機構, "先端位置", "分離機構先端位置", "100 mm", "700 mm", def));
		paramList.add(new Parameter(分離機構, "外径", "分離機構外径", "5 mm", "200 mm", def));


		paramList.add(new Parameter(ボディーチューブ, "チューブ1長さ", "チューブ1長さ", "100 mm", "1000 mm", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ2長さ", "チューブ2長さ", "100 mm", "1000 mm", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ3長さ", "チューブ3長さ", "100 mm", "1000 mm", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ1質量", "チューブ1質量", "60 g", "600 g", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ2質量", "チューブ2質量", "60 g", "600 g", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ3質量", "チューブ3質量", "60 g", "600 g", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ1重心位置", "チューブ1重心位置", "0 mm", "1000 mm", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ2重心位置", "チューブ2重心位置", "500 mm", "1500 mm", def));
		paramList.add(new Parameter(ボディーチューブ, "チューブ3重心位置", "チューブ3重心位置", "1000 mm", "2500 mm", def));


		paramList.add(new Parameter(エンジンを抑えるプレート, "質量", "エンジンを抑えるプレート質量", "10 g", "500 g", def));
		paramList.add(new Parameter(エンジンを抑えるプレート, "先端位置", "エンジンを抑えるプレート先端位置", "500 mm", "11800 mm", def));
		paramList.add(new Parameter(エンジンを抑えるプレート, "長さ", "エンジンを抑えるプレート長さ", "0 mm", "10 mm", def));


		paramList.add(new Parameter(グレイン, "燃焼前質量", "燃焼前グレイン質量", "100 g", "500 g", def));
		paramList.add(new Parameter(グレイン, "燃焼後質量", "燃焼後グレイン質量", "100 g", "500 g", def));
		paramList.add(new Parameter(グレイン, "先端位置", "グレイン先端位置", "500 mm", "2000 mm", def));
		paramList.add(new Parameter(グレイン, "長さ", "グレイン長さ", "50 mm", "500 mm", def));
		paramList.add(new Parameter(グレイン, "外径", "グレイン外径", "5 mm", "200 mm", def));


		paramList.add(new Parameter(インジェクターベル, "質量", "インジェクターベル質量", "30 g", "500 g", def));
		paramList.add(new Parameter(インジェクターベル, "先端位置", "インジェクターベル先端位置", "500 mm", "2000 mm", def));
		paramList.add(new Parameter(インジェクターベル, "長さ", "インジェクターベル長さ", "10 mm", "100 mm", def));
		paramList.add(new Parameter(インジェクターベル, "外径", "インジェクターベル外径", "5 mm", "200 mm", def));


		paramList.add(new Parameter(酸化剤タンク, "燃焼前質量", "燃焼前酸化剤タンク質量", "200 g", "5000 g", def));
		paramList.add(new Parameter(酸化剤タンク, "燃焼後質量", "燃焼後酸化剤タンク質量", "100 g", "2500 g", def));
		paramList.add(new Parameter(酸化剤タンク, "先端位置", "酸化剤タンク先端位置", "500 mm", "2000 mm", def));
		paramList.add(new Parameter(酸化剤タンク, "長さ", "酸化剤タンク長さ", "100 mm", "1000 mm", def));
		paramList.add(new Parameter(酸化剤タンク, "外径", "酸化剤タンク外径", "20 mm", "200 mm", def));


		paramList.add(new Parameter(フィン, "枚数", "フィン枚数", null, null, new IntegerChecker(3,4)));
		paramList.add(new Parameter(フィン, "高さ", "フィン高さ", "10 mm", "200 mm", def));
		paramList.add(new Parameter(フィン, "根本長さ", "フィン根本長さ", "10 mm", "400 mm", def));
		paramList.add(new Parameter(フィン, "端部長さ", "フィン端部長さ", "10 mm", "400 mm", def));
		paramList.add(new Parameter(フィン, "後退長さ", "フィン後退長さ", "10 mm", "200 mm", def));
		paramList.add(new Parameter(フィン, "先端位置", "フィン先端位置", "500 mm", "2000 mm", def));
		paramList.add(new Parameter(フィン, "重心位置", "フィン重心位置", "500 mm", "2500 mm", def));
		paramList.add(new Parameter(フィン, "質量", "フィン質量", "10 g", "1000 g", def));
		paramList.add(new Parameter(フィン, "圧力中心位置", "フィン圧力中心位置", "500 mm", "2500 mm", def));
		paramList.add(new Parameter(フィン, "法線力係数", "フィン法線力係数CNαf", null, null, def));

		paramList.add(new Parameter(合成パラメータ, "干渉係数", "干渉係数Kfb"));
		paramList.add(new Parameter(合成パラメータ, "干渉込みフィン法線力係数", "干渉込みフィン法線力係数CNαfb"));
		paramList.add(new Parameter(合成パラメータ, "合計法線力係数", "合計法線力係数CNα"));
		paramList.add(new Parameter(合成パラメータ, "復元モーメント係数", "復元モーメント係数C1"));
		paramList.add(new Parameter(合成パラメータ, "空力減衰モーメント", "空力減衰モーメントCA(燃焼前)"));
		paramList.add(new Parameter(合成パラメータ, "ジェット減衰モーメント", "ジェット減衰モーメントCR(燃焼前)"));
		paramList.add(new Parameter(合成パラメータ, "減衰モーメント係数", "減衰モーメント係数C2(燃焼前)"));
		paramList.add(new Parameter(合成パラメータ, "慣性モーメント(ピッチ・ヨー)", "慣性モーメント(ピッチ・ヨー)"));
		paramList.add(new Parameter(合成パラメータ, "慣性モーメント(ロール)", "慣性モーメント(ロール)"));

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