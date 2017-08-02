package adshotrunner.dispatcher;

import adshotrunner.campaigns.Campaign;
import adshotrunner.shotter.AdShotter;

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
			_runningCampaign.setError("UNABLE TO CAPTURE ADSHOTS"); return;
		}
		
		//Generate the PowerPoint
		try {_runningCampaign.generatePowerPoint();}
		catch (Exception e) {
			_runningCampaign.setError("UNABLE TO GENERATE POWERPOINT"); e.printStackTrace(); return;
		}
		
		//Mark the Campaign's status to FINSIHED
		_runningCampaign.setStatus(Campaign.FINISHED);
		
		//Create and send the email
		try {
			
			CampaignEmail resultsEmail = CampaignEmail.createCampaignEmail(_runningCampaign);
			resultsEmail.send(_runningCampaign.userEmailAddress());
			
		} catch (Exception e) {
			_runningCampaign.setError("UNABLE TO SEND CAMPAIGN EMAIL"); return;
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
	

}
