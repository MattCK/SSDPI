package adshotrunner.tests;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TrailingWhiteTest {

	public static void main(String[] args) throws IOException {
		
		BufferedImage screenshot = ImageIO.read(new File("cr.png"));
		int whiteSpaceHeight = getTraillingWhitespaceHeight(screenshot);
		System.out.println("Whitespace: " + whiteSpaceHeight);
		
	}

	public static int getTraillingWhitespaceHeight(BufferedImage image) {
		
		//Loop through each pixel row, starting from the lowest, until 
		//a non-white pixel is found
	    int imageWidth = image.getWidth();
	    int imageHeight = image.getHeight();
	    int yPosition = imageHeight - 1;
	    int whiteSpaceLength = 0;
	    boolean nonWhitePixelFound = false;
	    while ((yPosition >= 0) && (!nonWhitePixelFound)) {
	    	
	    	//Check the current row for any non-white pixels
	        for(int xPosition = 0; xPosition < imageWidth; xPosition++) {
	            int currentPixelColor = image.getRGB(xPosition, yPosition);  
	            if (currentPixelColor != Color.WHITE.getRGB()) {
	            	nonWhitePixelFound = true;
	            }
	        }
	        
	        //If no non-white pixels were found, increment the white space length
	        if (!nonWhitePixelFound) {++whiteSpaceLength;}
	        --yPosition;
	    }

	    //Return the final trailing white space height
		return whiteSpaceLength;
	}
	
	
}
