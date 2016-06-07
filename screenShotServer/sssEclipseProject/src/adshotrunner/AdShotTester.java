package adshotrunner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Used to unit test AdShotRunner
 *
 */
public class AdShotTester {

	public static void main(String[] args) throws Exception {
		
		ArrayList<String> tagURLs = new ArrayList<String>();
		tagURLs.add("www.newsweek.com/assange-google-not-what-it-seems-279447?piano_t=1");
		tagURLs.add("www.desmoinesregister.com/story/news/politics/2014/12/08/iowa-digital-smartphone-drivers-license/20114979/");
		tagURLs.add("www.wired.com/2014/12/nokia-here-autonomous-car-maps/");
		/*tagURLs.add("nydailynews.com");
		tagURLs.add("boston.com");
		tagURLs.add("chicago.eater.com");
		tagURLs.add("chicagotribune.com");
		tagURLs.add("dallasnews.com");
		/*tagURLs.add("denverpost.com");
		tagURLs.add("latimes.com");
		tagURLs.add("nydailynews.com");
		tagURLs.add("nj.com");
		tagURLs.add("nypost.com");
		tagURLs.add("omaha.com");
		tagURLs.add("online.wsj.com");
		tagURLs.add("philly.com");
		tagURLs.add("www.sfgate.com");
		tagURLs.add("slashdot.org");
		tagURLs.add("www.usatoday.com");
		tagURLs.add("washingtonpost.com");
		//*/
		//////tagURLs.add("nytimes.com");
		//////tagURLs.add("suntimes.com");
		
		ArrayList<String> fillerTags = getFillerTags();
		
		AdShotter testRunner = new AdShotter();
		
		for(String currentURL : tagURLs) {
			for(String currentTag: fillerTags) {
				testRunner.addTag(currentURL, currentTag);
			}	
			
			testRunner.addTag(currentURL, "https://s3.amazonaws.com/asr-tagimages/fillers/filler-300x250-B.jpg", 2);
			testRunner.addTag(currentURL, "https://s3.amazonaws.com/asr-tagimages/fillers/filler-300x250-C.jpg", 3);
			
			testRunner.addTag(currentURL, "https://s3.amazonaws.com/asr-tagimages/fillers/filler-728x90-B.jpg", 2);
			testRunner.addTag(currentURL, "https://s3.amazonaws.com/asr-tagimages/fillers/filler-728x90-C.jpg", 3);
		}
		
		List<BufferedImage> adShots = testRunner.getAdShots();
		for(BufferedImage currentAdShot : adShots) {
			System.out.println("Saving a screenshot");
			try {saveImageAsPNG(currentAdShot, "ScreenShot" + System.nanoTime() + ".png");}
			catch (Exception e) {
				System.out.println("Could not save screenshot");
			}
		}
	}
	
	public static ArrayList<String> getFillerTags() {
		
		ArrayList<String> tagList = new ArrayList<String>();
		
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-120x240.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-120x60.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-120x600.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-120x90.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-125x125.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-160x600.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-180x150.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-234x60.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-240x400.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-250x250.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-300x250.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-336x280.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-468x60.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-728x90.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/filler-88x31.jpg");
		
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-120x30.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-230x33.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-300x600.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-500x350.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-550x480.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-720x300.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-728x210.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-94x15.jpg");
		tagList.add("https://s3.amazonaws.com/asr-tagimages/fillers/nsfiller-250x970.jpg");
				
		return tagList;
	}
	
	private static void saveImageAsPNG(BufferedImage imageToSave, String filepath) throws IOException {
		
		//Write the image as a PNG
		ImageIO.write(imageToSave, "png", new File(filepath));         	
	}
	
}
