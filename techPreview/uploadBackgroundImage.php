<?PHP

/**
* Uploads the background image to storage, saves it in the database, and sets the user's default background to it
*
* @package AdShotRunner
*/
/**
* File to define all the system paths
*/
require_once('systemSetup.php');

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');

use AdShotRunner\System\ASRProperties;
use AdShotRunner\PowerPoint\PowerPointBackground;
use AdShotRunner\Users\User;

//Verify the information was passed
if (!$_POST['backgroundTitle']) {
	echo createJSONResponse(false, "Enter a name for the background image"); return;
}

if (!$_POST['backgroundFontColor']) {
	echo createJSONResponse(false, "No font color passed"); return;
}

if (!$_FILES['backgroundImage']) {
	echo createJSONResponse(false, "Choose a background image file"); return;
}

//Store and save the image and its information
$newBackground = PowerPointBackground::create(	$_POST['backgroundTitle'], 
												$_POST['backgroundFontColor'], 
												$_FILES['backgroundImage']['name'], 
												$_FILES['backgroundImage']['tmp_name'], 
												USERID);


//Set the new background as the current user's default
$currentUser = User::getUser(USERID);
$currentUser->setPowerPointBackgroundID($newBackground->getID());
User::update($currentUser);

//Put the new background info into an array to return
$newBackgroundInfo = 	[
							"id" => $newBackground->getID(),
							"title" => $newBackground->getTitle(),
							"fontColor" => $newBackground->getFontColor(),
							"filename" => $newBackground->getFilename(),
							"thumbnailFilename" => $newBackground->getThumbnailFilename()
						];



echo createJSONResponse(true, "", $newBackgroundInfo); return;


//echo createJSONResponse(true, "Menu not found.", array());

/**
* Creates a standard JSON response object to return to the client.
*
* This function can be used to create a standardized JSON object to return to the QDRO browser client. It returns an object consisting of four parts/keys.
*
* - 'success': Returns either 1 for TRUE or 0 for FALSE.
* - 'message': Optional message. This is generally used for sending a specific error message.
* - 'data': Optional data to pass. This can be anything from an HTML string to an array of data.
* - 'focus': Optional focus name. This generally refers either to an input element ot place the focus on or a row in a table to highlight.
*
* @param bool $success  		Flags whether or not the message should be marked successful. TRUE for successful and FALSE otherwise.
* @param string $message  		Optional message to send to the client.
* @param mixed $data  			Data to send to the client.
* @param string $focus  		Optional name of element to focus upon.
* @return string  				JSON object string to pass to client.
*/
function createJSONResponse($success, $message = '', $data = NULL, $focus = '') {

	//Set the successByte (either 1 or 0)
	$successByte = ($success) ? 1 : 0;
	
	//Create the object
	$responseArray = array(
		'success' => $successByte,
		'message' => $message,
		'data' => $data,
		'focus' => $focus
	);
	
	//Convert it to JSON and return it
	return json_encode($responseArray);
}




?>
