package adshotrunner;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Used to unit test AdShotRunner
 *
 */
public class AdShotTester {

	public static void main(String[] args) throws IOException {
		
		ArrayList<String> tagURLs = new ArrayList<String>();
		tagURLs.add("chicagotribune.com");
		tagURLs.add("latimes.com");
		tagURLs.add("omaha.com");
		tagURLs.add("nydailynews.com");
		tagURLs.add("nypost.com");
		tagURLs.add("washingtonpost.com");
		tagURLs.add("suntimes.com");
		tagURLs.add("denverpost.com");
		tagURLs.add("dallasnews.com");
		tagURLs.add("nj.com");
		//tagURLs.add("philly.com");
		//tagURLs.add("www.sfgate.com");
		//tagURLs.add("xxxxxxxxxx.com");
		//tagURLs.add("boston.com");
		//tagURLs.add("www.usatoday.com");
		//tagURLs.add("nytimes.com");
		//tagURLs.add("slashdot.org");
		//tagURLs.add("online.wsj.com");
		
		ArrayList<String> fillerTags = getFillerTags();
		
		AdShotter testRunner = new AdShotter();
		
		for(String currentURL : tagURLs) {
			for(String currentTag: fillerTags) {
				testRunner.addTag(currentURL, currentTag);
			}
		}

		
		/*testRunner.addTag("boston.com", "https://s3.amazonaws.com/asr-tagimages/184x90.jpg", 0);
		testRunner.addTag("boston.com", "https://s3.amazonaws.com/asr-tagimages/728x90.gif", 0);
		testRunner.addTag("latimes.com", "https://s3.amazonaws.com/asr-tagimages/728x90.gif", 0);
		testRunner.addTag("omaha.com", "https://s3.amazonaws.com/asr-tagimages/300x250.gif", 0);
		//testRunner.addTag("omaha.com", "https://s3.amazonaws.com/asr-tagimages/300x250-2.jpeg", 1);
		testRunner.addTag("slashdot.org", "https://s3.amazonaws.com/asr-tagimages/300x250-2.jpeg", 1);*/

		/*testRunner.addTag("boston.com", "https://s3.amazonaws.com/asr-tagimages/184x90.jpg", 0);
		testRunner.addTag("boston.com", "https://s3.amazonaws.com/asr-tagimages/728x90.gif", 0);
		testRunner.addTag("boston.com", "https://s3.amazonaws.com/asr-tagimages/300x250.gif", 0);
		
		testRunner.addTag("latimes.com", "https://s3.amazonaws.com/asr-tagimages/184x90.jpg", 0);
		testRunner.addTag("latimes.com", "https://s3.amazonaws.com/asr-tagimages/728x90.gif", 0);
		testRunner.addTag("latimes.com", "https://s3.amazonaws.com/asr-tagimages/300x250.gif", 0);
		
		testRunner.addTag("omaha.com", "https://s3.amazonaws.com/asr-tagimages/184x90.jpg", 0);
		testRunner.addTag("omaha.com", "https://s3.amazonaws.com/asr-tagimages/728x90.gif", 0);
		testRunner.addTag("omaha.com", "https://s3.amazonaws.com/asr-tagimages/300x250.gif", 0);
		//testRunner.addTag("omaha.com", "https://s3.amazonaws.com/asr-tagimages/300x250-2.jpeg", 1)
		;
		testRunner.addTag("nytimes.com", "https://s3.amazonaws.com/asr-tagimages/184x90.jpg", 1);
		testRunner.addTag("nytimes.com", "https://s3.amazonaws.com/asr-tagimages/728x90.gif", 1);
		testRunner.addTag("nytimes.com", "https://s3.amazonaws.com/asr-tagimages/300x250.gif", 1);*/
		
		testRunner.getAdShots();
		
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
