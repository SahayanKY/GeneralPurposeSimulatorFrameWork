package icg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

import simulation.Simulator;
import simulation.param.Parameter;
import simulation.param.checker.BeforeAfterParamChecker;
import simulation.param.checker.DateFormatChecker;
import simulation.param.checker.DefaultParameterChecker;
import simulation.param.checker.IntegerChecker;
import simulation.param.checker.ParameterChecker;
import simulation.param.checker.ThrustDataChecker;
import simulation.param.checker.WhiteSpaceChecker;

public class ICG extends Simulator{
	private ArrayList<Parameter> paramList = new ArrayList<>();
	private File thrustFile;
	private double
		ρ,
		Vmax,
		lsp,
		tb,
		launcherL,
		launcherAzimuth,
		anemometerH,
		fireAngle,


		rocketOuterDiameter,
		rocketInnerDiameter,
		rocketCP,
		rocketL,
		rocketCD,
		rocketCNα,
		launchLagSecondLastCG,
		launchLagLastCG,

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
		grainContentsBefM,
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
		tankContentsBefM,
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
		BefRollI,
		BefPitchyawI;

	public static void main(String args[]) {
		ICG icg = new ICG();
		icg.createParameters();
		icg.openDataInputFrame(340,450);
	}

	/*
	 * ICGのシミュレーション本体の実装部分。このメソッド内でループ処理などを行う。
	 * */
	@Override
	protected void executeSimulation() {
		try(BufferedReader reader = new BufferedReader(new FileReader(thrustFile));) {
			publish("start");
			ArrayList<double[]> thrustList = new ArrayList<>();
			String st;
			for(int i=0;(st = reader.readLine()) != null;i++) {
				String[] dataSet = st.split(" +|	+|,{1}");
				thrustList.add(new double[] {Double.parseDouble(dataSet[0]),Double.parseDouble(dataSet[1])});
			}
			int thrustListSize = thrustList.size();
			double progressRate = 0;
			double time=0,
					dt=0,
					g=9.8,
					lastLagX=rocketL-launchLagLastCG,//ランチャー方向の座標における最後端のラグの位置
					lastLagV=0,//ランチャー方向の座標における最後端ラグの速度
					grainContentsM = grainContentsBefM,
					tankContentsM = tankContentsBefM,
					rocketM = rocketAftM+grainContentsM+tankContentsM,
					rocketBefCG = (rocketAftM*rocketAftCG+grainContentsBefM*grainCG+tankContentsBefM*tankCG)/(rocketAftM+grainContentsBefM+tankContentsBefM),
					rocketCG = rocketBefCG,
					Δξ = 0,
					pitchYawI = BefPitchyawI,
					CD = rocketCD,
					atomosP = 1013, //hPa単位なので注意
					temperature = 20, //℃単位なので注意
					N_ρ = atomosP/(2.87*(temperature+273.15)),//H2=Q2/(2.87*(R2+273.15))
					crossA = rocketOuterDiameter*rocketOuterDiameter/4*Math.PI,
					anemometerV = 7, //x軸正の向きが正
					windVelocity = 0, 
					windAngle = 0,
					attackAngle = 0,
					diffCGCP = rocketCP-rocketCG,
					ω = 0,
					θ = Math.toRadians(fireAngle),
					Vx = 0,
					Vz = 0,
					Velocity = 0,
					//修正後
					//XCG = rocketOuterDiameter/2*Math.sin(θ)+(rocketL-rocketBefCG)*Math.cos(θ),
					//修正前
					XCG = 0,
					//
					//修正後
					//ZCG = -rocketOuterDiameter/2*Math.cos(θ)+(rocketL-rocketBefCG)*Math.sin(θ),
					//修正前
					ZCG = 0,
					//
					relativeVelocityToAir = 0,
					normalForce = 0,
					drag = 0,
					staticMoment = 0,
					dampingMoment = 0,
					dampingMomentCoefficient = 0;
			boolean secondLastLagCleared = false,lastLagCleared = false;
			publish("時間/s,推力/N,質量/kg,重心/m,抗力係数,空気密度/kg m-3,風速/m s-1,風方向角/rad,迎え角/rad,CP-CG/m,気圧/hPa,気温/℃,重心Vx/m s-1,重心Vz/m s-1,重心X,重心Z,対気流速度/m s-1,法線力/N,抗力/N,ω/rad s-1,θ/rad,ランチクリア");

			String format = "%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%b";
			publish(String.format(format, time, thrustList.get(0)[1], rocketM, rocketCG, CD, N_ρ, windVelocity, windAngle, attackAngle, diffCGCP, atomosP, temperature, Vx, Vz, XCG, ZCG, relativeVelocityToAir, normalForce, drag, ω, θ,false));

			for(int j=0;updateProgress(0.5) && ZCG>=0 ;j++) {
				double ω2,θ2,Vx2,Vz2,XCG2,ZCG2,thrust,forceX,forceZ,torqueY,lastLagX2,rocketM2=rocketM,rocketCG2=rocketCG,grainContentsM2=grainContentsM,tankContentsM2=tankContentsM;
				if(j < thrustListSize) {
					if(thrustList.get(j)[1]<=0) {
						continue;
					}
					//推力が出ていた場合(Bn>0)
					grainContentsM2 = grainContentsBefM*(thrustList.get(thrustListSize-1)[0]-thrustList.get(j)[0])/(thrustList.get(thrustListSize-1)[0]-thrustList.get(0)[0]);
					tankContentsM2 = tankContentsBefM*(thrustList.get(thrustListSize-1)[0]-thrustList.get(j)[0])/(thrustList.get(thrustListSize-1)[0]-thrustList.get(0)[0]);
					rocketM2 = rocketAftM +grainContentsM2 +tankContentsM2;
					rocketCG2 = (rocketAftM*rocketAftCG+grainContentsM2*grainCG+tankContentsM2*tankCG)/(rocketAftM+grainContentsM2+tankContentsM2);

					if(j<thrustListSize-1) {
						dt = thrustList.get(j+1)[0] -thrustList.get(j)[0];
						thrust = thrustList.get(j+1)[1];
					}else {
						thrust = 0;
					}

				}else {
					thrust = 0;
				}

				//修正後ランチクリア判定
				/*
				if(lastLagX>=launcherL) {
				*/
				//修正前ランチクリア判定
				if(XCG > launcherL*Math.cos(θ)){
					//一番最後のランチラグがクリアしたとき
					lastLagCleared = true;
					secondLastLagCleared = true;
				}else {
					lastLagCleared = false;
					if(lastLagX+launchLagLastCG-launchLagSecondLastCG >= launcherL) {
						//最後から2番目のラグがクリアしたとき
						secondLastLagCleared = true;
					}else {
						secondLastLagCleared = false;
					}
				}



				temperature = 20 -0.0065*ZCG;
				atomosP = 1013 *Math.pow((1-(0.0065*ZCG)/(20+273.15)),0.5257);
				N_ρ = atomosP/(2.87*(temperature+273.15));

				diffCGCP = rocketCP -rocketCG2;
				windVelocity = anemometerV*Math.pow(ZCG/anemometerH,1/6.0);
				//windVelocity<0のとき向かい風

				CD = rocketCD *((Math.abs(Math.toDegrees(attackAngle)) < 15)? (0.012*Math.pow(Math.toDegrees(attackAngle),2)+1):5);
				drag = CD*N_ρ*crossA*relativeVelocityToAir*relativeVelocityToAir/2;
				normalForce = rocketCNα *N_ρ *relativeVelocityToAir*relativeVelocityToAir *attackAngle *crossA/2;
				staticMoment = normalForce *diffCGCP;
				dampingMoment = N_ρ *crossA *Velocity/2 *(noseCNα*Math.pow(noseCP -rocketAftCG,2) +finCNαb *Math.pow(finCP -rocketAftCG,2))*ω;
				torqueY = -staticMoment -dampingMoment;

				windAngle = Math.atan2(Vz, Vx-windVelocity);
				//機体から見てどの方向から風が吹いているか
				//機体から見た風の相対速度の逆ベクトル
				//x軸正の向きが0rad、反時計回りが正
				attackAngle = θ - windAngle;
				//機体の進行方向の軸から、風の吹く方向がどれだけずれているか.
				//進行方向の軸からx軸正の向きへの回転方向が正

				double lift = -drag*Math.tan(attackAngle) +normalForce/Math.cos(attackAngle);
				lift = 0;
				System.out.println(lift);
				if(lastLagCleared) {
					//完全にランチャーからクリアした後の計算
					forceX = thrust*Math.cos(θ) -drag*Math.cos(windAngle) -lift*Math.sin(windAngle);
					forceZ = thrust*Math.sin(θ) -drag*Math.sin(windAngle) +lift*Math.cos(windAngle) -rocketM2*g;
					Vx2 = Vx +forceX/rocketM2 *dt;
					Vz2 = Vz +forceZ/rocketM2 *dt;

					Velocity = Math.sqrt(Vx2*Vx2+Vz2*Vz2);
					relativeVelocityToAir = Math.sqrt(Math.pow(windVelocity-Vx2,2)+Vz2*Vz2);
					XCG2 = XCG +(Vx+Vx2)/2*dt;
					ZCG2 = ZCG +(Vz+Vz2)/2*dt;

					ω2 = ω +torqueY/pitchYawI*dt;
					θ2 = θ + (ω+ω2)/2*dt;


				}else/* if(secondLastLagCleared) {
						//まだ最後の1つのラグがクリアしていないときの計算
						//ここではランチャー方向の座標で一旦計算していることに注意
						θ2 = θ +ω*dt;


						//元の座標系におけるXZ方向の力の成分分解
						lift = -drag*Math.tan(attackAngle) +normalForce/Math.cos(attackAngle);
						forceX = thrust*Math.cos(θ) -drag*Math.cos(windAngle) -lift*Math.sin(windAngle);
						forceZ = thrust*Math.sin(θ) -drag*Math.sin(windAngle) +lift*Math.cos(windAngle) -rocketM2*g;


						double α,β,J,pitchYawI2,Δξ2,Xgo1,Xgo2,Zgo1,Zgo2,lastLagV2;



						double rfA = Math.toRadians(fireAngle);
						double cosfA = Math.cos(rfA), sinfA = Math.sin(rfA);
						double sinφ2=Math.sin(θ2-rfA),cosφ2=Math.cos(θ2-rfA),sinφ=Math.sin(θ-rfA),cosφ=Math.cos(θ-rfA);

						Δξ2 =rocketBefCG-rocketCG2;
						Xgo1 = cosφ*(launchLagLastCG-rocketBefCG+Δξ)+sinφ*(-rocketOuterDiameter/2);
						Xgo2 = cosφ2*(launchLagLastCG-rocketBefCG+Δξ2)+sinφ2*(-rocketOuterDiameter/2);

						Zgo1 = -sinφ*(launchLagLastCG-rocketBefCG+Δξ)+cosφ*(-rocketOuterDiameter/2);
						Zgo2 = -sinφ2*(launchLagLastCG-rocketBefCG+Δξ2)+cosφ2*(-rocketOuterDiameter/2);

						pitchYawI2
						=noseM*Math.pow(noseCG-rocketCG2,2)
						+separaterM*(Math.pow(separaterTop+separaterL/2-rocketCG2,2)+separaterOuterDiameter*separaterOuterDiameter/16+separaterL*separaterL/12)
						+tubeM*(Math.pow(tubeCG-rocketCG2,2)+(rocketInnerDiameter*rocketInnerDiameter+rocketOuterDiameter*rocketOuterDiameter)/16+tubeL*tubeL/12)
						+aluminumPlateM*(Math.pow(aluminumPlateTop+aluminumPlateL/2-rocketCG2,2)+rocketInnerDiameter*rocketInnerDiameter/16+aluminumPlateL*aluminumPlateL/12)
						+(grainContentsM2+grainAftM)*(Math.pow(grainTop+grainL/2-rocketCG2,2)+grainOuterDiameter*grainOuterDiameter/16+grainL*grainL/12)
						+injectorM*(Math.pow(injectorTop+injectorL/2-rocketCG2,2) +injectorOuterDiameter*injectorOuterDiameter/16 +injectorL*injectorL/12)
						+(tankContentsM2+tankAftM)*(Math.pow(tankTop+tankL/2-rocketCG2,2)+tankOuterDiameter*tankOuterDiameter/16+tankL*tankL/12)
						+finM*Math.pow(finCG-rocketCG2,2);


						//導出した計算式をここに入力
						α = rocketM*lastLagV +rocketM*Zgo1*ω+dt*(forceX*cosfA +forceZ*sinfA) +(rocketM2-rocketM)*lastLagV +ω*((grainContentsM2-grainContentsM)*(-sinφ*(launchLagLastCG-grainCG)+cosφ*(-rocketOuterDiameter/2)) +(tankContentsM2-tankContentsM)*(-sinφ*(launchLagLastCG-tankCG) +cosφ*(-rocketOuterDiameter/2)));
						β = rocketM*Zgo1*lastLagV +(pitchYawI+rocketM*(Math.pow(launchLagLastCG-rocketBefCG, 2)+rocketOuterDiameter*rocketOuterDiameter/4))*ω +dt*(torqueY +Zgo1*(forceX*cosfA+forceZ*sinfA) -Xgo1*(-forceX*sinfA +forceZ*cosfA))  +lastLagV*(rocketM2*Zgo2 -rocketM*Zgo1) -ω*((grainContentsM2-grainContentsM)*(Math.pow(launchLagLastCG-grainCG,2) +rocketOuterDiameter*rocketOuterDiameter/4) +(tankContentsM2-tankContentsM)*(Math.pow(launchLagLastCG-tankCG, 2) +rocketOuterDiameter*rocketOuterDiameter/4) );
						J = (β -α*Zgo2)/(pitchYawI2 +rocketM2*Xgo2*Xgo2);

						lastLagV2 = α/rocketM2 -Zgo2*J;
						ω2 = J;
						lastLagX = lastLagX + lastLagV*dt;

						//重心の元の座標における速度、位置の計算

						XCG2 = lastLagX*cosfA +Xgo1*cosfA -Zgo1*sinfA;
						ZCG2 = lastLagX*sinfA +Xgo1*sinfA +Zgo1*cosfA;
						Vx2 = lastLagV2*cosfA +ω*(Xgo1*sinfA +Zgo1*cosfA); //vg = v0 + ω×rg0
						Vz2 = lastLagV2*sinfA -ω*(Xgo1*cosfA -Zgo1*sinfA);

						Δξ = Δξ2;
						pitchYawI = pitchYawI2;
				}else */{
						//2つ以上のラグが残っているときの計算
						
						//修正後の式
						/*
						forceX = thrust*Math.cos(θ) -drag*Math.cos(attackAngle)*Math.cos(θ) +lift*Math.sin(attackAngle)*Math.cos(θ) -rocketM2 *g *Math.sin(θ)*Math.cos(θ);
						forceZ = thrust*Math.sin(θ) -drag*Math.cos(attackAngle)*Math.sin(θ) +lift*Math.sin(attackAngle)*Math.sin(θ) -rocketM2 *g *Math.pow(Math.sin(θ), 2);
						*/
					
						//修正前の式
					
						forceX = thrust*Math.cos(θ) -drag*Math.cos(windAngle);
						forceZ = thrust*Math.sin(θ) -rocketM2*g -drag*Math.sin(windAngle);
						
						
						Vx2 = Vx +forceX/rocketM2 *dt;
						Vz2 = Vz +forceZ/rocketM2 *dt;

						Velocity = Math.sqrt(Vx2*Vx2+Vz2*Vz2);
						relativeVelocityToAir = Math.sqrt(Math.pow(windVelocity-Vx2,2)+Vz2*Vz2);
						XCG2 = XCG +(Vx+Vx2)/2*dt;
						ZCG2 = ZCG +(Vz+Vz2)/2*dt;

						windAngle = Math.atan2(Vz, Vx-windVelocity);
						ω2 = 0;
						θ2 = θ;


						lastLagX = XCG/Math.cos(Math.toRadians(fireAngle))+rocketCG2-launchLagLastCG;
						lastLagV = Vx2/Math.cos(Math.toRadians(fireAngle));
				}


				//得られた次のステップを出力する
				publish(String.format(format, time+dt, thrust, rocketM2, rocketCG2, CD, N_ρ, windVelocity, windAngle, attackAngle, diffCGCP, atomosP, temperature, Vx2, Vz2, XCG2, ZCG2, relativeVelocityToAir, normalForce, drag, ω2, θ2, lastLagCleared));

				//ループの更新処理
				rocketM = rocketM2;
				rocketCG = rocketCG2;
				if(grainContentsM != grainContentsM2) {
					grainContentsM = grainContentsM2;
					tankContentsM = tankContentsM2;
				}
				ω = ω2;
				θ = θ2;
				Vx = Vx2;
				Vz = Vz2;
				XCG = XCG2;
				ZCG = ZCG2;
				time += dt;
			}
			updateProgress(1);
		} catch (IOException e) {
		}

	}


