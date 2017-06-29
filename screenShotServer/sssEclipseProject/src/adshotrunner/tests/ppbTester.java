package adshotrunner.tests;

import adshotrunner.powerpoint.PowerPointBackground;

public class ppbTester {

	public static void main(String[] args) {
		
		PowerPointBackground testBackground = PowerPointBackground.getPowerPointBackground(3);
		System.out.println("ID: " + testBackground.id());
		System.out.println("Name: " + testBackground.name());
		System.out.println("Font Color: " + testBackground.fontColor());
		System.out.println("Original Filename: " + testBackground.originalFilename());
		System.out.println("Filename: " + testBackground.filename());
		System.out.println("URL: " + testBackground.url());
		System.out.println("Thumbnail Filename: " + testBackground.thumbnailFilename());
		System.out.println("Thumbnail URL: " + testBackground.thumbnailURL());
		System.out.println("Timestamp: " + testBackground.timestamp());
		System.out.println("Archived: " + testBackground.archived());
	}

}
