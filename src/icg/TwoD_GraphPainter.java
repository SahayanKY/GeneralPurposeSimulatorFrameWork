package icg;

import java.awt.Color;
import java.awt.Graphics;

public class TwoD_GraphPainter implements GraphPainter {
	int i=0;

	public void graphPaint(Graphics g) {
		g.setColor(Color.black);
		g.drawLine(0, i, 100, 100+i);
		i += 10;
	}

	public void setData(double[][] data) {

	}

	public void savePaint() {

	}
}
