package icg;

import java.awt.Graphics;

public class Dispersion_GraphPainter implements GraphPainter {
	int i=0;
	public void graphPaint(Graphics g) {
		g.drawLine(i, i, 50, 100);
		i += 10;
	}

	public void setData(double[][] data) {

	}

	
	public void savePaint() {

	}
}
