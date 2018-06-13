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


public class ImagePainter {


	public static void main(String[] args) {/*

		BufferedImage bufferImage = null;

		//	ファイル読み込み
		try
		{
    		  bufferImage = ImageIO.read(new File("D:/ゆうき/キャプチャ1.jpg"));
		} catch (Exception e) {
    		  e.printStackTrace();
		}

		Graphics graphics = bufferImage.createGraphics();

		//	いたずら書き
		graphics.setColor(Color.RED);
		graphics.drawString("いたずら書きだよー",0,20);

		//	ファイル保存
	    	try
    		{
        		  ImageIO.write(bufferImage, "jpeg", new File("test.jpg"));
    		}
    		catch (Exception e)
    		{
        		  e.printStackTrace();
    		}

    		System.out.println("終わりました");*/


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
	}
}
