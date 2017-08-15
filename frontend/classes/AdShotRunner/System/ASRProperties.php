<?PHP
/**
* Contains the class for retrieving properties information for the ASR system
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\System;

/**
* The ASRProperties class returns system properties for the AdShotRunner system. Such
* properties include database permissions and message queues.
*/
class ASRProperties {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	const PROPERTIESFILE = RESTRICTEDPATH . "asrProperties.ini";

	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Variables ------------------------------------
	//---------------------------------------------------------------------------------------
	//***************************** Private Static Variables ********************************
	private static $properties = null;

	//---------------------------------------------------------------------------------------
	//---------------------------------- Static Methods -------------------------------------
	//---------------------------------------------------------------------------------------
	//***************************** Public Static Methods ***********************************

	//Database Methods
	public static function asrDatabaseHost() {return (self::getProperties())['asrDatabase']['host'];}
	public static function asrDatabaseUsername() {return (self::getProperties())['asrDatabase']['username'];}
	public static function asrDatabasePassword() {return (self::getProperties())['asrDatabase']['password'];}
	public static function asrDatabase() {return (self::getProperties())['asrDatabase']['database'];}

	//Database Methods
	public static function asrDomain() {return (self::getProperties())['domains']['asrDomain'];}

	//Database Methods
	public static function awsAccessKey() {return (self::getProperties())['aws']['accessKey'];}
	public static function awsSecretKey() {return (self::getProperties())['aws']['secretKey'];}
	public static function awsRegion() {return (self::getProperties())['aws']['region'];}

	//Storage containers
	public static function containerForCampaignJobs() {return (self::getProperties())['storageContainers']['campaignJobs'];}
	public static function containerForPowerPoints() {return (self::getProperties())['storageContainers']['powerPoints'];}
	public static function containerForPowerPointBackgrounds() {return (self::getProperties())['storageContainers']['powerPointBackgrounds'];}
	public static function containerForScreenshots() {return (self::getProperties())['storageContainers']['screenshots'];}
	public static function containerForCreativeImages() {return (self::getProperties())['storageContainers']['creativeimages'];}
	public static function containerForTagPages() {return (self::getProperties())['storageContainers']['tagPages'];}
	public static function containerForTagTexts() {return (self::getProperties())['storageContainers']['tagTexts'];}

	//DFP credentials
	public static function dfpClientID() {return (self::getProperties())['dfp']['clientID'];}
	public static function dfpClientSecret() {return (self::getProperties())['dfp']['clientSecret'];}
	public static function dfpRefreshToken() {return (self::getProperties())['dfp']['refreshToken'];}
	public static function dfpApplicationName() {return (self::getProperties())['dfp']['applicationName'];}

	//Email Addresses
	public static function emailAddressDoNotReply() {return (self::getProperties())['emailAddresses']['doNotReply'];}
	public static function emailAddressContact() {return (self::getProperties())['emailAddresses']['contact'];}
	public static function emailAddressInfo() {return (self::getProperties())['emailAddresses']['info'];}
	public static function emailAddressSupport() {return (self::getProperties())['emailAddresses']['support'];}

	//Message queues
	public static function queueForTagImageRequests() {return (self::getProperties())['messageQueues']['tagImageRequests'];}
	public static function queueForScreenshotRequests() {return (self::getProperties())['messageQueues']['screenshotRequests'];}

	//Notification groups
	public static function notificationGroupForSSSIssues() {return (self::getProperties())['notificationGroups']['sssIssues'];}

	//PowerPoint
	public static function powerPointDefaultBackgroundImage() {return (self::getProperties())['powerPoint']['defaultBackgroundImage'];}
	public static function powerPointDefaultTitle() {return (self::getProperties())['powerPoint']['defaultTitle'];}
	public static function powerPointDefaultFont() {return (self::getProperties())['powerPoint']['defaultFont'];}

	//***************************** Private Static Methods **********************************
	private static function getProperties() {

		//If the properties have not been retrieved, get them from the file
		if (self::$properties == null) {
			self::$properties = parse_ini_file(self::PROPERTIESFILE, true);
		}

		return self::$properties;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************

	//******************************** Protected Methods ************************************

	//********************************* Private Methods *************************************



}


//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------

