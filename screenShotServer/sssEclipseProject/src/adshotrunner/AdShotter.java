package adshotrunner;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.firefox.internal.ProfilesIni;

import adshotrunner.errors.AdShotRunnerException;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

/**
 * The AdShotter class inserts passed tags into webpages and returns screenshots.
 * 
 * Multiple unique tags, optionally ranked, can be supplied for the same URL. 
 *
 */
public class AdShotter {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//final private static String SELENIUMHUBADDRESS = "http://localhost:4444/wd/hub";
	final private static String SELENIUMHUBADDRESS = "http://ec2-54-172-131-29.compute-1.amazonaws.com:4444/wd/hub";
	final private static String SELENIUMPROFILE = "SeleniumDPI";
	final private static String ADINJECTERJSPATH = "javascript/adInjecter.js";
	final private static String FIREFOXPROFILEFATH = "/home/ec2-user/seleniumdpi";
	final private static int PAGELOADTIME = 300;			//in miliseconds
	final private static int ESCAPEATTEMPTTIME = 2000;		//in miliseconds
	final private static int ESCAPEPAUSETIME = 100;			//in miliseconds
	final private static int JAVASCRIPTWAITTIME = 2000;		//in miliseconds
	final private static int SCREENSHOTATTEMPTS = 3;
	final private static int SCREENSHOTTIMEOUT = 12000;		//in miliseconds
	final private static int DEFAULTVIEWWIDTH = 1366;		//in pixels
	final private static int DEFAULTVIEWHEIGHT = 768;		//in pixels


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
	/**
	 * Overall list of tags by path associated with their dimensions. 
	 * 
	 * The inner map needs to have two named elements: 'height' and 'width'
	 * 
	 * Each points to the tags respective dimension in pixels.
	 */
	private HashMap<String, Map<String, Integer>> _tagDimensions;
	
	/**
	 * List of URLs and their associated tags. 
	 * 
	 * The inner map is for each tag path with its placement ranking.
	 */
	private HashMap<String, Map<String, Integer>> _urlTags;
	
	/**
	 * View width to set the browser. If not specified in constructor, set to DEFAULTVIEWWIDTH.
	 */
	private int _browserViewWidth;
	
	/**
	 * View height to set the browser. If not specified in constructor, set to DEFAULTVIEWHEIGHT.
	 */
	private int _browserViewHeight;


	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Instantiates basic private variables. Nothing more.
	 */
	public AdShotter() {
		
		this(DEFAULTVIEWWIDTH, DEFAULTVIEWHEIGHT);		
	}

