<?PHP
/**
* Contains the class for creating and retrieving Campaigns
*
* This class mirrors the Java and Javascript Creative classes
* ANY CHANGES TO THIS CLASS SHOULD BE MADE IN THOSE CLASSES AS WELL.
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\Campaigns;

use AdShotRunner\Database\ASRDatabase;
use AdShotRunner\System\ASRProperties;
use AdShotRunner\Utilities\FileStorageClient;
use AdShotRunner\PowerPoint\PowerPointBackground;
use AdShotRunner\Campaigns\AdShot;

/**
* The Creative class creates creatives and retrieves them from the database. 
*
* This class mirrors the Java and Javascript Creative classes
* ANY CHANGES TO THIS CLASS SHOULD BE MADE IN THOSE CLASSES AS WELL.
*/
class Campaign {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------
	//Status constants for Campaign processing
	const CREATED = "CREATED"; 
	const READY = "READY"; 
	const QUEUED = "QUEUED"; 
	const PROCESSING = "PROCESSING"; 
	const FINISHED = "FINISHED"; 
	const ERROR = "ERROR"; 

	//Error constants
	const SCREENSHOTCAPTURE = "SCREENSHOTCAPTURE"; 
	const POWERPOINTGENERATION = "POWERPOINTGENERATION"; 
	const CAMPAIGNEMAILSEND = "CAMPAIGNEMAILSEND"; 

	//URL path for screenshot images
	const POWERPOINTURLPATH = "https://s3.amazonaws.com/";

	//---------------------------------------------------------------------------------------
	//---------------------------------- Static Methods -------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************* Public Static Methods *********************************
	/**
	 * Retrieves and returns a Campaign from the database with the provided ID.
	 * 
	 * @param 	int 		campaignID	ID of the Campaign
	 * @return 	Campaign				Campaign on success and NULL on failure
	*/
	static public function getCampaign($campaignID) {

		//Verify the ID and that it's not less than 1. If not, return NULL.
		if ((!$campaignID) || (!is_numeric($campaignID)) || ($campaignID < 1)) {return NULL;}
		
		//Check to see if a Campaign with that ID exists in the database
		$getCampaignQuery = "SELECT * FROM campaigns WHERE CMP_id = $campaignID";
		$campaignResult = ASRDatabase::executeQuery($getCampaignQuery);
		$campaignDetails = $campaignResult->fetch_assoc();

		//If no creative was found, return NULL.
		if (!$campaignDetails) {return NULL;}

		//Otherwise, create a new Campaign and put the database information into it
		$retrievedCampaign = new Campaign();
		$retrievedCampaign->_id = $campaignID;
		$retrievedCampaign->_uuid = $campaignDetails['CMP_uuid'];
		$retrievedCampaign->_customerName = $campaignDetails['CMP_customerName'];
		$retrievedCampaign->_powerPointFilename = $campaignDetails['CMP_powerPointFilename'];
		$retrievedCampaign->_status = $campaignDetails['CMP_status'];
		$retrievedCampaign->_errorMessage = $campaignDetails['CMP_errorMessage'];
		$retrievedCampaign->_createdTimestamp = strtotime($campaignDetails['CMP_createdTimestamp']);
		$retrievedCampaign->_readyTimestamp = strtotime($campaignDetails['CMP_readyTimestamp']);
		$retrievedCampaign->_queuedTimestamp = strtotime($campaignDetails['CMP_queuedTimestamp']);
		$retrievedCampaign->_processingTimestamp = strtotime($campaignDetails['CMP_processingTimestamp']);
		$retrievedCampaign->_finishedTimestamp = strtotime($campaignDetails['CMP_finishedTimestamp']);
		$retrievedCampaign->_errorTimestamp = strtotime($campaignDetails['CMP_errorTimestamp']);

		//Get the PowerPointBackground
		$retrievedCampaign->_powerPointBackground = PowerPointBackground::getPowerPointBackground($campaignDetails['CMP_PPB_id']);

		//Get the AdShots associated with the Campaign
		$adShotsQuery = "SELECT * FROM adshots WHERE ADS_CMP_id = $campaignID";
		$adShotsResults = ASRDatabase::executeQuery($adShotsQuery);

		//Add the AdShots to the Campaign
		$retrievedCampaign->_adShots = [];
		while ($adShotEntry = $adShotsResults->fetch_assoc()) {
			$retrievedCampaign->_adShots[] = AdShot::getAdShot($adShotEntry['ADS_id']);
		}

		//Return the Campaign
		return $retrievedCampaign;
	}

