package adshotrunner.utilities;

import java.util.HashMap;
import java.util.Map;

public class UtilityTester {

	public static void main(String[] args) {
		/*try {
			String[] emailAddresses = {"asr@juiciobrennan.com", "asr2@juiciobrennan.com"};
			EmailClient.sendEmail(EmailClient.SCREENSHOTADDRESS, "asr@juiciobrennan.com", "email test", "text body", "html body");
			System.out.println("Email sent");
		}
		catch(Exception e) {
			System.out.println(e);
		}*/
		try {
			FileStorageClient.saveFile(FileStorageClient.TAGIMAGESCONTAINER, "ScreenShot1109332778578.png", "testUpload.png");
			System.out.println("Image uploaded");
			FileStorageClient.deleteFile(FileStorageClient.TAGIMAGESCONTAINER, "testUpload.png");
			System.out.println("Image deleted");
		}
		catch(Exception e) {
			System.out.println(e);
		}

		/*try {
			NotificationClient.sendNotice(NotificationClient.FRONTEND, "TESTER", "Message text");
			System.out.println("Notice sent");
		}
		catch(Exception e) {
			System.out.println(e);
		}*/
		try {
			//MessageQueueClient.sendMessage(MessageQueueClient.TAGIMAGEREQUESTS, "Message text");
			//System.out.println("Queue message sent");
		}
		catch(Exception e) {
			System.out.println(e);
		}
		/*try {
			HashMap<String, String> receivedMessages = MessageQueueClient.getMessages(MessageQueueClient.TAGIMAGEREQUESTS);
			boolean deletedOne = false;
			for (Map.Entry<String, String> entry : receivedMessages.entrySet()) {
				System.out.println("Message:" + entry.getKey() + " - " + entry.getValue());
				
				//while (!deletedOne) {
					MessageQueueClient.deleteMessage(MessageQueueClient.TAGIMAGEREQUESTS, entry.getKey());
					System.out.println("Deleted a message");
					deletedOne = true;
				//}
			}
			
		}
		catch(Exception e) {
			System.out.println(e);
		}*/
	}

}
