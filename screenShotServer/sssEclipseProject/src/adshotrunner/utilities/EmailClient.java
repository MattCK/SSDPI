package adshotrunner.utilities;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import adshotrunner.system.ASRProperties;

/**
 * The EmailClient sends emails to the intended recipients from the stated email address
 */
public class EmailClient {
	
	//--------------------------------------------------------------------------------------
	//---------------------------------- Static Methods ------------------------------------
	//--------------------------------------------------------------------------------------
	//***************************** Public Static Methods **********************************
	/**
	* Sends an email to the recipient via the passed from address, subject, and body
	*
	* @param 	 	fromAddress  			Originating email address (From: field).
	* @param 	 	toAddress  				Recipient email address(es) (To: field). String or array of strings.
	* @param 	 	subject  				Email subject
	* @param 	 	textBody  				Plain text email body
	* @param 	 	htmlBody  				HTML text email body
	*/
	public static void sendEmail(String fromAddress, String toAddresses, String subject, 
								String textBody, String htmlBody) {
        
        //Construct the destination instance that holds all of the "TO:" addresses
        Destination emailDestination = new Destination().withToAddresses(new String[]{toAddresses});
        
        //Create the subject and body (text and/or html) of the message.
        Content contentSubject = new Content().withData(subject);
        Content contentTextBody = new Content().withData(textBody); 
        Content contentHTMLBody = new Content().withData(htmlBody); 
        Body emailBody = new Body().withText(contentTextBody).withHtml(contentHTMLBody);
        
        //Create a message with the specified subject and body.
        Message emailMessage = new Message().withSubject(contentSubject).withBody(emailBody);
        
        //Put the email request together
        SendEmailRequest emailRequest = new SendEmailRequest().withSource(fromAddress).withDestination(emailDestination).withMessage(emailMessage);
    
        //Instantiate an Amazon SES client using AdShotRunner's AWS credentials
        AmazonSimpleEmailServiceClient sesHandle = new AmazonSimpleEmailServiceClient(AWSConnector.getCredentials());
           
        //Set the AWS region 
        Regions awsRegions = Regions.fromName(ASRProperties.awsRegion());
        Region emailRegion = Region.getRegion(awsRegions);
        sesHandle.setRegion(emailRegion);
   
        //Send the email.
        sesHandle.sendEmail(emailRequest);  
	}
}
