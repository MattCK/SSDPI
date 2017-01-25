<?PHP
/**
* Contains the class for uploading, inserting, archiving, and retrieving PowerPoint backgrounds
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\PowerPoint;

use AdShotRunner\Utilities\FileStorageClient;

/**
* The PowerPointBackground controls the flow of information between the system and the database 
* concerning PowerPoint background information and uploads them to storage. 
*
* The PowerPointBackground controls the flow of information between the system and the database 
* concerning PowerPoint background information and uploads them to storage. The table it 
* accesses is 'powerPointBackgrounds'. This class can be used to retrieve, upload, or archive 
* a PowerPoint background.  
*/
class PowerPointBackground {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	const THUMBNAILMAXWIDTH = 200;
	const THUMBNAILMAXHEIGHT = 200;

	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Methods ---------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	/**
	* Returns an instance of the PowerPointBackground with the passed ID.
	*
	* @param 	int 	$powerPointBackgroundID  	ID of the PowerPoint background to retrieve
	* @return 	Plan  								Instance of the PowerPointBackground to be retrieved. NULL on failure.
	*/
	static public function getPowerPointBackground($powerPointBackgroundID) {
		
		//Verify the ID and that it's not less than 1. If not, return NULL.
		if ((!$powerPointBackgroundID) || (!is_numeric($powerPointBackgroundID)) || ($powerPointBackgroundID < 1)) {return NULL;}
		
		//Get the info from the powerPointBackgrounds table
		$getBackgroundQuery = "SELECT * FROM powerPointBackgrounds WHERE PPB_id = $powerPointBackgroundID";
		$getBackgroundResult = databaseQuery($getBackgroundQuery);
		$backgroundInfo = $getBackgroundResult->fetch_assoc();;

		//If no data was returned, return NULL.
		if (!$backgroundInfo) {return NULL;}
		
		//Create an instance of the class to return with the retrieved information
		$background = new PowerPointBackground($backgroundInfo);
		$background->setID($powerPointBackgroundID);
		$background->setUploadTimestamp(strtotime($backgroundInfo['PPB_timestamp']));
		$background->setArchiveStatus($backgroundInfo['PPB_archived']);
		
		//Return the background instance
		return $background;
	}

	/**
	* Creates a PowerPointBackground with the passed background information and stores the image
	*
	* On success, the a PowerPointBackground is returned. On failure, NULL is returned.
	*
	* @param string 	$title  				Title of the PowerPoint background
	* @param string 	$fontColor 				Font color to be used witht the background
	* @param string 	$originalImageFilename  The original background image filename
	* @param string 	$imageFilename			Filename of the background image with associated path
	* @param int 	 	$userID 				ID of the user associated with the background
	* @return PowerPointBackground  			PowerPointBackground with the new information. NULL on failure.
	*/
	static public function create($title, $fontColor, $originalImageFilename, $imageFilename, $userID) {
		
		//Verify all parameters were passed.
		if ((!$title) || (!$fontColor) || (!$originalImageFilename) || 
			(!$imageFilename) || (!$userID)) {return NULL;}

		//Get the filename extension and return NULL if unavailable
		$imageType = self::getFilenameExtension($originalImageFilename);
		if (!$imageType) {return NULL;}

		//If the image extension type is not jpg, png, or bmp, return NULL
		if (($imageType != "jpg") && ($imageType != "png") && ($imageType != "bmp")) {
			return NULL;
		}

		//Create the new filename: filename + user ID + timestamp + extension
		$namePart = explode(".", $originalImageFilename);
		$newFilename = $namePart[0] . "-" . $userID . "-" . time() . "." . $imageType;

		//Save the background image
		FileStorageClient::saveFile(FileStorageClient::POWERPOINTBACKGROUNDS, $imageFilename, $newFilename);

		//Create the thumbnail
		$thumbnailFilename = $namePart[0] . "-" . $userID . "-" . time() . "_thumbnail.png";
		self::createPNGThumbnail($imageFilename, RESTRICTEDPATH . 'temporaryFiles/' . $thumbnailFilename);

		//Save the thumbnail and delete the local file
		FileStorageClient::saveFile(FileStorageClient::POWERPOINTBACKGROUNDS, 
									RESTRICTEDPATH . 'temporaryFiles/' . $thumbnailFilename, 
									"thumbnails/" . $thumbnailFilename);
		unlink(RESTRICTEDPATH . 'temporaryFiles/' . $thumbnailFilename);
		
		//Add the info to the table
		$insertBackgroundQuery = "	INSERT INTO powerPointBackgrounds (	PPB_title,
																		PPB_fontColor,
																		PPB_originalFilename,
																		PPB_filename,
																		PPB_thumbnailFilename,
																		PPB_USR_id)
								 	VALUES ('" . databaseEscape($title) . "',
											'" . databaseEscape($fontColor) . "',
											'" . databaseEscape($originalImageFilename) . "',
											'" . databaseEscape($newFilename) . "',
											'" . databaseEscape($thumbnailFilename) . "',
											'" . databaseEscape($userID) . "')";
		databaseQuery($insertBackgroundQuery);

