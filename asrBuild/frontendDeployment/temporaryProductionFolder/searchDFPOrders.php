<?PHP
/**
* Returns the orders with an exact order ID or partial order name, advertiser name, 
* or agency name that matches the passed search term.
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
use AdShotRunner\DFP\DFPCommunicator;

//Check to see if a search term was passed
if (!$_REQUEST['searchTerm']) {echo createJSONResponse(false, 'No search term was passed.'); return;}

//Create the DFP Communicator
$dfpCommunicator = DFPCommunicator::create(ASRProperties::dfpClientID(), 
										   ASRProperties::dfpClientSecret(), 
										   ASRProperties::dfpRefreshToken(), 
										   USERDFPNETWORKCODE, 
										   ASRProperties::dfpApplicationName());

$orders = $dfpCommunicator->searchOrders($_REQUEST['searchTerm']);
echo createJSONResponse(true, null, $orders);

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
