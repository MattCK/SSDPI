package adshotrunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The StoryChecker scores the text of a passed URL according to how negative its
 * contents is.
 * 
 * The target URL is passed through a static function and a number returned. The 
 * higher the number, the more negative the content.
 */
public class StoryChecker {
	
	/*
	 * Returns a score for the passed url signifying the amount of negative
	 * words found in its HTML.
	 * 
	 * Before examination, all of the anchors are removed in order to reduce
	 * their titles affecting the current story's score.
	 * 
	 * @param targetURL		The URL (story) to get a score from
	 * 
	 * @return				Score for passed URL as a count of the negative words
	 */
	public static int score(String targetURL) {
		
		//Get the URL HTML minus the anchors
		String urlHTML = getTextFromURL(targetURL);
		
		//Get the negative words and add 1 to the score for each occurrence.
		ArrayList<String> negativeWordList = getNegativeWords();
		int score = 0;
		for (String currentWord : negativeWordList) {
			
		    Pattern currentWordPattern = Pattern.compile("(?i)" + currentWord);
		    Matcher wordMatcher = currentWordPattern.matcher(urlHTML);
		    while (wordMatcher.find()){
		    	score += 1;
		    }
		}	    
	    
		return score;
	}
	
	/**
	 * Returns a JSON string of the HTML without anchors from the passed URL
	 * 
	 * @param url			URL to grab the HTML from
	 * @return				String of HTML without anchors from the URL
	 */
	private static String getTextFromURL(String url) {
		
		//Try to make the phantomjs call and return the JSON
        String phantomJSResponse = null;
        try {
            
        	//Run the retrieve anchors js file with phantomjs
            Process p = Runtime.getRuntime().exec(new String[]{
	            "./phantomjs", 
	            "retrieveHTMLWithoutAnchorsFromURL.js",
	            url        	
            });
            
            //Get the string returned from phantomjs
            BufferedReader commandLineInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            phantomJSResponse = commandLineInput.readLine();
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
		
        //If the command was successful, return the phantomjs response
		return phantomJSResponse;
	}
	
	/*
	 * Simply returns an ArrayList of negative words that could appear in a story.
	 * 
	 * @return		List of negative words that could appear in a story
	 */
	private static ArrayList<String> getNegativeWords() {
		
		ArrayList<String> negativeWordList = new ArrayList<String>();
		negativeWordList.add("murder");
		negativeWordList.add("homicide");
		negativeWordList.add("death");
		negativeWordList.add("kill");
		negativeWordList.add("manslaughter");
		negativeWordList.add("rape");
		negativeWordList.add("dui");
		negativeWordList.add("heroin");
		negativeWordList.add("cocaine");
		negativeWordList.add("meth");
		negativeWordList.add("lsd");
		negativeWordList.add("angel dust");
		negativeWordList.add("mescaline");
		negativeWordList.add("slaying");
		negativeWordList.add("massacre");
		negativeWordList.add("school shooting");
		negativeWordList.add("mass killing");
		negativeWordList.add("mass murder");
		negativeWordList.add("genocide");
		negativeWordList.add("holocaust");
		negativeWordList.add("abortion");
		negativeWordList.add("reported missing");
		negativeWordList.add("tragedy");
		negativeWordList.add("armed man");
		negativeWordList.add("armed woman");
		negativeWordList.add("body found");
		negativeWordList.add("bomb threat");
		negativeWordList.add("epidemic");
		negativeWordList.add("die");
		negativeWordList.add("hospitalized");
		negativeWordList.add("collapsed in fire");
		negativeWordList.add("building collapse");
		negativeWordList.add("child abuse");
		negativeWordList.add("kidnapping");
		negativeWordList.add("sexual abuse");
		negativeWordList.add("criminal");
		negativeWordList.add("bus collision");
		negativeWordList.add("jihad");
		negativeWordList.add("drone strike");
		negativeWordList.add("missile strike");
		negativeWordList.add("hit and run");
		negativeWordList.add("dismember");
		negativeWordList.add("missing girl");
		negativeWordList.add("missing boy");
		negativeWordList.add("sex offender");
		negativeWordList.add("preyed upon");
		negativeWordList.add("masturbate");
		negativeWordList.add("arson");
		negativeWordList.add("stabbing");
		negativeWordList.add("suicide");
		negativeWordList.add("critical condition");
		negativeWordList.add("prostitute");
		negativeWordList.add("sex worker");
		negativeWordList.add("gang bang");
		negativeWordList.add("shooting victim");
		negativeWordList.add("stabbing victim");
		negativeWordList.add("body found");
		negativeWordList.add("struck by car");
		negativeWordList.add("struck by bus");
		negativeWordList.add("struck by truck");
		negativeWordList.add("struck by motorcycle");
		negativeWordList.add("armed men");
		negativeWordList.add("robbe");

		return negativeWordList;
	}
	
	/*
	 * Private to force use of static functions.
	 */
	private StoryChecker() {}
	
	
}