		//Return the newly created PowerPointBackground
		return self::getPowerPointBackground(databaseLastInsertID());
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
		
		//Verify the PowerPoint background in the database is not archived. If it is, do nothing and return the PowerPointBackground.
		$currentBackground = PowerPointBackground::getPowerPointBackground($powerPointBackgroundID);
		if ($currentBackground->isArchived()) {return $currentBackground;}
				
		//Archive the background
		$archiveBackgroundQuery = "	UPDATE powerPointBackgrounds 
							 	  	SET PPB_archived = 1
							 		WHERE PPB_id = " . $powerPointBackgroundID;
		databaseQuery($archiveBackgroundQuery);
		
		//Send the archived background
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
	private $id;

	/**
	* @var string  User defined title of the PowerPoint background
	*/
	private $title;	

	/**
	* @var string  Six character color for the font used with the background. (i.e. FFDDEE)
	*/
	private $fontColor;

	/**
	* @var string  The original filename of the uploaded PowerPoint background
	*/
	private $originalFilename;

	/**
	* @var string  Filename of the PowerPoint background as it appears in storage
	*/
	private $filename;

	/**
	* @var string  Filename of the background image thumbnail
	*/
	private $thumbnailFilename;

	/**
	* @var int  ID of the user associated with the PowerPoint background
	*/
	private $userID;

	/**
	* @var int	UNIX timestamp of when the background was uploaded
	*/
	private $uploadTimestamp;

	/**
	* @var boolean	Flags whether or not PowerPoint background has been archived. TRUE if PowerPoint background has been archived.
	*/
	private $archived;

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Initializes the PowerPoint object according to the optional information passed to it.
	*
	* The constructor will create a PowerPointBackground instance without any details if no 
	* information is passed. Otherwise, it receives an associative array with the names of 
	* the table fields and their corresponding data. Any subset of this array can be passed
	* and the information will be set in the instance.
	*
	* Ex:
	* <code>
	* array (
	* 	'PPB_title' => 'My Background',
	*	'PPB_fontColor' => 'D7D7D7',
	*	...
	* )
	* <\/code>
	*
	* It will receive all the fields in the table that it has a public set accessor for. (i.e. One cannot set the id or archive status.)
	*
	* @param mixed $powerPointBackgroundInfo  Associative array with the names of the table fields and their corresponding data
	*/
	private function __construct($powerPointBackgroundInfo = NULL) {
		
		if (($powerPointBackgroundInfo) && (is_array($powerPointBackgroundInfo))) {
			$this->setTitle($powerPointBackgroundInfo['PPB_title']);
			$this->setFontColor($powerPointBackgroundInfo['PPB_fontColor']);
			$this->setOriginalFilename($powerPointBackgroundInfo['PPB_originalFilename']);
			$this->setFilename($powerPointBackgroundInfo['PPB_filename']);
			$this->setThumbnailFilename($powerPointBackgroundInfo['PPB_thumbnailFilename']);
			$this->setUserID($powerPointBackgroundInfo['PPB_USR_id']);
		}
	}

	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* Returns the ID of the PowerPoint background. NULL if an ID has not been set.
	*
	* @return int  ID of the PowerPoint background
	*/
	public function getID(){
		return $this->id;
	}