	public AdShotter(int viewWidth, int viewHeight) {
		
		_tagDimensions = new HashMap<String, Map<String, Integer>>();
		_urlTags = new HashMap<String, Map<String, Integer>>();
		_browserViewWidth = viewWidth;
		_browserViewHeight = viewHeight;
		
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************
	/**
	 * Adds a tag and its URL to the instance. A URL with its associated tags will be
	 * processed through the AdInjecter.
	 * 
	 * The placement rank is automatically set to 0, the most prominent ranking
	 * 
	 * @param targetURL			The URL the tags should be placed upon
	 * @param tagPath			Path to the tag's image to be inserted into page
	 * @return					True on success and false on failure
	 */
	public boolean addTag(String targetURL, String tagPath) {
		
		return addTag(targetURL, tagPath, 0);
	}

	/**
	 * Adds a tag and its URL to the instance. A URL with its associated tags will be
	 * processed through the AdInjecter.
	 * 
	 * @param targetURL			The URL the tags should be placed upon
	 * @param tagPath			Path to the tag's image to be inserted into page
	 * @param placementRank		Placement rank of tag, a lower number is more likely to be shown prominently
	 * @return					True on success and false on failure
	 */
	public boolean addTag(String targetURL, String tagPath, int placementRank) {
		
		//Begin by registering the tag
		registerTag(tagPath);
		
		//If the URL does not exist yet, add it the overall list
		if (!_urlTags.containsKey(targetURL)) {
			_urlTags.put(targetURL, new HashMap<String, Integer>());
		}
		
		//If the tag already exists, delete it
		Map<String, Integer> targetURLTags = _urlTags.get(targetURL);
		if (!targetURLTags.containsKey(tagPath)) {
			targetURLTags.remove(tagPath);
		}
		
		//Add the tag to the URL
		targetURLTags.put(tagPath, placementRank);
		
		return true;
	}
	
	/**
	 * Removes a URL and all of its tags from the instance.
	 * 
	 * If the URL does not occur in the instance, nothing happens.
	 * 
	 * @param targetURL		URL to remove from instance
	 * @return				True on sucess and false on failure
	 */
	public boolean removeURL(String targetURL) {
		
		//If the URL exists, delete it from the overall list.
		if (_urlTags.containsKey(targetURL)) {
			_urlTags.remove(targetURL);
		}
		return true;
	}
	
	public Map<String, BufferedImage> getAdShots() {
		//public HashMap<BufferedImage, HashMap<String, String>> getAdShots() {
		        
		//Create the final data structure to store ad shots within
		HashMap<BufferedImage, HashMap<String, String>> adShots = new HashMap<BufferedImage, HashMap<String, String>>();
		
		//Try to create a web driver to connect with
		WebDriver firefoxDriver = null;
		try {
			firefoxDriver = getSeleniumDriver();
		}
		//On failure, throw runtime error
		catch (Exception e) {
			throw new AdShotRunnerException("Could not connect with Selenium server", e);
		}
        
        //Loop through each URL, inject the ads, and grab the screenshots
		Map<String, BufferedImage> finalAdShots = new HashMap<String, BufferedImage>();
        for(Map.Entry<String, Map<String, Integer>> currentURL: _urlTags.entrySet()) {
        	
			System.out.println(currentURL.getKey() + ":");
			long startTime = System.nanoTime();
			Exception adShotException = null;
			BufferedImage adShotImage = null;
			
			try {adShotImage = takeAdShot(firefoxDriver, currentURL.getKey(), currentURL.getValue());}
			catch(Exception e) {adShotException = e;}
        	
			System.out.print("Exception: ");
			System.out.println(adShotException);			
			
        	long endTime = System.nanoTime();
        	System.out.print("Running time: ");
			System.out.println((endTime - startTime)/1000000 + " ms");
			
			//If there was an error and we are not connected to a window, reconnect and try one more time.
			//System.out.println("Exception and not connected: " + ((adShotException != null) && (!webdriverIsConnectedToWindow(firefoxDriver))));
			if ((adShotException != null) && (!webdriverIsConnectedToWindow(firefoxDriver))) {
				
				//Try to quit the current driver
				/*System.out.print("Attempting to quit broken driver...");
				try {firefoxDriver.quit();}
				catch (Exception e) {
					System.out.print(e + "...");
				} //On error, do nothing
				System.out.println("Done.");*/
				quitWebdriver(firefoxDriver);
				
				//Try to get a new driver
				System.out.print("Attempting to get new driver...");
				try {firefoxDriver = getSeleniumDriver();}
				catch (Exception e) {
					System.out.print(e + "...");
				} //Still ignore any new error
				System.out.println("Done.");
				
				//If we were able to get a new working driver, try one more time to get the AdShot
				System.out.println("Working Driver: " + (webdriverIsConnectedToWindow(firefoxDriver)));
				if (webdriverIsConnectedToWindow(firefoxDriver)) {
					try {adShotImage = takeAdShot(firefoxDriver, currentURL.getKey(), currentURL.getValue());}
					catch(Exception e) {adShotException = e;}
					
					//On fail, just try to restart the webdriver again
					System.out.println("Exception and not connected again: " + ((adShotException != null) && (!webdriverIsConnectedToWindow(firefoxDriver))));
					if ((adShotException != null) && (!webdriverIsConnectedToWindow(firefoxDriver))) {
						
						//Try to quit the current driver
						quitWebdriver(firefoxDriver);
						
						//Try to get a new driver
						System.out.print("Attempting to get new driver...");
						try {firefoxDriver = getSeleniumDriver();}
						catch (Exception e) {
							System.out.print(e + "...");
						} //Still ignore any new error
						System.out.println("Done.");
					}
				}
			}
			else if (adShotException != null) {
				System.out.println("Attempting AdShot again...");
				try {adShotImage = takeAdShot(firefoxDriver, currentURL.getKey(), currentURL.getValue());}
				catch(Exception e) {adShotException = e;}
			}
			
			//If a screenshot was taken without error, keep it to return
			if ((adShotException == null) && (adShotImage != null)) {
				finalAdShots.put(currentURL.getKey(), adShotImage);
			}
			
			
        }
		
    	
    	//Quit the driver
        quitWebdriver(firefoxDriver);
    	//firefoxDriver.quit();
    	
		/*System.out.print("Final webdriver quit...");
    	final WebDriver finalDriver = firefoxDriver;
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
			System.out.print(e + "...");
		}
		System.out.println("Done.");*/
        
		return finalAdShots;
	}

	public Map<String, BufferedImage> getTagShots() {
		
		//Try to create a web driver to connect with
		WebDriver firefoxDriver = null;
		try {
			firefoxDriver = getSeleniumDriver();
		}
		//On failure, throw runtime error
		catch (Exception e) {
			throw new AdShotRunnerException("Could not connect with Selenium server", e);
		}
        
		//Open each tag into its own tab
		Set<String> listOfURLs = _urlTags.keySet();
		Iterator<String> urlIterator = listOfURLs.iterator();
		while (urlIterator.hasNext()) {
			String currentURL = urlIterator.next();
			
        	//Open the current URL within the PAGELOADTIMEOUT time (if we haven't had an error)
        	try {navigateSeleniumDriverToURL(firefoxDriver, currentURL);} 
    		catch (Exception e) {
    			System.out.println("Couldn't navigate to page: " + currentURL);
    		}
        	
        	//If there is another URL after this one, open a new tab
        	if (urlIterator.hasNext()) { 
	        	firefoxDriver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"t");
        	}
		}	
		
		//Go to the first tab
		firefoxDriver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"\t");
				
