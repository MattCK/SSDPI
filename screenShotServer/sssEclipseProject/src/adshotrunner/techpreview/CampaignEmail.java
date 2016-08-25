package adshotrunner.techpreview;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import adshotrunner.AdShot;
import adshotrunner.TagImage;
import adshotrunner.utilities.EmailClient;

public class CampaignEmail {

	public static boolean sendEmail(String customerName, ArrayList<AdShot> adShots, 
									String adShotsURL, String powerPointURL,
									String domain, String adShotDate, 
									String emailAddress) {
		
		//Verify something exists in the strings
		if ((customerName == null) || (customerName.isEmpty()) ||
			(adShotsURL == null) || (adShotsURL.isEmpty()) ||
			(powerPointURL == null) || (powerPointURL.isEmpty()) ||
			(domain == null) || (domain.isEmpty()) ||
			(adShotDate == null) || (adShotDate.isEmpty()) ||
			(emailAddress == null) || (emailAddress.isEmpty())) {
			return false;
		}
		
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
	}
	
}
