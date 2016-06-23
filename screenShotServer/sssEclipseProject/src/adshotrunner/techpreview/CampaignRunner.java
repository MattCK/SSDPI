package adshotrunner.techpreview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import adshotrunner.AdShotter;
import adshotrunner.StoryFinder;
import adshotrunner.utilities.FileStorageClient;
import adshotrunner.utilities.URLTool;

public class CampaignRunner implements Runnable {
	
	Thread imagerThread;
	CampaignRequest requestInfo;
	int testInt;
	
	CampaignRunner(CampaignRequest passedRequest) {
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
			System.out.println("Request URL: " + pageURL);
			
			//If finding a story is requested, set the page URL to the found story
			if (findStory) {
				String foundStoryURL = "";
				try {
					foundStoryURL = new StoryFinder(URLTool.setProtocol("http",pageURL)).Scorer().getStory();
					System.out.println("StoryFinder found: " + foundStoryURL);
				} catch (MalformedURLException | UnsupportedEncodingException
						| URISyntaxException e) {
					// TODO Auto-generated catch block
					System.out.println("Error finding story");
					e.printStackTrace();
				}
				
				if(foundStoryURL != null && !foundStoryURL.isEmpty()) {
					pageURL = URLTool.removeProtocol(foundStoryURL);
					System.out.println("Final page URL: " + pageURL);
					//Remove the possible protocol
					//pageURL = foundStoryURL.replace(/^http:\/\//, '');
				}
			}
			
			//Add the pages and tags to the ScreenShotter
			//If only a screenshot needs to be taken, add the URL with a stud tag image
			if (onlyScreenshot) {
				campaignShotter.addTag(pageURL, "https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-1x1.jpg");
			}
			
			//Otherwise, loop through the tags and add them
			else {
				for (int tagIndex = 0; tagIndex < requestInfo.tagImages.size(); tagIndex++) {
					campaignShotter.addTag(pageURL, URLTool.setProtocol("https", requestInfo.tagImages.get(tagIndex)), tagIndex);
				}
			}
		}
	
		//Get the screenshots
		Map<String, BufferedImage> screenshots = campaignShotter.getAdShots();	
		
		//Upload them
		int imageIndex = 1;
		HashMap<String, String>  pageAndScreenshotURLs = new HashMap<String, String>();
		for (Map.Entry<String, BufferedImage> currentScreenshot : screenshots.entrySet()) {
			System.out.println("Saving a screenshot");
			System.out.println("URL: " + currentScreenshot.getKey());
			String imageFilename = requestInfo.jobID + "-" + imageIndex + ".png";		
			
			try {
				saveImageAsPNG(currentScreenshot.getValue(), "screenshots/" + imageFilename);
				FileStorageClient.saveFile(FileStorageClient.SCREENSHOTSCONTAINER, "screenshots/" + imageFilename, imageFilename);
			}
			catch (Exception e) {
				System.out.println("Could not save screenshot");
			}
			++imageIndex;
			pageAndScreenshotURLs.put(currentScreenshot.getKey(), "https://s3.amazonaws.com/asr-screenshots/" + imageFilename);
		}
		
		//Create the powerpoint
		/*System.out.println("Creating powerpoint");
		CampaignPowerPointGenerator powerPoint = new CampaignPowerPointGenerator("back1.jpg", "16x9", "Campaign Name");
		for (Map.Entry<String, BufferedImage> currentScreenshot : screenshots.entrySet()) {
			powerPoint.AddScreenshotSlide(currentScreenshot.getKey(), currentScreenshot.getValue());
		}
		powerPoint.SaveCampaignPowerPoint("powerpoints/" + requestInfo.jobID + ".pptx");
		FileStorageClient.saveFile(FileStorageClient.POWERPOINTS, "powerpoints/" + requestInfo.jobID + ".pptx", requestInfo.jobID + ".pptx");
		System.out.println("Done with powerpoint");*/
		
		//Create and upload the job result
		CampaignResult jobResult = new CampaignResult();
		jobResult.jobID = requestInfo.jobID;
		jobResult.queued = false;
		jobResult.success = true;
		jobResult.message = "";
		jobResult.screenshots = pageAndScreenshotURLs;
		jobResult.powerPointURL = "";//"https://s3.amazonaws.com/asr-powerpoints/" + requestInfo.jobID + ".pptx";
		jobResult.zipURL = "";
		try {
			FileUtils.writeStringToFile(new File("campaignJobs/" + jobResult.jobID), jobResult.toJSON());
		} catch (IOException e) {
			System.out.println("Could not save job result");
		}
		FileStorageClient.saveFile(FileStorageClient.CAMPAIGNJOBS, "campaignJobs/" + jobResult.jobID, jobResult.jobID);
	}
	
	private static void saveImageAsPNG(BufferedImage imageToSave, String filepath) throws IOException {
		
		//Write the image as a PNG
		ImageIO.write(imageToSave, "png", new File(filepath));         	
	}

}
