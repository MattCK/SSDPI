package adshotrunner;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.utilities.URLTool;

/**
 * Class that contains the URL, list of tag images, screenshot image, and possible error for
 * an AdShot.
 * 
 * Optional setting to flag as a tag. Tags receive less load time and no javascript is injected.
 * 
 * @author juicio
 *
 */
public class AdShot {
	
	/**
	 * Creates an instance of an AdShot using the passed URLs.
	 * 
	 * If no protocol precedes the URL, "http" is added.
	 * 
	 * If the URL is null or empty, an AdShotRunnerException is thrown.
	 * 
	 * @param pageURL			URL where the screenshot should be taken
	 * @return
	 */
	public static AdShot create(String pageURL) {
		return AdShot.create(pageURL, new ArrayList<TagImage>(), false);
	}

	/**
	 * Creates an instance of an AdShot using the passed URL and list of tag images.
	 * 
	 * If no protocol precedes the URL, "http" is added.
	 * 
	 * If the URL is null or empty, an AdShotRunnerException is thrown.
	 * 
	 * @param pageURL			URL where the screenshot should be taken
	 * @param pageTagImages		List of tag images to be inserted into the page
	 * @return
	 */
	public static AdShot create(String pageURL, List<TagImage> pageTagImages) {
		return AdShot.create(pageURL, pageTagImages, false);
	}
	
	/**
	 * Creates an instance of an AdShot using the passed URL and flags it as a tag.
	 * Tags receive less page load time and no javascript is injected.
	 * 
	 * If no protocol precedes the URL, "http" is added.
	 * 
	 * If the URL is null or empty, an AdShotRunnerException is thrown.
	 * 
	 * TagImages cannot be added to this instance.
	 * 
	 * @param pageURL			URL where the screenshot should be taken
	 * @return
	 */
	public static AdShot createForTag(String pageURL) {
		return AdShot.create(pageURL, new ArrayList<TagImage>(), true);
	}
	
	/**
	 * Creates an instance of an AdShot using the passed URL, list of tag images, and
	 * whether or not the instance should be treated as a tag.
	 * 
	 * If no protocol precedes the URL, "http" is added.
	 * 
	 * If the URL is null or empty, an AdShotRunnerException is thrown.
	 * 
	 * @param pageURL			URL where the screenshot should be taken
	 * @param pageTagImages		List of tag images to be inserted into the page
	 * @param isTag				If set to true, the AdShot is flagged as a tag. Tags are given less load time and no javascript is injected.
	 * @return
	 */
	private static AdShot create(String pageURL, List<TagImage> pageTagImages, boolean isTag) {
		
		//Verify the URL is not null and not an empty string
		if (pageURL == null || pageURL.isEmpty()) {throw new AdShotRunnerException("URL empty or null");}
		
		//If no http protocol is included with the URL, add it
		if (!pageURL.substring(0, 4).equals("http")) {
			pageURL = URLTool.setProtocol("http", pageURL);
		}
		
		//If a null TagImage list was passed, instantiate one
		if (pageTagImages == null) {pageTagImages = new ArrayList<TagImage>();}
		
		//Create and return the AdShot
		return new AdShot(pageURL, pageTagImages, isTag);
	}
	
	/**
	 * URL for the screenshot (includes protocol)
	 */
	private final String _url;
	
	/**
	 * List of TagImages to be inserted into the page
	 */
	private final List<TagImage> _tagImages;
	
	/**
	 * Flags whether or not the AdShot should be treated as a tag. Tags receive less page load time and no
	 * javascript is injected. If TRUE, TagImages cannot be added to the instance.
	 */
	private final boolean _treatAsTag;
	
	/**
	 * Image of the final screenshot.
	 */
	private BufferedImage _image;
	
	/**
	 * Possible error that occurred during the screenshot attempt. Default: null
	 */
	private Exception _error;
	
	/**
	 * Sets private variables of instance. Parameter verification should be done in static factory.
	 * @param pageURL			URL for the screenshot
	 * @param pageTagImages		List of TagImages to be inserted into the page
	 * @param isTag				Flags whether the AdShot should be treated like a tag
	 */
	private AdShot(String pageURL, List<TagImage> pageTagImages, boolean isTag) {
		_url = pageURL;
		_tagImages = pageTagImages;
		_treatAsTag = isTag;
	}
	
	/**
	 * Adds a new tag image to be inserted into the page.
	 * 
	 * The TagImage is NOT added if the instance has been flagged as a tag.
	 * 
	 * @param newTagImage	New tag image to be inserted into the pate
	 */
	public void addTagImage(TagImage newTagImage) {
		if (!_treatAsTag) {
			_tagImages.add(newTagImage);
		}
	}
	
	/**
	 * @return	URL for the screenshot
	 */
	public String url() {return _url;}
	
	/**
	 * @return	List of tag images to insert into the page (unmodifiable)
	 */
	public List<TagImage> tagImages() {return Collections.unmodifiableList(_tagImages);}
	
	/**
	 * @return	Whether or not to treat the instance as a tag. Tags receive less page load time and no javascript is injected.
	 */
	public boolean treatAsTag() {return _treatAsTag;}
	
	/**
	 * @return	Image of the final screenshot. Default: null
	 */
	public BufferedImage image() {return _image;}
	
	/**
	 * @return	Possible error that occurred during screenshot attempt. Default: null
	 */
	public Exception error() {return _error;}
	
	/**
	 * Sets the screenshot image
	 * @param newImage	Screenshot image
	 */
	public void setImage(BufferedImage newImage) {_image = newImage;}
	
	/**
	 * Sets error that occurred during screenshot attempt.
	 * @param newError	Error that occurred during screenshot attempt
	 */
	public void setError(Exception newError) {_error = newError;}
	
}
