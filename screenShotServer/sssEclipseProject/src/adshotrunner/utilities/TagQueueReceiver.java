package adshotrunner.utilities;

import java.util.HashMap;
import java.util.Map;

public class TagQueueReceiver {

	public static void main(String[] args) {
		
		while (true) {
			HashMap<String, String> receivedMessages = MessageQueueClient.getMessages(MessageQueueClient.TAGIMAGEREQUESTS);
			for (Map.Entry<String, String> entry : receivedMessages.entrySet()) {
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
