package adshotrunner;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.imageio.ImageIO;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.utilities.ASRDatabase;
import adshotrunner.utilities.URLTool;

/**
 * Class to contain URL, image, dimensions, and priority of a creative.
 */
public class Creative implements Comparable<Creative> {

	final public static String CREATED = "CREATED"; 
	final public static String QUEUED = "QUEUED"; 
	final public static String PROCESSING = "PROCESSING"; 
	final public static String FINISHED = "FINISHED"; 
	final public static String ERROR = "ERROR"; 

	/**
	 * Creates instance of Creative object. Retrieves the image at the passed URL and
	 * uses it to set the dimensions.
	 * 
	 * If no protocol is included with the link, "http" is added.
	 * 
	 * On failure, an AdShotRunnerException is thrown.
	 * 
	 * @param tagURL		URL of creative image
	 * @param tagPriority	Relative priority of creative in relation to creatives of same dimensions
	 * @return				
	 */
	public static Creative create(String tagURL, int tagPriority) {
		
		//Set the protocol to http. Many ads won't appear on https.
		tagURL = URLTool.setProtocol("http", tagURL);
		
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
		
		return new Creative(tagURL, tagImage, tagWidth, tagHeight, tagPriority);
	}

	/**
	 * Creates instance of Creative object. Retrieves the image at the passed URL and
	 * uses it to set the dimensions. Tag priority is set to 0.
	 * 
	 * On failure, an AdShotRunnerException is thrown.
	 * 
	 * @param tagURL		URL of tag image
	 * @return				
	 */
	public static Creative create(String tagURL) {
		
		return Creative.create(tagURL, 0);
	}
	
	/**
	 * Creates a copy of the provided Creative.
	 * 
	 * A new unique ID for the instance is created.
	 * 
	 * @param originalCreative	Creative to copy
	 * @return
	 */
	public static Creative create(Creative originalCreative) {
		return new Creative(originalCreative._imageURL, originalCreative._image, 
							originalCreative._width, originalCreative._height, 
							originalCreative._priority);
	}
	
	public static Creative fromDatabaseID(int creativeID) {
		
		//Check to see if a Creative with that ID exists in the database
		try {
			ResultSet creativeResults = ASRDatabase.executeQuery("SELECT * FROM creatives WHERE CRV_id = " + creativeID);
	
			//If a match was found, instantiate the new Creative with it 
			if (creativeResults.next()) {
				
			}	
			
			//Otherwise simply return null
			else {return null;}
		}
		
		//If we couldn't querry the database correctly or another error occurred, print it out and return null
		catch (Exception e) {
			System.out.println(e);
			return null;
		}
		
		return null;
	}

	/**
	 * ID of the creative
	 */
	private int _id;

	/**
	 * UUID of the creative
	 */
	private String _uuid;
	
	/**
	 * URL of the creative image (includes 'http' protocol)
	 */
	private String _imageURL;
	
	/**
	 * Image of the creative 
	 */
	private BufferedImage _image;
	
	/**
	 * Width of the image in pixels
	 */
	private int _width;
	
	/**
	 * Height of the image in pixels
	 */
	private int _height;
	
	/**
	 * Relative priority of the creative in relation to creative of the same dimensions
	 */
	private int _priority;
	
	/**
	 * The tag script used to generate the Creative image. (Can be null)
	 */
	private String _tagScript;

	/**
	 * The tag page used to generate the Creative image. (Can be null)
	 * The tag page is the automatically generated HTML page with the tag script inside of it.
	 */
	private String _tagPageURL;
		
	/**
	 * The current processing status of the Creative.
	 * Possible values set by public static status members: CREATED, QUEUED, PROCESSING, FINISHED, ERROR
	 */
	private String _status;

	/**
	 * Error message if an error occurred while processing the Creative
	 */
	private String _errorMessage;
	
	/**
	 * The UNIX timestamp the Creative was inserted into the database
	 */
	private long _createdTimestamp;
	
	/**
	 * The UNIX timestamp the Creative status was set to QUEUED
	 */
	private long _queuedTimestamp;
	
	/**
	 * The UNIX timestamp the Creative status was set to PROCESSING
	 */
	private long _processingTimestamp;
	
	/**
	 * The UNIX timestamp the Creative status was set to FINISHED
	 */
	private long _finishedTimestamp;

	/**
	 * The UNIX timestamp the Creative status was set to ERROR when an error occurred
	 */
	private long _errorTimestamp;

	
	
	/**
	 * Sets private variables. Parameter verification should be done in static factory.
	 * 
	 * @param creativeImageURL			URL of creative image
	 * @param creativeImage				Image of creative image
	 * @param creativeWidth				Width of image in pixels
	 * @param creativeHeight			Height of image in pixels
	 * @param creativePriority			Relative priority of creative in relation to creatives of same dimensions
	 */
	private Creative(String creativeImageURL, BufferedImage creativeImage, 
					 int creativeWidth, int creativeHeight, int creativePriority) {
		
		//Set the private member variables
		_imageURL = creativeImageURL;
		_image = creativeImage;
		_width = creativeWidth;
		_height = creativeHeight;
		_priority = creativePriority;
		
		//Create a unique ID for this tag
		_uuid = UUID.randomUUID().toString();
	}
	
	private Creative(ResultSet creativeDetails) throws SQLException {
		
		_id = creativeDetails.getInt("CRV_id");
		_uuid = creativeDetails.getString("CRV_uuid");
		_imageURL = creativeDetails.getString("CRV_imageURL");
		_width = creativeDetails.getInt("CRV_width");
		_height = creativeDetails.getInt("CRV_height");
		_priority = creativeDetails.getInt("CRV_priority");
		_tagScript = creativeDetails.getString("CRV_tagScript");
		_tagPageURL = creativeDetails.getString("CRV_tagPageURL");
		_status = creativeDetails.getString("CRV_imageURL");
		_errorMessage = creativeDetails.getString("CRV_imageURL");
		_createdTimestamp = creativeDetails.getInt("CRV_createdTimestamp");
		_queuedTimestamp = creativeDetails.getInt("CRV_queuedTimestamp");
		_processingTimestamp = creativeDetails.getInt("CRV_processingTimestamp");
		_finishedTimestamp = creativeDetails.getInt("CRV_finishedTimestamp");
		_errorTimestamp = creativeDetails.getInt("CRV_errorTimestamp");
	}
	
	/**
	 * @return	ID of the creative
	 */
	public int id() {return _id;}
	
	/**
	 * @return	UUID of the creative
	 */
	public String uuid() {return _uuid;}
	
	/**
	 * @return	URL of the creative image
	 */
	public String url() {return _imageURL;}
	
	/**
	 * @return	Image of creative
	 */
	public BufferedImage image() {return _image;}
	
	/**
	 * @return	Width of creative image in pixels
	 */
	public int width() {return _width;}
	
	/**
	 * @return	Height of creative image in pixels
	 */
	public int height() {return _height;}
	
	/**
	 * @return	Priority of creative image in relation to creatives of same dimensions
	 */
	public int priority() {return _priority;}
	
	/**
	 * Allows sorting Creatives based on priority
	 */
	@Override
	public int compareTo(Creative otherCreative) {
	    return Integer.compare(this._priority, otherCreative._priority);
	}
}
