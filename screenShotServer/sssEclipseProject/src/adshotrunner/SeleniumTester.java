package adshotrunner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.io.BufferedWriter;
import java.io.FileWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

public class SeleniumTester {

	//final private static String SELENIUMHUBADDRESS = "http://localhost:4444/wd/hub";
	final private static String SELENIUMHUBADDRESS = "http://ec2-54-172-131-29.compute-1.amazonaws.com:4444/wd/hub";

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
		WebDriver firefoxDriver = null;
        try {
			firefoxDriver = new RemoteWebDriver(
			    			new URL(SELENIUMHUBADDRESS), 
			    			capabilities);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("Malformed URL");
		}
        
        List<String> urls = Arrays.asList("http://nytimes.com", 
        								  "http://slashdot.org", 
        								  "http://chicagotribune.com");
        
		for (String currentURL : urls) {
	        firefoxDriver.get(currentURL);
	        firefoxDriver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"t");
		}        
	    
		TimeUnit.SECONDS.sleep(1);
		firefoxDriver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"\t");  
		
		int screenshotIndex = 0;
		for (String currentURL : urls) {
			++screenshotIndex;
			File testScreenshot = captureSeleniumDriverScreenshot(firefoxDriver);
			testScreenshot.renameTo(new File("screenshot" + screenshotIndex + ".png"));
			firefoxDriver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"\t");  
		}        
			
	
	
	}
	
	
	private static File captureSeleniumDriverScreenshot(final WebDriver activeSeleniumWebDriver) {
		
		//Define the screenshot File variable to hold the final image
    	File screenShot = null;
    	
    	//Attempt to get screenshot a few times within the reasonable time frame
    	TimeLimiter timeoutLimiter = new SimpleTimeLimiter();
    	int currentAttempt = 0;
    	while ((screenShot == null) && (currentAttempt < 3)) {
    		try {
    		screenShot = timeoutLimiter.callWithTimeout(new Callable<File>() {
						    public File call() {
						      return ((TakesScreenshot) activeSeleniumWebDriver).getScreenshotAs(OutputType.FILE);
						    }
						  }, 25000, TimeUnit.MILLISECONDS, false); 
    		}
    		catch (Exception e) {
    			System.out.println("Error getting screenshot. -" + e.toString() );
    			//Ignore any error and try another attempt (if any are left)
    		}
    		++currentAttempt;
    	}
    	
    	return screenShot;
	}

}
