<?PHP
/**
* Contains the class for creating and retrieving Creatives
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
use AdShotRunner\Utilities\WebPageCommunicator;

/**
* The Creative class creates creatives and retrieves them from the database. 
*
* This class mirrors the Java and Javascript Creative classes
* ANY CHANGES TO THIS CLASS SHOULD BE MADE IN THOSE CLASSES AS WELL.
*/
class Creative {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------
	//Status constants for Creative processing
	const CREATED = "CREATED"; 
	const READY = "READY"; 
	const QUEUED = "QUEUED"; 
	const PROCESSING = "PROCESSING"; 
	const FINISHED = "FINISHED"; 
	const ERROR = "ERROR"; 

	//Error constants
	const URLNAVIGATION = "URLNAVIGATION";
	const SCREENSHOTCAPTURE = "SCREENSHOTCAPTURE";
	const SCREENSHOTCROP = "SCREENSHOTCROP";
	const IMAGEUPLOAD = "IMAGEUPLOAD";

	//URL path for creative images
	const CREATIVEURLPATH = "https://s3.amazonaws.com/";

	//---------------------------------------------------------------------------------------
	//---------------------------------- Static Methods -------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************* Public Static Methods *********************************
	/**
	 * Retrieves and returns a Creative from the database with the provided ID.
	 * 
	 * @param 	int 		creativeID	ID of the Creative
	 * @return 	Creative				Creative on success and NULL on failure
	*/
	static public function getCreative($creativeID) {

		//Verify the ID and that it's not less than 1. If not, return NULL.
		if ((!$creativeID) || (!is_numeric($creativeID)) || ($creativeID < 1)) {return NULL;}
		
		//Check to see if a Creative with that ID exists in the database
		$getCreativeQuery = "SELECT * FROM creatives WHERE CRV_id = $creativeID";
		$creativeResult = ASRDatabase::executeQuery($getCreativeQuery);
		$creativeDetails = $creativeResult->fetch_assoc();

		//If no creative was found, return NULL.
		if (!$creativeDetails) {return NULL;}

		//Otherwise, create a new Creative and put the database information into it
		$retrievedCreative = new Creative();
		$retrievedCreative->_id = $creativeID;
		$retrievedCreative->_uuid = $creativeDetails['CRV_uuid'];
		$retrievedCreative->_imageFilename = $creativeDetails['CRV_imageFilename'];
		$retrievedCreative->_width = $creativeDetails['CRV_width'];
		$retrievedCreative->_height = $creativeDetails['CRV_height'];
		$retrievedCreative->_priority = $creativeDetails['CRV_priority'];
		$retrievedCreative->_tagScript = $creativeDetails['CRV_tagScript'];
		$retrievedCreative->_tagPageFilename = $creativeDetails['CRV_tagPageFilename'];
		$retrievedCreative->_status = $creativeDetails['CRV_status'];
		$retrievedCreative->_errorMessage = $creativeDetails['CRV_errorMessage'];
		$retrievedCreative->_finalError = $creativeDetails['CRV_finalError'];
		$retrievedCreative->_createdTimestamp = strtotime($creativeDetails['CRV_createdTimestamp']);
		$retrievedCreative->_readyTimestamp = strtotime($creativeDetails['CRV_readyTimestamp']);
		$retrievedCreative->_queuedTimestamp = strtotime($creativeDetails['CRV_queuedTimestamp']);
		$retrievedCreative->_processingTimestamp = strtotime($creativeDetails['CRV_processingTimestamp']);
		$retrievedCreative->_finishedTimestamp = strtotime($creativeDetails['CRV_finishedTimestamp']);
		$retrievedCreative->_errorTimestamp = strtotime($creativeDetails['CRV_errorTimestamp']);
		return $retrievedCreative;
	}

