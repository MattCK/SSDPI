package adshotrunner.tests;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import adshotrunner.storyfinder.StoryFinder;

public class StoryCacheTester {

	public static void main(String[] args) throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {

		
		
//		List<String> newStories = new ArrayList<String>();
//		newStories.add("http://www.example.com/1");
//		newStories.add("http://www.example.com/2");
//		newStories.add("http://www.example.com/3");
//		new StoryFinder("http://www.example.com").storeStoriesInDatabaseCache(newStories);
//		
//		List<String> cachedStories = new StoryFinder("http://www.example.com").getStoriesFromDatabaseCache();
//	
		List<String> stories = StoryFinder.getStories("http://omaha.com/sports", 3);
		System.out.println("Found stories: " + stories.size());	
		for (String currentStory : stories) {
			System.out.println(currentStory);
		}
	}

}