	/**
	* Returns the user defined title of the PowerPoint background
	*
	* @return string	User defined title of the PowerPoint background
	*/
	public function getTitle(){
		return $this->title;
	}
	/**
	* Sets the user defined title of the PowerPoint background
	*
	* @param string 	$newTitle  New user defined title of the PowerPoint background 
	*/
	public function setTitle($newTitle){
		$this->title = $newTitle;
	}	

	/**
	* Returns the font color to be used with the background
	*
	* @return string	Font color to be used with the background
	*/
	public function getFontColor(){
		return $this->fontColor;
	}
	/**
	* Sets the font color to be used with the background
	*
	* @param string 	$newFontColor  New font color to be used with the background 
	*/
	public function setFontColor($newFontColor){
		$this->fontColor = $newFontColor;
	}

	/**
	* Returns the original filename associated with the background image
	*
	* @return string	Original filename associated with the background image
	*/
	public function getOriginalFilename(){
		return $this->originalFilename;
	}
	/**
	* Sets the original filename associated with the background image
	*
	* @param string 	$newOriginalFilename  New original filename associated with the background image 
	*/
	public function setOriginalFilename($newOriginalFilename){
		$this->originalFilename = $newOriginalFilename;
	}

	/**
	* Returns the filename for the background image as it is in storage
	*
	* @return string	Filename for the background image as it is in storage
	*/
	public function getFilename(){
		return $this->filename;
	}
	/**
	* Sets the filename for the background image as it is in storage
	*
	* @param string 	$newFilename  New filename for the background image as it is in storage 
	*/
	public function setFilename($newFilename){
		$this->filename = $newFilename;
	}

	/**
	* Returns the filename of the background image's thumbnail
	*
	* @return string	Filename of the background image's thumbnail
	*/
	public function getThumbnailFilename(){
		return $this->thumbFilename;
	}
	/**
	* Sets the filename of the background image's thumbnail
	*
	* @param string 	$newThumbnailFilename  New filename of the background image's thumbnail 
	*/
	public function setThumbnailFilename($newThumbnailFilename){
		$this->thumbFilename = $newThumbnailFilename;
	}

	/**
	* Returns the ID of the user associated with the PowerPoint background
	*
	* @return int	ID of the user associated with the PowerPoint background
	*/
	public function getUserID(){
		return $this->userID;
	}
	/**
	* Sets the ID of the user associated with the PowerPoint background
	*
	* @param int 	$newUserID  New ID of the user associated with the PowerPoint background 
	*/
	public function setUserID($newUserID){
		$this->userID = $newUserID;
	}

	/**
	* Returns the UNIX timestamp of when the background was uploaded.
	*
	* @return integer  Unix timestamp of when the background was uploaded
	*/
	public function getUploadTimestamp(){
		return $this->uploadTimestamp;
	}

	/**
	* Returns the archive status of the PowerPoint background
	*
	* @return string  Archive status of the PowerPoint background
	*/
	public function isArchived(){
		return $this->archived;
	}


	//********************************* Private Accessors ***********************************
	/**
	* Sets the ID of the PowerPoint background
	*
	* @param int $newID  New ID for the PowerPoint background 
	*/
	private function setID($newID){
		$this->id = $newID;
	}
	
	/**
	* Sets the UNIX timestamp of when the background was uploaded.
	*
	* @param int $newTimestamp  New UNIX timestamp of when the background was uploaded 
	*/
	private function setUploadTimestamp($newTimestamp){
		$this->uploadTimestamp = $newTimestamp;
	}
	
	/**
	* Sets the the archive status of the PowerPoint background
	*
	* @param string $newArchiveStatus  New archive status of the PowerPoint background 
	*/
	private function setArchiveStatus($newArchiveStatus){
		$this->archived = $newArchiveStatus;
	}




}