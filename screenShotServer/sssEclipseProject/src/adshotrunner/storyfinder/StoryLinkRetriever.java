package adshotrunner.storyfinder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.shotter.SeleniumBase;
import adshotrunner.utilities.ASRDatabase;
import adshotrunner.utilities.URLTool;

/**
 * The StoryLinkRetriever returns StoryLinks from a given URL. 
 * 
 * The class will check the database for exceptions to use for a given domain/subdomain. These
 * exceptions tell the instance to only get StoryLinks from inside a container element with a specific
 * ID or with a specific CSS class name.
 */
public class StoryLinkRetriever extends SeleniumBase {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//Paths for base get possible StoryLinks javascript file
	final private static String POSSIBLESTORYLINKSJSPATH = "javascript/getPossibleStoryLinks.js";

	//Timeout for page load
	final private static int STORYTIMEOUT = 7000;			//in milliseconds
	
	//Minimum amount of stories required for a retrieval with exceptions
	//If less than this amount is returned, a retrieval without exceptions is executed
	final private static int MINIMUMWITHEXCEPTION = 4;
	
	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	/**
	 * Returns a list of StoryLinks for the passed URL.
	 * 
	 * If exceptions exist in the database for the URL's subdomain/domain, they will be used.
	 * These exceptions tell the instance to only get StoryLinks from inside a container element 
	 * with a specific ID or a specific CSS class name.
	 * 
	 * @param url				URL of page to get StoryLinks from
	 * @param viewportWidth		Width of driver viewport to use
	 * @param viewportHeight	Height of driver viewport to use
	 * @return			List of StoryLinks from the passed URL
	 */
	static public List<StoryLink> getStoryLinks(String url, int viewportWidth, int viewportHeight) {
		
		//Try to create a web driver to connect with
		WebDriver activeWebDriver = null;
		try {activeWebDriver = getStoryDriver(viewportWidth, viewportHeight);}
		catch (Exception e) {throw new AdShotRunnerException("Could not connect with Selenium server", e);}
		
		//Navigate to the passed URL
		if (!navigateSeleniumDriverToURL(activeWebDriver, url, STORYTIMEOUT)) {
			quitWebdriver(activeWebDriver);
			throw new AdShotRunnerException("StoryLinks: could not navigate to URL");
		}
		
		//Get possible exception container from the database
		Map<String, String> storyExceptionContainer = getStoryLinkExceptionContainer(url);
		String exceptionContainerID = (storyExceptionContainer != null) ? storyExceptionContainer.get("id") : "";
		String exceptionContainerClassName = (storyExceptionContainer != null) ? storyExceptionContainer.get("className") : "";				
		if (storyExceptionContainer != null) {
			consoleLog("Exception Found: " + exceptionContainerID + ", " + exceptionContainerClassName);
		}
		
		//Create the javascript to retrieve the StoryLinks from the page
		String possibleStoryLinksJS = "";
		consoleLog("Getting JS");
		try {possibleStoryLinksJS = getPossibleStoryLinksJS(exceptionContainerID, exceptionContainerClassName);}
		catch (Exception e) {
			throw new AdShotRunnerException("StoryLinks: could not create Javascript", e);
		}
		
		//Execute the javascript and get the StoryLinks as a JSON response
		consoleLog("Executing JS");
		String storyLinksJSON = "";
		try {storyLinksJSON = executeSeleniumDriverJavascript(activeWebDriver, possibleStoryLinksJS);}
		catch (Exception e) {
			throw new AdShotRunnerException("StoryLinks: could not execute Javascript", e);
		}
		
		//Get the story links from the returned JSON
		List<StoryLink> storyLinks = getStoryLinksFromJSON(storyLinksJSON, url);
		
		//If not enough links were found and an exception was used, try without the exception
		if ((storyLinks.size() <= MINIMUMWITHEXCEPTION) && (storyExceptionContainer != null)) {
			
			//Create the javascript again but without the exceptions
			consoleLog("Getting JS without exceptions");
			try {possibleStoryLinksJS = getPossibleStoryLinksJS("", "");}
			catch (Exception e) {
				throw new AdShotRunnerException("StoryLinks: could not create Javascript", e);
			}
			
			//Execute the javascript and get the StoryLinks as a JSON response
			consoleLog("Executing JS");
			try {storyLinksJSON = executeSeleniumDriverJavascript(activeWebDriver, possibleStoryLinksJS);}
			catch (Exception e) {
				throw new AdShotRunnerException("StoryLinks: could not execute Javascript", e);
			}
			
			//Get the new StoryLinks
			storyLinks = getStoryLinksFromJSON(storyLinksJSON, url);
		}
		
		//Quit the driver
		quitWebdriver(activeWebDriver);
		
		//Return the found StoryLink objects
		return storyLinks;
	}
	
