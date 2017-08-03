package adshotrunner.shotter;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import adshotrunner.campaigns.AdShot;
import adshotrunner.campaigns.Creative;
import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.ASRDatabase;
import adshotrunner.utilities.URLTool;

/**
 * The AdShotter captures images of AdShots and adds them to each respective AdShot.
 * 
 * The AdShotter navigates to the AdShot's candidate URL(s), injects the Creative and exceptions
 * using the Creativeinjecter.js, takes the screenshot, crops the screenshot, and adds the
 * screenshot image to the AdShot.
 * 
 * If an AdShot URL is behind a paywall, the AdShotter will attempt to login to the site
 * before navigating to the URL.
 */
public class AdShotter extends BasicShotter {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//Paths for base CreativerInjecter javascript file
	final private static String CREATIVEINJECTERJSPATH = ASRProperties.pathForAdInjecterJavascript();
	
	//Paths for browser plug-ins
	final private static String ADMARKERPATH = ASRProperties.pathForAdMarkerExtension();
	final private static String CSPDISABLEPATH = "chromeExtensions/chrome-csp-disable-master.crx";
	
	//Driver viewport sizes
	final private static int DESKTOPVIEWWIDTH = 1366;		//in pixels
	final private static int DESKTOPVIEWHEIGHT = 3150;		//in pixels
	final private static int MOBILEVIEWWIDTH = 375;			//in pixels
	final private static int MOBILEPIXELRATIO = 2;			//in pixels
	
	//User agent for the mobile browser
	final private static String MOBILEUSERAGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";
	
	//Timeouts for page loads and javascript execution
	final private static int INITIALMOBILETIMEOUT = 17000;	//in milliseconds
	
	//Below the fold start heights and top padding
	final private static int BTFDESKTOPSTARTHEIGHT = 700;	//in pixels
	final private static int BTFMOBILESTARTHEIGHT = 500;	//in pixels
	final private static int BTFCROPPADDING = 150;			//in pixels
	
	//Crop lengths and bottom margin
	final private static int MINIMUMCROPLENGTH = 1560;		//in pixels
	final private static int MAXIMUMCROPLENGTH = 3000; 		//in pixels
	final private static int CROPBOTTOMMARGIN = 40;			//in pixels

	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	/**
	 * Captures the screenshot of each AdShot with its Creatives and exceptions injected, 
	 * crops the screenshot, and places the image in the AdShot along with its final URL
	 * and page title.
	 * 
	 * @param adShots		Set of AdShots to capture and crop screenshots for
	 */
	static public void captureAdShotImages(Set<AdShot> adShots) {
		
		//If no AdShots were passed, return and do nothing
		if (adShots.size() == 0) {return;}
		
		//Separate the adShots into desktop and mobile sets
		Set<AdShot> desktopAdShots = new LinkedHashSet<AdShot>();
		Set<AdShot> mobileAdShots = new LinkedHashSet<AdShot>();
		for (AdShot currentAdShot : adShots) {
			if (currentAdShot.mobile()) {mobileAdShots.add(currentAdShot);}
			else {desktopAdShots.add(currentAdShot);}
		}
		
		//Capture the AdShots
		if (desktopAdShots.size() > 0) {captureAdShotBrowserGroup(desktopAdShots, false);}
		if (mobileAdShots.size() > 0) {captureAdShotBrowserGroup(mobileAdShots, true);}
	}
	
