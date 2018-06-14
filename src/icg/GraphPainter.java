package icg;

import java.awt.Graphics;

public interface GraphPainter {
	public void graphPaint(Graphics g);
	public void setData(double[][] data);
	public void savePaint();
}
