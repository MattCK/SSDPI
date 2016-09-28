package adshotrunner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import adshotrunner.StoryFinder;
import adshotrunner.utilities.FileStorageClient;
import adshotrunner.utilities.URLTool;

public class StoryFinderTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ArrayList<String> sectionList = new ArrayList<String>();
		/*sectionList.add("http://www.alexcityoutlook.com/category/opinion/");
		sectionList.add("http://www.andalusiastarnews.com/category/schools/");*/
		//sectionList.add("http://www.palmbeachpost.com/s/living/");
		
		//sectionList.add("http://newsok.com/entertainment");
		
		/*sectionList.add("http://www.si.com/olympics");
		sectionList.add("http://www.sandmountainreporter.com/sports/");
		sectionList.add("http://nypost.com/business/");
		sectionList.add("http://www.investors.com/news/");
		sectionList.add("http://www.aol.com/finance/");//actually wrong
		sectionList.add("http://www.cbssports.com/college-football/");
		sectionList.add("http://fortune.com/autos/");
		sectionList.add("http://thebiglead.com/category/golf/");
		sectionList.add("http://observer.com/culture/");
		sectionList.add("http://www.eonline.com/news/tv");
		sectionList.add("http://www.cincinnati.com/business/");
		sectionList.add("http://www.forbes.com/technology/");
		sectionList.add("http://www.indystar.com/travel/");
		sectionList.add("http://www.theverge.com/tech");
		sectionList.add("http://www.indystar.com/life/");
		sectionList.add("http://www.latimes.com/sports/");
		sectionList.add("http://www.mercurynews.com/business");
		sectionList.add("http://www.nydailynews.com/entertainment");
		sectionList.add("http://www.dispatch.com/content/sections/sports/index.html?cmpid=tplnnv");
		sectionList.add("http://www.sfgate.com/entertainment/");
		sectionList.add("http://www.msnbc.com/economy");
		sectionList.add("http://www.reuters.com/news/technology");
		sectionList.add("http://www.alaskajournal.com/oil_gas");
		sectionList.add("http://www.foxsports.com/college-football");
		sectionList.add("http://www.nytimes.com/pages/politics/index.html");
		sectionList.add("http://www.usatoday.com/sports/");
		sectionList.add("http://www.dailynews.com/entertainment");
		sectionList.add("http://www.huffingtonpost.com/dept/whats-working");
		sectionList.add("http://www.laweekly.com/marijuana");
		sectionList.add("http://variety.com/v/film/");
		sectionList.add("http://www.cleveland.com/community/");*/
		sectionList.add("http://www.annistonstar.com/features/");
		/*sectionList.add("http://www.thearabtribune.com/sports/");
		sectionList.add("http://www.adn.com/section/outdoors-adventure/");
		sectionList.add("http://www.anchoragepress.com/post-categories/food-drink");
		sectionList.add("http://www.deltadiscovery.com/section/village_telegraph");
		sectionList.add("http://realestate.boston.com/");
		sectionList.add("http://www.boston.com/section/culture");
		sectionList.add("http://www.sbnation.com/nhl");
		sectionList.add("http://www.sfchronicle.com/foodandhome/");
		sectionList.add("https://www.washingtonpost.com/national/?nid=top_nav_national");
		sectionList.add("http://arizonasports.com/phoenix-suns/");
		sectionList.add("http://www.nbcsports.com/nba");
		sectionList.add("http://www.theblaze.com/business/");
		sectionList.add("http://www.cbssports.com/soccer/");
		sectionList.add("http://www.sandiegouniontribune.com/news/entertainment/");
		sectionList.add("http://www.wsj.com/news/us");
		sectionList.add("http://www.dailydot.com/topics/dot-esports/");
		sectionList.add("http://www.inc.com/technology");
		sectionList.add("http://www.tampabay.com/news/politics/");
		sectionList.add("http://www.deseretnews.com/moneywise");
		sectionList.add("http://dailycaller.com/section/sports/");
		sectionList.add("http://www.sltrib.com/entertainment/");
		sectionList.add("http://www.freep.com/opinion/");
		sectionList.add("http://www.miamiherald.com/opinion/");
		sectionList.add("http://www.ibtimes.com/national");
		sectionList.add("http://www.masslive.com/sports/");
		sectionList.add("http://money.cnn.com/technology/?iid=Tech_Nav");
		sectionList.add("http://www.theadvocate.com/baton_rouge/opinion/");
		sectionList.add("http://www.nola.com/eat-drink/");
		sectionList.add("http://www.foxnews.com/entertainment.html");
		sectionList.add("http://www.omaha.com/news/world/");
		sectionList.add("http://www.azcentral.com/things-to-do/");
		sectionList.add("http://www.tucsonnewsnow.com/category/5168/tucson-news");
		sectionList.add("http://www.westport-news.com/opinion/");
		sectionList.add("http://www.postandcourier.com/food/");
		sectionList.add("http://www.usnews.com/education");
		sectionList.add("http://www.cnet.com/apple/");
		sectionList.add("http://sports.yahoo.com/nfl/");
		sectionList.add("http://www.hollywoodreporter.com/music");
		sectionList.add("http://www.tulsaworld.com/scene/");
		sectionList.add("http://www.newsworks.org/index.php/art-entertainment-sports");
		sectionList.add("http://www.pcworld.com/howto/");
		sectionList.add("http://www.livescience.com/environment");
		sectionList.add("http://www.csmonitor.com/Business");
		sectionList.add("http://www.chicagotribune.com/suburbs/");
		sectionList.add("http://www.toledoblade.com/opinion");
		sectionList.add("http://juneauempire.com/neighbors");*/
		

		
		TreeMap<String, String> sectionsAndStories = new TreeMap<String, String>();
		
		for (String currentSection : sectionList) {

			String foundStoryURL = "";
			try {
				ArrayList<String> foundStories = new StoryFinder(URLTool.setProtocol("http",currentSection)).Scorer().getStories(3);
				System.out.println("StoryFinder found: " + foundStories);
				if (!foundStories.isEmpty()) {
					for (int storyIndex = 0; storyIndex < foundStories.size(); ++storyIndex) {
						sectionsAndStories.put(currentSection + "#" + storyIndex, foundStories.get(storyIndex));
					}
				}
				
			} catch (MalformedURLException | UnsupportedEncodingException
					| URISyntaxException e) {
				// TODO Auto-generated catch block
				System.out.println("Error finding story");
				e.printStackTrace();
			}
			
		}
		
		boolean makeHTMLSuccess = writeFoundStoriesToHTML(sectionsAndStories);
		System.out.println("Wrote stories html to disk: " + makeHTMLSuccess);

	}
	
	public static boolean writeFoundStoriesToHTML(Map<String, String> sectionAndStoryHash){
		boolean writeSuccess = false;
		String htmlOut = "";
		htmlOut +=  "<!DOCTYPE html PUBLIC \"-//IETF//DTD HTML 2.0//EN\"> <HTML> <HEAD> <TITLE> Section </TITLE> </HEAD> <BODY><table style=\"width:100%\" border = \"2\"><tr><th>Section</th><th>Story</th> </tr>";
		
		for (Map.Entry<String, String> currentSection : sectionAndStoryHash.entrySet()) {			    
			htmlOut += "    <tr> <td><a href=\" "+ currentSection.getKey() + " \">"+ currentSection.getKey() + "</a></td> <td><a href=\" "+ currentSection.getValue() + " \">"+ currentSection.getValue() + "</a></td>  </tr>";
			//System.out.println("sect: " + currentSection.getKey());
			//System.out.println("story: "  + currentSection.getValue());
			
		}
		
		htmlOut += "  </table> </BODY> </HTML>";
		try {
			FileUtils.writeStringToFile(new File("Stories-" + UUID.randomUUID() + ".html"), htmlOut);
			writeSuccess = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//System.out.println(htmlOut);
		return writeSuccess;
	}

}
