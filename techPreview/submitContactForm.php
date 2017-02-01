<?PHP
/**
* Submits contact form information to the notification service
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

use AdShotRunner\System\ASRProperties;
use AdShotRunner\Database\ASRDatabase;
use AdShotRunner\Utilities\EmailClient;

//Make sure all the necessary information has been passed
if (($_POST["contactName"]) && ($_POST["contactEmail"]) && ($_POST["contactType"]) && 
	($_POST["contactProblem"]) && ($_POST["contactDescription"])) {

	//Format the data for datbase insertion
	$userID = ($_POST["contactUserID"]) ? (int)$_POST["contactUserID"] : 0;
	$jobID = ASRDatabase::escape($_POST["contactJobID"]);
	$name = ASRDatabase::escape($_POST["contactName"]);
	$email = ASRDatabase::escape($_POST["contactEmail"]);
	$type = ASRDatabase::escape($_POST["contactType"]);
	$problem = ASRDatabase::escape($_POST["contactProblem"]);
	$description = ASRDatabase::escape($_POST["contactDescription"]);

	//Store the message in the database
	ASRDatabase::executeQuery("INSERT INTO contactMessages (CNM_USR_id, 
												CNM_jobID,
												CNM_name,
												CNM_email,
												CNM_type,
												CNM_problem,
												CNM_description) 
				   VALUES ($userID,
				   		  '$jobID',
				   		  '$name',
				   		  '$email',
				   		  '$type',
				   		  '$problem',
				   		  '$description')");

	//Create the email subject
	$messageType = ($_POST["contactType"] == "IDEA") ? "Idea" :
													   "Issue - " . $_POST["contactProblem"];
	$emailSubject = "ASR Contact Message: " . $messageType . " (" . $_POST["contactName"] . ")";

	//Create the email body
	$emailBody =  "User ID: " . $_POST["contactUserID"] . "\n";
	$emailBody .= "Job ID: " . $_POST["contactJobID"] . "\n";
	$emailBody .= "Name: " . $_POST["contactName"] . "\n";
	$emailBody .= "Email: " . $_POST["contactEmail"] . "\n";
	$emailBody .= "Type: " . $_POST["contactType"] . "\n";
	$emailBody .= "Problem: " . $_POST["contactProblem"] . "\n";
	$emailBody .= "Description: " . $_POST["contactDescription"] . "\n";

	//Send the email
	EmailClient::sendEmail(ASRProperties::emailAddressContact(), ASRProperties::emailAddressInfo(), 
							$emailSubject, $emailBody);

	//Return a success. 
	echo createJSONResponse(true);

}

//Otherwise, ask the user to fill out all fields
else {
	echo createJSONResponse(false, "Please fill out all the fields.");
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
