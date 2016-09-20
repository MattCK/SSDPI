package adshotrunner;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.imageio.ImageIO;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.utilities.URLTool;

/**
 * Class to contain URL, image, dimensions, and priority of an ad tag image.
 */
public class TagImage implements Comparable<TagImage> {

	/**
	 * Creates instance of TagImage object. Retrieves the image at the passed URL and
	 * uses it to set the dimensions.
	 * 
	 * If no protocol is included with the link, "http" is added.
	 * 
	 * On failure, an AdShotRunnerException is thrown.
	 * 
	 * @param tagURL		URL of tag image
	 * @param tagPriority	Relative priority of tag in relation to tags of same dimensions
	 * @return				
	 */
	public static TagImage create(String tagURL, int tagPriority) {
		
		//Set the protocol to http. Many ads won't appear on https.
		tagURL = URLTool.setProtocol("http", tagURL);
		System.out.println("Tag URL after change: " + tagURL);
		
		//Retrieve the image
		BufferedImage tagImage = null;
		try {
			URL imageURL = new URL(tagURL);
			tagImage = ImageIO.read(imageURL); 
		}
        catch (IOException e) {
        	throw new AdShotRunnerException("Could not load tag image from URL: " + tagURL, e);
        }
		
		//Get the image's width and height
		int tagWidth = tagImage.getWidth();
		int tagHeight = tagImage.getHeight();
		
		return new TagImage(tagURL, tagImage, tagWidth, tagHeight, tagPriority);
	}

	/**
	 * Creates instance of TagImage object. Retrieves the image at the passed URL and
	 * uses it to set the dimensions. Tag priority is set to 0.
	 * 
	 * On failure, an AdShotRunnerException is thrown.
	 * 
	 * @param tagURL		URL of tag image
	 * @return				
	 */
	public static TagImage create(String tagURL) {
		
		return TagImage.create(tagURL, 0);
	}
	
	/**
	 * Creates a copy of the provided TagImage.
	 * 
	 * A new unique ID for the instance is created.
	 * 
	 * @param originalTagImage	TagImage to copy
	 * @return
	 */
	public static TagImage create(TagImage originalTagImage) {
		return new TagImage(originalTagImage._url, originalTagImage._image, 
							originalTagImage._width, originalTagImage._height, 
							originalTagImage._priority);
	}

	/**
	 * ID of the tag instance (automatically generated)
	 */
	private final String _id;
	
	/**
	 * URL of the tag (includes protocol)
	 */
	private final String _url;
	
	/**
	 * Image of the tag from the passed URL
	 */
	private final BufferedImage _image;
	
	/**
	 * Width of the image in pixels
	 */
	private final int _width;
	
	/**
	 * Height of the image in pixels
	 */
	private final int _height;
	
	/**
	 * Relative priority of the tag in relation to tags of the same dimensions
	 */
	private final int _priority;
	
	/**
	 * Sets private variables. Parameter verification should be done in static factory.
	 * 
	 * @param tagURL		URL of tag image
	 * @param tagImage		Image of tag
	 * @param tagWidth		Width of image in pixels
	 * @param tagHeight		Height of image in pixels
	 * @param tagPriority	Relative priority of tag in relation to tags of same dimensions
	 */
	private TagImage(String tagURL, BufferedImage tagImage, int tagWidth, int tagHeight, int tagPriority) {
		
		//Set the private member variables
		_url = tagURL;
		_image = tagImage;
		_width = tagWidth;
		_height = tagHeight;
		_priority = tagPriority;
		
		//Create a unique ID for this tag
		_id = UUID.randomUUID().toString();
	}
	
	/**
	 * @return	ID of tag image
	 */
	public String id() {return _id;}
	
	/**
	 * @return	URL of tag image
	 */
	public String url() {return _url;}
	
	/**
	 * @return	Image of tag
	 */
	public BufferedImage image() {return _image;}
	
	/**
	 * @return	Width of tag image in pixels
	 */
	public int width() {return _width;}
	
	/**
	 * @return	Height of tag image in pixels
	 */
	public int height() {return _height;}
	
	/**
	 * @return	Priority of tag image in relation to tags of same dimensions
	 */
	public int priority() {return _priority;}

	@Override
	public int compareTo(TagImage otherTagImage) {
	    return Integer.compare(this._priority, otherTagImage._priority);
	}
}
