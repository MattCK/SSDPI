package adshotrunner;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.ASRDatabase;
import adshotrunner.utilities.URLTool;

/**
 * The AdShotter class inserts passed tags into webpages and returns screenshots.
 * 
 * Multiple unique tags, optionally ranked, can be supplied for the same URL. 
 *
 */
public class AdShotter3 {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//final private static String SELENIUMHUBADDRESS = "http://localhost:4444/wd/hub";
	final private static String SELENIUMHUBADDRESS = ASRProperties.seleniumHubURL();
	final private static String ADINJECTERJSPATH = ASRProperties.pathForAdInjecterJavascript();
	final private static String ADMARKERPATH = ASRProperties.pathForAdMarkerExtension();
	final private static String CSPDISABLEPATH = "chromeExtensions/chrome-csp-disable-master.crx";
	final private static String DISABLEVISABILITYPATH = "chromeExtensions/disableVisibility.crx";
	final private static String PROXIESJSONPATH = ASRProperties.pathForProxiesJSON();
	final private static int JAVASCRIPTWAITTIME = 2500;		//in milliseconds
	final private static int DEFAULTTIMEOUT = 1000;			//in milliseconds
	final private static int PAGETIMEOUT = 12000;			//in milliseconds
	final private static int TAGTIMEOUT = 5000;				//in milliseconds
	final private static int TAGALLOWFINISHTIME = 20000;	//in milliseconds
	final private static int INITIALMOBILETIMEOUT = 17000;	//in milliseconds
	final private static int SCREENSHOTATTEMPTS = 3;
	final private static int SCREENSHOTTIMEOUT = 11000;		//in milliseconds
	final private static int DEFAULTVIEWWIDTH = 1366;		//in pixels
	final private static int DEFAULTVIEWHEIGHT = 2800;		//in pixels
	final private static int MOBILEVIEWWIDTH = 375;			//in pixels
	final private static int MOBILEPIXELRATIO = 2;			//in pixels
	final private static int MINIMUMCROPHEIGHT = 1560;		//in pixels
	final private static int MAXIMUMCROPHEIGHT = 3000; 		//in pixels
	final private static String MOBILEUSERAGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";
//	final private static String MOBILEUSERAGENT = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16";
	final private static boolean VERBOSE = true;
	final private static int DEFAULTOPENTABS = 1;
	final private static int TAGSOPENTABS = 4;


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
	public static AdShotter3 create() {
		return new AdShotter3(DEFAULTVIEWWIDTH, DEFAULTVIEWHEIGHT);
	}

	public static AdShotter3 create(int viewWidth, int viewHeight) {
		return new AdShotter3(viewWidth, viewHeight);
	}

	public static AdShotter3 createForMobile() {
		return new AdShotter3(MOBILEVIEWWIDTH, DEFAULTVIEWHEIGHT, true, false);
	}

	public static AdShotter3 createForTags() {
		return new AdShotter3(DEFAULTVIEWWIDTH, DEFAULTVIEWHEIGHT, false, true);
	}



	//**************************** Protected Static Methods *********************************


	//***************************** Private Static Methods **********************************


	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Public Variables *************************************
	
	

	//******************************* Protected Variables ***********************************


	//******************************** Private Variables ************************************
	/**
	 * View width to set the browser. If not specified in constructor, set to DEFAULTVIEWWIDTH.
	 */
	private int _browserViewWidth;
	
	/**
	 * View height to set the browser. If not specified in constructor, set to DEFAULTVIEWHEIGHT.
	 */
	private int _browserViewHeight;

	/**
	 * Flag whether or not the AdShotter should use a mobile driver
	 */
	private Boolean _useMobile;

	/**
	 * Flag whether or not the AdShotter should treat the AdShots as tags.
	 */
	private Boolean _treatAsTags;


	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Instantiates object with the default viewport width and height
	 */
	private AdShotter3() {
		
		this(DEFAULTVIEWWIDTH, DEFAULTVIEWHEIGHT);		
	}
	
	/**
	 * Instantiates object with the passed viewport width and height set
	 * 
	 * @param viewWidth		Width of browser viewport
	 * @param viewHeight	Height of browser viewport
	 */
	private AdShotter3(int viewWidth, int viewHeight) {
		
		this(viewWidth, viewHeight, false, false);
	}

