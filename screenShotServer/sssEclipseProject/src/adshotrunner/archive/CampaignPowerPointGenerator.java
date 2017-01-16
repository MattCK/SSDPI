package adshotrunner.archive;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

public class CampaignPowerPointGenerator {
	//private Date PresentationDate;
	//private String AspectRatio;
	//private String BackgroundImage;
	//private String Name;
	private PowerPointXMLGenerator CampaignPowerPoint;
	
	private String FontColor = "000000";
    private boolean debug = false;
    private String AspectRatio;
    private void dbgmsg(String Msg)
    {
        if (this.debug)
        {
            System.out.println("Debug: " + Msg);
        }
    }
	
	//Campaign Aspect Ratio acceptable inputs are "4x3" and "16x9"
	//
	public CampaignPowerPointGenerator(String PathToBackgroundImage, String CampaignAspectRatio, String CampaignName, String CampaignFontColor){
		//this should create the title slide and set the background image and put the date on the slide
		//
		CampaignPowerPoint = new PowerPointXMLGenerator();
		//BackgroundImage = PathToBackgroundImage;
		this.AspectRatio = CampaignAspectRatio;
		this.FontColor = CampaignFontColor;
		CampaignPowerPoint.CreatePresentation(CampaignAspectRatio);
		String simpleDate = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
		
		//create the title slide
		//Title first
		CampaignPowerPoint.addnewslide();
		TextBoxDetails textTitle = new TextBoxDetails();
		textTitle.Font = "Arial";
		textTitle.Text = CampaignName;
		textTitle.fontColor = this.FontColor;
		textTitle.x = 40;
		textTitle.y = 440;
		textTitle.fontSize = 18;
		String TitleXML = textTitle.BuildXMLTextBoxString();
		CampaignPowerPoint.addTextBoxToCurrentSlide(TitleXML);
		//CampaignPowerPoint.setBackgroundOfCurrentSlide(PathToBackgroundImage);
		// Then Date
		TextBoxDetails textDate = new TextBoxDetails();
		textDate.Font = "Arial";
		textDate.Text = simpleDate;
		textDate.fontColor = this.FontColor;
		textDate.x = 40;
		textDate.y = 465;
		textDate.fontSize = 12;
		String DateXML = textDate.BuildXMLTextBoxString();
		CampaignPowerPoint.addTextBoxToCurrentSlide(DateXML);
		
		CampaignPowerPoint.setBackgroundOfFirstSlide(PathToBackgroundImage);
		
	}
	public boolean AddScreenshotSlide(String ScreenshotURL, BufferedImage ScreenshotImage){
		//add the new slide and insert the title and screenshot
		CampaignPowerPoint.addnewslide();
		TextBoxDetails textTitle = new TextBoxDetails();
		textTitle.Font = "Arial";
		textTitle.Text = ScreenshotURL;
		textTitle.fontColor = this.FontColor;
		textTitle.x = 1;
		textTitle.y = 1;
		//textTitle.fontSize = 11;
		textTitle.fontSize = this.GetUrlFontSize(ScreenshotURL);
		
		String TitleXML = textTitle.BuildXMLTextBoxString();
		CampaignPowerPoint.addTextBoxToCurrentSlide(TitleXML);
		CampaignPowerPoint.setBackgroundOfCurrentSlide();
		//now add the screenshot
		//get screenshot dimensions

		//Get the image's width and height
		this.dbgmsg("About to set get width and height");
		int ScreenshotWidth = ScreenshotImage.getWidth();
		int ScreenshotHeight = ScreenshotImage.getHeight();
		this.dbgmsg("About to add to the slide");
		this.dbgmsg("image width: " + Integer.toString(ScreenshotWidth));
		this.dbgmsg("image height: " + Integer.toString(ScreenshotHeight));
		int TopLeftAndResolution[];
		TopLeftAndResolution = this.CalculatePositionAndSizeOfScreenshot(ScreenshotWidth, ScreenshotHeight, this.AspectRatio);
		
		this.dbgmsg("Left: " + Integer.toString(TopLeftAndResolution[0]));
		this.dbgmsg("Top: " + Integer.toString(TopLeftAndResolution[1]));
		this.dbgmsg("width: " + Integer.toString(TopLeftAndResolution[2]) + " - Converted : " + (TopLeftAndResolution[2] * 9525));
		this.dbgmsg("height: " + Integer.toString(TopLeftAndResolution[3])+ " - Converted : " + (TopLeftAndResolution[3] * 9525));
		
		CampaignPowerPoint.addScreenShotToCurrentSlide(ScreenshotImage, TopLeftAndResolution[0], TopLeftAndResolution[1], TopLeftAndResolution[3], TopLeftAndResolution[2]);
		//CampaignPowerPoint.addScreenShotToCurrentSlide(ScreenshotPath, 80, 50, ScreenshotWidth, ScreenshotHeight);

		return true;
	}
	public void SaveCampaignPowerPoint(String OutputFilePath){
		CampaignPowerPoint.SaveFile(OutputFilePath);
	}
	
