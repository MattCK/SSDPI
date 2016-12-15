package adshotrunner.techpreview;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class ScreenshotTest {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		 System.setProperty("webdriver.chrome.driver", "/home/juicio/Desktop/selenium/chromedriver");

		  WebDriver driver = new ChromeDriver();
		  driver.manage().timeouts().pageLoadTimeout(7000, TimeUnit.MILLISECONDS);
		  driver.get("http://www.wkyc.com/");
		  //driver.get("http://www.chicagotribune.com/lifestyles/");
		  File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		  //Thread.sleep(5000);  // Let the user actually see something!
		  //WebElement searchBox = driver.findElement(By.name("q"));
		  //searchBox.sendKeys("ChromeDriver");
		  //searchBox.submit();
		  //Thread.sleep(5000);  // Let the user actually see something!
		  driver.quit();
	}

}
