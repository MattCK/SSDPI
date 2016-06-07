package adshotrunner.techpreview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import adshotrunner.AdShotter;
import adshotrunner.utilities.FileStorageClient;

public class TagImager implements Runnable {

	Map<String, String> urlsWithIDs;
	Thread imagerThread;
	
	TagImager(Map<String, String> urls) {
		urlsWithIDs = urls;
		imagerThread = new Thread(this);
		imagerThread.start();
	}
	
	@Override
	public void run() {
		System.out.println("\nIn TagImager - " + urlsWithIDs);
		System.out.println();
		/*for(Map.Entry<String, String> currentURLSet : urlsWithIDs.entrySet()) {
			System.out.println(currentURLSet.getKey() + ": " + currentURLSet.getValue());
		}*/
		AdShotter tagShotter = new AdShotter(50, 30);
		//AdShotter tagShotter = new AdShotter(550, 300);
		
		for (String currentURL : urlsWithIDs.values()) {
			tagShotter.addTag("s3.amazonaws.com/asr-tagpages/" + currentURL, "https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-1x1.jpg");
		}
		
		Map<String, BufferedImage> tagShots = tagShotter.getAdShots();	
		System.out.println("AdShot URLS - \n\n");
		System.out.println(tagShots.keySet());
		System.out.println();
		
		for(Map.Entry<String, String> currentURLSet : urlsWithIDs.entrySet()) {
			System.out.println("Saving a screenshot");
			System.out.println("URL: " + currentURLSet.getValue());
			BufferedImage adShotImage = tagShots.get("s3.amazonaws.com/asr-tagpages/" + currentURLSet.getValue());
			int width = adShotImage.getWidth();
			int height = adShotImage.getHeight();
			BufferedImage croppedAdClip = adShotImage.getSubimage(425, 75, width - 425, height - 75);
			String imageFilename = currentURLSet.getKey() + ".png";
			//String imageFilename = System.nanoTime() + ".png";
			try {
				saveImageAsPNG(croppedAdClip, "tagImages/" + imageFilename);
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
