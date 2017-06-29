<?PHP
/**
* Contains the class for creating and retrieving PowerPoint backgrounds
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\PowerPoint;

use AdShotRunner\System\ASRProperties;
use AdShotRunner\Database\ASRDatabase;
use AdShotRunner\Utilities\FileStorageClient;

/**
* The PowerPointBackground controls the flow of information between the system and the database 
* concerning PowerPoint background information and uploads the images to storage. 
*/
class PowerPointBackground {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------
	//Dimensions of PowerPoint background thumbnail	
	const THUMBNAILMAXWIDTH = 200;
	const THUMBNAILMAXHEIGHT = 80;

	//URL path for images
	const IMAGEURLPATH = "https://s3.amazonaws.com/";

	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Methods ---------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	/**
	* Returns an instance of the PowerPointBackground with the passed ID.
	*
	* @param 	int 	$powerPointBackgroundID  	ID of the PowerPoint background to retrieve
	* @return 	PowerPointBackground				Instance of the PowerPointBackground to be retrieved. NULL on failure.
	*/
	static public function getPowerPointBackground($powerPointBackgroundID) {
		
		//Verify the ID and that it's not less than 1. If not, return NULL.
		if ((!$powerPointBackgroundID) || (!is_numeric($powerPointBackgroundID)) || ($powerPointBackgroundID < 1)) {return NULL;}
		
		//Get the info from the powerPointBackgrounds table
		$getBackgroundQuery = "SELECT * FROM powerPointBackgrounds WHERE PPB_id = $powerPointBackgroundID";
		$getBackgroundResult = ASRDatabase::executeQuery($getBackgroundQuery);
		$backgroundInfo = $getBackgroundResult->fetch_assoc();;

		//If no data was returned, return NULL.
		if (!$backgroundInfo) {return NULL;}
		
		//Create an instance of the class to return with the retrieved information
		$background = new PowerPointBackground();
		$background->_id = $powerPointBackgroundID;
		$background->_name = $backgroundInfo['PPB_name'];
		$background->_fontColor = $backgroundInfo['PPB_fontColor'];
		$background->_originalFilename = $backgroundInfo['PPB_originalFilename'];
		$background->_filename = $backgroundInfo['PPB_filename'];
		$background->_thumbnailFilename = $backgroundInfo['PPB_thumbnailFilename'];
		$background->_timestamp = strtotime($backgroundInfo['PPB_timestamp']);
		$background->_archived = $backgroundInfo['PPB_archived'];
		
		//Return the background instance
		return $background;
	}

	/**
	* Creates a PowerPointBackground with the passed background information and stores the image
	*
	* On success, the a PowerPointBackground is returned. On failure, NULL is returned.
	*
	* @param string 	$name   				Name of the PowerPoint background
	* @param string 	$fontColor 				Font color to be used witht the background
	* @param string 	$originalImageFilename  The original background image filename
	* @param string 	$imageFilename			Filename of the background image with associated path
	* @param int 	 	$userID 				ID of the user associated with the background
	* @return PowerPointBackground  			PowerPointBackground with the new information. NULL on failure.
	*/
	static public function create($name, $fontColor, $originalImageFilename, $imageFilename, $userID) {
		
		//Verify all parameters were passed.
		if ((!$name) || (!$fontColor) || (!$originalImageFilename) || 
			(!$imageFilename) || (!$userID)) {return NULL;}

		//Get the filename extension and return NULL if unavailable
		$imageType = self::getFilenameExtension($originalImageFilename);
		if (!$imageType) {return NULL;}

		//If the image extension type is not jpg, png, or bmp, return NULL
		if (($imageType != "jpg") && ($imageType != "jpeg") && ($imageType != "png") && ($imageType != "bmp")) {
			return NULL;
		}

		//Create the new filename: filename + user ID + timestamp + extension
		$namePart = explode(".", $originalImageFilename);
		$newFilename = $namePart[0] . "-" . $userID . "-" . time() . "." . $imageType;

		//Save the background image
		FileStorageClient::saveFile(ASRProperties::containerForPowerPointBackgrounds(), $imageFilename, $newFilename);

		//Create the thumbnail
		$thumbnailFilename = $namePart[0] . "-" . $userID . "-" . time() . "_thumbnail.png";
		self::createPNGThumbnail($imageFilename, RESTRICTEDPATH . 'temporaryFiles/' . $thumbnailFilename);

		//Save the thumbnail and delete the local file
		FileStorageClient::saveFile(ASRProperties::containerForPowerPointBackgrounds(), 
									RESTRICTEDPATH . 'temporaryFiles/' . $thumbnailFilename, 
									"thumbnails/" . $thumbnailFilename);
		unlink(RESTRICTEDPATH . 'temporaryFiles/' . $thumbnailFilename);
		
		//Add the info to the table
		$insertBackgroundQuery = "	INSERT INTO powerPointBackgrounds (	PPB_name,
																		PPB_fontColor,
																		PPB_originalFilename,
																		PPB_filename,
																		PPB_thumbnailFilename,
																		PPB_USR_id)
								 	VALUES ('" . ASRDatabase::escape($name) . "',
											'" . ASRDatabase::escape($fontColor) . "',
											'" . ASRDatabase::escape($originalImageFilename) . "',
											'" . ASRDatabase::escape($newFilename) . "',
											'" . ASRDatabase::escape($thumbnailFilename) . "',
											'" . ASRDatabase::escape($userID) . "')";
		ASRDatabase::executeQuery($insertBackgroundQuery);

