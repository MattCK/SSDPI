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
				/*try {
					JSONObject messageObject = new JSONObject(entry.getValue());
					requestID = messageObject.getString("id");
	
					JSONArray urlArray = messageObject.getJSONArray("urls");
					for (int i = 0; i < urlArray.length(); i++) {
						requestURLs.add(urlArray.getString(i));
					}

					System.out.println("ID is: " + requestID);
					System.out.println("URLS: " + requestURLs);
					
					new TagImager(requestID, requestURLs);
				} catch (Exception e) {}*/
				
				System.out.println("before try");
				try {
					System.out.println("in try");
					Gson gson = new Gson();
					System.out.println("made GSON");
					Type stringStringMap = new TypeToken<HashMap<String, String>>(){}.getType();
					Map<String,String> urlsWithIDs = gson.fromJson(entry.getValue(), stringStringMap);
					System.out.println("got map");
					requestURLs = new ArrayList<String>(urlsWithIDs.values());
					System.out.println("URLS: " + requestURLs);
					
					new TagImager(urlsWithIDs);
				} catch (Exception e) {System.out.println(e);}
				
				
				System.out.println("Message: " + entry.getValue());
				
				MessageQueueClient.deleteMessage(MessageQueueClient.TAGIMAGEREQUESTS, entry.getKey());
			}
			
        	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
