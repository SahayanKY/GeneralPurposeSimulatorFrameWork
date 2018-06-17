package icg;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;

import icg.frame.GraphLabel;

public abstract class GraphPainter {
	protected BufferedImage bImage;
	protected GraphLabel paintingLabel;

	/*ラベル中の画像の左上の座標位置*/
	protected Point ImageOriginPoint;
	protected double scaleToLabel;

	/*
	 * コンストラクタ。描画対象となるGraphLabelインスタンスを指定する。
	 * */
	GraphPainter(GraphLabel lb){
		this.paintingLabel = lb;
	}

	/*
	 * 描画対象のラベルが取得したGraphicsインスタンスを指定する。GraphPainter実装クラスに
	 * 基づいて描画を行う。
	 * @param 描画対象のGraphLabelが取得したGraphicsインスタンス
	 * */
	public void PaintGraph(Graphics g) {
		if(bImage==null) {
			return;
		}
		Dimension labelDimension = paintingLabel.getSize();

		if(ImageOriginPoint == null) {
			setParameter(labelDimension.width, labelDimension.height, bImage.getWidth(), bImage.getHeight());
		}

		makeGraph(bImage.createGraphics());

		//ちょっと重たい
		Image scaledImage = bImage.getScaledInstance((int)(bImage.getWidth()*scaleToLabel), -1, Image.SCALE_SMOOTH);
		g.drawImage(scaledImage, ImageOriginPoint.x, ImageOriginPoint.y, null);

		//荒すぎ
		//g.drawImage(labelImage, ImageOriginPoint.x, ImageOriginPoint.y, labelDimension.width-2*ImageOriginPoint.x, labelDimension.height-2*ImageOriginPoint.y, null);
	};

	/*
	 * 画像の上に描画する場合に必要となるオフセット座標、および倍率の計算を行う。
	 * 計算の結果はメンバ変数scaleToLabel(オフスクリーンイメージからラベルへサイズを合わせる倍率)
	 * と、ImageOriginPoint(オフスクリーンイメージの原点のラベル上での座標)に保持される。
	 * @param
	 * labelW 描画対象のラベルの幅
	 * labelH 描画対象のラベルの縦幅
	 * imageW オフスクリーンイメージの幅
	 * imageH オフスクリーンイメージの縦幅
	 * */
	protected final void setParameter(int labelW, int labelH, int imageW, int imageH) {
		if(labelW*imageH<imageW*labelH) {
			//画像の横幅をラベルの横幅に合わせる場合
			scaleToLabel = ((double)labelW)/imageW;
			ImageOriginPoint = new Point(0,(int)(labelH-imageH*scaleToLabel)/2);
		}else {
			//縦幅の方を合わせる場合
			scaleToLabel = ((double)labelH)/imageH;
			ImageOriginPoint = new Point((int)(labelW-imageW*scaleToLabel)/2,0);
		}
	}

	/*
	 * GraphPainterを実装するクラスが規定する描画処理を行う。引数にはGraphPainterが内部に保持する
	 * オフクリーンイメージ(BufferedImage)から生成したGraphics2Dインスタンスを指定する。
	 * @param bImageのGraphics2Dインスタンス
	 * */
	protected abstract void makeGraph(Graphics2D g2);

	/*
	 * 描画に必要なデータをこのGraphPainterにセットする。
	 * @param
	 * data 描画に必要なデータを指定する。具体的なフォーマットに関しては
	 * GraphPainterを実装するクラスに従う。
	 * */
	public abstract void setData(double[][] data);

	/*
	 * 現在までの描画内容を指定されたファイルに保存する。
	 * @param
	 * saveFile 保存先のファイルを示すFileインスタンス
	 * */
	public abstract void savePaint(File saveFile);
}
