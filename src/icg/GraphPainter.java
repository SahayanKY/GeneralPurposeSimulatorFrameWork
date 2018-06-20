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
	protected BufferedImage saveImage;

	/*
	 * このイメージの原点とスケールはGraphLabelのそれと一致する。
	 * */
	protected BufferedImage temporaryImage;

	protected final GraphLabel paintingLabel;
	protected final Dimension labelDimension;

	/*ラベル中の画像の左上の座標位置*/
	protected Point ImageOriginPoint;
	protected double scaleToLabel;


	/*
	 * コンストラクタ。描画対象となるGraphLabelインスタンスを指定する。
	 * */
	GraphPainter(GraphLabel paintingLabel){
		this.paintingLabel = paintingLabel;
		this.labelDimension = paintingLabel.getSize();
	}

	/*
	 * 描画対象のラベルが取得したGraphicsインスタンスを指定する。GraphPainter実装クラスに
	 * 基づいて描画を行う。
	 * @param 描画対象のGraphLabelが取得したGraphicsインスタンス
	 * */
	public void paintGraph(Graphics g) {
		if(saveImage==null) {
			return;
		}

		if(ImageOriginPoint == null) {
			setParameter(labelDimension.width, labelDimension.height, saveImage.getWidth(), saveImage.getHeight());
		}

		makeSaveGraph(saveImage.createGraphics());

		//ちょっと重たい
		Image scaledImage = saveImage.getScaledInstance((int)(saveImage.getWidth()*scaleToLabel), -1, Image.SCALE_SMOOTH);
		g.drawImage(scaledImage, ImageOriginPoint.x, ImageOriginPoint.y, null);

		if(temporaryImage == null) {
			return;
		}else {
			makeTemporaryGraph(temporaryImage.createGraphics());
			g.drawImage(temporaryImage, 0, 0, null);
		}
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
	protected void setParameter(int labelW, int labelH, int imageW, int imageH) {
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

	protected Point convertCoordinate_LabelToImage() {
		int clikedPointx =(int) ((this.paintingLabel.clickedPoint.x - this.ImageOriginPoint.x)/scaleToLabel);
		int clikedPointy = (int) ((this.paintingLabel.clickedPoint.y - this.ImageOriginPoint.y)/scaleToLabel);
		return new Point(clikedPointx,clikedPointy);
	}

	/*
	 * GraphPainterを実装するクラスが規定する描画処理を行う。引数にはGraphPainterが内部に保持する
	 * オフクリーンイメージ(saveImage)から生成したGraphics2Dインスタンスを指定する。
	 * このメソッドではイメージを保存する際に、保存したいその内容を記述する。
	 * @param saveImageのGraphics2Dインスタンス
	 * */
	protected abstract void makeSaveGraph(Graphics2D g2);

	/*
	 * GraphPainterを実装するクラスが規定する描画処理を行う。引数にはGraphPainterが内部に保持する
	 * オフクリーンイメージ(temporaryImage)から生成したGraphics2Dインスタンスを指定する。
	 * このメソッドではGraphLabelに一時的に描画し、イメージを保存する際には含めたくない内容を記述する。
	 * @param temporaryImageのGraphics2Dインスタンス
	 * */
	protected void makeTemporaryGraph(Graphics2D g2) {}

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
