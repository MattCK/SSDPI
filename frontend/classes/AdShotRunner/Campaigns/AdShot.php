<?PHP
/**
* Contains the class for creating and retrieving AdShots
*
* This class mirrors the Java and Javascript AdShot classes
* ANY CHANGES TO THIS CLASS SHOULD BE MADE IN THOSE CLASSES AS WELL.
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\Campaigns;

use AdShotRunner\Database\ASRDatabase;
use AdShotRunner\System\ASRProperties;
use AdShotRunner\Utilities\FileStorageClient;
use AdShotRunner\Campaigns\Creative;

/**
* The AdShot class creates creatives and retrieves AdShots from the database. 
*
* This class mirrors the Java and Javascript AdShot classes
* ANY CHANGES TO THIS CLASS SHOULD BE MADE IN THOSE CLASSES AS WELL.
*/
class AdShot {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------
	//Status constants for AdShot processing
	const CREATED = "CREATED"; 
	const PROCESSING = "PROCESSING"; 
	const FINISHED = "FINISHED"; 
	const ERROR = "ERROR"; 

	//URL path for screenshot images
	const SCREENSHOTURLPATH = "https://s3.amazonaws.com/";

	//---------------------------------------------------------------------------------------
	//---------------------------------- Static Methods -------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************* Public Static Methods *********************************
	/**
	 * Retrieves and returns an AdShot from the database with the provided ID.
	 * 
	 * @param 	int 		adShotID	ID of the AdShot
	 * @return 	Creative				AdShot on success and NULL on failure
	*/
	static public function getAdShot($adShotID) {

		//Verify the ID and that it's not less than 1. If not, return NULL.
		if ((!$adShotID) || (!is_numeric($adShotID)) || ($adShotID < 1)) {return NULL;}
		
		//Check to see if an AdShot with that ID exists in the database
		$getAdShotQuery = "SELECT * FROM adshots WHERE ADS_id = $adShotID";
		$adShotResult = ASRDatabase::executeQuery($getAdShotQuery);
		$adShotDetails = $adShotResult->fetch_assoc();

		//If no AdShot was found, return NULL.
		if (!$adShotDetails) {return NULL;}

		//Otherwise, create a new AdShot and put the database information into it
		$retrievedAdShot = new AdShot();
		$retrievedAdShot->_id = $adShotID;
		$retrievedAdShot->_uuid = $adShotDetails['ADS_uuid'];
		$retrievedAdShot->_campaignID = $adShotDetails['ADS_CMP_id'];
		$retrievedAdShot->_requestedURL = $adShotDetails['ADS_requestedURL'];
		$retrievedAdShot->_storyFinder = $adShotDetails['ADS_storyFinder'];
		$retrievedAdShot->_mobile = $adShotDetails['ADS_mobile'];
		$retrievedAdShot->_belowTheFold = $adShotDetails['ADS_belowTheFold'];
		$retrievedAdShot->_finalURL = $adShotDetails['ADS_finalURL'];
		$retrievedAdShot->_pageTitle = $adShotDetails['ADS_pageTitle'];
		$retrievedAdShot->_imageFilename = $adShotDetails['ADS_imageFilename'];
		$retrievedAdShot->_width = $adShotDetails['ADS_width'];
		$retrievedAdShot->_height = $adShotDetails['ADS_height'];
		$retrievedAdShot->_status = $adShotDetails['ADS_status'];
		$retrievedAdShot->_errorMessage = $adShotDetails['ADS_errorMessage'];
		$retrievedAdShot->_createdTimestamp = strtotime($adShotDetails['ADS_createdTimestamp']);
		$retrievedAdShot->_processingTimestamp = strtotime($adShotDetails['ADS_processingTimestamp']);
		$retrievedAdShot->_finishedTimestamp = strtotime($adShotDetails['ADS_finishedTimestamp']);
		$retrievedAdShot->_errorTimestamp = strtotime($adShotDetails['ADS_errorTimestamp']);

		//Get the Creatives associated with the AdShot
		$creativesQuery = "SELECT * FROM adshotCreatives WHERE ASC_ADS_id = $adShotID";
		$creativesResult = ASRDatabase::executeQuery($creativesQuery);

		//Add the creatives to the AdShot
		$retrievedAdShot->_creatives = [];
		$creativesByID = [];
		while ($creativeItem = $creativesResult->fetch_assoc()) {
			$adShotCreative = Creative::getCreative($creativeItem['ASC_CRV_id']);
			if ($adShotCreative) {
				$retrievedAdShot->_creatives[] = $adShotCreative;
				$creativesByID[$adShotCreative->id()] = $adShotCreative;
			}
		}

		//Get the injected Creatives
		$injectedQuery = "SELECT * FROM injectedCreatives WHERE IJC_ADS_id = $adShotID";
		$injectedResult = ASRDatabase::executeQuery($injectedQuery);

		//Add the injected creatives to the AdShot
		$retrievedAdShot->_injectedCreatives = [];
		while ($injectedCreativeItem = $injectedResult->fetch_assoc()) {
			if ($creativesByID[$injectedCreativeItem['IJC_CRV_id']]) {
				$retrievedAdShot->_injectedCreatives[] = $creativesByID[$injectedCreativeItem['IJC_CRV_id']];
			}
		}

		//Return the retrieved AdShot
		return $retrievedAdShot;
	}