	//********************************* Private Methods *************************************
	/**
	 * Sets up, captures, and crops the AdShots using either a non-mobile or mobile driver.
	 * 
	 * @param adShots			Set of AdShots to capture and crop screenshots for
	 * @param mobileDriver		TRUE to use a mobile driver and FALSE to use a desktop driver
	 */
	static private void captureAdShotBrowserGroup(Set<AdShot> adShots, boolean mobileDriver) {
		
		//Try to create a web driver to connect with
		WebDriver activeWebDriver = null;
		try {activeWebDriver = getAdShotDriver(mobileDriver);}
		catch (Exception e) {throw new AdShotRunnerException("Could not connect with Selenium server", e);}
						
		//Login to any sites that require it
		setCookiesforSites(activeWebDriver, adShots);
		
		//Loop through the AdShots and process them
		boolean initialPageLoad = true;
		for (AdShot currentAdShot : adShots) {
			
			//Mark the AdShot as being processed.
			currentAdShot.setStatus(AdShot.PROCESSING);
			
			//Loop through candidate URLs
			boolean initialURL = true;
			if ((initialURL) || ((currentAdShot.injectedCreatives().size() == 0) && (currentAdShot.hasNextCandidateURL()))) {
				
				//Get next candidate URL if applicable
				if (initialURL) {initialURL = false;}
				else {currentAdShot.nextCandidateURL();}
				
				//Navigate to the target URL. IF this is the first time and using mobile, add extra time
				int pageLoadTime = PAGETIMEOUT;
//				if ((initialPageLoad) && (mobileDriver)) {
//					pageLoadTime = INITIALMOBILETIMEOUT; initialPageLoad = false;
//				}
				boolean navigationSucceeded = navigateSeleniumDriverToURL(activeWebDriver, currentAdShot.targetURL(), pageLoadTime);
				
				//IF the navigation failed, set the AdShot error
				if (!navigationSucceeded) {
					currentAdShot.setError("UNABLE TO NAVIGATE TO URL");
				}
				
				//Otherwise, get the screenshot
				else {
					createAdShotImage(activeWebDriver, currentAdShot);
				}
			}
			
			//If there was no error, mark the AdShot as finished
			if (!currentAdShot.status().equals(AdShot.ERROR)) {
				currentAdShot.setStatus(AdShot.FINISHED);
			}
		}

		//Quit the driver
		quitWebdriver(activeWebDriver);
	}
	
	
	/**
	 * Injects the Creative and exceptions into the browser, captures and crops
	 * the screenshot, and sets the screenshot image, final URL, and page title
	 * into the AdShot.
	 * 
	 * @param activeWebDriver	Webdriver navigated to AdShot current candidate URL
	 * @param activeAdShot			AdShot to capture screenshot image for
	 */
	static private void createAdShotImage(WebDriver activeWebDriver, AdShot activeAdShot) {
		
		//Scroll down and back up to load lazy ads (ads that only load after the user scrolls)
		scrollToLoadLazyAds(activeWebDriver);
		
		//Get the CreativeInjecter javascript with the creatives inserted into it
		String creativeInjecterJS = "";
		try {creativeInjecterJS = getInjecterJS(activeAdShot);}
		catch (Exception e) {
			activeAdShot.setError("UNABLE TO GET CREATIVEINJECTER JAVASCRIPT"); return;
		}
		
		//Execute the CreativeInjecter javascript
		String injecterResponseJSON = "";
		try {injecterResponseJSON = executeSeleniumDriverJavascript(activeWebDriver, creativeInjecterJS);}
		catch (Exception e) {
			consoleLog("ERROR: Could not execute CreativeInjecter");
			e.printStackTrace();
			activeAdShot.setError("COULD NOT EXECUTE CREATIVEINJECTER"); return;
		}
		
		//Parse the Injecter response, mark which Creatives were injected and 
		//find crop start height and optimal length
		int cropStartHeight = 0;
		int optimalCropLength = MINIMUMCROPLENGTH;
		try {
			
			InjecterResponse responseFromInjecter = new Gson().fromJson(injecterResponseJSON, InjecterResponse.class);
				
			//Output the message log
			consoleLog("\n-----Injecter Script Log-----\n" + responseFromInjecter.outputLog + "\n--------------------------");
			
			//Get the crop start height. Default is 0 unless below-the-fold screenshot.
			cropStartHeight = (activeAdShot.belowTheFold()) ? 
										getBTFCropStartHeight(responseFromInjecter) : 0;
			
			//Get the optimal crop length
			optimalCropLength = getOptimalCropLength(responseFromInjecter, activeAdShot, cropStartHeight);
			
			//Mark the Creatives as injected if they are in the visible range
			for (Map.Entry<Integer, InjecterResponse.Coordinate> injected : responseFromInjecter.injectedCreatives.entrySet()) {
				if ((injected.getValue().y >= cropStartHeight) && 
					(injected.getValue().y <= (cropStartHeight + optimalCropLength))) {
					
					Creative currentCreative = getCreativeByID(injected.getKey(), activeAdShot.creatives());
			    	activeAdShot.creativeInjected(currentCreative);
				}
			}						
		}
		catch (Exception e) {
			consoleLog("------ Unable to Parse Injecter Response ------");
		}
		
		
		//Take the screenshot 
		File screenShot = null;
		try {screenShot = captureSeleniumDriverScreenshot(activeWebDriver);}
		catch (Exception e) {
			activeAdShot.setError("UNABLE TO TAKE SCREENSHOT"); return;
		}
		
		//Store the final URL in the AdShot
		activeAdShot.setFinalURL(activeWebDriver.getCurrentUrl());
		
		//Store the pageTitle in the AdShot
		activeAdShot.setPageTitle(activeWebDriver.getTitle());
		
		//Crop the image
		consoleLog("Crop Start: " + cropStartHeight);
		consoleLog("Crop Length: " + optimalCropLength);
		BufferedImage croppedScreenshot = null;
		int cropWidth = (activeAdShot.mobile()) ? MOBILEVIEWWIDTH : DESKTOPVIEWWIDTH;
		try {croppedScreenshot = cropAdShotScreenshot(screenShot, cropWidth, cropStartHeight, optimalCropLength);}
		catch (Exception e) {
			e.printStackTrace();
			activeAdShot.setError("COULD NOT CROP SCREENSHOT"); return;
		}
		
		//Add the final image to the AdShot
		try {activeAdShot.setImage(croppedScreenshot);}
		catch (Exception e) {
			activeAdShot.setError("COULD NOT SET AND UPLOAD IMAGE"); return;
		}
		
		
	}
		
