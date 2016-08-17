package adshotrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.utilities.URLTool;

import com.google.common.net.InternetDomainName;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * The StoryFinder class attempts to retrieve a feature story from the passed URL. This story
 * must be prominent on the page and not focus on a negative topic such as a murder.
 * 
 * The target URL is passed through the constructor. All links on the page and their info are retrieved.
 * The resulting object is immutable.
 * 
 * The story is retrieved through the nested class Scorer. Scorer has the option of
 * setting the many variables used to calculate the best story. 
 */
public class StoryFinder {
	
	final private static boolean VERBOSE = true;
	
	private static void consoleLog(String message) {
		if (VERBOSE) {System.out.println(message);}
	}
	
	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Private Static Methods **********************************
	/**
	 * Returns a JSON string of all the links and their info from the passed URL
	 * 
	 * Link info: id, href, name, onclick, text, style, title
	 * 
	 * @param url			URL to grab the links from
	 * @param viewWidth		Width of the screen phantomjs should use
	 * @param viewHeight	Height of the screen phantomjs should use
	 * @param viewHeight	URL to grab the links from
	 * @return				JSON string of links and their info
	 */
	private static String getLinkJSONFromURL(String url, int viewWidth, int viewHeight, String userAgent) {
		
		//Try to make the phantomjs call and return the JSON
        String phantomJSResponse = null;
        
        try {
            
        	//Run the retrieve links js file with phantomjs
        	StoryFinder.consoleLog("getLinkJSONFromUrl url: " + url);
        	//StoryFinder.consoleLog("getLinkJSONFromUrl width: " + viewWidth);
        	//StoryFinder.consoleLog("getLinkJSONFromUrl height: " + viewHeight);
            Process p = Runtime.getRuntime().exec(new String[]{
	            "phantomjs/phantomjs", 
	            "javascript/retrievePossibleStoriesFromURL.js",
	            url, Integer.toString(viewWidth), Integer.toString(viewHeight), userAgent        	
            });
            
            //Get the string returned from phantomjs
            String thisLine = "";
            BufferedReader commandLineInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
           // while ((thisLine = commandLineInput.readLine()) != null) {
            //	phantomJSResponse += thisLine;
            //}
            phantomJSResponse = commandLineInput.readLine();
        }
        catch (IOException e) {
			throw new AdShotRunnerException("Could not execute phantomjs", e);
        }
		//StoryFinder.consoleLog("PhantomJSResponse: " + phantomJSResponse);
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
	 * All links on the target URL and their info (id, href, name, onclick, text, style, title) (immutable)
	 */
	private final List<StoryLink> _links;
	
	/**
	 * Width to set phantomjs screen to before grabbing links
	 */
	private final int _screenWidth;
	
	/**
	 * Height to set phantomjs screen to before grabbing links
	 */
	private final int _screenHeight;
	
	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Returns a StoryFinder instance with the links retrieved from the passed URL
	 * 
	 * The screen is automatically set to 1366x768.
	 * 
	 * Instance is immutable
	 * 
	 * @param url	URL to retrieve stories from
	 * 
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException 
	 */
	public StoryFinder(String url) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		
		//Create the instance with the default 1366x768 phantomjs screen
		this(url, 1366, 768);
	}

	/**
	 * Returns a StoryFinder instance with the links retrieved from the passed URL, utilizing the
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
	public StoryFinder(String url, int viewWidth, int viewHeight) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		
		//Store the target URL for the class to get stories from
		_targetURL = url;
		
		//Store the height and width to use for the phantomjs screen
		_screenWidth = viewWidth;
		_screenHeight = viewHeight;
		
		//loop through the phantomjs request trying different user agents
		String userAgents[] = new String [] {"googlebot","firefox", "msnbot", "firefoxlinux"};
		int userAgentIncrementor = 0;
		int linkCountMinimum = 10;
		List<StoryLink> currLinks = new ArrayList<StoryLink>();
		while((userAgentIncrementor < userAgents.length) && (currLinks.size()< linkCountMinimum)){
		
			StoryFinder.consoleLog("Trying phantomJS with agent:" + userAgents[userAgentIncrementor]);
			//Get the possible story links using phantomjs
			String linkJSON = getLinkJSONFromURL(_targetURL, _screenWidth, _screenHeight, userAgents[userAgentIncrementor]);
	                
	        //Get the immutable link info as array of maps
			currLinks = getLinkInfoFromJSON(linkJSON);
			
			userAgentIncrementor++;
		}
		_links = currLinks;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Private Methods *************************************
	/**
	 * Returns an immutable list of links and their info (each link an immutable map)
	 * 
	 * @param linkJSON	JSON string of links as their info in the form of an array of associative arrays
	 * @return				Immutable list of immutable maps, each map a set of link info (text, href, xPosition, etc...)
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException 
	 */
	private List<StoryLink> getLinkInfoFromJSON(String linkJSON) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		
		//Turn the returned JSON into an array of objects
		try {
			FileUtils.writeStringToFile(new File("storyFinderJSON/" + new Date().getTime() + ".txt"), linkJSON);
			System.out.println("Saved StoryFinder JSON");
		} catch (IOException e) {
			System.out.println("Could not save StoryFinder JSON");
		}
		//JsonArray linksArray = new Gson().fromJson(linkJSON, JsonArray.class);
		Gson gson = new Gson();
		Type arrayStoryLinksToken = new TypeToken<ArrayList<StoryLink>>(){}.getType();
		//StoryFinder.consoleLog("TokenType: " + arrayStoryLinksToken.toString());
		//StoryFinder.consoleLog("linkJSON: " + linkJSON);
		ArrayList<StoryLink> storyLinkList = gson.fromJson(linkJSON, arrayStoryLinksToken);

