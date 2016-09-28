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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import adshotrunner.errors.AdShotRunnerException;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The AdShotter class inserts passed tags into webpages and returns screenshots.
 * 
 * Multiple unique tags, optionally ranked, can be supplied for the same URL. 
 *
 */
public class AdShotter2 {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//final private static String SELENIUMHUBADDRESS = "http://localhost:4444/wd/hub";
	final private static String SELENIUMHUBADDRESS = "http://ec2-54-172-131-29.compute-1.amazonaws.com:4444/wd/hub";
	final private static String SELENIUMPROFILE = "SeleniumDPI";
	final private static String ADINJECTERJSPATH = "javascript/adInjecter.js";
	final private static String FIREFOXPROFILEFATH = "/home/ec2-user/seleniumdpi";
	final private static int PAGELOADTIME = 2000;			//in miliseconds
	final private static int ESCAPEATTEMPTTIME = 2000;		//in miliseconds
	final private static int ESCAPEPAUSETIME = 100;			//in miliseconds
	final private static int JAVASCRIPTWAITTIME = 2000;		//in miliseconds
	final private static int SCREENSHOTATTEMPTS = 3;
	final private static int SCREENSHOTTIMEOUT = 20000;		//in miliseconds
	final private static int DEFAULTVIEWWIDTH = 1366;		//in pixels
	final private static int DEFAULTVIEWHEIGHT = 768;		//in pixels
	final private static int DEFAULTCROPHEIGHT = 1200;		//in pixels
	final private static int MAXCROPHEIGHT = 2500;			//in pixels
	final private static List<Integer> DEFAULTVIEWPORT = Collections.unmodifiableList(Arrays.asList(1366, 768));
	final private static List<Integer> MOBILEVIEWPORT = Collections.unmodifiableList(Arrays.asList(360, 640));
	final private static String MOBILEUSERAGENT = "Mozilla/5.0 (Mobile; rv:26.0) Gecko/26.0 Firefox/26.0";
	final private static boolean VERBOSE = true;
	final private static int MAXOPENTABS = 3;


	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Variables -------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Variables *********************************
	public static AdShotter2 create() {
		return new AdShotter2(DEFAULTVIEWPORT.get(0), DEFAULTVIEWPORT.get(1), null);
	}

	public static AdShotter2 create(int viewWidth, int viewHeight) {
		return new AdShotter2(viewWidth, viewHeight, null);
	}

