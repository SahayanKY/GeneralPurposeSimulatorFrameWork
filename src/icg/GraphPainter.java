package icg;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import icg.frame.GraphLabel;

public abstract class GraphPainter {
	protected BufferedImage labelImage;
	protected GraphLabel paintingLabel;

	/*ラベル中の画像の左上の座標位置*/
	protected Point ImageOriginPoint;
	protected double scaleToLabel;

	GraphPainter(GraphLabel lb){
		this.paintingLabel = lb;
	}

	public void PaintGraph(Graphics g) {
		if(labelImage==null) {
			return;
		}
		Dimension labelDimension = paintingLabel.getSize();

		if(ImageOriginPoint == null) {
			setParameter(labelDimension.width, labelDimension.height, labelImage.getWidth(), labelImage.getHeight());
		}

		Graphics2D g2 = labelImage.createGraphics();
		makeGraph(g2);

		g.drawImage(labelImage, ImageOriginPoint.x, ImageOriginPoint.y, labelDimension.width-2*ImageOriginPoint.x, labelDimension.height-2*ImageOriginPoint.y, null);

	};

	/*
	 * 画像の上に描画する場合に必要となるオフセット座標、および倍率の計算
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

	protected abstract void makeGraph(Graphics2D g2);
	public abstract void setData(double[][] data);
	public abstract void savePaint();
}
