<?PHP
/**
* Returns a menu for the passed domain
*
* @package AdShotRunner
*/
/**
* File to define all the system paths and the tournament data
*/
require_once('systemSetup.php');

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');

use AdShotRunner\Menu\MenuGrabber;
use AdShotRunner\Utilities\WebPageCommunicator;

//Check to see if a domain was passed
if (!$_REQUEST['domain']) {echo createJSONResponse(false, 'No domain was passed.'); return;}

//Get the menus
$domainMenuGrabber = new MenuGrabber();
$domainMenu = $domainMenuGrabber->getBestDomainMenu($_REQUEST['domain']);

//If a menu was returned, return it
//Store the domain in the session variable so it can be shown to the user
//on the next campaign
if (count($domainMenu) > 0) {
	$_SESSION['lastCampaignDomain'] = $_REQUEST['domain'];
	echo createJSONResponse(true, "Domain found", $domainMenu);
}

//Otherwise check to see if the domain is bad
else {

	//Try to grab the webpage
	$webCommunicator = new WebPageCommunicator();
	$domainResponse = $webCommunicator->getURLResponse($_REQUEST['domain']);

	//If no response was returned, return the error
	if ($domainResponse == "") {echo createJSONResponse(false, 'Website unreachable. Check the domain URL.'); return;}

	//Otherwise, the system couldn't find any menu. Simply return an empty array.
	else {echo createJSONResponse(true, "Menu not found.", array());}
}

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
