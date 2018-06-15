package icg;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import icg.frame.GraphLabel;

/*
 *
 * */
public class GraphViewFrame extends JFrame {
	public enum GraphKind{
		TwoD_Graph(new TwoD_GraphPainter()),
		Dispersion_Graph(new Dispersion_GraphPainter());

		GraphKind(GraphPainter painter){
			this.painter = painter;
		}
		public GraphPainter painter;
	}

	private GraphLabel graphLb;

	private GraphPainter currentPainter;

	public static void main(String[] args) {
		new GraphViewFrame("GraphFrame");
	}

	GraphViewFrame(String title){
		setBounds(100,100,700,700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();

		graphLb = new GraphLabel();
		ImageIcon icon = new ImageIcon("D:/ゆうき/キャプチャ10.jpg");
		graphLb.setIcon(icon);
		setPainter(GraphKind.TwoD_Graph.painter);
		graphLb.setReactiveToClick(true);
		panel.add(graphLb);

		JButton paintBtn = new JButton("描画");
		paintBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				graphLb.repaint();
			}
		});
		panel.add(paintBtn);

		JButton savebtn = new JButton("保存");
		savebtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try (FileImageOutputStream output = new FileImageOutputStream(new File("D:/ゆうき/AAA.jpg"))) {

					BufferedImage readImage = ImageIO.read(new File("D:/ゆうき/キャプチャ10.jpg"));

					Graphics graphics = readImage.createGraphics();

					//	いたずら書き
					currentPainter.graphPaint(graphics);

					ImageWriter writeImage = ImageIO.getImageWritersByFormatName("jpeg").next();
					ImageWriteParam writeParam = writeImage.getDefaultWriteParam();
					writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					writeParam.setCompressionQuality(1.0f);
					writeImage.setOutput(output);
					writeImage.write(null, new IIOImage(readImage, null, null), writeParam);
					writeImage.dispose();
				} catch (IOException exc) {
					exc.printStackTrace();
				}
			}
		});
		panel.add(savebtn);

		JButton changeBtn = new JButton("画像変更");
		changeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		panel.add(changeBtn);

		this.add(panel);
		setVisible(true);
	}


	private void setPainter(GraphPainter painter) {
		currentPainter = painter;
		graphLb.setGraphPainter(painter);
	}

}
