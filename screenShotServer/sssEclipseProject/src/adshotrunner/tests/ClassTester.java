package adshotrunner.tests;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.json.JSONException;

public class ClassTester {
	
	public static void main(String[] args) throws JSONException, MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));		
		
		// TODO Auto-generated method stub
		System.out.println("This is a pre storyfinder message");
		/*String storyURL = new StoryFinder("http://nytimes.com")
									 .Scorer()
									 	.positionLeftmostXScore(1).positionOneThirdXScore(11).positionOneHalfXScore(20)
									 	.positionOptimalXScore(21).positionTwoThirdXScore(20).positionRightmostXScore(1)
									 	.topRegionOneHeight(351).topRegionOneHandicap(-10)
									 	.topRegionTwoHeight(476).topRegionTwoHandicap(-5)
									 	.minimumTextLength(4).shortTextLength(10).shortTextHandicap(-11)
									 	.longTextLength(17).longTextScore(12)
									 	.minimumWordCount(4).minimumWordHandicap(-9)
									 	.samePathPartsScore(8).allCapsHandicap(-11)
									 	.getStory();*/
		System.out.println("This is the first output after the storyfinder runs");
		//System.out.println(storyURL);
		//**/
		//System.out.println(StoryChecker.score("http://www.ketv.com/news/police-man-reports-attempted-murder-to-get-out-of-speeding-ticket/27146358"));
	}
	

}