	/**
	 * Returns a Desktop or mobile Chrome browser (as declared in arguments) with the
	 * AdMarker and CSPDisabler plug-ins loaded.
	 * 
	 * @param mobile		TRUE to use a mobile browser and FALSE to use desktop
	 * @return				Initialized Chrome WebDriver
	 */
	static private WebDriver getAdShotDriver(boolean mobile) throws MalformedURLException {

		//System.setProperty("webdriver.chrome.driver", "chromedriver");
		
		//Begin creating the driver for a Chrome window
		consoleLog("Creating Chrome driver...");
		DesiredCapabilities driverCapabilities = DesiredCapabilities.chrome();
		ChromeOptions driverOptions = new ChromeOptions();
		HashMap<String, Object> chromePreferences = new HashMap<String, Object>();
		
		//Set the driver to use a Windows node
		driverCapabilities.setPlatform(Platform.WINDOWS);
		
		//If the browser needs to be in mobile mode, set the driver options for it
		if (mobile) {
			
			//Define the device metrics to use 
			Map<String, Object> mobileEmulation = new HashMap<String, Object>();
			Map<String, Object> deviceMetrics = new HashMap<String, Object>();
			deviceMetrics.put("width", MOBILEVIEWWIDTH );
			deviceMetrics.put("height", DESKTOPVIEWHEIGHT);
			deviceMetrics.put("pixelRatio", MOBILEPIXELRATIO);
			mobileEmulation.put("deviceMetrics", deviceMetrics);
			
			//Set the user agent
			mobileEmulation.put("userAgent", MOBILEUSERAGENT);	
			
			//Add the mobile information to the driver options
			driverOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
			
			//Turn off flash and the pdf viewer
			chromePreferences.put("plugins.plugins_disabled", new String[] {
				    "Adobe Flash Player",
				    "Chrome PDF Viewer"
				});
		}
		driverOptions.setExperimentalOption("prefs", chromePreferences);
		
		try {
			driverOptions.addExtensions(new File(ADMARKERPATH));
			driverOptions.addExtensions(new File(CSPDISABLEPATH));
		} catch (Exception e) {
			consoleLog("	FAILED: Unable to load AdMarker and Disabled CSP Extensions: " + e.toString() );
		}


		//Initialize the actual driver
		WebDriver chromeDriver = null;
		driverCapabilities.setCapability(ChromeOptions.CAPABILITY, driverOptions);
		chromeDriver = new RemoteWebDriver(
							new URL(SELENIUMHUBURL), 
							driverCapabilities);
		

		//Set the viewport position
		chromeDriver.manage().window().setPosition(new Point(0,20));
		
		//Set the page timeout
		setCommandTimeout(chromeDriver, DEFAULTTIMEOUT);
		
		//If not using mobile, set the viewport size
		if (!mobile) {
			chromeDriver.manage().window().setSize(new Dimension(DESKTOPVIEWWIDTH, DESKTOPVIEWHEIGHT));
		}		
		
		//Return the initialized remote chrome web driver
		consoleLog("Done creating screenshot driver.");
		return chromeDriver;
	} 
	
	
	/**
	 * For any AdShot with a domain behind a paywall, logins the driver browser by navigating
	 * the login page defined by the domain's site login database entry and setting in the browser the
	 * cookies stored in the database entry.
	 * 
	 * @param activeWebDriver		WebDriver to setup logins
	 * @param adShots				Set of AdShots to check for paywall logins
	 */
	static private void setCookiesforSites(final WebDriver activeWebDriver, Set<AdShot> adShots) {
		
		//If no AdShots were passed, do nothing and return
		if ((adShots == null) || (adShots.size() == 0)) {return;}
		
		//Get a set of all the AdShots' domains
		Set<String> domainSet = new HashSet<String>();
		for (AdShot currentAdShot: adShots) {
			domainSet.add(URLTool.getDomain(currentAdShot.targetURL()));
		}
		
		//Check the database to see if any entries matching the domain exist
		try {
			String domainsInClause = "";
			for (String domain : domainSet) {
				if (domainsInClause != "") {domainsInClause += ", ";}
				domainsInClause += "'" + domain + "'";
			}
			
			//Get any cookies for the domain
			try (ResultSet loginCookie = ASRDatabase.executeQuery("SELECT * " + 
																"FROM siteLoginCookies " +
																"WHERE SLC_site IN (" + domainsInClause + ")")) {
					
				//If any matches were found, navigate to the login page and run the login script 
				if (loginCookie.next()) {
					List<Cookie> loginCookies = cookiesFromJSON(loginCookie.getString("SLC_cookiesJSON"));
					addCookiesToSeleniumDriver(activeWebDriver, loginCookie.getString("SLC_loginPage"), loginCookies);
				}
			} catch (Exception e) {
				throw new AdShotRunnerException("Could not query database for cookies: " + domainsInClause, e);
			}				
			
		//If the database was unaccessible, do nothing for the time being
		} catch (Exception e){return;} 
	} 
		
