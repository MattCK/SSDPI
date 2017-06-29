package adshotrunner.tests;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import adshotrunner.campaigns.AdShot;
import adshotrunner.campaigns.Creative;

public class AdShot2Tester {

	public static void main(String[] args) {

		AdShot testAdShot = AdShot.getAdShot(18);
		
		
		
//		System.out.println("Creatives: " + testAdShot.creatives());
//		System.out.println("Injected Creatives: " + testAdShot.injectedCreatives());
//		System.out.println("ID: " + testAdShot.id());
//		System.out.println("UUID: " + testAdShot.uuid());
//		System.out.println("Campaign ID: " + testAdShot.campaignID());
//		System.out.println("Requested URL: " + testAdShot.requestedURL());
//		System.out.println("Story Finder: " + testAdShot.storyFinder());
//		System.out.println("Mobile: " + testAdShot.mobile());
//		System.out.println("Below-the-Fold: " + testAdShot.belowTheFold());
//		System.out.println("Final URL: " + testAdShot.finalURL());
//		System.out.println("Page Title: " + testAdShot.pageTitle());
//		System.out.println("Image Filename: " + testAdShot.imageFilename());
//		System.out.println("Image URL: " + testAdShot.imageURL());
//		System.out.println("Width: " + testAdShot.width());
//		System.out.println("Height: " + testAdShot.height());
//		System.out.println("Status: " + testAdShot.status());
//		System.out.println("Error Message: " + testAdShot.errorMessage());
//		System.out.println("Created Timestamp: " + testAdShot.createdTimestamp());
//		System.out.println("Processing Timestamp: " + testAdShot.processingTimestamp());
//		System.out.println("Finished Timestamp: " + testAdShot.finishedTimestamp());
//		System.out.println("Error Timestamp: " + testAdShot.errorTimestamp());
//		System.out.println();
//
//		System.out.println("PROCESSING");
//		testAdShot.setStatus(AdShot2.PROCESSING);
//		System.out.println(testAdShot.status());
//		System.out.println(testAdShot.createdTimestamp());
//		System.out.println(testAdShot.processingTimestamp());
//		System.out.println(testAdShot.finishedTimestamp());
//		System.out.println(testAdShot.errorTimestamp());
//		System.out.println();
//
//		System.out.println("FINISHED");
//		testAdShot.setStatus(AdShot2.FINISHED);
//		System.out.println(testAdShot.status());
//		System.out.println(testAdShot.createdTimestamp());
//		System.out.println(testAdShot.processingTimestamp());
//		System.out.println(testAdShot.finishedTimestamp());
//		System.out.println(testAdShot.errorTimestamp());
//		System.out.println();
//
//		System.out.println("ERROR");
//		testAdShot.setError("new error message");
//		System.out.println(testAdShot.status());
//		System.out.println(testAdShot.createdTimestamp());
//		System.out.println(testAdShot.processingTimestamp());
//		System.out.println(testAdShot.finishedTimestamp());
//		System.out.println(testAdShot.errorTimestamp());
//		System.out.println(testAdShot.errorMessage());
//		System.out.println();
//		
//		System.out.println("Setting image");
//		BufferedImage adShotImage = null; 
//		try {
//			URL imageURL = new URL("https://s3.amazonaws.com/asr-development/creativeimages/0014d0d8-e44f-472b-a2c2-29572763ffac.png");
//			adShotImage = ImageIO.read(imageURL); 
//		}
//        catch (IOException e) {
//        	e.printStackTrace();
//        }
//		
//		
//		
//		testAdShot.setImage(adShotImage);
//		System.out.println(testAdShot.imageFilename());
//		System.out.println(testAdShot.imageURL());
//		System.out.println(testAdShot.image());
//		System.out.println(testAdShot.width());
//		System.out.println(testAdShot.height());

//	
//		System.out.println("Final URL: " + testAdShot.finalURL());
//		System.out.println("Page Title: " + testAdShot.pageTitle());
//		testAdShot.setFinalURL("http://newfinalurl.com");
//		testAdShot.setPageTitle("New final page title");
//		System.out.println("Final URL: " + testAdShot.finalURL());
//		System.out.println("Page Title: " + testAdShot.pageTitle());
	
		
//		System.out.println("Requested URL: " + testAdShot.requestedURL());
//		System.out.println("Story Finder: " + testAdShot.storyFinder());
//		System.out.println("First Target URL: " + testAdShot.targetURL());
//
//		while (testAdShot.hasNextCandidateURL()) {
//			System.out.println("Next Target URL: " + testAdShot.nextCandidateURL());
//		}
	
//		System.out.println("Creatives: " + testAdShot.creatives());
//		System.out.println("Injected Creatives: " + testAdShot.injectedCreatives());
//		for (Creative tempCreative : testAdShot.creatives()) {
//			testAdShot.creativeInjected(tempCreative);
//		}
//		System.out.println("Creatives: " + testAdShot.creatives());
//		System.out.println("Injected Creatives: " + testAdShot.injectedCreatives());
	
	}

}