	public static AdShotter2 createForMobile() {
		return new AdShotter2(MOBILEVIEWPORT.get(0), MOBILEVIEWPORT.get(1), MOBILEUSERAGENT);
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
	 * User Agent string the browser should provide. If empty or NULL, the browser's default user agent is used.
	 */
	private String _userAgent;


	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Instantiates object with the default viewport width and height
	 */
	private AdShotter2() {
		
		this(DEFAULTVIEWWIDTH, DEFAULTVIEWHEIGHT);		
	}
	
	/**
	 * Instantiates object with the passed viewport width and height set
	 * 
	 * @param viewWidth		Width of browser viewport
	 * @param viewHeight	Height of browser viewport
	 */
	private AdShotter2(int viewWidth, int viewHeight) {
		
		_browserViewWidth = viewWidth;
		_browserViewHeight = viewHeight;
	}

	/**
	 * Instantiates object with the passed viewport and user agent string
	 * 
	 * If the userAgent string is empty or null, the browser's default will be used.
	 * 
	 * @param viewWidth		Width of browser viewport
	 * @param viewHeight	Height of browser viewport
	 */
	private AdShotter2(int viewWidth, int viewHeight, String userAgent) {
		
		_browserViewWidth = viewWidth;
		_browserViewHeight = viewHeight;
		_userAgent = userAgent;
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
		WebDriver firefoxDriver = null;
		try {firefoxDriver = getSeleniumDriver();}
		catch (Exception e) {throw new AdShotRunnerException("Could not connect with Selenium server", e);}
        
		//Open each AdShot URL in a new tab up to the max tab amount
		long tabStartTime = System.nanoTime();
		
		int openTabIndex = 0;
		while ((openTabIndex < MAXOPENTABS) && (openTabIndex < adShots.size())) {
			int pageLoadTime = (treatAsTags) ? 500 : PAGELOADTIME;
			try {navigateSeleniumDriverToURL(firefoxDriver, adShots.get(openTabIndex).url(), pageLoadTime);} 
    		catch (Exception e) {
    			consoleLog("Couldn't navigate to page: " + adShots.get(openTabIndex).url());
    			adShots.get(openTabIndex).setError(new AdShotRunnerException("Could not navigate to URL", e));
    		}
			
			//If there are still adShots to be opened and we are less than max tabs, create a new tab
			++openTabIndex;
			if ((openTabIndex < adShots.size()) && (openTabIndex < MAXOPENTABS)) {
				openNewTab(firefoxDriver);
			}
			
			//Otherwise, loop back to the front
			else if (adShots.size() > 1) {navigateToNextTab(firefoxDriver);}			
		}
				
		//Pause if still in the load time frame
		int maxLoadTime = (treatAsTags) ? 7000 : 7000;
    	int currentPageLoadTime = (int) ((System.nanoTime() - tabStartTime)/1000000);	//Divide by a million to move nanoseconds to miliseconds
    	if (currentPageLoadTime < maxLoadTime) {
			pause(maxLoadTime - currentPageLoadTime);
    	}
		
		//Loop through each AdShot and take the screenshot
		for (int adShotIndex = 0; adShotIndex < adShots.size(); ++ adShotIndex) {
			long startTime = System.nanoTime();
			AdShot currentAdShot = adShots.get(adShotIndex);
			takeAdShot(firefoxDriver, currentAdShot, treatAsTags);
			
			//If no tag images were injected into the page AND alternate URLs exist, try those
			if ((currentAdShot.injectedTagImages().size() == 0) && (currentAdShot.alternateURLs().size() > 0)) {
				Iterator<String> urlIterator = currentAdShot.alternateURLs().iterator();
				while ((currentAdShot.injectedTagImages().size() == 0) && 
					   (urlIterator.hasNext())) {
					
					String alternateURL = urlIterator.next();
					
					try {navigateSeleniumDriverToURL(firefoxDriver, alternateURL, PAGELOADTIME);} 
		    		catch (Exception e) {
		    			consoleLog("Couldn't navigate to page: " + adShots.get(openTabIndex).url());
		    			adShots.get(openTabIndex).setError(new AdShotRunnerException("Could not navigate to URL", e));
		    		}
					
					takeAdShot(firefoxDriver, currentAdShot, treatAsTags);
				}
			}
			
			//If there is an AdShot that needs to be loaded still,
			//put it in the current tab and navigate to the next
			if ((adShotIndex + MAXOPENTABS) < adShots.size()) {
				int pageLoadTime = (treatAsTags) ? 500 : PAGELOADTIME;
				try {navigateSeleniumDriverToURL(firefoxDriver, adShots.get(adShotIndex + MAXOPENTABS).url(), pageLoadTime);} 
	    		catch (Exception e) {
	    			consoleLog("Couldn't navigate to page: " + adShots.get(adShotIndex).url());
	    			adShots.get(openTabIndex).setError(new AdShotRunnerException("Could not navigate to URL", e));
	    		}	
			}
			if (adShots.size() > 1) {navigateToNextTab(firefoxDriver);}			
						
        	long endTime = System.nanoTime();
			consoleLog("AdShot time: " + (endTime - startTime)/1000000 + " ms");
		}
    	
    	//Quit the driver
        quitWebdriver(firefoxDriver);
        
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
			
	    	//Send esc to stop any final loading and close possible popups 
			try {sendSeleniumDriverEscapeCommand(activeSeleniumWebDriver, ESCAPEATTEMPTTIME);}
			catch (Exception e) {
				
				//If the key couldn't be pressed, keep on going
				consoleLog("Could not perform escape key press");
			}
		
	    	//First, get the AdInjecter javascript with the current tags inserted
			String adInjecterJS = "";
			try {adInjecterJS = getInjecterJS(adShot.tagImages());}
			catch (Exception e) {
				consoleLog("Could not create AdInjecter javascript");
				adShot.setError(new AdShotRunnerException("Could not create AdInjecter javascript", e)); return;
			}
	    	
	    	//Execute the javascript
			String injecterResponse = "";
			try {injecterResponse = executeSeleniumDriverJavascript(activeSeleniumWebDriver, adInjecterJS);}
			catch (Exception e) {
				consoleLog("Could not execute AdInjecter in page");
				consoleLog(e.getMessage());
				adShot.setError(new AdShotRunnerException("Could not execute AdInjecter in page", e)); //return;
			}
			
			//Mark which ads were injected and store the lowest tags bottom position
			consoleLog("Javascript Response: " + injecterResponse);
			Type injecterJSONType = new TypeToken<HashMap<String, ArrayList<String>>>(){}.getType();
			Map<String, List<String>> injectedTagInfo = new Gson().fromJson(injecterResponse, injecterJSONType);
			adShot.markTagImageAsInjected(injectedTagInfo.get("injectedTagIDs"));
			lowestTagBottom = Integer.parseInt(injectedTagInfo.get("lowestTagPosition").get(0));
			System.out.println("Injected Tags Size: " + adShot.injectedTagImages().size());
			System.out.println("Bottom position: " + lowestTagBottom);
		}
		
    	//Take the screenshot 
		consoleLog("Taking screenshot...");
		long screenShotStartTime = System.nanoTime();
		
    	File screenShot = null;
    	try {screenShot = captureSeleniumDriverScreenshot(activeSeleniumWebDriver);}
		catch (Exception e) {
			consoleLog("Could not take screenshot");
			adShot.setError(new AdShotRunnerException("Could not take screenshot", e)); return;
		}
		
    	long screenShotEndTime = System.nanoTime();
		consoleLog("Done! - " + (screenShotEndTime - screenShotStartTime)/1000000 + " ms");
		
		//Store the final URL
		adShot.setFinalURL(activeSeleniumWebDriver.getCurrentUrl());
		
		//Crop the image
		try {adShot.setImage(cropAndConvertImageFile(screenShot, lowestTagBottom, treatAsTag));}
		catch (Exception e) {
			consoleLog("Could not crop screenshot");
			adShot.setError(new AdShotRunnerException("Could not crop screenshot", e)); return;
		}
    			
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
		ProxyDetails chosenProxy = getProxyServer();
		String proxyIPAddress = chosenProxy.ip;
		int proxyPort = chosenProxy.port;
        // Direct = 0, Manual = 1, PAC = 2, AUTODETECT = 4, SYSTEM = 5
        ffProfile.setPreference("network.proxy.type", 1);
        ffProfile.setPreference("network.proxy.http", proxyIPAddress);
        ffProfile.setPreference("network.proxy.http_port", proxyPort);
        ffProfile.setPreference("network.proxy.socks_remote_dns", false);
        
        //profile data
        ffProfile.setPreference("app.update.auto", false);
        ffProfile.setPreference("app.update.enabled", false);
        //ffProfile.setPreference("browser.privatebrowsing.autostart", true);
        ffProfile.setPreference("browser.shell.checkDefaultBrowser", false);
        ffProfile.setPreference("browser.tabs.warnOnClose", false);
        ffProfile.setPreference("privacy.trackingprotection.pbmode.enabled", false);
        ffProfile.setPreference("extensions.blocklist.enabled", false);
        
        //these are to make the screenshots look pretty
        //ffProfile.setPreference("gfx.direct2d.disabled", true);
        //ffProfile.setPreference("layers.acceleration.disabled", true);
        //ffProfile.setPreference("gfx.font_rendering.cleartype_params.cleartype_level", 2);
        //ffProfile.setPreference("gfx.font_rendering.cleartype_params.enhanced_contrast", 2);
        //ffProfile.setPreference("gfx.font_rendering.cleartype_params.gamma", 2);
        //ffProfile.setPreference("gfx.font_rendering.cleartype_params.pixel_structure", 2);
        //ffProfile.setPreference("gfx.font_rendering.cleartype_params.rendering_mode", 2);
        
        //deal with firefox forcing bad rendering when it doesn't like the driver
        ffProfile.setPreference("webgl.force-enabled", true);
        ffProfile.setPreference("webgl.msaa-force", true);
        ffProfile.setPreference("layers.acceleration.force-enabled", true);
        ffProfile.setPreference("gfx.direct2d.force-enabled", true);
        
        
        ffProfile.setPreference("network.http.response.timeout", 7); //this is time in seconds
        
        ffProfile.setPreference("xpinstall.signatures.required", false); //this is time in seconds
        
        //If a user agent was defined, set it in the browser
        if ((_userAgent != null) && (!_userAgent.isEmpty())) {
	        ffProfile.setPreference("general.useragent.override", _userAgent);
        }

        //install extension
        //String AdMarkerPath = "/home/ec2-user/firefoxExtensions/adMarker.xpi";
        String AdMarkerPath = "firefoxExtensions/adMarker.xpi";   
        try {
			ffProfile.addExtension(new File(AdMarkerPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //String DoNotDisturbPath = "/home/ec2-user/firefoxExtensions/donotdisturb-1.4.2.xpi";
        String DoNotDisturbPath = "firefoxExtensions/donotdisturb-1.4.2.xpi";
        try {
			ffProfile.addExtension(new File(DoNotDisturbPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        String scriptStopperPath = "firefoxExtensions/@asrscriptstopperextension-0.0.1.xpi";
        try {
			ffProfile.addExtension(new File(scriptStopperPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //set new firefox profile to be used in selenium
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        //capabilities.setCapability("marionette", true);
        capabilities.setCapability(FirefoxDriver.PROFILE, ffProfile);
		
		WebDriver firefoxDriver = null;
        firefoxDriver = new RemoteWebDriver(
		    			new URL(SELENIUMHUBADDRESS), 
		    			capabilities);
 
        //String marionetteDriverLocation = "c:\\selenium\\geckodriver.exe";
        //System.setProperty("webdriver.gecko.driver", marionetteDriverLocation);
        //WebDriver driver = new MarionetteDriver();
        

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
	 * @param maxLoadTime				Maximum load time to allow. Set to PAGELOADTIME if less than it. (in miliseconds)
	 */
	private void navigateSeleniumDriverToURL(WebDriver activeSeleniumWebDriver, String pageURL, int maxLoadTime) {
		
		//If the max load time is shorter than the class constant PAGELOADTIME, set it to PAGELOADTIME
		//maxLoadTime = (maxLoadTime > PAGELOADTIME) ? maxLoadTime : PAGELOADTIME;  
		
    	//Mark the time we start loading the page
    	long pageLoadStartTime = System.nanoTime();
    	
    	//Attempt to load the passed page
    	try {activeSeleniumWebDriver.get(pageURL);} 
    	
    	//If the timeout is reached, just keep moving
    	catch (TimeoutException e) {
    		//Just keep moving if the timeout is reached
    	}
    	
    	//If the page seem to load before the load time, give it the difference as extra time through sleep
    	finally {
    		
        	//If the current load time is less that maximum load time, sleep the difference
        	int currentPageLoadTime = (int) ((System.nanoTime() - pageLoadStartTime)/1000000);	//Divide by a million to move nanoseconds to miliseconds
        	if (currentPageLoadTime < maxLoadTime) {
				pause(maxLoadTime - currentPageLoadTime);
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
	 * Opens up a new tab in the driver browser and focuses on it.
	 * 
	 * Designed to work with Firefox on Windows or Linux
	 * 
	 * @param activeSeleniumDriver	Selenium driver to interact with
	 */
	private void openNewTab(WebDriver activeSeleniumDriver) {
		int attempts = 0;
		boolean succeeded = false;
		while (!succeeded && (attempts < 3)) {
			try {
				//activeSeleniumDriver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"t");
				pause(2000);
				new Actions(activeSeleniumDriver).sendKeys(Keys.chord(Keys.CONTROL, "t")).perform();
				activeSeleniumDriver.switchTo().window((String) activeSeleniumDriver.getWindowHandles().toArray()[0]);
				activeSeleniumDriver.switchTo().defaultContent();
				pause(1000);
				succeeded = activeSeleniumDriver.getCurrentUrl().equals("about:newtab");
			}
			catch (Exception e) {
				++attempts; 
				consoleLog("Couldn't create new tab: " + attempts);
				pause(500);
			}
		}
	}
	
	/**
	 * Navigates to the next tab in the driver browser if one exists
	 * 
	 * Designed to work with Firefox on Windows or Linux
	 * 
	 * @param activeSeleniumDriver	Selenium driver to interact with
	 */
	private void navigateToNextTab(WebDriver activeSeleniumDriver) {
		int attempts = 0;
		boolean succeeded = false;
		while (!succeeded && (attempts < 3)) {
			try {
				pause(1500);
				String startURL = activeSeleniumDriver.getCurrentUrl();
				new Actions(activeSeleniumDriver).sendKeys(Keys.chord(Keys.CONTROL, Keys.TAB)).perform();
				activeSeleniumDriver.switchTo().window((String) activeSeleniumDriver.getWindowHandles().toArray()[0]);
				activeSeleniumDriver.switchTo().defaultContent();
				pause(1000);
				succeeded = (!activeSeleniumDriver.getCurrentUrl().equals(startURL));
			}
			catch (Exception e) {
				++attempts; 
				consoleLog("Couldn't move to next tab: " + attempts);
				pause(500);
			}
		}
	}
	
	/**
	 * Closes the current tab
	 * 
	 * Designed to work with Firefox on Windows or Linux
	 * 
	 * @param activeSeleniumDriver	Selenium driver to interact with
	 */
	private void closeTab(WebDriver activeSeleniumDriver) {
		new Actions(activeSeleniumDriver).sendKeys(Keys.chord(Keys.CONTROL, "w")).perform();
		pause(3000);
		//activeSeleniumDriver.switchTo().window((String) activeSeleniumDriver.getWindowHandles().toArray()[0]);
		//activeSeleniumDriver.switchTo().defaultContent();
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
				consoleLog("Pausing for 1 seconds");
				pause(200);
				new Actions(activeSeleniumWebDriver).sendKeys(Keys.chord(Keys.CONTROL, Keys.SHIFT, "e")).perform();
				consoleLog("Send stop scripts key");
				consoleLog("Pausing for 1.5 seconds");
				pause(1500);
				consoleLog("Send start scripts key");	        	
				new Actions(activeSeleniumWebDriver).sendKeys(Keys.chord(Keys.CONTROL, Keys.ALT, Keys.SHIFT, "e")).perform();
				consoleLog("Pausing for 1.5 seconds");
				pause(1500);
				
				escapeCommandSuccessful = true;
        	}

    		//Either way, let's sleep for the escape pause time
    		finally {
				pause(ESCAPEPAUSETIME);
    		}
    	}
    	consoleLog("Done with stop-start script");
    	
    	//Return whether the attempt was successful or not
    	return escapeCommandSuccessful;
	}
	
	private class ProxyDetails{
		public String ip;
		public int port;
		public ProxyDetails(String ipAddress, int portNumber){
			ip = ipAddress;
			port = portNumber;
		}
	}
	
	private ProxyDetails getProxyServer(){
		
		ArrayList<ProxyDetails> proxyList = new ArrayList<ProxyDetails>();
		proxyList.add(new ProxyDetails("192.210.148.231", 3128));
		proxyList.add(new ProxyDetails("198.23.217.23", 3128));
		proxyList.add(new ProxyDetails("104.144.165.103", 3128));
		proxyList.add(new ProxyDetails("107.173.182.217", 3128));
		proxyList.add(new ProxyDetails("104.168.23.154", 3128));
		
		Random randomGenerator = new Random();
		int anyFromProxyList = randomGenerator.nextInt(proxyList.size());
		
		return proxyList.get(anyFromProxyList);
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
		
		consoleLog("Send stop scripts key - Just before screenshot");	        	
		consoleLog("Pausing for .5 seconds");
		pause(500);
		
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
    			consoleLog("Error getting screenshot. -" + e.toString() );
    			//Ignore any error and try another attempt (if any are left)
    		}
    		++currentAttempt;
    	}
		new Actions(activeSeleniumWebDriver).sendKeys(Keys.chord(Keys.CONTROL, Keys.ALT, Keys.SHIFT, "e")).perform();
   	
    	return screenShot;
	}
	
	/**
	 * Crops the passed image according to the class VIEWPORT constants and passed maximum bottom.
	 * 
	 * The cropped image is returned on success and a runtime error is thrown on failure.
	 * 
	 * @param originalImageFile		File of image to crop
	 * @param requestedCropHeight	Requested crop height. If set too low or to zero, class defaults will be used.
	 * @param treatAsTag			If flagged as a tag, the crop width will be set to the image width (not the default view width)
	 * @return						Cropped image
	 * @throws IOException 
	 */
	private BufferedImage cropAndConvertImageFile(File originalImageFile, int requestedCropHeight, boolean treatAsTag) throws IOException {
				
		//Get the image from the file and determine the height and width
		BufferedImage originalImage = ImageIO.read(originalImageFile);
		int cropHeight = ((requestedCropHeight < 1) || (requestedCropHeight < DEFAULTCROPHEIGHT)) ? 
				DEFAULTCROPHEIGHT : requestedCropHeight;
		cropHeight = (cropHeight < MAXCROPHEIGHT) ? cropHeight : MAXCROPHEIGHT;
		cropHeight = (originalImage.getHeight() < cropHeight) ? originalImage.getHeight() : cropHeight;
		int defaultWidth = (treatAsTag) ? originalImage.getWidth() : DEFAULTVIEWWIDTH;
		int cropWidth = (originalImage.getWidth() < defaultWidth) ? originalImage.getWidth() : defaultWidth;
		BufferedImage croppedImage = originalImage.getSubimage(0, 0, cropWidth, cropHeight);
    	
    	//Make BufferedImage generic so it can be written as png or jpg
    	BufferedImage cleanedImage = new BufferedImage(croppedImage.getWidth(),
    												   croppedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    	cleanedImage.createGraphics().drawImage(croppedImage, 0, 0, Color.WHITE, null);
    	
    	//Return the modified image
        return croppedImage;
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
				consoleLog("Failed! - " + e);
				return false;
			}

			
			//activeSeleniumWebDriver.getCurrentUrl();
			consoleLog("Worked!");
			return true;
		}
		
		//If we couldn't connect, then return FALSE
		catch (Exception e) {
			consoleLog("Failed! - " + e);
			return false;
		}
	}
	
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
			System.out.print(e + " - Attemp to quit webdriver timed out");
		}
		consoleLog("Done quitting web driver.");
	}

	private void pause(int pauseTime) {
    	try {
			Thread.sleep(pauseTime);
		} 
    	catch (InterruptedException e) {
			//If the sleep was interrupted, just keep moving
		} 
	}
	
	private void consoleLog(String message) {
		if (VERBOSE) {
			String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
			System.out.println(timeStamp + ": " + message);
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
