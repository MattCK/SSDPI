package adshotrunner.techpreview;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class campaignPPTXTester {

	public static void main(String[] args) {

		String BackgroundImagePath = "/home/matt/Work/DangerousPenguins/localTesting/shots/back1.jpg";
		String CampaignName = "Boston Globe Red Ads";
		String AspectRatio = "16x9";
		String ScreenshotPath01 = "/home/matt/Work/DangerousPenguins/localTesting/shots/ss01.jpg";
		String ScreenshotPath02 = "/home/matt/Work/DangerousPenguins/localTesting/shots/ss02.png";
		String ScreenshotPath03 = "/home/matt/Work/DangerousPenguins/localTesting/shots/ss03.png";
		String ScreenshotPath04 = "/home/matt/Work/DangerousPenguins/localTesting/shots/ss04.png";
		String ScreenshotUrl01 = "http://www.boston.com/";
		String ScreenshotUrl02 = "http://www.desmoinesregister.com/story/money/business/new-business/2016/04/05/gilroys-restaurant-court-avenue-brewing-west-des-moines/82662480/";
		String ScreenshotUrl03 = "http://www.newsweek.com/indie-amnesty-hashtag-music-sins-444622";
		String ScreenshotUrl04 = "http://www.newsweek.com/indie-amnesty-hashtag-music-sins-444622http://www.newsweek.com/indie-amnesty-hashtag-music-sins-444622";
		String PowerPointOutPath = "/home/matt/Work/DangerousPenguins/localTesting/pptxTests/ppTester50.pptx";
		Boolean slideWorked = false;
		
		BufferedImage SSImage01 = null;
		BufferedImage SSImage02 = null;
		BufferedImage SSImage03 = null;
		BufferedImage SSImage04 = null;
		try {
			URL SSImage01URL = new URL("file:///" + ScreenshotPath01);
			SSImage01 = ImageIO.read(SSImage01URL);
		}
		catch (IOException e) {
        	System.out.println("IO Exception on reading the image");
        }
		try {
			URL SSImage02URL = new URL("file:///" + ScreenshotPath02);
			SSImage02 = ImageIO.read(SSImage02URL);
		}
		catch (IOException e) {
        	System.out.println("IO Exception on reading the image");
        }
		try {
			URL SSImage03URL = new URL("file:///" + ScreenshotPath03);
			SSImage03 = ImageIO.read(SSImage03URL);
		}
		catch (IOException e) {
        	System.out.println("IO Exception on reading the image");
        }
		try {
			URL SSImage04URL = new URL("file:///" + ScreenshotPath04);
			SSImage04 = ImageIO.read(SSImage04URL);
		}
		catch (IOException e) {
        	System.out.println("IO Exception on reading the image");
        }
		
		CampaignPowerPointGenerator CampaignPowerPoint = new CampaignPowerPointGenerator(BackgroundImagePath, AspectRatio, CampaignName, "000000");
		slideWorked = CampaignPowerPoint.AddScreenshotSlide(ScreenshotUrl01, SSImage01);
		System.out.println("Adding slide 1 Worked?" + slideWorked.toString());
		slideWorked = CampaignPowerPoint.AddScreenshotSlide(ScreenshotUrl02, SSImage02);
		System.out.println("Adding slide 2 Worked?" + slideWorked.toString());
		slideWorked = CampaignPowerPoint.AddScreenshotSlide(ScreenshotUrl03, SSImage03);
		System.out.println("Adding slide 3 Worked?" + slideWorked.toString());
		slideWorked = CampaignPowerPoint.AddScreenshotSlide(ScreenshotUrl04, SSImage04);
		System.out.println("Adding slide 4 Worked?" + slideWorked.toString());
		CampaignPowerPoint.SaveCampaignPowerPoint(PowerPointOutPath);

	}

}
