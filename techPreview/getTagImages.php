<?PHP
/**
* Saves passed tags in HTML files for image processing and notifies the SSS through the message queue
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

use AdShotRunner\Utilities\FileStorageClient;
use AdShotRunner\Utilities\MessageQueueClient;
use AdShotRunner\Utilities\WebPageCommunicator;

if (!$_POST['tags']) {echo "{}"; return;}

$filePages = [];
foreach ($_POST['tags'] as $currentID => $currentTag) {

	//If the tag is an image tag, simply store it
	if (substr(trim(strtolower($currentTag)), 0, 4) == '<img') {

		//Get the src from the img tag
		$xpath = new DOMXPath(@DOMDocument::loadHTML($currentTag));
		$imageSrc = $xpath->evaluate("string(//img/@src)");

		//Get the image 
		$webCommunicator = new WebPageCommunicator();
		$tagImage = $webCommunicator->getURLResponse($imageSrc);

		//Conver the image to a png
		$fileName = $currentID . ".png";
		imagepng(imagecreatefromstring($tagImage), RESTRICTEDPATH . 'temporaryFiles/' . $fileName);

		//Store the image
		FileStorageClient::saveFile(FileStorageClient::TAGIMAGESCONTAINER, RESTRICTEDPATH . 'temporaryFiles/' . $fileName, $fileName);
		unlink(RESTRICTEDPATH . 'temporaryFiles/' . $fileName);
	}

	//Otherwise, store the tag in an html file and add it to the tag list for sending to the sss for processing
	else {
		$fileName = USERID . "-" . $currentID . ".html";
		$tagPageHTML = "<style>body { margin: 0px; padding:0px;} #adTagContainer {display: table;}</style><div id='adTagContainer'>" . $currentTag . "</div>";
		file_put_contents(RESTRICTEDPATH . 'temporaryFiles/' . $fileName, $tagPageHTML);
		FileStorageClient::saveFile(FileStorageClient::TAGPAGESCONTAINER, RESTRICTEDPATH . 'temporaryFiles/' . $fileName, $fileName);
		unlink(RESTRICTEDPATH . 'temporaryFiles/' . $fileName);
		$filePages[$currentID] = $fileName;
	}
}

//Create the queue request and add it
if (count($filePages) > 0) {
	$requestObject = $filePages;
	MessageQueueClient::sendMessage(MessageQueueClient::TAGIMAGEREQUESTS, json_encode($requestObject));
}

echo "{}"; return;

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


//SAVE - Firefox Style String
//$styleString = "<style>body { margin: 75px 0 0 425px; padding:0px;}</style>";


?>
