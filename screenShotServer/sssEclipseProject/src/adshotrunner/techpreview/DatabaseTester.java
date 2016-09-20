package adshotrunner.techpreview;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import adshotrunner.utilities.MySQLDatabase;
import adshotrunner.utilities.URLTool;

public class DatabaseTester {

	public static void main(String[] args) throws SQLException {
		System.out.println(getException("www.chicagotribune.com/"));
		System.out.println(getException("www.chicagotribune.com/news/"));
		System.out.println(getException("www.chicagotribune.com/news/opinion"));
		System.out.println(getException("www.chicagotribune.com/sports"));
		System.out.println(getException("www.chicagotribune.com/somethingelse"));
		System.out.println(getException("www.notindatabase.com"));
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
}
