package adshotrunner;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.json.JSONException;

import adshotrunner.StoryFinder.Calculator;

public class ClassTester {
	
	public static void main(String[] args) throws JSONException, MalformedURLException, URISyntaxException {
		// TODO Auto-generated method stub
		String storyURL = new StoryFinder("http://www.boston.com/entertainment")
									 .Calculator()
									 	.positionLeftmostXScore(1).positionOneThirdXScore(11).positionOneHalfXScore(20)
									 	.positionOptimalXScore(21).positionTwoThirdXScore(20).positionRightmostXScore(1)
									 	.topRegionOneHeight(351).topRegionOneHandicap(-10)
									 	.topRegionTwoHeight(476).topRegionTwoHandicap(-5)
									 	.minimumTextLength(4).shortTextLength(10).shortTextHandicap(-11)
									 	.longTextLength(17).longTextScore(12)
									 	.minimumWordCount(4).minimumWordHandicap(-9)
									 	.samePathPartsScore(8).allCapsHandicap(-11)
									 	.getStory();
		System.out.println(storyURL);
	}
	

}
