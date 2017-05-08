<?PHP
/**
* Contains the class for sending emails
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\Utilities;

/**
* The EmailClient sends emails to the intended recipients from the stated email address
*/
class EmailClient {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//const SCREENSHOTADDRESS = 'screenshots@adshotrunner.com';
	//const MATTADDRESS = 'matt@adshotrunner.com';
	//const JUICIOADDRESS = 'juicio@adshotrunner.com';
	//const CONTACTFORMADDRESS = 'contactform@adshotrunner.com';
	//const ASRINFOADDRESS = 'info@adshotrunner.com';
	

	//--------------------------------------------------------------------------------------
	//---------------------------------- Static Methods ------------------------------------
	//--------------------------------------------------------------------------------------
	//***************************** Public Static Methods **********************************
	/**
	* Sends an email to the recipient via the passed from address, subject, and body
	*
	* @param string	 	$fromAddress  			Originating email address (From: field).
	* @param mixed	 	$toAddress  			Recipient email address(es) (To: field). String or array of strings.
	* @param string	 	$subject  				Email subject
	* @param string	 	$textBody  				Plain text email body
	* @param string	 	$htmlBody  				HTML text email body
	* @return boolean  		 					TRUE on success and FALSE on failure.
	*/
	static public function sendEmail($fromAddress, $toAddresses, $subject, $textBody, $htmlBody = null) {

		//Make sure a from adress was passed
		if (strlen($fromAddress) < 1) {return FALSE;}

		//If the TO addresses were not an array, make it one
		if (!is_array($toAddresses)) {$toAddresses = array($toAddresses);}

		//Make sure there is at least one TO address of length 1
		$noValidAddresses = true;
		foreach($toAddresses as $curAddress) {
			if (strlen($curAddress) > 0) {$noValidAddresses = false;}
		}
		if ($noValidAddresses) {return FALSE;}

		//Make sure a text or html body was passed
		if ((strlen($textBody) < 1) && (strlen($htmlBody) < 1)) {return FALSE;}
		
		//Create the SES handler object
		$awsFactory = getAWSFactory();
		$sesHandler = $awsFactory->get('ses');

		//Create the email info object
		$emailInfo = array(
			'Source' => $fromAddress,
			'Destination' => array('ToAddresses' => $toAddresses),
			'Message' => array()
		);

		//Add the subject if passed
		if ($subject) {
			$emailInfo['Message']['Subject'] = array('Data' => $subject, 'Charset' => 'utf-8');
		}

		//Add the text if passed
		$emailInfo['Message']['Body'] = array();
		if ($textBody) {
			$emailInfo['Message']['Body']['Text'] = array('Data' => $textBody, 'Charset' => 'utf-8');
		}

		//Add the html if passed
		if ($htmlBody) {
			$emailInfo['Message']['Body']['Html'] = array('Data' => $htmlBody, 'Charset' => 'utf-8');
		}

		//Send the email and return the result
		$result = $sesHandler->sendEmail($emailInfo);
		return $result;
	}

}


//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------

