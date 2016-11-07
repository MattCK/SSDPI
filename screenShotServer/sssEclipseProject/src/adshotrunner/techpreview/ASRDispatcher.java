package adshotrunner.techpreview;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import adshotrunner.utilities.MessageQueueClient;

public class ASRDispatcher {

	public static void main(String[] args) {
		while (true) {
			HashMap<String, String> receivedMessages = new HashMap<String, String>();
			try {receivedMessages = MessageQueueClient.getMessages(MessageQueueClient.TAGIMAGEREQUESTS);}
			catch (Exception e) {} //Do nothing. A connection error occurs seldomly but regularly
			for (Map.Entry<String, String> entry : receivedMessages.entrySet()) {
				String requestID = "";
				List<String> requestURLs = new ArrayList<String>();
				
				try {
					Gson gson = new Gson();
					Type stringStringMapToken = new TypeToken<HashMap<String, String>>(){}.getType();
					Map<String,String> urlsWithIDs = gson.fromJson(entry.getValue(), stringStringMapToken);
					requestURLs = new ArrayList<String>(urlsWithIDs.values());
					System.out.println("URLS: " + requestURLs);
					
					new TagImager(urlsWithIDs);
				} catch (Exception e) {System.out.println(e);}
								
				MessageQueueClient.deleteMessage(MessageQueueClient.TAGIMAGEREQUESTS, entry.getKey());
			}

			try {receivedMessages = MessageQueueClient.getMessages(MessageQueueClient.SCREENSHOTREQUESTS);}
			catch (Exception e) {receivedMessages = new HashMap<String, String>();} //Do nothing. A connection error occurs seldomly but regularly
			for (Map.Entry<String, String> entry : receivedMessages.entrySet()) {				
				try {
					CampaignRequest request = CampaignRequest.fromJSON(entry.getValue());
					new CampaignRunner(request);
				} catch (Exception e) {System.out.println(e);}
								
				MessageQueueClient.deleteMessage(MessageQueueClient.SCREENSHOTREQUESTS, entry.getKey());
			}
			
			//Wait two seconds before checking the queues again
        	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