	private int GetUrlFontSize(String ssURL){
		int urlFontSize;
		urlFontSize = 10;
		
		if (ssURL.length() > 85){
			urlFontSize = 9;
			
		}
		if (ssURL.length() > 100){
			urlFontSize = 9;
			
		}
		if (ssURL.length() > 130){
			urlFontSize = 8;
			
		}
		if (ssURL.length() > 150){
			urlFontSize = 7;
			
		}
		
		return urlFontSize;
	}
	private int[] CalculatePositionAndSizeOfScreenshot(int screenshotWidth, int screenshotHeight, String pptAspectRatio){
		int XYandSize[] = new int[4];
		int sideMargin = 30;
		int topMargin = 25;
		int bottomMargin = 10;
		int wideScreenWidth = 960;
		int wideScreenHeight = 540;
		int squareWidth = 1024;
		int squareHeight = 768;
		int adjustedScreenshotWidth;
		int adjustedScreenshotHeight;
		
		int slideWidth = 0;
		int slideHeight = 0;
		
		if (pptAspectRatio == "16x9"){
			
			slideWidth = wideScreenWidth;
			slideHeight = wideScreenHeight;
		}
		else{
			
			slideWidth = squareWidth;
			slideHeight = squareHeight;
		}
		
		double widthRatio = ((double)(screenshotWidth)) / (((double)(slideWidth)) - ((double)(sideMargin) * 2.0));
		double heightRatio = ((double)screenshotHeight) / (((double)slideHeight) - (((double)topMargin) + ((double)bottomMargin)));
		this.dbgmsg("width ratio: " + String.format("%.2f", widthRatio));
		this.dbgmsg("height ratio: " + String.format("%.2f", heightRatio));
		//this is if we need to shrink the image
		if((widthRatio > 1) || (heightRatio > 1)){
			this.dbgmsg("In the shrink image section");
			//determine if the height or width is more out of whack
			if (widthRatio > heightRatio){
				this.dbgmsg("Inside widthratio greater than heightratio");
				adjustedScreenshotWidth = (slideWidth - (sideMargin * 2));
				//adjustedScreenshotHeight = (int)((((double)slideWidth - ((double)sideMargin * 2.0)) / ((double)screenshotWidth)) * (double)screenshotWidth);
				double shrinkToPercentage = (((double)slideWidth) - (((double)sideMargin * 2.0) )) / ((double)screenshotWidth);
				adjustedScreenshotHeight = (int)(shrinkToPercentage * (double)screenshotHeight);
				this.dbgmsg("ShrinkPercentage: " + shrinkToPercentage);
				this.dbgmsg("AdjustedWidth : " + adjustedScreenshotWidth );
				this.dbgmsg("AdjustedHeight : " + adjustedScreenshotHeight );
			}
			else{
				this.dbgmsg("inside heightratio greater than or equal to widthratio");
				adjustedScreenshotHeight =  (slideHeight - (topMargin + bottomMargin));
				//adjustedScreenshotWidth = (int)((((double)slideHeight - (((double)topMargin + (double)bottomMargin))) / ((double)screenshotHeight)));
				double shrinkToPercentage = (((double)slideHeight) - (((double)topMargin) + ((double)bottomMargin))) / ((double)screenshotHeight);
				adjustedScreenshotWidth = (int)(shrinkToPercentage * (double)screenshotWidth);
				this.dbgmsg("ShrinkPercentage: " + shrinkToPercentage);
				this.dbgmsg("AdjustedWidth : " + adjustedScreenshotWidth );
				this.dbgmsg("AdjustedHeight : " + adjustedScreenshotHeight );
			}
			
		}
		//this is if we don't
		else {
			this.dbgmsg("Didn't make it into the shrink image section");
			adjustedScreenshotHeight = screenshotHeight;
			adjustedScreenshotWidth = screenshotWidth;
		}
		//find top left corner
		int leftMost = 0;
		int topMost = 0;
		leftMost = (int)(((double)slideWidth / 2.0) - ((double)adjustedScreenshotWidth / 2.0));
		topMost = (int)(((double)slideHeight / 2.0) - ((double)adjustedScreenshotHeight / 2.0) + Math.abs(topMargin - bottomMargin));
		
		XYandSize[0] = leftMost;
		XYandSize[1] = topMost;
		XYandSize[2] = adjustedScreenshotWidth;
		XYandSize[3] = adjustedScreenshotHeight;
		
		return XYandSize;
	}
		
		
	
	

}