	/**
	 * Retrieves and returns a Campaign from the database with the provided UUID.
	 * 
	 * @param 	String 		campaignUUID	UUID of the Campaign
	 * @return 	Campaign					Campaign on success and NULL on failure
	*/
	static public function getCampaignByUUID($campaignUUID) {

		//Verify the ID and that it's not an empty string. If not, return NULL.
		if ((!$campaignUUID) || ($campaignUUID == "")) {return NULL;}
		
		//Check to see if a Campaign with that UUID exists in the database
		$getCampaignQuery = "SELECT * FROM campaigns WHERE CMP_uuid = '$campaignUUID'";
		$campaignResult = ASRDatabase::executeQuery($getCampaignQuery);
		$campaignDetails = $campaignResult->fetch_assoc();

		//If no creative was found, return NULL.
		if (!$campaignDetails) {return NULL;}

		//Otherwise, get and return the full Campaign
		return Campaign::getCampaign($campaignDetails["CMP_id"]);
	}

	/**
	 * Creates a new Campaign using the passed information and returns it.
	 *
	 * The Campaign status is set to CREATED.
	 *
	 * @param 	String 		customerName				Customer name for the Campaign
	 * @param 	int 		powerPointBackgroundID		ID of the PowerPointBackground to use for final PowerPoint
	 * @return 	Campaign								Newly created Campaign
	*/
	static public function create($customerName, $powerPointBackgroundID) {

		//Verify a customer name was passed. If not, return NULL.
		if ((!$customerName) || ($customerName == "")) {return NULL;}

		//Verify the PowerPointBackground exists
		$campaignBackground = PowerPointBackground::getPowerPointBackground($powerPointBackgroundID);
		if (!$campaignBackground) {return NULL;}

		//Create a unique UUID for the Campaign 
		$campaignUUID = self::getUUID();

		//Add the Campaign to the campaigns table
		$addCampaignQuery = "INSERT INTO campaigns (CMP_uuid,
												    CMP_customerName,
												    CMP_PPB_id,
												    CMP_USR_id)
						 	 VALUES ('" . ASRDatabase::escape($campaignUUID) . "',
									 '" . ASRDatabase::escape($customerName) . "',
									 						  $powerPointBackgroundID,
									 '" . USERID . "')";
		ASRDatabase::executeQuery($addCampaignQuery);
		
		//Get the ID of the new Campaign
		$campaignID = ASRDatabase::lastInsertID();

		//Get the newly created Campaign with all of its defaults such as CREATED timestamp set
		return self::getCampaign($campaignID);
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
	* @var int  ID of the Campaign
	*/
	private $_id;

	/**
	* @var string  UUID of the Campaign
	*/
	private $_uuid;

	/**
	* @var string  Customer name for the Campaign
	*/
	private $_customerName;

	/**
	* @var array  AdShots associated with the Campaign (array)
	*/
	private $_adShots;

	/**
	* @var PowerPointBackground  PowerPointBackground for the final PowerPoint
	*/
	private $_powerPointBackground;

	/**
	* @var string  Filename of the final PowerPoint
	*/
	private $_powerPointFilename;

	/**
	* @var string  Current processing status of the Campaign (Static class constants: CREATED, QUEUED, PROCESSING, FINISHED, ERROR)
	*/
	private $_status;

	/**
	* @var string  Error message if an error occurred while processing the Campaign
	*/
	private $_errorMessage;

