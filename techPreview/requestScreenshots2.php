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

use AdShotRunner\Campaigns\Campaign;
use AdShotRunner\Campaigns\Creative;

//Verify the correct information was sent
if ((!$_POST['customerName']) || ($_POST['customerName'] == "")) {
	echo createJSONResponse(false, "No customer name passed."); return;
}

if (!$_POST['backgroundID']) {
	echo createJSONResponse(false, "No background ID passed."); return;
}

if ((!$_POST['adShots']) || (count($_POST['adShots']) == 0)) {
	echo createJSONResponse(false, "No AdShots passed."); return;
}

//Set the Creative priorities
foreach ($_POST['creativePriorities'] as $creativeID => $priority) {
	$currentCreative = Creative::getCreative($creativeID);
	$success = $currentCreative->setPriority((int) $priority);
}

//Create the new Campaign
$newCampaign = Campaign::create($_POST['customerName'], $_POST['backgroundID']);

//Add the AdShots
foreach ($_POST['adShots'] as $currentAdShotJSON) {
	$newAdShot = $newCampaign->createAdShotFromJSON($currentAdShotJSON);
	if ($newAdShot == NULL) {echo createJSONResponse(false, "Could not create AdShot."); return;}
}

//Mark the Campaign ready for processing
$newCampaign->readyForProcessing();

//Return the UUID to the server
echo createJSONResponse(true, "", ["uuid" => $newCampaign->uuid()]); return;


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
