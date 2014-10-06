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
<<<<<<< HEAD
require_once('systemSetup.php');

use Aws\Common\Aws;

$response = sendAdShotRunnerEmail('johann@dangerouspenguins.com', 'jbmk@mailinator.com', 
								 'Dangerous Penguins Text Test', 'test text body', 'test html body');

//$response = sendAdShotRunnerNotification('Menu Grabber Event Occurred', 'This is the menu grabber event description');
=======
require_once('pathDefinitions.php');

/**
* File with functions for getting navigation information and building navigation interfaces
*/
require_once(THIRDPARTYPATH . 'aws/aws-autoloader.php');

use Aws\Common\Aws;

//$response = sendAdShotRunnerEmail('johann@dangerouspenguins.com', 'jbmk@mailinator.com', 
//								 'Dangerous Penguins Text Test', 'test text body', 'test html body');

$response = sendAdShotRunnerNotification('Menu Grabber Event Occurred', 'This is the menu grabber event description');
>>>>>>> 1e8d904b282d87ad3f026e89d80c7e654adcd9b0
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
<<<<<<< HEAD
	$aws = getAWSFactory();
=======
	$aws = Aws::factory(RESTRICTEDPATH . 'awsSESProfile.php');
>>>>>>> 1e8d904b282d87ad3f026e89d80c7e654adcd9b0
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
<<<<<<< HEAD
	$aws = getAWSFactory();
=======
	$aws = Aws::factory(RESTRICTEDPATH . 'awsSESProfile.php');
>>>>>>> 1e8d904b282d87ad3f026e89d80c7e654adcd9b0
	$snsHandler = $aws->get('sns');

	$result = $snsHandler->publish(array(
	    'TargetArn' => 'arn:aws:sns:us-east-1:469658404108:MenuGrabberEvents',
	    'Message' => $message,
        'Subject' => $subject
	));

		
	//Return response status.
	return $result;
}
