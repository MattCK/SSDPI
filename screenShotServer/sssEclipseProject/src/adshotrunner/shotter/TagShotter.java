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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import adshotrunner.campaigns.AdShot;
import adshotrunner.campaigns.Creative;
import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.system.ASRProperties;

/**
 * The TagShotter captures images of Creative tag pages and adds them to the Creatives.
 */
public class TagShotter extends SeleniumBase {
	
	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------		
	//Paths for plug-in to disable javascript visibility API
	final private static String DISABLEVISABILITYPATH = "chromeExtensions/disableVisibility.crx";
	
	//Path for list of possible proxies for Creative driver
	final private static String PROXIESJSONPATH = ASRProperties.pathForProxiesJSON();
	
	//Timeout for page load
	final private static int CREATIVETIMEOUT = 14000;			//in milliseconds
	
	//The time each Creative needs to run for animations to finish
	final private static int CREATIVERUNTIME = 17000;			//in milliseconds
	
	//The HTML Div element containing the tag to image
	final private static String TAGDIVCONTAINERID = "adTagContainer";

	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	/**
	 * Captures a screenshot of each Creative tag, crops it, and places the image in the passed
	 * Creative.
	 * 
	 * @param creatives		Set of Creatives to capture and crop screenshot images for
	 */
	static public void captureTagImages(Set<Creative> creatives) {
		
		//If no creatives were passed, do nothing and return;
		if (creatives.size() == 0) {return;}
		
		//Try to create a web driver to connect with
		WebDriver activeWebDriver = null;
		try {activeWebDriver = getCreativeDriver();}
		catch (Exception e) {throw new AdShotRunnerException("Could not connect with Selenium server", e);}
		
		//Loop through the Creatives and process them
		for (Creative currentCreative : creatives) {
			
			//Mark the Creative as being processed.
			currentCreative.setStatus(Creative.PROCESSING);
			
			//----------------NOTE! The CREATIVETIMEOUT is temporary until a new driver is compiled for Chrome 59			
			long pageLoadStartTime = System.nanoTime();
			boolean navigationSucceeded = navigateSeleniumDriverToURL(activeWebDriver, currentCreative.tagPageURL(), CREATIVETIMEOUT);
			
			//IF the navigation failed, set the AdShot error
			if (!navigationSucceeded) {
				currentCreative.setError(Creative.URLNAVIGATION);
			}
			
			//Otherwise, get the screenshot
			else {
				
				//Pause if the Creative still needs time to run to finish animations
				long currentRunTime = (System.nanoTime() - pageLoadStartTime)/1000000;
				if (currentRunTime < CREATIVERUNTIME) {
					pause((int) (CREATIVERUNTIME - currentRunTime));
				}
				
				//Create the image
				captureImage(activeWebDriver, currentCreative);
			}
			
			//If there was no error, mark the Creative as finished
			if (!currentCreative.status().equals(Creative.ERROR)) {
				currentCreative.setStatus(Creative.FINISHED);
			}			
		}

		//Quit the driver
		quitWebdriver(activeWebDriver);
	}
	
	//********************************* Private Methods *************************************
	/**
	 * Takes a screenshot of the webdriver, crops it to the tag's dimensions, and
	 * places it in the Creative (thus uploading it to storage and saving its info
	 * in the database)
	 * 
	 * @param activeWebDriver		Webdriver pointing to the Creative's tag page
	 * @param activeCreative				Creative to capture and set image for
	 */
	static private void captureImage(WebDriver activeWebDriver, Creative activeCreative) {
		
		//Take the screenshot 
		File screenShot = null;
		try {screenShot = captureSeleniumDriverScreenshot(activeWebDriver);}
		catch (Exception e) {
			activeCreative.setError(Creative.SCREENSHOTCAPTURE); return;
		}
		
		//Crop the image
		BufferedImage croppedScreenshot = null;
		try {croppedScreenshot = cropCreativeScreenshot(activeWebDriver, screenShot);}
		catch (Exception e) {
			e.printStackTrace();
			activeCreative.setError(Creative.SCREENSHOTCROP); return;
		}
		
		//Add the final image to the Creative
		try {activeCreative.setImage(croppedScreenshot);}
		catch (Exception e) {
			activeCreative.setError(Creative.IMAGEUPLOAD); return;
		}
	}

