<?PHP
/**
* Functions to send and interact with emails
*
* @package AdShotRunner
* @subpackage Functions
*/
/**
* File to setup all the path definitions
*/
require_once('../systemSetup.php');

use AdShotRunner\Utilities\EmailClient;
use AdShotRunner\Utilities\MessageQueueClient;
use AdShotRunner\Utilities\NotificationClient;

header("Content-Type: text/plain");

/*for ($i = 0; $i < 10; ++$i) {
	$response = EmailClient::sendEmail(EmailClient::SCREENSHOTADDRESS, 'juicio@dangerouspenguins.com', 
									 'Dangerous Penguins Speed Test ' . $i, "Speed Test: $i", "Speed test: <b>$i</b>");
	print_r($response); echo "\n\n<br><br>";
}*/
//$response = sendAdShotRunnerNotification('Menu Grabber Event Occurred', 'This is the menu grabber event description');


//$response = sendAdShotRunnerEmail('johann@dangerouspenguins.com', 'jbmk@mailinator.com', 
//								 'Dangerous Penguins Text Test', 'test text body', 'test html body');

//$response = NotificationClient::sendNotice(NotificationClient::FRONTEND, 'Menu Grabber Event Occurred', 'This is the menu grabber event description');

echo "Sending message...";
$response = MessageQueueClient::sendMessage(MessageQueueClient::TAGIMAGEREQUESTS, 'This is the tag image request message');
echo "Done!\n";

echo "Polling for messages...";
$messages = array();
while (count($messages) < 1) {
	echo " polling...";
	$messages = MessageQueueClient::getMessages(MessageQueueClient::TAGIMAGEREQUESTS);
}

//Clean up the output
/*$cleanMessages = array();
foreach ($messages as $curMessage) {
	$cleanMessages[$curMessage['MessageId']] = $curMessage['Body'];
}

//Clean up the output
$receiptMessages = array();
foreach ($messages as $curMessage) {
	$receiptMessages[$curMessage['ReceiptHandle']] = $curMessage['Body'];
}*/

echo "Done!\n";

print_r($messages);

//echo "\n\nDeleting one message...";
reset($messages);
$receiptID = key($messages);
//$response = MessageQueueClient::deleteMessage(MessageQueueClient::TAGIMAGEREQUESTS, $receiptID);
echo "Done!\n";
echo "Receipt: $receiptID \n\n";

print_r($response);
//var_export($response->getKeys());

/**
* Sends an email through the AdShotRunner system
*
* @param string	 	$fromAddress  			Originating email address (From: field).
* @param mixed	 	$toAddress  			Recipient email address(es) (To: field). String or array of strings.
* @param string	 	$subject  				Email subject
* @param string	 	$textBody  				Plain text email body
* @param string	 	$htmlBody  				HTML text email body
* @return boolean  		 					TRUE on success and FALSE on failure.
*/
function sendAdShotRunnerEmail($fromAddress, $toAddress, $subject, $textBody, $htmlBody = null) {
	
	//Create the SES handler object
	$aws = getAWSFactory();
	$sesHandler = $aws->get('ses');

	$result = $sesHandler->sendEmail(array(
	    // Source is required
	    'Source' => $fromAddress,
	    // Destination is required
	    'Destination' => array(
	        'ToAddresses' => array($toAddress)
	    ),
	    // Message is required
	    'Message' => array(
	        // Subject is required
	        'Subject' => array(
	            // Data is required
	            'Data' => $subject,
	            'Charset' => 'utf-8',
	        ),
	        // Body is required
	        'Body' => array(
	            'Text' => array(
	                // Data is required
	                'Data' => $textBody,
	                'Charset' => 'utf-8',
	            ),
	            'Html' => array(
	                // Data is required
	                'Data' => $htmlBody,
	                'Charset' => 'utf-8',
	            ),
	        ),
	    ),
	));
		
	//Return response status.
	return $result;
}

/**
* Sends a notification through the AdShotRunner system
*
* @param string	 	$topic  				Topic to publish message to
* @param string	 	$subject  				Message subject
* @param string	 	$message  				Message body
* @return boolean  		 					TRUE on success and FALSE on failure.
*/
function sendAdShotRunnerNotification($subject, $message) {
	
	//Create the SES handler object
	$aws = getAWSFactory();
	$snsHandler = $aws->get('sns');

	$result = $snsHandler->publish(array(
	    'TargetArn' => 'arn:aws:sns:us-east-1:469658404108:MenuGrabberEvents',
	    'Message' => $message,
        'Subject' => $subject
	));

		
	//Return response status.
	return $result;
}