		//Return the newly created PowerPointBackground
		return self::getPowerPointBackground(ASRDatabase::lastInsertID());
	}

	/**
	* Archives the PowerPoint background in the system with the passed ID.
	*
	* Archives the PowerPoint background that corresponds to the passed ID. On success, 
	* the PowerPointBackground object is returned. On failure, NULL is returned. 
	*
	* @param 	int 	$powerPointBackgroundID  	ID of the PowerPoint background to archive
	* @return 	PowerPointBackground  				Instance of the PowerPointBackground archived
	*/
	static public function archive($powerPointBackgroundID) {
		
		//Verify and ID was passed and it's not less than 1. If not, return NULL.
		if ((!$powerPointBackgroundID) || (!is_numeric($powerPointBackgroundID)) || ($powerPointBackgroundID < 1)) {return NULL;}
		
		//Verify the PowerPoint background in the database is not archived.
		$currentBackground = PowerPointBackground::getPowerPointBackground($powerPointBackgroundID);
		if ($currentBackground == NULL) {return NULL;}
		if ($currentBackground->isArchived()) {return $currentBackground;}
				
		//Archive the background
		$archiveBackgroundQuery = "	UPDATE powerPointBackgrounds 
							 	  	SET PPB_archived = 1
							 		WHERE PPB_id = " . $powerPointBackgroundID;
		ASRDatabase::executeQuery($archiveBackgroundQuery);
		
		//Send the archived background.
		return self::getPowerPointBackground($powerPointBackgroundID);
	}

	//***************************** Private Static Methods **********************************
	/**
	* Returns the extension of the filename.
	*
	* The extension is simply the text after the last '.' . This function returns it as lowercase.
	*
	* @param string 	$filename  		Filename
	* @return string 			 		Lowercase extension of the filename.
	*/	
	static private function getFilenameExtension($filename) {return strtolower(substr(strrchr($filename, "."), 1));}

	/**
	* Creates a PNG thumbnail of the passed image and saves it to the specified file.
	*
	* The thumbnail size is defined by the class constants THUMBNAILMAXWIDTH and THUMBNAILMAXHEIGHT
	*
	* @param string 	$imageFilename  		Image file to create thumbnail for
	* @param string 	$thumbnailFilename  	File to save thumbnail to
	* @return string 			 				Lowercase extension of the filename.
	*/	
	static private function createPNGThumbnail($imageFilename, $thumbnailFilename) {
	    $imagick = new \Imagick(realpath($imageFilename));
	    $imagick->thumbnailImage(self::THUMBNAILMAXWIDTH, self::THUMBNAILMAXHEIGHT, true);
	    $imagick->setImageFormat('png');
	    $imagick->writeImage($thumbnailFilename);
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	* @var int  Unique ID of the PowerPoint background
	*/
	private $_id;

	/**
	* @var string  User defined name of the PowerPoint background
	*/
	private $_name;	

	/**
	* @var string  Six character color for the font used with the background. (i.e. FFDDEE)
	*/
	private $_fontColor;

	/**
	* @var string  The original filename of the uploaded PowerPoint background
	*/
	private $_originalFilename;

	/**
	* @var string  Filename of the PowerPoint background as it appears in storage
	*/
	private $_filename;

	/**
	* @var string  Filename of the background image thumbnail
	*/
	private $_thumbnailFilename;

	/**
	* @var int	UNIX timestamp of when the background was uploaded
	*/
	private $_timestamp;

	/**
	* @var boolean	Flags whether or not PowerPoint background has been archived. TRUE if PowerPoint background has been archived.
	*/
	private $_archived;

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Constructor set to private so only static factories can create new instances
	*/
	private function __construct() {}

	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* @return int  ID of the PowerPoint background
	*/
	public function id() {
		return $this->_id;
	}

	/**
	* @return string	User defined name of the PowerPoint background
	*/
	public function name() {
		return $this->_name;
	}

	/**
	* @return string	Font color to be used with the background
	*/
	public function fontColor() {
		return $this->_fontColor;
	}

	/**
	* @return string	Original filename associated with the background image
	*/
	public function originalFilename() {
		return $this->_originalFilename;
	}

	/**
	* @return string	Filename for the background image as it is in storage
	*/
	public function filename() {
		return $this->_filename;
	}

	/**
	* @return string	Full URL including protocol to the background image
	*/
	public function url() {
		if ($this->_filename == "") {return "";}
		return $this::IMAGEURLPATH . ASRProperties::containerForPowerPointBackgrounds() . "/" . $this->_filename;
	}

	/**
	* @return string	Filename of the background image's thumbnail
	*/
	public function thumbnailFilename() {
		return $this->_thumbnailFilename;
	}

	/**
	* @return string	Full URL including protocol to the background thumbnail image
	*/
	public function thumbnailURL() {
		if ($this->_thumbnailFilename == "") {return "";}
		return $this::IMAGEURLPATH . ASRProperties::containerForPowerPointBackgrounds() . "/thumbnails/" . $this->_thumbnailFilename;
	}

	/**
	* @return integer  Unix timestamp of when the background was uploaded
	*/
	public function timestamp() {
		return $this->_timestamp;
	}

	/**
	* @return boolean  Archive status of the PowerPoint background
	*/
	public function archived() {
		return $this->_archived;
	}


	//********************************* Private Accessors ***********************************




}