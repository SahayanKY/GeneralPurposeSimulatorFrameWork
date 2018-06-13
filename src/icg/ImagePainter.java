package icg;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class ImagePainter {


	public static void main(String[] args) {
		try (FileImageOutputStream output = new FileImageOutputStream(new File("D:/ゆうき/test.jpg"))) {

			BufferedImage readImage = ImageIO.read(new File("D:/ゆうき/キャプチャ1.jpg"));

			Graphics graphics = readImage.createGraphics();

			//	いたずら書き
			graphics.setColor(Color.RED);
			graphics.drawString("いたずら書きだよー",0,20);

			ImageWriter writeImage = ImageIO.getImageWritersByFormatName("jpeg").next();
			ImageWriteParam writeParam = writeImage.getDefaultWriteParam();
			writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			writeParam.setCompressionQuality(1.0f);
			writeImage.setOutput(output);
			writeImage.write(null, new IIOImage(readImage, null, null), writeParam);
			writeImage.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}


		JFrame f = new JFrame();
		f.setBounds(100,300,500,300);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel p = new JPanel();
		f.add(p);

		JLabel lb = new JLabel();
		ImageIcon icon = new ImageIcon("D:/ゆうき/test.jpg");
		lb.setIcon(icon);
		p.add(lb);

		f.setVisible(true);
	}
}
