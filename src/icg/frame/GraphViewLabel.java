package icg.frame;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import icg.GraphPainter;

/*
 * シミュレーションの結果得られた数値データをグラフにするクラス
 *
 * */
public class GraphViewLabel extends JLabel implements MouseListener {
	public enum GraphKind{
		TwoD_Graph(),
		Dispersion_Graph();

		GraphPainter painter;
	}

	private Point point=new Point(-50,-50);
	private GraphPainter painter;

	public GraphViewLabel(){
		addMouseListener(this);
	}


	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		painter.graphPaint(g);
	}

	@Override
	public void mouseClicked(MouseEvent e){
		point = e.getPoint();
		Object obj;
		if((obj = e.getSource()) instanceof JLabel) {
			((JLabel)obj).repaint();
		}
	}

	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
}
