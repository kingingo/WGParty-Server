package net.kingingo.server.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

public class Utils {
	private static Random rand = new Random();
	
	public static void main(String[] a) {
		String path = "C:"+File.separatorChar+"Users"+File.separatorChar+"obena"+File.separatorChar+"git"+File.separatorChar+"wgparty"+File.separatorChar+"WGParty"+File.separatorChar+"src"+File.separatorChar+"images"+File.separatorChar+"profiles"+File.separatorChar;
		String[] names = new String[] {
				"Oskar",
				"Jonas",
				"Henrik",
				"Moritz",
				"Jonathan",
				"Leon"
		};
		for(String name : names)
			try {
				resize(new File(path+"original"+File.separatorChar+name+".png"),path+"resize"+File.separatorChar+UUID.nameUUIDFromBytes(name.getBytes()).toString()+".jpg",256,256);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	/**
     * Resizes an image to a absolute width and height (the image may not be
     * proportional)
     * @param inputImagePath Path of the original image
     * @param outputImagePath Path to save the resized image
     * @param scaledWidth absolute width in pixels
     * @param scaledHeight absolute height in pixels
     * @throws IOException
     */
    public static void resize(File inputFile,
            String outputImagePath, int scaledWidth, int scaledHeight)
            throws IOException {
        // reads input image
//        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);
 
        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, BufferedImage.TYPE_INT_RGB); //inputImage.getType()
 
        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        /** MUSS NICHT BEI JEDEN BILD ROTIERT WERDEN VLT GROEßEM ANHAENGIG!**/
//        g2d.rotate(Math.toRadians(90), scaledWidth/2, scaledHeight/2);
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
 
        // extracts extension of output file
//        String formatName = outputImagePath.substring(outputImagePath.lastIndexOf(".") + 1);
 
        // writes to output file
        ImageIO.write(outputImage, "jpg", new File(outputImagePath));
    }
 
    /**
     * Resizes an image by a percentage of original size (proportional).
     * @param inputImagePath Path of the original image
     * @param outputImagePath Path to save the resized image
     * @param percent a double number specifies percentage of the output image
     * over the input image.
     * @throws IOException
     */
    public static void resize(File inputFile,
            String outputImagePath, double percent) throws IOException {
//        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);
        int scaledWidth = (int) (inputImage.getWidth() * percent);
        int scaledHeight = (int) (inputImage.getHeight() * percent);
        resize(inputFile, outputImagePath, scaledWidth, scaledHeight);
    }
	
	public static int randInt(int min, int max) {
	    return rand.nextInt((max - min) + 1) + min;
	}
	
	public static void createDirectorie(String path) {
		new File(path).getParentFile().mkdirs();
	}
	
	public static Image toImage(String path) throws IOException {
		return ImageIO.read(new File(path));
	}
	
	public static File toFile(String path, byte[] bytearray) throws IOException {
		FileUtils.writeByteArrayToFile(new File(path), bytearray);
		return new File(path);
	}
	
	public static byte[] toBytes (String path) throws IOException {
		return  Files.readAllBytes(new File(path).toPath());
	}
	
	public static String toTime(long milis) {
		if(milis >= TimeSpan.DAY) {
			return (milis/TimeSpan.DAY)+" Tage "+toTime(milis%TimeSpan.DAY);
		} else {
			if(milis >= TimeSpan.HOUR) {
				return (milis/TimeSpan.HOUR)+":"+toTime(milis%TimeSpan.HOUR);
			} else {
				if(milis >= TimeSpan.MINUTE) {
					return (milis/TimeSpan.MINUTE)+":"+toTime(milis%TimeSpan.MINUTE);
				} else {
					if(milis >= TimeSpan.SECOND) {
						String s = toTime(milis%TimeSpan.SECOND);
						
						return (milis/TimeSpan.SECOND)+(s.isEmpty()?"":" "+s);
					} else {
						return (milis > 0 ? milis+"ms" : "");
					}
				}
			}
		}
	}
}
