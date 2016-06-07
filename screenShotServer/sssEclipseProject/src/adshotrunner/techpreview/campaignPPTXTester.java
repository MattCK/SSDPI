package adshotrunner.techpreview;

public class campaignPPTXTester {

	public static void main(String[] args) {

		String BackgroundImagePath = "/home/matt/Work/DangerousPenguins/localTesting/shots/back1.jpg";
		String CampaignName = "Boston Globe Red Ads";
		String AspectRatio = "16x9";
		String ScreenshotPath01 = "/home/matt/Work/DangerousPenguins/localTesting/shots/ss01.jpg";
		String ScreenshotPath02 = "/home/matt/Work/DangerousPenguins/localTesting/shots/ss02.png";
		String ScreenshotPath03 = "/home/matt/Work/DangerousPenguins/localTesting/shots/ss03.png";
		String ScreenshotUrl01 = "http://www.boston.com/";
		String ScreenshotUrl02 = "http://www.desmoinesregister.com/story/money/business/new-business/2016/04/05/gilroys-restaurant-court-avenue-brewing-west-des-moines/82662480/";
		String ScreenshotUrl03 = "http://www.newsweek.com/indie-amnesty-hashtag-music-sins-444622";
		String PowerPointOutPath = "/home/matt/Work/DangerousPenguins/localTesting/pptxTests/ppTester43.pptx";
		Boolean slideWorked = false;
		
		CampaignPowerPointGenerator CampaignPowerPoint = new CampaignPowerPointGenerator(BackgroundImagePath, AspectRatio, CampaignName);
		slideWorked = CampaignPowerPoint.AddScreenshotSlide(ScreenshotUrl01, ScreenshotPath01);
		System.out.println("Adding slide 1 Worked?" + slideWorked.toString());
		slideWorked = CampaignPowerPoint.AddScreenshotSlide(ScreenshotUrl02, ScreenshotPath02);
		System.out.println("Adding slide 2 Worked?" + slideWorked.toString());
		slideWorked = CampaignPowerPoint.AddScreenshotSlide(ScreenshotUrl03, ScreenshotPath03);
		System.out.println("Adding slide 3 Worked?" + slideWorked.toString());
		CampaignPowerPoint.SaveCampaignPowerPoint(PowerPointOutPath);

	}

}
