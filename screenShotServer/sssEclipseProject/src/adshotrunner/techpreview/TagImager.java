package adshotrunner.techpreview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import adshotrunner.AdShot;
import adshotrunner.AdShotter3;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.FileStorageClient;

public class TagImager implements Runnable {

	final public static String TAGPAGESPATH = "http://s3.amazonaws.com/" + ASRProperties.containerForTagPages() + "/";
	final public static int TAGRETRYCOUNT = 2;
	
	private Map<String, String> urlsWithIDs;
	private Thread imagerThread;
	
	TagImager(Map<String, String> urls) {
		urlsWithIDs = urls;
		imagerThread = new Thread(this);
		imagerThread.start();
	}
	
	@Override
	public void run() {
		System.out.println("\nIn Creativer - " + urlsWithIDs);
		System.out.println();
		
		AdShotter3 tagShotter = AdShotter3.createForTags();
		
		HashMap<String, AdShot> adShotsByIDMap = new HashMap<String, AdShot>();
		for(Map.Entry<String, String> currentURLSet : urlsWithIDs.entrySet()) {
			adShotsByIDMap.put(currentURLSet.getKey(), AdShot.create(TAGPAGESPATH + currentURLSet.getValue()));
			System.out.println("AdShot URL: " + adShotsByIDMap.get(currentURLSet.getKey()).url());
		}
		
		tagShotter.takeAdShots(new ArrayList<AdShot>(adShotsByIDMap.values()));
		
		//Check if any processed images are white
//		ArrayList<AdShot> whiteAdShots = new ArrayList<AdShot>();
//		for (AdShot currentAdShot : adShotsByIDMap.values()) {
//			if ((currentAdShot.image() != null) && 
//				(numberOfColorsInImage(currentAdShot.image()) == 1)) {
//				whiteAdShots.add(currentAdShot);
//			}
//			
//		}
//		if (whiteAdShots.size() > 0) {
//			tagShotter.takeAdShots(whiteAdShots);
//		}
		
		//Retry any tags that returned a white image or no image
		int retryAttempts = 0;
		ArrayList<AdShot> adShotsWithoutImages = getAdShotsWithoutImages(new ArrayList<AdShot>(adShotsByIDMap.values()));
		while ((adShotsWithoutImages.size() > 0) && (retryAttempts < TAGRETRYCOUNT)) {
			System.out.println("Retrying tags without images: " + adShotsWithoutImages.size());
			tagShotter.takeAdShots(adShotsWithoutImages);
			adShotsWithoutImages = getAdShotsWithoutImages(adShotsWithoutImages);
			++retryAttempts;
		}
		
		
		for(Map.Entry<String, AdShot> adShotEntry : adShotsByIDMap.entrySet()) {
			System.out.println("Saving a screenshot - URL: " + adShotEntry.getValue().url());
			String imageFilename = adShotEntry.getKey() + ".png";
			
			//If the AdShotter returned a null image, upload the error image
			if (adShotEntry.getValue().image() == null) {
				try {
					FileStorageClient.saveFile(ASRProperties.containerForCreativeImages(), 
											   "images/imageProcessingError.png", imageFilename);
	
				}
				catch (Exception e) {
					System.out.println("Could not save processing error image");
				}
			}
			
			//Otherwise, upload the processed image
			else {
				try {
					saveImageAsPNG(adShotEntry.getValue().image(), ASRProperties.pathForTemporaryFiles() + imageFilename);
					FileStorageClient.saveFile(ASRProperties.containerForCreativeImages(), 
											   ASRProperties.pathForTemporaryFiles() + imageFilename, imageFilename);
					
					//Delete the local file
					File tagImageFile = new File(ASRProperties.pathForTemporaryFiles() + imageFilename);
					tagImageFile.delete();
	
				}
				catch (Exception e) {
					System.out.println("Could not save screenshot");
				}
			}
		}
	}
	
	private static void saveImageAsPNG(BufferedImage imageToSave, String filepath) throws IOException {
		
		//Write the image as a PNG
		ImageIO.write(imageToSave, "png", new File(filepath));         	
	}
	
	private static int numberOfColorsInImage(BufferedImage image) {
//		byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		Set<Integer> colors = new HashSet<Integer>();
	    int imageWidth = image.getWidth();
	    int imageHeight = image.getHeight();
	    for(int yPosition = 0; yPosition < imageHeight; yPosition++) {
	        for(int xPosition = 0; xPosition < imageWidth; xPosition++) {
	            int pixel = image.getRGB(xPosition, yPosition);     
	            colors.add(pixel);
	        }
	    }
		System.out.println("in get colors");
		System.out.println("Color amount: " + colors.size());
		return colors.size();
	}
	
	private static ArrayList<AdShot> getAdShotsWithoutImages(ArrayList<AdShot> adShotsList) {
		ArrayList<AdShot> adShotsWithoutImages = new ArrayList<AdShot>();
		for (AdShot currentAdShot : adShotsList) {
			if (currentAdShot.image() == null) {
				adShotsWithoutImages.add(currentAdShot);
			}
			else if (numberOfColorsInImage(currentAdShot.image()) == 1) {
				adShotsWithoutImages.add(currentAdShot);
			}
		}
		return adShotsWithoutImages;
	}
}
