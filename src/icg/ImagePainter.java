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

import icg.frame.GraphViewLabel;

/*
 * このクラスがgetGraphics()で渡すGraphicsインスタンスで描画した内容を
 * 画像ファイルとして保存するクラス。
 * */
public class ImagePainter extends JPanel {
	public enum GraphKind{
		TwoD_Graph(new TwoD_GraphPainter()),
		Dispersion_Graph(new Dispersion_GraphPainter());

		GraphKind(GraphPainter painter){
			this.painter = painter;
		}
		public GraphPainter painter;
	}

	private GraphViewLabel graphLb;

	private GraphPainter currentPainter;

	public static void main(String[] args) {
		new ImagePainter("ImagePainter");
	}

	ImagePainter(String title){
		JFrame frame = new JFrame(title);
		frame.setBounds(100,100,700,700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);

		graphLb = new GraphViewLabel();
		ImageIcon icon = new ImageIcon("D:/ゆうき/キャプチャ10.jpg");
		graphLb.setIcon(icon);
		setPainter(GraphKind.TwoD_Graph.painter);
		graphLb.setOpaque(false);
		this.add(graphLb);

		JButton paintBtn = new JButton("描画");
		paintBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				graphLb.repaint();
			}
		});
		this.add(paintBtn);

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
		this.add(savebtn);

		JButton changeBtn = new JButton("画像変更");
		changeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		this.add(changeBtn);


		frame.setVisible(true);
	}


	private void setPainter(GraphPainter painter) {
		currentPainter = painter;
		graphLb.setGraphPainter(painter);
	}

}
