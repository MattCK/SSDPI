package adshotrunner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

public class ComponentTester {

	public static void main(String[] args) throws IOException, URISyntaxException {
		
		String newsSite = "latimes.com";
		
		System.out.println("Retrieving random section for " + newsSite + "...");
		URL newsSiteURL = new URL("http://54.83.22.192/tools/menuItemGrabber.php?url=" + newsSite);
		String newsSectionURL = IOUtils.toString(newsSiteURL.openStream(), StandardCharsets.UTF_8.name());
		if (newsSectionURL.charAt(0) == '/') {newsSectionURL = "http://" + newsSite + newsSectionURL;}
		
		System.out.println("Site section: " + newsSectionURL);
		System.out.println("Getting random story...");
		
		String storyURL = new StoryFinder(newsSectionURL).Scorer().getStory();
		storyURL = storyURL.substring(1, storyURL.length() - 1);
		
		System.out.println("Story: " + storyURL);
		System.out.println("Injecting Ads and taking screenshot...");
		
		AdShotter testRunner = new AdShotter();
		
		ArrayList<String> fillerTags = getFillerTags();
		for(String currentTag: fillerTags) {
			testRunner.addTag(storyURL, currentTag);
		}
		testRunner.addTag(storyURL, "https://s3.amazonaws.com/asr-tagimages/fillers/filler-300x250-B.jpg", 2);
		testRunner.addTag(storyURL, "https://s3.amazonaws.com/asr-tagimages/fillers/filler-300x250-C.jpg", 3);
		testRunner.addTag(storyURL, "https://s3.amazonaws.com/asr-tagimages/fillers/filler-728x90-B.jpg", 2);
		testRunner.addTag(storyURL, "https://s3.amazonaws.com/asr-tagimages/fillers/filler-728x90-C.jpg", 3);
		
		testRunner.getAdShots();
		
		System.out.println("Done!");
		
		
		
		
	}
		
public static ArrayList<String> getFillerTags() {
		
		ArrayList<String> tagList = new ArrayList<String>();
		
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-120x240.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-120x60.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-120x600.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-120x90.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-125x125.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-160x600.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-180x150.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-234x60.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-240x400.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-250x250.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-300x250.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-336x280.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-468x60.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-728x90.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-88x31.jpg");
		
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-120x30.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-230x33.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-300x600.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-500x350.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-550x480.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-720x300.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-728x210.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-94x15.jpg");
				
		return tagList;
	}	
	
	
	
}
