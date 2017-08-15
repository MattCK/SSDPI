package adshotrunner.dispatcher;

import java.text.SimpleDateFormat;

import adshotrunner.campaigns.Campaign;
import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.EmailClient;

/**
 * The ErrorEmail class sends an email to a Campaign's user notifying them
 * that there was an error processing the Campaign.
 */
public class ErrorEmail {

	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	public static ErrorEmail createErrorEmail(Campaign problemCampaign) {
				
		//Verify the Campaign had an error. If not, throw an error.
		if (!problemCampaign.status().equals(Campaign.ERROR)) {
			throw new AdShotRunnerException("Cannot send error email for non-error Campaign");
		}		
		//Construct the ErrorEmail and return it
		return new ErrorEmail(problemCampaign);
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	 * Problem Campaign to format for the email
	 */
	final private Campaign _problemCampaign;
	
	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Instantiates ErrorEmail with passed Campaign stored
	 * 
	 * @param problemCampaign		Problem Campaign to send user email for
	 */
	private ErrorEmail(Campaign problemCampaign) {

		//Store the Campaign
		_problemCampaign = problemCampaign;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************		
	public void send() {

		//Get the campaign date
		String campaignDate = new SimpleDateFormat("MM/dd/yyyy").format(_problemCampaign.createdTimestamp());
		
		//Create the subject line
		String emailSubject = "AdShotRunner™ Campaign Error: " + 
							   _problemCampaign.customerName() + " - " + campaignDate;

		//Send the email
		EmailClient.sendEmail(ASRProperties.emailAddressSupport(), _problemCampaign.userEmailAddress(), 
							  emailSubject, getPlainText(), getHTMLText());
	}
	
	private String getPlainText() {
		
		//Get the campaign date
		String campaignDate = new SimpleDateFormat("MM/dd/yyyy").format(_problemCampaign.createdTimestamp());
		
		//Create the body text
		String message = "Unfortunately, a problem occurred while processing your screenshots: ";
		message += _problemCampaign.customerName() + " - " + campaignDate + "\n\n";
		
		message += "We have been notified of this issue and are looking into it. ";
		message += "In the meantime, please try resubmitting your request: ";
		message += "https://" + ASRProperties.asrDomain() + "/ \n\n";
		
		message += "We apologize for this inconvenience and we appreciate your understanding ";
		message += "while we investigate this matter. ";
		message += "If you have any questions or the problem persists, please reply directly to this email ";
		message += "or call (773) 295-2386.\n\n";
		
		message += "Sincerely, \n\n";
		message += "AdShotRunner™ Support Team \n";
		message += ASRProperties.emailAddressSupport() + "\n";
		message += "(773) 295-2386 (US)\n";
		message += "www.adshotrunner.com";
		
		message += "\n\nID: " + _problemCampaign.id();
		
		return message;
	}

	private String getHTMLText() {
		
		//Get the campaign date
		String campaignDate = new SimpleDateFormat("MM/dd/yyyy").format(_problemCampaign.createdTimestamp());
		
		//Create the body text
		String htmlMessage = "Unfortunately, a problem occurred while processing your screenshots: ";
		htmlMessage += "<strong>" + _problemCampaign.customerName() + " - " + campaignDate + "</strong><br/><br/>";
		
		htmlMessage += "We have been notified of this issue and are looking into it. ";
		htmlMessage += "In the meantime, please try ";
		htmlMessage += "<a href='https://" + ASRProperties.asrDomain() + "/'>resubmitting your request</a>.<br/><br/>";
		
		htmlMessage += "We apologize for this inconvenience and we appreciate your understanding ";
		htmlMessage += "while we investigate this matter. ";
		htmlMessage += "If you have any questions or the problem persists, please reply directly to this email ";
		htmlMessage += "or call (773) 295-2386.<br/><br/>";
		
		htmlMessage += "Sincerely, <br/><br/>";
		htmlMessage += "AdShotRunner™ Support Team <br/>";
		htmlMessage += ASRProperties.emailAddressSupport() + "<br/>";
		htmlMessage += "(773) 295-2386 (US)<br/>";
		htmlMessage += "www.adshotrunner.com";
		
		htmlMessage += "<br/><br/>ID: " + _problemCampaign.id();
		
		return htmlMessage;
	}

}
