package adshotrunner.dispatcher;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import adshotrunner.campaigns.Creative;
import adshotrunner.shotter.TagShotter;

/**
 * The TagImager captures tag images for Creative using the TagShotter.
 */
public class TagImager implements Runnable {
	
	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------		
	//The number of times to retry capturing a Creative if no image was returned or the image is one solid color
	final public static int CAPTURERETRYCOUNT = 2;

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	 * Creatives to capture images for
	 */
	final private Set<Creative> _creativesToCapture;

	/**
	 * Thread of TagImager instance used to carry out the image capture
	 */
	final private Thread _imagerThread;
	
	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Creates a new thread and captures the creative tag page images
	 * @param creatives		Creatives to capture images for
	 */
	public TagImager(Set<Creative> creatives) {
		
		//Store the creatives
		_creativesToCapture = creatives;
		
		//Start the thread to capture the Creative tag pages
		_imagerThread = new Thread(this);
		_imagerThread.start();

	}
	
	//---------------------------------------------------------------------------------------
	//-------------------------------- Thread Executable ------------------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Captures each Creative using the TagShotter. Any Creatives without an image or with
	 * an image that's one solid color (tag didn't load ad), are retried up to 
	 * CAPTURERETRYCOUNT.
	 */
	@Override
	public void run() {
		
		//Capture the tag images
		TagShotter.captureTagImages(_creativesToCapture);
		
		//Retry any Creative without images (either null or one solid color)
		int retryAttempts = 0;
		Set<Creative> creativeWithoutImages = getCreativeWithoutImages(_creativesToCapture);
		while ((creativeWithoutImages.size() > 0) && (retryAttempts < CAPTURERETRYCOUNT)) {
			
			//Attempt to get capture the tag page again
			TagShotter.captureTagImages(creativeWithoutImages);
			
			//Update the Creative set without images and increment the attempt count
			creativeWithoutImages = getCreativeWithoutImages(_creativesToCapture);
			retryAttempts++;
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************		
	/**
	 * Returns the set of Creative from the passed Creatives that either have "null" for their
	 * image or have an image of only one solid color (the tag did not load the image)
	 * 
	 * @param creatives		List of Creatives to examine for missing images
	 * @return				Set of Creatives from passed Creatives without images
	 */
	private static Set<Creative> getCreativeWithoutImages(Set<Creative> creatives) {
		
		//Loop through Creative and store those without images or with images of one color
		Set<Creative> creativeWithoutImages = new HashSet<Creative>();
		for (Creative currentCreative : creatives) {
			if ((currentCreative.image() == null) || (numberOfColorsInImage(currentCreative.image()) == 1)) {
				creativeWithoutImages.add(currentCreative);
			}
		}
		
		//Return the set of Creatives without images
		return creativeWithoutImages;
	}

	/**
	 * Returns the number of unique colors in the passed image
	 * 
	 * @param image		Image to examine for unique colors
	 * @return			The number of unique colors in the passed image
	 */
	private static int numberOfColorsInImage(BufferedImage image) {
		
		//Loop through each pixel and add each pixel color to the set (thus storing only unique colors)
		Set<Integer> colors = new HashSet<Integer>();
	    int imageWidth = image.getWidth();
	    int imageHeight = image.getHeight();
	    for(int yPosition = 0; yPosition < imageHeight; yPosition++) {
	        for(int xPosition = 0; xPosition < imageWidth; xPosition++) {
	            int pixel = image.getRGB(xPosition, yPosition);     
	            colors.add(pixel);
	        }
	    }
	    
	    //Return the amount of colors in the image
	    return colors.size();
	}

	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	 * @return	Thread of the TagImager
	 */
	public Thread thread() {return _imagerThread;}
	
}
