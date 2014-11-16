package adshotrunner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
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
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

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
	
	public HashMap<BufferedImage, HashMap<String, String>> getAdShots() throws IOException {
		
		//Create a web driver to connect with and set its dimensions
        WebDriver firefoxDriver = new RemoteWebDriver(
			new URL("http://localhost:4444/wd/hub"), 
			DesiredCapabilities.firefox());
        firefoxDriver.manage().window().setSize(new Dimension(1024,768));
        //firefoxDriver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        firefoxDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        
        //Loop through each URL
        int ssCount = 0;
        for(Map.Entry<String, Map<String, Integer>> currentURL: _urlTags.entrySet()) {
        	
        	long startTime = System.nanoTime();

        	//Get the page
        	System.out.println(currentURL.getKey());

        	//Break out of the loading if the timeout is exceeded
        	try {
        		firefoxDriver.get("http://" + currentURL.getKey());
        		//firefoxDriver.get(currentURL.getKey());
			} catch (TimeoutException e) {
				//This was just to break out from the perpetually loading page			
	        }
        	
        	//Let's wait to 10 seconds for ads to fully load
        	int loadTime = (int) ((System.nanoTime() - startTime)/1000000);
        	if (loadTime < 10000) {
	        	try {
					Thread.sleep(10000 - loadTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}

        	
        	//Send esc to stop any final loading and close possible popups
        	boolean escapeSuccessful = false;
        	while (!escapeSuccessful) {
	        	try {
					Actions builder = new Actions(firefoxDriver);
		        	builder.sendKeys(Keys.ESCAPE).perform();
		        	escapeSuccessful = true;
	        	}
	        	catch(WebDriverException e) {
	        		escapeSuccessful = false;
	        		System.out.println("Escape failed");
	        	}
        	}
			
        	//Execute the javascript
        	System.out.print("Injecting JS...");
        	String adInjecterJS = getInjecterJS(currentURL.getValue());
			String returned = (String) ((JavascriptExecutor) firefoxDriver).executeScript(adInjecterJS);
			System.out.println("Done");
			System.out.println(returned);
			
			System.out.print("2 Second sleep...");
        	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Done");

        	//Take the screenshot 
			System.out.print("Taking screenshot...");
        	//WebDriver augmentedDriver = new Augmenter().augment(firefoxDriver);
        	//File screenShot = ((TakesScreenshot)augmentedDriver).getScreenshotAs(OutputType.FILE);
        	//WebDriver augmentedDriver = new Augmenter().augment(firefoxDriver);
        	File screenShot = ((TakesScreenshot) firefoxDriver).getScreenshotAs(OutputType.FILE);
        	++ssCount;
			System.out.println("Done");
        				
        	//Crop the image
        	BufferedImage screenShotImage = ImageIO.read(screenShot);
			//int cropHeight = (screenShotImage.getHeight() < 1000) ? screenShotImage.getHeight() : 1000;
        	//BufferedImage croppedImage = screenShotImage.getSubimage(0, 0, 1009, cropHeight);
        	
        	//Save the image to a file
            //ImageIO.write(croppedImage, "png", screenShot);
        	//FileUtils.copyFile(screenShot, new File("/home/juicio/Desktop/" + ssCount + ".png"));
        	FileUtils.copyFile(screenShot, new File("ScreenShot" + ssCount + ".png"));
     	
        	
        	long endTime = System.nanoTime();
        	System.out.println("Beginning to end: " + (endTime - startTime)/1000000);
        	
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

	private String getInjecterJS(Map<String,Integer> tags) throws IOException {
		
		//Get the JS from the file
		String adInjecterJS = new String(Files.readAllBytes(Paths.get("javascript/adInjecter.js")));
		
		//Create the tags object by looping through the tags
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
        
        //Insert the tags into the code
        String finalJS = adInjecterJS.replace("//INSERT TAGS OBJECT//", tagsString);
        
        return finalJS;
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
