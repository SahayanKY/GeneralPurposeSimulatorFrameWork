package icg;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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


public class ImagePainter extends JPanel implements MouseListener, ActionListener{
	private Point point = new Point(-5,-5);

	public static void main(String[] args) {
		new ImagePainter("ImagePainter");
	}

	ImagePainter(String title){
		JFrame frame = new JFrame(title);
		frame.setBounds(100,300,500,300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);

		JLabel lb = new JLabel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawOval(point.x-3,point.y-3,6,6);
			}
		};
		ImageIcon icon = new ImageIcon("D:/ゆうき/キャプチャ1.jpg");
		lb.setIcon(icon);
		lb.addMouseListener(this);
		this.add(lb);


		JButton btn = new JButton("保存");
		btn.addActionListener(this);
		this.add(btn);

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
