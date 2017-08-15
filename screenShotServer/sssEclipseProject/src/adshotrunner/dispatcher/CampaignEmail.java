package adshotrunner.dispatcher;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
 * The CampaignEmail class sends an email to a Campaign's user formatted with the
 * finished Campaign details such as which Creative was injected on which pages and links
 * to the results page and final PowerPoint.
 */
public class CampaignEmail {
	
	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//URL to final results page. The query portion takes the Campaign UUID.
	final public static String RESULTSPAGEURL = "https://" + ASRProperties.asrDomain() + "/campaignResults.php?uuid="; 
	
	//HTML Styles
	final private static String TITLESTYLE = "style='font-weight: bold; font-size: 15px;'";

	
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
		return "<p>" + getIntroText() + "</p><p>" + 
					   resultLinks + "</p><p>" + 
					   getContactText(true) + "</p><br>" + 
					   getErrorTable() + 
					   getSummaryTable() + "<br><br>" +
					   getUnusedAdShotsTable() +
					   getCreativeNamesTable();
	}
	
	private String getPlainText() {
		
		//Create the links for the powerpoint and campaign results
		String resultLinks = "PowerPoint: " + _finishedCampaign.powerPointURL() + " \n\n" + 
						     "Screenshots: " + RESULTSPAGEURL + _finishedCampaign.uuid();
		
		//Put the email parts together
		return getIntroText() + "\n\n" + resultLinks + "\n\n" + getContactText(false);
	}
	
	/**
	 * Returns the introduction text for the email stating the campaign is finished
	 * while giving the Campaign's customer name and date it was finished.
	 * 
	 * @return	Introduction text with Campaign customer name and Campaign finish date
	 */
	private String getIntroText() {
		
		//Get the finished date
		String campaignDate = new SimpleDateFormat("MM/dd/yyyy").format(_finishedCampaign.finishedTimestamp());
		
		return "AdshotRunner™ has finished the screenshots for your campaign: " + 
				_finishedCampaign.customerName() + " - " + campaignDate;
	}
	
	/**
	 * Returns the contact us text, phone number, and email address.
	 * 
	 * If TRUE is passed, line breaks will be defined by <br/> tags. If
	 * FALSE is passed, line breaks will be defined by the "\n" character.
	 * 
	 * @param formatForHTML		TRUE to use <br/> for line breaks, otherwise "\n" will be used.
	 * @return
	 */
	private String getContactText(boolean formatForHTML) {
		
		//Set the newline dilineator
		String newlineDilineator = (formatForHTML) ? "<br/>" : "\n";
		
		//Create the text
		String contactText = "Experiencing Issues or Have Questions?" + newlineDilineator;
		contactText += "Call Us: (773) 295-2386" + newlineDilineator;
		contactText += "Email Us: " + ASRProperties.emailAddressSupport();
		return contactText;
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
	
	/**
	 * Returns HTML string of the summary table for the finished AdShots that were placed
	 * in the final PowerPoint.
	 * 
	 * The HTML includes a "Summary: " span followed by the actual table.
	 * 
	 * @return		HTML string of the summary table with title
	 */
	private String getSummaryTable() {
		
		//Create the title text
		String titleText = "<span " + TITLESTYLE + ">Summary: </span><br/><br/>";
		
		//Create the table that will show each AdShot and its information
		String summaryTable = "<table border='1' cellpadding='3px' cellspacing='0' width='800px' style='border-collapse: collapse;'>";
		
		//Create the header row	
		summaryTable += "<tr style='font-weight: bold'><th>Page</th>"
				   	  + "<th>Device</th>"
				  	  + "<th>Below Fold</th>"
					  + "<th>Creative Used</tr>";		
		
		//Add the rows for Desktop AdShots that finished without error
		for (AdShot currentAdShot : _finishedCampaign.adShots()) {
			if ((currentAdShot.status().equals(AdShot.FINISHED)) && (!currentAdShot.mobile())) {
				summaryTable += getSummaryTableRow(currentAdShot);
			}
		}
		
		//Add the rows for Mobile AdShots that finished without error
		for (AdShot currentAdShot : _finishedCampaign.adShots()) {
			if ((currentAdShot.status().equals(AdShot.FINISHED)) && (currentAdShot.mobile())) {
				summaryTable += getSummaryTableRow(currentAdShot);
			}
		}
		
		summaryTable += "</table>";
				
		//Return the title with the table
		return titleText + summaryTable;
	}
	
	/**
	 * Creates and returns the row (<tr>...</tr>) HTML for the passed
	 * AdShot to be placed in the summary table
	 * 
	 * @param targetAdShot		AdShot to create row for
	 * @return
	 */
	private String getSummaryTableRow(AdShot targetAdShot) {

		//Create the string of injected tag image names to be placed in the cell
		Map<Creative, String> creativeImageNames = getInjectedCreativeNames();
		String injectedCreativeNames = "";
		for (Creative currentCreative : targetAdShot.injectedCreatives()) {
			if (!injectedCreativeNames.isEmpty()) {injectedCreativeNames += ", ";}
			injectedCreativeNames += creativeImageNames.get(currentCreative);
		}
		
		//Create the cell text for the device and below-the-fold
		String deviceText = (targetAdShot.mobile()) ? "Mobile" : "Desktop";
		String btfText = (targetAdShot.belowTheFold()) ? "✓" : "";
		
		//Create the row HTML
		String rowHTML = "<tr>"
						   + "<td>" + targetAdShot.finalURL() + "</td>"
						   + "<td style='text-align: center;'>" + deviceText + "</td>"
						   + "<td style='text-align: center;'>" + btfText + "</td>"
						   + "<td style='text-align: center;'>" + injectedCreativeNames + "</td>"
						   + "</tr>";
		
		return rowHTML;
	}
	
	private String getErrorTable() {
		
		//Determine if there were any AdShots with errors (not including Creative not placed)
		Set<AdShot> problemAdShots = new LinkedHashSet<AdShot>();
		for (AdShot currentAdShot : _finishedCampaign.adShots()) {
			if ((currentAdShot.status().equals(AdShot.ERROR)) && 
				(!currentAdShot.errorMessage().equals(AdShot.CREATIVENOTINJECTED))) {
				problemAdShots.add(currentAdShot);
			}
		}
		
		//If no errors were found, return an empty string
		if (problemAdShots.size() == 0) {return "";}
					
		//If errors were found, begin by creating the title text
		String errorTitle = "<span " + TITLESTYLE + ">Issues: </span><br/>";
		errorTitle += "<p>Unfortunately, a problem occurred while processing the following screenshots. "
					 + "We have been notified of this issue and are looking into it. <br/>"
					 + "We apologize for this inconvenience and we appreciate your understanding "
					 + "while we investigate this matter.</p>";
		
		//Create the table that will show each AdShot and its information
		String errorTable = "<table border='1' cellpadding='3px' cellspacing='0' width='800px' style='border-collapse: collapse;'>";
		
		//Create the header row	
		errorTable += "<tr style='font-weight: bold'><th>Page</th>"
			   	  	  + "<th>Device</th>"
			   	  	  + "<th>Story Finder</th>"
				  	  + "<th>Below Fold</th>"
					  + "<th>Creative Sizes</tr>";		

		//Add the rows for Desktop AdShots that had an error
		for (AdShot currentAdShot : problemAdShots) {
			if (!currentAdShot.mobile()) {
				errorTable += getErrorTableRow(currentAdShot);
			}
		}
		
		//Add the rows for Mobile AdShots that had an error
		for (AdShot currentAdShot : problemAdShots) {
			if (currentAdShot.mobile()) {
				errorTable += getErrorTableRow(currentAdShot);
			}
		}
		
		errorTable += "</table><br/><br/>";
		
		//Return the final title and table
		return errorTitle + errorTable;
	}
	
	private String getErrorTableRow(AdShot targetAdShot) {
		
		//Create the string of requested creative sizes
		String creativeSizes = "";
		for (Creative currentCreative : targetAdShot.creatives()) {
			if (!creativeSizes.isEmpty()) {creativeSizes += ", ";}
			creativeSizes += currentCreative.width() + "x" + currentCreative.height();
		}
		
		//Create the cell text for the device and below-the-fold
		String deviceText = (targetAdShot.mobile()) ? "Mobile" : "Desktop";
		String storyFinderText = (targetAdShot.storyFinder()) ? "✓" : "";
		String btfText = (targetAdShot.belowTheFold()) ? "✓" : "";
		
		//Create the row HTML
		String rowHTML = "<tr>"
						   + "<td>" + targetAdShot.requestedURL() + "</td>"
						   + "<td style='text-align: center;'>" + deviceText + "</td>"
						   + "<td style='text-align: center;'>" + storyFinderText + "</td>"
						   + "<td style='text-align: center;'>" + btfText + "</td>"
						   + "<td style='text-align: center;'>" + creativeSizes + "</td>"
						   + "</tr>";
		
		return rowHTML;
	}
	
	private String getUnusedAdShotsTable() {
		
		//Determine if there were any AdShots with Creative not placed
		Set<AdShot> unusedAdShots = new LinkedHashSet<AdShot>();
		for (AdShot currentAdShot : _finishedCampaign.adShots()) {
			if ((currentAdShot.status().equals(AdShot.ERROR)) && 
				(currentAdShot.errorMessage().equals(AdShot.CREATIVENOTINJECTED))) {
				unusedAdShots.add(currentAdShot);
			}
		}
		
		//If no unused AdShots were found, return an empty string
		if (unusedAdShots.size() == 0) {return "";}
		
		//If unused AdShots were found, begin by creating the title text
		String unusedTitle = "<span " + TITLESTYLE + ">Screenshots Not Included: </span><br/>";
		unusedTitle += "<p>The following pages appear not to have an ad placement size matching "
					 + "the Creative size(s). <br/>"
					 + "They have not been included in the PowerPoint.</p>";
		
		//Create the table that will show each AdShot and its information
		String unusedTable = "<table border='1' cellpadding='3px' cellspacing='0' width='800px' style='border-collapse: collapse;'>";
		
		//Create the header row	
		unusedTable += "<tr style='font-weight: bold'><th>Page</th>"
			   	  	  + "<th>Device</th>"
				  	  + "<th>Below Fold</th>"
					  + "<th>Creative Sizes</tr>";		

		//Add the rows for Desktop AdShots that had an error
		for (AdShot currentAdShot : unusedAdShots) {
			if (!currentAdShot.mobile()) {
				unusedTable += getUnusedAdshotsTableRow(currentAdShot);
			}
		}
		
		//Add the rows for Mobile AdShots that had an error
		for (AdShot currentAdShot : unusedAdShots) {
			if (currentAdShot.mobile()) {
				unusedTable += getUnusedAdshotsTableRow(currentAdShot);
			}
		}
		
		unusedTable += "</table><br/><br/>";
		
		//Return the final title and table
		return unusedTitle + unusedTable;
	}
	
	private String getUnusedAdshotsTableRow(AdShot targetAdShot) {
		
		//Create the string of requested creative sizes
		String creativeSizes = "";
		for (Creative currentCreative : targetAdShot.creatives()) {
			if (!creativeSizes.isEmpty()) {creativeSizes += ", ";}
			creativeSizes += currentCreative.width() + "x" + currentCreative.height();
		}
		
		//Create the cell text for the device and below-the-fold
		String deviceText = (targetAdShot.mobile()) ? "Mobile" : "Desktop";
		String btfText = (targetAdShot.belowTheFold()) ? "✓" : "";
		
		//Create the row HTML
		String rowHTML = "<tr>"
						   + "<td>" + targetAdShot.finalURL() + "</td>"
						   + "<td style='text-align: center;'>" + deviceText + "</td>"
						   + "<td style='text-align: center;'>" + btfText + "</td>"
						   + "<td style='text-align: center;'>" + creativeSizes + "</td>"
						   + "</tr>";
		
		return rowHTML;
	}
	
	private String getCreativeNamesTable() {
		
		//Get the tag image names
		Map<Creative, String> creativeImageNames = getInjectedCreativeNames();
		
		//Create the table that will show each AdShot page and the names of the injected Creatives
		String tagImagesTable = "<table border='1' cellpadding='3px' cellspacing='0' width='800px' style='border-collapse: collapse;'>";
		tagImagesTable += 	    "<tr style='font-weight: bold'><th>Name</th><th>Image</th></tr>";		

		//Add each injected tag to the table
		for (Map.Entry<Creative, String> creativeImageName : creativeImageNames.entrySet()) {
			tagImagesTable += "<tr><td>" + creativeImageName.getValue() + "</td>" + 
								  "<td><img src='" + creativeImageName.getKey().imageURL() + "'></td></tr>";
		}
		tagImagesTable += "</table>";
		
		//Create the title text
		String titleText = "<span " + TITLESTYLE + ">Creative: </span><br/><br/>";
		
		return titleText + tagImagesTable;
	}
	
}
