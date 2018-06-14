package icg;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
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
import javax.swing.JLabel;
import javax.swing.JPanel;

import icg.frame.GraphViewLabel;

/*
 * このクラスがgetGraphics()で渡すGraphicsインスタンスで描画した内容を
 * 画像ファイルとして保存するクラス。
 * */
public class ImagePainter extends JPanel implements ActionListener{
	private Point point = new Point(-5,-5);
	boolean f = true;
	JLabel lb;

	public static void main(String[] args) {
		new ImagePainter("ImagePainter");
	}

	ImagePainter(String title){
		JFrame frame = new JFrame(title);
		frame.setBounds(100,300,500,300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);

		lb = new GraphViewLabel();
		ImageIcon icon = new ImageIcon("D:/ゆうき/キャプチャ1.jpg");
		lb.setIcon(icon);
		this.add(lb);


		JButton savebtn = new JButton("保存");
		savebtn.addActionListener(this);
		this.add(savebtn);

		JButton changeBtn = new JButton("画像変更");
		changeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(f) {
					lb.setIcon(new ImageIcon("D:/ゆうき/キャプチャ9.jpg"));
					f=false;
				}else {
					lb.setIcon(new ImageIcon("D:/ゆうき/キャプチャ1.jpg"));
					f=true;
				}
			}
		});
		this.add(changeBtn);


		frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		try (FileImageOutputStream output = new FileImageOutputStream(new File("D:/ゆうき/AAA.jpg"))) {

			BufferedImage readImage = ImageIO.read(new File("D:/ゆうき/キャプチャ1.jpg"));

			Graphics graphics = readImage.createGraphics();

			//	いたずら書き
			graphics.setColor(Color.black);
			graphics.drawOval(point.x-3, point.y-3, 6, 6);
			graphics.drawString("faeawjeoif", 45, 45);

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
}
