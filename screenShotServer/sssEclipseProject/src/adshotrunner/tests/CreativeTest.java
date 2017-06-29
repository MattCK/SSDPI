package adshotrunner.tests;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import adshotrunner.campaigns.Creative;

public class CreativeTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Getting Creative");
		Creative tempCreative = Creative.getCreative(1);
		System.out.println("Got creative");
		System.out.println(tempCreative.id());
		System.out.println(tempCreative.uuid());
		System.out.println(tempCreative.imageFilename());
		System.out.println(tempCreative.imageURL());
		System.out.println(tempCreative.image());
		System.out.println(tempCreative.width());
		System.out.println(tempCreative.height());
		System.out.println(tempCreative.priority());
		System.out.println(tempCreative.tagScript());
		System.out.println(tempCreative.tagPageURL());
		System.out.println(tempCreative.status());
		System.out.println(tempCreative.errorMessage());
		System.out.println(tempCreative.createdTimestamp());
		System.out.println(tempCreative.queuedTimestamp());
		System.out.println(tempCreative.processingTimestamp());
		System.out.println(tempCreative.finishedTimestamp());
		System.out.println(tempCreative.errorTimestamp());
		System.out.println();
		
//		System.out.println("Original");
//		System.out.println(tempCreative.status());
//		System.out.println(tempCreative.createdTimestamp());
//		System.out.println(tempCreative.queuedTimestamp());
//		System.out.println(tempCreative.processingTimestamp());
//		System.out.println(tempCreative.finishedTimestamp());
//		System.out.println(tempCreative.errorTimestamp());
//		System.out.println();
//
//		System.out.println("QUEUED");
//		tempCreative.setStatus(Creative.QUEUED);
//		System.out.println(tempCreative.status());
//		System.out.println(tempCreative.createdTimestamp());
//		System.out.println(tempCreative.queuedTimestamp());
//		System.out.println(tempCreative.processingTimestamp());
//		System.out.println(tempCreative.finishedTimestamp());
//		System.out.println(tempCreative.errorTimestamp());
//
//		System.out.println("PROCESSING");
//		tempCreative.setStatus(Creative.PROCESSING);
//		System.out.println(tempCreative.status());
//		System.out.println(tempCreative.createdTimestamp());
//		System.out.println(tempCreative.queuedTimestamp());
//		System.out.println(tempCreative.processingTimestamp());
//		System.out.println(tempCreative.finishedTimestamp());
//		System.out.println(tempCreative.errorTimestamp());
//		System.out.println();
//
//		System.out.println("FINISHED");
//		tempCreative.setStatus(Creative.FINISHED);
//		System.out.println(tempCreative.status());
//		System.out.println(tempCreative.createdTimestamp());
//		System.out.println(tempCreative.queuedTimestamp());
//		System.out.println(tempCreative.processingTimestamp());
//		System.out.println(tempCreative.finishedTimestamp());
//		System.out.println(tempCreative.errorTimestamp());
//		System.out.println();
//
//		System.out.println("ERROR");
//		tempCreative.setError("new error message");
//		System.out.println(tempCreative.status());
//		System.out.println(tempCreative.createdTimestamp());
//		System.out.println(tempCreative.queuedTimestamp());
//		System.out.println(tempCreative.processingTimestamp());
//		System.out.println(tempCreative.finishedTimestamp());
//		System.out.println(tempCreative.errorTimestamp());
//		System.out.println(tempCreative.errorMessage());
//		System.out.println();
		
//		System.out.println("Setting image url");
//		tempCreative.setImageURL("https://s3.amazonaws.com/asr-development/creativeimages/8edc627f-32d9-4dd0-843b-6491c748ee50.png");
//		System.out.println(tempCreative.imageURL());
//		System.out.println(tempCreative.image());
//		System.out.println(tempCreative.width());
//		System.out.println(tempCreative.height());

		System.out.println("Setting image");
		BufferedImage creativeImage = null; 
		try {
			URL imageURL = new URL("https://s3.amazonaws.com/asr-development/creativeimages/0014d0d8-e44f-472b-a2c2-29572763ffac.png");
			creativeImage = ImageIO.read(imageURL); 
		}
        catch (IOException e) {
        	e.printStackTrace();
        }
		
		
		
		tempCreative.setImage(creativeImage);
		System.out.println(tempCreative.imageFilename());
		System.out.println(tempCreative.imageURL());
		System.out.println(tempCreative.image());
		System.out.println(tempCreative.width());
		System.out.println(tempCreative.height());

	}

}