	/**
	 * Creates a new Creative using the passed image file.
	 *
	 * On success, the image file will be uploaded to storage and class variables
	 * like width and height will be set according to its attributes.
	 *
	 * The status will be automatically set to FINISHED upon completion.
	 *
	 * The priority will be set to 0 and can be changed after with the
	 * setPriority(...) function.
	 *
	 * This function allows for most image file types such as JPG and BMP. It
	 * converts the file to a PNG before upload.
	 * 
	 * @param 	String 		imageFile	Path and filename to image file
	 * @return 	Creative				New Creative created based on passed image file
	*/
	static public function createFromImageFile($imageFile) {

		//Verify an image file was passed. If not, return NULL.
		if ((!$imageFile) || ($imageFile == "")) {return NULL;}

		//Create a unique UUID for the Creative and its files
		$creativeUUID = self::getUUID();

		//Convert the file to an image resource
		$imageFilename = $creativeUUID . ".png";
		$temporaryFile = RESTRICTEDPATH . 'temporaryFiles/' . $imageFilename;
		$creativeImage = imagecreatefromstring(file_get_contents($imageFile));
		if (!$creativeImage) {return NULL;}

		//Store the image's width and height
		$imageWidth = imagesx($creativeImage);
		$imageHeight = imagesy($creativeImage);

		//Convert the image to a PNG
		imagepng($creativeImage, $temporaryFile);

		//Upload the image and delete the local copy
		FileStorageClient::saveFile(ASRProperties::containerForCreativeImages(), $temporaryFile, $imageFilename);
		unlink($temporaryFile);

		//Add the Creative to the creatives table
		$addCreativeQuery = "INSERT INTO creatives (CRV_uuid,
													CRV_imageFilename,
													CRV_width,
													CRV_height,
													CRV_USR_id)
						 	 VALUES ('" . ASRDatabase::escape($creativeUUID) . "',
									 '" . ASRDatabase::escape($imageFilename) . "',
									 '" . ASRDatabase::escape($imageWidth) . "',
									 '" . ASRDatabase::escape($imageHeight) . "',
									 '" . USERID . "')";
		ASRDatabase::executeQuery($addCreativeQuery);
		
		//Get the ID of the new Creative
		$creativeID = ASRDatabase::lastInsertID();

