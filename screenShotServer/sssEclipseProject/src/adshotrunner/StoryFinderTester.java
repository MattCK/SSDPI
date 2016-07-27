package adshotrunner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import adshotrunner.StoryFinder;
import adshotrunner.utilities.FileStorageClient;
import adshotrunner.utilities.URLTool;

public class StoryFinderTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//System.out.println(args[0]);
		
		String sectionURL = "http://www.annistonstar.com/opinion/";
		//sectionURL = args[0]
		
		String foundStoryURL = "";
		try {
			foundStoryURL = new StoryFinder(URLTool.setProtocol("http",sectionURL)).Scorer().getStory();
			System.out.println("StoryFinder found: " + foundStoryURL);
		} catch (MalformedURLException | UnsupportedEncodingException
				| URISyntaxException e) {
			// TODO Auto-generated catch block
			System.out.println("Error finding story");
			e.printStackTrace();
		}
		

	}

}
