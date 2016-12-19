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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	final private static String SELENIUMHUBADDRESS = "http://ec2-54-172-131-29.compute-1.amazonaws.com:4444/wd/hub";
	final private static String ADINJECTERJSPATH = "javascript/adInjecter.js";
	final private static int JAVASCRIPTWAITTIME = 2000;		//in milliseconds
	final private static int SCREENSHOTATTEMPTS = 3;
	final private static int SCREENSHOTTIMEOUT = 30000;		//in milliseconds
	//final private static int DEFAULTWINDOWHEIGHT = 1366;		//in pixels
	//final private static int DEFAULTWINDOWWIDTH = 2800;		//in pixels
	final private static int DEFAULTVIEWWIDTH = 1366;		//in pixels
	final private static int DEFAULTVIEWHEIGHT = 2800;		//in pixels
	final private static int MOBILEVIEWWIDTH = 360;			//in pixels
	final private static int MOBILEPIXELRATIO = 3;			//in pixels
	final private static int DEFAULTCROPHEIGHT = 2600; ;	//in pixels
	final private static int MAXCROPHEIGHT = 3000; 			//in pixels
	//final private static List<Integer> DEFAULTVIEWPORT = Collections.unmodifiableList(Arrays.asList(1366, 2800));
	//final private static List<Integer> MOBILEVIEWPORT = Collections.unmodifiableList(Arrays.asList(320, 640));
	final private static String MOBILEUSERAGENT = "Mozilla/5.0 (Linux; Android 4.2.1; en-us; Nexus 5 Build/JOP40D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";
	final private static boolean VERBOSE = true;
	final private static int MAXOPENTABS = 4;


	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Variables -------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Variables *********************************
	public static AdShotter3 create() {
		return new AdShotter3(DEFAULTVIEWWIDTH, DEFAULTVIEWHEIGHT);
	}

	public static AdShotter3 create(int viewWidth, int viewHeight) {
		return new AdShotter3(viewWidth, viewHeight);
	}

	public static AdShotter3 createForMobile() {
		return new AdShotter3(MOBILEVIEWWIDTH, DEFAULTVIEWHEIGHT, true);
	}


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
		
		this(viewWidth, viewHeight, false);
	}

	/**
	 * Instantiates object with the passed viewport and user agent string
	 * 
	 * If the userAgent string is empty or null, the browser's default will be used.
	 * 
	 * @param viewWidth		Width of browser viewport
	 * @param viewHeight	Height of browser viewport
	 * @param useMobile		Flags whether or not to use a mobile driver
	 */
	private AdShotter3(int viewWidth, int viewHeight, Boolean useMobile) {
		
		_browserViewWidth = viewWidth;
		_browserViewHeight = viewHeight;
		_useMobile = useMobile;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************	
	public void takeAdShots(List<AdShot> adShots) {
		takeAdShots(adShots, false);
	}
	
	public void takeAdShots(List<AdShot> adShots, boolean treatAsTags) {
				
		//Try to create a web driver to connect with
		WebDriver remoteWebDriver = null;
		try {remoteWebDriver = getSeleniumChromeDriver();}
		catch (Exception e) {throw new AdShotRunnerException("Could not connect with Selenium server", e);}
		
		//Open each AdShot URL in a new tab up to the max tab amount
		consoleLog("Beginning to open tabs...");
		int numberOfRequiredTabs = (adShots.size() < MAXOPENTABS) ? adShots.size() : MAXOPENTABS;
		ArrayList<String> tabHandles = getTabs(remoteWebDriver, numberOfRequiredTabs);
		consoleLog("Done opening tabs.");
		
		//Load the initial pages into the tabs. 
		consoleLog("Loading initial pages...");
		Iterator<String> tabIterator = tabHandles.iterator();
		int adShotIndex = 0;
		long tabStartTime = System.nanoTime();
		while (tabIterator.hasNext()) {
			
			//Switch to the next tab
			String nextTabHandler = tabIterator.next();
			consoleLog("	Beginning content switch to next tab: " + nextTabHandler);
			remoteWebDriver.switchTo().window(nextTabHandler);
			remoteWebDriver.switchTo().defaultContent();
			consoleLog("	Done switching content to next tab.");
			
			//Navigate to the initial page
			consoleLog("	Navigating to initial page: " + adShots.get(adShotIndex).url());
			try {navigateSeleniumDriverToURL(remoteWebDriver, adShots.get(adShotIndex).url());} 
			catch (Exception e) {
				consoleLog("	Couldn't navigate to page: " + adShots.get(adShotIndex).url());
				adShots.get(adShotIndex).setError(new AdShotRunnerException("Could not navigate to URL", e));
			}
			
			//Iterate to the next AdShot
			++adShotIndex;
		}
		
		//Pause if still in the load time frame
		int maxLoadTime = (treatAsTags) ? 7000 : 7000;
		int currentPageLoadTime = (int) ((System.nanoTime() - tabStartTime)/1000000);	//Divide by a million to move nanoseconds to miliseconds
		if (currentPageLoadTime < maxLoadTime) {
			pause(maxLoadTime - currentPageLoadTime);
		}
		consoleLog("Done loading pages.");
		
		//Loop through each AdShot and take the screenshot
		tabIterator = tabHandles.iterator();
		for (adShotIndex = 0; adShotIndex < adShots.size(); ++adShotIndex) {
			consoleLog("------------- New AdShot --------------");

			if ((adShots.size() > 1) && (MAXOPENTABS > 1)) {
				String nextTabHandler = tabIterator.next();
				remoteWebDriver.switchTo().window(nextTabHandler);
				remoteWebDriver.switchTo().defaultContent();
				consoleLog("Switching to next tab:" + nextTabHandler);
				focusTab(remoteWebDriver);
				consoleLog("Done switching to next tab.");
				if (!tabIterator.hasNext()) {tabIterator = tabHandles.iterator();}
			}			

			long startTime = System.nanoTime();
			AdShot currentAdShot = adShots.get(adShotIndex);
			consoleLog("URL: " + currentAdShot.url());
			takeAdShot(remoteWebDriver, currentAdShot, treatAsTags);
			
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
					
					takeAdShot(remoteWebDriver, currentAdShot, treatAsTags);
				}
			}
			
			//If there is an AdShot that needs to be loaded still,
			//put it in the current tab and navigate to the next
			if ((adShotIndex + MAXOPENTABS) < adShots.size()) {
				try {navigateSeleniumDriverToURL(remoteWebDriver, adShots.get(adShotIndex + MAXOPENTABS).url());} 
				catch (Exception e) {
					consoleLog("Couldn't navigate to page: " + adShots.get(adShotIndex).url());
					adShots.get(adShotIndex).setError(new AdShotRunnerException("Could not navigate to URL", e));
				}	
			}
						
			long endTime = System.nanoTime();
			consoleLog("Total Adshot runtime: " + (endTime - startTime)/1000000 + " ms");
			consoleLog("------------------------------");
		}
		
		//Quit the driver
		quitWebdriver(remoteWebDriver);
		
		return;
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
	private void takeAdShot(WebDriver activeSeleniumWebDriver, AdShot adShot, boolean treatAsTag) throws AdShotRunnerException {
		
		//If the AdShot is not to be treated like a tag, create and inject javascript
		int lowestTagBottom = 0;
		if (!treatAsTag) {
					
			//First, get the AdInjecter javascript with the current tags inserted
			consoleLog("Creating Injecter JS...");
			String adInjecterJS = "";
			try {
				adInjecterJS = getInjecterJS(adShot.tagImages());
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
			
			//Mark which ads were injected and store the lowest tags bottom position
			Type injecterJSONType = new TypeToken<HashMap<String, ArrayList<String>>>(){}.getType();
			Map<String, List<String>> injectedTagInfo = new Gson().fromJson(injecterResponse, injecterJSONType);
			if ((injectedTagInfo != null) && (injectedTagInfo.containsKey("injectedTagIDs"))) {
				adShot.markTagImageAsInjected(injectedTagInfo.get("injectedTagIDs"));
				lowestTagBottom = Integer.parseInt(injectedTagInfo.get("lowestTagPosition").get(0));
				consoleLog("	Injected Tags Size: " + adShot.injectedTagImages().size());
				consoleLog("	Bottom position: " + lowestTagBottom);
			}
			else {
				consoleLog("------------------- Javascript returned empty String -------------------------");
			}
			consoleLog("Done injecting JS.");
		}
		
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
		
		//Crop the image
		consoleLog("Cropping screenshot...");
		try {
			adShot.setImage(cropAndConvertImageFile(activeSeleniumWebDriver, screenShot, lowestTagBottom, treatAsTag));
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
		}
		
		//Set the proxy to use
		String proxyDetails = getProxyDetails();
		Proxy chromeProxy = new Proxy();
		chromeProxy.setProxyType(ProxyType.MANUAL);
		chromeProxy.setSslProxy(proxyDetails);
		chromeProxy.setHttpProxy(proxyDetails);
		driverCapabilities.setCapability(CapabilityType.PROXY, chromeProxy);
		
		//Install the AdMarker extension to mark ad elements
		String AdMarkerPath = "chromeExtensions/adMarker.crx";   
		try {
			driverOptions.addExtensions(new File(AdMarkerPath));
		} catch (Exception e) {
			consoleLog("	FAILED: Unable to load AdMarker. -" + e.toString() );
		}

		//Initialize the actual driver
		WebDriver chromeDriver = null;
		driverCapabilities.setCapability(ChromeOptions.CAPABILITY, driverOptions);
		chromeDriver = new RemoteWebDriver(
							new URL(SELENIUMHUBADDRESS), 
							driverCapabilities);
		

		//Set the viewport position
		chromeDriver.manage().window().setPosition(new Point(0,0));
		
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
		
		consoleLog("		Sending navigation command...");
		String navigateScript = "setTimeout(function(){window.location.href='" + pageURL + "';}, 200);";
		((JavascriptExecutor) activeSeleniumWebDriver).executeScript(navigateScript);
		pause(200);
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
		pause(100);

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
	private String getProxyDetails(){
		
		return "dangerpenguins.shader.io:60000";		
	}

	/**
	 * Returns the AdInjecter javascript as a String with the passed tags inserted into it
	 * 
	 * @param tags			Tags and their placement rankings
	 * @return				AdInjecter javascript with passed tags inserted into it
	 */
	private String getInjecterJS(Set<TagImage> tagImageSet) throws IOException {
		
		//Get the AdInjecter javascript file
		String adInjecterJS = new String(Files.readAllBytes(Paths.get(ADINJECTERJSPATH)));
		
		//Create the tags object by looping through the tags and adding them to the tags string
		String tagsString = "tags = [";
		for (TagImage tagImage: tagImageSet) {
			
			//build the current tag object and add it to overall object
			tagsString +=  "{id: '" + tagImage.id() + "', " +
							"tag: '" + tagImage.url() + "', " +
							"placement: " + tagImage.priority() + ", " +
							"width: " + tagImage.width() + ", " +
							"height: " + tagImage.height() + "},";
		}
		tagsString += "];";
		
		//Insert the tags into the code by replacing the 'insert tags object' marker with them
		String finalJS = adInjecterJS.replace("//INSERT TAGS OBJECT//", tagsString);
		FileUtils.writeStringToFile(new File("adInjecterWithTags.js"), finalJS);
		
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
				consoleLog("	FAILED: Error getting screenshot. -" + e.toString() );
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
	private BufferedImage cropAndConvertImageFile(final WebDriver activeSeleniumWebDriver, File originalImageFile, int requestedCropHeight, boolean treatAsTag) throws IOException {
		
		//If this image is a tag, crop it to the tag width and height
		BufferedImage originalImage = ImageIO.read(originalImageFile);
		int cropHeight = DEFAULTCROPHEIGHT;
		int cropWidth = DEFAULTVIEWWIDTH;
		if (treatAsTag) {
			
			//Get the tag height and width
			WebElement tagDiv = activeSeleniumWebDriver.findElement(By.id("adTagContainer"));
			cropHeight = tagDiv.getSize().getHeight();
			cropWidth = tagDiv.getSize().getWidth();
			consoleLog("tagDiv dimensions: " + cropHeight + "x" + cropWidth);
		}
		
		//Otherwise use the requested crop height and default width
		else {
			
			//Determine the crop height
			cropHeight = ((requestedCropHeight < 1) || (requestedCropHeight < DEFAULTCROPHEIGHT)) ? 
					DEFAULTCROPHEIGHT : requestedCropHeight;
			cropHeight = (cropHeight < MAXCROPHEIGHT) ? cropHeight : MAXCROPHEIGHT;
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
	 * @param activeSeleniumWebDriver
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
*/