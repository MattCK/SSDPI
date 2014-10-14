<?PHP
/**
* Contains the class for sending notices
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\Utilities;

/**
* The NotificationClient sends notices to the defined group
*/
class NotificationClient {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	const FRONTEND = 'arn:aws:sns:us-east-1:469658404108:Frontend';
	const SSS = 'arn:aws:sns:us-east-1:469658404108:ScreenShotServer';
	

	//--------------------------------------------------------------------------------------
	//---------------------------------- Static Methods ------------------------------------
	//--------------------------------------------------------------------------------------
	//***************************** Public Static Methods **********************************
	/**
	* Sends a message to the specified group
	*
	* @param string	 	$group  				Group ID to publish message to
	* @param string	 	$subject  				Message subject
	* @param string	 	$message  				Message body
	* @return boolean  		 					TRUE on success and FALSE on failure.
	*/
	function sendNotice($groupID, $subject, $message) {

		//Verify all arguments were not empty
		if (!$groupID || !$subject || !$message) {return FALSE;}

		//Create the SES handler object
		$awsFactory = getAWSFactory();
		$snsHandler = $awsFactory->get('sns');

		//Send the notice and return the result
		$result = $snsHandler->publish(array(
		    'TargetArn' => $groupID,
		    'Message' => $message,
	        'Subject' => $subject
		));
		return $result;

	}

}


//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------

