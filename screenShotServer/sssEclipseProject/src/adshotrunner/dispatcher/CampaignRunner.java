package adshotrunner.dispatcher;

import org.apache.commons.lang.exception.ExceptionUtils;

import adshotrunner.campaigns.AdShot;
import adshotrunner.campaigns.Campaign;
import adshotrunner.shotter.AdShotter;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.NotificationClient;

/**
 * The CampaignRunner captures a Campaign's AdShot images using the AdShotter, creates the
 * PowerPoint from them, and then emails the Campaign's user when finished.
 */
public class CampaignRunner implements Runnable {

	
	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	 * Campaign to capture images, process the PowerPoint, and email the user for
	 */
	final private Campaign _runningCampaign;

	/**
	 * Thread of CampaignRunner instance used to capture images, process the PowerPoint, and email the user
	 */
	final private Thread _campaignThread;
	
	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Creates a new thread and runs the campaign
	 * 
	 * @param campaignToRun		Campaign to run (capture AdShot images, build PowerPoint, email user)
	 */	
	public CampaignRunner(Campaign campaignToRun) {
		
		//Store the Campaign
		_runningCampaign = campaignToRun;
		
		//Start the thread to run the campaign
		_campaignThread = new Thread(this);
		_campaignThread.start();
	}
	
	//---------------------------------------------------------------------------------------
	//-------------------------------- Thread Executable ------------------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Captures the AdShot images using the AdShotter, creates the final PowerPoint from the
	 * AdShots, and emails the user when finished
	 */
	@Override
	public void run() {
		
		//Mark the Campaign's status to PROCESSING
		_runningCampaign.setStatus(Campaign.PROCESSING);
		
		//Capture the AdShot images
		try {AdShotter.captureAdShotImages(_runningCampaign.adShots());}
		catch (Exception e) {
			_runningCampaign.setError(Campaign.SCREENSHOTCAPTURE); 
			sendErrorNotification(e);
			ErrorEmail.createErrorEmail(_runningCampaign).send();
			e.printStackTrace(); return;
		}
		
		//Generate the PowerPoint
		try {_runningCampaign.generatePowerPoint();}
		catch (Exception e) {
			_runningCampaign.setError(Campaign.POWERPOINTGENERATION); 
			sendErrorNotification(e);
			ErrorEmail.createErrorEmail(_runningCampaign).send();
			e.printStackTrace(); return;
		}
		
		//Mark the Campaign's status to FINSIHED
		_runningCampaign.setStatus(Campaign.FINISHED);
		
		//Create and send the email
		try {
			
			CampaignEmail resultsEmail = CampaignEmail.createCampaignEmail(_runningCampaign);
			resultsEmail.send(_runningCampaign.userEmailAddress());
			
		} catch (Exception e) {
			_runningCampaign.setError(Campaign.CAMPAIGNEMAILSEND); 
			sendErrorNotification(e);
			e.printStackTrace(); return;
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	 * @return	Thread of the CampaignRunner
	 */
	public Thread thread() {return _campaignThread;}
	
	//******************************** Private Accessors *************************************
	/**
	 * Sends notification to SSS Issues group that includes the passed error message,
	 * the Exception stacktrace, and Campaign information (ID, UUID, User Email, Customer)
	 * 
	 * @param campaignException		Exception thrown from the error
	 */
	private void sendErrorNotification(Exception campaignException) {
		
		//Create the initial notification message with the Campaign information
		String notice = "There was an error processing a Campaign: " + _runningCampaign.errorMessage() + "\n\n";
		notice += "ID: " + _runningCampaign.id() + "\n";
		notice += "UUID: " + _runningCampaign.uuid() + "\n";
		notice += "User: " + _runningCampaign.userEmailAddress() + "\n";
		notice += "Customer: " + _runningCampaign.customerName() + "\n\n";
		
		//Include the AdShot URLs
		notice += "AdShot URLs: \n\n";
		for (AdShot currentAdShot : _runningCampaign.adShots()) {
			notice += "    - " + currentAdShot.requestedURL();
			if (currentAdShot.storyFinder()) {notice += " (StoryFinder)";}
			notice += "\n";
		}
		notice += "\n";
		
		//Include the exception
		notice += "Thrown Exception: \n\n" + ExceptionUtils.getStackTrace(campaignException);
		
		//Send the notice
		NotificationClient.sendNotice(ASRProperties.notificationGroupForSSSIssues(), 
									  "ERROR PROCESSING CAMPAIGN: " + _runningCampaign.errorMessage(), notice);
	}
	
}