		//Get the newly created Creative with all of its defaults such as CREATED timestamp set
		//Set its status to finished and return it.
		$newCreative = self::getCreative($creativeID);
		$newCreative->setStatus(self::FINISHED);
		return $newCreative;
	}

	/**
	 * Creates a new Creative using the passed image file URL.
	 *
	 * On success, the image file will be downloaded from the URL,
	 * uploaded to storage and class variables like width and height 
	 * will be set according to its attributes.
	 *
	 * The status will be automatically set to FINISHED upon completion.
	 *
	 * The priority will be set to 0 and can be changed after with the
	 * setPriority(...) function.
	 *
	 * This function allows for most image file types such as JPG and BMP. It
	 * converts the file to a PNG before upload.
	 * 
	 * @param 	String 		imageURL	Image file URL
	 * @return 	Creative				New Creative created based on passed image file
	*/
	static public function createFromImageURL($imageURL) {

		//Verify an image URL was passed. If not, return NULL.
		if ((!$imageURL) || ($imageURL == "")) {return NULL;}

		//Download the image 
		$webCommunicator = new WebPageCommunicator();
		$creativeImage = $webCommunicator->getURLResponse($imageURL);
		if (!$creativeImage) {return NULL;}

		//Save the image to a temporary file
		$temporaryFile = RESTRICTEDPATH . 'temporaryFiles/' . self::getUUID() . ".tmp";
		file_put_contents($temporaryFile, $creativeImage);

		//Create the Creative from the file, delete the file, and return the new Creative
		$newCreative = self::createFromImageFile($temporaryFile);
		unlink($temporaryFile);
		return $newCreative;
	}

	/**
	 * Creates a new Creative using the passed tag script and sets
	 * its status to QUEUED so that TagImager will process it.
	 *
	 * Generates a "Tag Page" for the script and uploads it to storage.
	 * A tag page puts the script in a small amount of HTML framework
	 * that the TagImager navigates to in order to capture the
	 * creative image.
	 *
	 * On success, the "Tag Page" will be created and uploaded, the
	 * creative will be added to the database, and its status will
	 * be set to QUEUED to notify the TagImager it needs to be
	 * processed.
	 * 
	 * The priority will be set to 0 and can be changed after with the
	 * setPriority(...) function.
	 *
	 * @param 	String 		tagScript	Creative tag script to image
	 * @return 	Creative				New Creative created based on passed image file
	*/
	static public function createFromTagScript($tagScript) {

		//Verify a tag script was passed. If not, return NULL.
		if ((!$tagScript) || ($tagScript == "")) {return NULL;}

		//Create a unique UUID for the Creative and its files
		$creativeUUID = self::getUUID();

		//Get the tag page HTML and put it into a temporary file
		$tagPageFilename = $creativeUUID . ".html";
		$temporaryFile = RESTRICTEDPATH . 'temporaryFiles/' . $tagPageFilename;
		$tagPageHTML = self::getTagPageHTML($tagScript);
		file_put_contents($temporaryFile, $tagPageHTML);

		//Upload the tag page to storage then delete it
		FileStorageClient::saveFile(ASRProperties::containerForTagPages(), $temporaryFile, $tagPageFilename);
		unlink($temporaryFile);

		//Add the Creative to the creatives table
		$addCreativeQuery = "INSERT INTO creatives (CRV_uuid,
													CRV_tagScript,
													CRV_tagPageFilename,
													CRV_USR_id)
						 	 VALUES ('" . ASRDatabase::escape($creativeUUID) . "',
									 '" . ASRDatabase::escape($tagScript) . "',
									 '" . ASRDatabase::escape($tagPageFilename) . "',
									 '" . USERID . "')";
		ASRDatabase::executeQuery($addCreativeQuery);
		
		//Get the ID of the new Creative
		$creativeID = ASRDatabase::lastInsertID();

		//Get the newly created Creative with all of its defaults such as CREATED timestamp set
		//Set its status to queued and return it.
		$newCreative = self::getCreative($creativeID);
		$newCreative->setStatus(self::READY);
		return $newCreative;
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

	/**
	 * Creates the tag page HTML with the passed tag script included into it.
	 *
	 * The HTML is a simple framework to remove borders, margin, and padding
	 * from the final image (usually an IFrame). 
	 *
	 * The div holding the script tag is given the ID "adTagContainer". This
	 * is used by the backend to properly get the creative's size which
	 * is used to crop the image.
	 * 
	 * @param 	String 	tagScript	Creative tag script
	 * @return 	String				Tag page HTML with the passed script inserted
	*/
	static private function getTagPageHTML($tagScript) {

		return "<style>body { margin: 0px; padding:0px;} #adTagContainer {display: table;}</style>
				<div id='adTagContainer'>" . $tagScript . "</div>";
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	* @var int  ID of the creative
	*/
	private $_id;

	/**
	* @var string  UUID of the creative
	*/
	private $_uuid;

	/**
	* @var string  Filename of the creative image
	*/
	private $_imageFilename;

	/**
	* @var int  Width of the image in pixels
	*/
	private $_width;

	/**
	* @var int  Height of the image in pixels
	*/
	private $_height;

	/**
	* @var int  Relative priority of the creative in relation to creative of the same dimensions
	*/
	private $_priority;

	/**
	* @var string  The tag script used to generate the Creative image. (Can be empty string)
	*/
	private $_tagScript;

	/**
	* @var string  The tag page used to generate the Creative image. (Can be empty string)
	*/	
	private $_tagPageFilename;

	/**
	* @var string  Current processing status of the Creative (Static class constants: CREATED, QUEUED, PROCESSING, FINISHED, ERROR)
	*/
	private $_status;

	/**
	* @var string  Error message if an error occurred while processing the Creative
	*/
	private $_errorMessage;

	/**
	* @var boolean  TRUE if an error occurred during processing and no more attempts will be made, FALSE otherwise
	*/
	private $_finalError;

	/**
	* @var int  The UNIX timestamp the Creative was inserted into the database
	*/
	private $_createdTimestamp;

	/**
	* @var int  The UNIX timestamp the Creative status was set to READY
	*/
	private $_readyTimestamp;

	/**
	* @var int  The UNIX timestamp the Creative status was set to QUEUED
	*/
	private $_queuedTimestamp;

	/**
	* @var int  The UNIX timestamp the Creative status was set to PROCESSING
	*/
	private $_processingTimestamp;

	/**
	* @var int  The UNIX timestamp the Creative status was set to FINISHED
	*/
	private $_finishedTimestamp;

	/**
	* @var int  The UNIX timestamp the Creative status was set to ERROR when an error occurred
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
	 * Sets the processing status of the Creative to ERROR, sets the error timestamp, and stores
	 * the passed error message.
	 * 
	 * If an error already exists for the Creative, this function will overwrite it and its
	 * timestamp.
	 * 
	 * @param 	string 		$errorMessage 	Error message
	 * @return 	boolean		True on success and NULL on failure
	 */
	public function setError($errorMessage) {

		//Update the status in the database
		$setStatusQuery = "UPDATE creatives 
						   SET CRV_status = '" . $this::ERROR . "', 
						   	   CRV_errorMessage = '" . ASRDatabase::escape($errorMessage) . "',
						       CRV_errorTimestamp = CURRENT_TIMESTAMP 
						   WHERE CRV_id = " . $this->_id;
		ASRDatabase::executeQuery($setStatusQuery);

		//Set the new error message and status in the instance
		$this->_errorMessage = $errorMessage;
		$this->_status = $this::ERROR;
		
		//Query the database for the new timestamp. This is to prevent localization errors.
		$getCreativeQuery = "SELECT * FROM creatives WHERE CRV_id = " . $this->_id;
		$creativeResult = ASRDatabase::executeQuery($getCreativeQuery);
		$creativeDetails = $creativeResult->fetch_assoc();
		$this->_errorTimestamp = strtotime($creativeDetails["CRV_errorTimestamp"]);

		//Return success
		return TRUE;
	}

	/**
	 * Sets the priority level of the Creative.
	 * 
	 * @param 	string 		$priorityLevel	 	Error message
	 * @return 	boolean							True on success and NULL on failure
	 */
	public function setPriority($priorityLevel) {

		//Verify the priority exists and is numeric. If not, return NULL.
		if (($priorityLevel === NULL) || (!is_numeric($priorityLevel)) || (!is_int($priorityLevel))) {return NULL;}

		//Update the priority in the database
		$setStatusQuery = "UPDATE creatives 
						   SET CRV_priority = $priorityLevel 
						   WHERE CRV_id = " . $this->_id;
		ASRDatabase::executeQuery($setStatusQuery);

		//Set the new priority in the instance
		$this->_priority = $priorityLevel;

		//Return success
		return TRUE;
	}

	/**
	 * Returns a JSON object representing all of the member values of the Creative instance.
	 *
	 * The final object also includes full URLs for the image and tag page files.
	 * 
	 * @return 	String		JSON object representing the member values of the instance
	*/
	public function toJSON() {

		//Create the associative array with all the key value pairs
		$jsonObject = [];
		$jsonObject["id"] = (int) $this->_id;
		$jsonObject["uuid"] = $this->_uuid;
		$jsonObject["imageFilename"] = $this->_imageFilename;
		$jsonObject["imageURL"] = $this->imageURL();
		$jsonObject["width"] = $this->_width;
		$jsonObject["height"] = $this->_height;
		$jsonObject["priority"] = $this->_priority;
		$jsonObject["tagScript"] = $this->_tagScript;
		$jsonObject["tagPageFilename"] = $this->_tagPageFilename;
		$jsonObject["tagPageURL"] = $this->tagPageURL();
		$jsonObject["status"] = $this->_status;
		$jsonObject["errorMessage"] = $this->_errorMessage;
		$jsonObject["finalError"] = $this->_finalError;
		$jsonObject["createdTimestamp"] = $this->_createdTimestamp;
		$jsonObject["readyTimestamp"] = $this->_readyTimestamp;
		$jsonObject["queuedTimestamp"] = $this->_queuedTimestamp;
		$jsonObject["processingTimestamp"] = $this->_processingTimestamp;
		$jsonObject["finishedTimestamp"] = $this->_finishedTimestamp;
		$jsonObject["errorTimestamp"] = $this->_errorTimestamp;

		//Convert the array to a JSON string and return it
		return json_encode($jsonObject);
	}	

	//********************************* Private Methods *************************************
	/**
	 * Sets the processing status of the Creative and sets the associated timestamp.
	 * 
	 * The options are the static members: QUEUED, PROCESSING, FINISHED
	 * 
	 * If a timestamp for the passed status already exists, this function will overwrite it
	 * with the new current time.
	 * 
	 * If the provided status is empty or does not exist, an exception is thrown 
	 * 
	 * The ERROR status cannot be set with this function. setError(...) should be used.
	 * 
	 * @param 	string 		$creativeStatus 	Creative status has defined by static members: QUEUED, PROCESSING, FINISHED
	 * @return 	boolean		True on success and NULL on failure
	 */
	private function setStatus($creativeStatus) {

		//If the new status is an empty string, return NULL
		if ($creativeStatus == "") {return NULL;}

		//Determine the timestamp field to change based on the status.
		$timestampField = "";
		switch ($creativeStatus) {
		
			case $this::READY: $timestampField = "CRV_readyTimestamp"; break;
			case $this::QUEUED: $timestampField = "CRV_queuedTimestamp"; break;
			case $this::PROCESSING: $timestampField = "CRV_processingTimestamp"; break;
			case $this::FINISHED: $timestampField = "CRV_finishedTimestamp"; break;
			
			//If the passed status did not match an official status, return NULL
			default: return NULL; 
		}

		//Update the status in the database
		$setStatusQuery = "UPDATE creatives 
						   SET CRV_status = '" . $creativeStatus . "', " .
							   $timestampField . " = CURRENT_TIMESTAMP 
						   WHERE CRV_id = " . $this->_id;
		ASRDatabase::executeQuery($setStatusQuery);

		//Set the new status in the instance
		$this->_status = $creativeStatus;
		
		//Query the database for the new timestamp. This is to prevent localization errors.
		//Load all three timestamps to simplify code
		$getCreativeQuery = "SELECT * FROM creatives WHERE CRV_id = " . $this->_id;
		$creativeResult = ASRDatabase::executeQuery($getCreativeQuery);
		$creativeDetails = $creativeResult->fetch_assoc();
		$this->_readyTimestamp = strtotime($creativeDetails["CRV_readyTimestamp"]);
		$this->_queuedTimestamp = strtotime($creativeDetails["CRV_queuedTimestamp"]);
		$this->_processingTimestamp = strtotime($creativeDetails["CRV_processingTimestamp"]);
		$this->_finishedTimestamp = strtotime($creativeDetails["CRV_finishedTimestamp"]);

		//Return success
		return TRUE;
	}


	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* @return int	ID of the Creative
	*/
	public function id() {
		return $this->_id;
	}

	/**
	* @return string	UUID of the Creative
	*/
	public function uuid() {
		return $this->_uuid;
	}

	/**
	* @return string	Image filename of the Creative
	*/
	public function imageFilename() {
		return $this->_imageFilename;
	}

	/**
	* @return string	Full URL including protocol to the Creative image
	*/
	public function imageURL() {
		if ($this->_imageFilename == "") {return "";}
		return $this::CREATIVEURLPATH . ASRProperties::containerForCreativeImages() . "/" . $this->_imageFilename;
	}

	/**
	* @return int	Width of the creative image in pixels
	*/
	public function width() {
		return $this->_width;
	}

	/**
	* @return int	Height of the creative image in pixels
	*/
	public function height() {
		return $this->_height;
	}

	/**
	* @return int	Priority of creative image in relation to creatives of same dimensions
	*/
	public function priority() {
		return $this->_priority;
	}

	/**
	* @return string	The tag script used to generate the Creative image. (Can be empty string)
	*/
	public function tagScript() {
		return $this->_tagScript;
	}

	/**
	* @return string	Filename of tag page used to generate the Creative image. (Can be empty string)
	*/
	public function tagPageFilename() {
		return $this->_tagPageFilename;
	}

	/**
	* @return string	Full URL including protocol to the Creative tag page
	*/
	public function tagPageURL() {
		if ($this->_tagPageFilename == "") {return "";}
		return $this::CREATIVEURLPATH . ASRProperties::containerForTagPages() . "/" . $this->_tagPageFilename;
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
	* @return boolean	TRUE if an error occurred during processing and no more attempts will be made, FALSE otherwise
	*/
	public function finalError() {
		return $this->_finalError;
	}

	/**
	* @return int	UNIX Timestamp the Creative was inserted into the database
	*/
	public function createdTimestamp() {
		return $this->_createdTimestamp;
	}

	/**
	* @return int	UNIX Timestamp the Creative status was set to READY
	*/
	public function readyTimestamp() {
		return $this->_readyTimestamp;
	}

	/**
	* @return int	UNIX Timestamp the Creative status was set to QUEUED
	*/
	public function queuedTimestamp() {
		return $this->_queuedTimestamp;
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



