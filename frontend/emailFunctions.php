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
require_once('systemSetup.php');

use Aws\Common\Aws;

$response = sendAdShotRunnerEmail('johann@dangerouspenguins.com', 'jbmk@mailinator.com', 
								 'Dangerous Penguins Text Test', 'test text body', 'test html body');

//$response = sendAdShotRunnerNotification('Menu Grabber Event Occurred', 'This is the menu grabber event description');
print_r($response);


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
