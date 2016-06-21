<?PHP
/**
* Contains the class for queueing and reading messages
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\Utilities;

/**
* The MessageQueueClient sends and retrieves messages from selected queue
*/
class MessageQueueClient {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	const TAGIMAGEREQUESTS = 'https://sqs.us-east-1.amazonaws.com/469658404108/TagImageRequests';
	const SCREENSHOTREQUESTS = 'https://sqs.us-east-1.amazonaws.com/469658404108/ScreenShotRequests';
	

	//--------------------------------------------------------------------------------------
	//---------------------------------- Static Methods ------------------------------------
	//--------------------------------------------------------------------------------------
	//***************************** Public Static Methods **********************************
	/**
	* Sends a message to the specified queue
	*
	* @param string	 	$queueID  				Queue ID to publish message to
	* @param string	 	$message  				Message text to send
	* @return boolean  		 					TRUE on success and FALSE on failure.
	*/
	static public function sendMessage($queueID, $message) {

		//Verify all arguments were not empty
		if (!$queueID || !$message) {return FALSE;}

		//Create the SQS handler object
		$awsFactory = getAWSFactory();
		$sqsHandler = $awsFactory->get('sqs');

		//Send the notice and return the result
		$result = $sqsHandler->sendMessage(array(
		    'QueueUrl' => $queueID,
		    'MessageBody' => $message,
		));
		return $result;
	}

	/**
	* Gets messages from the chosen queue (up to 10 messages)
	*
	* @param string	 	$queueID  				Queue ID to get messages from
	* @return boolean  		 					TRUE on success and FALSE on failure.
	*/
	static public function getMessages($queueID) {

		//Verify the queue ID is not empty
		if (!$queueID) {return FALSE;}

		//Create the SQS handler object
		$awsFactory = getAWSFactory();
		$sqsHandler = $awsFactory->get('sqs');

		//Poll the queue for new messages
		$response = $sqsHandler->receiveMessage(array(
		    'QueueUrl' => $queueID,
		    'MaxNumberOfMessages' => 10
		));

		//Put any returned messages into an array and return it
		$receivedMessages = array();
		if ($response['Messages'] && (count($response['Messages']) > 0)) {
			foreach ($response['Messages'] as $curMessage) {
				$receivedMessages[$curMessage['ReceiptHandle']] = $curMessage['Body'];
			}
		}
		return $receivedMessages;
	}

	/**
	* Deletes a message from the chosen queue
	*
	* @param string	 	$queueID  				Queue ID to delete message from
	* @param string	 	$messageID  			Message ID of message to delete
	* @return boolean  		 					TRUE on success and FALSE on failure.
	*/
	static public function deleteMessage($queueID, $messageID) {

		//Verify the queue ID is not empty
		if (!$queueID || !$messageID) {return FALSE;}

		//Create the SQS handler object
		$awsFactory = getAWSFactory();
		$sqsHandler = $awsFactory->get('sqs');

		//Send the notice and return the result
		$result = $sqsHandler->deleteMessage(array(
		    'QueueUrl' => $queueID,
		    'ReceiptHandle' => $messageID,
		));
		return $result;
	}

}


//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------

