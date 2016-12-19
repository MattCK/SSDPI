package adshotrunner.utilities;

import java.util.HashMap;
import java.util.List;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

/**
 * The MessageQueueClient sends and retrieves messages from selected queue
 */
public class MessageQueueClient {
	
	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	final public static String TAGIMAGEREQUESTS = "https://sqs.us-east-1.amazonaws.com/469658404108/TagImageRequests";
	final public static String SCREENSHOTREQUESTS = "https://sqs.us-east-1.amazonaws.com/469658404108/ScreenShotRequests";
	
	//--------------------------------------------------------------------------------------
	//---------------------------------- Static Methods ------------------------------------
	//--------------------------------------------------------------------------------------
	//***************************** Public Static Methods **********************************
	/**
	* Sends a message to the specified queue
	*
	* @param 	 	queueID  				Queue ID to publish message to
	* @param 	 	message  				Message text to send
	*/
	public static void sendMessage(String queueID, String message) {
		
        //Instantiate an Amazon SQS client using AdShotRunner's AWS credentials
		AmazonSQSClient sqsHandle = new AmazonSQSClient(AWSPermitter.getCredentials());
           
        //Set the AWS region 
        Region REGION = Region.getRegion(Regions.US_EAST_1);
        sqsHandle.setRegion(REGION);
        
        //Send the message  
        SendMessageResult sendResult = sqsHandle.sendMessage(new SendMessageRequest()
			.withQueueUrl(queueID)
			.withMessageBody(message));
		
	}
	
	
	/**
	* Gets messages from the chosen queue (up to 10 messages)
	*
	* @param 	 	queueID  				Queue ID to get messages from
	*/
	public static HashMap<String, String> getMessages(String queueID) throws Exception {
		
        //Instantiate an Amazon SQS client using AdShotRunner's AWS credentials
		AmazonSQSClient sqsHandle = new AmazonSQSClient(AWSPermitter.getCredentials());
           
        //Set the AWS region 
        Region REGION = Region.getRegion(Regions.US_EAST_1);
        sqsHandle.setRegion(REGION);
        
        //Get the messages from the queue
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueID);
        receiveMessageRequest.setMaxNumberOfMessages(10);
        List<Message> returnedMessages = sqsHandle.receiveMessage(receiveMessageRequest).getMessages();
        		
        //Put the messages into a returnable Map and return it
        HashMap<String, String> finalMessages = new HashMap<String, String>();
        for (Message currentMessage : returnedMessages) {
        	finalMessages.put(currentMessage.getReceiptHandle(), currentMessage.getBody());
        }
        return finalMessages;
	}
	
	
	/**
	* Deletes a message from the chosen queue
	*
	* @param 	 	queueID  				Queue ID to delete message from
	* @param 	 	messageID  				Message ID of message to delete
	*/
	public static void deleteMessage(String queueID, String messageID) {
		
        //Instantiate an Amazon SQS client using AdShotRunner's AWS credentials
		AmazonSQSClient sqsHandle = new AmazonSQSClient(AWSPermitter.getCredentials());
           
        //Set the AWS region 
        Region REGION = Region.getRegion(Regions.US_EAST_1);
        sqsHandle.setRegion(REGION);
        
        //Delete the message
        sqsHandle.deleteMessage(queueID, messageID);        
	}
	
	
}
