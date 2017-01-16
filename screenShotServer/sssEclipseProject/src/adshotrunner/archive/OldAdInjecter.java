package adshotrunner.archive;

import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.awt.image.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;

import org.openqa.selenium.*;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.firefox.*;

public class OldAdInjecter {

	
    private final boolean debug = true;
    private void dbgmsg(String Msg)
    {
        if (this.debug)
        {
            System.out.println("Debug: " + Msg);
        }
    }
    
	public Boolean insertImagePath(String tagID) {
	
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
		Class.forName("com.mysql.jdbc.Driver");
		conn =
		   DriverManager.getConnection("jdbc:mysql://10.1.1.17:3306/ssPrototype?" +
									   "user=root&password=qwas12");
		
		stmt = conn.createStatement();
		if (stmt.execute("INSERT INTO tagAdClips VALUES (" + tagID + ", '" + tagID + ".png')")) {
			rs = stmt.getResultSet();
		}
		  } catch (Exception e) {dbgmsg(e + " ");}

		// Do something with the Connection

		//rs.next();
		//String tagScript = rs.getString("TGI_usedTag");
		return true;
	
	}
	
   public Boolean saveAdClip(File adClip, String tagID)
    {
        Boolean saveSuccess = false;
        try
        {
            FileUtils.copyFile(adClip, new File("" + tagID + ".png"));
            saveSuccess = true;
        }
        catch(IOException e)
        {
           this.dbgmsg("Unable to save adClip - " + e);
        }
        return saveSuccess;
    }
   
   public String createAdClipFileName()
   {
       Boolean fileAlreadyExists = true;
       UUID potentialFileName = UUID.randomUUID();
       while(fileAlreadyExists)
         { 
            File f = new File(potentialFileName.toString() + ".png");
            if(!f.exists()) 
            {
                fileAlreadyExists = false;
            }
            else
            {
                potentialFileName = UUID.randomUUID();
            }
         }
        return potentialFileName.toString() + ".png";
       
   }
    
    public File grabAdClip(String URL)
    {
        File screenshot = null;
       
        try
        {
			
		/*DesiredCapabilities capability = new DesiredCapabilities();
		capability.setBrowserName(DesiredCapabilities.firefox().getBrowserName());
		FirefoxProfile profile = new ProfilesIni().getProfile("Selenium");
		capability.setCapability(FirefoxDriver.PROFILE, profile);
		RemoteWebDriver remoteDriver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capability);*/
		ProfilesIni profile = new ProfilesIni();
	
		FirefoxProfile myprofile = profile.getProfile("selenium");

		WebDriver remoteDriver = new RemoteWebDriver(
		new URL("http://localhost:4444/wd/hub"), 
		DesiredCapabilities.firefox());
		
		remoteDriver.manage().window().setPosition(new Point(0, 0));
		remoteDriver.manage().window().setSize(new Dimension(1920, 1080));
		//selenium.windowMaximize();
			
        //Dimension normalWindow = new Dimension(1200, 900);
		//remoteDriver.manage().window().setSize(normalWindow);

		//remoteDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		
        remoteDriver.get(URL);
		
		String filePath = "adInjecter.js";
		
		String adJavascript = "";
		try {
			adJavascript = new String(Files.readAllBytes(Paths.get(filePath)));
		} catch (IOException e) {}
		
		((JavascriptExecutor) remoteDriver).executeScript(adJavascript);
		/*driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        Dimension smallWindow = new Dimension(425,200);
		
        driver.manage().window().setSize(normalWindow);*/
		
		//IWait<IWebDriver> wait = new OpenQA.Selenium.Support.UI.WebDriverWait(driver, TimeSpan.FromSeconds(30.00));
		//wait.Until(driver1 => ((IJavaScriptExecutor)driver).ExecuteScript("return document.readyState").Equals("complete"));

		/*Thread.sleep(3000);
		
		//Mouse imageMouse = driver.getMouse();
		Actions myMouse = new Actions(driver); 
		//WebElement bodyElement = driver.find_element_by_xpath("/html/body");
		WebElement bodyElement = driver.findElement(By.xpath("/html/body"));
		//myMouse.moveToElement(bodyElement, 428, 78).click();
		myMouse.click(bodyElement);
		//myMouse.moveToElement(bodyElement);
		//myMouse.dragAndDropBy(bodyElement, 5, 5);
		myMouse.perform();
		//myMouse.moveToElement(bodyElement, 428, 378);
		//myMouse.moveByOffset(1, 1);
        
		Thread.sleep(6000);*/
        
        // RemoteWebDriver does not implement the TakesScreenshot class
        // if the driver does have the Capabilities to take a screenshot
        // then Augmenter will add the TakesScreenshot methods to the instance
        //WebDriver augmentedDriver = new Augmenter().augment(driver);
        WebDriver augmentedDriver = new Augmenter().augment(remoteDriver);
        screenshot = ((TakesScreenshot)augmentedDriver).getScreenshotAs(OutputType.FILE);
        //remoteDriver.close();
        }
        //catch(MalformedURLException | WebDriverException | InterruptedException e)
        catch(MalformedURLException | WebDriverException e)
        {
            this.dbgmsg("Unable to grab ad clip - " + e);
        }
        
        return screenshot;
    }


	public File cropAdClip(File adClip)
    {	
		BufferedImage sourceAdClip = null;
		try {
			sourceAdClip = ImageIO.read(adClip);
		}
		catch(IOException e)
        {
           this.dbgmsg("Unable to open adClip image - " + e);
        }

		int width = sourceAdClip.getWidth();
		int height = sourceAdClip.getHeight();
		//dbgmsg("width - " + width);
		//dbgmsg("height - " + height);
		BufferedImage croppedAdClip = sourceAdClip.getSubimage(425, 75, width - 425, height - 75);
		
		try {
			ImageIO.write(croppedAdClip, "png", adClip);
		}
		catch(IOException e)
        {
           this.dbgmsg("Unable to open adClip image - " + e);
        }

		return adClip;
    }
	
	
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
     
     OldAdInjecter injecter = new OldAdInjecter();
     File adClip = injecter.grabAdClip(args[0]);
     //adClip = clipper.cropAdClip(adClip);
     injecter.saveAdClip(adClip, args[1]);
	 //injecter.insertImagePath(args[1]);
    }

}
