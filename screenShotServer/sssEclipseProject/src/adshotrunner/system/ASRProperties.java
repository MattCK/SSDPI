package adshotrunner.system;

import java.io.FileInputStream;
import java.util.Properties;

public class ASRProperties {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	final private static String PROPERTIESFILE = "config/asr.properties";

	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Variables -------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Variables *********************************

	//**************************** Protected Static Variables *******************************


	//***************************** Private Static Variables ********************************
	private static Properties asrProperties = null;

	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	//Database properties
	public static String asrDatabaseHost() {return getProperties().getProperty("asrDatabaseHost");}
	public static String asrDatabaseUsername() {return getProperties().getProperty("asrDatabaseUsername");}
	public static String asrDatabasePassword() {return getProperties().getProperty("asrDatabasePassword");}
	public static String asrDatabase() {return getProperties().getProperty("asrDatabase");}

	//ASR Domain
	public static String asrDomain() {return getProperties().getProperty("asrDomain");}

	//AWS Credentials
	public static String awsAccessKey() {return getProperties().getProperty("awsAccessKey");}
	public static String awsSecretKey() {return getProperties().getProperty("awsSecretKey");}
	public static String awsRegion() {return getProperties().getProperty("awsRegion");}

	//File storage containers
	public static String containerForCampaignJobs() {return getProperties().getProperty("containerForCampaignJobs");}
	public static String containerForPowerPoints() {return getProperties().getProperty("containerForPowerPoints");}
	public static String containerForPowerPointBackgrounds() {return getProperties().getProperty("containerForPowerPointBackgrounds");}
	public static String containerForScreenshots() {return getProperties().getProperty("containerForScreenshots");}
	public static String containerForTagImages() {return getProperties().getProperty("containerForTagImages");}
	public static String containerForTagPages() {return getProperties().getProperty("containerForTagPages");}
	public static String containerForTagTexts() {return getProperties().getProperty("containerForTagTexts");}

	//Email Addresses
	public static String emailAddressScreenshots() {return getProperties().getProperty("emailAddressScreenshots");}
	
	//Notification groups
	public static String notificationGroupForFrontend() {return getProperties().getProperty("notificationGroupForFrontend");}
	public static String notificationGroupForSSS() {return getProperties().getProperty("notificationGroupForSSS");}
	
	//Paths
	public static String pathForAdInjecterJavascript() {return getProperties().getProperty("pathForAdInjecterJavascript");}
	public static String pathForAdMarkerExtension() {return getProperties().getProperty("pathForAdMarkerExtension");}
	public static String pathForDefaultBackground() {return getProperties().getProperty("pathForDefaultBackground");}
	public static String pathForHTMLWithoutAnchorsJavascript() {return getProperties().getProperty("pathForHTMLWithoutAnchorsJavascript");}
	public static String pathForPhantomJS() {return getProperties().getProperty("pathForPhantomJS");}
	public static String pathForPossibleStoriesJavascript() {return getProperties().getProperty("pathForPossibleStoriesJavascript");}
	public static String pathForTemporaryFiles() {return getProperties().getProperty("pathForTemporaryFiles");}
	
	//Message queues
	public static String queueForScreenshotRequests() {return getProperties().getProperty("queueForScreenshotRequests");}
	public static String queueForTagImageRequests() {return getProperties().getProperty("queueForTagImageRequests");}
	
	//Selenium Hub information
	public static String seleniumHubURL() {return getProperties().getProperty("seleniumHubURL");}
	
	
	//**************************** Protected Static Methods *********************************


	//***************************** Private Static Methods **********************************
	private static Properties getProperties() {
		
		//Get the properties from the file if not yet done
		if (asrProperties == null) {
			asrProperties = new Properties();
			FileInputStream propertiesInputStream;
			try {
				propertiesInputStream = new FileInputStream(PROPERTIESFILE);
				asrProperties.load(propertiesInputStream);
				propertiesInputStream.close();
			} catch (Exception e) {System.out.println(e);}
		}
		
		return asrProperties;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Public Variables *************************************
	
	

	//******************************* Protected Variables ***********************************


	//******************************** Private Variables ************************************

	
}