		//Get the primary domain of the StoryFinder URL
		String primaryDomain = getURIDomain(_targetURL);
		
		//Loop through the list removing null and empty href elements and setting null class to empty string
		Iterator<StoryLink> linksIterator = storyLinkList.iterator();
		while(linksIterator.hasNext()){
			
			StoryLink currentLink = linksIterator.next();
			
			
			/*if ((currentLink.href != null) && (!currentLink.href.isEmpty()) && (currentLink.href.length() >= 2)){
				StoryFinder.consoleLog(" currlink:" + currentLink.href );
				StoryFinder.consoleLog("currlink: " + currentLink.href + "cur len: " + currentLink.href.length() + "substr: " + currentLink.href.substring(0, 2)) ;
			}*/
			//Check for // at beginning of href and add protocol if not there
			if ((currentLink.href != null) && (!currentLink.href.isEmpty()) && (currentLink.href.length() >= 2) && (currentLink.href.substring(0, 2).equals("//"))){
				//StoryFinder.consoleLog("Inside //: " + currentLink.href );
				currentLink.href = "http:" + currentLink.href;
			}
			
			String currentDomain = (currentLink.href != null) ? getURIDomain(currentLink.href) : null;
			
			if (currentLink.href == null || currentLink.href.isEmpty()) {
				linksIterator.remove();
			}
			else if ((currentLink.href != null) && (!currentLink.href.isEmpty()) && (currentLink.href.length() >= 7) && (currentLink.href.substring(0, 7).equals("mailto:"))){
				linksIterator.remove();
			}
			else if ((currentDomain != "") && (!primaryDomain.equals(currentDomain))) {
				linksIterator.remove();
			}
			else {
				if (currentLink.className == null) {currentLink.className = "";}
				
				//If the link begins with javascript, try to get the URL
				if ((currentLink.href.length() >= 11) && currentLink.href.substring(0, 11).equals("javascript:")) {
			        String urlPattern = "((https?|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
			        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
			        Matcher m = p.matcher(currentLink.href);
			        if (m.find()) {currentLink.href = m.group(0);}
			        else {linksIterator.remove();}
			    }
			}
		}
		
		//Get the primary domain of the target URL
		//String urlDomain = getURIDomain(_targetURL);
		
		//Convert the data into an ArrayList of immutable link HashMaps
		/*ArrayList<Map<String, String>> linksList = new ArrayList<Map<String, String>>();
        for(int i = 0; i < linksArray.size(); i++){
        	
        	//Convert the current array item into an link object
            JsonObject curLink = linksArray.get(i).getAsJsonObject();
           
            //As long as the href domain is the same as the target site, add the link info. Otherwise, ignore.
            if (!curLink.href.isJsonNull()){
            	String currentDomain = getURIDomain(curLink.href.getAsString());
            
	            if ((currentDomain == "") || (urlDomain.equals(currentDomain))) {
	            
		            //Put all the link attributes into a map
		            HashMap<String, String> linkMap = new HashMap<String, String>();
					for (Map.Entry<String,JsonElement> linkAttribute : curLink.entrySet()) {
						linkMap.put(linkAttribute.getKey(), linkAttribute.getValue().toString());
					}+0
					
					//Make the map immutable and place it into the final arraylist
					Map<String, String> immutableLinkMap = Collections.unmodifiableMap(linkMap); 
					linksList.add(immutableLinkMap);
	            }
            }
        }*/
        
        //Make the linksList immutable and return it
		//List<Map<String, String>> immutableLinkList = Collections.unmodifiableList(linksList);  
		return storyLinkList;
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
			try{
			URI uriObject = new URI(urlObject.getProtocol(), urlObject.getHost(), urlObject.getPath(), urlObject.getQuery(), nullFragment);
			if (uriObject.getHost() == null) {return "";}
				return InternetDomainName.from(uriObject.getHost()).topPrivateDomain().toString();
			}
			catch(Exception e){
				return "";
				//private suffix will cause an exception but can be ignored
			}
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
	
	public class StoryColumn{
		
		public String className;
		public int xPosition;
		public int runningScore;
		public StoryColumn(){
			className  = "";
			xPosition = 0;
			runningScore = 0;
		}
	}
	
	/**
	 * Calculates scores for each of the containing StoryFinder's links and returns 
	 * the most likely good story (highest score with highest class)
	 * 
	 * Each of the formula variables and scores can be set.
	 */
	public class Scorer {		
		
		/**
		 * Score of the left most width point (complete left side of page). DEFAULT: 0
		 */
		private int POSITIONLEFTMOSTXSCORE;
		/**
		 * Score of the one third point of the screen width. DEFAULT: 10
		 */
		private int POSITIONONEFOURTHXSCORE;
		/**
		 * Score of the one half point of the screen width. DEFAULT: 19
		 */
		private int POSITIONTHREEQUARTERXSCORE;
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
		 * Height in pixels of the lowest a story should reasonably be Must be greater than TOPREGIONSTWOHEIGHT. DEFAULT: 2000
		 */
		private int TOOFARDOWNPAGEHEIGHT;
		/**
		 * Handicap for stories that lie too far down the page (negative number). DEFAULT: -8
		 */
		//private int TOOFARDOWNPAGEHANDICAP = -8;
		
