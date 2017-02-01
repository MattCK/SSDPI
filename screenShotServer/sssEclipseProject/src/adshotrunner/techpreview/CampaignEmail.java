package adshotrunner.techpreview;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import adshotrunner.AdShot;
import adshotrunner.TagImage;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.EmailClient;

public class CampaignEmail {
	
	final private String _customerName;
	final private String _campaignDomain;
	final private String _campaignDate;
	final private String _campaignResultsURL;
	final private String _powerPointURL;
	final private List<AdShot> _adShots;
	
	public static CampaignEmail createCampaignEmail(String customerName, String campaignDomain, 
													String campaignDate, 
												 	String campaignResultsURL, String powerPointURL,
												 	ArrayList<AdShot> adShots) {
		
		//All strings are required. If null or empty strings are passed, return null;
		if ((customerName == null) || (customerName.isEmpty()) ||
			(campaignDomain == null) || (campaignDomain.isEmpty()) ||
			(campaignDate == null) || (campaignDate.isEmpty()) ||
			(campaignResultsURL == null) || (campaignResultsURL.isEmpty()) ||
			(powerPointURL == null) || (powerPointURL.isEmpty())) {
			return null;
		}
		
		//Otherwise, construct the CampaignEmail and return it
		return new CampaignEmail(customerName, campaignDomain, campaignDate, 
								 campaignResultsURL, powerPointURL, adShots);
	}
	
	private CampaignEmail(String customerName, String campaignDomain, String campaignDate, 
				  		  String campaignResultsURL, String powerPointURL,
				  		  ArrayList<AdShot> adShots) {

		_customerName = customerName;
		_campaignDomain = campaignDomain;
		_campaignDate = campaignDate;
		_campaignResultsURL = campaignResultsURL;
		_powerPointURL = powerPointURL;
		_adShots = adShots;
	}

	public String getHTMLText() {
		
		//Create the links for the powerpoint and campaign results
		String resultLinks = "PowerPoint:  <a href='" + _powerPointURL + "'>Download</a><br>" + 
						     "Screenshots: <a href='" + _campaignResultsURL + "'>View All</a>";
		
		//Put the email parts together
		return "<p>" + getIntroText() + "</p><p>" + resultLinks + "</p><br><br>" + 
				getAdShotInjectedTagImagesTable() + "<br><br>" +
				getTagImageNamesTable();
	}
	
	public String getPlainText() {
		
		//Create the links for the powerpoint and campaign results
		String resultLinks = "PowerPoint: " + _powerPointURL + " \n\n" + 
						     "Screenshots: " + _campaignResultsURL;
		
		//Put the email parts together
		return getIntroText() + "\n\n" + resultLinks;
	}
	
	public void send(String emailAddress) {

		//Create the subject line
		String emailSubject = "AdShotRunner™ Campaign Finished: " + 
							  _customerName + " - " + _campaignDomain + " - " + _campaignDate;

		//Send the email
		EmailClient.sendEmail(ASRProperties.emailAddressScreenshots(), emailAddress, emailSubject, 
															 		   getPlainText(), getHTMLText());
	}
	
	private String getIntroText() {
		
		return "AdshotRunner™ has finished the screenshots for your campaign: " + _customerName + " - " +
				_campaignDomain + " - " + _campaignDate;
	}
	
