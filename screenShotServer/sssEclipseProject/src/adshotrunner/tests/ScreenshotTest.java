package adshotrunner.tests;

import java.io.File;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

public class ScreenshotTest {

	public static void main(String[] args) throws InterruptedException {
		//System.setProperty("webdriver.chrome.driver", "/home/matt/Work/DangerousPenguins/localTesting/chromedriver");
//		System.setProperty("webdriver.chrome.driver", "/home/juicio/Desktop/selenium/chromedriver");
		System.setProperty("webdriver.chrome.driver", "chromedriver");
		
		DesiredCapabilities driverCapabilities = DesiredCapabilities.chrome();
		ChromeOptions driverOptions = new ChromeOptions();
		driverOptions.setBinary("/usr/bin/google-chrome-beta");
		
		driverOptions.addArguments("hide-scrollbars");
		driverOptions.addArguments("start-maximized");
		driverOptions.addArguments("headless");
		driverOptions.addArguments("disable-gpu");
		
		driverCapabilities.setCapability(ChromeOptions.CAPABILITY, driverOptions);
		
		 
		 WebDriver driver = new ChromeDriver(driverCapabilities);
		  //driver.manage().timeouts().pageLoadTimeout(4000, TimeUnit.MILLISECONDS);
		  System.out.println("about to get website");
		  try{
//			  driver.get("https://development.adshotrunner.com/tagPageTest.html");
			  driver.get("https://news.ycombinator.com");
		  }
		  catch (Exception e){
			  System.out.println("caught get site exception : " + e);
		  }

		  System.out.println("waiting 15 seconds");
		try {
			Thread.sleep(15000);
		} 
		catch (InterruptedException e) {
			//If the sleep was interrupted, just keep moving
		} 
		  System.out.println("done waiting");

		  
		  System.out.println("After Get before take screenshot");
		  //driver.manage().timeouts().pageLoadTimeout(40000, TimeUnit.MILLISECONDS);
		  //driver.get("http://www.chicagotribune.com/lifestyles/");
		  try{
			  File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			  System.out.println("After takescreenhot before rename");
			  screenshot.renameTo(new File("screenshot.png"));
			  System.out.println("after file rename");
		  }
		  catch (Exception e){
			  System.out.println("caught screenshot exception : " + e);
		  }
		 
		  //Thread.sleep(5000);  // Let the user actually see something!
		  //WebElement searchBox = driver.findElement(By.name("q"));
		  //searchBox.sendKeys("ChromeDriver");
		  //searchBox.submit();
		  //Thread.sleep(5000);  // Let the user actually see something!
		  driver.quit();
	}

}
