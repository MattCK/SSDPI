package adshotrunner.shotter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.system.ASRProperties;

/**
 * The BasicShotter class contains protected constants and functions used by both the 
 * AdShotter and the TagShotter.
 * 
 * The constants include the Selenium Hub address, default timeouts, and the number of
 * screenshot attempts to take. The functions include navigating a web driver, executing
 * javascript in it, and quitting it.
 */
public class BasicShotter {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//Selenium Hub URL
	final protected static String SELENIUMHUBURL = ASRProperties.seleniumHubURL(); 
			
	//Timeouts for page loads and javascript execution
	final protected static int PAGETIMEOUT = 12000;			//in milliseconds
	final protected static int DEFAULTTIMEOUT = 1000;		//in milliseconds
	final protected static int JAVASCRIPTWAITTIME = 2500;	//in milliseconds

	//Screenshot timeout and attempt amount
	final protected static int SCREENSHOTATTEMPTS = 3;
	final protected static int SCREENSHOTTIMEOUT = 11000;		//in milliseconds
		
	//Set to TRUE to show console output and FALSE to hide output
	final protected static boolean VERBOSE = true;

	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//**************************** Protected Static Methods *********************************
	/**
	 * Navigates the passed selenium driver to the passed URL.
	 * 
	 * The timeout is set to the PAGETIMEOUT constant.
	 * 
	 * @param activeWebDriver			Selenium web driver to navigate
	 * @param pageURL					Page URL to navigate to
	 */
	static protected boolean navigateSeleniumDriverToURL(WebDriver activeWebDriver, String pageURL) {
		
		return navigateSeleniumDriverToURL(activeWebDriver, pageURL, PAGETIMEOUT);
	}
		
	/**
	 * Navigates the passed selenium driver to the passed URL.
	 * 
	 * @param activeWebDriver			Selenium web driver to navigate
	 * @param pageURL					Page URL to navigate to
	 * @param navigationTimeout			Time the page should stop loading after in milliseconds
	 * @return							TRUE if the navigation succeeded and FALSE otherwise
	 */
	static protected boolean navigateSeleniumDriverToURL(WebDriver activeWebDriver, String pageURL, int navigationTimeout) {
		
		//Set the navigation command timeout and navigate to the url
		consoleLog("		Sending navigation command: " + pageURL + "...");
		try {
			setCommandTimeout(activeWebDriver, navigationTimeout);
			((JavascriptExecutor) activeWebDriver).executeScript("window.location.href = '" + pageURL + "';");
			setCommandTimeout(activeWebDriver, DEFAULTTIMEOUT);
			consoleLog("		Navigation command sent.");		
			return true;
		} 
		
		//If it failed, return false
		catch (Exception e) {return false;}
	}

	/**
	 * Executes the passed javascript on the current selenium driver page. 
	 * 
	 * After execution, there is a pause equal to the constant JAVASCRIPTWAITTIME before moving forward. 
	 * This allows spawned threads such as image loading to finish. 
	 * 
	 * The String response from the executed script is returned.
	 * 
	 * @param activeWebDriver		Selenium driver to execute javascript on
	 * @param javascriptCode		Javascript code to execute
	 * @return						Response message from the browser
	 */
	static protected String executeSeleniumDriverJavascript(WebDriver activeWebDriver, String javascriptCode) {
		
		//Attempt to execute the script. If this fails, a runtime error will be thrown
		String response = (String) ((JavascriptExecutor) activeWebDriver).executeScript(javascriptCode);
		
		//Pause a moment to let the javascript execute (this is for threads such as image loading, etc.)
		pause(JAVASCRIPTWAITTIME);
		
		//Return the response from the executed javascript
		return response;
	}

	/**
	 * Attempts to take a screenshot using the passed selenium web driver
	 * 
	 * The screenshot File is returned on success, a runtime error is thrown on failure
	 * 
	 * @param activeWebDriver		Selenium driver to take screenshot on
	 * @return						Screenshot of selenium driver window
	 */
	static protected File captureSeleniumDriverScreenshot(final WebDriver activeWebDriver) {
		
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
							  return ((TakesScreenshot) activeWebDriver).getScreenshotAs(OutputType.FILE);
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
	 * Quits the passed selenium web driver. 
	 * 
	 * If the driver does not quit after a matter of seconds, program execution continues.
	 * 
	 * @param activeWebDriver	Selenium driver to quit
	 */
	static protected void quitWebdriver(WebDriver activeWebDriver) {
		
		consoleLog("Quitting web driver...");
		final WebDriver finalDriver = activeWebDriver;
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
	 * Sets the time each chromedriver command runs for.
	 * 
	 * NOTE: THIS IS FOR USE WITH THE MODIFIED CHROMEDRIVER!!!
	 * 
	 * With the chromedriver, pagetimout does not refer to the loading time of a
	 * page but to the timeout of every command.
	 * 
	 * This is due to a bug with the chromedriver.
	 * 
	 * @param activeWebDriver		Selenium driver to set command timeout on
	 * @param timeout				Timeout in milliseconds each chromedriver command should run
	 */
	static protected void setCommandTimeout(WebDriver activeWebDriver, int timeout) {
		activeWebDriver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.MILLISECONDS);
	}

	
	/**
	 * Alias for Thread.sleep(...) with InterruptedException ignored
	 * 
	 * @param pauseTime		Time in miliseconds to pause/sleep
	 */
	static protected void pause(int pauseTime) {
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
	 * 
	 * @param message		Message to output to the console
	 */
	static protected void consoleLog(String message) {
		if (VERBOSE) {
			
			//Remove gecko driver binary if present
			String cleanMessage = message.replaceAll("profile(.*?)appBuild", "");
			
			//Get the current timestamp
			String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
			
			//Output the message preceded by the timestamp and thread ID
			System.out.println(timeStamp + " - " + Thread.currentThread().getId() + ": " + cleanMessage);
		}
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	protected BasicShotter() {}

}
