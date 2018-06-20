package icg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

import icg.frame.GraphLabel;

public class Dispersion_GraphPainter extends GraphPainter {
	protected Point scaleP1,scaleP2,launchP;

	protected int dispersionPaintState = 0;
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
		if(paintingLabel.isClicked) {
			//saveImage上でクリックされた位置を計算
			Point clickedImagePoint = convertCoordinate_LabelToImage();
			g2.drawLine(clickedImagePoint.x, clickedImagePoint.y, 50, 100);
		}
	}

	@Override
	protected void makeTemporaryGraph(Graphics2D g2) {
		if(paintingLabel.isClicked) {
			g2.setColor(Color.red);
			g2.fillOval(paintingLabel.clickedPoint.x-4, paintingLabel.clickedPoint.y-4, 8, 8);


			if(scaleP1 == null) {
				scaleP1 = paintingLabel.clickedPoint;
			}else if(scaleP2 == null) {
				scaleP2 = paintingLabel.clickedPoint;
			}else if() {

			}
		}
	}

	@Override
	public void setData(double[][] data) {

	}

	public void setReducedScale() {

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

	public void resetTemporaryImage() {
		this.temporaryImage = new BufferedImage(labelDimension.width, labelDimension.height, BufferedImage.TYPE_INT_ARGB);
	}
}
