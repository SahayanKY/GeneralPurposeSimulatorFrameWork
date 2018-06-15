package icg.frame;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import icg.GraphPainter;

/*
 * シミュレーションの結果得られた数値データのグラフを表示するラベルクラス
 *
 * */
public class GraphLabel extends JLabel implements MouseListener {
	private Point point=new Point(-50,-50);
	/*このインスタンスが描画を委譲するPainterクラスインスタンス*/
	private GraphPainter painter;
	/*このインスタンスがクリックに対して反応するかどうかを設定*/
	private boolean isReactiveToClick = false;

	public GraphLabel(){
		addMouseListener(this);
	}

	public void setGraphPainter(GraphPainter painter) {
		this.painter = painter;
	}

	public void setReactiveToClick(boolean isReactive) {
		this.isReactiveToClick = isReactive;
	}


	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		painter.graphPaint(g);
	}

	@Override
	public void mouseClicked(MouseEvent e){
		if(!this.isReactiveToClick) {
			return;
		}
		point = e.getPoint();
		Object obj;
		if((obj = e.getSource()) instanceof GraphLabel) {
			((JLabel)obj).repaint();
		}
	}

	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
}
