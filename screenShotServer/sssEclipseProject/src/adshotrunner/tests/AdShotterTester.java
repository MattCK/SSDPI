package adshotrunner.tests;

import java.util.HashSet;
import java.util.Set;

import adshotrunner.campaigns.AdShot;
import adshotrunner.campaigns.Campaign;
import adshotrunner.campaigns.Creative;
import adshotrunner.dispatcher.CampaignRunner;
import adshotrunner.dispatcher.TagImager;
import adshotrunner.shotter.AdShotter;
import adshotrunner.shotter.TagShotter;

public class AdShotterTester {

	public static void main(String[] args) throws Exception {

		Campaign campaignToRun = Campaign.getCampaign(20);
		campaignToRun.generatePowerPoint();
		System.out.println(campaignToRun.powerPointURL());
		
//		System.out.println(campaignToRun.powerPointURL());
//		new CampaignRunner2(campaignToRun);
//	
//		//Print out the statuses
//		for (AdShot2 currentAdShot : campaignToRun.adShots()) {
//			System.out.println(currentAdShot.status() + ": " + currentAdShot.finalURL());
//			System.out.println(currentAdShot.imageURL());
//			System.out.println("Injected: " + currentAdShot.injectedCreatives().size() + "/" + currentAdShot.creatives().size());
//			System.out.println();
//		}

//		Creative creative1 = Creative.getCreative(6);
//		Creative creative2 = Creative.getCreative(7);
//		Creative creative3 = Creative.getCreative(8);
//		
//		Creative creative1b = Creative.getCreative(6);
//		Creative creative2b = Creative.getCreative(7);
//		Creative creative3b = Creative.getCreative(8);
//		
//		System.out.println("6 == 6: " + (creative1 == creative1b));
//		
//		Set<Creative> creatives = new HashSet<Creative>();
//		creatives.add(creative1);
//		creatives.add(creative2);
//		creatives.add(creative3);
//		creatives.add(creative1b);
//		creatives.add(creative2b);
//		creatives.add(creative3b);
//		System.out.println("Final Size: " + creatives.size());
		
		
//		Set<Creative> creativeSet = new HashSet<Creative>();
//		creativeSet.add(creative1);
//		creativeSet.add(creative2);
//		creativeSet.add(creative3);
//		
//		new TagImager2(creativeSet);
		
//		TagShotter.captureTagImages(creativeSet);
//		
//		for (Creative currentCreative : creativeSet) {
//			System.out.println(currentCreative.status() + ": " + currentCreative.imageURL());
//		}
		
//		Campaign testCampaign = Campaign.getCampaign(15);
//		AdShotter4.captureAdShotImages(testCampaign.adShots());
//		
//		//Print out the statuses
//		for (AdShot2 currentAdShot : testCampaign.adShots()) {
//			System.out.println(currentAdShot.status() + ": " + currentAdShot.finalURL());
//			System.out.println(currentAdShot.imageURL());
//			System.out.println("Injected: " + currentAdShot.injectedCreatives().size() + "/" + currentAdShot.creatives().size());
//			System.out.println();
//		}
		
		
	}

}
