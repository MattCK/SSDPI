package adshotrunner.tests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import adshotrunner.campaigns.Campaign;
import adshotrunner.campaigns.Creative;
import adshotrunner.utilities.ASRDatabase;

public class DatabaseMemoryLeak {

	public static void main(String[] args) {

		while (true) {
			Set<Campaign> queuedCampaigns = getQueuedCampaigns();
			
	    	try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
		
	}
	
	public static Set<Campaign> getQueuedCampaigns() {
		
		//Try getting Campaigns from the database
		Set<Campaign> queuedCampaigns = new HashSet<Campaign>();
		try (ResultSet campaignsResult = ASRDatabase.executeQuery(	"SELECT * " + 
														"FROM campaigns " + 
														"WHERE CMP_status = 'QUEUED'")) {
					
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

}
