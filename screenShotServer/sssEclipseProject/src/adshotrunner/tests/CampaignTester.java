package adshotrunner.tests;

import java.io.File;
import java.net.URLEncoder;

import org.apache.commons.lang.StringEscapeUtils;

import adshotrunner.campaigns.Campaign;
import adshotrunner.powerpoint.CampaignPowerPoint;

public class CampaignTester {

	public static void main(String[] args) throws Exception {

		String name = "JOHN&MARY";
		String formattedName = StringEscapeUtils.escapeHtml(name);
		
		
		new CampaignPowerPoint(formattedName, "01/01/2001", 
				   "ffffff", 
				   new File("DigilantBackground.png"));
		
//		Campaign testCampaign = Campaign.getCampaign(1);
//		System.out.println("ID: " + testCampaign.id());
//		System.out.println("UUID: " + testCampaign.uuid());
//		System.out.println("Customer Name: " + testCampaign.customerName());
//		System.out.println("AdShots: " + testCampaign.adShots());
//		System.out.println("PowerPoint Background: " + testCampaign.powerPointBackground());
//		System.out.println("PowerPoint Filename: " + testCampaign.powerPointFilename());
//		System.out.println("PowerPoint URL: " + testCampaign.powerPointURL());
//		System.out.println("Status: " + testCampaign.status());
//		System.out.println("Error Message: " + testCampaign.errorMessage());
//		System.out.println("Created Timestamp: " + testCampaign.createdTimestamp());
//		System.out.println("Queued Timestamp: " + testCampaign.queuedTimestamp());
//		System.out.println("Processing Timestamp: " + testCampaign.processingTimestamp());
//		System.out.println("Finished Timestamp: " + testCampaign.finishedTimestamp());
//		System.out.println("Error Timestamp: " + testCampaign.errorTimestamp());
//		System.out.println();
//		
//		System.out.println("QUEUED");
//		testCampaign.setStatus(Campaign.QUEUED);
//		System.out.println(testCampaign.status());
//		System.out.println(testCampaign.createdTimestamp());
//		System.out.println(testCampaign.processingTimestamp());
//		System.out.println(testCampaign.finishedTimestamp());
//		System.out.println(testCampaign.errorTimestamp());
//		System.out.println();
//
//		System.out.println("PROCESSING");
//		testCampaign.setStatus(Campaign.PROCESSING);
//		System.out.println(testCampaign.status());
//		System.out.println(testCampaign.createdTimestamp());
//		System.out.println(testCampaign.processingTimestamp());
//		System.out.println(testCampaign.finishedTimestamp());
//		System.out.println(testCampaign.errorTimestamp());
//		System.out.println();
//
//		System.out.println("FINISHED");
//		testCampaign.setStatus(Campaign.FINISHED);
//		System.out.println(testCampaign.status());
//		System.out.println(testCampaign.createdTimestamp());
//		System.out.println(testCampaign.processingTimestamp());
//		System.out.println(testCampaign.finishedTimestamp());
//		System.out.println(testCampaign.errorTimestamp());
//		System.out.println();
		
//		System.out.println(testCampaign.errorMessage());
//		System.out.println(testCampaign.errorTimestamp());
//		System.out.println("ERROR");
//		testCampaign.setError("new error message");
//		System.out.println(testCampaign.status());
//		System.out.println(testCampaign.createdTimestamp());
//		System.out.println(testCampaign.processingTimestamp());
//		System.out.println(testCampaign.finishedTimestamp());
//		System.out.println(testCampaign.errorTimestamp());
//		System.out.println(testCampaign.errorMessage());
//		System.out.println();
		
		
		
		
		
		
	}

}
