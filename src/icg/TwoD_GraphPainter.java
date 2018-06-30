package icg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import simulation.frame.GraphLabel;

public class TwoD_GraphPainter extends GraphPainter {
	int i=0;


	public TwoD_GraphPainter(GraphLabel lb) {
		super(lb);
		this.saveImage = new BufferedImage(100,100, BufferedImage.TYPE_INT_ARGB);
	}

	@Override
	protected void makeSaveGraph(Graphics2D g2) {
		g2.setColor(Color.black);
		g2.drawLine(0, i, 100, 100+i);
		i+=10;
	}

	@Override
	public void setData(double[][] data) {

	}

	@Override
	public void savePaint(File saveFile) {

	}
}