	/**
	 * Creates a new AdShot using the passed information and returns it.
	 *
	 * The AdShot status is set to CREATED.
	 *
	 * @param 	int 		campaignID			ID of the Campaign the AdShot belongs to
	 * @param 	String 		requestedURL		Requested URL of the AdShot (Before StoryFinder)
	 * @param 	boolean 	useStoryFinder		TRUE to use a story from the requested URL page
	 * @param 	boolean 	useMobile			TRUE to take a mobile screenshot
	 * @param 	boolean 	useBelowTheFold		TRUE to take a Below-the-Fold screenshot
	 * @return 	AdShot					Newly created AdShot
	*/
	static public function create($campaignID, $requestedURL, $useStoryFinder, $useMobile, $useBelowTheFold) {

		//Verify a proper campaign ID and that it's not less than 1. If not, return NULL.
		if ((!$campaignID) || (!is_numeric($campaignID)) || ($campaignID < 1)) {return NULL;}

		//Verify a campaign with that ID exists in the database
		$campaignQuery = "SELECT * FROM campaigns WHERE CMP_id = $campaignID";
		$campaignResult = ASRDatabase::executeQuery($campaignQuery);
		$campaignDetails = $campaignResult->fetch_assoc();
		if (!$campaignDetails) {return NULL;}

		//Verify a requested URL was passed. If not, return NULL.
		if ((!$requestedURL) || ($requestedURL == "")) {return NULL;}

		//Create a unique UUID for the AdShot 
		$adShotUUID = self::getUUID();

		//Add the AdShot to the adshots table
		$addAdShotQuery = "INSERT INTO adshots (ADS_uuid,
												 ADS_CMP_id,
												 ADS_requestedURL,
												 ADS_storyFinder,
												 ADS_mobile,
												 ADS_belowTheFold,
												 ADS_USR_id)
						 	 VALUES ('" . ASRDatabase::escape($adShotUUID) . "',
									 						  $campaignID,
									 '" . ASRDatabase::escape($requestedURL) . "',
									 '" . ASRDatabase::escape($useStoryFinder) . "',
									 '" . ASRDatabase::escape($useMobile) . "',
									 '" . ASRDatabase::escape($useBelowTheFold) . "',
									 '" . USERID . "')";
		ASRDatabase::executeQuery($addAdShotQuery);
		
		//Get the ID of the new AdShot
		$adShotID = ASRDatabase::lastInsertID();