	//**************************** Private Static Methods ***********************************
	/**
	 * Returns an initialized Selenium Webdriver for headless Chrome.
	 * 
	 * (CURRENTLY USES TAG IMAGER NODES)
	 * 
	 * @param viewportWidth		Width of driver viewport to use
	 * @param viewportHeight	Height of driver viewport to use
	 * @return					Initialized Chrome WebDriver
	 */
	static private WebDriver getStoryDriver(int viewportWidth, int viewportHeight) throws MalformedURLException {

		//Define the path to the ChromeDriver. 
		System.setProperty("webdriver.chrome.driver", "chromedriver");
		
		//Create the capability, option, and preference objects for the driver
		consoleLog("Creating Story driver...");
		DesiredCapabilities driverCapabilities = DesiredCapabilities.chrome();
		ChromeOptions driverOptions = new ChromeOptions();	

		//Define the options to run the latest Chrome headless
		driverOptions.setBinary("/usr/bin/google-chrome-beta");
		driverOptions.addArguments("headless");
		driverOptions.addArguments("disable-gpu");

		//Set the viewport
		driverOptions.addArguments("window-size=" + viewportWidth + "," + viewportHeight);
		
		//Use the AWS tag imagers for now
		driverCapabilities.setCapability("applicationName", "awsTagImager");
//		driverCapabilities.setPlatform(Platform.WINDOWS);
				
		//Initialize the actual driver
		WebDriver chromeDriver = null;
		driverCapabilities.setCapability(ChromeOptions.CAPABILITY, driverOptions);
		chromeDriver = new RemoteWebDriver(
							new URL(SELENIUMHUBURL), 
							driverCapabilities);
		
		//Set the page timeout
		setCommandTimeout(chromeDriver, DEFAULTTIMEOUT);

		//Set the viewport
//		chromeDriver.manage().window().setSize(new Dimension(viewportWidth, viewportHeight));

		//Return the initialized remote chrome web driver
		consoleLog("Done creating Story driver.");
		return chromeDriver;
	} 

	/**
	 * Returns the container ID and/or class name to get StoryLinks from if a matching
	 * exception exists in the database for the passed URL.
	 * 
	 * This function matches the fullest possible subdomain. For example, www.example.com and
	 * news.example.com will match an entry for example.com. If a second entry exists for 
	 * www.example.com, a URL for www.example.com will match www.example.com while news.example.com
	 * will match example.com.
	 * 
	 * The exceptions are returned in a Map with the keys "id" and "className".
	 * 
	 * NULL is returned if no exceptions exist in the database.
	 * 
	 * @param url	URL to get exceptions for if any exist
	 * @return		Map with keys "id" and "className" or NULL if no exceptions found
	 */
	static private Map<String, String> getStoryLinkExceptionContainer(String url) {
		
		//Get the domain with subdomain of the url. The protocol type is not important. It is necessary for getDomain.
		String urlDomain = URLTool.getDomain(url);
		
		//Check the database to see if any entries matching the domain exist
		String matchingURL = "", containerID = "", containerClassName = "";
		try (ResultSet exceptionsSet = ASRDatabase.executeQuery("SELECT * " + 
															 	"FROM exceptionsStoryFinder " +
															 	"WHERE ESF_url LIKE '" + urlDomain + "%'")) {
				
			//If a match was found, store the container ID and class of the longest matching subdomain
			while (exceptionsSet.next()) {
							
				//Get the matching url
				String currentMatchingURL = exceptionsSet.getString("ESF_url");
				
				//If the current matching URL is a substring of the target URL, 
				if (url.toLowerCase().contains(currentMatchingURL.toLowerCase())) {
					
					//See if the new part is longer than the current
					if (currentMatchingURL.length() > matchingURL.length()) {
						
						//Store the new exception
						matchingURL = currentMatchingURL;
						containerID = exceptionsSet.getString("ESF_containerID");
						containerClassName = exceptionsSet.getString("ESF_className");
					}
				}			
			}
		} 
		
		//If the database was unaccessible, return NULL for the time being
		catch (Exception e) {return null;} 
		
		//If no matches in the database were found, return null
		if (matchingURL.isEmpty()) {return null;}
		
		//Otherwise, put the info in a map and return it
		else {
			Map<String, String> storyLinksExceptions = new HashMap<String, String>();
			storyLinksExceptions.put("id", containerID);
			storyLinksExceptions.put("className", containerClassName);
			return storyLinksExceptions;
		}
	} 
	
