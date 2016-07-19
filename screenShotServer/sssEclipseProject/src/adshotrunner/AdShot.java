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
		return AdShot.create(pageURL, new ArrayList<TagImage>());
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
	 * @return
	 */
	public static AdShot create(String pageURL, List<TagImage> pageTagImages) {
		
		//Verify the URL is not null and not an empty string
		if (pageURL == null || pageURL.isEmpty()) {throw new AdShotRunnerException("URL empty or null");}
		
		//If no http protocol is included with the URL, add it
		if (!pageURL.substring(0, 4).equals("http")) {
			pageURL = URLTool.setProtocol("http", pageURL);
		}
		
		//If a null TagImage list was passed, instantiate one
		if (pageTagImages == null) {pageTagImages = new ArrayList<TagImage>();}
		
		//Create and return the AdShot
		return new AdShot(pageURL, pageTagImages);
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
	 * Image of the final screenshot.
	 */
	private BufferedImage _image;
	
	/**
	 * The final URL of the screenshot after page redirects. Default: null
	 */
	private String _finalURL;
	
	/**
	 * Possible error that occurred during the screenshot attempt. Default: null
	 */
	private Exception _error;
	
	/**
	 * Sets private variables of instance. Parameter verification should be done in static factory.
	 * @param pageURL			URL for the screenshot
	 * @param pageTagImages		List of TagImages to be inserted into the page
	 */
	private AdShot(String pageURL, List<TagImage> pageTagImages) {
		_url = pageURL;
		_tagImages = pageTagImages;
	}
	
	/**
	 * Adds a new tag image to be inserted into the page.
	 * 
	 * @param newTagImage	New tag image to be inserted into the page
	 */
	public void addTagImage(TagImage newTagImage) {
		_tagImages.add(newTagImage);
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
	 * @return	Image of the final screenshot. Default: null
	 */
	public BufferedImage image() {return _image;}
	
	/**
	 * @return	Final URL after redirects of the final screenshot. Default: null
	 */
	public String finalURL() {return _finalURL;}
	
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
	 * Sets the final URL of the screenshot after redirects
	 * @param String	Final URL after redirects
	 */
	public void setFinalURL(String newURL) {_finalURL = newURL;}
	
	/**
	 * Sets error that occurred during screenshot attempt.
	 * @param newError	Error that occurred during screenshot attempt
	 */
	public void setError(Exception newError) {_error = newError;}
	
}