	/**
	* @var int  The UNIX timestamp the Campaign was inserted into the database
	*/
	private $_createdTimestamp;

	/**
	* @var int  The UNIX timestamp the Campaign status was set to READY
	*/
	private $_readyTimestamp;

	/**
	* @var int  The UNIX timestamp the Campaign status was set to QUEUED
	*/
	private $_queuedTimestamp;

	/**
	* @var int  The UNIX timestamp the Campaign status was set to PROCESSING
	*/
	private $_processingTimestamp;

	/**
	* @var int  The UNIX timestamp the Campaign status was set to FINISHED
	*/
	private $_finishedTimestamp;

	/**
	* @var int  The UNIX timestamp the Campaign status was set to ERROR when an error occurred
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
	 * Creates a new AdShot associated to the Campaign.
	 *
	 * @param 	String 		requestedURL		Requested URL of the AdShot (Before StoryFinder)
	 * @param 	boolean 	useStoryFinder		TRUE to use a story from the requested URL page
	 * @param 	boolean 	useMobile			TRUE to take a mobile screenshot
	 * @param 	boolean 	useBelowTheFold		TRUE to take a Below-the-Fold screenshot
	 * @return 	AdShot					Newly created AdShot
	*/
	public function createAdShot($requestedURL, $useStoryFinder, $useMobile, $useBelowTheFold) {

		//Create the AdShot. Return NULL if nothing returned.
		$newAdShot = AdShot::create($this->id(), $requestedURL, $useStoryFinder, $useMobile, $useBelowTheFold);
		if (!$newAdShot) {return NULL;}

		//Add the AdShot to the instance
		$this->_adShots[] = $newAdShot;

		//Return the AdShot
		return $newAdShot;
	}

	/**
	 * Creates a new AdShot associated to the Campaign using the provided JSON string,
	 * which is generated by a javascript AdShotBuilder on the client.
	 *
	 * @param 	String 		adShotJSON		New AdShot JSON string generated by an AdShotBuilder
	 * @return 	AdShot						Newly created AdShot
	*/
	public function createAdShotFromJSON($adShotJSON) {

		//Decode the JSON and validate the variables
		$jsonObject = json_decode($adShotJSON, true);
		if (($jsonObject["requestedURL"] == null)) {return null;}

		//Create the AdShot. Return NULL if nothing returned.
		$newAdShot = AdShot::create($this->id(), $jsonObject["requestedURL"], $jsonObject["storyFinder"], 
												 $jsonObject["mobile"], $jsonObject["belowTheFold"]);
		if (!$newAdShot) {return NULL;}

		//Add the Creative to the AdShot
		foreach ($jsonObject["creativeIDs"] as $currentID) {
			$addCreativeSuccess = $newAdShot->addCreativeByID($currentID);
			if (!$addCreativeSuccess) {return NULL;}
		}

		//Add the AdShot to the instance
		$this->_adShots[] = $newAdShot;

		//Return the AdShot
		return $newAdShot;
	}

	/**
	 * Marks the Campaign ready for processing.
	 *
	 * This notifies the processing server the Campaign is fully built and is ready for its
	 * AdShots to be imaged and the resulting PowerPoint to be made.
	 * 
	 * @return 	boolean		True on success and NULL on failure
	 */
	public function readyForProcessing() {

		//Update the status in the database
		$setStatusQuery = "UPDATE campaigns 
						   SET CMP_status = '" . $this::READY . "', 
						   	   CMP_readyTimestamp = CURRENT_TIMESTAMP 
						   WHERE CMP_id = " . $this->_id;
		ASRDatabase::executeQuery($setStatusQuery);

		//Set the new status in the instance
		$this->_status = $this::QUEUED;
		
		//Query the database for the new timestamp. This is to prevent localization errors.
		$getCampaignQuery = "SELECT * FROM campaigns WHERE CMP_id = " . $this->_id;
		$campaignResult = ASRDatabase::executeQuery($getCampaignQuery);
		$campaignEntry = $campaignResult->fetch_assoc();
		$this->_readyTimestamp = strtotime($campaignEntry["CMP_readyTimestamp"]);

		//Return success
		return TRUE;
	}