		Map<String, BufferedImage> finalAdShots = new HashMap<String, BufferedImage>();
		urlIterator = listOfURLs.iterator();
		while (urlIterator.hasNext()) {
			
			String currentURL = urlIterator.next();
						
	    	//Send esc to stop any final loading and close possible popups (if we haven't had an error)
			try {sendSeleniumDriverEscapeCommand(firefoxDriver, ESCAPEATTEMPTTIME);}
    		catch (Exception e) {
    			System.out.println("Couldn't send escape command");
    		}
			
	    	//Take the screenshot 
			System.out.print("Taking screenshot...");
			long screenShotStartTime = System.nanoTime();
			
	    	File screenShot = null;
        	try {screenShot = captureSeleniumDriverScreenshot(firefoxDriver);}
			catch (Exception e) {
				System.out.println("Could not take screenshot");
			}
			
	    	long screenShotEndTime = System.nanoTime();
			System.out.print("Done! - ");
			System.out.println((screenShotEndTime - screenShotStartTime)/1000000 + " ms");

			BufferedImage originalImage = null;
			try {
				originalImage = ImageIO.read(screenShot);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Couldn't convert screenshot file to image");
			}
			
			finalAdShots.put(currentURL, originalImage);
			firefoxDriver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"\t");

		}
        quitWebdriver(firefoxDriver);

		return finalAdShots;
	}

	//******************************** Protected Methods ************************************


	//********************************* Private Methods *************************************
	/**
	 * Registers the passed tag path as a tag within the system. 
	 * 
	 * The tag as its path is stored and its image retrieved to determine its 
	 * dimensions, also stored.
	 * 
	 * @param tagPath	Path to tag image
	 * @return			True on success and false on failure
	 */
	public boolean registerTag(String tagPath) {
		
		//If the current tag set does not yet include the passed tag, add it
		if (!_tagDimensions.containsKey(tagPath)) {
			
			//Get the image
			BufferedImage tagImage = null;
			try {
				URL tagURL = new URL(tagPath);
				tagImage = ImageIO.read(tagURL); 
			}
	        catch (IOException e) {
	        	//Add before production
	        	return false;
	        }
			
			//Get the image's width and height
			int tagWidth = tagImage.getWidth();
			int tagHeight = tagImage.getHeight();
			
			//Add the tag and its dimensions to the class list
			HashMap<String, Integer> currentDimensions = new HashMap<String, Integer>();
			currentDimensions.put("width", tagWidth);
			currentDimensions.put("height", tagHeight);
			_tagDimensions.put(tagPath, currentDimensions);
		}
	
		return true;
	}
	
	/**
	 * Takes and returns an AdShot for the passed URL with the passed tags.
	 * 
	 * Navigates driver to URL, stops page loading (if needed), executes AdInjecter
	 * javascript, and takes a final screenshot.
	 * 
	 * @param activeSeleniumWebDriver	Selenium web driver to navigate
	 * @param URL						URL to navigate to and use for AdShot
	 * @param tags						Tags to attempt to inject into URL page
	 * @return							Screenshot of URL with tags injected in JPG format
	 * @throws AdShotRunnerException 	Error and description if taking the ad screenshot failed
	 */
	private BufferedImage takeAdShot(WebDriver activeSeleniumWebDriver, String URL, Map<String,Integer> tags) throws AdShotRunnerException {
		
		//Create an empty Exception to store any errors
		AdShotRunnerException adShotException = null;
		
    	//First, get the AdInjecter javascript with the current tags inserted
		String adInjecterJS = "";
		try {adInjecterJS = getInjecterJS(tags);}
		catch (Exception e) {
			adShotException = new AdShotRunnerException("Could not create AdInjecter javascript", e);
		}
    	
    	//Open the current URL within the PAGELOADTIMEOUT time (if we haven't had an error)
		if (adShotException == null) {
        	try {navigateSeleniumDriverToURL(activeSeleniumWebDriver, URL);} 
    		catch (Exception e) {
    			adShotException = new AdShotRunnerException("Could not navigate Selenium driver to page: " + URL, e);
    		}
		}
    	        	
    	//Send esc to stop any final loading and close possible popups (if we haven't had an error)
		if (adShotException == null) {
			try {boolean escapeSuccessful = sendSeleniumDriverEscapeCommand(activeSeleniumWebDriver, ESCAPEATTEMPTTIME);}
    		catch (Exception e) {
    			adShotException = new AdShotRunnerException("Could not perform escape key press", e);
    		}
		}
		
    	//Execute the javascript
		if (adShotException == null) {
			try {String javascriptResponse = executeSeleniumDriverJavascript(activeSeleniumWebDriver, adInjecterJS);}
			catch (Exception e) {
    			adShotException = new AdShotRunnerException("Could not execute AdInjecter in page", e);
			}
		}
		
    	//Take the screenshot 
		System.out.print("Taking screenshot...");
		long screenShotStartTime = System.nanoTime();
		
    	File screenShot = null;
		if (adShotException == null) {
        	try {screenShot = captureSeleniumDriverScreenshot(activeSeleniumWebDriver);}
			catch (Exception e) {
				adShotException = new AdShotRunnerException("Could not take screenshot", e);
			}
		}
		
    	long screenShotEndTime = System.nanoTime();
		System.out.print("Done! - ");
		System.out.println((screenShotEndTime - screenShotStartTime)/1000000 + " ms");
		
		//Crop the image
		BufferedImage screenShotImage = null;
		if (adShotException == null) {
			try {screenShotImage = cropAndConvertImageFile(screenShot, 1200);}
			catch (Exception e) {
				adShotException = new AdShotRunnerException("Could not crop screenshot", e);
			}
		}
    	
		//Save the image
		/*if (adShotException == null) {
			try {saveImageAsPNG(screenShotImage, "ScreenShot" + System.nanoTime() + ".png");}
			catch (Exception e) {
				adShotException = new AdShotRunnerException("Could not save screenshot", e);
			}
		}*/
		
		//I am unsure why I built the error-checking originally using if-thens instead of
		//try-catchs. In case there was a reason, I am keeping the code as is and throwing
		//the error here.
		if (adShotException != null) {throw adShotException;}
		
		return screenShotImage;
	}
	
	/**
	 * Returns an initialized selenium webdriver. 
	 *
	 * The selenium address used is set to the class constant SELENIUMHUBADDRESS.
	 * The view dimensions are set to the class constants VIEWWIDTH and VIEWHEIGHT.
	 * The page load time (until an error is thrown) is set to the class constant PAGELOADTIME.
	 * 
	 * @return			Initialized selenium webdriver
	 */
	private WebDriver getSeleniumDriver() throws MalformedURLException {
		
		//Attempt to create the actual web driver used to connect to selenium
		
        //added by matt to test proxy settings and authentication issues
		//FirefoxProfile ffProfile = new FirefoxProfile(new File(FIREFOXPROFILEFATH));
		FirefoxProfile ffProfile = new FirefoxProfile();
        
		//proxy
        // Direct = 0, Manual = 1, PAC = 2, AUTODETECT = 4, SYSTEM = 5
        ffProfile.setPreference("network.proxy.type", 1);
        ffProfile.setPreference("network.proxy.http", "192.210.148.231");
        ffProfile.setPreference("network.proxy.http_port", 3128);
        ffProfile.setPreference("network.proxy.socks_remote_dns", false);
        
        //profile data
        ffProfile.setPreference("app.update.auto", false);
        ffProfile.setPreference("app.update.enabled", false);
        ffProfile.setPreference("browser.privatebrowsing.autostart", true);
        ffProfile.setPreference("browser.shell.checkDefaultBrowser", false);
        ffProfile.setPreference("browser.tabs.warnOnClose", false);
        ffProfile.setPreference("privacy.trackingprotection.pbmode.enabled", false);
        ffProfile.setPreference("extensions.blocklist.enabled", false);
        //these are to make the screenshots look pretty
        ffProfile.setPreference("gfx.direct2d.disabled", true);
        ffProfile.setPreference("layers.acceleration.disabled", true);
        ffProfile.setPreference("gfx.font_rendering.cleartype_params.cleartype_level", 2);
        ffProfile.setPreference("gfx.font_rendering.cleartype_params.enhanced_contrast", 2);
        ffProfile.setPreference("gfx.font_rendering.cleartype_params.gamma", 2);
        ffProfile.setPreference("gfx.font_rendering.cleartype_params.pixel_structure", 2);
        ffProfile.setPreference("gfx.font_rendering.cleartype_params.rendering_mode", 2);
        
        //install Ad Marker extension
        String AdMarkerPath = "/home/ec2-user/ffExtensions/adMarker.xpi";
        
        try {
			ffProfile.addExtension(new File(AdMarkerPath));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //set new firefox profile to be used in selenium
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        //capabilities.setCapability(FirefoxDriver.PROFILE, ffProfile);
		
		WebDriver firefoxDriver = null;
        firefoxDriver = new RemoteWebDriver(
		    			new URL(SELENIUMHUBADDRESS), 
		    			capabilities);
        
        //Set the viewport size and the time to load before sending an error
        firefoxDriver.manage().window().setSize(new Dimension(_browserViewWidth, _browserViewHeight));
        firefoxDriver.manage().timeouts().pageLoadTimeout(PAGELOADTIME, TimeUnit.MILLISECONDS);
        
        
        //Return the initialized remote firefox web driver
        return firefoxDriver;
	}
	
	/**
	 * Navigates the passed selenium driver to the passed URL.
	 * 
	 * The function will automatically timeout at the time passed time or run until that much
	 * time has passed. If the passed maximum load time is less than the constant PAGELOADTIME, it
	 * will be set to PAGELOADTIME.
	 * 
	 * @param activeSeleniumWebDriver	Selenium web driver to navigate
	 * @param pageURL					Page URL to navigate to
	 * @param maxLoadTime				Maxium load time to allow. Set to PAGELOADTIME if less than it. (in miliseconds)
	 */
	private void navigateSeleniumDriverToURL(WebDriver activeSeleniumWebDriver, String pageURL, int maxLoadTime) {
		
		//If the max load time is shorter than the class constant PAGELOADTIME, set it to PAGELOADTIME
		maxLoadTime = (maxLoadTime > PAGELOADTIME) ? maxLoadTime : PAGELOADTIME;  
		
    	//Mark the time we start loading the page
    	long pageLoadStartTime = System.nanoTime();
    	
    	//Attempt to load the passed page
    	try {activeSeleniumWebDriver.get("http://" + pageURL);} 
    	
    	//If the timeout is reached, just keep moving
    	catch (TimeoutException e) {
    		//Just keep moving if the timeout is reached
    	}
    	
    	//If the page seem to load before the load time, give it the difference as extra time through sleep
    	finally {
    		
        	//If the current load time is less that maximum load time, sleep the difference
        	int currentPageLoadTime = (int) ((System.nanoTime() - pageLoadStartTime)/1000000);	//Divide by a million to move nanoseconds to miliseconds
        	if (currentPageLoadTime < maxLoadTime) {
	        	try {
					Thread.sleep(maxLoadTime - currentPageLoadTime);
				} 
	        	catch (InterruptedException e) {
					//If the sleep was interrupted, just keep moving
				} 
        	}
    	}
	}
	
	/**
	 * Navigates the passed selenium driver to the passed URL.
	 * 
	 * The function will automatically timeout at the PAGELOADTIME or run until that much
	 * time has passed.
	 * 
	 * @param activeSeleniumWebDriver	Selenium web driver to navigate
	 * @param pageURL					Page URL to navigate to
	 */
	private void navigateSeleniumDriverToURL(WebDriver activeSeleniumWebDriver, String pageURL) {
		navigateSeleniumDriverToURL(activeSeleniumWebDriver, pageURL, PAGELOADTIME);
	}
	
	/**
	 * Attempts to send the escape command to the selenium driver in order to stop page loading.
	 * 
	 * The command will be sent every 100ms until successful or until the maximumAttemptTime has passed.
	 * 
	 * Returns TRUE on success and FALSE otherwise.
	 * 
	 * @param activeSeleniumWebDriver	Selenium web driver to interact with
	 * @param maximumAttemptTime		Maximum to try sending the escape command before the function fails
	 * @return							TRUE on successful attempt and FALSE otherwise
	 */
	private boolean sendSeleniumDriverEscapeCommand(WebDriver activeSeleniumWebDriver, int maximumEscapeAttemptTime) {
		
    	//Mark the time we start sending the command
    	long attemptStartTime = System.nanoTime();
    	
		//While the escape command was not successful and we are within the maximumAttemptTime, send the command
    	boolean escapeCommandSuccessful = false;
    	while ((!escapeCommandSuccessful) &&
    		   (maximumEscapeAttemptTime > ((int) ((System.nanoTime() - attemptStartTime)/1000000)))) { //maximumEscapeAttemptTime > current running time
        	
    		//Send the escape command. If no error, set the successful flag to true
    		try {
				Actions builder = new Actions(activeSeleniumWebDriver);
	        	builder.sendKeys(Keys.ESCAPE).perform();
	        	escapeCommandSuccessful = true;
        	}

    		//Either way, let's sleep for the escape pause time
    		finally {
	        	try {
					Thread.sleep(ESCAPEPAUSETIME);
				} 
	        	catch (InterruptedException e) {
					//If the sleep was interrupted, just keep moving
				} 
    		}
    	}
    	
    	//Return whether the attempt was successful or not
    	return escapeCommandSuccessful;
	}

	/**
	 * Returns the AdInjecter javascript as a String with the passed tags inserted into it
	 * 
	 * @param tags			Tags and their placement rankings
	 * @return				AdInjecter javascript with passed tags inserted into it
	 */
	private String getInjecterJS(Map<String,Integer> tags) throws IOException {
		
		//Get the AdInjecter javascript file
		String adInjecterJS = new String(Files.readAllBytes(Paths.get(ADINJECTERJSPATH)));
		
		//Create the tags object by looping through the tags and adding them to the tags string
		String tagsString = "tags = [";
        for(Map.Entry<String, Integer> currentTag: tags.entrySet()) {
        	
        	//Get the tag's dimensions
        	Map<String, Integer> currentDimensions = _tagDimensions.get(currentTag.getKey());

        	//build the current tag object and add it to overall object
        	tagsString +=  "{tag: '" + currentTag.getKey() + "', " +
							"placement: " + currentTag.getValue() + ", " +
							"width: " + currentDimensions.get("width") + ", " +
							"height: " + currentDimensions.get("height") + "},";
        }
        tagsString += "];";
        
        //Insert the tags into the code by replacing the 'insert tags object' marker with them
        String finalJS = adInjecterJS.replace("//INSERT TAGS OBJECT//", tagsString);
        
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
    	try {
			Thread.sleep(JAVASCRIPTWAITTIME);
		} 
    	catch (InterruptedException e) {
			//If the sleep was interrupted, just keep moving
		} 
		
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
    	File screenShot = null;
    	
    	//Attempt to get screenshot a few times within the reasonable time frame
    	TimeLimiter timeoutLimiter = new SimpleTimeLimiter();
    	int currentAttempt = 0;
    	while ((screenShot == null) && (currentAttempt < SCREENSHOTATTEMPTS)) {
    		try {
    		screenShot = timeoutLimiter.callWithTimeout(new Callable<File>() {
						    public File call() {
						      return ((TakesScreenshot) activeSeleniumWebDriver).getScreenshotAs(OutputType.FILE);
						    }
						  }, SCREENSHOTTIMEOUT, TimeUnit.MILLISECONDS, false); 
    		}
    		catch (Exception e) {
    			System.out.println("Error getting screenshot. -" + e.toString() );
    			//Ignore any error and try another attempt (if any are left)
    		}
    		++currentAttempt;
    	}
    	
    	return screenShot;
	}
	
	/**
	 * Crops the passed image according to the class VIEWPORT constants and passed maximum bottom.
	 * It then converts the file to JPG.
	 * 
	 * The cropped image is returned on success and a runtime error is thrown on failure.
	 * 
	 * @param originalImageFile		File of image to modify
	 * @param maximumBottom			Lowest cutoff allowed for image
	 * @return						Modified image
	 * @throws IOException 
	 */
	private BufferedImage cropAndConvertImageFile(File originalImageFile, int maximumBottom) throws IOException {
		
		//Get the image from the file and determine the height and width
		BufferedImage originalImage = ImageIO.read(originalImageFile);
		int cropHeight = (originalImage.getHeight() < maximumBottom) ? originalImage.getHeight() : maximumBottom;
		//mk: changed to DEFAULTVIEWWIDTH from static 1009
		int cropWidth = (originalImage.getWidth() < DEFAULTVIEWWIDTH) ? originalImage.getWidth() : DEFAULTVIEWWIDTH;
    	BufferedImage croppedImage = originalImage.getSubimage(0, 0, cropWidth, cropHeight);
    	
    	//Make BufferedImage generic so it can be written as png or jpg
    	BufferedImage cleanedImage = new BufferedImage(croppedImage.getWidth(),
    												   croppedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    	cleanedImage.createGraphics().drawImage(croppedImage, 0, 0, Color.WHITE, null);
    	
    	//Return the modified image
        return croppedImage;
	}
	
	/**
	 * Saves the passed image to the passed filepath as a PNG
	 * 
	 * @param imageToSave		Image to save as PNG
	 * @param filepath			File the image should be saved as
	 * @throws IOException 
	 */
	private void saveImageAsPNG(BufferedImage imageToSave, String filepath) throws IOException {
		
		//Write the image as a PNG
		ImageIO.write(imageToSave, "png", new File(filepath));         	
	}

	/**
	 * Saves the passed image to the passed filepath as a JPG
	 * 
	 * @param imageToSave		Image to save as JPG
	 * @param filepath			File the image should be saved as
	 * @throws IOException 
	 */
	private void saveImageAsJPG(BufferedImage imageToSave, String filepath) throws IOException {
		
		//Create a writer iterator for 
    	Iterator<ImageWriter> writerIterator = ImageIO.getImageWritersByFormatName("jpeg");  
  	  
    	//Just get the first JPEG writer available  
    	ImageWriter jpegWriter = writerIterator.next();  
    	  
    	//Set the compression quality  
    	ImageWriteParam param = jpegWriter.getDefaultWriteParam();  
    	param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);  
    	param.setCompressionQuality(1.0f);  
    	  
    	//Write the image to the file  
    	FileImageOutputStream outputStream = new FileImageOutputStream(new File(filepath));  
    	jpegWriter.setOutput(outputStream);  
    	jpegWriter.write(null, new IIOImage(imageToSave, null, null), param);  
    	jpegWriter.dispose();  
    	outputStream.close();          	
	}

	/**
	 * Returns TRUE if the passed webdriver is connected to a window and FALSE if not
	 * 
	 * @param activeSeleniumWebDriver		Selenium driver to check
	 */
	private boolean webdriverIsConnectedToWindow(final WebDriver activeSeleniumWebDriver) {
		
		//Try to get the current URL. 
		System.out.print("Trying to get the current URL to test state...");
		try {
			
	    	TimeLimiter timeoutLimiter = new SimpleTimeLimiter();
			try {
			timeoutLimiter.callWithTimeout(new Callable<File>() {
						    public File call() {
						    	activeSeleniumWebDriver.getCurrentUrl();
						    	return null;
						    }
						  }, 5000, TimeUnit.MILLISECONDS, false); 
			}
			catch (Exception e) {
				System.out.println("Failed! - " + e);
				return false;
			}

			
			//activeSeleniumWebDriver.getCurrentUrl();
			System.out.println("Worked!");
			return true;
		}
		
		//If we couldn't connect, then return FALSE
		catch (Exception e) {
			System.out.println("Failed! - " + e);
			return false;
		}
	}
	
	private void quitWebdriver(WebDriver activeSeleniumWebDriver) {
		
		System.out.print("Quitting web driver...");
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
			System.out.print(e + " - Attemp to quit webdriver timed out");
		}
		System.out.println("Done quitting web driver.");
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
