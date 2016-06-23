package adshotrunner.techpreview;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import adshotrunner.utilities.MessageQueueClient;

public class RequestResponder {

	public static void main(String[] args) {
		while (true) {
			HashMap<String, String> receivedMessages = MessageQueueClient.getMessages(MessageQueueClient.TAGIMAGEREQUESTS);
			for (Map.Entry<String, String> entry : receivedMessages.entrySet()) {
				String requestID = "";
				List<String> requestURLs = new ArrayList<String>();
				
				System.out.println("before all of it");
				try {
					System.out.println("in try");
					Gson gson = new Gson();
					System.out.println("made GSON");
					Type stringStringMapToken = new TypeToken<HashMap<String, String>>(){}.getType();
					Map<String,String> urlsWithIDs = gson.fromJson(entry.getValue(), stringStringMapToken);
					System.out.println("got map");
					requestURLs = new ArrayList<String>(urlsWithIDs.values());
					System.out.println("URLS: " + requestURLs);
					
					new TagImager(urlsWithIDs);
				} catch (Exception e) {System.out.println(e);}
				
				
				System.out.println("Message: " + entry.getValue());
				
				MessageQueueClient.deleteMessage(MessageQueueClient.TAGIMAGEREQUESTS, entry.getKey());
			}

			receivedMessages = MessageQueueClient.getMessages(MessageQueueClient.SCREENSHOTREQUESTS);
			for (Map.Entry<String, String> entry : receivedMessages.entrySet()) {
				List<String> requestURLs = new ArrayList<String>();
				
				try {
					System.out.println("Creating request object");
					CampaignRequest request = CampaignRequest.fromJSON(entry.getValue());
					System.out.println("Running campaign runner thread");
					new CampaignRunner(request);
					System.out.println("Ran campaign runner");
				} catch (Exception e) {System.out.println(e);}
				
				
				System.out.println("Message: " + entry.getValue());
				
				MessageQueueClient.deleteMessage(MessageQueueClient.SCREENSHOTREQUESTS, entry.getKey());
			}

			
			//Request request = Request.fromJSON(jsonText);
			
        	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