	/*
	 * ICGが使用するパラメータに対応するParameterインスタンスの生成と、シミュレーション実行前のパラメータの設定式を規定する。
	 * */
	@Override
	public void createParameters() {
		super.createParameters();

		final ParameterChecker def = new DefaultParameterChecker(), befAft = new BeforeAfterParamChecker();
		final String
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



		final Parameter
			機体バージョン = new Parameter(一般, "機体バージョン", "機体バージョン", null, null, new WhiteSpaceChecker()),
			使用燃焼データ年月 = new Parameter(一般, "燃焼データ年月20XX/YY", "使用燃焼データ年月20XX/YY", null, null, new DateFormatChecker()),
			thrustFileParam = new Parameter(一般, "燃焼データファイル", "燃焼データファイル", null, null, new ThrustDataChecker()),
			空気密度 = new Parameter(一般, "空気密度", "空気密度", "0.5 kg/m3", "5 kg/m3", def),
			最大飛行速度 = new Parameter(一般, "最大飛行速度", "最大飛行速度", "20 m/s", "200 m/s", def),
			比推力 = new Parameter(一般, "比推力", "比推力", "50 s", "500 s", def),
			燃焼持続時間 = new Parameter(一般, "燃焼持続時間", "燃焼持続時間", "2 s", "30 s", def),
			ランチャー長さ = new Parameter(一般, "ランチャー長さ", "ランチャー長さ", "3 m", "10 m", def),
			方位角 = new Parameter(一般, "打上げ方位角[°]", "磁東からの打上げ方位角[°]", "0", "359", def),
			射角 = new Parameter(一般, "射角[°]", "射角[°]", "50", "90", def),
			風速計高さ = new Parameter(一般, "風速計高さ", "風速計高さ", "0 m", "15 m", def),

			ロケット外径 = new Parameter(ロケット全体, "外径", "ロケット外径", "70 mm", "170 mm", def),
			ロケット内径 = new Parameter(ロケット全体, "内径", "ロケット内径", "70 mm", "170 mm", def),
			全体圧力中心位置 = new Parameter(ロケット全体, "圧力中心位置", "全体圧力中心位置", "1000 mm", "2500 mm", def),
			全体重心位置 = new Parameter(ロケット全体, "重心位置", "全体重心位置", "700 mm", "1500 mm", befAft),
			ロケット全長 = new Parameter(ロケット全体, "全長", "ロケット全長", "1200 mm", "2500 mm", def),
			ロケット質量 = new Parameter(ロケット全体, "質量", "ロケット質量", "4 kg", "10 kg", befAft),
			ロケット抗力係数CD = new Parameter(ロケット全体, "抗力係数", "ロケット抗力係数CD", null, null, def),
			最後のラグ重心位置 = new Parameter(ロケット全体, "最後のラグ重心位置", "最後のラグ重心位置", "1200 mm", "2500 mm", def),
			最後から2番目のラグ重心位置 = new Parameter(ロケット全体, "最後から2番目のラグ重心位置", "最後から2番目のラグ重心位置", "100 mm", "2500 mm", def),

			ノーズコーン長さ = new Parameter(ノーズコーン, "長さ", "ノーズコーン長さ", "100 mm", "500 mm", def),
			ノーズコーン質量 = new Parameter(ノーズコーン, "質量", "ノーズコーン質量", "100 g", "400 g", def),
			ノーズコーン重心位置 = new Parameter(ノーズコーン, "重心位置", "ノーズコーン重心位置", "10 mm", "500 mm", def),
			ノーズコーン圧力中心位置 = new Parameter(ノーズコーン, "圧力中心位置", "ノーズコーン圧力中心位置", "10 mm", "500 mm", def),
			ノーズコーン法線力係数CNαn = new Parameter(ノーズコーン, "法線力係数", "ノーズコーン法線力係数CNαn", null, null, def),

			分離機構長さ = new Parameter(分離機構, "長さ", "分離機構長さ", "5 mm", "300 mm", def),
			分離機構質量 = new Parameter(分離機構, "質量", "分離機構質量", "10 g", "500 g", def),
			分離機構先端位置 = new Parameter(分離機構, "先端位置", "分離機構先端位置", "100 mm", "700 mm", def),
			分離機構外径 = new Parameter(分離機構, "外径", "分離機構外径", "5 mm", "200 mm", def),

			チューブ1長さ = new Parameter(ボディーチューブ, "チューブ1長さ", "チューブ1長さ", "100 mm", "1000 mm", def),
			チューブ2長さ = new Parameter(ボディーチューブ, "チューブ2長さ", "チューブ2長さ", "100 mm", "1000 mm", def),
			チューブ3長さ = new Parameter(ボディーチューブ, "チューブ3長さ", "チューブ3長さ", "100 mm", "1000 mm", def),
			チューブ1質量 = new Parameter(ボディーチューブ, "チューブ1質量", "チューブ1質量", "60 g", "600 g", def),
			チューブ2質量 = new Parameter(ボディーチューブ, "チューブ2質量", "チューブ2質量", "60 g", "600 g", def),
			チューブ3質量 = new Parameter(ボディーチューブ, "チューブ3質量", "チューブ3質量", "60 g", "600 g", def),
			チューブ1重心位置 = new Parameter(ボディーチューブ, "チューブ1重心位置", "チューブ1重心位置", "0 mm", "1000 mm", def),
			チューブ2重心位置 = new Parameter(ボディーチューブ, "チューブ2重心位置", "チューブ2重心位置", "500 mm", "1500 mm", def),
			チューブ3重心位置 = new Parameter(ボディーチューブ, "チューブ3重心位置", "チューブ3重心位置", "1000 mm", "2500 mm", def),

			エンジンを抑えるプレート質量 = new Parameter(エンジンを抑えるプレート, "質量", "エンジンを抑えるプレート質量", "10 g", "500 g", def),
			エンジンを抑えるプレート先端位置 = new Parameter(エンジンを抑えるプレート, "先端位置", "エンジンを抑えるプレート先端位置", "500 mm", "11800 mm", def),
			エンジンを抑えるプレート長さ = new Parameter(エンジンを抑えるプレート, "長さ", "エンジンを抑えるプレート長さ", "0 mm", "10 mm", def),

			燃焼前グレイン質量 = new Parameter(グレイン, "燃焼前質量", "燃焼前グレイン質量", "100 g", "500 g", def),
			燃焼後グレイン質量 = new Parameter(グレイン, "燃焼後質量", "燃焼後グレイン質量", "100 g", "500 g", def),
			グレイン先端位置 = new Parameter(グレイン, "先端位置", "グレイン先端位置", "500 mm", "2000 mm", def),
			グレイン長さ = new Parameter(グレイン, "長さ", "グレイン長さ", "50 mm", "500 mm", def),
			グレイン外径 = new Parameter(グレイン, "外径", "グレイン外径", "5 mm", "200 mm", def),

			インジェクターベル質量 = new Parameter(インジェクターベル, "質量", "インジェクターベル質量", "30 g", "500 g", def),
			インジェクターベル先端位置 = new Parameter(インジェクターベル, "先端位置", "インジェクターベル先端位置", "500 mm", "2000 mm", def),
			インジェクターベル長さ = new Parameter(インジェクターベル, "長さ", "インジェクターベル長さ", "10 mm", "100 mm", def),
			インジェクターベル外径 = new Parameter(インジェクターベル, "外径", "インジェクターベル外径", "5 mm", "200 mm", def),

			燃焼前酸化剤タンク質量 = new Parameter(酸化剤タンク, "燃焼前質量", "燃焼前酸化剤タンク質量", "200 g", "5000 g", def),
			燃焼後酸化剤タンク質量 = new Parameter(酸化剤タンク, "燃焼後質量", "燃焼後酸化剤タンク質量", "100 g", "2500 g", def),
			酸化剤タンク先端位置 = new Parameter(酸化剤タンク, "先端位置", "酸化剤タンク先端位置", "500 mm", "2000 mm", def),
			酸化剤タンク長さ = new Parameter(酸化剤タンク, "長さ", "酸化剤タンク長さ", "100 mm", "1000 mm", def),
			酸化剤タンク外径 = new Parameter(酸化剤タンク, "外径", "酸化剤タンク外径", "20 mm", "200 mm", def),

			フィン枚数 = new Parameter(フィン, "枚数", "フィン枚数", null, null, new IntegerChecker(3,4)),
			フィン高さ = new Parameter(フィン, "高さ", "フィン高さ", "10 mm", "200 mm", def),
			フィン根本長さ = new Parameter(フィン, "根本長さ", "フィン根本長さ", "10 mm", "400 mm", def),
			フィン端部長さ = new Parameter(フィン, "端部長さ", "フィン端部長さ", "10 mm", "400 mm", def),
			フィン後退長さ = new Parameter(フィン, "後退長さ", "フィン後退長さ", "10 mm", "200 mm", def),
			フィン先端位置 = new Parameter(フィン, "先端位置", "フィン先端位置", "500 mm", "2000 mm", def),
			フィン重心位置 = new Parameter(フィン, "重心位置", "フィン重心位置", "500 mm", "2500 mm", def),
			フィン質量 = new Parameter(フィン, "質量", "フィン質量", "10 g", "1000 g", def),
			フィン圧力中心位置 = new Parameter(フィン, "圧力中心位置", "フィン圧力中心位置", "500 mm", "2500 mm", def),
			フィン法線力係数CNαf = new Parameter(フィン, "法線力係数", "フィン法線力係数CNαf", null, null, def),

			干渉係数Kfb = new Parameter(合成パラメータ, "干渉係数Kfb"),
			干渉込みフィン法線力係数CNαfb = new Parameter(合成パラメータ, "干渉込みフィン法線力係数CNαfb"),
			合計法線力係数CNα = new Parameter(合成パラメータ, "合計法線力係数CNα"),
			復元モーメント係数C1 = new Parameter(合成パラメータ, "復元モーメント係数C1"),
			燃焼前空力減衰モーメントCA = new Parameter(合成パラメータ, "空力減衰モーメントCA(燃焼前)"),
			燃焼前ジェット減衰モーメントCR = new Parameter(合成パラメータ, "ジェット減衰モーメントCR(燃焼前)"),
			燃焼前減衰モーメント係数C2 = new Parameter(合成パラメータ, "減衰モーメント係数C2(燃焼前)"),
			ピッチヨー慣性モーメント = new Parameter(合成パラメータ, "慣性モーメント(ピッチ・ヨー)"),
			ロール慣性モーメント = new Parameter(合成パラメータ, "慣性モーメント");

		thrustFileParam.setNeedInputButton(true);

		paraMan.addParameter(機体バージョン);
		paraMan.addParameter(使用燃焼データ年月);
		paraMan.addParameter(thrustFileParam);
		paraMan.addParameter(空気密度);
		paraMan.addParameter(最大飛行速度);
		paraMan.addParameter(比推力);
		paraMan.addParameter(燃焼持続時間);
		paraMan.addParameter(ランチャー長さ);
		paraMan.addParameter(方位角);
		paraMan.addParameter(射角);
		paraMan.addParameter(風速計高さ);

		paraMan.addParameter(ロケット外径);
		paraMan.addParameter(ロケット内径);
		paraMan.addParameter(全体圧力中心位置);
		paraMan.addParameter(全体重心位置);
		paraMan.addParameter(ロケット全長);
		paraMan.addParameter(ロケット質量);
		paraMan.addParameter(ロケット抗力係数CD);
		paraMan.addParameter(最後のラグ重心位置);
		paraMan.addParameter(最後から2番目のラグ重心位置);

		paraMan.addParameter(ノーズコーン長さ);
		paraMan.addParameter(ノーズコーン質量);
		paraMan.addParameter(ノーズコーン重心位置);
		paraMan.addParameter(ノーズコーン圧力中心位置);
		paraMan.addParameter(ノーズコーン法線力係数CNαn);

		paraMan.addParameter(分離機構長さ);
		paraMan.addParameter(分離機構質量);
		paraMan.addParameter(分離機構先端位置);
		paraMan.addParameter(分離機構外径);

		paraMan.addParameter(チューブ1長さ);
		paraMan.addParameter(チューブ2長さ);
		paraMan.addParameter(チューブ3長さ);
		paraMan.addParameter(チューブ1質量);
		paraMan.addParameter(チューブ2質量);
		paraMan.addParameter(チューブ3質量);
		paraMan.addParameter(チューブ1重心位置);
		paraMan.addParameter(チューブ2重心位置);
		paraMan.addParameter(チューブ3重心位置);

		paraMan.addParameter(エンジンを抑えるプレート質量);
		paraMan.addParameter(エンジンを抑えるプレート先端位置);
		paraMan.addParameter(エンジンを抑えるプレート長さ);

		paraMan.addParameter(燃焼前グレイン質量);
		paraMan.addParameter(燃焼後グレイン質量);
		paraMan.addParameter(グレイン先端位置);
		paraMan.addParameter(グレイン長さ);
		paraMan.addParameter(グレイン外径);

		paraMan.addParameter(インジェクターベル質量);
		paraMan.addParameter(インジェクターベル先端位置);
		paraMan.addParameter(インジェクターベル長さ);
		paraMan.addParameter(インジェクターベル外径);

		paraMan.addParameter(燃焼前酸化剤タンク質量);
		paraMan.addParameter(燃焼後酸化剤タンク質量);
		paraMan.addParameter(酸化剤タンク先端位置);
		paraMan.addParameter(酸化剤タンク長さ);
		paraMan.addParameter(酸化剤タンク外径);

		paraMan.addParameter(フィン枚数);
		paraMan.addParameter(フィン高さ);
		paraMan.addParameter(フィン根本長さ);
		paraMan.addParameter(フィン端部長さ);
		paraMan.addParameter(フィン後退長さ);
		paraMan.addParameter(フィン先端位置);
		paraMan.addParameter(フィン重心位置);
		paraMan.addParameter(フィン質量);
		paraMan.addParameter(フィン圧力中心位置);
		paraMan.addParameter(フィン法線力係数CNαf);

		paraMan.addParameter(干渉係数Kfb);
		paraMan.addParameter(干渉込みフィン法線力係数CNαfb);
		paraMan.addParameter(合計法線力係数CNα);
		paraMan.addParameter(復元モーメント係数C1);
		paraMan.addParameter(燃焼前空力減衰モーメントCA);
		paraMan.addParameter(燃焼前ジェット減衰モーメントCR);
		paraMan.addParameter(燃焼前減衰モーメント係数C2);
		paraMan.addParameter(ピッチヨー慣性モーメント);
		paraMan.addParameter(ロール慣性モーメント);


		paraMan.addRunnable(()->{
			Function<Parameter,Double> getDoubleValue = (parameter) -> new PhysicalQuantity(parameter.getValue()).Number;

			thrustFile = new File(thrustFileParam.getValue());
			ρ = getDoubleValue.apply(空気密度);
			Vmax = getDoubleValue.apply(最大飛行速度);
			lsp = getDoubleValue.apply(比推力);
			tb = getDoubleValue.apply(燃焼持続時間);
			launcherL = getDoubleValue.apply(ランチャー長さ);
			launcherAzimuth = getDoubleValue.apply(方位角);
			fireAngle = getDoubleValue.apply(射角);
			anemometerH = getDoubleValue.apply(風速計高さ);

			rocketOuterDiameter = getDoubleValue.apply(ロケット外径);
			rocketInnerDiameter = getDoubleValue.apply(ロケット内径);
			rocketCP = getDoubleValue.apply(全体圧力中心位置);
			rocketL = getDoubleValue.apply(ロケット全長);
			rocketCD = getDoubleValue.apply(ロケット抗力係数CD);
			launchLagSecondLastCG = getDoubleValue.apply(最後から2番目のラグ重心位置);
			launchLagLastCG = getDoubleValue.apply(最後のラグ重心位置);

			noseL = getDoubleValue.apply(ノーズコーン長さ);
			noseM = getDoubleValue.apply(ノーズコーン質量);
			noseCG = getDoubleValue.apply(ノーズコーン重心位置);
			noseCP = getDoubleValue.apply(ノーズコーン圧力中心位置);
			noseCNα = getDoubleValue.apply(ノーズコーン法線力係数CNαn);

			separaterL = getDoubleValue.apply(分離機構長さ);
			separaterM = getDoubleValue.apply(分離機構質量);
			separaterTop = getDoubleValue.apply(分離機構先端位置);
			separaterOuterDiameter = getDoubleValue.apply(分離機構外径);

			tubeL = getDoubleValue.apply(チューブ1長さ)
					+getDoubleValue.apply(チューブ2長さ)
					+getDoubleValue.apply(チューブ3長さ);
			tube1M = getDoubleValue.apply(チューブ1質量);
			tube2M = getDoubleValue.apply(チューブ2質量);
			tube3M = getDoubleValue.apply(チューブ3質量);
			tubeM = tube1M + tube2M + tube3M;
			tubeCG =(getDoubleValue.apply(チューブ1重心位置)*tube1M
					+getDoubleValue.apply(チューブ2重心位置)*tube2M
					+getDoubleValue.apply(チューブ3重心位置)*tube3M)
					/tubeM;

			aluminumPlateM = getDoubleValue.apply(エンジンを抑えるプレート質量);
			aluminumPlateTop = getDoubleValue.apply(エンジンを抑えるプレート先端位置);
			aluminumPlateL = getDoubleValue.apply(エンジンを抑えるプレート長さ);

			grainBefM = getDoubleValue.apply(燃焼前グレイン質量);
			grainAftM = getDoubleValue.apply(燃焼後グレイン質量);
			grainContentsBefM = grainBefM -grainAftM;
			grainTop = getDoubleValue.apply(グレイン先端位置);
			grainL = getDoubleValue.apply(グレイン長さ);
			grainCG = grainTop +grainL/2;
			grainOuterDiameter = getDoubleValue.apply(グレイン外径);

			injectorM = getDoubleValue.apply(インジェクターベル質量);
			injectorTop = getDoubleValue.apply(インジェクターベル先端位置);
			injectorL = getDoubleValue.apply(インジェクターベル長さ);
			injectorOuterDiameter = getDoubleValue.apply(インジェクターベル外径);

			tankBefM = getDoubleValue.apply(燃焼前酸化剤タンク質量);
			tankAftM = getDoubleValue.apply(燃焼後酸化剤タンク質量);
			tankContentsBefM = tankBefM -tankAftM;
			tankTop = getDoubleValue.apply(酸化剤タンク先端位置);
			tankL = getDoubleValue.apply(酸化剤タンク長さ);
			tankCG = tankTop +tankL/2;
			tankOuterDiameter = getDoubleValue.apply(酸化剤タンク外径);

			finNumber = getDoubleValue.apply(フィン枚数);
			finH = getDoubleValue.apply(フィン高さ);
			finRootL = getDoubleValue.apply(フィン根本長さ);
			finEdgeL = getDoubleValue.apply(フィン端部長さ);
			finBackL = getDoubleValue.apply(フィン後退長さ);
			finTop = getDoubleValue.apply(フィン先端位置);
			finCG = getDoubleValue.apply(フィン重心位置);
			finM = getDoubleValue.apply(フィン質量);
			finCP = getDoubleValue.apply(フィン圧力中心位置);
			finCNα = getDoubleValue.apply(フィン法線力係数CNαf);


			String st;
			st = ロケット質量.getValue();
			rocketAftM = new PhysicalQuantity(st.substring(0,st.length()-1)).Number;
			if(st.endsWith("b")) {
				//燃焼後(乾燥時)の質量に直す
				rocketAftM -= (tankContentsBefM +grainContentsBefM);
			}

			st = 全体重心位置.getValue();
			rocketAftCG = new PhysicalQuantity(st.substring(0,st.length()-1)).Number;
			if(st.endsWith("b")) {
				//同様
				//右辺のrocketAftCGは燃焼前重心
				rocketAftCG = (1+(grainContentsBefM+tankContentsBefM)/rocketAftM)*rocketAftCG -(grainContentsBefM*grainCG+tankContentsBefM*tankCG)/rocketAftM;
			}


			Kfb = 1+rocketOuterDiameter/(2*finH+rocketOuterDiameter);
			finCNαb = Kfb*finCNα;
			rocketCNα = noseCNα +finCNαb;
			干渉係数Kfb.setValue(String.valueOf(Kfb));
			干渉込みフィン法線力係数CNαfb.setValue(String.valueOf(finCNαb));
			合計法線力係数CNα.setValue(String.valueOf(rocketCNα));


			CA = ρ*Vmax*rocketOuterDiameter*rocketOuterDiameter/4*(noseCNα*Math.pow(noseCP-rocketAftCG,2)+finCNαb*Math.pow(finCP-rocketAftCG,2));
			CR = grainContentsBefM*Math.pow(rocketL-rocketAftCG,2)/tb;


			復元モーメント係数C1.setValue(String.valueOf(ρ*(noseCNα+finCNαb)*Math.pow(rocketOuterDiameter/2*Vmax,2)*(rocketCP-rocketAftCG))+" kg m2/s2");
			燃焼前空力減衰モーメントCA.setValue(String.valueOf(CA)+" kg m2/s");
		//一時的に機体の重心を燃焼後(乾燥)で出している
		//ジェット減衰モーメントは燃焼前の状態で出しているのだが
		//後で要確認
			燃焼前ジェット減衰モーメントCR.setValue(String.valueOf(CR)+" kg m2/s");
			燃焼前減衰モーメント係数C2.setValue(String.valueOf(CA+CR)+" kg m2/s");


			BefPitchyawI
				=noseM*Math.pow(noseCG-rocketAftCG,2)
				+separaterM*(Math.pow(separaterTop+separaterL/2-rocketAftCG,2)+separaterOuterDiameter*separaterOuterDiameter/16+separaterL*separaterL/12)
				+tubeM*(Math.pow(tubeCG-rocketAftCG,2)+(rocketInnerDiameter*rocketInnerDiameter+rocketOuterDiameter*rocketOuterDiameter)/16+tubeL*tubeL/12)
				+aluminumPlateM*(Math.pow(aluminumPlateTop+aluminumPlateL/2-rocketAftCG,2)+rocketInnerDiameter*rocketInnerDiameter/16+aluminumPlateL*aluminumPlateL/12)
				+grainAftM*(Math.pow(grainTop+grainL/2-rocketAftCG,2)+grainOuterDiameter*grainOuterDiameter/16+grainL*grainL/12)
				+injectorM*(Math.pow(injectorTop+injectorL/2-rocketAftCG,2) +injectorOuterDiameter*injectorOuterDiameter/16 +injectorL*injectorL/12)
				+tankAftM*(Math.pow(tankTop+tankL/2-rocketAftCG,2)+tankOuterDiameter*tankOuterDiameter/16+tankL*tankL/12)
				+finM*Math.pow(finCG-rocketAftCG,2);

			BefRollI
				=noseM*rocketOuterDiameter*rocketOuterDiameter/16
				+separaterM*separaterOuterDiameter*separaterOuterDiameter/4
				+tubeM*(rocketInnerDiameter*rocketInnerDiameter+rocketOuterDiameter*rocketOuterDiameter)/4
				+aluminumPlateM*rocketInnerDiameter*rocketInnerDiameter/4
				+(grainAftM+injectorM+tankAftM)*injectorOuterDiameter*injectorOuterDiameter/4
				+finM*(Math.pow(rocketOuterDiameter/2+finH,3)-Math.pow(rocketOuterDiameter, 3)/8)/finH;

			ピッチヨー慣性モーメント.setValue(String.valueOf(BefPitchyawI)+" kg m2");
			ロール慣性モーメント.setValue(String.valueOf(BefRollI)+" kg m2");
		});

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