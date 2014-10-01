package adshotrunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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

public class StoryFinder {
	
	private final String targetURL;
	private final List<Map<String, String>> urlAnchors;
	
	private static String getAnchorsFromURL(String targetURL) {
		
		//Try to make the phantomjs call and return the JSON
        String phantomJSResponse = null;
        try {
            
        	//Run the retrieve anchors js file with phantomjs
            Process p = Runtime.getRuntime().exec(new String[]{
	            "/home/juicio/Documents/SSDPI/screenShotServer/sssEclipseProject/src/adshotrunner/phantomjs", 
	            "/home/juicio/Documents/SSDPI/screenShotServer/sssEclipseProject/src/adshotrunner/retrievePossibleStoriesFromURL.js",
	            targetURL            	
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
	
	private List<Map<String, String>> getImmutableAnchorInfoFromJSON(String anchorJSON) throws MalformedURLException, URISyntaxException {
		
		//Turn the returned JSON into an array of objects
		JsonArray anchorsArray = new Gson().fromJson(anchorJSON, JsonArray.class);
		
		//Get the primary domain of the target URL
		String urlDomain = getURIDomain(targetURL);
		
		//Convert the data into an ArrayList of immutable anchor HashMaps
		ArrayList<Map<String, String>> anchorsList = new ArrayList<Map<String, String>>();
        for(int i = 0; i < anchorsArray.size(); i++){
        	
        	//Convert the current array item into an anchor object
            JsonObject curAnchor = anchorsArray.get(i).getAsJsonObject();
           
            //As long as the href domain is the same as the target site, add the anchor info. Otherwise, ignore.
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
        
        //Make the anchorsList immutable and return it
		List<Map<String, String>> immutableAnchorList = Collections.unmodifiableList(anchorsList);  
		return immutableAnchorList;
	}
	
	StoryFinder(String url) throws MalformedURLException, URISyntaxException {
		
		//Store the target URL for the class to get stories from
		targetURL = url;
		
		//Get the possible story links using phantomjs
		String anchorJSON = getAnchorsFromURL(targetURL);
                
        //Get the immutable anchor info as array of maps
		urlAnchors = getImmutableAnchorInfoFromJSON(anchorJSON);
	}
	
	private String getURIDomain(String URIString) throws MalformedURLException, URISyntaxException {
		URI uriObject = new URI(URIString.trim());
		if (uriObject.getHost() == null) {return "";}
		return InternetDomainName.from(uriObject.getHost()).topPrivateDomain().toString();
	}
	
	public Calculator Calculator() {
		return new Calculator();
	}
	
	class Calculator {		
		private int SCREENWIDTH;
		
		private int POSITIONLEFTMOSTXSCORE;
		private int POSITIONONETHIRDXSCORE;
		private int POSITIONONEHALFXSCORE;
		private int POSITIONOPTIMALXSCORE;
		private int POSITIONTWOTHIRDXSCORE;
		private int POSITIONRIGHTMOSTXSCORE;
		
		private int TOPREGIONONEHEIGHT;
		private int TOPREGIONTWOHEIGHT;
		private int TOPREGIONONEHANDICAP;
		private int TOPREGIONTWOHANDICAP;
		
		private int MINIMUMTEXTLENGTH;
		private int SHORTTEXTLENGTH;
		private int SHORTTEXTHANDICAP;
		private int LONGTEXTLENGTH;
		private int LONGTEXTSCORE;
		private int MINIMUMWORDCOUNT;
		private int MINIMUMWORDHANDICAP;
		
		private int SAMEPATHPARTSCORE;
		
		private int ALLCAPSHANDICAP;
		
		Calculator() {
			
			SCREENWIDTH = 1024;
			
			POSITIONLEFTMOSTXSCORE = 0;
			POSITIONONETHIRDXSCORE = 10;
			POSITIONONEHALFXSCORE = 19;
			POSITIONOPTIMALXSCORE = 20;
			POSITIONTWOTHIRDXSCORE = 19;
			POSITIONRIGHTMOSTXSCORE = 0;

			TOPREGIONONEHEIGHT = 350;
			TOPREGIONONEHANDICAP = -11;
			TOPREGIONTWOHEIGHT = 475;
			TOPREGIONTWOHANDICAP = -6;
			
			MINIMUMTEXTLENGTH = 3;
			SHORTTEXTLENGTH = 9;
			SHORTTEXTHANDICAP = -10;
			LONGTEXTLENGTH = 16;
			LONGTEXTSCORE = 11;
			MINIMUMWORDCOUNT = 3;
			MINIMUMWORDHANDICAP = -10;
			
			SAMEPATHPARTSCORE = 7;
			
			ALLCAPSHANDICAP = -12;

		}
		
		
		/**
		 * Sets the score of the leftmost (0 pixel) position for page location scoring
		 * @param newValue	The new value of the leftmost position score
		 * @return 			Reference to the current class instance
		 */
		public Calculator positionLeftmostXScore(int newValue) {
			POSITIONLEFTMOSTXSCORE = newValue; return this;
		}
		/**
		 * Sets the score of the one-third position for page location scoring
		 * @param newValue	The new value of the one-third position score
		 * @return 			Reference to the current class instance
		 */
		public Calculator positionOneThirdXScore(int newValue) {
			POSITIONONETHIRDXSCORE = newValue; return this;
		}
		/**
		 * Sets the score of the one-half position for page location scoring
		 * @param newValue	The new value of the one-half position score
		 * @return 			Reference to the current class instance
		 */
		public Calculator positionOneHalfXScore(int newValue) {
			POSITIONONEHALFXSCORE = newValue; return this;
		}
		/**
		 * Sets the score of the optimal position for page location scoring
		 * @param newValue	The new value of the optimal position score
		 * @return 			Reference to the current class instance
		 */
		public Calculator positionOptimalXScore(int newValue) {
			POSITIONOPTIMALXSCORE = newValue; return this;
		}
		/**
		 * Sets the score of the two-third position for page location scoring
		 * @param newValue	The new value of the two-third position score
		 * @return 			Reference to the current class instance
		 */
		public Calculator positionTwoThirdXScore(int newValue) {
			POSITIONTWOTHIRDXSCORE = newValue; return this;
		}
		/**
		 * Sets the score of the rightmost position (far right of page) for page location scoring
		 * @param newValue	The new value of the rightmost position score
		 * @return 			Reference to the current class instance
		 */
		public Calculator positionRightmostXScore(int newValue) {
			POSITIONRIGHTMOSTXSCORE = newValue; return this;
		}
		
		/**
		 * Sets the height (in pixels) for the topmost region
		 * @param newValue	Height (in pixels) of topmost region
		 * @return 			Reference to the current class instance
		 */
		public Calculator topRegionOneHeight(int newValue) {
			TOPREGIONONEHEIGHT = newValue; return this;
		}
		/**
		 * Sets the negative handicap for a story in the topmost region
		 * @param newValue	Negative handicap for stories in the topmost region
		 * @return 			Reference to the current class instance
		 */
		public Calculator topRegionOneHandicap(int newValue) {
			TOPREGIONONEHANDICAP = newValue; return this;
		}
		/**
		 * Sets the height (in pixels) for the the second top region
		 * @param newValue	Height (in pixels) of second top region (must be greater than the topmost region height)
		 * @return 			Reference to the current class instance
		 */
		public Calculator topRegionTwoHeight(int newValue) {
			TOPREGIONTWOHEIGHT = newValue; return this;
		}
		/**
		 * Sets the negative handicap for a story in the second top region
		 * @param newValue	Negative handicap for stories in the second top region
		 * @return 			Reference to the current class instance
		 */
		public Calculator topRegionTwoHandicap(int newValue) {
			TOPREGIONTWOHANDICAP = newValue; return this;
		}

		/**
		 * Sets the minimum text character length a url title must be to be scored
		 * @param newValue	Minimum text length in characters that a url title must be
		 * @return 			Reference to the current class instance
		 */
		public Calculator minimumTextLength(int newValue) {
			MINIMUMTEXTLENGTH = newValue; return this;
		}
		/**
		 * Sets the length in characters that a title is considered short
		 * @param newValue	Text length in characters that defines a short title
		 * @return 			Reference to the current class instance
		 */
		public Calculator shortTextLength(int newValue) {
			SHORTTEXTLENGTH = newValue; return this;
		}
		/**
		 * Sets the negative handicap a url receives if it is considered short text
		 * @param newValue	Negative handicap to apply to url score if the url title is considered short text
		 * @return 			Reference to the current class instance
		 */
		public Calculator shortTextHandicap(int newValue) {
			SHORTTEXTHANDICAP = newValue; return this;
		}
		/**
		 * Sets the length in characters that a title is considered long
		 * @param newValue	Text length in characters that defines a long title
		 * @return 			Reference to the current class instance
		 */
		public Calculator longTextLength(int newValue) {
			LONGTEXTLENGTH = newValue; return this;
		}
		/**
		 * Sets the score a url receives if it is considered long text
		 * @param newValue	Score to apply to url score if the url title is considered long text
		 * @return 			Reference to the current class instance
		 */
		public Calculator longTextScore(int newValue) {
			LONGTEXTSCORE = newValue; return this;
		}
		/**
		 * Sets the minimum word count of a title (used to handicap short titles)
		 * @param newValue	Minimum word count of a title 
		 * @return 			Reference to the current class instance
		 */
		public Calculator minimumWordCount(int newValue) {
			MINIMUMWORDCOUNT = newValue; return this;
		}
		/**
		 * Sets the negative handicap a url receives if its title doesn't meet the minimum word count
		 * @param newValue	Negative handicap applied to urls with titles that don't meet the minimum word count 
		 * @return 			Reference to the current class instance
		 */
		public Calculator minimumWordHandicap(int newValue) {
			MINIMUMWORDHANDICAP = newValue; return this;
		}

		/**
		 * Sets the score a url receives if its href has the first path part same as the target url
		 * @param newValue	Score the url receives if its path's first part is the same as the target url
		 * @return 			Reference to the current class instance
		 */
		public Calculator samePathPartsScore(int newValue) {
			SAMEPATHPARTSCORE = newValue; return this;
		}

		/**
		 * Sets the negative handicap a url receives if its title is in all caps
		 * @param newValue	Negative handicap applied to urls with titles in call caps
		 * @return 			Reference to the current class instance
		 */
		public Calculator allCapsHandicap(int newValue) {
			ALLCAPSHANDICAP = newValue; return this;
		}

		
		public String getStory() {
			
			//Get all the anchor scores using the containing class anchors and the Calculators numbers
			HashMap<Integer,Integer> anchorScores = getAnchorScores();
			
			//Get a ranked list of the anchors' classes based off each classes anchors' averages
			ArrayList<String> rankedClasses = getClassesRankedByAveragedAnchorScore(anchorScores);
			
			//Get the story with the highest score and with the ranked class
			String storyURL = "";
			int highestScore = 0;
			for (Map.Entry<Integer, Integer> currentScore : anchorScores.entrySet()) {			    
				
				//If the current anchor has the class and a higher score, make it the current story URL
				if ((urlAnchors.get(currentScore.getKey()).get("class").contains(rankedClasses.get(0))) &&
					(currentScore.getValue() > highestScore)) {
					storyURL = urlAnchors.get(currentScore.getKey()).get("href");
					highestScore = currentScore.getValue();
				}				
			}
			
			
			//System.out.println(rankedClasses.toString());
			//System.out.println(anchorScores.toString());
			//System.out.println(highestScore);
			//System.out.println(storyURL);
			
			return storyURL;
		}
		
		public HashMap<Integer, Integer> getAnchorScores() {
			
			//Create the score object. A map is used to maintain relation to the urlAnchors object
			HashMap<Integer,Integer> urlScores = new HashMap<Integer,Integer>(); 			
			for (int anchorIndex = 0; anchorIndex < urlAnchors.size(); ++anchorIndex) {
				urlScores.put(anchorIndex, 0);
			}
			
			urlScores = adjustScoresByPageLocation(urlScores);
			urlScores = adjustScoresByTitleLength(urlScores);
			urlScores = adjustScoresBySimilarPaths(urlScores);
			urlScores = adjustScoreIfAllCaps(urlScores);
			
			return urlScores;
		}
		
		public HashMap<Integer, Integer> adjustScoresByPageLocation(HashMap<Integer,Integer> urlScores) {
			
			//First, lets loop through the urls and mark any that fall off the visible page
			ArrayList<Integer> unseenURLs = new ArrayList<Integer>();
			for (Map.Entry<Integer, Integer> currentScore : urlScores.entrySet()) {		
				int currentAnchorXPosition = Integer.parseInt(urlAnchors.get(currentScore.getKey()).get("xPosition"));
			    if ((currentAnchorXPosition < 0) || (currentAnchorXPosition > SCREENWIDTH)) {
			    	unseenURLs.add(currentScore.getKey());
			    }
			}
			
			//Delete from the score object the marked unseen urls
			for (Integer currentKey: unseenURLs) {
		    	urlScores.remove(currentKey);
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
			int secondPoint = SCREENWIDTH/3;					//One third mark
			int thirdPoint = SCREENWIDTH/2;						//Halfway mark
			int fourthPoint = SCREENWIDTH/3 + SCREENWIDTH/4;  	//Halfway to 
			int fifthPoint = SCREENWIDTH*2/3;					//Two third mark
			int sixthPoint = SCREENWIDTH;						//Also added for clarity
			
			//Loop through each remaining anchor and determine its score according to its place on the page
			for (Map.Entry<Integer, Integer> currentScore : urlScores.entrySet()) {			    
				
				//Set the score offset to zero for starters
				int scoreOffset = 0;
				
				//Grab the current anchor's position
				int anchorXPosition = Integer.parseInt(urlAnchors.get(currentScore.getKey()).get("xPosition"));
				int anchorYPosition = Integer.parseInt(urlAnchors.get(currentScore.getKey()).get("yPosition"));
				
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
			
			//Send back the modified url scores
			return urlScores;
		}
		
		
		public HashMap<Integer, Integer> adjustScoresByTitleLength(HashMap<Integer,Integer> urlScores) {
			
			//First, lets loop through the urls and mark any that have no text or only a few characters
			ArrayList<Integer> lowTextURLs = new ArrayList<Integer>();
			for (Map.Entry<Integer, Integer> currentScore : urlScores.entrySet()) {		
				String currentAnchorText = urlAnchors.get(currentScore.getKey()).get("text");
			    if (currentAnchorText.length() <= MINIMUMTEXTLENGTH) {
			    	lowTextURLs.add(currentScore.getKey());
			    }
			}
			
			//Delete from the score object the marked low text urls
			for (Integer currentKey: lowTextURLs) {
		    	urlScores.remove(currentKey);
			}
			
			
			//Loop through the anchors and adjust scores by length and word count
			for (Map.Entry<Integer, Integer> currentScore : urlScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Grab the current anchor's text and length
				String urlText = urlAnchors.get(currentScore.getKey()).get("text");
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
			
			//Send back the modified url scores
			return urlScores;
		}
		
		public HashMap<Integer, Integer> adjustScoresBySimilarPaths(HashMap<Integer,Integer> urlScores) {
			
			//Grab the first path part of the target url for comparison
			String targetURLPathPart = getFirstPartOfURIPath(targetURL);
				
			//If we are not at the topmost domain (no path after domain), then score the urls
			if (targetURLPathPart.length() > 1) {
				
				//Loop through each anchor and adjust its score based on similar first path part
				for (Map.Entry<Integer, Integer> currentScore : urlScores.entrySet()) {			    
					
					//Set the score offset to zero
					int scoreOffset = 0;
					
					//Grab the current anchor's href and path part
					String urlHref = urlAnchors.get(currentScore.getKey()).get("href");
					String urlPathPart = getFirstPartOfURIPath(urlHref);
					
					//If the targetURL and current anchor path parts are the same, increment the score
					if (targetURLPathPart.equals(urlPathPart)) {scoreOffset += SAMEPATHPARTSCORE;}
					
					//Add the score offset to the anchor object
					currentScore.setValue(currentScore.getValue() + scoreOffset);
				}
			}
			
			//Send back the modified url scores
			return urlScores;
		}
				
		public HashMap<Integer, Integer> adjustScoreIfAllCaps(HashMap<Integer,Integer> urlScores) {
							
			//Loop through each anchor and handicap its score if it is in all capital letters
			for (Map.Entry<Integer, Integer> currentScore : urlScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's text
				String urlText = urlAnchors.get(currentScore.getKey()).get("text");
				
				//If the text is in all caps, apply the reduction
				if (urlText.equals(urlText.toUpperCase())) {
					scoreOffset += ALLCAPSHANDICAP;
				}
				
				//Add the score offset to the anchor object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
			
			//Send back the modified url scores
			return urlScores;
		}
		
		private ArrayList<String> getClassesRankedByAveragedAnchorScore(HashMap<Integer,Integer> urlScores) {
			
			//Prepare to store each class' use count and total score
			HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
			HashMap<String, Integer> classTotalScores = new HashMap<String, Integer>();
			
			//Loop through the urls and total up the classes with the urls' scores
			for (Map.Entry<Integer, Integer> currentScore : urlScores.entrySet()) {		
				
				//Get the url's classes from the class attribute
				String classString = urlAnchors.get(currentScore.getKey()).get("class");
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

