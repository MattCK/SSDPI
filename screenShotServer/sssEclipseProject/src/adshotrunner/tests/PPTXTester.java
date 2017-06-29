package adshotrunner.tests;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.docx4j.openpackaging.parts.PresentationML.SlidePart;

import adshotrunner.archive.AdShotOriginal;
import adshotrunner.archive.CampaignPowerPointOriginal;

public class PPTXTester {

	public static void main(String[] args) throws Exception {
		
		File backgroundImage = new File("config/DefaultBackground.jpg");
		BufferedImage image = ImageIO.read(new File("config/wkycBackground.jpg"));
		AdShotOriginal tempAdShot = AdShotOriginal.create("google.com");
		tempAdShot.setImage(image);
		tempAdShot.setFinalURL("http://google.com");
		
		CampaignPowerPointOriginal testPowerPoint = new CampaignPowerPointOriginal("Campaign Name", "01/02/2013", "FFFFFF", backgroundImage);
		testPowerPoint.addSlide(tempAdShot);
		testPowerPoint.save("temp.pptx");
//		PowerPoint tempPowerpoint = new PowerPoint(backgroundImage);
//		SlidePart newSlide = tempPowerpoint.addNewSlide();
//		tempPowerpoint.addImageToSlide(newSlide, image, 100, 100, 100, 100);
//		tempPowerpoint.addTextToSlide(newSlide, "Some Text", 50, 50, 500, "Arial", 10, "00FF00");
//		tempPowerpoint.save("temp.pptx");
		
	}

}