	/**
	 * Returns the getPossibleStoryLinks Javascript as a string with the passed
	 * exception containerID and className inserted. 
	 * 
	 * @param containerID			ID of element to get StoryLinks from within
	 * @param containerClassName	CSS class of elements to get StoryLinks from within
	 * @return						getPossibleStoryLinks javascript
	 */
	static private String getPossibleStoryLinksJS(String containerID, String containerClassName) throws IOException {
		
		//Get the getPossibleStoryLinks javascript file
		String storyLinksJS = new String(Files.readAllBytes(Paths.get(POSSIBLESTORYLINKSJSPATH)));
		
		//Insert the container exceptions into the script
		String exceptionVariables = "containerID = '" + containerID + "';\n";
		exceptionVariables += 		"containerClassName = '" + containerClassName + "';";
		String finalJS = storyLinksJS.replace("//INSERT EXCEPTIONS//", exceptionVariables);
				
		//Store the file for testing purposes
		FileUtils.writeStringToFile(new File("getPossibleStoryLinksWithExceptions.js"), finalJS);
		
		//Return the modified javascript
		return finalJS;
	}

	/**
	 * Converts the passed StoryLinksJSON into a list of StoryLink objects and
	 * returns the valid ones.
	 * 
	 * StoryLinks are considered invalid and removed from the list if:
	 * 		- The HREF field is empty or null
	 * 		- The HREF field is a mailto:
	 * 		- The HREF field points to an external domain
	 * 		- The HREF field is javascript and a URL cannot be found in it
	 * 
	 * @param storyLinksJSON	StoryLink JSON to convert to StoryLink objects
	 * @param url				URL the StoryLinkJSON came from
	 * @return					Valid StoryLinks
	 */
	static private List<StoryLink> getStoryLinksFromJSON(String storyLinksJSON, String url) {
		
		//Try to turn the pass JSON into a list of StoryLink objects
		Type arrayStoryLinksToken = new TypeToken<ArrayList<StoryLink>>(){}.getType();
		ArrayList<StoryLink> storyLinkList = new ArrayList<StoryLink>();
		try{
			storyLinkList = (new Gson()).fromJson(storyLinksJSON, arrayStoryLinksToken);
		}
		catch(Exception e){
			consoleLog("unable to parse linkJSON");	
		}
		
		//Get the primary domain of the URL (excludes subdomain)
		String primaryDomain = URLTool.getDomain(url);
		
		//Loop through the list removing null and empty href elements and setting null class to empty string
		Iterator<StoryLink> linksIterator = storyLinkList.iterator();
		while(linksIterator.hasNext()) {
			
			//Get the next StoryLink in the list
			StoryLink currentLink = linksIterator.next();
			
			//Check for // at beginning of href and add protocol if not there
			if ((currentLink.href != null) && (!currentLink.href.isEmpty()) && 
				(currentLink.href.length() >= 2) && (currentLink.href.substring(0, 2).equals("//"))){
				currentLink.href = "http:" + currentLink.href;
			}
			
			//If the text field is null or empty but there is a title, set the text equal to the title
			if ((currentLink.text == null || currentLink.text.isEmpty()) && 
				(currentLink.title != null && !currentLink.title.isEmpty())){
				currentLink.text = currentLink.title;
			}
			
			//If the class name is null, set it to an empty string
			if (currentLink.className == null) {currentLink.className = "";}
			
			//Remove the link if it has an empty or null href field
			String currentDomain = (currentLink.href != null) ? URLTool.getDomain(currentLink.href) : null;
			if (currentLink.href == null || currentLink.href.isEmpty()) {
				linksIterator.remove();
			}
			
			//Remove the link if it is a mailto:
			else if ((currentLink.href != null) && (!currentLink.href.isEmpty()) && (currentLink.href.length() >= 7) && 
					 (currentLink.href.substring(0, 7).equals("mailto:"))){
				linksIterator.remove();
			}
			
			//Remove the link if it points to an external domain
			else if ((currentDomain != "") && (!primaryDomain.equals(currentDomain))) {
				linksIterator.remove();
			}
			
			//If the link is javascript, try to get the URL out of it if one exists
			else if ((currentLink.href.length() >= 11) && currentLink.href.substring(0, 11).equals("javascript:")) {
			        
				//Search the href for a URL
				String urlPattern = "((https?|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
		        Matcher m = p.matcher(currentLink.href);
		        
		        //If a URL is found, set the href to it
		        if (m.find()) {currentLink.href = m.group(0);}
		        
		        //If no URL is found, remove the link
		        else {linksIterator.remove();}		    
			}
		}

		//Return the list of final StoryLinks
		return storyLinkList;
	}
}
