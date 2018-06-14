package icg;

import java.awt.Color;
import java.awt.Graphics;

public class TwoD_GraphPainter implements GraphPainter {

	public void graphPaint(Graphics g) {
		g.setColor(Color.black);
		g.drawLine(0, 0, 100, 100);
	}

	public void setData(double[][] data) {

	}

	public void savePaint() {

	}
}
