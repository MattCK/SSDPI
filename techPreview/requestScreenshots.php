<?PHP
/**
* Requests screenshots
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

if (!$_POST['jobID']) {
	echo createJSONResponse(false, "No Job ID passed."); return;
}

if (!$_POST['tagImages']) {
	echo createJSONResponse(false, "No tags passed."); return;
}

if (!$_POST['pages']) {
	echo createJSONResponse(false, "No pages passed."); return;
}

//Create the final object of data to turn into JSON
$screenshotRequestObject = ['jobID' => $_POST['jobID'], 
							'tagImages' => $_POST['tagImages'],
							'pages' => []];

//Add the pages to the final request object
foreach ($_POST['pages'] as $currentID => $currentPage) {
	$pageInfo = ['url' => $currentPage];
	$pageInfo['findStory'] = ($_POST['findStory'][$currentID] && ($_POST['findStory'][$currentID] == 1)) ? 1 : 0;
	$pageInfo['onlyScreenshot'] = ($_POST['onlyScreenshot'][$currentID]) ? 1 : 0;
	$screenshotRequestObject['pages'][] = $pageInfo;
}

//Create the queue request
MessageQueueClient::sendMessage(MessageQueueClient::SCREENSHOTREQUESTS, json_encode($screenshotRequestObject));

//Store the job status
$jobStatus = ['jobID' => $_POST['jobID'], 
			  'queued' => true];
file_put_contents(RESTRICTEDPATH . 'temporaryFiles/' . $_POST['jobID'], json_encode($jobStatus));
FileStorageClient::saveFile(FileStorageClient::CAMPAIGNJOBS, RESTRICTEDPATH . 'temporaryFiles/' . $_POST['jobID'], $_POST['jobID']);

echo createJSONResponse(true, "", json_encode($screenshotRequestObject)); return;
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
