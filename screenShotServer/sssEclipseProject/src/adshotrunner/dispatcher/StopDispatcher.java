package adshotrunner.dispatcher;

import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.MessageQueueClient;

/**
 * Sends the 'stop' command to the ASRDispatcher.
 */
public class StopDispatcher {

	public static void main(String[] args) {
		MessageQueueClient.sendMessage(ASRProperties.queueForDispatcherCommands(), "stop");
	}
}
