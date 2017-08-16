package adshotrunner.tests;

import adshotrunner.campaigns.Campaign;
import adshotrunner.dispatcher.CampaignEmail;
import adshotrunner.storyfinder.StoryLinkRetriever;

public class CampaignEmailTest {

	public static void main(String[] args) {

//		Campaign testCampaign = Campaign.getCampaign(217);
//		CampaignEmail resultsEmail = CampaignEmail.createCampaignEmail(testCampaign);
//		resultsEmail.send(testCampaign.userEmailAddress());

		//System.out.println(StoryLinkRetriever.getStoryLinks("http://www.businessinsider.com"));
		System.out.println(StoryLinkRetriever.getStoryLinks("http://www.observer.com"));
		
		

//		Creative creative144 = Creative.getCreative(144);
//		Creative creative146 = Creative.getCreative(146);
//		Creative creative10 = Creative.getCreative(10);
//		Creative creative11 = Creative.getCreative(11);
//		Creative creative12 = Creative.getCreative(12);
//		Creative creative13 = Creative.getCreative(13);
//		Creative creative14 = Creative.getCreative(14);
//		Creative creative15 = Creative.getCreative(15);
//		Creative creative16 = Creative.getCreative(16);
//		Creative creative17 = Creative.getCreative(17);
//		Creative creative18 = Creative.getCreative(18);
//		Creative creative19 = Creative.getCreative(19);
//		Set<Creative> someSet = new TreeSet<Creative>();
//		someSet.add(creative10);
//		someSet.add(creative11);
//		someSet.add(creative12);
//		someSet.add(creative13);
//		someSet.add(creative14);
//		someSet.add(creative15);
//		someSet.add(creative16);
//		someSet.add(creative17);
//		someSet.add(creative18);
//		someSet.add(creative19);
//		someSet.add(creative10);
//		someSet.add(creative11);
//		someSet.add(creative12);
//		someSet.add(creative13);
//		someSet.add(creative14);
//		someSet.add(creative15);
//		someSet.add(creative16);
//		someSet.add(creative17);
//		someSet.add(creative18);
//		someSet.add(creative19);
//		System.out.println("Set size: " + someSet.size());
//		for (Creative currentCreative: someSet) {
//			System.out.println(currentCreative.id() + ": " + currentCreative.priority());
//		}
		
//		Campaign testCampaign = Campaign.getCampaign(46);
//		
//		for (AdShot currentAdShot : testCampaign.adShots()) {
//			System.out.println(currentAdShot.finalURL() + ": " + currentAdShot.injectedCreatives().size());
//			if (currentAdShot.injectedCreatives().size() > 0) {
//				for (Creative currentCreative: currentAdShot.injectedCreatives()) {
//					System.out.println("    - " + currentCreative.id() + ": " + currentCreative.width() + "x" + currentCreative.height());
//				}
//			}
//			else {System.out.println("    - None");}
//		}
//		
//		CampaignEmail email = CampaignEmail.createCampaignEmail(testCampaign);
//		email.getInjectedCreativeNames();
	}

}