		/**
		 * Minimum text length, in characters, an link's title must be to be scored. DEFAULT: 3
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
		 * The score added to link's with titles considered to be long text. DEFAULT: 11
		 */
		private int LONGTEXTSCORE;
		/**
		 * Minimum word count an link title must have not to receive a handicap. DEFAULT: 3
		 */
		private int MINIMUMWORDCOUNT;
		/**
		 * Handicap link receives if its title does not meet the minimum word count (negative number). DEFAULT: -10
		 */
		private int MINIMUMWORDHANDICAP;
		
		/**
		 * The score added to link's when containing one of the preferred classnames. Default: 5
		 */ 
		private int PREFERREDCLASSNAMESCORE;
		
		/**
		 * Handicap for link's when containing one of the unwanted classnames. Default: -4
		 */ 
		private int UNWANTEDCLASSNAMEHANDICAP;
		
		/**
		 * Handicap for link's when containing one of the unwanted terms in the URL. Default: -5
		 */ 
		private int UNWANTEDURLTERMSHANDICAP;
		
		/**
		 * Handicap link's when classname is extremely long . Default: -4
		 */ 
		private int LONGCLASSNAMEHANDICAP;
		
		/**
		 * Length limit when classname is extremely long . Default: 65
		 */ 
		private int LONGCLASSNAMELENGTH;
		
		/**
		 * The score added to link's where the first path part is the same as the target url (i.e. /entertainment, /sports). DEFAULT: 7
		 */
		private int SAMEPATHPARTSCORE;
		/**
		 * Handicap link's text has negative words in it . Default: -5
		 */ 
		private int NEGATIVEWORDSINLINKTEXTHANDICAP;
		
		/**
		 * Handicap link receives if the title only contains capital letters (negative number). DEFAULT: 12
		 */
		private int ALLCAPSHANDICAP;
		
		/**
		 * Handicap link receives if the title starts with 'video:'
		 */
		private int VIDEOHANDICAP;
		
		//Set link size scores
		/**
		 * Handicap link receives if the link is too narrow
		 */
		private int TOONARROWHANDICAP;
		/**
		 * too narrow max width
		 */
		private int TOONARROWWIDTH;
		/**
		 * Handicap link receives if the link is too wide
		 */
		private int TOOWIDEHANDICAP;
		/**
		 * too wide min width
		 */
		private int TOOWIDEWIDTH;
		/**
		 * ideal link width bounds
		 */
		private int PREFERREDWIDTHMINIMUM;
		private int PREFERREDWIDTHMAXIMUM;
		/**
		 * Score link receives if the link is the preferred size
		 */
		private int PREFERREDLINKWIDTHSCORE;
		/**
		 * Handicap class rank receives if the class has only one entry
		 */
		private int ONLYONECLASSENTRYHANDICAP;
		
		/**
		 * Creates the instance and sets all of the variables to their default values
		 */
		
