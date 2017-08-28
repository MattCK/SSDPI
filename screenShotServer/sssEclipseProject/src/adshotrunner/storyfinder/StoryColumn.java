package adshotrunner.storyfinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoryColumn {

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
