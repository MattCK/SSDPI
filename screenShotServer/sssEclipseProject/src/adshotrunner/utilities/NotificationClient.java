package adshotrunner.utilities;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

/**
 * The NotificationClient sends notices to the defined group
 */
public class NotificationClient {
	
	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	final public static String FRONTEND = "arn:aws:sns:us-east-1:469658404108:Frontend";
	final public static String SSS = "arn:aws:sns:us-east-1:469658404108:ScreenShotServer";
	
	//--------------------------------------------------------------------------------------
	//---------------------------------- Static Methods ------------------------------------
	//--------------------------------------------------------------------------------------
	//***************************** Public Static Methods **********************************
	/**
	* Sends a message to the specified group
	*
	* @param 	 	groupID  				Group ID to publish message to
	* @param 	 	subject  				Message subject
	* @param 	 	message  				Message body
	*/
	public static void sendNotice(String groupID, String subject, String message) {
        
        //Instantiate an Amazon SNS client using AdShotRunner's AWS credentials
		AmazonSNSClient snsHandle = new AmazonSNSClient(AWSPermitter.getCredentials());
           
        //Set the AWS region 
        Region REGION = Region.getRegion(Regions.US_EAST_1);
        snsHandle.setRegion(REGION);
		
		//Create the request to publish the notification with the passed arguments
		PublishRequest publishRequest = new PublishRequest(groupID, message, subject);
		
		//Send the notice to the group
		PublishResult publishResult = snsHandle.publish(publishRequest);
	}
	
}