		public Scorer() {
			
			//Set page position defaults
			POSITIONLEFTMOSTXSCORE = 0;
			POSITIONONEFOURTHXSCORE = 18;
			POSITIONTHREEQUARTERXSCORE = -4;
			POSITIONRIGHTMOSTXSCORE = -12;
			
			//Set top region defaults
			TOPREGIONONEHEIGHT = 350;
			TOPREGIONONEHANDICAP = -11;
			TOPREGIONTWOHEIGHT = 475;
			TOPREGIONTWOHANDICAP = -6;
			TOOFARDOWNPAGEHEIGHT = 2500;
			//TOOFARDOWNPAGEHANDICAP = -8;
			
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
			
			//Set class name score
			PREFERREDCLASSNAMESCORE = 6;
			UNWANTEDCLASSNAMEHANDICAP = -5;
			LONGCLASSNAMEHANDICAP = -5;
			LONGCLASSNAMELENGTH = 65;
			
			//Set URL Terms Score
			UNWANTEDURLTERMSHANDICAP = -5;
			
			//Set handicap for all caps
			ALLCAPSHANDICAP = -12;
			
			// set handicap for video:
			VIDEOHANDICAP = -3;
			
			//Set link size scores
			TOONARROWHANDICAP = -3;
			TOONARROWWIDTH = 250;
			TOOWIDEHANDICAP = -3;
			TOOWIDEWIDTH = 728;
			PREFERREDWIDTHMINIMUM = 300;
			PREFERREDWIDTHMAXIMUM = 500;
			PREFERREDLINKWIDTHSCORE = 4;
			
			//set handicap for negative words in link text
			NEGATIVEWORDSINLINKTEXTHANDICAP = -5;
			
			// set handicap for only class entry
			ONLYONECLASSENTRYHANDICAP = -9;
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
		public Scorer positionOneFourthXScore(int newValue) {
			POSITIONONEFOURTHXSCORE = newValue; return this;
		}
		/**
		 * Sets the score of the two-third position for page location scoring
		 * @param newValue	The new value of the two-third position score
		 * @return 			Reference to the current class instance
		 */
		public Scorer positionThreeQuarterXScore(int newValue) {
			POSITIONTHREEQUARTERXSCORE = newValue; return this;
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
		 * Returns the best story URL from the containing StoryFinder's links.
		 * 
		 * Calculates for each link a score and returns the 'href' of the highest in the best 'class' group.
		 * (Determined by average score of links with that class.)
		 * 
		 * @return	URL of best story 
		 */
		public String getStory(String classNameOverride){
			
			ArrayList<String> listOfRankedStories = new ArrayList<String>();
			listOfRankedStories = getStories(1, classNameOverride);
			String topStory = listOfRankedStories.get(0);
			
		}
		
		public String getStories(int topXStories, String classNameOverride) {
			
			//Get all the link scores using the containing class links and the Scorer's numbers
			HashMap<Integer, Integer> linkScores = getLinkScores();
			
			//Get a ranked list of the links' classes based off each classes links' averages
			//ArrayList<String> rankedClasses = getClassesRankedByAveragedLinkScore(linkScores);
			
			ArrayList<StoryColumn> rankedClasses =getClassesScoredByColumn(linkScores);
			
			if (!rankedClasses.isEmpty()){
				StoryFinder.consoleLog("Top Ranked ClassName : " + rankedClasses.get(0).className + " at: " + rankedClasses.get(0).xPosition);
				StoryFinder.consoleLog("With a final score of: " + rankedClasses.get(0).runningScore);
			}
			
			
			//Get the story with the highest score and with the highest ranked class
			String storyURL = "";
			String selectedClassName = "";
			if (classNameOverride != ""){
				selectedClassName = classNameOverride;
				
			}
			else{
				selectedClassName = rankedClasses.get(0).className;
			}

			int highestScore = 0;
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {
				
				//this re-scores the links after all the other scoring so that the correct class
				//will still be selected but now negative stories will be scored down
				currentScore.setValue(scoreLinkText(_links.get(currentScore.getKey()).text, currentScore.getValue()));
				
				//If the current link has the class and a higher score, make it the current story URL
				if ((_links.get(currentScore.getKey()).className.contains(selectedClassName)) &&
					(currentScore.getValue() > highestScore)) {
					storyURL = _links.get(currentScore.getKey()).href;
					highestScore = currentScore.getValue();
				}				
			}
<<<<<<< HEAD
			//writeStoryCSV(linkScores);

=======
			//write the completed CSV file to disk
			/*try {
				FileUtils.writeStringToFile(new File("StoryScores" + UUID.randomUUID() + ".csv"), storyScoringCSV);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
>>>>>>> 1fb6186109f7a9bad5a2837e951fd83f58bec1b2
			//Clean up the URL
			//If it begins with http, do nothing
			if ((storyURL.length() >=4) && (storyURL.substring(0, 4).equals("http"))) {}
			
			//Some sites put // before the substring to keep protocol
			//Since we don't care about protocol at the moment (due to redirects) just remove it
			else if ((storyURL.length() >=2) &&(storyURL.substring(0, 2).equals("//"))) {
				storyURL = storyURL.substring(2);
			}
			
			//Otherwise, add the domain
			else {
				
				//Add a slash before the URL if none exists
				if ((storyURL.length() >=4) && (!storyURL.substring(0, 1).equals("/"))) {
					storyURL = "/" + storyURL;
				}
				
				//Add the domain and set protocol to http
				String targetDomain = URLTool.getDomain(_targetURL);
				storyURL = URLTool.setProtocol("http", targetDomain + storyURL);
			}
			
			return storyURL;
		}
		
		/**
		 * Returns scores for each valid link based on their possibility to be a good story.
		 * The returned map points each link list key to that link's score.
		 * 
		 * Not all links have a score.
		 * 
		 * @return		Map with key the same as the link key in 'links' and the link's score
		 */
		public HashMap<Integer, Integer> getLinkScores() {
			
			//Create the score object. A map is used to maintain relation to the links object
			HashMap<Integer,Integer> linkScores = new HashMap<Integer, Integer>(); 			
			for (int linkIndex = 0; linkIndex < _links.size(); ++linkIndex) {
				linkScores.put(linkIndex, 0);
			}
			
			//Score each link based on the following criteria
			adjustScoresByPageLocation(linkScores);
			adjustScoresByTitleLength(linkScores);
			adjustScoresBySimilarPaths(linkScores);
			adjustScoreIfAllCaps(linkScores);
			adjustScoreForPreferredClassNames(linkScores);
			adjustScoreForUnwantedClassNames(linkScores);
			adjustScoreIfVideoLink(linkScores);
			adjustScoreForLinkWidth(linkScores);
			adjustScoreForClassNameTooLong(linkScores);
			adjustScoreForUnwantedTermsInURL(linkScores);
			
			//Return the scores of each valid link
			return linkScores;
		}
		
		/**
		 * Adjusts the scores of the passed links object according to page location. This
		 * includes height (y-position), width location (x-position), proximity to top of the page,
		 * and visibility.
		 * 
		 * Links not on the visible region (x-position < 0) are removed from the linkScores object.
		 * 
		 * See internal documentation for how x-position score is calculated.
		 * 
		 * @param linkScores	Map of link keys associated with their individual scores
		 */
		public void adjustScoresByPageLocation(HashMap<Integer, Integer> linkScores) {
			
			//First, lets loop through the urls and mark any that fall off the visible page
			ArrayList<Integer> unseenURLs = new ArrayList<Integer>();
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {		
				int currentLinkXPosition = _links.get(currentScore.getKey()).xPosition;
			    if ((currentLinkXPosition < 0) || (currentLinkXPosition > _screenWidth)) {
			    	unseenURLs.add(currentScore.getKey());
			    }
			}
			
			//Delete from the score object the marked unseen urls
			for (Integer currentKey: unseenURLs) {
		    	linkScores.remove(currentKey);
			}
			
			// remove links too far down the page
			ArrayList<Integer> tooFarDownPageURLs = new ArrayList<Integer>();
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {		
				int currentLinkYPosition = _links.get(currentScore.getKey()).yPosition;
			    if ((currentLinkYPosition >= TOOFARDOWNPAGEHEIGHT)) {
			    	tooFarDownPageURLs.add(currentScore.getKey());
			    }
			}
			
			//Delete from the score object urls too far down the page
			for (Integer currentKey: tooFarDownPageURLs) {
		    	linkScores.remove(currentKey);
			}
			
			//Loop through each remaining link and determine its score according to its place on the page
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {			    
				
				//Set the score offset to zero for starters
				int scoreOffset = 0;
				
				//Grab the current link's position
				int linkXPosition = _links.get(currentScore.getKey()).xPosition;
				int linkYPosition = _links.get(currentScore.getKey()).yPosition;
				
				int xPositionScoreAdjust = scoreXPosition(linkXPosition);

				scoreOffset += xPositionScoreAdjust;
				//log the xy score adjustment
				//_links.get(currentScore.getKey()).addScoreLog( "firstPt:" + firstPoint + " secondPt:" + secondPoint + " thirdPt" + thirdPoint + " fourthPt" + fourthPoint + " relativeX:" + linkRelativeXPosition + " rightPt:" + rightPoint );
				_links.get(currentScore.getKey()).addScoreLog( "X Position Score Adjustment: " + xPositionScoreAdjust + " xPos :" + linkXPosition);//+ " W: " + _links.get(currentScore.getKey()).width);//+ " xPos :" + linkXPosition + " regionSlope:" + regionSlope + " yIntercept: " + yIntercept);
				
				//--------Check to see if link is too high------------
				//Apply handicap if link lies in page top regions. 
				//The second regions encompasses the first.
				if (linkYPosition < TOPREGIONONEHEIGHT) {
					scoreOffset += TOPREGIONONEHANDICAP;
					//log the y position score
					_links.get(currentScore.getKey()).addScoreLog( "Y Position Top 1 Score: " + Integer.toString(TOPREGIONONEHANDICAP));
				}
				else if (linkYPosition < TOPREGIONTWOHEIGHT) {
					scoreOffset += TOPREGIONTWOHANDICAP;
					_links.get(currentScore.getKey()).addScoreLog( "Y Position Top 2 Score: " + Integer.toString(TOPREGIONTWOHANDICAP));
				}
				
				//Add the score offset to the link object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		private int scoreXPosition(int xPosition){
			
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
			int linkXPosition = xPosition;
			//Set the points based on the screen size
			int firstPoint = 0;  								//Named for simpler clarity
			int secondPoint = _screenWidth*3/12;					//One fourth mark
			int thirdPoint = _screenWidth*3/4;						//three fourths mark
			int fourthPoint = _screenWidth;  					////Also added for clarity
			
			int linkRelativeXPosition = 0;
			double xPositionScoreAdjustment = 0;
			
			//Based on the location of the link, determine the two points that it's between and their scores.
			int leftPoint, leftScore, rightPoint, rightScore = 0;
			if (linkXPosition < secondPoint) {
				leftPoint = 0; leftScore = POSITIONLEFTMOSTXSCORE;
				rightPoint = secondPoint - firstPoint; rightScore = POSITIONONEFOURTHXSCORE;
				linkRelativeXPosition = linkXPosition - firstPoint;
				//xPositionScoreAdjustment = 3;
				
			}
			else if (linkXPosition < thirdPoint) {
				leftPoint = 0; leftScore = POSITIONONEFOURTHXSCORE;
				rightPoint = thirdPoint - secondPoint; rightScore = POSITIONTHREEQUARTERXSCORE;
				linkRelativeXPosition = linkXPosition - secondPoint;
				//xPositionScoreAdjustment = 8;
			}
			else 	{
				leftPoint = 0; leftScore = POSITIONTHREEQUARTERXSCORE;
				rightPoint = fourthPoint - thirdPoint; rightScore = POSITIONRIGHTMOSTXSCORE;
				linkRelativeXPosition = linkXPosition - thirdPoint;
				//xPositionScoreAdjustment = -5;
			}
			
			
			//Use the points as the x and the scores as y, get the slope				
			double regionSlope = (double) (rightScore - leftScore)/(rightPoint - leftPoint);
			
			//Get the y-intercept
			int yIntercept = (int) (leftScore - regionSlope*leftPoint);
			
			//Finally, get the score for the link location
			xPositionScoreAdjustment = (double) (linkRelativeXPosition * regionSlope) + yIntercept;
			
			return (int) (xPositionScoreAdjustment);
		}
		
		/**
		 * Adjusts the scores of the passed link scores based on title length. This includes being too short, somewhat short,
		 * long, and meeting word counts or not.
		 * 
		 * Links with titles not meeting the minimum text length are removed from the passed link scores object.
		 * 
		 * @param linkScores	Map of link keys associated with their individual scores
		 */
		public void adjustScoresByTitleLength(HashMap<Integer, Integer> linkScores) {
			
			//First, lets loop through the urls and mark any that have no text or only a few characters
			ArrayList<Integer> lowTextURLs = new ArrayList<Integer>();
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {		
				String currentLinkText = _links.get(currentScore.getKey()).text;
			    if (currentLinkText.length() <= MINIMUMTEXTLENGTH) {
			    	lowTextURLs.add(currentScore.getKey());
			    }
			}
			
			//Delete from the score object the marked low text urls
			for (Integer currentKey: lowTextURLs) {
		    	linkScores.remove(currentKey);
			}
			
			//Loop through the links and adjust scores by length and word count
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Grab the current link's text and length
				String urlText = _links.get(currentScore.getKey()).text;
				int urlTextLength = urlText.length();
				
				//If the text is too short, give the url a handicap
				if (urlTextLength <= SHORTTEXTLENGTH) {
					scoreOffset += SHORTTEXTHANDICAP;
					_links.get(currentScore.getKey()).addScoreLog( "Text too short penalty: " + Integer.toString(SHORTTEXTHANDICAP));
				}

				//If the text is long enough, give the url a higher score
				else if (urlTextLength >= LONGTEXTLENGTH) {
					scoreOffset += LONGTEXTSCORE;
					_links.get(currentScore.getKey()).addScoreLog( "Text good length score: " + Integer.toString(LONGTEXTSCORE));
				}
				
				//If there are not enough words in the text, penalize the url
				int wordCount = urlText.trim().split("\\s+").length;
				if (wordCount < MINIMUMWORDCOUNT) {
					scoreOffset += MINIMUMWORDHANDICAP;
					_links.get(currentScore.getKey()).addScoreLog( "Text too few words penalty: " + Integer.toString(MINIMUMWORDHANDICAP));
				}

				//Add the score offset to the link object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		
		/**
		 * Increases the score of an link that shares the same first path part of the target URL.
		 * 
		 * In other words, this is the path part right after the domain part. For example, the first path part of 
		 * 'boston.com/entertainment' is 'entertainment'.
		 * 
		 * @param linkScores	Map of link keys associated with their individual scores
		 */
		public void adjustScoresBySimilarPaths(HashMap<Integer, Integer> linkScores) {
			
			//Grab the first path part of the target url for comparison
			String targetURLPathPart = getFirstPartOfURIPath(_targetURL);
				
			//If we are not at the topmost domain (no path after domain), then score the urls
			if (targetURLPathPart.length() > 1) {
				
				//Loop through each link and adjust its score based on similar first path part
				for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {			    
					
					//Set the score offset to zero
					int scoreOffset = 0;
					
					//Grab the current link's href and path part
					String urlHref = _links.get(currentScore.getKey()).href;
					String urlPathPart = getFirstPartOfURIPath(urlHref);
					//StoryFinder.consoleLog("UrlPathScoring - StoryHref:" + urlHref + " path: " + urlPathPart);
					//StoryFinder.consoleLog("UrlPathScoring - TargetHref:" + _targetURL + " path: " +targetURLPathPart);
					//If the targetURL and current link path parts are the same, increment the score
					if (targetURLPathPart.equals(urlPathPart)) {
						scoreOffset += SAMEPATHPARTSCORE;
						_links.get(currentScore.getKey()).addScoreLog( "URL Same Path Score: " + Integer.toString(SAMEPATHPARTSCORE));
					}
					
					//Add the score offset to the link object
					currentScore.setValue(currentScore.getValue() + scoreOffset);
				}
			}
		}
		
		/**
		 * Applies handicap to any link's score with a title consisting of all caps
		 * 
		 * @param linkScores	Map of link keys associated with their individual scores
		 */
		public void adjustScoreIfAllCaps(HashMap<Integer, Integer> linkScores) {
							
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's text
				String urlText = _links.get(currentScore.getKey()).text;
				
				//If the text is in all caps, apply the reduction
				if (urlText.equals(urlText.toUpperCase())) {
					scoreOffset += ALLCAPSHANDICAP;
					_links.get(currentScore.getKey()).addScoreLog( "All Caps Title Penalty: " + Integer.toString(ALLCAPSHANDICAP));
				}
				
				//Add the score offset to the link object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		
		public void adjustScoreForLinkWidth(HashMap<Integer, Integer> linkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's width
				int linkWidth = _links.get(currentScore.getKey()).width;
				
				//If the text is in all caps, apply the reduction
				if (linkWidth <= TOONARROWWIDTH) {
					scoreOffset += TOONARROWHANDICAP;
					_links.get(currentScore.getKey()).addScoreLog( "Link too narrow: " + Integer.toString(TOONARROWHANDICAP));
				} else if ((linkWidth >= PREFERREDWIDTHMINIMUM) && (linkWidth <= PREFERREDWIDTHMAXIMUM)){
					scoreOffset += PREFERREDLINKWIDTHSCORE;
					_links.get(currentScore.getKey()).addScoreLog( "Link size preferred score: " + Integer.toString(PREFERREDLINKWIDTHSCORE));
				} else if (linkWidth >= TOOWIDEWIDTH){
					scoreOffset += TOOWIDEHANDICAP;
					_links.get(currentScore.getKey()).addScoreLog( "Link too wide: " + Integer.toString(TOOWIDEHANDICAP));
				}
				
				//Add the score offset to the link object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		
		public void adjustScoreIfVideoLink(HashMap<Integer, Integer> linkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's text
				String urlText = _links.get(currentScore.getKey()).text;
				urlText = urlText.toLowerCase();
				//If the text is in all caps, apply the reduction
				if ((urlText.length() >= 6) && (urlText.substring(0, 6).equals("video:"))) {
					scoreOffset += VIDEOHANDICAP;
					_links.get(currentScore.getKey()).addScoreLog( "Video Link Penalty: " + Integer.toString(VIDEOHANDICAP));
				}
				
				//Add the score offset to the link object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		
		
		public void adjustScoreForPreferredClassNames(HashMap<Integer, Integer> linkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's classname
				String classNameText = _links.get(currentScore.getKey()).className.toLowerCase();
				String preferredClassNames[] = new String [] {"story","news-item","relatedListTitle",
						"headline", "content", "feature"};
				
				if (stringContainsItemFromListCapInsensitive(classNameText, preferredClassNames)) {
					scoreOffset += PREFERREDCLASSNAMESCORE;
					_links.get(currentScore.getKey()).addScoreLog( "Preferred ClassName Score: " + Integer.toString(PREFERREDCLASSNAMESCORE));
				}
				
				//Add the score offset to the link object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		
		public void adjustScoreForUnwantedClassNames(HashMap<Integer, Integer> linkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's classname
				String classNameText = _links.get(currentScore.getKey()).className.toLowerCase();
				String unwantedClassNames[] = new String [] {"mnu","menu","nav","navigation","header","promo",
						"subscribe", "feed", "trending", "ledestory", "feat-widget", "related-topics",
						"carousel", "kicker-link", "display-above", "secondary", "gallery", "gameContent",
						"partners", "pgem-item-link", "relatedtopics", "explore-graphic"};
				
				if (stringContainsItemFromListCapInsensitive(classNameText, unwantedClassNames)) {
					scoreOffset += UNWANTEDCLASSNAMEHANDICAP;
					_links.get(currentScore.getKey()).addScoreLog( "Unwanted ClassName Handicap: " + Integer.toString(UNWANTEDCLASSNAMEHANDICAP));
				}
				
				//Add the score offset to the link object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		public void adjustScoreForUnwantedTermsInURL(HashMap<Integer, Integer> linkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's classname
				String URLText = _links.get(currentScore.getKey()).href.toLowerCase();
				String unwantedURLTerms[] = new String [] {"sponsored", "video", "gallery",
						"slideshow", "sponsor", "interactives"};
				
				if (stringContainsItemFromListCapInsensitive(URLText, unwantedURLTerms)) {
					scoreOffset += UNWANTEDURLTERMSHANDICAP;
					_links.get(currentScore.getKey()).addScoreLog( "Unwanted URL Term Handicap: " + Integer.toString(UNWANTEDCLASSNAMEHANDICAP));
				}
				
				//Add the score offset to the link object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		
		public void adjustScoreForClassNameTooLong(HashMap<Integer, Integer> linkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's classname
				String classNameText = _links.get(currentScore.getKey()).className.toLowerCase();
				
				if (classNameText.length() >= LONGCLASSNAMELENGTH) {
					scoreOffset += LONGCLASSNAMEHANDICAP;
					_links.get(currentScore.getKey()).addScoreLog( "ClassName too long Handicap: " + Integer.toString( LONGCLASSNAMEHANDICAP));
				}
				
				//Add the score offset to the link object
				currentScore.setValue(currentScore.getValue() + scoreOffset);
			}
		}
		
		private int scoreLinkText(String linkText, int linkScore){
			
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
			negativeWordList.add("mass shooting");
			negativeWordList.add("deadliest");
			negativeWordList.add("victim");
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
			negativeWordList.add("robbery");
			negativeWordList.add("Follow Us");
			negativeWordList.add("RSS");
			
			for (String currentWord : negativeWordList) {

				Pattern currentWordPattern = Pattern.compile("(?i)" + currentWord);
				Matcher wordMatcher = currentWordPattern.matcher(linkText);
				while (wordMatcher.find()) {
					linkScore += NEGATIVEWORDSINLINKTEXTHANDICAP;
				}
			}
			
			return linkScore;
		}
		
		/**
		 * Returns array list of all the links' classes ranked by average link score. The first class (at 0)
		 * has the highest score.
		 * 
		 * The score is calculated by taking the sum of the scores of all the links in that class and column
		 * 
		 * 
		 * @param linkScores	Map of link keys associated with their individual scores
		 * @return
		 */
		private ArrayList<StoryColumn> getClassesScoredByColumn(HashMap<Integer, Integer> linkScores) {
			
			//Prepare to store each class' use count and total score
			HashMap<String, StoryColumn> classScores = new HashMap<String, StoryColumn>();
			int ColumnMinimumXScore = 2;
			
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {		
				
				//Get the url's classes and xpos from the class attribute
				String classColumnString = _links.get(currentScore.getKey()).className + ":x:" + _links.get(currentScore.getKey()).xPosition;
				
				//Loop through each class if any exist
				if (classColumnString.length() > 0) {
					
					if (classScores.get(classColumnString) == null){
						StoryColumn firstInsertColumn = new StoryColumn();
						firstInsertColumn.className = _links.get(currentScore.getKey()).className;
						firstInsertColumn.xPosition = _links.get(currentScore.getKey()).xPosition;
						firstInsertColumn.runningScore = currentScore.getValue();
						if(scoreXPosition(firstInsertColumn.xPosition) >= ColumnMinimumXScore){
							classScores.put(classColumnString, firstInsertColumn);
						}
					}
					else{
						StoryColumn adjustScoreColumn = classScores.get(classColumnString);
						adjustScoreColumn.runningScore += currentScore.getValue();
						classScores.put(classColumnString, adjustScoreColumn);
					}
					
				}
			}
			
			//Get the column scores and put them into a TreeMap for sorting
			TreeMap<Integer, StoryColumn> sortedColumnScores = new TreeMap<Integer, StoryColumn>(Collections.reverseOrder());
			for (Map.Entry<String, StoryColumn> currentColumnScore : classScores.entrySet()) {
				
				sortedColumnScores.put(currentColumnScore.getValue().runningScore, currentColumnScore.getValue());
			}
			
			//Turn the sorted map into the final array and return it
			ArrayList<StoryColumn> rankedColumns = new ArrayList<StoryColumn>();
			for (Map.Entry<Integer, StoryColumn> currentScore : sortedColumnScores.entrySet()) {
				rankedColumns.add(currentScore.getValue());
			}
			
			return rankedColumns;
		}
		
		/**
		 * Returns array list of all the links' classes ranked by average link score. The first class (at 0)
		 * has the highest average.
		 * 
		 * The average is calculated by taking the sum of the scores of all the links that have a class
		 * and dividing by that number of links.
		 * 
		 * THE AVERAGES GET PENALIZED FOR ONLY HAVING ONE CLASS ENTRY
		 * 
		 * @param linkScores	Map of link keys associated with their individual scores
		 * @return
		 */
		private ArrayList<String> getClassesRankedByAveragedLinkScore(HashMap<Integer, Integer> linkScores) {
			
			//Prepare to store each class' use count and total score
			HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
			HashMap<String, Integer> classTotalScores = new HashMap<String, Integer>();
			
			//Loop through the urls and total up the classes with the urls' scores
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {		
				
				//Get the url's classes from the class attribute
				String classString = _links.get(currentScore.getKey()).className;
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
				//This is where the deduction for only one class entry happens
				//StoryFinder.consoleLog("find average of :" + currentClassScore.getKey() + "- :" + currentClassScore.getValue() + " Div by: " + classCounts.get(currentClassScore.getKey()));
				if(classCounts.get(currentClassScore.getKey()) == 1){
					averageScore += ONLYONECLASSENTRYHANDICAP;
					//StoryFinder.consoleLog("Scoring down class for one entry: " + currentClassScore.getKey());
				}
				//StoryFinder.consoleLog("Average - " + averageScore);
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
			else if ((URIString.substring(0,1).equals("/")) || URIString.substring(1, 2).equals("/")) {
				//StoryFinder.consoleLog("inside relative path:" + uriParts[1]);
				return uriParts[1];
			}
			
			//Otherwise, return the path part after the domain
			else if (uriParts.length >= 4) {
				return uriParts[3];
			}
			
			//Or return nothing
			else {
				//StoryFinder.consoleLog("return nothing");
				return "";
				}
		}
		
		
		private boolean stringContainsItemFromListCapInsensitive(String inputString, String[] items)
		{
			inputString = inputString.toLowerCase();
		    for(int i =0; i < items.length; i++)
		    {
		        if(inputString.contains(items[i].toLowerCase()))
		        {
		            return true;
		        }
		    }
		    return false;
		}
		/**
		 * Writes a csv file with all the stories and scores 
		 * this is intended to make tuning the story finder easier
		 */
		public void writeStoryCSV(HashMap<Integer, Integer> linkScores) {
			
			String storyScoringCSV = "";
			String CSVSeparator = "|";
			storyScoringCSV += "URL" + CSVSeparator;
			storyScoringCSV += "className" + CSVSeparator;
			storyScoringCSV += "text" + CSVSeparator;
			storyScoringCSV += "Score" + CSVSeparator;
			storyScoringCSV += "X Pos" + CSVSeparator;
			storyScoringCSV += "ScoreExplanation" + CSVSeparator;
			storyScoringCSV += "Re-Score" + CSVSeparator;
			storyScoringCSV += System.getProperty("line.separator");
			
			for (Map.Entry<Integer, Integer> currentScore : linkScores.entrySet()) {
				
				//this is to build a sortable and useful CSV file
				storyScoringCSV += _links.get(currentScore.getKey()).href + CSVSeparator;
				storyScoringCSV += _links.get(currentScore.getKey()).className + CSVSeparator;
				storyScoringCSV += _links.get(currentScore.getKey()).text + CSVSeparator;
				storyScoringCSV += currentScore.getValue() + CSVSeparator;
				storyScoringCSV += _links.get(currentScore.getKey()).xPosition + CSVSeparator;
				storyScoringCSV += _links.get(currentScore.getKey()).scoreExplanationLog + CSVSeparator;
				storyScoringCSV += System.getProperty("line.separator");
						
			}
			//write the completed CSV file to disk
			String siteName = _targetURL;
			siteName = siteName.replaceAll("/", "");
			//siteName = siteName.replaceAll(".", "");
			siteName = siteName.replaceAll(";", "");
			siteName = siteName.replaceAll("/", "");
			siteName = siteName.replaceAll("\\\\", "");
			siteName = siteName.replaceAll("/", "");
			siteName = siteName.replaceAll("http", "");
			siteName = siteName.replaceAll(":", "");
			
			try {
				FileUtils.writeStringToFile(new File("zz" + siteName + UUID.randomUUID() + ".csv"), storyScoringCSV);
			} catch (IOException e) {
				e.printStackTrace();
			}
			

		}
		
	}
}

