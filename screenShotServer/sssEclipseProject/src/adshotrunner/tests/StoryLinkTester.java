package adshotrunner.tests;

import java.util.ArrayList;
import java.util.List;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.storyfinder.StoryFinder;
import adshotrunner.storyfinder.StoryLink;
import adshotrunner.utilities.URLTool;

public class StoryLinkTester {

	public static void main(String[] args) {
		
		int NUMBEROFSTORIES = 3;
		
		String url = "http://bloomberg.com";

		List<String> originalStories = new ArrayList<String>();
		List<StoryLink> originalLinks = new ArrayList<StoryLink>();
		List<String> newStories = new ArrayList<String>();
		List<StoryLink> newLinks = new ArrayList<StoryLink>();
		
		//Get the original stories
		try {
			StoryFinder tempStoryFinder = new StoryFinder(URLTool.setProtocol("http", url));
			originalLinks = tempStoryFinder._links;
			originalStories = tempStoryFinder.Scorer().getStories(NUMBEROFSTORIES);
		}
		catch (Exception e) {
			System.out.println("Unable to find original stories");
        	throw new AdShotRunnerException("Unable to find original stories", e);
		}

		//Get the new stories
		try {
			StoryFinder tempNewStoryFinder = new StoryFinder(URLTool.setProtocol("http", url), 1366, 768, true);
			newLinks = tempNewStoryFinder._links;
			newStories = tempNewStoryFinder.Scorer().getStories(NUMBEROFSTORIES);
		}
		catch (Exception e) {
			System.out.println("Unable to find new stories");
        	throw new AdShotRunnerException("Unable to find new stories", e);
		}
		
		//----------------------------------------------------------------------------------
		//Compare the number of links
		System.out.println("Original Links: " + originalLinks.size() + ", New Links: " + newLinks.size());
//		for (int linkIndex = 0; linkIndex < originalLinks.size(); ++linkIndex) {
//			System.out.println(originalLinks.get(linkIndex).xPosition + ", " + originalLinks.get(linkIndex).yPosition);
//			System.out.println(newLinks.get(linkIndex).xPosition + ", " + newLinks.get(linkIndex).yPosition);
//		}
		//----------------------------------------------------------------------------------

		
		//Compare the two sets of stories
		if (originalStories.size() != newStories.size()) {
			System.out.println("Different amounts of stories!");
		}
		else if (originalStories.size() == 0) {
			System.out.println("No stories found at all!");
		}
		else {
			
			//Loop through the stories and compare each
			boolean mismatchFound = false;
			for (int storyIndex = 0; storyIndex < originalStories.size(); ++storyIndex) {
				
				if (!originalStories.get(storyIndex).equals(newStories.get(storyIndex))) {
					mismatchFound = true;
					System.out.println("Mismatch found!");
					System.out.println(originalStories.get(storyIndex));
					System.out.println(newStories.get(storyIndex));
				}
			}
			
			if (mismatchFound) {System.out.println("The stories are not the same!");}
			
		}
		
		
		
		System.out.println("Done comparing");
	}
	
}