	/**
	 * Instantiates object with the passed viewport and user agent string
	 * 
	 * If the userAgent string is empty or null, the browser's default will be used.
	 * 
	 * @param viewWidth		Width of browser viewport
	 * @param viewHeight	Height of browser viewport
	 * @param useMobile		Flags whether or not to use a mobile driver
	 * @param useMobile		Flags whether or not to treat AdShots as tags
	 */
	private AdShotter3(int viewWidth, int viewHeight, Boolean useMobile, Boolean treatAsTags) {
		
		_browserViewWidth = viewWidth;
		_browserViewHeight = viewHeight;
		_useMobile = useMobile;
		_treatAsTags = treatAsTags;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************		
	public long takeAdShots(List<AdShot> adShots) {
				
		//Try to create a web driver to connect with
		WebDriver remoteWebDriver = null;
		try {remoteWebDriver = getSeleniumChromeDriver();}
		catch (Exception e) {throw new AdShotRunnerException("Could not connect with Selenium server", e);}
		
		//Store the time after a driver has been made available to the thread
		long adShotsStartTime = System.nanoTime();
		
		//Open each AdShot URL in a new tab up to the max tab amount
		consoleLog("Beginning to open tabs...");
		int numberOfOpenTabs = (_treatAsTags) ? TAGSOPENTABS : DEFAULTOPENTABS;
		int numberOfRequiredTabs = (adShots.size() < numberOfOpenTabs) ? adShots.size() : numberOfOpenTabs;
		ArrayList<String> tabHandles = getTabs(remoteWebDriver, numberOfRequiredTabs);
		consoleLog("Done opening tabs.");

		//Stores the start time the AdShot page is first loaded.
		//When working with tags, this makes sure each tag has enough time
		//for its animation to run.
		Map<Integer, Long> loadStartTimes = new HashMap<Integer, Long>();
		
		//Load the initial pages into the tabs. 
		consoleLog("Loading initial pages...");
		Iterator<String> tabIterator = tabHandles.iterator();
		int adShotIndex = 0;
		while (tabIterator.hasNext()) {
			
			//Switch to the next tab
			String nextTabHandler = tabIterator.next();
			consoleLog("	Beginning content switch to next tab: " + nextTabHandler);
			remoteWebDriver.switchTo().window(nextTabHandler);
			remoteWebDriver.switchTo().defaultContent();
			consoleLog("	Done switching content to next tab.");
			
			//Navigate to the initial page
			consoleLog("	Navigating to initial page: " + adShots.get(adShotIndex).url());
			try {
				
				//Store the AdShot's starting load time
				loadStartTimes.put(adShotIndex, System.nanoTime());
//				consoleLog("Load Start Time (" + adShotIndex + ": " + System.nanoTime() + " (" + (System.nanoTime()/1000000) + ")");
				
				//Navigate to the page
				if (_useMobile) {
					navigateSeleniumDriverToURL(remoteWebDriver, adShots.get(adShotIndex).url(), INITIALMOBILETIMEOUT);					
				}
				else {
					navigateSeleniumDriverToURL(remoteWebDriver, adShots.get(adShotIndex).url());
				}
			} 
			catch (Exception e) {
				consoleLog("	Couldn't navigate to page: " + adShots.get(adShotIndex).url());
				adShots.get(adShotIndex).setError(new AdShotRunnerException("Could not navigate to URL", e));
			}
			
			//Iterate to the next AdShot
			++adShotIndex;
		}

		consoleLog("Done loading pages.");
		
		//Loop through each AdShot and take the screenshot
		tabIterator = tabHandles.iterator();
		for (adShotIndex = 0; adShotIndex < adShots.size(); ++adShotIndex) {
			consoleLog("------------- New AdShot (" + (adShotIndex + 1) + "/" + adShots.size() +  ") --------------");

			if ((adShots.size() > 1) && (numberOfOpenTabs > 1)) {
				String nextTabHandler = tabIterator.next();
				remoteWebDriver.switchTo().window(nextTabHandler);
				remoteWebDriver.switchTo().defaultContent();
				consoleLog("Switching to next tab:" + nextTabHandler);
				focusTab(remoteWebDriver);
				consoleLog("Done switching to next tab.");
				if (!tabIterator.hasNext()) {tabIterator = tabHandles.iterator();}
			}			

			//If we are dealing with tags, confirm it had enough time to run its animation
			if (_treatAsTags) {
				
				long currentRunTimeMS = ((System.nanoTime() - loadStartTimes.get(adShotIndex)))/1000000;
//				consoleLog("Load Start Time (" + adShotIndex + "): " + loadStartTimes.get(adShotIndex) + " (" + (loadStartTimes.get(adShotIndex)/1000000) + ")");
//				consoleLog("Load Finish Time (" + adShotIndex + "): " + System.nanoTime() + " (" + (System.nanoTime()/1000000) + ")");
//				consoleLog("Run Time: " + currentRunTimeMS);
				if (currentRunTimeMS < TAGALLOWFINISHTIME) {
					pause((int) (TAGALLOWFINISHTIME - currentRunTimeMS));
					consoleLog("Pausing: " + (TAGALLOWFINISHTIME - currentRunTimeMS));
				}
			}
			
			//Take the actual AdShot
			long currentAdShotStartTime = System.nanoTime();
			AdShot currentAdShot = adShots.get(adShotIndex);
			consoleLog("URL: " + currentAdShot.url());
			takeAdShot(remoteWebDriver, currentAdShot);
			
			//If no tag images were injected into the page AND alternate URLs exist, try those
			if ((currentAdShot.injectedTagImages().size() == 0) && (currentAdShot.alternateURLs().size() > 0)) {
				Iterator<String> urlIterator = currentAdShot.alternateURLs().iterator();
				while ((currentAdShot.injectedTagImages().size() == 0) && 
					   (urlIterator.hasNext())) {
					
					String alternateURL = urlIterator.next();
					
					try {navigateSeleniumDriverToURL(remoteWebDriver, alternateURL);} 
					catch (Exception e) {
						consoleLog("Couldn't navigate to page: " + adShots.get(adShotIndex).url());
						adShots.get(adShotIndex).setError(new AdShotRunnerException("Could not navigate to URL", e));
					}
					
					takeAdShot(remoteWebDriver, currentAdShot);
				}
			}
			
			//If there is an AdShot that needs to be loaded still,
			//put it in the current tab and navigate to the next
			if ((adShotIndex + numberOfOpenTabs) < adShots.size()) {
				
				//Store the AdShot's starting load time
				loadStartTimes.put(adShotIndex  + numberOfOpenTabs, System.nanoTime());
				
				try {navigateSeleniumDriverToURL(remoteWebDriver, adShots.get(adShotIndex + numberOfOpenTabs).url());} 
				catch (Exception e) {
					consoleLog("Couldn't navigate to page: " + adShots.get(adShotIndex).url());
					adShots.get(adShotIndex).setError(new AdShotRunnerException("Could not navigate to URL", e));
				}	
			}
						
			long currentAdShotEndTime = System.nanoTime();
			consoleLog("Total Adshot runtime: " + (currentAdShotEndTime - currentAdShotStartTime)/1000000 + " ms");
		}
		
		//Quit the driver
		quitWebdriver(remoteWebDriver);
		
		return System.nanoTime() - adShotsStartTime;
	}


	//******************************** Protected Methods ************************************


	//********************************* Private Methods *************************************
	/**
	 * Takes and returns an AdShot for the passed URL with the passed tags.
	 * 
	 * Navigates driver to URL, stops page loading (if needed), executes AdInjecter
	 * javascript, and takes a final screenshot.
	 * 
	 * @param activeSeleniumWebDriver	Selenium web driver to navigate
	 * @param adShot					AdShot object used to take the screenshot
	 * @param treatAsTag				If true, no javascript is injected into the page
	 * @throws AdShotRunnerException 	Error and description if taking the ad screenshot failed
	 */
	private void takeAdShot(WebDriver activeSeleniumWebDriver, AdShot adShot) throws AdShotRunnerException {
		
		//This is the shortest the screenshot should be cropped
		//It represents the lowest bottom y position of all tags
		//first injections. In other words, the bottom of all
		//the first appearances of the tags.
		//We say 'requested' since it might be below or above 
		//the crop minimum and maximums.
		int requestedCropHeight = 0;

		//If the AdShot is not to be treated like a tag, create and inject javascript
		if (!_treatAsTags) {
					
			//First, get the AdInjecter javascript with the current tags inserted
			consoleLog("Creating Injecter JS...");
			String adInjecterJS = "";
			try {
				adInjecterJS = getInjecterJS(adShot.tagImages(), adShot.url());
				consoleLog("Done creating Injecter JS.");
			}
			catch (Exception e) {
				consoleLog("FAILED: Could not create AdInjecter javascript");
				adShot.setError(new AdShotRunnerException("Could not create AdInjecter javascript", e)); return;
			}
			
			//Execute the javascript
			consoleLog("Injecting JS...");
			String injecterResponse = "";
			try {
				injecterResponse = executeSeleniumDriverJavascript(activeSeleniumWebDriver, adInjecterJS);
				consoleLog("	Injecter response:");
				consoleLog("	" + injecterResponse);
			}
			catch (Exception e) {
				consoleLog("FAILED: Could not execute AdInjecter in page!");
				consoleLog(e.getMessage());
				adShot.setError(new AdShotRunnerException("Could not execute AdInjecter in page", e)); //return;
			}
			
			//Get the list of tags that were injected and the positions they were injected from the AdInjecter
			Type injecterJSONType = new TypeToken<HashMap<String, HashMap<String, HashMap<String, Float>>>>(){}.getType();
			Map<String, Map<String, Map<String, Float>>> injecterResults = new Gson().fromJson(injecterResponse, injecterJSONType);
			if (injecterResults != null) {
				
				//Put the injected Creatives info and output log into separate variables
				Map<String, Map<String, Float>>injectedTagInfo = injecterResults.get("injectedCreatives");
				String responseMessages = injecterResults.get("outputLog").keySet().iterator().next();
				
				//Output the message log
				consoleLog("\n---------------Injecter Script Log---------------\n" + 
							responseMessages +
							"\n------------------------------------------------");
				
				//Mark the tags as injected and determine the requested crop height
				for (Map.Entry<String, Map<String, Float>> entry : injectedTagInfo.entrySet()) {

					//Name the keys for readability and get the TagImage object
					String currentTagID = entry.getKey();											//ID of TagImage
				    TagImage currentTagImage = getTagImageByID(currentTagID, adShot.tagImages());	//TagImage from ID
				    Map<String, Float> coordinates = entry.getValue();	//List of x,y positions of tag injection
				    int currentTagXCoordinate = Math.round(coordinates.get("x")); 
				    int currentTagYCoordinate = Math.round(coordinates.get("y")); 
				
				    //Determine the bottom coordinate of the injected creative
				    int bottomCoordinate = currentTagYCoordinate + currentTagImage.height();
				    
				    //If the bottom coordinate is lower than the requested crop height, use it
				    if (bottomCoordinate > requestedCropHeight) {requestedCropHeight = bottomCoordinate;}
				    consoleLog("Position: " + currentTagXCoordinate + ", " + currentTagYCoordinate);
				    consoleLog("Height: " + currentTagImage.height());
					consoleLog("Current requested Crop Height: " + requestedCropHeight);
				    
				    //Mark the creative as injected
				    adShot.markTagImageAsInjected(currentTagID);
				}
			}
//			//Get the list of tags that were injected and the positions they were injected from the AdInjecter
//			Type injecterJSONType = new TypeToken<HashMap<String, HashMap<String, Float>>>(){}.getType();
//			Map<String, Map<String, Float>> injectedTagInfo = new Gson().fromJson(injecterResponse, injecterJSONType);
//			if (injectedTagInfo != null) {
//				
//				//Mark the tags as injected and determine the requested crop height
//				for (Map.Entry<String, Map<String, Float>> entry : injectedTagInfo.entrySet()) {
//
//					//Name the keys for readability and get the TagImage object
//					String currentTagID = entry.getKey();											//ID of TagImage
//				    TagImage currentTagImage = getTagImageByID(currentTagID, adShot.tagImages());	//TagImage from ID
//				    Map<String, Float> coordinates = entry.getValue();	//List of x,y positions of tag injection
//				    int currentTagXCoordinate = Math.round(coordinates.get("x")); 
//				    int currentTagYCoordinate = Math.round(coordinates.get("y")); 
//				
//				    //Determine the bottom coordinate of the injected creative
//				    int bottomCoordinate = currentTagYCoordinate + currentTagImage.height();
//				    
//				    //If the bottom coordinate is lower than the requested crop height, use it
//				    if (bottomCoordinate > requestedCropHeight) {requestedCropHeight = bottomCoordinate;}
//				    consoleLog("Position: " + currentTagXCoordinate + ", " + currentTagYCoordinate);
//				    consoleLog("Height: " + currentTagImage.height());
//					consoleLog("Current requested Crop Height: " + requestedCropHeight);
//				    
//				    //Mark the creative as injected
//				    adShot.markTagImageAsInjected(currentTagID);
//				}
//				
//				
//				
//			}
			else {
				consoleLog("------------------- Javascript returned empty String -------------------------");
			}
			
			//Add a little margin to the minimum cutoff
			requestedCropHeight += 40;
			consoleLog("Final requested Crop Height: " + requestedCropHeight);
			consoleLog("Done injecting JS.");
		}
		else {
			//this is to allow ads to finish their play loops
			//ensuring the tag images are taken at a time when the ads look good.
			//pause(TAGALLOWFINISHTIME);
		}
		
		//this can be uncommented to test how the javascript ran
		/*System.out.print("Javascript run and ads inserted. Press Enter to continue");
		int inChar;
		try {
			inChar = System.in.read();
		} catch (IOException e1) {
			consoleLog("error reading from command line");
		}*/
		
		//Take the screenshot 
		long screenShotStartTime = System.nanoTime();
		
		File screenShot = null;
		try {
			screenShot = captureSeleniumDriverScreenshot(activeSeleniumWebDriver);
		}
		catch (Exception e) {
			consoleLog("FAILED: Could not take screenshot!");
			adShot.setError(new AdShotRunnerException("Could not take screenshot", e)); return;
		}
		
		long screenShotEndTime = System.nanoTime();
		consoleLog("Screenshot Time: - " + (screenShotEndTime - screenShotStartTime)/1000000 + " ms");
		
		//Store the final URL
		adShot.setFinalURL(activeSeleniumWebDriver.getCurrentUrl());
		
		//Store the pageTitle
		adShot.setPageTitle(activeSeleniumWebDriver.getTitle());
		
		//Crop the image
		consoleLog("Cropping screenshot...");
		try {
			adShot.setImage(cropAndConvertImageFile(activeSeleniumWebDriver, screenShot, requestedCropHeight));
			consoleLog("Done cropping.");
		}
		catch (Exception e) {
			consoleLog("FAILED: Could not crop screenshot!");
			adShot.setError(new AdShotRunnerException("Could not crop screenshot", e)); return;
		}
				
	}
		
	/**
	 * Returns an initialized selenium webdriver. 
	 *
	 * The selenium address used is set to the class constant SELENIUMHUBADDRESS.
	 * The view dimensions are set to the class constants VIEWWIDTH and VIEWHEIGHT.
	 * 
	 * @return			Initialized selenium webdriver
	 */
	private WebDriver getSeleniumChromeDriver() throws MalformedURLException {
		
		//Attempt to create the actual web driver used to connect to selenium
		consoleLog("Creating Chrome driver...");
		DesiredCapabilities driverCapabilities = DesiredCapabilities.chrome();
		ChromeOptions driverOptions = new ChromeOptions();
		HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
		
		//If the browser needs to be in mobile mode, set the driver options for it
		if (_useMobile) {
			
			//Define the device metrics to use 
			Map<String, Object> mobileEmulation = new HashMap<String, Object>();
			Map<String, Object> deviceMetrics = new HashMap<String, Object>();
			deviceMetrics.put("width", _browserViewWidth);
			deviceMetrics.put("height", _browserViewHeight);
			deviceMetrics.put("pixelRatio", MOBILEPIXELRATIO);
			mobileEmulation.put("deviceMetrics", deviceMetrics);
			
			//Set the user agent
			mobileEmulation.put("userAgent", MOBILEUSERAGENT);	
			
			//Add the mobile information to the driver options
			driverOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
			
			//Turn off flash and the pdf viewer
			chromePrefs.put("plugins.plugins_disabled", new String[] {
				    "Adobe Flash Player",
				    "Chrome PDF Viewer"
				});
			
		
		}
		
//		chromePrefs.put("profile.managed_default_content_settings.cookies", new Integer(2));
		driverOptions.setExperimentalOption("prefs", chromePrefs);
		//the below option freezes chrome when injecting the ad injector
		//driverOptions.addArguments("disable-web-security");
		//driverOptions.addArguments("user-data-dir=/tmp/chromeprofile");
		
		//if this is a tag set the proxy server
		//some ad tags won't show up if they come from amazon ip addreses
		if (_treatAsTags) {
			//this option turns off the proxy for connections to aws resources
			driverOptions.addArguments("proxy-bypass-list='*.amazonaws.com'");
			//Set the proxy to use. If it is not empty, set the proxy capability3
			String proxyDetails = getProxyDetails();
			if (!proxyDetails.isEmpty()) {
				consoleLog("Using proxy: " + proxyDetails);
				Proxy chromeProxy = new Proxy();
				chromeProxy.setProxyType(ProxyType.MANUAL);
				chromeProxy.setSslProxy(proxyDetails);
				chromeProxy.setHttpProxy(proxyDetails);
				driverCapabilities.setCapability(CapabilityType.PROXY, chromeProxy);

				driverOptions.addExtensions(new File(DISABLEVISABILITYPATH));
			}
			else {consoleLog("WARNING!!! NOT USING A PROXY!");}
		}
		
		//Install the AdMarker extension to mark ad elements if not a tag
		if (!_treatAsTags) {
			try {
				driverOptions.addExtensions(new File(ADMARKERPATH));
				driverOptions.addExtensions(new File(CSPDISABLEPATH));
			} catch (Exception e) {
				consoleLog("	FAILED: Unable to load AdMarker. -" + e.toString() );
			}
		}


		//Initialize the actual driver
		WebDriver chromeDriver = null;
		driverCapabilities.setCapability(ChromeOptions.CAPABILITY, driverOptions);
		chromeDriver = new RemoteWebDriver(
							new URL(SELENIUMHUBADDRESS), 
							driverCapabilities);
		

		//Set the viewport position
		chromeDriver.manage().window().setPosition(new Point(0,20));
		
		//Set the page timeout
		setCommandTimeout(chromeDriver, DEFAULTTIMEOUT);
		
		//If not using mobile, set the viewport size
		if (!_useMobile) {chromeDriver.manage().window().setSize(new Dimension(_browserViewWidth, _browserViewHeight));}		
		
		//Return the initialized remote chrome web driver
		consoleLog("Done creating chrome driver.");
		return chromeDriver;
	} 
	

	
	/**
	 * Navigates the passed selenium driver to the passed URL.
	 * 
	 * @param activeSeleniumWebDriver	Selenium web driver to navigate
	 * @param pageURL					Page URL to navigate to
	 */
	private void navigateSeleniumDriverToURL(WebDriver activeSeleniumWebDriver, String pageURL) {
		
		//Use a shorter timeout for tags
		int navigationTimeout = (_treatAsTags) ? TAGTIMEOUT : PAGETIMEOUT;
		navigateSeleniumDriverToURL(activeSeleniumWebDriver, pageURL, navigationTimeout);
	}
		
	/**
	 * Navigates the passed selenium driver to the passed URL.
	 * 
	 * @param activeSeleniumWebDriver	Selenium web driver to navigate
	 * @param pageURL					Page URL to navigate to
	 * @param navigationTimeout			Time the page should stop loading after in milliseconds
	 */
	private void navigateSeleniumDriverToURL(WebDriver activeSeleniumWebDriver, String pageURL, int navigationTimeout) {
		
		//Set the navigation command timeout and navigate to the url
		consoleLog("		Sending navigation command: " + pageURL + "...");
		setCommandTimeout(activeSeleniumWebDriver, navigationTimeout);
		((JavascriptExecutor) activeSeleniumWebDriver).executeScript("window.location.href = '" + pageURL + "';");
		setCommandTimeout(activeSeleniumWebDriver, DEFAULTTIMEOUT);
		executeSeleniumDriverJavascript(activeSeleniumWebDriver, 
				"window.scrollBy(0, 300); setTimeout(function() {window.scrollBy(0, -300);}, 200);");
		consoleLog("		Navigation command sent.");		
	}
		
	/**
	 * Opens up the driver to the requested number of tabs. If the number of tabs already
	 * open is equal to or more than the requested number, no new tabs are opened.
	 * 
	 * @param activeSeleniumWebDriver		Selenium web driver to act upon
	 * @param numberOfTabs				Requested number of tabs to have open
	 * @return
	 */
	ArrayList<String> getTabs(WebDriver activeSeleniumWebDriver, int numberOfTabs) {
		
		//While their are less window handles than the requested number of tabs, keep openning tabs
		while (activeSeleniumWebDriver.getWindowHandles().size() < numberOfTabs) {
			openNewTab(activeSeleniumWebDriver);
		}
		
		//Return the list of all the window handles
		return new ArrayList<String> (activeSeleniumWebDriver.getWindowHandles());
	}
	
	/**
	 * Opens up a new tab in the driver browser
	 * 
	 * Designed to work with Chrome
	 * 
	 * @param activeSeleniumWebDriver	Selenium driver to interact with
	 */
	private void openNewTab(WebDriver activeSeleniumWebDriver) {
		int attempts = 0;
		boolean succeeded = false;
		while (!succeeded && (attempts < 3)) {
			try {
				consoleLog("	Sending new tab command");				
				((JavascriptExecutor) activeSeleniumWebDriver).executeScript("window.open('','_blank');");
				consoleLog("	New tab command sent");
				pause(200);
				succeeded = true;
			}
			catch (Exception e) {
				++attempts; 
				consoleLog("Couldn't create new tab: " + attempts);
				pause(500);
			}
		}
	}
	
	/**
	 * Brings the driver's current context tab into focus. Necessary for script injection.
	 * 
	 * @param activeSeleniumWebDriver	Selenium driver to interact with
	 */
	private void focusTab(WebDriver activeSeleniumWebDriver) {
		((TakesScreenshot) activeSeleniumWebDriver).getScreenshotAs(OutputType.FILE);

	}
	
	/**
	 * Returns the proxy details the driver should be set to in the format:
	 * 
	 * 		hostname:portnumber
	 * 
	 * Should be either a random proxy from the service or a proxy that
	 * automatically chooses a random route
	 * 
	 * @return	Proxy details in the format hostname::portnumber
	 */
	private String getProxyDetails() {
		
		//Get the proxies JSON from the proxies file. On failure, return an empty string.
		String proxiesJSON;
		try {
			proxiesJSON = new String(Files.readAllBytes(Paths.get(PROXIESJSONPATH)));
		} catch (IOException e) {
			consoleLog("Error: could not read proxies file");
			e.printStackTrace();
			return "";
		}
		
		//Get the array of proxies
		Type stringListType = new TypeToken<ArrayList<String>>(){}.getType();
		List<String> proxies = new Gson().fromJson(proxiesJSON, stringListType);
		
		//If the list is empty, simply return an empty string
		if (proxies.isEmpty()) {return "";}
		
		//Return a random proxy from the list
		int randomIndex = new Random().nextInt(proxies.size());
		return proxies.get(randomIndex);
	}

	/**
	 * Returns the AdInjecter javascript as a String with the passed tags inserted into it
	 * 
	 * @param tags			Tags and their placement rankings
	 * @param targetURL		URL the tags will be injected into, used for exceptions
	 * @return				AdInjecter javascript with passed tags inserted into it
	 */
	private String getInjecterJS(Set<TagImage> tagImageSet, String targetURL) throws IOException {
		
		//Get the AdInjecter javascript file
		String adInjecterJS = new String(Files.readAllBytes(Paths.get(ADINJECTERJSPATH)));
		
		//Create the creatives object by looping through the creatives and adding them to the JSON string
		String creativesJSON = "creatives = [";
		for (TagImage tagImage: tagImageSet) {
			
			//build the current tag object and add it to overall object
			creativesJSON +=  "{id: '" + tagImage.id() + "', " +
							  "imageURL: '" + tagImage.url() + "', " +
							  "width: " + tagImage.width() + ", " +
							  "height: " + tagImage.height() + ", " +
							  "priority: " + tagImage.priority() + "},";
		}
		creativesJSON += "];";
		
		//Insert the creatives into the code by replacing the 'INSERT CREATIVES OBJECT' marker with them
		String finalJS = adInjecterJS.replace("//INSERT CREATIVES OBJECT//", creativesJSON);
		
//		
		//If an exceptions exists, insert its script into the final string
		String exceptionScript = "";
		try {exceptionScript = getAdInjecterException(targetURL); } catch (Exception e) {}
		if (!exceptionScript.isEmpty()) {
			finalJS = finalJS.replace("//INSERT EXCEPTION SCRIPT//", exceptionScript);
		}
		
		FileUtils.writeStringToFile(new File("creativeInjecterWithTags.js"), finalJS);
		
		//Return the modified javascript as a String
		return finalJS;
	}

	/**
	 * Executes the passed javascript on the current selenium driver page. It then waits for the
	 * constant JAVASCRIPTWAITTIME before moving forward. This allows spawned threads such as image loading to finish. 
	 * 
	 * The String response from the executed script is returned.
	 * 
	 * @param activeSeleniumWebDriver		Selenium driver to execute javascript on
	 * @param javascriptCode				Javascript code to execute
	 * @return								AdInjecter javascript with passed tags inserted into it
	 */
	private String executeSeleniumDriverJavascript(WebDriver activeSeleniumWebDriver, String javascriptCode) {
		
		//Attempt to execute the script. If this fails, a runtime error will be thrown
		String response = (String) ((JavascriptExecutor) activeSeleniumWebDriver).executeScript(javascriptCode);
		
		//Pause a moment to let the javascript execute (this is for threads such as image loading, etc.)
		pause(JAVASCRIPTWAITTIME);
		
		//Return the response from the executed javascript
		return response;
	}
	
	/**
	 * Attempts to take a screenshot through the passed selenium web driver
	 * 
	 * The screenshot File is returned on success, a runtime error is thrown on failure
	 * 
	 * @param activeSeleniumWebDriver		Selenium driver to take screenshot on
	 * @return								Screenshot of selenium driver window
	 * @throws Exception 
	 */
	private File captureSeleniumDriverScreenshot(final WebDriver activeSeleniumWebDriver) {
		
		//Define the screenshot File variable to hold the final image
		consoleLog("Beginning to take screenshot...");		
		File screenShot = null;
		
		//Attempt to get screenshot a few times within the reasonable time frame
		TimeLimiter timeoutLimiter = new SimpleTimeLimiter();
		int currentAttempt = 0;
		while ((screenShot == null) && (currentAttempt < SCREENSHOTATTEMPTS)) {
			consoleLog("	Starting attempt: " + (currentAttempt + 1));
			try {
			screenShot = timeoutLimiter.callWithTimeout(new Callable<File>() {
							public File call() {
							  return ((TakesScreenshot) activeSeleniumWebDriver).getScreenshotAs(OutputType.FILE);
							}
						  }, SCREENSHOTTIMEOUT, TimeUnit.MILLISECONDS, false); 
			}
			catch (Exception e) {
				consoleLog("	FAILED: Error getting screenshot.");
				//Ignore any error and try another attempt (if any are left)
			}
			++currentAttempt;
		}
		
		if (screenShot == null) {
			consoleLog("Unable to take screenshot.");
			throw new AdShotRunnerException("Could not take screenshot");
		}
		
		consoleLog("Done taking screenshot.");
		
		return screenShot;
	}
	
	/**
	 * Crops the passed image according to the class VIEWPORT constants and passed maximum bottom.
	 * 
	 * The cropped image is returned on success and a runtime error is thrown on failure.
	 * 
	 * @param activeSeleniumWebDriver	Selenium driver used to get tag image size
	 * @param originalImageFile			File of image to crop
	 * @param requestedCropHeight		Requested crop height. If set too low or to zero, class defaults will be used.
	 * @param treatAsTag				If flagged as a tag, the crop width will be set to the image width (not the default view width)
	 * @return							Cropped image
	 * @throws IOException 
	 */
	private BufferedImage cropAndConvertImageFile(final WebDriver activeSeleniumWebDriver, File originalImageFile, int requestedCropHeight) throws IOException {
		
		//If this image is a tag, crop it to the tag width and height
		BufferedImage originalImage = ImageIO.read(originalImageFile);
		int cropHeight = MINIMUMCROPHEIGHT;
		int cropWidth = DEFAULTVIEWWIDTH;
		if (_treatAsTags) {
			
			//Get the tag height and width
			WebElement tagDiv = activeSeleniumWebDriver.findElement(By.id("adTagContainer"));
			cropHeight = tagDiv.getSize().getHeight();
			cropWidth = tagDiv.getSize().getWidth();
			consoleLog("tagDiv dimensions: " + cropHeight + "x" + cropWidth);
		}
		
		//Otherwise use the requested crop height and default width
		else {
			
			//Determine the crop height
			cropHeight = (requestedCropHeight < MINIMUMCROPHEIGHT) ? 
					MINIMUMCROPHEIGHT : requestedCropHeight;
			cropHeight = (cropHeight < MAXIMUMCROPHEIGHT) ? cropHeight : MAXIMUMCROPHEIGHT;
			cropHeight = (originalImage.getHeight() < cropHeight) ? originalImage.getHeight() : cropHeight;
			
			//Determine the width
			cropWidth = (originalImage.getWidth() < _browserViewWidth) ? originalImage.getWidth() : _browserViewWidth;
		}
				
		//Crop the image
		BufferedImage croppedImage = originalImage.getSubimage(0, 0, cropWidth, cropHeight);
		
		//Make BufferedImage generic so it can be written as png or jpg
		BufferedImage cleanedImage = new BufferedImage(croppedImage.getWidth(),
													   croppedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
		cleanedImage.createGraphics().drawImage(croppedImage, 0, 0, Color.WHITE, null);
		
		//Return the modified image
		return croppedImage;
	}
	
	/**
	 * Attempts to quit the passed selenium web driver. 
	 * 
	 * If the driver does not quit after a matter of seconds, program execution continues.
	 * 
	 * @param activeSeleniumWebDriver	Selenium driver to quit
	 */
	private void quitWebdriver(WebDriver activeSeleniumWebDriver) {
		
		consoleLog("Quitting web driver...");
		final WebDriver finalDriver = activeSeleniumWebDriver;
		TimeLimiter timeoutLimiter = new SimpleTimeLimiter();
		try {
			timeoutLimiter.callWithTimeout(new Callable<File>() {
							public File call() {
								finalDriver.quit();
								return null;
							}
						  }, 5000, TimeUnit.MILLISECONDS, false); 
		}
		catch (Exception e) {
			System.out.print(e + " - Attempt to quit webdriver timed out");
		}
		consoleLog("Done quitting web driver.");
	}

	private String getAdInjecterException(String targetURL) throws SQLException {
		
		//Get the domain including subdomain of the url. A protocol type is necessary for getDomain.
		String urlDomain = URLTool.getDomain(URLTool.setProtocol("http", targetURL));
		
		//Check the database to see if any entries matching the domain exist
		ResultSet exceptionsSet = ASRDatabase.executeQuery("SELECT * " + 
															 "FROM exceptionsAdInjecter " +
															 "WHERE EAI_url LIKE '" + urlDomain + "%'");
				
		//If a match was found, return the script 
		if (exceptionsSet.next()) {
			return exceptionsSet.getString("EAI_script");
		}
		
		//Otherwise, return an empty string
		return "";
	} 
	
	private TagImage getTagImageByID(String tagID, Set<TagImage> tagImageList) {
		
		for (TagImage currentTagImage : tagImageList) {
			if (currentTagImage.id().equals(tagID)) {return currentTagImage;}
		}
		
		//If the tag wasn't found, return null
		return null;
	}

	/**
	 * Sets the time each chromedriver command runs.
	 * 
	 * NOTE: THIS IS FOR USE WITH THE MODIFIED CHROMEDRIVER!!!
	 * 
	 * With the chromedriver, pagetimout does not refer to the loading time of a
	 * page but to the timeout of every command.
	 * 
	 * This is due to a bug with the chromedriver.
	 * 
	 * @param activeSeleniumWebDriver	Selenium driver to set command timeout on
	 * @param timeout					Timeout in milliseconds each chromedriver command should run
	 */
	private void setCommandTimeout(WebDriver activeSeleniumWebDriver, int timeout) {
		activeSeleniumWebDriver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Alias for Thread.sleep(...) with InterruptedException ignored
	 * 
	 * @param pauseTime		Time in miliseconds to pause/sleep
	 */
	private void pause(int pauseTime) {
		try {
			Thread.sleep(pauseTime);
		} 
		catch (InterruptedException e) {
			//If the sleep was interrupted, just keep moving
		} 
	}
	
	/**
	 * Outputs the message to standard output with the thread ID and timestamp
	 * if the VERBOSE flag constant is set to TRUE
	 * @param message
	 */
	private void consoleLog(String message) {
		if (VERBOSE) {
			
			//Remove gecko driver binary if present
			String cleanMessage = message.replaceAll("profile(.*?)appBuild", "");
			
			String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
			System.out.println(timeStamp + " - " + Thread.currentThread().getId() + ": " + cleanMessage);
		}
	}
	
	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
 

	//********************************* Protected Accessors *********************************


	//********************************* Private Accessors ***********************************


}

//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------


/**************************** All input command types ***************************
activeSeleniumWebDriver.manage().window().maximize();
consoleLog("	Sending new tab command");
//pause(2000);

consoleLog("	Type: javascript");
String response = (String) ((JavascriptExecutor) activeSeleniumWebDriver).executeScript("window.open('','_blank');");
pause(500);

/*consoleLog("	Type: link");
WebElement link = activeSeleniumWebDriver.findElement(By.linkText("Gmail"));
Actions newTab = new Actions(activeSeleniumWebDriver);
newTab.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT).click(link).keyUp(Keys.CONTROL).keyUp(Keys.SHIFT).build().perform();
Thread.sleep(5000);


consoleLog("	Type: cssSelector");
activeSeleniumWebDriver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"t");
pause(500);
activeSeleniumWebDriver.findElement(By.cssSelector("body")).sendKeys(Keys.chord(Keys.CONTROL, "t"));
pause(500);

consoleLog("	Type: Action");
new Actions(activeSeleniumWebDriver).sendKeys(Keys.CONTROL +"t").perform();
pause(500);
new Actions(activeSeleniumWebDriver).sendKeys(Keys.chord(Keys.CONTROL, "t")).perform();
pause(500);

consoleLog("	Type: tag name");
activeSeleniumWebDriver.findElement(By.tagName("body")).sendKeys(Keys.CONTROL +"t");
pause(500);
activeSeleniumWebDriver.findElement(By.tagName("body")).sendKeys(Keys.chord(Keys.CONTROL, "t"));
pause(500);

consoleLog("	Type id");
activeSeleniumWebDriver.findElement(By.id("lst-ib")).sendKeys(Keys.SHIFT +"t");
pause(500);
activeSeleniumWebDriver.findElement(By.id("lst-ib")).sendKeys(Keys.chord(Keys.SHIFT, "t"));
pause(500);

consoleLog("	Type: builder");
Actions builder = new Actions(activeSeleniumWebDriver);
Action select= builder
		.keyDown(Keys.CONTROL)
		.sendKeys("t")
		.keyUp(Keys.CONTROL)
		.build();
select.perform();
pause(500);

consoleLog("	Type: click");
WebElement tempElement = activeSeleniumWebDriver.findElement(By.cssSelector("body"));
tempElement.click();
tempElement.sendKeys(Keys.chord(Keys.CONTROL, "T"));
pause(500);

consoleLog("	Sending input command");

//consoleLog("	Type: cssSelector");
//activeSeleniumWebDriver.findElement(By.cssSelector("input")).sendKeys("m");
//pause(500);

//consoleLog("	Type: tag name");
//activeSeleniumWebDriver.findElement(By.tagName("input")).sendKeys("m");
//pause(500);

consoleLog("	Type: id");
WebDriverWait wait = new WebDriverWait(activeSeleniumWebDriver, 10000);
wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("lst-ib"))));
activeSeleniumWebDriver.findElement(By.id("lst-ib")).sendKeys("m");
pause(500);

//activeSeleniumWebDriver.switchTo().window((String) activeSeleniumWebDriver.getWindowHandles().toArray()[0]);
//activeSeleniumWebDriver.switchTo().defaultContent();
consoleLog("	New tab command sent");

//Install the stopper extension
String ASRLoadStopperPath = "chromeExtensions/loadStopper.crx";   
try {
	driverOptions.addExtensions(new File(ASRLoadStopperPath));
} catch (Exception e) {
	consoleLog("	FAILED: Unable to load ASRLoadStopper. -" + e.toString() );
}

//		//If an exceptions exists, insert its script into the final string
//		String exceptionScript = "";
//		try {exceptionScript = getAdInjecterException(targetURL); } catch (Exception e) {}
//		if (!exceptionScript.isEmpty()) {
//			finalJS = finalJS.replace("//INSERT EXCEPTION SCRIPT//", exceptionScript);
//		}
//		//Create the tags object by looping through the tags and adding them to the tags string
//		String tagsString = "tags = [";
//		for (TagImage tagImage: tagImageSet) {
//			
//			//build the current tag object and add it to overall object
//			tagsString +=  "{id: '" + tagImage.id() + "', " +
//							"tag: '" + tagImage.url() + "', " +
//							"placement: " + tagImage.priority() + ", " +
//							"width: " + tagImage.width() + ", " +
//							"height: " + tagImage.height() + "},";
//		}
//		tagsString += "];";
//		
//		//Insert the tags into the code by replacing the 'insert tags object' marker with them
//		String finalJS = adInjecterJS.replace("//INSERT TAGS OBJECT//", tagsString);


*/