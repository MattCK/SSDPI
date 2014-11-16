package adshotrunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.net.InternetDomainName;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The StoryFinder class attempts to retrieve a feature story from the passed URL. This story
 * must be prominent on the page and not focus on a negative topic such as a murder.
 * 
 * The target URL is passed through the constructor. All anchors on the page and their info are retrieved.
 * The resulting object is immutable.
 * 
 * The story is retrieved through the nested class Scorer. Scorer has the option of
 * setting the many variables used to calculate the best story. 
 */
public class StoryFinder {
	
	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Private Static Methods **********************************
	/**
	 * Returns a JSON string of all the anchors and their info from the passed URL
	 * 
	 * Anchor info: id, href, name, onclick, text, style, title
	 * 
	 * @param url			URL to grab the anchors from
	 * @param viewWidth		Width of the screen phantomjs should use
	 * @param viewHeight	Height of the screen phantomjs should use
	 * @param viewHeight	URL to grab the anchors from
	 * @return				JSON string of anchors and their info
	 */
	private static String getAnchorJSONFromURL(String url, int viewWidth, int viewHeight) {
		
		//Try to make the phantomjs call and return the JSON
        String phantomJSResponse = null;
        try {
            
        	//Run the retrieve anchors js file with phantomjs
            Process p = Runtime.getRuntime().exec(new String[]{
	            "phantomjs/phantomjs", 
	            "javascript/retrievePossibleStoriesFromURL.js",
	            url, Integer.toString(viewWidth), Integer.toString(viewHeight)        	
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
	
	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	 * URL to find a story on
	 */
	private final String _targetURL;
	
	/**
	 * All anchors on the target URL and their info (id, href, name, onclick, text, style, title) (immutable)
	 */
	private final List<Map<String, String>> _anchors;
	
	/**
	 * Width to set phantomjs screen to before grabbing anchors
	 */
	private final int _screenWidth;
	
	/**
	 * Height to set phantomjs screen to before grabbing anchors
	 */
	private final int _screenHeight;
	
	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Returns a StoryFinder instance with the anchors retrieved from the passed URL
	 * 
	 * The screen is automatically set to 1024x768.
	 * 
	 * Instance is immutable
	 * 
	 * @param url	URL to retrieve stories from
	 * 
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException 
	 */
	StoryFinder(String url) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		
		//Create the instance with the default 1024x768 phantomjs screen
		this(url, 1024, 768);
	}

	/**
	 * Returns a StoryFinder instance with the anchors retrieved from the passed URL, utilizing the
	 * screen size passed.
	 * 
	 * Instance is immutable
	 * 
	 * @param url			URL to retrieve stories from 
	 * @param viewWidth		Width of the screen phantomjs should use
	 * @param viewHeight	Height of the screen phantomjs should use
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException 
	 */
	StoryFinder(String url, int viewWidth, int viewHeight) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		
		//Store the target URL for the class to get stories from
		_targetURL = url;
		
		//Store the height and width to use for the phantomjs screen
		_screenWidth = viewWidth;
		_screenHeight = viewHeight;
		
		//Get the possible story links using phantomjs
		String anchorJSON = getAnchorJSONFromURL(_targetURL, _screenWidth, _screenHeight);
                
        //Get the immutable anchor info as array of maps
		_anchors = getImmutableAnchorInfoFromJSON(anchorJSON);
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Private Methods *************************************
	/**
	 * Returns an immutable list of anchors and their info (each anchor an immutable map)
	 * 
	 * @param anchorJSON	JSON string of anchors as their info in the form of an array of associative arrays
	 * @return				Immutable list of immutable maps, each map a set of anchor info (text, href, xPosition, etc...)
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException 
	 */
	private List<Map<String, String>> getImmutableAnchorInfoFromJSON(String anchorJSON) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		
		//Turn the returned JSON into an array of objects
		JsonArray anchorsArray = new Gson().fromJson(anchorJSON, JsonArray.class);
		
		//Get the primary domain of the target URL
		String urlDomain = getURIDomain(_targetURL);
		
		//Convert the data into an ArrayList of immutable anchor HashMaps
		ArrayList<Map<String, String>> anchorsList = new ArrayList<Map<String, String>>();
        for(int i = 0; i < anchorsArray.size(); i++){
        	
        	//Convert the current array item into an anchor object
            JsonObject curAnchor = anchorsArray.get(i).getAsJsonObject();
           
            //As long as the href domain is the same as the target site, add the anchor info. Otherwise, ignore.
            if (!curAnchor.get("href").isJsonNull()){
            	String currentDomain = getURIDomain(curAnchor.get("href").getAsString());
            
	            if ((currentDomain == "") || (urlDomain.equals(currentDomain))) {
	            
		            //Put all the anchor attributes into a map
		            HashMap<String, String> anchorMap = new HashMap<String, String>();
					for (Map.Entry<String,JsonElement> anchorAttribute : curAnchor.entrySet()) {
						anchorMap.put(anchorAttribute.getKey(), anchorAttribute.getValue().toString());
					}
					
					//Make the map immutable and place it into the final arraylist
					Map<String, String> immutableAnchorMap = Collections.unmodifiableMap(anchorMap); 
					anchorsList.add(immutableAnchorMap);
	            }
            }
        }
        
        //Make the anchorsList immutable and return it
		List<Map<String, String>> immutableAnchorList = Collections.unmodifiableList(anchorsList);  
		return immutableAnchorList;
	}
	
	/**
	 * Returns the domain of the URI string. The domain does not include any subdomain or any protocol (i.e. http://)
	 * 
	 * @param URIString		URI to retrieve domain from
	 * @return				Domain of URI
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException 
	 * @throws MalformedURLException 
	 */
	private String getURIDomain(String URIString) throws URISyntaxException, UnsupportedEncodingException, MalformedURLException {
		URIString = URIString.replace("\"", "");
		if(URIString.contains("://") && ((URIString.contains("http") || URIString.contains("spdy"))) && (!URIString.startsWith("javascript"))){

			URL urlObject = new URL(URIString);
			String nullFragment = null;
			URI uriObject = new URI(urlObject.getProtocol(), urlObject.getHost(), urlObject.getPath(), urlObject.getQuery(), nullFragment);
			if (uriObject.getHost() == null) {return "";}
			return InternetDomainName.from(uriObject.getHost()).topPrivateDomain().toString();
		}
		else{	
			return "";
		}
	}
	
	/**
	 * Simply returns a new Scorer attached to this StoryFinder instance.
	 * 
	 * Exists for code simplicity and clarity.
	 * 
	 * @return		New scorer object attached to this StoryFinder instance
	 */
	public Scorer Scorer() {
		return new Scorer();
	}
	
	/**
	 * Calculates scores for each of the containing StoryFinder's anchors and returns 
	 * the most likely good story (highest score with highest class)
	 * 
	 * Each of the formula variables and scores can be set.
	 */
	class Scorer {		
		
		/**
		 * Score of the left most width point (complete left side of page). DEFAULT: 0
		 */
		private int POSITIONLEFTMOSTXSCORE;
		/**
		 * Score of the one third point of the screen width. DEFAULT: 10
		 */
		private int POSITIONONETHIRDXSCORE;
		/**
		 * Score of the one half point of the screen width. DEFAULT: 19
		 */
		private int POSITIONONEHALFXSCORE;
		/**
		 * Score of the optimal point of the screen width. DEFAULT: 20
		 */
		private int POSITIONOPTIMALXSCORE;
		/**
		 * Score of two one third point of the screen width. DEFAULT: 19
		 */
		private int POSITIONTWOTHIRDXSCORE;
		/**
		 * Score of the right most width point (complete right side of page). DEFAULT: 0
		 */
		private int POSITIONRIGHTMOSTXSCORE;
		
		/**
		 * Height in pixels of the topmost region (which begins at the very top, y=0, of the page). DEFAULT: 350
		 */
		private int TOPREGIONONEHEIGHT;
		/**
		 * Handicap for stories that lie in the topmost region (negative number). DEFAULT: -11
		 */
		private int TOPREGIONONEHANDICAP;
		/**
		 * Height in pixels of the second top region (which also begins at the very top, y=0, of the page). Must be greater than TOPREGIONSONEHEIGHT. DEFAULT: 475
		 */
		private int TOPREGIONTWOHEIGHT;
		/**
		 * Handicap for stories that lie in the second top region (negative number). DEFAULT: -6
		 */
		private int TOPREGIONTWOHANDICAP;
		
		/**
		 * Minimum text length, in characters, an anchor's title must be to be scored. DEFAULT: 3
		 */
		private int MINIMUMTEXTLENGTH;
		/**
		 * The length, in characters, of a short title text (where the title is equal to or less than this number). DEFAULT: 9
		 */
		private int SHORTTEXTLENGTH;
		/**
		 * Handicap for stories with titles considered short (negative number). DEFAULT: -10
		 */
		private int SHORTTEXTHANDICAP;
		/**
		 * The length, in characters, of a long title text (where the title is equal to or more than this number). DEFAULT: 16
		 */
		private int LONGTEXTLENGTH;
		/**
		 * The score added to anchor's with titles considered to be long text. DEFAULT: 11
		 */
		private int LONGTEXTSCORE;
		/**
		 * Minimum word count an anchor title must have not to receive a handicap. DEFAULT: 3
		 */
		private int MINIMUMWORDCOUNT;
		/**
		 * Handicap anchor receieves if its title does not meet the minimum word count (negative number). DEFAULT: -10
		 */
		private int MINIMUMWORDHANDICAP;
		
		/**
		 * The score added to anchor's where the first path part is the same as the target url (i.e. /entertainment, /sports). DEFAULT: 7
		 */
		private int SAMEPATHPARTSCORE;
		
		/**
		 * Handicap anchor receives if the title only contains capital letters (negative number). DEFAULT: 12
		 */
		private int ALLCAPSHANDICAP;
		
		/**
		 * Creates the instance and sets all of the variables to their default values
		 */
		Scorer() {
			
			//Set page position defaults
			POSITIONLEFTMOSTXSCORE = 0;
			POSITIONONETHIRDXSCORE = 10;
			POSITIONONEHALFXSCORE = 19;
			POSITIONOPTIMALXSCORE = 20;
			POSITIONTWOTHIRDXSCORE = 19;
			POSITIONRIGHTMOSTXSCORE = 0;
			
			//Set top region defaults
			TOPREGIONONEHEIGHT = 350;
			TOPREGIONONEHANDICAP = -11;
			TOPREGIONTWOHEIGHT = 475;
			TOPREGIONTWOHANDICAP = -6;
			
			//Set text attribute defaults
			MINIMUMTEXTLENGTH = 3;
			SHORTTEXTLENGTH = 9;
			SHORTTEXTHANDICAP = -10;
			LONGTEXTLENGTH = 16;
			LONGTEXTSCORE = 11;
			MINIMUMWORDCOUNT = 3;
			MINIMUMWORDHANDICAP = -10;
			
			//Set same path score
			SAMEPATHPARTSCORE = 7;
			
			//Set handicap for all caps
			ALLCAPSHANDICAP = -12;
		}
		
		
		/**
		 * Sets the score of the leftmost (0 pixel) position for page location scoring
		 * @param newValue	The new value of the leftmost position score
		 * @return 			Reference to the current class instance
		 */
		public Scorer positionLeftmostXScore(int newValue) {
			POSITIONLEFTMOSTXSCORE = newValue; return this;
		}
		/**
		 * Sets the score of the one-third position for page location scoring
		 * @param newValue	The new value of the one-third position score
		 * @return 			Reference to the current class instance
		 */
		public Scorer positionOneThirdXScore(int newValue) {
			POSITIONONETHIRDXSCORE = newValue; return this;
		}
		/**
		 * Sets the score of the one-half position for page location scoring
		 * @param newValue	The new value of the one-half position score
		 * @return 			Reference to the current class instance
		 */
		public Scorer positionOneHalfXScore(int newValue) {
			POSITIONONEHALFXSCORE = newValue; return this;
		}
		/**
		 * Sets the score of the optimal position for page location scoring
		 * @param newValue	The new value of the optimal position score
		 * @return 			Reference to the current class instance
		 */
		public Scorer positionOptimalXScore(int newValue) {
			POSITIONOPTIMALXSCORE = newValue; return this;
		}
		/**
		 * Sets the score of the two-third position for page location scoring
		 * @param newValue	The new value of the two-third position score
		 * @return 			Reference to the current class instance
		 */
		public Scorer positionTwoThirdXScore(int newValue) {
			POSITIONTWOTHIRDXSCORE = newValue; return this;
		}
		/**
		 * Sets the score of the rightmost position (far right of page) for page location scoring
		 * @param newValue	The new value of the rightmost position score
		 * @return 			Reference to the current class instance
		 */
		public Scorer positionRightmostXScore(int newValue) {
			POSITIONRIGHTMOSTXSCORE = newValue; return this;
		}
		
		/**
		 * Sets the height (in pixels) for the topmost region
		 * @param newValue	Height (in pixels) of topmost region
		 * @return 			Reference to the current class instance
		 */
		public Scorer topRegionOneHeight(int newValue) {
			TOPREGIONONEHEIGHT = newValue; return this;
		}
		/**
		 * Sets the negative handicap for a story in the topmost region
		 * @param newValue	Negative handicap for stories in the topmost region
		 * @return 			Reference to the current class instance
		 */
		public Scorer topRegionOneHandicap(int newValue) {
			TOPREGIONONEHANDICAP = newValue; return this;
		}
		/**
		 * Sets the height (in pixels) for the the second top region
		 * @param newValue	Height (in pixels) of second top region (must be greater than the topmost region height)
		 * @return 			Reference to the current class instance
		 */
		public Scorer topRegionTwoHeight(int newValue) {
			TOPREGIONTWOHEIGHT = newValue; return this;
		}
		/**
		 * Sets the negative handicap for a story in the second top region
		 * @param newValue	Negative handicap for stories in the second top region
		 * @return 			Reference to the current class instance
		 */
		public Scorer topRegionTwoHandicap(int newValue) {
			TOPREGIONTWOHANDICAP = newValue; return this;
		}

		/**
		 * Sets the minimum text character length a url title must be to be scored
		 * @param newValue	Minimum text length in characters that a url title must be
		 * @return 			Reference to the current class instance
		 */
		public Scorer minimumTextLength(int newValue) {
			MINIMUMTEXTLENGTH = newValue; return this;
		}
		/**
		 * Sets the length in characters that a title is considered short
		 * @param newValue	Text length in characters that defines a short title
		 * @return 			Reference to the current class instance
		 */
		public Scorer shortTextLength(int newValue) {
			SHORTTEXTLENGTH = newValue; return this;
		}
		/**
		 * Sets the negative handicap a url receives if it is considered short text
		 * @param newValue	Negative handicap to apply to url score if the url title is considered short text
		 * @return 			Reference to the current class instance
		 */
		public Scorer shortTextHandicap(int newValue) {
			SHORTTEXTHANDICAP = newValue; return this;
		}
		/**
		 * Sets the length in characters that a title is considered long
		 * @param newValue	Text length in characters that defines a long title
		 * @return 			Reference to the current class instance
		 */
		public Scorer longTextLength(int newValue) {
			LONGTEXTLENGTH = newValue; return this;
		}
		/**
		 * Sets the score a url receives if it is considered long text
		 * @param newValue	Score to apply to url score if the url title is considered long text
		 * @return 			Reference to the current class instance
		 */
		public Scorer longTextScore(int newValue) {
			LONGTEXTSCORE = newValue; return this;
		}
		/**
		 * Sets the minimum word count of a title (used to handicap short titles)
		 * @param newValue	Minimum word count of a title 
		 * @return 			Reference to the current class instance
		 */
		public Scorer minimumWordCount(int newValue) {
			MINIMUMWORDCOUNT = newValue; return this;
		}
		/**
		 * Sets the negative handicap a url receives if its title doesn't meet the minimum word count
		 * @param newValue	Negative handicap applied to urls with titles that don't meet the minimum word count 
		 * @return 			Reference to the current class instance
		 */
		public Scorer minimumWordHandicap(int newValue) {
			MINIMUMWORDHANDICAP = newValue; return this;
		}

		/**
		 * Sets the score a url receives if its href has the first path part same as the target url
		 * @param newValue	Score the url receives if its path's first part is the same as the target url
		 * @return 			Reference to the current class instance
		 */
		public Scorer samePathPartsScore(int newValue) {
			SAMEPATHPARTSCORE = newValue; return this;
		}

		/**
		 * Sets the negative handicap a url receives if its title is in all caps
		 * @param newValue	Negative handicap applied to urls with titles in call caps
		 * @return 			Reference to the current class instance
		 */
		public Scorer allCapsHandicap(int newValue) {
			ALLCAPSHANDICAP = newValue; return this;
		}

		/**
		 * Returns the best story URL from the containing StoryFinder's anchors.
		 * 
		 * Calculates for each anchor a score and returns the 'href' of the highest in the best 'class' group.
		 * (Determined by average score of anchors with that class.)
		 * 
		 * @return	URL of best story 
		 */
		public String getStory() {
			
			//Get all the anchor scores using the containing class anchors and the Scorer's numbers
			HashMap<Integer, Integer> anchorScores = getAnchorScores();
			
			//Get a ranked list of the anchors' classes based off each classes anchors' averages
			ArrayList<String> rankedClasses = getClassesRankedByAveragedAnchorScore(anchorScores);
			
			//Get the story with the highest score and with the highest ranked class
			String storyURL = "";
			int highestScore = 0;
			for (Map.Entry<Integer, Integer> currentScore : anchorScores.entrySet()) {			    
				
				//If the current anchor has the class and a higher score, make it the current story URL
				if ((_anchors.get(currentScore.getKey()).get("class").contains(rankedClasses.get(0))) &&
					(currentScore.getValue() > highestScore)) {
					storyURL = _anchors.get(currentScore.getKey()).get("href");
					highestScore = currentScore.getValue();
				}				
			}
						
			return storyURL;
		}
		
		/**
		 * Returns scores for each valid anchor based on their possibility to be a good story.
		 * The returned map points each anchor list key to that anchor's score.
		 * 
		 * Not all anchors have a score.
		 * 
		 * @return		Map with key the same as the anchor key in 'anchors' and the anchor's score
		 */
		public HashMap<Integer, Integer> getAnchorScores() {
			
			//Create the score object. A map is used to maintain relation to the anchors object
			HashMap<Integer,Integer> anchorScores = new HashMap<Integer, Integer>(); 			
			for (int anchorIndex = 0; anchorIndex < _anchors.size(); ++anchorIndex) {
				anchorScores.put(anchorIndex, 0);
			}
			
			//Score each anchor based on the following criteria
			adjustScoresByPageLocation(anchorScores);
			adjustScoresByTitleLength(anchorScores);
			adjustScoresBySimilarPaths(anchorScores);
			adjustScoreIfAllCaps(anchorScores);
			
			//Return the scores of each valid anchor
			return anchorScores;
		}
		
		/**
		 * Adjusts the scores of the passed anchors object according to page location. This
		 * includes height (y-position), width location (x-position), proximity to top of the page,
		 * and visibility.
		 * 
		 * Anchors not on the visible region (x-position < 0) are removed from the anchorScores object.
		 * 
		 * See internal documentation for how x-position score is calculated.
		 * 
		 * @param anchorScores	Map of anchor keys associated with their individual scores
		 */
		public void adjustScoresByPageLocation(HashMap<Integer, Integer> anchorScores) {
			
			//First, lets loop through the urls and mark any that fall off the visible page
			ArrayList<Integer> unseenURLs = new ArrayList<Integer>();
			for (Map.Entry<Integer, Integer> currentScore : anchorScores.entrySet()) {		
				int currentAnchorXPosition = Integer.parseInt(_anchors.get(currentScore.getKey()).get("xPosition"));
			    if ((currentAnchorXPosition < 0) || (currentAnchorXPosition > _screenWidth)) {
			    	unseenURLs.add(currentScore.getKey());
			    }
			}
			
			//Delete from the score object the marked unseen urls
			for (Integer currentKey: unseenURLs) {
		    	anchorScores.remove(currentKey);
			}
			
			/**
			 * The screen is divided into five regions:
			 * 		1) The first third
			 * 		2) First Mark (1) to the half page mark
			 * 		3) Second Mark (2) to HALFWAY TO THE TWO THIRD MARK
			 * 		4) Third Mark (3) to the two third mark  (This is the optimal region)
			 * 		5) The rightmost third of the page
			 * 
			 * There are six points that make up the borders.
			 * 
			 * Where the story lands on the page determines which score it gets.
			 * Each section is a linear equation determined by the regions two border points score
			 */
			//Set the points based on the screen size
			int firstPoint = 0;  								//Named for simpler clarity
			int secondPoint = _screenWidth/3;					//One third mark
			int thirdPoint = _screenWidth/2;						//Halfway mark
			int fourthPoint = _screenWidth/3 + _screenWidth/4;  	//Halfway to 
			int fifthPoint = _screenWidth*2/3;					//Two third mark
			int sixthPoint = _screenWidth;						//Also added for clarity
			
			//Loop through each remaining anchor and determine its score according to its place on the page
			for (Map.Entry<Integer, Integer> currentScore : anchorScores.entrySet()) {			    
				
				//Set the score offset to zero for starters
				int scoreOffset = 0;
				
				//Grab the current anchor's position
				int anchorXPosition = Integer.parseInt(_anchors.get(currentScore.getKey()).get("xPosition"));
				int anchorYPosition = Integer.parseInt(_anchors.get(currentScore.getKey()).get("yPosition"));
				
				//Based on the location of the anchor, determine the two points that it's between and their scores.
				int leftPoint, leftScore, rightPoint, rightScore = 0;
				if (anchorXPosition < secondPoint) {
					leftPoint = firstPoint; leftScore = POSITIONLEFTMOSTXSCORE;
					rightPoint = secondPoint; rightScore = POSITIONONETHIRDXSCORE;
				}
				else if (anchorXPosition < thirdPoint) {
					leftPoint = secondPoint; leftScore = POSITIONONETHIRDXSCORE;
					rightPoint = thirdPoint; rightScore = POSITIONONEHALFXSCORE;
				}
				else if (anchorXPosition < fourthPoint) {
					leftPoint = thirdPoint; leftScore = POSITIONONEHALFXSCORE;
					rightPoint = fourthPoint; rightScore = POSITIONOPTIMALXSCORE;
				}
				else if (anchorXPosition < fifthPoint) {
					leftPoint = fourthPoint; leftScore = POSITIONOPTIMALXSCORE;
					rightPoint = fifthPoint; rightScore = POSITIONTWOTHIRDXSCORE;
				}
				else {
					leftPoint = fifthPoint; leftScore = POSITIONTWOTHIRDXSCORE;
					rightPoint = sixthPoint; rightScore = POSITIONRIGHTMOSTXSCORE;
				}
				
				//Use the points as the x and the scores as y, get the slope				
				double regionSlope = (double) (rightScore - leftScore)/(rightPoint - leftPoint);
				
				//Get the y-intercept
				int yIntercept = (int) (leftScore - regionSlope*leftPoint);
				
				//Finally, get the score for the anchor location
				scoreOffset += (int) (anchorXPosition * regionSlope) + yIntercept;
				
				//--------Check to see if anchor is too high------------
				//Apply handicap if anchor lies in page top regions. 
				//The second regions encompasses the first.
				if (anchorYPosition < TOPREGIONONEHEIGHT) {
					scoreOffset += TOPREGIONONEHANDICAP;
				}
				else if (anchorYPosition < TOPREGIONTWOHEIGHT) {
					scoreOffset += TOPREGIONTWOHANDICAP;
				}
				
				//Add the score offset to the anchor object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		
		/**
		 * Adjusts the scores of the passed anchor scores based on title length. This includes being too short, somewhat short,
		 * long, and meeting word counts or not.
		 * 
		 * Anchors with titles not meeting the minimum text length are removed from the passed anchor scores object.
		 * 
		 * @param anchorScores	Map of anchor keys associated with their individual scores
		 */
		public void adjustScoresByTitleLength(HashMap<Integer, Integer> anchorScores) {
			
			//First, lets loop through the urls and mark any that have no text or only a few characters
			ArrayList<Integer> lowTextURLs = new ArrayList<Integer>();
			for (Map.Entry<Integer, Integer> currentScore : anchorScores.entrySet()) {		
				String currentAnchorText = _anchors.get(currentScore.getKey()).get("text");
			    if (currentAnchorText.length() <= MINIMUMTEXTLENGTH) {
			    	lowTextURLs.add(currentScore.getKey());
			    }
			}
			
			//Delete from the score object the marked low text urls
			for (Integer currentKey: lowTextURLs) {
		    	anchorScores.remove(currentKey);
			}
			
			//Loop through the anchors and adjust scores by length and word count
			for (Map.Entry<Integer, Integer> currentScore : anchorScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Grab the current anchor's text and length
				String urlText = _anchors.get(currentScore.getKey()).get("text");
				int urlTextLength = urlText.length();
				
				//If the text is too short, give the url a handicap
				if (urlTextLength <= SHORTTEXTLENGTH) {scoreOffset += SHORTTEXTHANDICAP;}

				//If the text is long enough, give the url a higher score
				else if (urlTextLength >= LONGTEXTLENGTH) {scoreOffset += LONGTEXTSCORE;}
				
				//If there are not enough words in the text, penalize the url
				int wordCount = urlText.trim().split("\\s+").length;
				if (wordCount < MINIMUMWORDCOUNT) {scoreOffset += MINIMUMWORDHANDICAP;}

				//Add the score offset to the anchor object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		
		/**
		 * Increases the score of an anchor that shares the same first path part of the target URL.
		 * 
		 * In other words, this is the path part right after the domain part. For example, the first path part of 
		 * 'boston.com/entertainment' is 'entertainment'.
		 * 
		 * @param anchorScores	Map of anchor keys associated with their individual scores
		 */
		public void adjustScoresBySimilarPaths(HashMap<Integer, Integer> anchorScores) {
			
			//Grab the first path part of the target url for comparison
			String targetURLPathPart = getFirstPartOfURIPath(_targetURL);
				
			//If we are not at the topmost domain (no path after domain), then score the urls
			if (targetURLPathPart.length() > 1) {
				
				//Loop through each anchor and adjust its score based on similar first path part
				for (Map.Entry<Integer, Integer> currentScore : anchorScores.entrySet()) {			    
					
					//Set the score offset to zero
					int scoreOffset = 0;
					
					//Grab the current anchor's href and path part
					String urlHref = _anchors.get(currentScore.getKey()).get("href");
					String urlPathPart = getFirstPartOfURIPath(urlHref);
					
					//If the targetURL and current anchor path parts are the same, increment the score
					if (targetURLPathPart.equals(urlPathPart)) {scoreOffset += SAMEPATHPARTSCORE;}
					
					//Add the score offset to the anchor object
					currentScore.setValue(currentScore.getValue() + scoreOffset);
				}
			}
		}
		
		/**
		 * Applies handicap to any anchor's score with a title consisting of all caps
		 * 
		 * @param anchorScores	Map of anchor keys associated with their individual scores
		 */
		public void adjustScoreIfAllCaps(HashMap<Integer, Integer> anchorScores) {
							
			//Loop through each anchor and handicap its score if it is in all capital letters
			for (Map.Entry<Integer, Integer> currentScore : anchorScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's text
				String urlText = _anchors.get(currentScore.getKey()).get("text");
				
				//If the text is in all caps, apply the reduction
				if (urlText.equals(urlText.toUpperCase())) {
					scoreOffset += ALLCAPSHANDICAP;
				}
				
				//Add the score offset to the anchor object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		
		/**
		 * Returns array list of all the anchors' classes ranked by average anchor score. The first class (at 0)
		 * has the highest average.
		 * 
		 * The average is calculated by taking the sum of the scores of all the anchors that have a class
		 * and dividing by that number of anchors.
		 * 
		 * @param anchorScores	Map of anchor keys associated with their individual scores
		 * @return
		 */
		private ArrayList<String> getClassesRankedByAveragedAnchorScore(HashMap<Integer, Integer> anchorScores) {
			
			//Prepare to store each class' use count and total score
			HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
			HashMap<String, Integer> classTotalScores = new HashMap<String, Integer>();
			
			//Loop through the urls and total up the classes with the urls' scores
			for (Map.Entry<Integer, Integer> currentScore : anchorScores.entrySet()) {		
				
				//Get the url's classes from the class attribute
				String classString = _anchors.get(currentScore.getKey()).get("class");
				String[] urlClasses = classString.trim().split("\\s+");
				
				//Loop through each class if any exist
				if (classString.length() > 0) {
					for (String currentClass : urlClasses) {
						
						//If this class has not been seen before, begin an entry for it
						if (!classCounts.containsKey(currentClass)) {
							classCounts.put(currentClass, 1);
							classTotalScores.put(currentClass, currentScore.getValue());
						}
						
						//Otherwise, add the current score to its score and increment the count
						else {
							classCounts.put(currentClass, classCounts.get(currentClass) + 1);
							classTotalScores.put(currentClass, classTotalScores.get(currentClass) + currentScore.getValue());
						}
						
					}
				}
			}
			
			//Get the class score averages and put them into a TreeMap for sorting
			TreeMap<Integer, String> averagedClassScores = new TreeMap<Integer, String>(Collections.reverseOrder());
			for (Map.Entry<String, Integer> currentClassScore : classTotalScores.entrySet()) {
				int averageScore = currentClassScore.getValue() / classCounts.get(currentClassScore.getKey());
				averagedClassScores.put(averageScore, currentClassScore.getKey());
			}
			
			//Turn the sorted map into the final array and return it
			ArrayList<String> rankedClasses = new ArrayList<String>();
			for (Map.Entry<Integer, String> currentScore : averagedClassScores.entrySet()) {
				rankedClasses.add(currentScore.getValue());
			}
			
			return rankedClasses;
		}
		
		/**
		 * Returns the first path part of a URI.
		 * 
		 * In other words, the part right after the domain part. For example, 'boston.com/entertainment' would return
		 * 'entertainment'.
		 * 
		 * @param URIString		URI to get first path part
		 * @return				First path part (i.e. 'entertainment' of 'boston.com/entertainment' 
		 */
		private String getFirstPartOfURIPath(String URIString) {
			
			//Separate the string by slashes
			String[] uriParts = URIString.split("/");
						
			//If the string was not split, return an empty string
			if (uriParts.length <= 1) {return "";}
			
			//If the passed URI was a relative path beginning with a forward slash, return second array section
			else if (URIString.substring(1,2).equals("/")) {
				return uriParts[1];
			}
			
			//Otherwise, return the path part after the domain
			else if (uriParts.length >= 4) {
				return uriParts[3];
			}
			
			//Or return nothing
			else {return "";}
		}
		
	}
}

