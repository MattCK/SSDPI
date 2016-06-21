package adshotrunner.techpreview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import adshotrunner.AdShotter;
import adshotrunner.utilities.FileStorageClient;

public class CampaignRunner implements Runnable {
	
	Thread imagerThread;
	ScreenshotRequest requestInfo;
	int testInt;
	
	CampaignRunner(ScreenshotRequest passedRequest) {
		requestInfo = passedRequest;
		String temp = requestInfo.jobID;
		testInt = 3;
		imagerThread = new Thread(this);
		imagerThread.start();
	}
	
	@Override
	public void run() {
		
		//Create the AdShotter to use
		AdShotter campaignShotter = new AdShotter();
		System.out.println(testInt);
		System.out.println(requestInfo);
		System.out.println(requestInfo.jobID);
		
		//Loop through each page, adding the tags and finding stories as necessary
		for (Map<String, String> currentPage : requestInfo.pages) {
			
			//Put the values into easier to understand variables
			String pageURL = currentPage.get("url");
			boolean findStory = (currentPage.get("findStory").equals("1"));
			boolean onlyScreenshot = (currentPage.get("onlyScreenshot").equals("1"));
			
			//Add the pages and tags to the ScreenShotter
			//If only a screenshot needs to be taken, add the URL with a stud tag image
			if (onlyScreenshot) {
				campaignShotter.addTag(pageURL, "https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-1x1.jpg");
			}
			
			//Otherwise, loop through the tags and add them
			else {
				for (int tagIndex = 0; tagIndex < requestInfo.tagImages.size(); tagIndex++) {
					campaignShotter.addTag(pageURL, requestInfo.tagImages.get(tagIndex), tagIndex);
				}
			}
		}
	
		//Get the screenshots
		Map<String, BufferedImage> screenshots = campaignShotter.getAdShots();	
		
		//Upload them
		int imageIndex = 1;
		for(Map.Entry<String, BufferedImage> currentScreenshot : screenshots.entrySet()) {
			String imageFilename = requestInfo.jobID + "-" + imageIndex + ".png";		
			
			try {
				saveImageAsPNG(currentScreenshot.getValue(), "screenshots/" + imageFilename);
				FileStorageClient.saveFile(FileStorageClient.SCREENSHOTSCONTAINER, "screenshots/" + imageFilename, imageFilename);
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
