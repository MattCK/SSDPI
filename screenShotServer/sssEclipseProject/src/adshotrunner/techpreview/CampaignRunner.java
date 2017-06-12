package adshotrunner.techpreview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import adshotrunner.AdShot;
import adshotrunner.AdShotter3;
import adshotrunner.StoryFinder;
import adshotrunner.TagImage;
import adshotrunner.powerpoint.CampaignPowerPoint;
import adshotrunner.system.ASRProperties;
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
		
		//Store the current time to show total runtime at end
		long campaignStartTime = System.nanoTime();
		
		System.out.println("\n\n\nImages: " + requestInfo.tagImages + "\n\n\n");
		
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
			TagImage currentCreative = null;
			while ((!tagRetrieved) && (tagRetrievalAttempts < 3)) {
				try {
					currentCreative = TagImage.create(tagURL); 
					tagRetrieved = true;
				}
				catch (Exception e) {
					++tagRetrievalAttempts;
					System.out.println("Creative create error: " + e);	
					e.printStackTrace();
				}
			}
			
			if (currentCreative != null) {
				tagImages.add(currentCreative);
			}
		}
				
		//Loop through each page, preparing the adshots and finding stories as necessary
		ArrayList<AdShot> adShotList = new ArrayList<AdShot>();
		for (Map<String, String> currentPage : requestInfo.pages) {
			
			//Put the values into easier to understand variables
			String pageURL = currentPage.get("url");
			List<String> alternateURLs = new ArrayList<String>();
			boolean findStory = (currentPage.get("findStory").equals("1"));
			boolean onlyScreenshot = (currentPage.get("onlyScreenshot").equals("1"));
			boolean individualTagScreenshots = (currentPage.get("individualTagScreenshots").equals("1"));
			System.out.println("Request URL: " + pageURL);
			
			//If finding a story is requested, set the page URL to the found story
			if (findStory) {
				ArrayList<String> foundStories = new ArrayList<String>();
				try {
					foundStories = new StoryFinder(URLTool.setProtocol("http",pageURL)).Scorer().getStories(3);
					System.out.println("StoryFinder found: " + foundStories);
				} catch (Exception e) {
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
				newAdShot.useMobile(currentPage.get("useMobile").equals("1"));
				adShotList.add(newAdShot);
			}
			
			else if (individualTagScreenshots) {
				for (TagImage singleTag: tagImages) {
					AdShot newAdShot = AdShot.create(pageURL, singleTag);
					if (!alternateURLs.isEmpty()) {newAdShot.addAlternatePageURL(alternateURLs);}
					newAdShot.useMobile(currentPage.get("useMobile").equals("1"));
					adShotList.add(newAdShot);
				}
			}
			
			else {
				AdShot newAdShot = AdShot.create(pageURL, tagImages);
				if (!alternateURLs.isEmpty()) {newAdShot.addAlternatePageURL(alternateURLs);}
				newAdShot.useMobile(currentPage.get("useMobile").equals("1"));
				adShotList.add(newAdShot);
			}
		}
		
		//Put the ad shots into standard and mobile lists for clarification
		List<AdShot> standardAdShots = new ArrayList<AdShot>();
		List<AdShot> mobileAdShots = new ArrayList<AdShot>();
		for (AdShot currentAdShot : adShotList) {
			if (currentAdShot.mobile()) {mobileAdShots.add(currentAdShot);}
			else {standardAdShots.add(currentAdShot);}
		}
		
		//Store the post page selection, story finder time
		long postPageSelectionTime = System.nanoTime();
		
		//Get the screenshots
		long standardAdShotRuntime = 0;
		long mobileAdShotRuntime = 0;
		if (standardAdShots.size() > 0) {standardAdShotRuntime = AdShotter3.create().takeAdShots(standardAdShots);}
		if (mobileAdShots.size() > 0) {mobileAdShotRuntime = AdShotter3.createForMobile().takeAdShots(mobileAdShots);}
		
		//Store the post AdShots time
		long postAdShotsTime = System.nanoTime();
		
		//Upload them
		int imageIndex = 1;
		HashMap<String, String>  pageAndScreenshotURLs = new LinkedHashMap<String, String>();
		for (AdShot currentAdShot : adShotList) {
			System.out.println("Saving a screenshot");
			System.out.println("URL: " + currentAdShot.finalURL());
			String imageFilename = requestInfo.jobID + "-" + imageIndex + ".png";		
			
			try {
				saveImageAsPNG(currentAdShot.image(), ASRProperties.pathForTemporaryFiles() + imageFilename);
				FileStorageClient.saveFile(ASRProperties.containerForScreenshots(), 
						ASRProperties.pathForTemporaryFiles() + imageFilename, imageFilename);
			}
			catch (Exception e) {
				System.out.println("Could not save screenshot");
				System.out.println(e);
			}
			++imageIndex;
			pageAndScreenshotURLs.put("https://s3.amazonaws.com/" + ASRProperties.containerForScreenshots() + 
									  "/" + imageFilename, 
									  currentAdShot.finalURL());
		
			//Delete the local file
			File screenshotFile = new File(ASRProperties.pathForTemporaryFiles() + imageFilename);
			screenshotFile.delete();
		}
		
		//Create the powerpoint filename
		System.out.println("Creating powerpoint");
		String pptxCustomerName = requestInfo.customer.trim().replace(' ', '-');
		String pptxDate = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime());
		String pptxTimestamp = Long.toString(Instant.now().getEpochSecond());
		pptxTimestamp = pptxTimestamp.substring(pptxTimestamp.length() - 6);
		String pptxFilename = pptxCustomerName + "-" + pptxDate + "-" + pptxTimestamp + ".pptx";
		
		//Get the background file. On fail, use the default one from the config folder.
		String backgroundFilename = ASRProperties.pathForTemporaryFiles() + requestInfo.powerPointBackground;
		File backgroundFile;
		String titleDate = new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime());
		System.out.println("PPTX Background:" + requestInfo.powerPointBackground);
		try {
			URL backgroundURL = new URL("https://s3.amazonaws.com/" + ASRProperties.containerForPowerPointBackgrounds() + 
									   "/" + requestInfo.powerPointBackground);
			backgroundFile = new File(backgroundFilename);		
			FileUtils.copyURLToFile(backgroundURL, backgroundFile);
		} catch (Exception e) {
			backgroundFilename = ASRProperties.pathForDefaultBackground();
			backgroundFile = new File(backgroundFilename);
			System.out.println("Failed getting background file");
			System.out.println("URL: " + "https://s3.amazonaws.com/" + ASRProperties.containerForPowerPointBackgrounds() + 
					   		   "/" + requestInfo.powerPointBackground);
		}
		
		//Create the powerpoint and add each AdShot as a slide
		try {
			
			//Font color is passed in with 6 digit hex value in string format
			CampaignPowerPoint powerPoint = new CampaignPowerPoint(requestInfo.customer, titleDate, requestInfo.powerPointFontColor, backgroundFile);
			for (AdShot currentAdShot : adShotList) {
				powerPoint.addSlide(currentAdShot);
			}
			powerPoint.save(ASRProperties.pathForTemporaryFiles() + pptxFilename);
			FileStorageClient.saveFile(ASRProperties.containerForPowerPoints(), 
									   ASRProperties.pathForTemporaryFiles() + pptxFilename, pptxFilename);
			
			//Delete the local file
			File powerPointFile = new File(ASRProperties.pathForTemporaryFiles() + pptxFilename);
			powerPointFile.delete();
			
		} catch (Exception e) {
			System.out.println("Could not create powerpoint");
		}
		System.out.println("Done with powerpoint");
		System.out.println("Results page: https://" + ASRProperties.asrDomain() + "/campaignResults.php?jobID=" + requestInfo.jobID);
		
		//Output the total campaign runtime
		long postUploadTime = System.nanoTime();
		long totalRuntime = ((postUploadTime - postAdShotsTime) + 
							  standardAdShotRuntime + mobileAdShotRuntime + 
							 (postPageSelectionTime - campaignStartTime))/1000000000;
		System.out.println("Total Campaign Runtime: " + totalRuntime + " s");

		//Create and upload the job result
		CampaignResult jobResult = new CampaignResult();
		jobResult.jobID = requestInfo.jobID;
		jobResult.queued = false;
		jobResult.success = true;
		jobResult.message = "";
		
		jobResult.customer = requestInfo.customer;
		jobResult.domain = requestInfo.domain;
		jobResult.date = new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime());
		
		jobResult.screenshots = pageAndScreenshotURLs;
		jobResult.powerPointURL = "https://s3.amazonaws.com/" + ASRProperties.containerForPowerPoints() +  
								  "/" + pptxFilename;
		jobResult.zipURL = "";
		
		jobResult.runtime = totalRuntime;
		
		//Upload the job result
		try {
			FileUtils.writeStringToFile(new File(ASRProperties.pathForTemporaryFiles() + jobResult.jobID), 
										jobResult.toJSON());
		} catch (IOException e) {
			System.out.println("Could not save job result");
		}
		FileStorageClient.saveFile(ASRProperties.containerForCampaignJobs(), 
								   ASRProperties.pathForTemporaryFiles() + jobResult.jobID, jobResult.jobID);
		
		//Delete the local file
		File jobResultFile = new File(ASRProperties.pathForTemporaryFiles() + jobResult.jobID);
		jobResultFile.delete();

		//Send the notification email
		CampaignEmail resultsEmail = CampaignEmail.createCampaignEmail(
										 requestInfo.customer, 
										 requestInfo.domain, 
										 new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime()), 
										 "https://" + ASRProperties.asrDomain() + "/campaignResults.php?jobID=" + requestInfo.jobID, 
										 jobResult.powerPointURL, 
										 adShotList);
		resultsEmail.send(requestInfo.email);

	}
	
	private static void saveImageAsPNG(BufferedImage imageToSave, String filepath) throws IOException {
		
		//Write the image as a PNG
		ImageIO.write(imageToSave, "png", new File(filepath));         	
	}

}
