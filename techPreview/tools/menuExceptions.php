<?PHP
/**
* Checks menu exceptions
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

use AdShotRunner\Menu\MenuGrabber;

//Check to see if a domain was passed
//$domains = ['cnn.com', 'nytimes.com', 'chicagotribune.com'];
//$domains = preg_split('/\s+/', $_POST['domains']);

//if (!$domains && (!is_array($domains))) {echo createJSONResponse(false, 'No domains were passed.'); return;}

//Get the menus
$domains = ["nytimes.com", "omaha.com"];
$domainMenuGrabber = new MenuGrabber();
//$domainMenuGrabber->deleteManyDomains($domains);
$domainMenus = $domainMenuGrabber->getDomainMenus($domains);

print_r($domainMenus);
exit();

//Let's go ahead and format the data for now
$domainMenuLists = [];
foreach ($domainMenus as $currentDomain => $currentMenus) {

	$menuHTML = "";
	//$topMenus = array_slice($currentMenus, 0, 3);
	$topMenus = $currentMenus;
	if (count($topMenus) > 0) {
		foreach ($topMenus as $menu) {
			$menuHTML .= $menu['score'] . ": ";
			foreach ($menu['items'] as $menuItem) {
				$menuHTML .= $menuItem['label'] . " - ";
			}
			$menuHTML .= "<br>\n";
		}
	}
	$domainMenuLists[] = ["domain" => $currentDomain, "menus" => $menuHTML];
}

//print_r($domainMenuLists);

echo createJSONResponse(true, "Menus found", $domainMenuLists);

exit();

//If a menu was returned, return it
if (count($domainMenu) > 0) {echo createJSONResponse(true, "Domain found", $domainMenu);}

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
