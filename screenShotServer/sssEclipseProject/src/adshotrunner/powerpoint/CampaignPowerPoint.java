package adshotrunner.powerpoint;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;

import adshotrunner.campaigns.AdShot;
import adshotrunner.campaigns.Creative;

public class CampaignPowerPoint {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	final private static int SLIDEWIDTH = 960;					//PowerPoint screen width. Magic number, not sure from where
	final private static int SLIDEHEIGHT = 540;					//PowerPoint screen height. Magic number, not sure from where

	final private static String DEFAULTFONTTYPE = "Arial";		//Default font type

	final private static String TITLEFONTTYPE = "Arial";		//Font type for first slide campaign title
	final private static int TITLEFONTSIZE = 18;				//Font size for first slide campaign title
	final private static int TITLEXPOSITION = 40;				//X position for first slide campaign title
	final private static int TITLEYPOSITION = 440;				//Y position for first slide campaign title
	final private static int TITLEWIDTH = 500;					//Width for first slide campaign title

	final private static String DATEFONTTYPE = "Arial";			//Font type for first slide campaign date
	final private static int DATEFONTSIZE = 12;					//Font size for first slide campaign date
	final private static int DATEXPOSITION = 40;				//X position for first slide campaign date
	final private static int DATEYPOSITION = 465;				//Y position for first slide campaign date
	final private static int DATEWIDTH = 500;					//Width for first slide campaign date
	
	final private static int SCREENSHOTTOPMARGIN = 50;			//Top margin of an AdShot screenshot
	final private static int SCREENSHOTSIDEMARGIN = 40;			//Left and right side margin of an AdShot screenshot
	final private static int SCREENSHOTBOTTOMMARGIN = 10;		//Bottom margin of an AdShot screenshot
	
	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Variables -------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Variables *********************************

	//**************************** Protected Static Variables *******************************


	//***************************** Private Static Variables ********************************
	//Prefix with underscore: _myVariable

	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************


	//**************************** Protected Static Methods *********************************


	//***************************** Private Static Methods **********************************


	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Public Variables *************************************
	
	

	//******************************* Protected Variables ***********************************


	//******************************** Private Variables ************************************
	//Prefix with underscore: _myVariable
	/**
	 * The campaign PowerPoint instance to modify 
	 */
	private PowerPoint _powerPoint;
	
	/**
	 * The font color to use throughout the powerpoint
	 */
	private String _fontColor;
	
	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	public CampaignPowerPoint(String campaignName, String campaignDate, 
							  String fontColor, File backgroundImage) throws Exception {
		
		//Initialize the actual PowerPoint
		_powerPoint = new PowerPoint(backgroundImage);
		
		
		//Add the first slide
		SlidePart firstSlide = _powerPoint.addNewSlide();
		
		//Add the title
		_powerPoint.addTextToSlide(firstSlide, campaignName, TITLEXPOSITION, TITLEYPOSITION, TITLEWIDTH, 
								   TITLEFONTTYPE, TITLEFONTSIZE, fontColor);
		
		//Add the date
		_powerPoint.addTextToSlide(firstSlide, campaignDate, DATEXPOSITION, DATEYPOSITION, DATEWIDTH, 
								   DATEFONTTYPE, DATEFONTSIZE, fontColor);
		
		//Store the font color
		_fontColor = fontColor;
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************		
	public void addSlide(AdShot slideAdShot) throws Exception {
		
		//Create the new slide
		SlidePart newSlide = _powerPoint.addNewSlide();
		
		//Add the final page title and injected creative dimensions text
		_powerPoint.addTextToSlide(newSlide, getSlideDescription(slideAdShot), 30, 1, 800, DEFAULTFONTTYPE, 15, _fontColor);
		_powerPoint.addTextToSlide(newSlide, StringEscapeUtils.escapeXml(slideAdShot.pageTitle()), 30, 25, 800, DEFAULTFONTTYPE, 10, _fontColor);
		
		//Get the new dimensions of the screenshot
		int targetWidth = SLIDEWIDTH - (SCREENSHOTSIDEMARGIN * 2);
		int targetHeight = SLIDEHEIGHT - SCREENSHOTTOPMARGIN - SCREENSHOTBOTTOMMARGIN;
		Map<String, Integer> screenshotDimensions = resizeImageDimensions(slideAdShot.image().getWidth(), 
																		  slideAdShot.image().getHeight(), 
																		  targetWidth, targetHeight);
		int finalWidth = screenshotDimensions.get("width");
		int finalHeight = screenshotDimensions.get("height");
		
		//Determine the x,y position of the screenshot
		int xPosition = (SLIDEWIDTH - finalWidth)/2;
		int yPosition = SCREENSHOTTOPMARGIN;
		
		//Insert the screenshot
		_powerPoint.addImageToSlide(newSlide, slideAdShot.image(), xPosition, yPosition, finalWidth, finalHeight);
	}
	
	public void save(String filepath) throws Docx4JException {
		_powerPoint.save(filepath);
	}

	//********************************* Private Methods **************************************		
	private HashMap<String, Integer> resizeImageDimensions(int startWidth, int startHeight, 
														   int targetWidth, int targetHeight) {
		
		//If the screenshot is bigger than the maximum possible dimensions, shrink its dimensions
		int finalWidth = startWidth;
		int finalHeight = startHeight;
		if ((startWidth > targetWidth) || (startHeight > targetHeight)) {
			
			//Get the screenshot aspect ratio
			double screenshotAspectRatio = (double) startWidth/startHeight;
			
			//If the width is greater than the height, set the width
			//to the maximum possible width and adjust the height
			if ((startWidth/targetWidth) > (startHeight/targetHeight)) {
				finalWidth = targetWidth;
				finalHeight = (int) (targetWidth/screenshotAspectRatio);
			}
			
			//Otherwise if the height is greater than the width, set the height to the
			//maximum possible height and adjust the width
			else {
				finalWidth = (int) (targetHeight*screenshotAspectRatio);
				finalHeight = targetHeight;
			}
		}
		
		//Return the new dimensions
		HashMap<String, Integer> finalDimensions = new HashMap<String, Integer>();
		finalDimensions.put("width", finalWidth);
		finalDimensions.put("height", finalHeight);
		return finalDimensions;
	}
	
	private String getSlideDescription(AdShot targetAdShot) {
		
		//Begin by determining if the AdShot was for desktop or mobile
		String adShotDescription = (targetAdShot.mobile()) ? "Mobile" : "Desktop";
		
		//Put all the AdShot creative dimensions in a set to preserve uniqueness
		Set<String> creativeDimensions = new HashSet<String>(); 
		for (Creative currentCreative : targetAdShot.injectedCreatives()) {
			creativeDimensions.add(currentCreative.width() + "x" + currentCreative.height());
		}
		
		//Put all the creative dimensions in a string separated by commas
		String creativeDimensionsText = "";
		for (String currentDimension : creativeDimensions) {
			if (!creativeDimensionsText.isEmpty()) {creativeDimensionsText += ",";}
			creativeDimensionsText += currentDimension;
		}
		
		//If creative dimensions were found, add them to the description
		if (!creativeDimensionsText.isEmpty()) {adShotDescription += ": " + creativeDimensionsText;}
		
		return adShotDescription;
	}
	
	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
 

	//********************************* Protected Accessors *********************************


	//********************************* Private Accessors ***********************************
	
}
