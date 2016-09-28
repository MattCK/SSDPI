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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.utilities.URLTool;
import adshotrunner.utilities.MySQLDatabase;

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
	private static String getLinkJSONFromURL(String url, int viewWidth, int viewHeight, String userAgent, String ExceptionID, String ExceptionClass) {
		
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
	            url, Integer.toString(viewWidth), Integer.toString(viewHeight), userAgent, ExceptionID, ExceptionClass        	
            });
            //Get the string returned from phantomjs
            String thisLine = "";
            BufferedReader commandLineInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            //StoryFinder.consoleLog("after the to call the readbuffer");
           // while ((thisLine = commandLineInput.readLine()) != null) {
            //	phantomJSResponse += thisLine;
            //}
            phantomJSResponse = commandLineInput.readLine();
            String[] commandArray = new String[]{
    	            "phantomjs/phantomjs", 
    	            "javascript/retrievePossibleStoriesFromURL.js",
    	            url, Integer.toString(viewWidth), Integer.toString(viewHeight), userAgent, ExceptionID, ExceptionClass};
            //StoryFinder.consoleLog("RunString: " + Arrays.toString(commandArray));
            //StoryFinder.consoleLog("FirstLineRead: " + phantomJSResponse);
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
	
	/**
	 * HTML ID Exception in the database for this URL 
	 */
	private String _dbExceptionID;
	/**
	 * HTML ClassName Exception in the database for this URL 
	 */
	private String _dbExceptionClassName;
	
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
		
		//get possible exceptions from the DB
		Map<String, String> storyException = null;
		_dbExceptionID = "";
		_dbExceptionClassName = "";
		try {
			storyException = getException(url);
			if (storyException != null){
				_dbExceptionID = storyException.get("containerID");
				_dbExceptionClassName = storyException.get("className");
			}
				
		} catch (SQLException e) {
			StoryFinder.consoleLog("Database Error When finding Exception for: " + url);
		}
		
		
		//loop through the phantomjs request trying different user agents
		String userAgents[] = new String [] {"googlebot","firefox", "msnbot", "firefoxlinux"};
		int userAgentIncrementor = 0;
		int linkCountMinimumNormal = 10;
		int linkCountMinimumWException = 5;
		int linkCountMinimum = linkCountMinimumNormal;
		boolean useException = false;
		
		if ((_dbExceptionID != "") || (_dbExceptionClassName != "")){
			linkCountMinimum = linkCountMinimumWException;
			useException = true;
		}
		
		List<StoryLink> retrievedLinks = new ArrayList<StoryLink>();
		while((userAgentIncrementor < userAgents.length) && (retrievedLinks.size() < linkCountMinimum)){
		
			StoryFinder.consoleLog("Trying phantomJS with agent:" + userAgents[userAgentIncrementor]);
			//Get the possible story links using phantomjs
			String linkJSON = getLinkJSONFromURL(_targetURL, _screenWidth, _screenHeight, userAgents[userAgentIncrementor], _dbExceptionID, _dbExceptionClassName);
			
			//StoryFinder.consoleLog("full String: " + linkJSON);
			
	        //Get the immutable link info as array of maps
			retrievedLinks = getStoryLinksFromJSON(linkJSON);
			
			userAgentIncrementor++;
			//if this is an exception and we've gotten all the way through all the user agents
			//without enough results loop through again without the exception
			if((userAgentIncrementor >= userAgents.length) && (_dbExceptionID != "" && _dbExceptionClassName != "")){
				StoryFinder.consoleLog("Unable to process exception: restarting without");
				_dbExceptionID = "";
				_dbExceptionClassName = "";
			}
		}
		
		_links = retrievedLinks;
	}
	
	public static Map<String, String> getException(String targetURL) throws SQLException {
		
		//Get the domain with subdomain of the url. The protocol type is not important. It is necessary for getDomain.
		String urlDomain = URLTool.getDomain(URLTool.setProtocol("http", targetURL));
		
		//Check the database to see if any entries matching the domain exist
		ResultSet exceptionsSet = MySQLDatabase.executeQuery("SELECT * " + 
															 "FROM exceptionsStoryFinder " +
															 "WHERE ESF_url LIKE '" + urlDomain + "%'");
				
		//If a match was found, use the container ID and class of the longest matching url part
		String urlPart = "", containerID = "", className = "";
		while (exceptionsSet.next()) {
						
			//Get the current URL part
			String currentURLPart = exceptionsSet.getString("ESF_url");
			
			//If the url part is a substring of the target URL, store its info if it is the longest
			if (targetURL.toLowerCase().contains(currentURLPart.toLowerCase())) {
				
				//See if the new part is longer than the current
				if (currentURLPart.length() > urlPart.length()) {
					
					//Store the new exception
					urlPart = currentURLPart;
					containerID = exceptionsSet.getString("ESF_containerID");
					className = exceptionsSet.getString("ESF_className");
				}
			}			
		}
		
		//If no matches in the database were found, return null
		if (urlPart.isEmpty()) {return null;}
		
		//Otherwise, put the info in a map and return it
		else {
			Map<String, String> storyException = new HashMap<String, String>();
			storyException.put("url", urlPart);
			storyException.put("containerID", containerID);
			storyException.put("className", className);
			return storyException;
		}
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
	private List<StoryLink> getStoryLinksFromJSON(String linkJSON) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		
		//Turn the returned JSON into an array of objects
		/*try {
			FileUtils.writeStringToFile(new File("storyFinderJSON/" + new Date().getTime() + ".json"), linkJSON);
			//StoryFinder.consoleLog("Saved StoryFinder JSON");
		} catch (IOException e) {
			StoryFinder.consoleLog("Could not save StoryFinder JSON");
		}*/
		Gson gson = new Gson();
		Type arrayStoryLinksToken = new TypeToken<ArrayList<StoryLink>>(){}.getType();
		ArrayList<StoryLink> storyLinkList = new ArrayList<StoryLink>();
		try{
			storyLinkList = gson.fromJson(linkJSON, arrayStoryLinksToken);
		}
		catch(com.google.gson.JsonSyntaxException e){
			StoryFinder.consoleLog("unable to parse linkJSON");
			
		}
		//Get the primary domain of the StoryFinder URL
		String primaryDomain = getURIDomain(_targetURL);
		
		//Loop through the list removing null and empty href elements and setting null class to empty string
		
			Iterator<StoryLink> linksIterator = storyLinkList.iterator();
			while(linksIterator.hasNext()){
				
				StoryLink currentLink = linksIterator.next();
				//StoryFinder.consoleLog("processing: " + currentLink.href);
				//Check for // at beginning of href and add protocol if not there
				if ((currentLink.href != null) && (!currentLink.href.isEmpty()) && (currentLink.href.length() >= 2) && (currentLink.href.substring(0, 2).equals("//"))){
					//StoryFinder.consoleLog("Inside //: " + currentLink.href );
					currentLink.href = "http:" + currentLink.href;
				}
				if ((currentLink.text == null || currentLink.text == "" || currentLink.text.isEmpty()) && (currentLink.title != null && !currentLink.title.isEmpty())){
					currentLink.text = currentLink.title;
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
		public HashMap<StoryLink,Integer> columnStoryLinks;
		public StoryColumn(){
			className  = "";
			xPosition = 0;
			runningScore = 0;
			columnStoryLinks =  new HashMap<StoryLink, Integer>();
		}
		public boolean goodColumn( boolean doubleStrict){
			boolean currentlyGood = true;
			int storyCount = columnStoryLinks.size();
			int topScore = 0;
			int secondScore = 0;
			int thirdScore = 0;
			List<Integer> tempStoryList=new ArrayList<Integer>();
			for (Map.Entry<StoryLink, Integer> storyLinkScore : columnStoryLinks.entrySet()) {
				tempStoryList.add(storyLinkScore.getValue());
			}
			Collections.sort(tempStoryList, Collections.reverseOrder());
			if(tempStoryList.size() >= 3){
				thirdScore = tempStoryList.get(2);
			}
			if(tempStoryList.size() >= 2){
				secondScore = tempStoryList.get(1);
			}
			if(tempStoryList.size() >= 1){
				topScore = tempStoryList.get(0);
			}
			
			double SCOREMULTIPLIERMINIMUM = 2.5;
			//if the total score is only 2.5x more than the top score then this isn't a good column
			if((double)topScore * SCOREMULTIPLIERMINIMUM > (double)runningScore ){
				currentlyGood = false;
				//StoryFinder.consoleLog("total only 2.5x: " + currentlyGood);
			}
			//if there are fewer than 3 stories then this isn't a good column
			int MINIMUMSTORYCOUNT = 3;
			if(storyCount <= MINIMUMSTORYCOUNT){
				currentlyGood = false;
				//StoryFinder.consoleLog("min count: " + currentlyGood);
			}
			if(doubleStrict){
				//if the average score is less than .65 of the top story then this isn't a good column
				double SCOREAVERAGEMULTIPLIER = .40;
				if(((double)runningScore / (double)storyCount) < ((double)topScore * SCOREAVERAGEMULTIPLIER)){
					currentlyGood = false;
					//StoryFinder.consoleLog(".65 avg: " + currentlyGood);
				}
				double SCOREMULTIPLIER = .65;
				//if the second and third score are too low compared to the first it isn't a good column
				if( ((double)((double)secondScore + (double)thirdScore)/(double)2) < ((double)topScore * SCOREMULTIPLIER)) {
					currentlyGood = false;
					//StoryFinder.consoleLog("2nd and 3rd low: " + currentlyGood);
				}
			}
			else{
				//if the average score is less than .65 of the top story then this isn't a good column
				double SCOREAVERAGEMULTIPLIER = .30;
				if(((double)runningScore / (double)storyCount) < ((double)topScore * SCOREAVERAGEMULTIPLIER)){
					currentlyGood = false;
					//StoryFinder.consoleLog(".65 avg: " + currentlyGood);
				}
				
			}
			
			/*StoryFinder.consoleLog("column entries:");
			for (Map.Entry<StoryLink, Integer> storyLinkScore : columnStoryLinks.entrySet()) {
				StoryFinder.consoleLog("l: " + storyLinkScore.getKey().href + " s: " + storyLinkScore.getValue() + " x: " + storyLinkScore.getKey().xPosition);
			}*/
			
			return currentlyGood;
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
		 * Handicap for link's when containing one of the unwanted terms in the URL. Default: -10
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
			UNWANTEDURLTERMSHANDICAP = -10;
			
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
		public String getStory() {
			
			ArrayList<String> listOfRankedStories = getStories(1);
			return listOfRankedStories.get(0);
			
		}
		
		public ArrayList<String> getStories(int maxStories) {
			
			//Get all the link scores using the containing class links and the Scorer's numbers
			HashMap<StoryLink, Integer> storyLinkScores = getLinkScores();
			
			//print list of stories for debugging purposes
			//for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {
				//StoryFinder.consoleLog("link: " + storyLinkScore.getKey().href);}
			
			/*if((_dbExceptionID != "") || (_dbExceptionClassName != "")){
				//this is just a reminder that we're not scoring any columns if there is an exception
				storyLinkScores = storyLinkScores;
				//StoryFinder.consoleLog("Inside DB Exception");
			}*/
			//else{
				ArrayList<StoryColumn> rankedByClass = getColumnsScoredWClassNameStrictXPos(storyLinkScores);
				//if the first ranked column is scored poorly then retry with less strict columns
				//StoryFinder.consoleLog("After get strict x w class");
				if (!rankedByClass.isEmpty() && rankedByClass.get(0).goodColumn(true)){
					//StoryFinder.consoleLog("strict x w class marked as good");
					//if the strict x pos and classname had a good first column grab all the columns of that class
					String topClassName = (!rankedByClass.isEmpty()) ? rankedByClass.get(0).className : "";
					if (!rankedByClass.isEmpty()){
						StoryFinder.consoleLog("Top Ranked ClassName : " + rankedByClass.get(0).className + " at: " + rankedByClass.get(0).xPosition);
						StoryFinder.consoleLog("With a final score of: " + rankedByClass.get(0).runningScore);
					}
					storyLinkScores = new HashMap<StoryLink, Integer>();
					//this adds every story from every column with a mataching classname where the column is good
					for (StoryColumn currColumn : rankedByClass) {
						if ((currColumn.className == topClassName) && (currColumn.goodColumn(true))){
							//StoryFinder.consoleLog("adding good columns to the storylinklist");
							storyLinkScores.putAll(currColumn.columnStoryLinks);
						}
						
					}
	
				}
				//if the strictx pos and classname did not return a good column
				else{
					//StoryFinder.consoleLog("strict x no class scoring");
					ArrayList<StoryColumn> looseColumns = getColumnsScoredWStrictXPosNoClass(storyLinkScores);
					if(!looseColumns.isEmpty() && looseColumns.get(0).goodColumn(false)){
						//StoryFinder.consoleLog("strict x no class scoring: had good columns");
						storyLinkScores = looseColumns.get(0).columnStoryLinks;
					}
					else{
						//StoryFinder.consoleLog("with class no X scoring");
						ArrayList<StoryColumn> justClassName = getColumnsScoredWClassNoXPos(storyLinkScores);
						if (!justClassName.isEmpty() && justClassName.get(0).goodColumn(false)){
							storyLinkScores = justClassName.get(0).columnStoryLinks;
						}
						else{
							//StoryFinder.consoleLog("just remove bad positions scoring");
							ArrayList<StoryColumn> reallyBadPositionsRemoved = getColumnsRemoveBadPositions(storyLinkScores);
							if(!reallyBadPositionsRemoved.isEmpty() && reallyBadPositionsRemoved.get(0).goodColumn(false)){
								storyLinkScores = reallyBadPositionsRemoved.get(0).columnStoryLinks;
							}
							else{
								//if none of the scoring worked return the whole list
								//this is here just to make it easier to follow
								storyLinkScores = storyLinkScores;
							}
						}
					}
	
				}
			//}
			
			if (storyLinkScores.isEmpty()) {return new ArrayList<String>();}
			
			
			//Get the story with the highest score and with the highest ranked class
			HashMap<String, Integer> topStories = new HashMap<String, Integer>();
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {
				
				//this re-scores the links after all the other scoring so that the correct class
				//will still be selected but now negative stories will be scored down
				storyLinkScore.setValue(scoreLinkText(storyLinkScore.getKey().text, storyLinkScore.getValue()));

				//Clean up the URL
				//If it begins with http, do nothing
				String storyURL = storyLinkScore.getKey().href;
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

				topStories.put(storyURL, storyLinkScore.getValue());
								
			}
			//re-added for debuging
			//writeStoryCSV(storyLinkScores);
			
			
			Map<String, Integer> sortedStories =
					topStories.entrySet().stream()
			                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
			                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
			                        (e1, e2) -> e2, LinkedHashMap::new));
			
			int storyIndex = 0;
			ArrayList<String> finalStories = new ArrayList<String>();
			Iterator<Map.Entry<String, Integer>> storyIterator = sortedStories.entrySet().iterator();
			while ((storyIndex < maxStories) && (storyIterator.hasNext())) {
				Map.Entry<String, Integer> story = storyIterator.next();
				finalStories.add(story.getKey());
				++storyIndex;
			}
			
			return finalStories;
			
			/*
			ArrayList<String> testStories = new ArrayList<String>();
			testStories.add("http://www.foxsports.com/college-football/story/pulse-shooting-orlando-rodney-sumter-tim-tebow-080316");
			testStories.add("http://www.foxsports.com/college-football/gallery/ohio-state-buckeyes-2016-season-preview-why-they-can-win-the-national-championship-082216");
			testStories.add("http://www.foxsports.com/college-football/gallery/cfb-preseason-predictions-sec-alabama-lsu-tennessee-georgia-bruce-feldman-082216");
			
			return testStories;
			 */
		}
		
		/**
		 * Returns scores for each valid link based on their possibility to be a good story.
		 * The returned map points each link list key to that link's score.
		 * 
		 * Not all links have a score.
		 * 
		 * @return		Map with key the same as the link key in 'links' and the link's score
		 */
		public HashMap<StoryLink, Integer> getLinkScores() {
			
			//Create the map to connect a StoryLink with its score
			HashMap<StoryLink,Integer> storyLinkScores = new HashMap<StoryLink, Integer>(); 			
			for (StoryLink story : _links) {
				//StoryFinder.consoleLog("Inside Get LinkScores: " + story.href);
				storyLinkScores.put(story, 0);
			}
			
			//Score each link based on the following criteria
			adjustScoresByPageLocation(storyLinkScores);
			adjustScoresByTitleLength(storyLinkScores);
			//for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {
				//StoryFinder.consoleLog("After score title length: " + storyLinkScore.getKey().href);}
			adjustScoresBySimilarPaths(storyLinkScores);
			adjustScoreIfAllCaps(storyLinkScores);
			adjustScoreForPreferredClassNames(storyLinkScores);
			adjustScoreForUnwantedClassNames(storyLinkScores);
			adjustScoreIfVideoLink(storyLinkScores);
			adjustScoreForLinkWidth(storyLinkScores);
			adjustScoreForClassNameTooLong(storyLinkScores);
			adjustScoreForUnwantedTermsInURL(storyLinkScores);
			
			
			//Return the scores of each valid link
			return storyLinkScores;
		}
		
		/**
		 * Adjusts the scores of the passed links object according to page location. This
		 * includes height (y-position), width location (x-position), proximity to top of the page,
		 * and visibility.
		 * 
		 * Links not on the visible region (x-position < 0) are removed from the storyLinkScores object.
		 * 
		 * See internal documentation for how x-position score is calculated.
		 * 
		 * @param storyLinkScores	Map of StoryLinks associated with their individual scores
		 */
		public void adjustScoresByPageLocation(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//First, lets loop through the urls and mark any that fall off the visible page
			ArrayList<StoryLink> unseenURLs = new ArrayList<StoryLink>();
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {		
				int currentLinkXPosition = storyLinkScore.getKey().xPosition;
			    if ((currentLinkXPosition < 0) || (currentLinkXPosition > _screenWidth)) {
			    	unseenURLs.add(storyLinkScore.getKey());
			    }
			}
			
			//Delete from the score object the marked unseen urls
			for (StoryLink currentKey: unseenURLs) {
		    	storyLinkScores.remove(currentKey);
			}
			
			// remove links too far down the page
			ArrayList<StoryLink> tooFarDownPageURLs = new ArrayList<StoryLink>();
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {		
				int currentLinkYPosition = storyLinkScore.getKey().yPosition;
			    if ((currentLinkYPosition >= TOOFARDOWNPAGEHEIGHT)) {
			    	tooFarDownPageURLs.add(storyLinkScore.getKey());
			    }
			}
			
			//Delete from the score object urls too far down the page
			for (StoryLink currentKey: tooFarDownPageURLs) {
		    	storyLinkScores.remove(currentKey);
			}
			
			//Loop through each remaining link and determine its score according to its place on the page
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {			    
				
				//Set the score offset to zero for starters
				int scoreOffset = 0;
				
				//Grab the current link's position
				int linkXPosition = storyLinkScore.getKey().xPosition;
				int linkYPosition = storyLinkScore.getKey().yPosition;
				
				int xPositionScoreAdjust = scoreXPosition(linkXPosition);

				scoreOffset += xPositionScoreAdjust;
				//log the xy score adjustment
				//storyLinkScore.getKey().addScoreLog( "firstPt:" + firstPoint + " secondPt:" + secondPoint + " thirdPt" + thirdPoint + " fourthPt" + fourthPoint + " relativeX:" + linkRelativeXPosition + " rightPt:" + rightPoint );
				storyLinkScore.getKey().addScoreLog( "X Position Score Adjustment: " + xPositionScoreAdjust + " xPos :" + linkXPosition);//+ " W: " + storyLinkScore.getKey().width);//+ " xPos :" + linkXPosition + " regionSlope:" + regionSlope + " yIntercept: " + yIntercept);
				
				//--------Check to see if link is too high------------
				//Apply handicap if link lies in page top regions. 
				//The second regions encompasses the first.
				if (linkYPosition < TOPREGIONONEHEIGHT) {
					scoreOffset += TOPREGIONONEHANDICAP;
					//log the y position score
					storyLinkScore.getKey().addScoreLog( "Y Position Top 1 Score: " + Integer.toString(TOPREGIONONEHANDICAP));
				}
				else if (linkYPosition < TOPREGIONTWOHEIGHT) {
					scoreOffset += TOPREGIONTWOHANDICAP;
					storyLinkScore.getKey().addScoreLog( "Y Position Top 2 Score: " + Integer.toString(TOPREGIONTWOHANDICAP));
				}
				
				//Add the score offset to the link object
				storyLinkScore.setValue(storyLinkScore.getValue() + scoreOffset);
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
		 * @param storyLinkScores	Map of StoryLinks associated with their individual scores
		 */
		public void adjustScoresByTitleLength(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//First, lets loop through the urls and mark any that have no text or only a few characters
			ArrayList<StoryLink> lowTextURLs = new ArrayList<StoryLink>();
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {		
				String currentLinkText = storyLinkScore.getKey().text;
			    if (currentLinkText.length() <= MINIMUMTEXTLENGTH) {
			    	//StoryFinder.consoleLog("link to be removed txt: " + currentLinkText + " length:" + currentLinkText.length());
			    	lowTextURLs.add(storyLinkScore.getKey());
			    }
			}
			
			//Delete from the score object the marked low text urls
			for (StoryLink currentKey: lowTextURLs) {
		    	storyLinkScores.remove(currentKey);
			}
			
			//Loop through the links and adjust scores by length and word count
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Grab the current link's text and length
				String urlText = storyLinkScore.getKey().text;
				int urlTextLength = urlText.length();
				
				//If the text is too short, give the url a handicap
				if (urlTextLength <= SHORTTEXTLENGTH) {
					scoreOffset += SHORTTEXTHANDICAP;
					storyLinkScore.getKey().addScoreLog( "Text too short penalty: " + Integer.toString(SHORTTEXTHANDICAP));
				}

				//If the text is long enough, give the url a higher score
				else if (urlTextLength >= LONGTEXTLENGTH) {
					scoreOffset += LONGTEXTSCORE;
					storyLinkScore.getKey().addScoreLog( "Text good length score: " + Integer.toString(LONGTEXTSCORE));
				}
				
				//If there are not enough words in the text, penalize the url
				int wordCount = urlText.trim().split("\\s+").length;
				if (wordCount < MINIMUMWORDCOUNT) {
					scoreOffset += MINIMUMWORDHANDICAP;
					storyLinkScore.getKey().addScoreLog( "Text too few words penalty: " + Integer.toString(MINIMUMWORDHANDICAP));
				}

				//Add the score offset to the link object
				storyLinkScore.setValue(storyLinkScore.getValue() + scoreOffset);
			}
		}
		
		/**
		 * Increases the score of an link that shares the same first path part of the target URL.
		 * 
		 * In other words, this is the path part right after the domain part. For example, the first path part of 
		 * 'boston.com/entertainment' is 'entertainment'.
		 * 
		 * @param storyLinkScores	Map of StoryLinks associated with their individual scores
		 */
		public void adjustScoresBySimilarPaths(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Grab the first path part of the target url for comparison
			String targetURLPathPart = getFirstPartOfURIPath(_targetURL);
				
			//If we are not at the topmost domain (no path after domain), then score the urls
			if (targetURLPathPart.length() > 1) {
				
				//Loop through each link and adjust its score based on similar first path part
				for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {			    
					
					//Set the score offset to zero
					int scoreOffset = 0;
					
					//Grab the current link's href and path part
					String urlHref = storyLinkScore.getKey().href;
					String urlPathPart = getFirstPartOfURIPath(urlHref);
					//StoryFinder.consoleLog("UrlPathScoring - StoryHref:" + urlHref + " path: " + urlPathPart);
					//StoryFinder.consoleLog("UrlPathScoring - TargetHref:" + _targetURL + " path: " +targetURLPathPart);
					//If the targetURL and current link path parts are the same, increment the score
					if (targetURLPathPart.equals(urlPathPart)) {
						scoreOffset += SAMEPATHPARTSCORE;
						storyLinkScore.getKey().addScoreLog( "URL Same Path Score: " + Integer.toString(SAMEPATHPARTSCORE));
					}
					
					//Add the score offset to the link object
					storyLinkScore.setValue(storyLinkScore.getValue() + scoreOffset);
				}
			}
		}
		
		/**
		 * Applies handicap to any link's score with a title consisting of all caps
		 * 
		 * @param storyLinkScores	Map of StoryLinks associated with their individual scores
		 */
		public void adjustScoreIfAllCaps(HashMap<StoryLink, Integer> storyLinkScores) {
							
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's text
				String urlText = storyLinkScore.getKey().text;
				
				//If the text is in all caps, apply the reduction
				if (urlText.equals(urlText.toUpperCase())) {
					scoreOffset += ALLCAPSHANDICAP;
					storyLinkScore.getKey().addScoreLog( "All Caps Title Penalty: " + Integer.toString(ALLCAPSHANDICAP));
				}
				
				//Add the score offset to the link object
				storyLinkScore.setValue(storyLinkScore.getValue() + scoreOffset);
			}
		}
		
		public void adjustScoreForLinkWidth(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's width
				int linkWidth = storyLinkScore.getKey().width;
				
				//If the text is in all caps, apply the reduction
				if (linkWidth <= TOONARROWWIDTH) {
					scoreOffset += TOONARROWHANDICAP;
					storyLinkScore.getKey().addScoreLog( "Link too narrow: " + Integer.toString(TOONARROWHANDICAP));
				} else if ((linkWidth >= PREFERREDWIDTHMINIMUM) && (linkWidth <= PREFERREDWIDTHMAXIMUM)){
					scoreOffset += PREFERREDLINKWIDTHSCORE;
					storyLinkScore.getKey().addScoreLog( "Link size preferred score: " + Integer.toString(PREFERREDLINKWIDTHSCORE));
				} else if (linkWidth >= TOOWIDEWIDTH){
					scoreOffset += TOOWIDEHANDICAP;
					storyLinkScore.getKey().addScoreLog( "Link too wide: " + Integer.toString(TOOWIDEHANDICAP));
				}
				
				//Add the score offset to the link object
				storyLinkScore.setValue(storyLinkScore.getValue() + scoreOffset);
			}
		}
		
		public void adjustScoreIfVideoLink(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's text
				String urlText = storyLinkScore.getKey().text;
				urlText = urlText.toLowerCase();
				//If the text is in all caps, apply the reduction
				if ((urlText.length() >= 6) && (urlText.substring(0, 6).equals("video:"))) {
					scoreOffset += VIDEOHANDICAP;
					storyLinkScore.getKey().addScoreLog( "Video Link Penalty: " + Integer.toString(VIDEOHANDICAP));
				}
				
				//Add the score offset to the link object
				storyLinkScore.setValue(storyLinkScore.getValue() + scoreOffset);
			}
		}
		
		
		public void adjustScoreForPreferredClassNames(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's classname
				String classNameText = storyLinkScore.getKey().className.toLowerCase();
				String preferredClassNames[] = new String [] {"story","news-item","relatedListTitle",
						"headline", "content", "feature"};
				
				if (stringContainsItemFromListCapInsensitive(classNameText, preferredClassNames)) {
					scoreOffset += PREFERREDCLASSNAMESCORE;
					storyLinkScore.getKey().addScoreLog( "Preferred ClassName Score: " + Integer.toString(PREFERREDCLASSNAMESCORE));
				}
				
				//Add the score offset to the link object
				storyLinkScore.setValue(storyLinkScore.getValue() + scoreOffset);
			}
		}
		
		public void adjustScoreForUnwantedClassNames(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's classname
				String classNameText = storyLinkScore.getKey().className.toLowerCase();
				String unwantedClassNames[] = new String [] {"mnu","menu","nav","navigation","header","promo",
						"subscribe", "feed", "trending", "ledestory", "feat-widget", "related-topics",
						"carousel", "kicker-link", "display-above", "secondary", "gallery", "gameContent",
						"partners", "pgem-item-link", "relatedtopics", "explore-graphic"};
				
				if (stringContainsItemFromListCapInsensitive(classNameText, unwantedClassNames)) {
					scoreOffset += UNWANTEDCLASSNAMEHANDICAP;
					storyLinkScore.getKey().addScoreLog( "Unwanted ClassName Handicap: " + Integer.toString(UNWANTEDCLASSNAMEHANDICAP));
				}
				
				//Add the score offset to the link object
				storyLinkScore.setValue(storyLinkScore.getValue() + scoreOffset);
			}
		}
		public void adjustScoreForUnwantedTermsInURL(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's classname
				String URLText = storyLinkScore.getKey().href.toLowerCase();
				String unwantedURLTerms[] = new String [] {"sponsored", "video", "gallery",
						"slideshow", "sponsor", "interactives", "deals"};
				
				if (stringContainsItemFromListCapInsensitive(URLText, unwantedURLTerms)) {
					scoreOffset += UNWANTEDURLTERMSHANDICAP;
					storyLinkScore.getKey().addScoreLog( "Unwanted URL Term Handicap: " + Integer.toString(UNWANTEDCLASSNAMEHANDICAP));
				}
				
				//Add the score offset to the link object
				storyLinkScore.setValue(storyLinkScore.getValue() + scoreOffset);
			}
		}
		
		public void adjustScoreForClassNameTooLong(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Loop through each link and handicap its score if it is in all capital letters
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {			    
				
				//Set the score offset to zero
				int scoreOffset = 0;
				
				//Get the url's classname
				String classNameText = storyLinkScore.getKey().className.toLowerCase();
				
				if (classNameText.length() >= LONGCLASSNAMELENGTH) {
					scoreOffset += LONGCLASSNAMEHANDICAP;
					storyLinkScore.getKey().addScoreLog( "ClassName too long Handicap: " + Integer.toString( LONGCLASSNAMEHANDICAP));
				}
				
				//Add the score offset to the link object
				storyLinkScore.setValue(storyLinkScore.getValue() + scoreOffset);
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
		 * @param storyLinkScores	Map of StoryLinks associated with their individual scores
		 * @return
		 */
		//should add if top scoring column is == (or maybe at least 4 stories?)to it's top story then should 
		//redo with a wider range than exact column and drop classname
		//maybe needs to happen after this function...
		
		private ArrayList<StoryColumn> getColumnsRemoveBadPositions(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Prepare to store each class' use count and total score
			HashMap<String, StoryColumn> classScores = new HashMap<String, StoryColumn>();
			int ColumnMinimumXScore = 2;
			
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {		
				
				//Get the url's classes and xpos from the class attribute
				String classColumnString = "fullList";
			
				if (classScores.get(classColumnString) == null){
					StoryColumn firstInsertColumn = new StoryColumn();
					firstInsertColumn.className = storyLinkScore.getKey().className;
					firstInsertColumn.xPosition = storyLinkScore.getKey().xPosition;
					firstInsertColumn.runningScore = storyLinkScore.getValue();
					firstInsertColumn.columnStoryLinks.put(storyLinkScore.getKey(), storyLinkScore.getValue());
					if(scoreXPosition(firstInsertColumn.xPosition) >= ColumnMinimumXScore){
						classScores.put(classColumnString, firstInsertColumn);
					}
				}
				else{
					StoryColumn adjustScoreColumn = classScores.get(classColumnString);
					adjustScoreColumn.runningScore += storyLinkScore.getValue();
					adjustScoreColumn.columnStoryLinks.put(storyLinkScore.getKey(), storyLinkScore.getValue());
					if(scoreXPosition(adjustScoreColumn.xPosition) >= ColumnMinimumXScore){
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
			for (Map.Entry<Integer, StoryColumn> storyLinkScore : sortedColumnScores.entrySet()) {
				rankedColumns.add(storyLinkScore.getValue());
			}
			
			return rankedColumns;
		}
		
		/**
		 * Returns array list of all the links' classes ranked by average link score. The first class (at 0)
		 * has the highest score.
		 * 
		 * The score is calculated by taking the sum of the scores of all the links in that class and column
		 * 
		 * 
		 * @param storyLinkScores	Map of StoryLinks associated with their individual scores
		 * @return
		 */
		//should add if top scoring column is == (or maybe at least 4 stories?)to it's top story then should 
		//redo with a wider range than exact column and drop classname
		//maybe needs to happen after this function...
		
		private ArrayList<StoryColumn> getColumnsScoredWClassNoXPos(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Prepare to store each class' use count and total score
			HashMap<String, StoryColumn> classScores = new HashMap<String, StoryColumn>();
			int ColumnMinimumXScore = 2;
			
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {		
				
				//Get the url's classes and xpos from the class attribute
				String classColumnString = storyLinkScore.getKey().className;
				
				//Loop through each class if any exist
				if (classColumnString.length() > 0) {
					
					if (classScores.get(classColumnString) == null){
						StoryColumn firstInsertColumn = new StoryColumn();
						firstInsertColumn.className = storyLinkScore.getKey().className;
						firstInsertColumn.xPosition = storyLinkScore.getKey().xPosition;
						firstInsertColumn.runningScore = storyLinkScore.getValue();
						firstInsertColumn.columnStoryLinks.put(storyLinkScore.getKey(), storyLinkScore.getValue());
						if(scoreXPosition(firstInsertColumn.xPosition) >= ColumnMinimumXScore){
							classScores.put(classColumnString, firstInsertColumn);
						}
					}
					else{
						StoryColumn adjustScoreColumn = classScores.get(classColumnString);
						adjustScoreColumn.runningScore += storyLinkScore.getValue();
						adjustScoreColumn.columnStoryLinks.put(storyLinkScore.getKey(), storyLinkScore.getValue());
						if(scoreXPosition(adjustScoreColumn.xPosition) >= ColumnMinimumXScore){
							classScores.put(classColumnString, adjustScoreColumn);
						}
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
			for (Map.Entry<Integer, StoryColumn> storyLinkScore : sortedColumnScores.entrySet()) {
				rankedColumns.add(storyLinkScore.getValue());
			}
			
			return rankedColumns;
		}
		
		/**
		 * Returns array list of all the columns with strict column x pos and using classname
		 * 
		 * The score is calculated by taking the sum of the scores of all the links in that class and column
		 * 
		 * 
		 * @param storyLinkScores	Map of StoryLinks associated with their individual scores
		 * @return
		 */

		
		private ArrayList<StoryColumn> getColumnsScoredWClassNameStrictXPos(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Prepare to store each class' use count and total score
			HashMap<String, StoryColumn> classScores = new HashMap<String, StoryColumn>();
			int ColumnMinimumXScore = 2;
			
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {		
				
				//Get the url's classes and xpos from the class attribute
				String classColumnString = storyLinkScore.getKey().className + ":x:" + storyLinkScore.getKey().xPosition;
				
				//Loop through each class if any exist
				if (classColumnString.length() > 0) {
					
					if (classScores.get(classColumnString) == null){
						StoryColumn firstInsertColumn = new StoryColumn();
						firstInsertColumn.className = storyLinkScore.getKey().className;
						firstInsertColumn.xPosition = storyLinkScore.getKey().xPosition;
						firstInsertColumn.runningScore = storyLinkScore.getValue();
						firstInsertColumn.columnStoryLinks.put(storyLinkScore.getKey(), storyLinkScore.getValue());
						if(scoreXPosition(firstInsertColumn.xPosition) >= ColumnMinimumXScore){
							classScores.put(classColumnString, firstInsertColumn);
						}
					}
					else{
						StoryColumn adjustScoreColumn = classScores.get(classColumnString);
						adjustScoreColumn.runningScore += storyLinkScore.getValue();
						adjustScoreColumn.columnStoryLinks.put(storyLinkScore.getKey(), storyLinkScore.getValue());
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
			for (Map.Entry<Integer, StoryColumn> storyLinkScore : sortedColumnScores.entrySet()) {
				rankedColumns.add(storyLinkScore.getValue());
			}
			
			return rankedColumns;
		}
		
		/**
		 * Returns array list of all the columns with strict column x pos and using classname
		 * 
		 * The score is calculated by taking the sum of the scores of all the links in that class and column
		 * 
		 * 
		 * @param storyLinkScores	Map of StoryLinks associated with their individual scores
		 * @return
		 */

		
		private ArrayList<StoryColumn> getColumnsScoredWStrictXPosNoClass(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Prepare to store each class' use count and total score
			HashMap<String, StoryColumn> classScores = new HashMap<String, StoryColumn>();
			int ColumnMinimumXScore = 2;
			
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {		
				
				//Get the url's classes and xpos from the class attribute
				String classColumnString = "x:" + storyLinkScore.getKey().xPosition;
				
				//Loop through each class if any exist
				if (classColumnString.length() > 0) {
					
					if (classScores.get(classColumnString) == null){
						StoryColumn firstInsertColumn = new StoryColumn();
						firstInsertColumn.className = storyLinkScore.getKey().className;
						firstInsertColumn.xPosition = storyLinkScore.getKey().xPosition;
						firstInsertColumn.runningScore = storyLinkScore.getValue();
						firstInsertColumn.columnStoryLinks.put(storyLinkScore.getKey(), storyLinkScore.getValue());
						if(scoreXPosition(firstInsertColumn.xPosition) >= ColumnMinimumXScore){
							classScores.put(classColumnString, firstInsertColumn);
						}
					}
					else{
						StoryColumn adjustScoreColumn = classScores.get(classColumnString);
						adjustScoreColumn.runningScore += storyLinkScore.getValue();
						adjustScoreColumn.columnStoryLinks.put(storyLinkScore.getKey(), storyLinkScore.getValue());
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
			for (Map.Entry<Integer, StoryColumn> storyLinkScore : sortedColumnScores.entrySet()) {
				rankedColumns.add(storyLinkScore.getValue());
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
		 * @param storyLinkScores	Map of StoryLinks associated with their individual scores
		 * @return
		 */
		private ArrayList<String> getClassesRankedByAveragedLinkScore(HashMap<StoryLink, Integer> storyLinkScores) {
			
			//Prepare to store each class' use count and total score
			HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
			HashMap<String, Integer> classTotalScores = new HashMap<String, Integer>();
			
			//Loop through the urls and total up the classes with the urls' scores
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {		
				
				//Get the url's classes from the class attribute
				String classString = storyLinkScore.getKey().className;
				String[] urlClasses = classString.trim().split("\\s+");
				
				//Loop through each class if any exist
				if (classString.length() > 0) {
					for (String currentClass : urlClasses) {
						
						//If this class has not been seen before, begin an entry for it
						if (!classCounts.containsKey(currentClass)) {
							classCounts.put(currentClass, 1);
							classTotalScores.put(currentClass, storyLinkScore.getValue());
						}
						
						//Otherwise, add the current score to its score and increment the count
						else {
							classCounts.put(currentClass, classCounts.get(currentClass) + 1);
							classTotalScores.put(currentClass, classTotalScores.get(currentClass) + storyLinkScore.getValue());
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
			for (Map.Entry<Integer, String> storyLinkScore : averagedClassScores.entrySet()) {
				rankedClasses.add(storyLinkScore.getValue());
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
		public void writeStoryCSV(HashMap<StoryLink, Integer> storyLinkScores) {
			
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
			
			for (Map.Entry<StoryLink, Integer> storyLinkScore : storyLinkScores.entrySet()) {
				
				//this is to build a sortable and useful CSV file
				storyScoringCSV += storyLinkScore.getKey().href + CSVSeparator;
				storyScoringCSV += storyLinkScore.getKey().className + CSVSeparator;
				storyScoringCSV += storyLinkScore.getKey().text + CSVSeparator;
				storyScoringCSV += storyLinkScore.getValue() + CSVSeparator;
				storyScoringCSV += storyLinkScore.getKey().xPosition + CSVSeparator;
				storyScoringCSV += storyLinkScore.getKey().scoreExplanationLog + CSVSeparator;
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

