package adshotrunner.dispatcher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import adshotrunner.campaigns.Campaign;
import adshotrunner.campaigns.Creative;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.ASRDatabase;
import adshotrunner.utilities.MessageQueueClient;

/**
 * The ASRDispatcher is a simple looping server that queries the database
 * for new Creatives and Campaigns that need to be processed and then
 * places them in CampaignRunner and TagImager threads as needed.
 */
public class ASRDispatcher {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//Time to pause between database queries
	final public static int QUERYPAUSETIME = 2000;	//In miliseconds 
	
	//Time to refresh the database connection to compensate for memory leaks
	final public static int CONNECTIONREFRESHTIME = 30000;	//In miliseconds 

	//---------------------------------------------------------------------------------------
	//------------------------------- Main Executable ---------------------------------------
	//---------------------------------------------------------------------------------------	
	public static void main(String[] args) {
		
		//Create the sets to store the the running threads
		Set<TagImager> runningTagImagers = new HashSet<TagImager>();
		Set<CampaignRunner> runningCampaignRunners = new HashSet<CampaignRunner>();
		
		//Begin the continuous loop
		boolean continueExecution = true;
		long timeOfLastRefresh = System.nanoTime();
		while (continueExecution) {
		
			//Query the database for QUEUED Creatives and give them to new TagImager threads
			try {
				
				//Get the QUEUED Creatives. Set each to PROCESSING and put in a new TagImager thread
				//For now, we are giving each Creative its own thread and driver.
				Set<Creative> queuedCreatives = getQueuedCreatives();
				for (Creative currentCreative : queuedCreatives) {
					currentCreative.setStatus(Creative.QUEUED);
					Set<Creative> creativeSet = new HashSet<Creative>();
					creativeSet.add(currentCreative);
					TagImager newTagImager = new TagImager(creativeSet);
					runningTagImagers.add(newTagImager);
				}
				
			} catch (Exception e) {e.printStackTrace();}
	
			//Query the database for QUEUED Campaigns and give them to new CampaignRunner threads
			try {
				
				//Get the QUEUED Campaigns. Set each to PROCESSING and put in a new CampaignRunner thread
				Set<Campaign> queuedCampaigns = getQueuedCampaigns();
				for (Campaign currentCampaign : queuedCampaigns) {
					currentCampaign.setStatus(Campaign.QUEUED);
					CampaignRunner newCampaignRunner = new CampaignRunner(currentCampaign);
					runningCampaignRunners.add(newCampaignRunner);
				}
				
			} catch (Exception e) {e.printStackTrace();}
	
			//Dereference any finished TagImagers
			Iterator<TagImager> tagImagerIterator = runningTagImagers.iterator();
			while (tagImagerIterator.hasNext()) {
				TagImager currentTagImager = tagImagerIterator.next();
				if (!currentTagImager.thread().isAlive()) {
					tagImagerIterator.remove();
				}
			}
			
			//Dereference any finished CampaignRunners
			Iterator<CampaignRunner> campaignRunnerIterator = runningCampaignRunners.iterator();
			while (campaignRunnerIterator.hasNext()) {
				CampaignRunner currentCampaignRunner = campaignRunnerIterator.next();
				if (!currentCampaignRunner.thread().isAlive()) {
					campaignRunnerIterator.remove();
				}
			}
			
			//If no threads are running and it is time to refresh the connection, do so
			if ((runningTagImagers.size() == 0) && (runningCampaignRunners.size() == 0) &&
				((System.nanoTime() - timeOfLastRefresh)/1000000) > CONNECTIONREFRESHTIME) {
					ASRDatabase.refreshConnection();
					timeOfLastRefresh = System.nanoTime();
			}
			
			//Check for a stop command
			Set<String> commands = getCommands();
			for (String currentCommand : commands) {
				if (currentCommand.toLowerCase().matches("stop")) {
					
					//Notify command received
					System.out.println("RECEIVED STOP COMMAND! - WAITING FOR THREADS");
					
					//Wait until tag imagers are terminated
					for (TagImager currentTagImager : runningTagImagers) {
						try {currentTagImager.thread().join();} 
						catch (Exception e) {e.printStackTrace();}
					}
					
					//Wait until campaign runners are terminated
					for (CampaignRunner currentCampaignRunner : runningCampaignRunners) {
						try {currentCampaignRunner.thread().join();} 
						catch (Exception e) {e.printStackTrace();}
					}
					
					//Flag to end the loop
					continueExecution = false;
					System.out.println("SHUTTING DOWN");
				}
			}
			
			//Pause until next database query
	    	try {
				Thread.sleep(QUERYPAUSETIME);
			} catch (InterruptedException e) {}
		}
	}

	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//**************************** Private Static Methods ***********************************
	/**
	 * Queries the database for Creatives with a "QUEUED" status and returns them
	 * in a Set.
	 * 
	 * @return	Set of Creatives from the database with "QUEUED" status
	 */
	public static Set<Creative> getQueuedCreatives() {
		
		//Try getting Creatives from the database
		Set<Creative> queuedCreatives = new HashSet<Creative>();
		String getCreativesQuery = 	"SELECT * " + 
									"FROM creatives " + 
									"WHERE CRV_status = 'READY'";
		try (ResultSet creativesResult = ASRDatabase.executeQuery(getCreativesQuery)){
					
			//Put any found creatives into the set to return
			queuedCreatives = new HashSet<Creative>();
			while (creativesResult.next()) {
				Creative currentCreative = Creative.getCreative(creativesResult.getInt("CRV_id"));
				if (currentCreative != null) {
					queuedCreatives.add(currentCreative);
				}
			}	
		}
		
		//If unsuccessful, print a warning and return any found creatives
		catch (Exception e) {
			System.out.println("Unable to query database for Creatives");
			e.printStackTrace();
			return queuedCreatives;
		}
		
		//Return the final set of Creatives
		return queuedCreatives;
	}

