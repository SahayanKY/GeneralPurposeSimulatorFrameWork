package animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class GIFCreator {
	int width = 500,height = 300;
	TimeStepper stepper;
	BufferedImage backgroundImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

	public static void main(String args[]) {
		GIFCreator gifcre = new GIFCreator();
		gifcre.stepper = gifcre.new TimeStepper();
		gifcre.stepper.addMaterialPoint(gifcre.new MaterialPoint(102,150,0,8,16,0,5));
		gifcre.stepper.addMaterialPoint(gifcre.new MaterialPoint(150,40,0,15,60,0,5));
		gifcre.stepper.addMaterialPoint(gifcre.new MaterialPoint(180,160,0,30,16,0,5));
		gifcre.stepper.addMaterialPoint(gifcre.new MaterialPoint(300,100,0,-15,-60,0,5));
		gifcre.stepper.addMaterialPoint(gifcre.new MaterialPoint(142,100,0,18,-7,0,5));
		gifcre.stepper.addMaterialPoint(gifcre.new MaterialPoint(280,110,0,15,60,0,5));
		gifcre.stepper.addMaterialPoint(gifcre.new MaterialPoint(180,50,0,-5,-10,0,5));
		gifcre.execute();
	}

	private void execute() {
		makeBackground();

		Iterator it = ImageIO.getImageWritersByFormatName("gif");
		ImageWriter iw = it.hasNext()? (ImageWriter) it.next(): null;
		File outputFile = new File("D:/ゆうき/大学/プログラム/test3.gif");

		try {
			ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
			iw.setOutput(ios);
			iw.prepareWriteSequence(null);

			for(int page=0;page<1000;page++) {
				try {
					System.out.println(page);
					BufferedImage bimage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
					createAnimationScene(bimage.createGraphics(),page);
					iw.writeToSequence(new IIOImage(bimage, null, null), null);
				}catch(IOException e) {
				}
			}
			iw.endWriteSequence();

		}catch(IOException e) {

		}
		System.out.println("ended");
	}

	private void makeBackground() {
		Graphics2D g2 = backgroundImage.createGraphics();
		for(int x = 0; x < width ; x++) {
			for(int y = 0; y < height ; y++) {
				if(Function.f(x, y, 0)<0) {
					g2.setColor(Color.gray);
				}else {
					g2.setColor(Color.white);
				}
				g2.fillRect(x, y, 1, 1);
			}
		}
	}

	private void createAnimationScene(Graphics2D g2, int page) {
		g2.drawImage(backgroundImage, 0, 0, null);

		if(page != 0) {
			//質点の更新
			stepper.calculateNextStep();
		}

		MaterialPoint points[] = stepper.getMaterialPoints();
		for(MaterialPoint point:points) {
			int colorN = point.hashCode()%(16*16*16);
			int r = colorN/(16*16);
			colorN = colorN%(16*16);
			int g = colorN/16;
			int b = colorN%16;
			g2.setColor(new Color(r*16,g*16,b*16));
			g2.fillOval((int)(point.x), (int)(point.y), 4, 4);
		}
	}

	public class TimeStepper{
		private double minimumConsideredWallThickness = 20;
		ArrayList<MaterialPoint> pointList = new ArrayList<>();
		public void addMaterialPoint(MaterialPoint mp) {
			pointList.add(mp);
		}
		public MaterialPoint[] getMaterialPoints() {
			return pointList.toArray(new MaterialPoint[pointList.size()]);
		}
		public void calculateNextStep() {
			double dt=0.1;
			for(MaterialPoint mp:pointList) {
				double distance,f1=0,f2=0,t1=0,t2=0;

				//現在位置と予想到達点を最低限考慮する行程距離以下に分割し、
				//その各点で壁の中に入っていないかを確認する

				//予想行程距離
				distance = Math.sqrt(mp.vx*mp.vx +mp.vy*mp.vy +mp.vz*mp.vz)*dt;
				int checkTime = (int) (distance/minimumConsideredWallThickness)+2;
				//f1に拘束条件Gが正となる最も衝突時に近い値を入れる
				//f2は衝突直後の値を入れる
				for(int i=0;i<checkTime;i++) {
					t2 = i*dt/(checkTime-1);
					f2 = Function.f(mp.x +mp.vx*t2, mp.y +mp.vy*t2, mp.z +mp.vz*t2);
					if(i==0 && f2 < 0) {
						throw new Error("衝突判定が異常です:code:"+mp.hashCode());
					}
					if(f2 < 0) {
						break;
					}else {
						t1 = t2;
						f1 = f2;
					}
				}

				if(f2 > 0) {
					//衝突しなかった場合
					eulerMethod(mp,dt);
				}else {
					//衝突した場合
					//2分法を使い、f1,f2の間にある衝突時刻を求める。
					for(int i=0;i<20;i++) {
						double t3 = (t1+t2)/2;
						double f3 = Function.f(mp.x+mp.vx*(t1+t2)/2, mp.y +mp.vy*(t1+t2)/2, mp.z +mp.vz*(t1+t2)/2);
						if(f3 > 0) {
							t1 = t3;
							f1 = f3;
						}else {
							t2 = t3;
							f2 = f3;
						}
						if(f1 < 1E-5 || f2 > -1E-5) {
							t1 = t2 = t3;
							break;
						}
					}

					eulerMethod(mp,t1);

					//壁との弾性衝突による速度変化
					Vector<Double> grad = Function.gradient(mp.x, mp.y, mp.z);
					double gradNorm = Math.pow(grad.get(0),2) +Math.pow(grad.get(1), 2) +Math.pow(grad.get(2), 2);
					double grad_dot_V = (grad.get(0)*mp.vx +grad.get(1)*mp.vy +grad.get(2)*mp.vz);
					mp.vx = mp.vx -2*grad.get(0)*grad_dot_V/gradNorm;
					mp.vy = mp.vy -2*grad.get(1)*grad_dot_V/gradNorm;
					mp.vz = mp.vz -2*grad.get(2)*grad_dot_V/gradNorm;

					//残りの時間発展
					eulerMethod(mp,dt-t1);
				}

			}


		}

		private void eulerMethod(MaterialPoint mp, double dt) {
			mp.x = mp.x +mp.vx*dt;
			mp.y = mp.y +mp.vy*dt;
			mp.z = mp.z +mp.vz*dt;

			mp.vy = mp.vy +20*dt;
		}
	}

	public static class Function{
		static int a=500,b=300;
		public static Vector<Double> gradient(double x, double y, double z){
			double dx = 0.1, dy = 0.1, dz = 0.1;
			Vector<Double> vector = new Vector<>(3);
			vector.add(0,(f(x+dx,y,z)-f(x,y,z))/dx);
			vector.add(1,(f(x,y+dy,z)-f(x,y,z))/dy);
			vector.add(2,(f(x,y,z+dz)-f(x,y,z))/dz);
			return vector;
		}

		public static double f(double x, double y, double z) {
			return 1 -(x-a/2.0)*(x-a/2.0)*4/a/a -(y-b/2.0)*(y-b/2.0)*4/b/b;
		}
	}

	public class MaterialPoint{
		double x,y,z;
		double vx,vy,vz;
		double m;

		private int code;

		MaterialPoint(double x,double y, double z, double vx, double vy, double vz, double m) throws IllegalArgumentException{
			if(Function.f(x, y, z)<0) {
				throw new IllegalArgumentException("初期位置が不正です:("+x+","+y+","+z+")");
			}
			this.x = x;
			this.y = y;
			this.z = z;
			this.vx = vx;
			this.vy = vy;
			this.vz = vz;
			this.m = m;
			this.code = 719*(int)x+401*(int)y+659*(int)z+101*(int)vx+139*(int)vy+241*(int)vz+353*(int)m;
		}

		@Override
		public int hashCode() {
			return this.code;
		}
	}


}
