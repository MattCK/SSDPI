package adshotrunner;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.json.JSONException;

import adshotrunner.StoryFinder.Calculator;

public class ClassTester {

	public static void main(String[] args) throws JSONException, MalformedURLException, URISyntaxException {
		// TODO Auto-generated method stub
		ArrayList<Integer> some = new StoryFinder("http://www.boston.com/entertainment").Calculator().getScoredStories();
	}

}