	/**
	 * Queries the database for Campaigns with a "QUEUED" status and returns them
	 * in a Set.
	 * 
	 * @return	Set of Campaigns from the database with "QUEUED" status
	 */
	public static Set<Campaign> getQueuedCampaigns() throws SQLException {
		
		//Try getting Campaigns from the database
		Set<Campaign> queuedCampaigns = new HashSet<Campaign>();
		String getCampaignsQuery = 	"SELECT * " + 
									"FROM campaigns " + 
									"WHERE CMP_status = 'READY'";
		try (ResultSet campaignsResult = ASRDatabase.executeQuery(getCampaignsQuery)) {
					
			//Put any found campaigns into the set to return
			while (campaignsResult.next()) {
				Campaign currentCampaign = Campaign.getCampaign(campaignsResult.getInt("CMP_id"));
				if (currentCampaign != null) {
					queuedCampaigns.add(currentCampaign);
				}
			}	
		}
		
		//If unsuccessful, print a warning and return any found campaigns
		catch (Exception e) {
			System.out.println("Unable to query database for Campaigns");
			e.printStackTrace();
			return queuedCampaigns;
		}
		
		
		//Return the final set of Campaigns
		return queuedCampaigns;
	}
	
	/**
	 * Checks the Dispatcher Commands queue for any new commands and returns them
	 * 
	 * @return	Commands found in the queue
	 */
	private static Set<String> getCommands() {
		
		//Check the queue for any messages
		HashMap<String, String> receivedMessages = new HashMap<String, String>();
		Set<String> commands = new HashSet<String>();
		try {receivedMessages = MessageQueueClient.getMessages(ASRProperties.queueForDispatcherCommands());}
		catch (Exception e) {e.printStackTrace();} //Do nothing. A connection error occurs seldom but regularly
		
		//Store any commands and delete the message from the queue
		for (Map.Entry<String, String> entry : receivedMessages.entrySet()) {		
			commands.add(entry.getValue());
			MessageQueueClient.deleteMessage(ASRProperties.queueForDispatcherCommands(), entry.getKey());
		}
		
		//Return the commands
		return commands;
	}
}
