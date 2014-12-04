package adshotrunner;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

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
	final private static String SELENIUMHUBADDRESS = "http://localhost:4444/wd/hub";
	final private static String ADINJECTERJSPATH = "javascript/adInjecter.js";
	final private static int PAGELOADTIME = 10000;			//in miliseconds
	final private static int ESCAPEATTEMPTTIME = 2000;		//in miliseconds
	final private static int ESCAPEPAUSETIME = 100;			//in miliseconds
	final private static int JAVASCRIPTWAITTIME = 2000;		//in miliseconds
	final private static int SCREENSHOTATTEMPTS = 3;
	final private static int SCREENSHOTTIMEOUT = 12000;		//in miliseconds
	final private static int VIEWWIDTH = 1024;				//in pixels
	final private static int VIEWHEIGHT = 768;				//in pixels


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


	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Instantiates basic private variables. Nothing more.
	 */
	public AdShotter() {
		
		_tagDimensions = new HashMap<String, Map<String, Integer>>();
		_urlTags = new HashMap<String, Map<String, Integer>>();
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************
	/**
	 * Adds a tag and its URL to the instance. A URL with its associated tags will be
	 * processed through the adshotter.
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
	 * processed through the adshotter.
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
	
	public HashMap<BufferedImage, HashMap<String, String>> getAdShots() throws Exception {
		        
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
        int ssCount = 0;
        for(Map.Entry<String, Map<String, Integer>> currentURL: _urlTags.entrySet()) {
        	
        	//First, get the AdInjecter javascript with the current tags inserted
        	String adInjecterJS = getInjecterJS(currentURL.getValue());
        	
        	//Open the current URL within the PAGELOADTIMEOUT time
        	try {navigateSeleniumDriverToURL(firefoxDriver, currentURL.getKey());} 
    		//On failure, throw runtime error
    		catch (Exception e) {
    			throw new AdShotRunnerException("Could not navigate Selenium driver to page: " + currentURL.getKey(), e);
    		}
        	        	
        	//Send esc to stop any final loading and close possible popups
        	boolean escapeSuccessful = sendSeleniumDriverEscapeCommand(firefoxDriver, ESCAPEATTEMPTTIME);
			
        	//Execute the javascript
			String javascriptResponse = executeSeleniumDriverJavascript(firefoxDriver, adInjecterJS);
			
        	//Take the screenshot 
        	//File screenShot = captureSeleniumDriverScreenshot(firefoxDriver);
			
			System.out.println(currentURL.getKey() + ":");
			System.out.print("Taking screenshot...");
			long startTime = System.nanoTime();
        	File screenShot = ((TakesScreenshot) firefoxDriver).getScreenshotAs(OutputType.FILE);
        	long endTime = System.nanoTime();
			System.out.print("Done! - ");
			System.out.println((endTime - startTime)/1000000 + " ms");

			//Crop the image
        	BufferedImage screenShotImage = cropAndConvertImageFile(screenShot, 1200);
        	
        	//Save the image to a file
            //ImageIO.write(screenShotImage, "png", screenShot);
        	++ssCount;
        	BufferedImage newBufferedImage = new BufferedImage(screenShotImage.getWidth(),
        			screenShotImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        	newBufferedImage.createGraphics().drawImage(screenShotImage, 0, 0, Color.WHITE, null);
            //ImageIO.write(newBufferedImage, "jpg", new File("ScreenShot" + ssCount + ".jpg"));
            ImageIO.write(newBufferedImage, "png", new File("ScreenShot" + ssCount + ".png"));
            
        	//FileUtils.copyFile(screenShot, new File("ScreenShot" + ssCount + ".png"));
        	//FileUtils.copyFile(screenShot, new File("ScreenShot" + ssCount + ".jpg"));
        	
        	Iterator<ImageWriter> i = ImageIO.getImageWritersByFormatName("jpeg");  
        	  
        	// Just get the first JPEG writer available  
        	ImageWriter jpegWriter = i.next();  
        	  
        	// Set the compression quality to 0.8  
        	ImageWriteParam param = jpegWriter.getDefaultWriteParam();  
        	param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);  
        	param.setCompressionQuality(1.0f);  
        	  
        	// Write the image to a file  
        	FileImageOutputStream out = new FileImageOutputStream(new File("ScreenShot" + ssCount + ".jpg"));  
        	jpegWriter.setOutput(out);  
        	//jpegWriter.write(null, new IIOImage(newBufferedImage, null, null), param);  
        	jpegWriter.dispose();  
        	out.close();          	
        }
		
    	
    	//Close the web driver
    	//firefoxDriver.close();
        
		return null;
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
		WebDriver firefoxDriver = null;
        firefoxDriver = new RemoteWebDriver(
		    			new URL(SELENIUMHUBADDRESS), 
		    			DesiredCapabilities.firefox());
        
        //Set the viewport size and the time to load before sending an error
        firefoxDriver.manage().window().setSize(new Dimension(VIEWWIDTH,VIEWHEIGHT));
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
    	
    	//If the page seem to load before the load time, give it the difference as extra time through sleep
    	catch (Exception e) {
    		//Just keep moving if the timeout is reached
    	}	
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
    		catch (Exception e) {}
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
		int cropWidth = (originalImage.getWidth() < 1009) ? originalImage.getWidth() : 1009;
    	BufferedImage croppedImage = originalImage.getSubimage(0, 0, cropWidth, cropHeight);
    	
    	//Return the modified image
        return croppedImage;
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