	/**
	 * Creates a map pairing each injected TagImage with its name.
	 * 
	 * 1) If there is only one injected TagImage of a certain dimension, it is named
	 * 	  the dimension. (WIDTHxHEIGHT)
	 * 
	 * 2) If more than one injected TagImage of the same dimension exist, they are named the
	 *    dimension with a letter suffix (-A, -B, -C, etc.) The order begins with the injected
	 *    TagImage with the highest priority.
	 *    
	 * All names are unique.
	 * 
	 * @return	Map of injected TagImages with their unique individual names.
	 */
	private Map<TagImage, String> getInjectedTagImageNames() {
		
		//Group all of the injected TagImages into sets of the same dimensions.
		//The sets are keyed to their dimensions string (WIDTHxHEIGHT)
		//Loop through each of the AdShots and add their injected TagImages into their proper set
		Map<String, Set<TagImage>> tagImageSetsByDimension = new TreeMap<String, Set<TagImage>>();
		for (AdShot currentAdShot : _adShots) {
			
			//Loop through the injected TagImages of the current AdShot and add them to
			//the proper set. If the TagImage already exists there, nothing occurs.
			Set<TagImage> injectedTagImages = currentAdShot.injectedTagImages();
			for (TagImage currentTagImage : injectedTagImages) {
				
				//Define the dimensions key for the TagImage
				String tagDimensionKey = currentTagImage.width() + "x" + currentTagImage.height();
				
				//If the set for the current dimensions key does not exist, create it
				if (!tagImageSetsByDimension.containsKey(tagDimensionKey)) {
					tagImageSetsByDimension.put(tagDimensionKey, new TreeSet<TagImage>());
				}
				
				//Add the TagImage into the set of its dimensions key.
				//If the TagImage already exists in the set, nothing occurs.
				tagImageSetsByDimension.get(tagDimensionKey).add(currentTagImage);
			}
		}
		
		//Name each tag according to its dimensions.
		//If only one TagImage of a give dimension exists, it is named the dimension (WIDTHxHEIGHT)
		//If more than one injected TagImage of the same dimension exist, they are named the
		//dimension with a letter suffix (-A, -B, -C, etc.) The order begins with the injected
		//TagImage with the highest priority.		
		Map<TagImage, String> tagImageNames = new LinkedHashMap<TagImage, String>();
		for (Map.Entry<String, Set<TagImage>> tagGroup : tagImageSetsByDimension.entrySet()) {
			
			//Put the group info into unique variables for clarity
			String tagDimensions = tagGroup.getKey();
			Set<TagImage> tagImageSet = tagGroup.getValue();
			
			//Add the name for each TagImage.
			//If more than one TagImage exist in a set, append "A", "B", etc. to each in the order they appear
			int letterIterator = 1;
			for (TagImage currentTagImage : tagImageSet) {
				
				//If there is only one TagImage in the set, use the dimensions as its name
				if (tagImageSet.size() <= 1) {tagImageNames.put(currentTagImage, tagDimensions);}
				
				//If more than one TagImage exists, append letters to the dimensions
				else {
					String currentLetter = String.valueOf((char)(letterIterator + 64));
					tagImageNames.put(currentTagImage, tagDimensions + "+" + currentLetter);
				}
				
				//Increment the letter iterator
				++letterIterator;
			}
		}		
		
		return tagImageNames;
	}
	
	private String getAdShotInjectedTagImagesTable() {
		
		//Get the tag image names
		Map<TagImage, String> tagImageNames = getInjectedTagImageNames();
		
		//Create the table that will show each AdShot page and the names of the injected TagImages
		String adShotsTable = "<table border='1' style='border-collapse: collapse;'>";
		adShotsTable += 	  "<tr style='font-weight: bold'><th>Page</th><th>Tags Used</th></tr>";		
		
		//For each adshot, add a row that shows which tag images, if any, were injected
		for (AdShot currentAdShot : _adShots) {
			
			//Create the string of injected tag image names to be placed in the cell
			String injectedTagNames = "";
			for (TagImage currentTagImage : currentAdShot.injectedTagImages()) {
				if (!injectedTagNames.isEmpty()) {injectedTagNames += ", ";}
				injectedTagNames += tagImageNames.get(currentTagImage);
			}
			
			//Add the current AdShot page row to the table
			adShotsTable += "<tr><td>" + currentAdShot.finalURL() + "</td>" + 
								"<td>" + injectedTagNames + "</td></tr>";
		}
		adShotsTable += "</table>";
		return adShotsTable;
	}
	
	private String getTagImageNamesTable() {
		
		//Get the tag image names
		Map<TagImage, String> tagImageNames = getInjectedTagImageNames();
		
		//Create the table that will show each AdShot page and the names of the injected TagImages
		String tagImagesTable = "<table border='1' style='border-collapse: collapse;'>";
		tagImagesTable += 	    "<tr style='font-weight: bold'><th>Name</th><th>Image</th></tr>";		

		//Add each injected tag to the table
		for (Map.Entry<TagImage, String> tagImageName : tagImageNames.entrySet()) {
			tagImagesTable += "<tr><td>" + tagImageName.getValue() + "</td>" + 
								  "<td><img src='" + tagImageName.getKey().url() + "'></td></tr>";
		}
		tagImagesTable += "</table>";
		return tagImagesTable;
	}

