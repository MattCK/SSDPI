package adshotrunner.tests;

import adshotrunner.system.ASRProperties;

public class PropertiesTester {

	public static void main(String[] args) {
		System.out.println(ASRProperties.asrDatabaseUsername());
		System.out.println(ASRProperties.asrDatabasePassword());
		System.out.println(ASRProperties.asrDatabaseHost());
		System.out.println(ASRProperties.asrDatabase());
		
		System.out.println(ASRProperties.queueForTagImageRequests());
		System.out.println(ASRProperties.queueForScreenshotRequests());

	}

}