	/**
	 * Returns an initialized Selenium Webdriver for headless Chrome on a
	 * tag imaging linux node. 
	 *
	 * If the AWS proxy is functional, the driver will be on the AWS TagImager
	 * node with the proxy setup. If the proxy is not functional, a driver
	 * on an external TagImager node will be returned.
	 * 
	 * @return				Initialized Chrome WebDriver
	 */
	static private WebDriver getCreativeDriver() throws MalformedURLException {

		//Define the path to the ChromeDriver. 
		//The Tag Imager uses the stock ChromeDriver and not our modified one
		System.setProperty("webdriver.chrome.driver", "chromedriver");
		
		//Create the capability, option, and preference objects for the driver
		consoleLog("Creating Creative driver...");
		DesiredCapabilities driverCapabilities = DesiredCapabilities.chrome();
		ChromeOptions driverOptions = new ChromeOptions();	

		//Define the options to run the latest Chrome headless
		driverOptions.setBinary("/usr/bin/google-chrome-beta");
		driverOptions.addArguments("headless");
		driverOptions.addArguments("disable-gpu");
		
		//Use the AWS tag imagers for now
		driverCapabilities.setCapability("applicationName", "awsTagImager");

		//Setup the proxy capability
		String proxyDetails = getProxyServer();
		boolean proxyIsFunctional = proxyIsFunctional(proxyDetails);
		consoleLog("Proxy is functional: " + proxyIsFunctional);
		if (proxyIsFunctional) {
			consoleLog("Using proxy: " + proxyDetails);
			Proxy chromeProxy = new Proxy();
			chromeProxy.setProxyType(ProxyType.MANUAL);
			chromeProxy.setSslProxy(proxyDetails);
			chromeProxy.setHttpProxy(proxyDetails);
			driverOptions.addArguments("proxy-server=" + proxyDetails);
			driverCapabilities.setCapability("applicationName", "awsTagImager");
		}
		
		//If the proxy is not working, use the external nodes
		else {
			consoleLog("WARNING!!! PROXY DOWN! USING EXTERNAL NODES!");
			driverCapabilities.setCapability("applicationName", "externalTagImager");
		}
		
		//Install extension to disable visibility so animations run when tab is hidden
		driverOptions.addExtensions(new File(DISABLEVISABILITYPATH));
		
		//Initialize the actual driver
		WebDriver chromeDriver = null;
		driverCapabilities.setCapability(ChromeOptions.CAPABILITY, driverOptions);
		chromeDriver = new RemoteWebDriver(
							new URL(SELENIUMHUBURL), 
							driverCapabilities);
		
		//Set the page timeout
		setCommandTimeout(chromeDriver, DEFAULTTIMEOUT);
				
		//Return the initialized remote chrome web driver
		consoleLog("Done creating Creative driver.");
		return chromeDriver;
	} 

	/**
	 * Returns the proxy server the driver should be set to in the format:
	 * 
	 * 		hostname:portnumber
	 * 
	 * Should be either a random proxy from the service or a proxy that
	 * automatically chooses a random route
	 * 
	 * @return	Proxy details in the format hostname::portnumber
	 */
	static private String getProxyServer() {
		
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
	 * Verifies whether or not the passed proxy server is functioning correctly. Returns
	 * TRUE if functional and FALSE otherwise.
	 * 
	 * This function attempts to download a 1x1 image off of AWS. If the resulting 
	 * status code is 200, the proxy is considered functional and TRUE is returned.
	 * Otherwise, FALSE is returned.
	 * 
	 * @param proxyServerWIthPort		Proxy server to test
	 * @return
	 */
	static private boolean proxyIsFunctional(String proxyServerWIthPort) {
		
		//Separate the proxy into IP and port
		String[] proxyParts = proxyServerWIthPort.split(":");
		String proxyIP = proxyParts[0];
		String proxyPort = proxyParts[1];
		
		//Try to get the code
		int responseCode = 0;
		try {
			WebClient webClient = new WebClient();
			ProxyConfig proxyConfig = new ProxyConfig(proxyIP, Integer.parseInt(proxyPort));
			webClient.getOptions().setProxyConfig(proxyConfig);			
			webClient.getOptions().setTimeout(2000);			
			responseCode = webClient.getPage(
		            "https://s3.amazonaws.com/asr-images/fillers/nsfiller-1x1.jpg"
		    ).getWebResponse().getStatusCode();
		    webClient.closeAllWindows();
		} catch(Exception e) {}
		
		return (responseCode == 200);
	}

	/**
	 * Crops the screenshot file image to the height and width of the tag.
	 * 
	 * The width and height are determined by querying the driver for the dimensions of 
	 * the "adTagContiner" div holding the tag.
	 * 
	 * @param activeWebDriver			Active web driver navigated to tag page
	 * @param originalImageFile			Screenshot of tag page
	 * @return
	 */
	static private BufferedImage cropCreativeScreenshot(final WebDriver activeWebDriver, File originalImageFile) throws IOException {
		
		//Get the tag height and width
		WebElement tagDiv = activeWebDriver.findElement(By.id(TAGDIVCONTAINERID));
		int cropHeight = tagDiv.getSize().getHeight();
		int cropWidth = tagDiv.getSize().getWidth();
						
		//Crop the image
		BufferedImage originalImage = ImageIO.read(originalImageFile);
		BufferedImage croppedImage = originalImage.getSubimage(0, 0, cropWidth, cropHeight);
		
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
	private TagShotter() {}

	
	
}
