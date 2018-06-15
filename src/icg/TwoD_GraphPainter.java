package icg;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class TwoD_GraphPainter implements GraphPainter {
	int i=0;

	BufferedImage BI = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);


	public void graphPaint(Graphics g) {
		Graphics2D BufferedG = BI.createGraphics();

		BufferedG.setColor(Color.black);
		BufferedG.drawLine(0, i, 100, 100+i);

		g.drawImage(BI, 0,0,null);

		i += 10;
	}

	public void setData(double[][] data) {

	}

	public void savePaint() {

	}
}
