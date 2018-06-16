package icg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import icg.frame.GraphLabel;

public class Dispersion_GraphPainter extends GraphPainter {
	int i=0;

	public Dispersion_GraphPainter(GraphLabel lb) {
		super(lb);
	}

	public void setMapImage(File ImageFile) {
		try {
			this.labelImage = ImageIO.read(ImageFile);
			setParameter(paintingLabel.getSize().width, paintingLabel.getSize().height, labelImage.getWidth(), labelImage.getHeight());
			System.out.println(labelImage.getHeight());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void makeGraph(Graphics2D g2) {
		g2.setColor(Color.black);
		if(paintingLabel.isCliked) {
			//BufferedImage上でクリックされた位置を計算
			int clikedPointx =(int) ((this.paintingLabel.clickedPoint.x - this.ImageOriginPoint.x)/scaleToLabel);
			int clikedPointy = (int) ((this.paintingLabel.clickedPoint.y - this.ImageOriginPoint.y)/scaleToLabel);
			g2.drawLine(clikedPointx, clikedPointy, 50, 100);
		}
	}

	@Override
	public void setData(double[][] data) {

	}



	@Override
	public void savePaint() {
		/*
		try (FileImageOutputStream output = new FileImageOutputStream(new File("D:/ゆうき/AAA.jpg"))) {

			BufferedImage readImage = ImageIO.read(new File("D:/ゆうき/キャプチャ10.jpg"));

			Graphics graphics = readImage.createGraphics();

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
		*/
	}
}