		//Get the newly created AdShot with all of its defaults such as CREATED timestamp set
		return self::getAdShot($adShotID);
	}

	//******************************* Private Static Methods *********************************
	/**
	 * Returns a random UUID
	 *
	 * Taken from: https://stackoverflow.com/a/15875555
	 * 
	 * @return 	String		Unique random-generated UUID
	*/
	static private function getUUID() {

		$data = random_bytes(16);
	    $data[6] = chr(ord($data[6]) & 0x0f | 0x40); // set version to 0100
	    $data[8] = chr(ord($data[8]) & 0x3f | 0x80); // set bits 6-7 to 10
	    return vsprintf('%s%s-%s-%s-%s-%s%s%s', str_split(bin2hex($data), 4));
    }


	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	* @var int  ID of the AdShot
	*/
	private $_id;

	/**
	* @var string  UUID of the AdShot
	*/
	private $_uuid;

	/**
	* @var int  ID of the campaign the AdShot is associated with
	*/
	private $_campaignID;

	/**
	* @var string   Requested URL of the screenshot. (This is the URL before the StoryFinder is ran if so flagged)
	*/
	private $_requestedURL;

	/**
	* @var boolean	Flags whether or not the StoryFinder should be used. TRUE if the StoryFinder should be used.
	*/
	private $_storyFinder;

	/**
	* @var boolean	Flags whether or not the AdShot is for mobile. TRUE if the AdShot is for mobile.
	*/
	private $_mobile;

	/**
	* @var boolean	Flags whether or not the AdShot should be taken below the fold. TRUE if the AdShot should be taken below the fold.
	*/
	private $_belowTheFold;

	/**
	* @var array  Creatives associated with the AdShot
	*/
	private $_creatives;

	/**
	* @var string  Final URL of the AdShot
	*/
	private $_finalURL;

	/**
	* @var string  Page title of the AdShot's final URL
	*/
	private $_pageTitle;

	/**
	* @var array  Creatives injected into the final AdShot image (This will always be the set or a subset of the associated AdShots)
	*/
	private $_injectedCreatives;

	/**
	* @var string  Filename of the AdShot image
	*/
	private $_imageFilename;

	/**
	* @var int  Width of the AdShot image in pixels
	*/
	private $_width;

	/**
	* @var int  Height of the AdShot image in pixels
	*/
	private $_height;

	/**
	* @var string  Current processing status of the AdShot (Static class constants: CREATED, QUEUED, PROCESSING, FINISHED, ERROR)
	*/
	private $_status;

	/**
	* @var string  Error message if an error occurred while processing the AdShot
	*/
	private $_errorMessage;

	/**
	* @var int  The UNIX timestamp the AdShot was inserted into the database
	*/
	private $_createdTimestamp;

	/**
	* @var int  The UNIX timestamp the AdShot status was set to PROCESSING
	*/
	private $_processingTimestamp;

	/**
	* @var int  The UNIX timestamp the AdShot status was set to FINISHED
	*/
	private $_finishedTimestamp;

	/**
	* @var int  The UNIX timestamp the AdShot status was set to ERROR when an error occurred
	*/
	private $_errorTimestamp;

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Private constructor for static factories
	*/
	private function __construct() {}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************
	/**
	 * Adds a Creative to the AdShot. 
	 *
	 * Associates the AdShot with the Creative in the database and adds it to the instance.
	 *
	 * If the Creative has already been added, nothing occurs.
	 * 
	 * @param 	Creative 		$newCreative 	Creative to add to the AdShot
	 * @return 	boolean			True on success and NULL on failure
	 */	
	public function addCreative(Creative $newCreative) {

		//If the creative has already been added, do nothing and return true. 
		//IDs are a stronger check than objects.
		$alreadyExists = false;
		foreach ($this->_creatives as $currentCreative) {
			if ($currentCreative->id() == $newCreative->id()) {$alreadyExists = true;}
		}
		if ($alreadyExists) {return TRUE;}

		//Associate the new creative in the database
		$setStatusQuery = "INSERT IGNORE INTO adshotCreatives (ASC_ADS_id, ASC_CRV_id)
						   VALUES (" . $this->id() . ", " . $newCreative->id() . ")";
		ASRDatabase::executeQuery($setStatusQuery);

		//Add the Creative to the instance
		$this->_creatives[] = $newCreative;

		//Return success
		return TRUE;
	}

	/**
	 * Adds the Creative with the provided ID to the AdShot. 
	 *
	 * Associates the AdShot with the Creative in the database and adds it to the instance.
	 *
	 * If the Creative has already been added, nothing occurs.
	 * 
	 * @param 	Creative 		$creativeID 	Creative to add to the AdShot
	 * @return 	boolean			True on success and NULL on failure
	 */	
	public function addCreativeByID($creativeID) {

		//Get the Creative. If not found, return NULL.
		$newCreative = Creative::getCreative($creativeID);
		if (!$newCreative) {return NULL;}

		//Add the Creative and return the result
		return $this->addCreative($newCreative);
	}

	/**
	 * Returns a JSON object representing all of the member values of the AdShot instance.
	 *
	 * The final object also includes full URLs for the image and tag page files.
	 * 
	 * @return 	String		JSON object representing the member values of the instance
	*/
	public function toJSON() {

		//Create the associative array with all the key value pairs
		$jsonObject = [];
		$jsonObject["id"] = $this->_id;
		$jsonObject["uuid"] = $this->_uuid;
		$jsonObject["campaignID"] = $this->_campaignID;
		$jsonObject["requestedURL"] = $this->_requestedURL;
		$jsonObject["storyFinder"] = $this->_storyFinder;
		$jsonObject["mobile"] = $this->_mobile;
		$jsonObject["belowTheFold"] = $this->_belowTheFold;
		$jsonObject["finalURL"] = $this->_finalURL;
		$jsonObject["pageTitle"] = $this->_pageTitle;
		$jsonObject["imageFilename"] = $this->_imageFilename;
		$jsonObject["imageURL"] = $this->imageURL();
		$jsonObject["width"] = $this->_width;
		$jsonObject["height"] = $this->_height;
		$jsonObject["status"] = $this->_status;
		$jsonObject["errorMessage"] = $this->_errorMessage;
		$jsonObject["createdTimestamp"] = $this->_createdTimestamp;
		$jsonObject["processingTimestamp"] = $this->_processingTimestamp;
		$jsonObject["finishedTimestamp"] = $this->_finishedTimestamp;
		$jsonObject["errorTimestamp"] = $this->_errorTimestamp;

		//Add the Creative as an array of their own JSON strings
		$jsonObject["creatives"] = [];
		foreach ($this->_creatives as $currentCreative) {
			$jsonObject["creatives"][] = $currentCreative->toJSON();
		}

		//Add the injected Creative as an array of IDs
		$jsonObject["injectedCreatives"] = [];
		foreach ($this->_injectedCreatives as $currentInjectedCreative) {
			$jsonObject["injectedCreatives"][] = $currentInjectedCreative->id();
		}

		//Convert the array to a JSON string and return it
		return json_encode($jsonObject);
	}	


	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* @return int	ID of the AdShot
	*/
	public function id() {
		return $this->_id;
	}

	/**
	* @return string	UUID of the AdShot
	*/
	public function uuid() {
		return $this->_uuid;
	}

	/**
	* @return int	ID of the campaign the AdShot is associated with
	*/
	public function campaignID() {
		return $this->_campaignID;
	}

	/**
	* @return string	Requested URL of the screenshot. (This is the URL before the StoryFinder is ran if so flagged)
	*/
	public function requestedURL() {
		return $this->_requestedURL;
	}
	
	/**
	* @return boolean	Flag for whether or not the StoryFinder should be used. (TRUE to use the StoryFinder)
	*/
	public function storyFinder() {
		return $this->_storyFinder;
	}
	
	/**
	* @return boolean	Flag for whether or not the AdShot is for mobile (TRUE to use mobile)
	*/
	public function mobile() {
		return $this->_mobile;
	}
	
	/**
	* @return boolean	Flag for whether or not the AdShot should be taken below the fold (TRUE to take it below the fold)
	*/
	public function belowTheFold() {
		return $this->_belowTheFold;
	}
	
	/**
	* @return array		Creatives associated with the AdShot
	*/
	public function creatives() {
		return $this->_creatives;
	}

	/**
	* @return string	Final URL of the AdShot
	*/
	public function finalURL() {
		return $this->_finalURL;
	}

	/**
	* @return string	Page title of the AdShot's final URL
	*/
	public function pageTitle() {
		return $this->_pageTitle;
	}

	/**
	* @return array		Creatives injected into the final AdShot image (This will always be the set or a subset of the associated Creatives)
	*/
	public function injectedCreatives() {
		return $this->_injectedCreatives;
	}

	/**
	* @return string	Filename of the AdShot image
	*/
	public function imageFilename() {
		return $this->_imageFilename;
	}

	/**
	* @return string	Full URL including protocol to the image
	*/
	public function imageURL() {
		if ($this->_imageFilename == "") {return "";}
		return $this::SCREENSHOTURLPATH . ASRProperties::containerForScreenshots() . "/" . $this->_imageFilename;
	}

	/**
	* @return int	Width of the AdShot image in pixels
	*/
	public function width() {
		return $this->_width;
	}

	/**
	* @return int	Height of the AdShot image in pixels
	*/
	public function height() {
		return $this->_height;
	}

	/**
	* @return string	Current processing status of the Creative. (Options const members: CREATED, QUEUED, PROCESSING, FINISHED, ERROR)
	*/
	public function status() {
		return $this->_status;
	}

	/**
	* @return string	Error message if an error occurred while processing the Creative
	*/
	public function errorMessage() {
		return $this->_errorMessage;
	}

	/**
	* @return int	UNIX Timestamp the Creative was inserted into the database
	*/
	public function createdTimestamp() {
		return $this->_createdTimestamp;
	}

	/**
	* @return int	UNIX Timestamp the Creative status was set to PROCESSING
	*/
	public function processingTimestamp() {
		return $this->_processingTimestamp;
	}

	/**
	* @return int	UNIX Timestamp the Creative status was set to FINISHED
	*/
	public function finishedTimestamp() {
		return $this->_finishedTimestamp;
	}

	/**
	* @return int	UNIX timestamp the Creative status was set to ERROR when an error occurred
	*/
	public function errorTimestamp() {
		return $this->_errorTimestamp;
	}

}