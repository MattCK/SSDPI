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



//Call the correct function based on the request sent
switch($_REQUEST['request']) {
	case 'sendPVWorkbooksTable': echo sendPVWorkbooksTable(); break;
	case 'sendTimelineTemplatesTable': echo sendTimelineTemplatesTable(); break;
	case 'uploadPVWorkbook': echo uploadPVWorkbook(); break;

	case 'sendDocumentTable': echo sendDocumentInsertsTable(); break;
	case 'uploadDocument': echo uploadDocumentInsert(); break;
	case 'replaceFile': echo replaceDocumentInsert(); break;
	case 'archiveDocument': echo archiveDocumentInsert(); break;
}

/**
* Sends the PV workbooks HTML table row.
*
* @return string  		HTML string of PV workbook table row
*/
function getPVWorkbooksTable() {
	return buildTableRows(getPVWorkbooksTableData());
}
function sendPVWorkbooksTable() {
	return createJSONResponse(true, NULL, getPVWorkbooksTable());
}

/**
* Saves an uploaded PV workbook in the database.
*
* On success, this function saves an uploaded PV workbook in the database with the information passed to it through $_REQUEST.  It then responds with a sucessful
* message and the data for the updated PV workbooks table. 
*
* On failure, it responds with a failure message.
*
* This function re-validates all the information passed as already should have been done through javascript.
*
* @return string  		JSON response string
*/
function uploadPVWorkbook() {
	
	//Confirm the file extension
	if (Document::getFilenameExtension($_FILES['pvWorkbookFile']['name']) != 'xls') {
		return createJSONResponse(false, 'The file must be in Excel Spreadsheet Workbook format (.xls). Please choose another file.', NULL, 'pvWorkbookFile');
	}
	
	//Verify the file was uploaded
	if (!is_uploaded_file($_FILES['pvWorkbookFile']['tmp_name'])) {
		return createJSONResponse(false, 'Unable to verify the file\'s source.');
	}
	
	//Verify there were no errors
	if ($_FILES['pvWorkbookFile']['error'] != 0) {
		return createJSONResponse(false, 'File did not upload correctly. Please try again. Error code: ' . $_FILES['pvWorkbookFile']['error']);
	}
	
	//Insert the file into the database and store it in the system
	$newDocumentID = PVWorkbook::insertDocument('PVWorkbook', $_FILES['pvWorkbookFile']['name'], 
												$_FILES['pvWorkbookFile']['tmp_name'], 0);
	
	//Send successful JSON message and the new table. Since this should have been done in an iframe, set the content type header to xml.
	return createJSONResponse(true, NULL, getPVWorkbooksTable(), "PVWorkbook" . $newDocumentID);
}

/**
* Sends the documents HTML table rows according to the class type, sortID, and sortOrder passed through $_REQUEST.
*
* @return string  		HTML string of document table rows
*/
function getDocumentInsertsTable() {
	return getDocumentInsertsTableData($_REQUEST['sortID'], $_REQUEST['sortOrder']);
}
function sendDocumentInsertsTable() {
	return createJSONResponse(true, NULL, getDocumentInsertsTable());
}

/**
* Stores an uploaded document and places its information into the database.
*
* On success, this function stores an uploaded document and places its information into the database with the information passed to it through $_REQUEST.  
* It then responds with a sucessful message and the data for the updated documents table. 
*
* On failure, it responds with a failure message.
*
* @return string  		JSON response string
*/
function uploadDocumentInsert() {

	//Insert the document into the system
	$response = DocumentUtilities::uploadDocument('DocumentInsert', $_REQUEST['documentInsertName'], 
												 $_FILES['documentInsertFile'], NULL, 
												 $_REQUEST['documentInsertComments'], $_REQUEST['documentInsertType']);
		
	//Send respective JSON message.
	if ($response['success']) {return createJSONResponse(true, NULL, getDocumentInsertsTable(), "DocumentInsert" . $response['documentID']);}
	else {return createJSONResponse(false, $response['errorMessage'], NULL, $response['focusElementID']);}
}

/**
* Replaces the document file in the system.
*
* On success, this function replaces the document's file with the uploaded one.
* It then responds with a sucessful message and the data for the updated documents table. 
*
* On failure, it responds with a failure message.
*
* @return string  		JSON response string
*/
function replaceDocumentInsert() {

	//Execute the file replace
	$response = DocumentUtilities::replaceFile('DocumentInsert', $_REQUEST['documentID'], $_FILES["documentInsert" . $_REQUEST['documentID'] . 'ReplaceFile']);
	
	//Send respective JSON message.
	if ($response['success']) {return createJSONResponse(true, NULL, getDocumentInsertsTable(), "DocumentInsert" . $response['documentID']);}
	else {return createJSONResponse(false, $response['errorMessage']);}
}

/**
* Archives a document in the database.
*
* On success, this function archives a document in the database with the information passed to it through $_REQUEST. 
* It then responds with a sucessful message and the data for the updated documents table. 
*
* On failure, it responds with a failure message.
*
* @return string  		JSON response string
*/
function archiveDocumentInsert() {
	
	//Execute the file archive
	$response = DocumentUtilities::archiveDocument('DocumentInsert', $_REQUEST['documentID']);
	
	//Send respective JSON message.
	if ($response['success']) {return createJSONResponse(true, NULL, getDocumentInsertsTable());}
	else {return createJSONResponse(false, $response['errorMessage']);}
}

?>
