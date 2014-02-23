/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phantomjstester;

/**
 *
 * @author matt
 */
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;

public class PhantomJSTester {
      private WebDriver driver;
  private String baseUrl;
  private StringBuffer verificationErrors = new StringBuffer();
  protected static DesiredCapabilities dCaps;

  @Before
  public void setUp() throws Exception {
	  
	  dCaps = new DesiredCapabilities();
	  dCaps.setJavascriptEnabled(true);
	  dCaps.setCapability("takesScreenshot", false);
	  
	  driver = new PhantomJSDriver(dCaps);
	  baseUrl = "http://assertselenium.com/";
	  driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @Test
  public void getLinksOfAssertSelenium() throws Exception {
    driver.get(baseUrl + "/");
    //Getting all the links present in the page by a HTML tag.
    java.util.List  links = driver.findElements(By.tagName("a"));
    
    //Printing the size, will print the no of links present in the page.
    System.out.println("Total Links present is "+links.size());
    
    //Printing the links in the page, we get through the href attribute.
    for(int i = 0; i < links.size();i++){
   	
    	System.out.println("Links are listed "+links.get(i).toString());
    }
  }

  @After
  public void tearDown() throws Exception {
    driver.quit();
    String verificationErrorString = verificationErrors.toString();
    if (!&amp;quot;&amp;quot;.equals(verificationErrorString)) {
      fail(verificationErrorString);
    }
  }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        
        
    }
    
}
