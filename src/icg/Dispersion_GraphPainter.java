package icg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

import icg.frame.GraphLabel;

public class Dispersion_GraphPainter extends GraphPainter {
	int i = 0;

	protected final boolean hasTemporaryImage = true;

	public Dispersion_GraphPainter(GraphLabel lb) {
		super(lb);
	}

	public void setMapImage(File ImageFile) {
		try {
			this.saveImage = ImageIO.read(ImageFile);

			setParameter(paintingLabel.getSize().width, paintingLabel.getSize().height, saveImage.getWidth(), saveImage.getHeight());
			this.temporaryImage = new BufferedImage(labelDimension.width, labelDimension.height, BufferedImage.TYPE_INT_ARGB);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void makeSaveGraph(Graphics2D g2) {
		g2.setColor(Color.black);
		if(paintingLabel.isCliked) {
			//saveImage上でクリックされた位置を計算
			int clikedPointx =(int) ((this.paintingLabel.clickedPoint.x - this.ImageOriginPoint.x)/scaleToLabel);
			int clikedPointy = (int) ((this.paintingLabel.clickedPoint.y - this.ImageOriginPoint.y)/scaleToLabel);
			g2.drawLine(clikedPointx, clikedPointy, 50, 100);
		}
	}

	@Override
	protected void makeTemporaryGraph(Graphics2D g2) {
		if(paintingLabel.isCliked) {
			g2.setColor(Color.red);
			g2.fillOval(paintingLabel.clickedPoint.x-4, paintingLabel.clickedPoint.y-4, 8, 8);
		}
	}

	@Override
	public void setData(double[][] data) {

	}



	@Override
	public void savePaint(File saveFile) {
		try (FileImageOutputStream output = new FileImageOutputStream(saveFile)) {
			ImageIO.write(saveImage, "png", output);
			/*ImageWriter writeImage = ImageIO.getImageWritersByFormatName("jpg").next();
			ImageWriteParam writeParam = writeImage.getDefaultWriteParam();
			writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			writeParam.setCompressionQuality(1.0f);
			writeImage.setOutput(output);
			writeImage.write(null, new IIOImage(labelImage, null, null), writeParam);
			writeImage.dispose();
	//		*/
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}
}
