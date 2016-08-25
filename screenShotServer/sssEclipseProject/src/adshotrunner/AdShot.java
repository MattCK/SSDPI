package adshotrunner;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
		return AdShot.create(pageURL, new HashSet<TagImage>());
	}

	
	/**
	 * Creates an instance of an AdShot using the passed URL and set of tag images.
	 * 
	 * If no protocol precedes the URL, "http" is added.
	 * 
	 * If the URL is null or empty, an AdShotRunnerException is thrown.
	 * 
	 * @param pageURL			URL where the screenshot should be taken
	 * @param pageTagImages		Set of tag images to be inserted into the page
	 * @return
	 */
	public static AdShot create(String pageURL, Set<TagImage> pageTagImages) {
		
		//Verify the URL is not null and not an empty string
		if (pageURL == null || pageURL.isEmpty()) {throw new AdShotRunnerException("URL empty or null");}
		
		//If no http protocol is included with the URL, add it
		if (!pageURL.substring(0, 4).equals("http")) {
			pageURL = URLTool.setProtocol("http", pageURL);
		}
		
		//If a null TagImage list was passed, instantiate one
		if (pageTagImages == null) {pageTagImages = new HashSet<TagImage>();}
		
		//Create and return the AdShot
		return new AdShot(pageURL, pageTagImages);
	}
	
	/**
	 * URL for the screenshot (includes protocol)
	 */
	private final String _url;
	
	/**
	 * Alternate page URLs. These are generally used if no tag images were injected into the primary URL page (includes protocol)
	 */
	private final Set<String> _alternateURLs;
	
	/**
	 * Set of TagImages to be inserted into the page
	 */
	private final Set<TagImage> _tagImages;
	
	/**
	 * Flags whether or not the AdShot should be taken in a mobile browser
	 */
	private Boolean _mobile;
	
	/**
	 * Set of TagImages that were injected into the page
	 */
	private final Set<TagImage> _injectedTagImages;
	
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
	 * 
	 * @param pageURL			URL for the screenshot
	 * @param pageTagImages		Set of TagImages to be inserted into the page
	 */
	private AdShot(String pageURL, Set<TagImage> pageTagImages) {
		_url = pageURL;
		_tagImages = pageTagImages;
		_alternateURLs = new HashSet<String>();
		_injectedTagImages = new HashSet<TagImage>();
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
	 * Sets the tag image with the passed ID as injected into the page.
	 * 
	 * @param tagImageID	ID of tag image that was injected into the page
	 */
	public void markTagImageAsInjected(String tagImageID) {
		
		ArrayList<String> idList = new ArrayList<String>();
		idList.add(tagImageID);
		markTagImageAsInjected(tagImageID);
	}
	
	/**
	 * Sets the tag images with the set of passed IDs as injected into the page.
	 * 
	 * @param tagImageIDs	IDs of tag images that were injected into the page
	 */
	public void markTagImageAsInjected(Collection<String> tagImageIDs) {	
		
        Iterator<String> tagImageIterator = tagImageIDs.iterator();

		while (tagImageIterator.hasNext()) {
			String tagImageID = tagImageIterator.next();
			_tagImages.forEach(tagImage -> {if (tagImage.id().equals(tagImageID)) {
												_injectedTagImages.add(tagImage);}});
		}	
		
	}
	
	/**
	 * Adds an alternate page URL. These are generally used if no tag images were
	 * injected into the primary URL page.
	 * 
	 * @param alternatePageURL		Alternate URL to include
	 */
	public void addAlternatePageURL(String alternatePageURL) {
		_alternateURLs.add(alternatePageURL);
	}
	
	/**
	 * Adds alternate page URLs. These are generally used if no tag images were
	 * injected into the primary URL page.
	 * 
	 * @param alternatePageURLs		Collection of alternate URLs to include
	 */
	public void addAlternatePageURL(Collection<String> alternatePageURLs) {
		_alternateURLs.addAll(alternatePageURLs);
	}
	
	/**
	 * @return	URL for the screenshot
	 */
	public String url() {return _url;}
	
	/**
	 * @return	Set of alternate page URLs (unmodifiable)
	 */
	public Set<String> alternateURLs() {return Collections.unmodifiableSet(_alternateURLs);}
	
	/**
	 * @return	Set of tag images to insert into the page (unmodifiable)
	 */
	public Set<TagImage> tagImages() {return Collections.unmodifiableSet(_tagImages);}
	
	/**
	 * @return	TRUE if the AdShot should use a mobile browser and FALSE otherwise
	 */
	public Boolean mobile() {return _mobile;}
	
	/**
	 * @return	Set of tag images that were injected into the page (unmodifiable)
	 */
	public Set<TagImage> injectedTagImages() {return Collections.unmodifiableSet(_injectedTagImages);}
	
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
	public void useMobile(Boolean mobileFlag) {_mobile = mobileFlag;}
	
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
