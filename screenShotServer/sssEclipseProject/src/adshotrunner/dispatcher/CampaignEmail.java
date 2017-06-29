package adshotrunner.dispatcher;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import adshotrunner.campaigns.AdShot;
import adshotrunner.campaigns.Campaign;
import adshotrunner.campaigns.Creative;
import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.EmailClient;

/**
 * The CampaignEmail class send an email to a Campaign's user formatted with the
 * finished Campaign details such as which Creative was injected on which pages and links
 * to the results page and final PowerPoint.
 */
public class CampaignEmail {
	
	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//URL to final results page. The query portion takes the Campaign UUID.
	final public static String RESULTSPAGEURL = "https://" + ASRProperties.asrDomain() + "/campaignResults.php?id="; 

	
	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	public static CampaignEmail createCampaignEmail(Campaign finishedCampaign) {
				
		//Verify the Campaign is finished. If not, throw an error.
		if (!finishedCampaign.status().equals(Campaign.FINISHED)) {
			throw new AdShotRunnerException("Cannot send email for non-finished Campaign");
		}		
		//Construct the CampaignEmail and return it
		return new CampaignEmail(finishedCampaign);
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	 * Finished Campaign to format for the email
	 */
	final private Campaign _finishedCampaign;
	
	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Instantiates CampaignEmail with passed Campaign stored
	 * 
	 * @param finishedCampaign		Finished Campaign to send user email for
	 */
	private CampaignEmail(Campaign finishedCampaign) {

		//Store the Campaign
		_finishedCampaign = finishedCampaign;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************		
	public void send(String emailAddress) {

		//Get the finished date
		String campaignDate = new SimpleDateFormat("MM/dd/yyyy").format(_finishedCampaign.finishedTimestamp());
		
		//Create the subject line
		String emailSubject = "AdShotRunner™ Campaign Finished: " + 
							  _finishedCampaign.customerName() + " - " + campaignDate;

		//Send the email
		EmailClient.sendEmail(ASRProperties.emailAddressScreenshots(), emailAddress, emailSubject, 
															 		   getPlainText(), getHTMLText());
	}

	//******************************** Private Methods **************************************		
	private String getHTMLText() {
		
		//Create the links for the powerpoint and campaign results
		String resultLinks = "PowerPoint:  <a href='" + _finishedCampaign.powerPointURL() + "'>Download</a><br>" + 
						     "Screenshots: <a href='" + RESULTSPAGEURL + _finishedCampaign.uuid() + "'>View All</a>";
		
		//Put the email parts together
		return "<p>" + getIntroText() + "</p><p>" + resultLinks + "</p><br><br>" + 
				getAdShotInjectedCreativesTable() + "<br><br>" +
				getCreativeNamesTable();
	}
	
	private String getPlainText() {
		
		//Create the links for the powerpoint and campaign results
		String resultLinks = "PowerPoint: " + _finishedCampaign.powerPointURL() + " \n\n" + 
						     "Screenshots: " + RESULTSPAGEURL + _finishedCampaign.uuid();
		
		//Put the email parts together
		return getIntroText() + "\n\n" + resultLinks;
	}
	
	
	private String getIntroText() {
		
		//Get the finished date
		String campaignDate = new SimpleDateFormat("MM/dd/yyyy").format(_finishedCampaign.finishedTimestamp());
		
		return "AdshotRunner™ has finished the screenshots for your campaign: " + 
				_finishedCampaign.customerName() + " - " + campaignDate;
	}
	
	/**
	 * Creates a map pairing each injected Creative with its name.
	 * 
	 * 1) If there is only one injected Creative of a certain dimension, it is named
	 * 	  the dimension. (WIDTHxHEIGHT)
	 * 
	 * 2) If more than one injected Creative of the same dimension exist, they are named the
	 *    dimension with a letter suffix (-A, -B, -C, etc.) The order begins with the injected
	 *    Creative with the highest priority.
	 *    
	 * All names are unique.
	 * 
	 * @return	Map of injected Creatives with their unique individual names.
	 */
	public Map<Creative, String> getInjectedCreativeNames() {
		
		//Group all of the injected Creatives into sets of the same dimensions.
		//The sets are keyed to their dimensions string (WIDTHxHEIGHT)
		//Loop through each of the AdShots and add their injected Creatives into their proper set
		Map<String, Set<Creative>> creativeImageSetsByDimension = new TreeMap<String, Set<Creative>>();
		for (AdShot currentAdShot : _finishedCampaign.adShots()) {
			
			//Loop through the injected Creatives of the current AdShot and add them to
			//the proper set. If the Creative already exists there, nothing occurs.
			Set<Creative> injectedCreatives = currentAdShot.injectedCreatives();
			for (Creative currentCreative : injectedCreatives) {
				
				//Define the dimensions key for the Creative
				String tagDimensionKey = currentCreative.width() + "x" + currentCreative.height();
				
				//If the set for the current dimensions key does not exist, create it
				if (!creativeImageSetsByDimension.containsKey(tagDimensionKey)) {
					creativeImageSetsByDimension.put(tagDimensionKey, new TreeSet<Creative>());
				}
				
				//Add the Creative into the set of its dimensions key.
				//If the Creative already exists in the set, nothing occurs.
				creativeImageSetsByDimension.get(tagDimensionKey).add(currentCreative);
			}
		}
		
		//Name each Creative according to its dimensions.
		//If only one Creative of a give dimension exists, it is named the dimension (WIDTHxHEIGHT)
		//If more than one injected Creative of the same dimension exist, they are named the
		//dimension with a letter suffix (-A, -B, -C, etc.) The order begins with the injected
		//Creative with the highest priority.		
		Map<Creative, String> creativeImageNames = new LinkedHashMap<Creative, String>();
		for (Map.Entry<String, Set<Creative>> creativeGroup : creativeImageSetsByDimension.entrySet()) {
			
			//Put the group info into unique variables for clarity
			String creativeDimensions = creativeGroup.getKey();
			Set<Creative> creativeImageSet = creativeGroup.getValue();
			
			//Add the name for each Creative.
			//If more than one Creative exist in a set, append "A", "B", etc. to each in the order they appear
			int letterIterator = 1;
			for (Creative currentCreative : creativeImageSet) {
				
				//If there is only one Creative in the set, use the dimensions as its name
				if (creativeImageSet.size() <= 1) {creativeImageNames.put(currentCreative, creativeDimensions);}
				
				//If more than one Creative exists, append letters to the dimensions
				else {
					String currentLetter = String.valueOf((char)(letterIterator + 64));
					creativeImageNames.put(currentCreative, creativeDimensions + "-" + currentLetter);
				}
				
				//Increment the letter iterator
				++letterIterator;
			}
		}		
		
		return creativeImageNames;
	}
	
	private String getAdShotInjectedCreativesTable() {
		
		//Get the tag image names
		Map<Creative, String> creativeImageNames = getInjectedCreativeNames();
		
		//Create the table that will show each AdShot page and the names of the injected Creatives
		String adShotsTable = "<table border='1' style='border-collapse: collapse;'>";
		adShotsTable += 	  "<tr style='font-weight: bold'><th>Page</th><th>Tags Used</th></tr>";		
		
		//For each adshot, add a row that shows which tag images, if any, were injected
		for (AdShot currentAdShot : _finishedCampaign.adShots()) {
			
			//Create the string of injected tag image names to be placed in the cell
			String injectedCreativeNames = "";
			for (Creative currentCreative : currentAdShot.injectedCreatives()) {
				if (!injectedCreativeNames.isEmpty()) {injectedCreativeNames += ", ";}
				injectedCreativeNames += creativeImageNames.get(currentCreative);
			}
			
			//Add the current AdShot page row to the table
			adShotsTable += "<tr><td>" + currentAdShot.finalURL() + "</td>" + 
								"<td>" + injectedCreativeNames + "</td></tr>";
		}
		adShotsTable += "</table>";
		return adShotsTable;
	}
	
	private String getCreativeNamesTable() {
		
		//Get the tag image names
		Map<Creative, String> creativeImageNames = getInjectedCreativeNames();
		
		//Create the table that will show each AdShot page and the names of the injected Creatives
		String tagImagesTable = "<table border='1' style='border-collapse: collapse;'>";
		tagImagesTable += 	    "<tr style='font-weight: bold'><th>Name</th><th>Image</th></tr>";		

		//Add each injected tag to the table
		for (Map.Entry<Creative, String> creativeImageName : creativeImageNames.entrySet()) {
			tagImagesTable += "<tr><td>" + creativeImageName.getValue() + "</td>" + 
								  "<td><img src='" + creativeImageName.getKey().imageURL() + "'></td></tr>";
		}
		tagImagesTable += "</table>";
		return tagImagesTable;
	}
	
}
