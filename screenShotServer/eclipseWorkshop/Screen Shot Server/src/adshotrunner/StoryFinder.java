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

/**
 * Retrieves possible stories from a URL. The stories are ranked with a higher
 * score being more likely to be a usable story of the subsection.
 */
public class StoryFinder {
	
	private final String targetURL;
	private final List<Map<String, String>> urlAnchors;
	
	/**
	 * Returns a list of all possible stories with their scores, titles, and URLs
	 * 
	 * @param url	URL to retrieve stories from
	 * @return		ArrayList of HashMaps with the keys 'score', 'title', and 
	 * 				'url' pointing to their respective data, sorted ascending
	 * 				by score
	 */
	public static ArrayList<HashMap<String, String>> getStories(String url) {
		return null;
	}

	private static String getAnchorsFromURL(String targetURL) {
		
		//Try to make the phantomjs call and return the JSON
        String phantomJSResponse = null;
        try {
            
        	//Run the retrieve anchors js file with phantomjs
            Process p = Runtime.getRuntime().exec(new String[]{
	            "/home/juicio/Documents/SSDPI/screenShotServer/eclipseWorkshop/Screen Shot Server/src/adshotrunner/phantomjs", 
	            "/home/juicio/Documents/SSDPI/screenShotServer/eclipseWorkshop/Screen Shot Server/src/adshotrunner/retrievePossibleStoriesFromURL.js",
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
		private int baseScore;
		
		private int SCREENWIDTH;
		
		private int LEFTXPOSITIONSCORE;
		private int ONETHIRDXPOSITIONSCORE;
		private int ONEHALFXPOSITIONSCORE;
		private int OPTIMALXPOSITIONSCORE;
		private int TWOTHIRDXPOSITIONSCORE;
		private int RIGHTXPOSITIONSCORE;
		
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
			
			LEFTXPOSITIONSCORE = 0;
			ONETHIRDXPOSITIONSCORE = 10;
			ONEHALFXPOSITIONSCORE = 19;
			OPTIMALXPOSITIONSCORE = 20;
			TWOTHIRDXPOSITIONSCORE = 19;
			RIGHTXPOSITIONSCORE = 0;

			TOPREGIONONEHEIGHT = 350;
			TOPREGIONTWOHEIGHT = 475;
			TOPREGIONONEHANDICAP = -11;
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
		
		public Calculator baseScore(int val) {
			baseScore = val;
			return this;
		}
		
		public int score() {
			return baseScore;
		}
		
		public ArrayList<Integer> getScoredStories() {
			
			//Create the score object. A map is used to maintain relation to the urlAnchors object
			HashMap<Integer,Integer> urlScores = new HashMap<Integer,Integer>(); 			
			for (int anchorIndex = 0; anchorIndex < urlAnchors.size(); ++anchorIndex) {
				urlScores.put(anchorIndex, 0);
			}
			
			urlScores = adjustScoresByPageLocation(urlScores);

			urlScores = adjustScoresByTitleLength(urlScores);
			
			urlScores = adjustScoresBySimilarPaths(urlScores);
			
			urlScores = adjustScoreIfAllCaps(urlScores);
			
			
			ArrayList<String> rankedClasses = new ArrayList<String>();
			rankedClasses = getClassesRankedByAveragedAnchorScore(urlScores);
			
			System.out.println(rankedClasses.toString());
			System.out.println(urlScores.toString());
			
			return null;
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
					leftPoint = firstPoint; leftScore = LEFTXPOSITIONSCORE;
					rightPoint = secondPoint; rightScore = ONETHIRDXPOSITIONSCORE;
				}
				else if (anchorXPosition < thirdPoint) {
					leftPoint = secondPoint; leftScore = ONETHIRDXPOSITIONSCORE;
					rightPoint = thirdPoint; rightScore = ONEHALFXPOSITIONSCORE;
				}
				else if (anchorXPosition < fourthPoint) {
					leftPoint = thirdPoint; leftScore = ONEHALFXPOSITIONSCORE;
					rightPoint = fourthPoint; rightScore = OPTIMALXPOSITIONSCORE;
				}
				else if (anchorXPosition < fifthPoint) {
					leftPoint = fourthPoint; leftScore = OPTIMALXPOSITIONSCORE;
					rightPoint = fifthPoint; rightScore = TWOTHIRDXPOSITIONSCORE;
				}
				else {
					leftPoint = fifthPoint; leftScore = TWOTHIRDXPOSITIONSCORE;
					rightPoint = sixthPoint; rightScore = RIGHTXPOSITIONSCORE;
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
				
				//System.out.println(currentScore.getValue() + ": " + scoreOffset + " - " + anchorXPosition + ", " + anchorYPosition);
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
				
				//System.out.println(currentScore.getValue() + ": " + scoreOffset + " - " + urlTextLength + ", " + wordCount);
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
					
					//System.out.println(currentScore.getValue() + " [" + scoreOffset + "]: " + targetURLPathPart + ", " + urlPathPart);
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
				
				//System.out.println(currentScore.getValue() + " [" + scoreOffset + "]: " + targetURLPathPart + ", " + urlPathPart);
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
				//String[] urlClasses = classString.trim().split("zzzzzzzzzzzzzzzzzzz");
				
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
			
			
			/*for (Map.Entry<Integer, String> currentScore : averagedClassScores.entrySet()) {
				System.out.println(currentScore.getValue() + " - " + currentScore.getKey());	
			}*/
			
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