	/**
	 * Navigates the driver to the passed login page and sets the passed cookies in the driver's browser
	 * 
	 * @param activeWebDriver		WebDriver to login
	 * @param loginPage				URL of login page 
	 * @param cookiesToAdd			List of Cookies to set in order to login the browser
	 */
	static private void addCookiesToSeleniumDriver(WebDriver activeWebDriver, String loginPage, List<Cookie> cookiesToAdd) {
		
		//If no cookies were passed, do nothing
		if ((cookiesToAdd == null) || (cookiesToAdd.size() == 0)) {return;}

		//Navigate to the login page
		navigateSeleniumDriverToURL(activeWebDriver, loginPage);
		
		//Add each cookie to the browser
		for(Cookie currentCookie : cookiesToAdd){
			activeWebDriver.manage().addCookie(currentCookie);
		}
	}
	
	/**
	 * Converts a JSON string of Cookies into a list of Cookie objects
	 * 
	 * @param cookieJSON	JSON string of cookies
	 * @return				List of Cookie objects created from passed cookies JSON
	 */
	static private List<Cookie> cookiesFromJSON(String cookieJSON){
		
		Type listType = new TypeToken<ArrayList<Cookie>>(){}.getType();
		return new Gson().fromJson(cookieJSON, listType);		
	}
	
	/**
	 * Scrolls the current browser page down and back up to load lazy ads (ads that
	 * do not load until the user scrolls)
	 * 
	 * @param activeWebDriver		WebDriver to scroll
	 */
	static private void scrollToLoadLazyAds(WebDriver activeWebDriver) {
		
		//Scroll down and back up to load the lazy ads
		executeSeleniumDriverJavascript(activeWebDriver, 
				"window.scrollBy(0, 300); setTimeout(function() {window.scrollBy(0, -300);}, 200);");
		
//		//If the AdShot is below-the-fold, scroll down to the start height
//		if (currentAdShot.belowTheFold()) {
//			int startHeight = (currentAdShot.mobile()) ? BTFMOBILESTARTHEIGHT : BTFDESKTOPSTARTHEIGHT;
//			consoleLog("BELOW THE FOLD: Scrolling to: " + startHeight);
//			pause(2500);
//			executeSeleniumDriverJavascript(activeWebDriver, "window.scrollBy(0, " + startHeight + ");");
//			consoleLog("Done!");
//			pause(2500);
//		}
//		
//		//If the AdShot is NOT below-the-fold, scroll down and back up to load advertisements
//		else {
//			consoleLog("DEFAULT: scrolling 300 down and back up");
//			executeSeleniumDriverJavascript(activeWebDriver, 
//					"window.scrollBy(0, 300); setTimeout(function() {window.scrollBy(0, -300);}, 200);");
//		}
	}