	/**
	 * Returns a JSON object representing all of the member values of the Campaign instance.
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
		$jsonObject["customerName"] = $this->_customerName;
		$jsonObject["powerPointFilename"] = $this->_powerPointFilename;
		$jsonObject["powerPointURL"] = $this->powerPointURL();
		$jsonObject["status"] = $this->_status;
		$jsonObject["errorMessage"] = $this->_errorMessage;
		$jsonObject["createdTimestamp"] = $this->_createdTimestamp;
		$jsonObject["readyTimestamp"] = $this->_readyTimestamp;
		$jsonObject["queuedTimestamp"] = $this->_queuedTimestamp;
		$jsonObject["processingTimestamp"] = $this->_processingTimestamp;
		$jsonObject["finishedTimestamp"] = $this->_finishedTimestamp;
		$jsonObject["errorTimestamp"] = $this->_errorTimestamp;

		//Add the AdShots as an array of their own JSON strings
		$jsonObject["adShots"] = [];
		foreach ($this->_adShots as $currentAdShot) {
			$jsonObject["adShots"][] = $currentAdShot->toJSON();
		}

		//Convert the array to a JSON string and return it
		return json_encode($jsonObject);
	}	

	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* @return int	ID of the Campaign
	*/
	public function id() {
		return $this->_id;
	}

	/**
	* @return string	UUID of the Campaign
	*/
	public function uuid() {
		return $this->_uuid;
	}

	/**
	* @return string	Customer name for the Campaign
	*/
	public function customerName() {
		return $this->_customerName;
	}

	/**
	* @return array 	AdShots associated with the Campaign (array)
	*/
	public function adShots() {
		return $this->_adShots;
	}

	/**
	* @return PowerPointBackground 	PowerPointBackground for the final PowerPoint
	*/
	public function powerPointBackground() {
		return $this->_powerPointBackground;
	}

	/**
	* @return string	Filename of the final PowerPoint
	*/
	public function powerPointFilename() {
		return $this->_powerPointFilename;
	}

	/**
	* @return string	Full URL including protocol to the final PowerPoint
	*/
	public function powerPointURL() {
		if ($this->_powerPointFilename == "") {return "";}
		return $this::POWERPOINTURLPATH . ASRProperties::containerForPowerPoints() . "/" . $this->_powerPointFilename;
	}

	/**
	* @return string	Current processing status of the Campaign. (Options const members: CREATED, QUEUED, PROCESSING, FINISHED, ERROR)
	*/
	public function status() {
		return $this->_status;
	}

	/**
	* @return string	Error message if an error occurred while processing the Campaign
	*/
	public function errorMessage() {
		return $this->_errorMessage;
	}

	/**
	* @return int	UNIX Timestamp the Campaign was inserted into the database
	*/
	public function createdTimestamp() {
		return $this->_createdTimestamp;
	}

	/**
	* @return int	UNIX Timestamp the Campaign status was set to READY
	*/
	public function readyTimestamp() {
		return $this->_readyTimestamp;
	}

	/**
	* @return int	UNIX Timestamp the Campaign status was set to QUEUED
	*/
	public function queuedTimestamp() {
		return $this->_queuedTimestamp;
	}

	/**
	* @return int	UNIX Timestamp the Campaign status was set to PROCESSING
	*/
	public function processingTimestamp() {
		return $this->_processingTimestamp;
	}

	/**
	* @return int	UNIX Timestamp the Campaign status was set to FINISHED
	*/
	public function finishedTimestamp() {
		return $this->_finishedTimestamp;
	}

	/**
	* @return int	UNIX timestamp the Campaign status was set to ERROR when an error occurred
	*/
	public function errorTimestamp() {
		return $this->_errorTimestamp;
	}




}