package adshotrunner.techpreview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import adshotrunner.AdShot;
import adshotrunner.AdShotter;
import adshotrunner.AdShotter2;
import adshotrunner.StoryFinder;
import adshotrunner.TagImage;
import adshotrunner.utilities.FileStorageClient;
import adshotrunner.utilities.URLTool;

public class CampaignRunner implements Runnable {
	
	Thread imagerThread;
	CampaignRequest requestInfo;
	
	CampaignRunner(CampaignRequest passedRequest) {
		requestInfo = passedRequest;
		imagerThread = new Thread(this);
		imagerThread.start();
	}
	
	@Override
	public void run() {
		
		//Put the tag images into a list
		HashSet<TagImage> tagImages = new HashSet<TagImage>();
		for (String tagURL: requestInfo.tagImages) {
			
			//////////////////////////////////////////////////////////////
			/////// For some stupid god damn reason, calls to ///////////
			/////// S3 on AWS' own network can fail. For this ///////////
			/////// stud campaign runner and for the fact it  ///////////
			/////// is incredibly rare, we'll try to grab it  ///////////
			/////// a few times and otherwise not use it.     ///////////
			//////////////////////////////////////////////////////////////
			boolean tagRetrieved = false;
			int tagRetrievalAttempts = 0;
			TagImage currentTagImage = null;
			while ((!tagRetrieved) && (tagRetrievalAttempts < 3)) {
				try {
					currentTagImage = TagImage.create(tagURL); 
					tagRetrieved = true;
				}
				catch (Exception e) {
					++tagRetrievalAttempts;
				}
			}
			
			if (currentTagImage != null) {
				tagImages.add(currentTagImage);
			}
		}
		
		//Create the AdShotter to use
		AdShotter2 campaignShotter = new AdShotter2();
		System.out.println(requestInfo);
		
		//Loop through each page, preparing the adshots and finding stories as necessary
		ArrayList<AdShot> adShotList = new ArrayList<AdShot>();
		for (Map<String, String> currentPage : requestInfo.pages) {
			
			//Put the values into easier to understand variables
			String pageURL = currentPage.get("url");
			List<String> alternateURLs = new ArrayList<String>();
			boolean findStory = (currentPage.get("findStory").equals("1"));
			boolean onlyScreenshot = (currentPage.get("onlyScreenshot").equals("1"));
			System.out.println("Request URL: " + pageURL);
			
			//If finding a story is requested, set the page URL to the found story
			if (findStory) {
				ArrayList<String> foundStories = new ArrayList<String>();
				try {
					foundStories = new StoryFinder(URLTool.setProtocol("http",pageURL)).Scorer().getStories(3);
					System.out.println("StoryFinder found: " + foundStories);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Error finding story");
					e.printStackTrace();
				}
				
				if((!foundStories.isEmpty()) && (!foundStories.get(0).isEmpty())) {
					pageURL = URLTool.removeProtocol(foundStories.get(0));
					System.out.println("Final page URL: " + pageURL);
					
					if (foundStories.size() > 1) {
						alternateURLs = foundStories.subList(1, (foundStories.size() - 1));
					}
				}
			}
			
			if (onlyScreenshot) {
				AdShot newAdShot = AdShot.create(pageURL);
				if (!alternateURLs.isEmpty()) {newAdShot.addAlternatePageURL(alternateURLs);}
				adShotList.add(newAdShot);
			}
			
			else {
				AdShot newAdShot = AdShot.create(pageURL, tagImages);
				if (!alternateURLs.isEmpty()) {newAdShot.addAlternatePageURL(alternateURLs);}
				adShotList.add(newAdShot);
			}
		}
	
		//Get the screenshots
		campaignShotter.takeAdShots(adShotList);	
		
		//Upload them
		int imageIndex = 1;
		HashMap<String, String>  pageAndScreenshotURLs = new HashMap<String, String>();
		for (AdShot currentAdShot : adShotList) {
			System.out.println("Saving a screenshot");
			System.out.println("URL: " + currentAdShot.finalURL());
			String imageFilename = requestInfo.jobID + "-" + imageIndex + ".png";		
			
			try {
				saveImageAsPNG(currentAdShot.image(), "screenshots/" + imageFilename);
				FileStorageClient.saveFile(FileStorageClient.SCREENSHOTSCONTAINER, "screenshots/" + imageFilename, imageFilename);
			}
			catch (Exception e) {
				System.out.println("Could not save screenshot");
				System.out.println(e);
				//e.printStackTrace();
			}
			++imageIndex;
			pageAndScreenshotURLs.put(currentAdShot.finalURL(), "https://s3.amazonaws.com/asr-screenshots/" + imageFilename);
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
