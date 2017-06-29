package adshotrunner.dispatcher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import adshotrunner.campaigns.Campaign;
import adshotrunner.campaigns.Creative;
import adshotrunner.utilities.ASRDatabase;

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

	//---------------------------------------------------------------------------------------
	//------------------------------- Main Executable ---------------------------------------
	//---------------------------------------------------------------------------------------	
	public static void main(String[] args) {
		
		//Begin the continuous loop
		while (true) {
		
			//Query the database for QUEUED Creatives and give them to new TagImager threads
			try {
				
				//Get the QUEUED Creatives. Set each to PROCESSING and put in a new TagImager thread
				//For now, we are giving each Creative its own thread and driver.
				Set<Creative> queuedCreatives = getQueuedCreatives();
				for (Creative currentCreative : queuedCreatives) {
					currentCreative.setStatus(Creative.PROCESSING);
					Set<Creative> creativeSet = new HashSet<Creative>();
					creativeSet.add(currentCreative);
					new TagImager(creativeSet);
				}
				
			} catch (Exception e) {e.printStackTrace();}
	
			//Query the database for QUEUED Campaigns and give them to new CampaignRunner threads
			try {
				
				//Get the QUEUED Campaigns. Set each to PROCESSING and put in a new CampaignRunner thread
				Set<Campaign> queuedCampaigns = getQueuedCampaigns();
				for (Campaign currentCampaign : queuedCampaigns) {
					currentCampaign.setStatus(Campaign.PROCESSING);
					new CampaignRunner(currentCampaign);
				}
				
			} catch (Exception e) {e.printStackTrace();}
	
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
	public static Set<Creative> getQueuedCreatives() throws SQLException {
		
		//Query the database for QUEUED Creatives
		ResultSet creativesResult = ASRDatabase.executeQuery("SELECT * " + 
															 "FROM creatives " + 
															 "WHERE CRV_status = 'QUEUED'");		
		
		//Put any found creatives into the set to return
		Set<Creative> queuedCreatives = new HashSet<Creative>();
		while (creativesResult.next()) {
			Creative currentCreative = Creative.getCreative(creativesResult.getInt("CRV_id"));
			if (currentCreative != null) {
				queuedCreatives.add(currentCreative);
			}
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
		
		//Query the database for QUEUED Campaigns
		ResultSet campaignsResult = ASRDatabase.executeQuery("SELECT * " + 
															 "FROM campaigns " + 
															 "WHERE CMP_status = 'QUEUED'");		
		
		//Put any found campaigns into the set to return
		Set<Campaign> queuedCampaigns = new HashSet<Campaign>();
		while (campaignsResult.next()) {
			Campaign currentCampaign = Campaign.getCampaign(campaignsResult.getInt("CMP_id"));
			if (currentCampaign != null) {
				queuedCampaigns.add(currentCampaign);
			}
		}	
		
		//Return the final set of Campaigns
		return queuedCampaigns;
	}
}
