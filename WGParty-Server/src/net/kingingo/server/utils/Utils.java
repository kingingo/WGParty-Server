package net.kingingo.server.utils;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

public class Utils {
	private static Random rand = new Random();
	
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