	/*public String getHTMLText() {
		
		//Create the subject line
		String emailSubject = "AdShotRunner Campaign: " + customerName + " - " + domain + " - " + adShotDate;
		
		//Create the part of the email with the links
		String linksPart = "PowerPoint: " + powerPointURL + "<br>Screenshots: " + adShotsURL;
		
		//Group all the TagImages together
		//Each group of TagImages with the same dimensions is put into a set ordered by tag priority
		//Each TagImage set is placed in a map with the key a string of dimensionWidthxdimensionHeight
		Map<String, Set<TagImage>> allTags = new TreeMap<String, Set<TagImage>>();
		for (AdShot currentAdShot : adShots) {
			
			Set<TagImage> currentTagImages = currentAdShot.injectedTagImages();
			for (TagImage currentTagImage : currentTagImages) {
				String tagDimensions = currentTagImage.width() + "x" + currentTagImage.height();
				
				//If the set for the current dimensions does not exist, create it
				if (!allTags.containsKey(tagDimensions)) {
					allTags.put(tagDimensions, new TreeSet<TagImage>());
				}
				
				//Add the TagImage into the set of its dimensions
				allTags.get(tagDimensions).add(currentTagImage);
			}
		}
		
		//Name each tag its dimensions
		//If more than one tag exists with the same dimensions, a letter is added to each
		//Letters are added in order of tag priority with "A" being assigned to the highest priority, lowest number
		Map<TagImage, String> tagNames = new LinkedHashMap<TagImage, String>();
		for (Map.Entry<String, Set<TagImage>> tagGroup : allTags.entrySet()) {
			
			//Put the group info into unique variables for clarity
			String tagDimensions = tagGroup.getKey();
			Set<TagImage> tagImageSet = tagGroup.getValue();
			
			//Add the name for each TagImage.
			//If more than one TagImage exist in a set, append "A", "B", etc. to each in the order they appear
			int letterIterator = 1;
			for (TagImage currentTagImage : tagImageSet) {
				
				//If there is only one TagImage in the set, use the dimensions as its name
				if (tagImageSet.size() <= 1) {tagNames.put(currentTagImage, tagDimensions);}
				
				//If more than one TagImage exists, append letters to the dimensions
				else {
					String currentLetter = String.valueOf((char)(letterIterator + 64));
					tagNames.put(currentTagImage, tagDimensions + "+" + currentLetter);
				}
			}
		}		
		
		//Create the table that shows each AdShots URL and the names of the tags that were injected
		String adShotsTable = "<table>";
		for (AdShot currentAdShot : adShots) {
			
			//Create the string of injected tag image names
			String injectedTagNames = "";
			for (TagImage currentTagImage : currentAdShot.injectedTagImages()) {
				if (!injectedTagNames.isEmpty()) {injectedTagNames += ", ";}
				injectedTagNames += tagNames.get(currentTagImage);
			}
			
			//Add the AdShot row to the table
			adShotsTable += "<tr><td>" + currentAdShot.finalURL() + "</td>" + 
								"<td>" + injectedTagNames + "</td></tr>";
		}
		adShotsTable += "</table>";
		
		//Create the table that lists the tag images used
		String tagImagesTable = "<table>";
		for (Map.Entry<TagImage, String> tagImageName : tagNames.entrySet()) {
			tagImagesTable += "<tr><td>" + tagImageName.getValue() + "</td>" + 
								  "<td><img src='" + tagImageName.getKey().url() + "'></td></tr>";
		}
		tagImagesTable += "</table>";
		
		//Create the plain text and HTML bodies of the email
		String plainTextBody = linksPart.replace("<br>", "\n");
		String htmlBody = linksPart + "<br><br>" + adShotsTable + "<br><br>" + tagImagesTable;
		
		//Send the email
		EmailClient.sendEmail(EmailClient.SCREENSHOTADDRESS, emailAddress, emailSubject, 
															 plainTextBody, htmlBody);
		
		return true;
	}*/
	
}
