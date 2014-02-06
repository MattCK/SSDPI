/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package oata;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
/**
 *
 * @author matt
 */
public class AdClipTester {
    
    private boolean debug = true;
    private void dbgmsg(String Msg)
    {
        if (this.debug)
        {
            System.out.println("Debug: " + Msg);
        }
    }
    
   public Boolean saveAdClip(File adClip, String fileName)
    {
        Boolean saveSuccess = false;
        try
        {
            FileUtils.copyFile(adClip, new File(fileName));
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
        WebDriver driver = new RemoteWebDriver(
        new URL("http://localhost:4444/wd/hub"), 
        DesiredCapabilities.firefox());

        driver.get(URL);
        Dimension smallWindow = new Dimension(10,10);
        driver.manage().window().setSize(smallWindow);
        Thread.sleep(6000);
        
        // RemoteWebDriver does not implement the TakesScreenshot class
        // if the driver does have the Capabilities to take a screenshot
        // then Augmenter will add the TakesScreenshot methods to the instance
        //WebDriver augmentedDriver = new Augmenter().augment(driver);
        WebDriver augmentedDriver = new Augmenter().augment(driver);
        screenshot = ((TakesScreenshot)augmentedDriver).getScreenshotAs(OutputType.FILE);
        driver.close();
        }
        catch(MalformedURLException | WebDriverException | InterruptedException e)
        {
            this.dbgmsg("Unable to grab ad clip - " + e);
        }
        
        return screenshot;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
     
     AdClipTester clipper = new AdClipTester();
     File clipped = clipper.grabAdClip(args[0]);
     clipper.saveAdClip(clipped, args[1]);
     
     
        
        
        
    }
    
}