	/**
	 * Returns the CreativeInjecter javascript with the AdShot's Creatives and site exceptions
	 * inserted into it.
	 * 
	 * @param currentAdShot			AdShot to use for inserting Creatives and exceptions
	 * @return						CreativeInjecter javascript string with AdShot Creatives and exceptions inserted
	 */
	static private String getInjecterJS(AdShot currentAdShot) throws IOException {
		
		//Get the CreativeInjecter javascript file
		String creativeInjecterJS = new String(Files.readAllBytes(Paths.get(CREATIVEINJECTERJSPATH)));
		
		//Create the creatives object by looping through the creatives and adding them to the JSON string
		String creativesJSON = "creatives = [";
		for (Creative currentCreative: currentAdShot.creatives()) {
			
			//build the current creative object and add it to overall object
			creativesJSON +=  "{id: '" + currentCreative.id() + "', " +
							  "imageURL: '" + currentCreative.imageURL() + "', " +
							  "width: " + currentCreative.width() + ", " +
							  "height: " + currentCreative.height() + ", " +
							  "priority: " + currentCreative.priority() + "},";
		}
		creativesJSON += "];\n";
		
		//Add the injection start height based on below-the-fold and mobile
		int injectionStartHeight = 0;
		if ((currentAdShot.belowTheFold()) && (!currentAdShot.mobile())) {
			injectionStartHeight = BTFDESKTOPSTARTHEIGHT;
		}
		else if ((currentAdShot.belowTheFold()) && (currentAdShot.mobile())) {
			injectionStartHeight = BTFMOBILESTARTHEIGHT;
		}
		creativesJSON += "injectionStartHeight = " + injectionStartHeight + ";";
		
		//Insert the creatives and start height into the code by 
		//replacing the 'INSERT CREATIVES OBJECT' marker with them
		String finalJS = creativeInjecterJS.replace("//INSERT CREATIVES OBJECT//", creativesJSON);
		
		//If an exceptions exists, insert its script into the final string
		String exceptionScript = "";
		try {exceptionScript = getCreativeInjecterException(currentAdShot.targetURL()); } catch (Exception e) {}
		if (!exceptionScript.isEmpty()) {
			finalJS = finalJS.replace("//INSERT EXCEPTION SCRIPT//", exceptionScript);
		}
		
		FileUtils.writeStringToFile(new File("creativeInjecterWithTags.js"), finalJS);
		
		//Return the modified javascript as a String
		return finalJS;
	}

	/**
	 * Queries the database for a CreativeInjecter site exception script for the passed URL
	 * and returns it if one exists.
	 * 
	 * @param targetURL			URL to query database for an exception script
	 * @return					Exception script for passed URL if one exists, empty string otherwise
	 * @throws SQLException
	 */
	static private String getCreativeInjecterException(String targetURL) throws SQLException {
		
		//Get subdomain of the url. A protocol type is necessary for getSubomain.
		String urlDomain = URLTool.getSubdomain(URLTool.setProtocol("http", targetURL));
		
		//Check the database to see if any entries matching the domain exist
		try (ResultSet exceptionsSet = ASRDatabase.executeQuery("SELECT * " + 
															 "FROM exceptionsCreativeInjecter " +
															 "WHERE EAI_url LIKE '" + urlDomain + "%'")) {
				
			//If a match was found, return the script 
			if (exceptionsSet.next()) {
				return exceptionsSet.getString("EAI_script");
			}
			
		//If the database was unaccessible, do nothing for the time being
		} catch (Exception e){return "";} 
		
		//Otherwise, return an empty string
		return "";
	} 

	/**
	 * Returns the Creative from the passed Creative set matching the passed ID if one exists
	 * and null othwerise.
	 * 
	 * @param creativeID	Creative ID to search for
	 * @param creatives		Creatives to search
	 * @return
	 */
	static private Creative getCreativeByID(int creativeID, Set<Creative> creatives) {
		
		//Loop through the Creatives and return the one matching the ID if found
		for (Creative currentCreative : creatives) {
			if (currentCreative.id() == creativeID) {return currentCreative;}
		}
		
		//If the Creative wasn't found, return null
		return null;
	}
	
