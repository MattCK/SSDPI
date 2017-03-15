<?PHP
/**
* Returns a line items and creatives for order
*
* @package AdShotRunner
*/
/**
* File to define all the system paths and the tournament data
*/
require_once('../systemSetup.php');

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');

use AdShotRunner\System\ASRProperties;
use AdShotRunner\DFP\DFPCommunicator;

//Check to see if an order was passed
if (!$_REQUEST['orderID']) {echo createJSONResponse(false, 'No order ID was passed.'); return;}


$dfpCommunicator = DFPCommunicator::create(ASRProperties::dfpClientID(), 
										   ASRProperties::dfpClientSecret(), 
										   ASRProperties::dfpRefreshToken(), 
										   USERDFPNETWORKCODE, 
										   ASRProperties::dfpApplicationName());

$dfpCommunicator->getLineItemsAndCreative($_REQUEST['orderID'], $lineItems, $creatives);

foreach($lineItems as $lineItemID => $lineItem) {
	echo "$lineItemID : " . $lineItem["name"] . " - " . count($lineItem["creatives"]) . " creatives - " . $lineItem["status"] . " <br/>";
	if (count($lineItem["creatives"]) < 1116) {
		foreach($lineItem["creatives"] as $creativeID) {
			$currentCreative = $creatives[$creativeID];
			echo "&nbsp;&nbsp; $creativeID (" . $currentCreative["type"] . ") - " . 
					$currentCreative["width"] . "x" . $currentCreative["height"] . ": " .
					"<a href='" . $currentCreative["previewURL"] . "'>Preview URL</a><br>";
		}
	}
	echo "<br/>";
}

exit;

echo createJSONResponse(true, "Order Found", ["lineItems" => $lineItems, "creatives" => $creatives]);

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
