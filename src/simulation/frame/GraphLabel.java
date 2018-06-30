package simulation.frame;

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
	public boolean isClicked = false;
	public Point clickedPoint=new Point(-50,-50);
	/*このインスタンスが描画を委譲するPainterクラスインスタンス*/
	private GraphPainter painter;
	/*このインスタンスがクリックに対して反応するかどうかを設定*/
	private boolean isReactiveToClick = false;

	/*
	 * コンストラクタ。
	 * */
	public GraphLabel(){
		addMouseListener(this);
	}


	/*
	 * このGraphLabelインスタンスへ描画を行うGraphPainterを指定する。
	 * @param
	 * painter 描画を行うGraphPainter実装クラスのインスタンス
	 * */
	public void setGraphPainter(GraphPainter painter) {
		this.painter = painter;
	}


	/*
	 * このGraphLabelインスタンスがマウスのクリックに対して反応するかを指定する。
	 * これによりクリックに応じた描画更新の有無が規定される。
	 * @param
	 * isReactive クリックに対して反応させる場合はtrue
	 * */
	public void setReactiveToClick(boolean isReactive) {
		this.isReactiveToClick = isReactive;
	}


	/*
	 * このGraphLabelの描画処理
	 * */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(painter!=null) {
			painter.paintGraph(g);
		}
		this.isClicked = false;
	}

	/*
	 * このGraphLabelのマウスクリックへの反応処理
	 * */
	@Override
	public void mouseClicked(MouseEvent e){
		if(!this.isReactiveToClick) {
			return;
		}
		clickedPoint = e.getPoint();
		Object obj;
		if((obj = e.getSource()) instanceof GraphLabel) {
			this.isClicked = true;
			((GraphLabel)obj).repaint();
		}
	}

	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
}
