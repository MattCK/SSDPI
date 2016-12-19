package adshotrunner.techpreview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import adshotrunner.AdShot;
import adshotrunner.AdShotter;
import adshotrunner.AdShotter3;
import adshotrunner.utilities.FileStorageClient;

public class TagImager implements Runnable {

	final public static String TAGPAGESPATH = "http://s3.amazonaws.com/asr-tagpages/";

	private Map<String, String> urlsWithIDs;
	private Thread imagerThread;
	
	TagImager(Map<String, String> urls) {
		urlsWithIDs = urls;
		imagerThread = new Thread(this);
		imagerThread.start();
	}
	
	@Override
	public void run() {
		System.out.println("\nIn TagImager - " + urlsWithIDs);
		System.out.println();
		
		AdShotter3 tagShotter = AdShotter3.create();
		
		HashMap<String, AdShot> adShotsByIDMap = new HashMap<String, AdShot>();
		for(Map.Entry<String, String> currentURLSet : urlsWithIDs.entrySet()) {
			adShotsByIDMap.put(currentURLSet.getKey(), AdShot.create(TAGPAGESPATH + currentURLSet.getValue()));
			System.out.println("AdShot URL: " + adShotsByIDMap.get(currentURLSet.getKey()).url());
		}
		
		tagShotter.takeAdShots(new ArrayList<AdShot>(adShotsByIDMap.values()), true);	
		
		for(Map.Entry<String, AdShot> adShotEntry : adShotsByIDMap.entrySet()) {
			System.out.println("Saving a screenshot - URL: " + adShotEntry.getValue().url());
//			int width = adShotEntry.getValue().image().getWidth();
//			int height = adShotEntry.getValue().image().getHeight();
//			BufferedImage croppedAdClip = adShotEntry.getValue().image().getSubimage(425, 75, width - 425, height - 75);
			String imageFilename = adShotEntry.getKey() + ".png";
			try {
				saveImageAsPNG(adShotEntry.getValue().image(), "tagImages/" + imageFilename);
				FileStorageClient.saveFile(FileStorageClient.TAGIMAGESCONTAINER, "tagImages/" + imageFilename, imageFilename);
			}
			catch (Exception e) {
				System.out.println("Could not save screenshot");
			}
		}
	}
	
	private static void saveImageAsPNG(BufferedImage imageToSave, String filepath) throws IOException {
		
		//Write the image as a PNG
		ImageIO.write(imageToSave, "png", new File(filepath));         	
	}
}