	/**
	 * Returns the crop start height for a below-the-fold AdShot based on its injected
	 * Creatives.
	 * 
	 * This function returns the y-coordinate of the highest injected Creative minus
	 * BTFCROPPADDING. If the value is less than 0, 0 is returned. If no Creative
	 * were injected, 0 is returned.
	 * 
	 * @param responseFromInjecter		Locations of injected Creatives from CreativeInjecter
	 * @return							Crop start height for a below-the-fold AdShot
	 */
	static private int getBTFCropStartHeight(InjecterResponse responseFromInjecter) {
		
		//Get the y-coordinate of the heighest injected Creative
		int highestCreativeCoordinate = 0;
		boolean firstComparison = true;
		for (Map.Entry<Integer, InjecterResponse.Coordinate> injected : responseFromInjecter.injectedCreatives.entrySet()) {
			
			//If this is the first comparison, initialize the highest coordinate to it
			if (firstComparison) {
				highestCreativeCoordinate = injected.getValue().y;
				firstComparison = false;
			}
			
			//If the current injected Creative is higher than the highest, use its value
			if (injected.getValue().y < highestCreativeCoordinate) {
				highestCreativeCoordinate = injected.getValue().y;
			}
		}
		
		//Return the highest injected coordinate with padding
		int desiredStartHeight = highestCreativeCoordinate - BTFCROPPADDING;
		return (desiredStartHeight >= 0) ? desiredStartHeight : 0;
	}
	
	/**
	 * Returns the optimal crop length for the screenshot. 
	 * 
	 * If the Creatives were injected in locations less than MINIMUMCROPLENGTH then MINIMUMCROPLENGTH is returned.
	 * 
	 * Otherwise, the functions returns the height of the margin (CROPBOTTOMMARGIN) underneath the lowest Creative
	 * that is still less than than maximum crop length (MAXIMUMCROPLENGTH)
	 * 
	 * @param responseFromInjecter			Locations of injected Creatives from CreativeInjecter
	 * @param currentAdShot					AdShot with Creative injected into the page
	 * @param cropStartHeight				Crop start height
	 * @return								Optimal crop height for the screenshot
	 */
	static private int getOptimalCropLength(InjecterResponse responseFromInjecter, AdShot currentAdShot, 
											int cropStartHeight) {
				
		//Loop through the injected Creatives and use the bottom of the lowest within the crop length limits
		int optimalCropLength = MINIMUMCROPLENGTH;
		for (Map.Entry<Integer, InjecterResponse.Coordinate> injected : responseFromInjecter.injectedCreatives.entrySet()) {
		
		    //Determine the bottom coordinate of the injected creative
		    Creative currentCreative = getCreativeByID(injected.getKey(), currentAdShot.creatives());
		    int bottomCoordinate = injected.getValue().y + currentCreative.height();
		    
		    //If the bottom coordinate is lower than the requested crop length but not lower than maximum, use it
		    int possibleNewLength = bottomCoordinate - cropStartHeight;
		    if ((possibleNewLength > optimalCropLength) && (possibleNewLength <= MAXIMUMCROPLENGTH)) {
		    	optimalCropLength = possibleNewLength;
		    }
		}
		
		//Add margin to crop length
		optimalCropLength += CROPBOTTOMMARGIN;
		
		//Return the optimal crop length
		return optimalCropLength;
	}
		
	
	/**
	 * Crops the screenshot according to the passed width and length
	 * 
	 * @param originalImageFile			Original screenshot captured by driver
	 * @param cropWidth					Width of final crop
	 * @param cropStartHeight			Height at which to begin the crop
	 * @param cropLength				Length of final crop
	 * @return							Cropped screenshot image
	 */
	static private BufferedImage cropAdShotScreenshot(File originalImageFile, int cropWidth, 
													  int cropStartHeight, int cropLength) throws IOException {
		
		//Verify the crop width is within the image width
		BufferedImage originalImage = ImageIO.read(originalImageFile);
		int finalWidth = (cropWidth < originalImage.getWidth()) ? cropWidth : originalImage.getWidth();
		
		//Verify the crop length is within the image length
		int finalHeight = ((cropLength + cropStartHeight) < originalImage.getHeight()) ? 
											cropLength : (originalImage.getHeight() - cropStartHeight);
		
		//Crop the image
		BufferedImage croppedImage = originalImage.getSubimage(0, cropStartHeight, finalWidth, finalHeight);
		
		//Make BufferedImage generic so it can be written as png or jpg
		BufferedImage cleanedImage = new BufferedImage(croppedImage.getWidth(),
													   croppedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
		cleanedImage.createGraphics().drawImage(croppedImage, 0, 0, Color.WHITE, null);
		
		//Return the modified image
		return croppedImage;
	}
	

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	private AdShotter() {}
}

