package adshotrunner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.imageio.ImageIO;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.ASRDatabase;
import adshotrunner.utilities.FileStorageClient;

/**
 * Class to retrieve and manipulate Creatives.
 *
 * This class mirrors the PHP and Javascript Creative classes.
 * ANY CHANGES TO THIS CLASS SHOULD BE MADE IN THOSE CLASSES AS WELL.
 */
public class Creative implements Comparable<Creative> {

	//Status constants for Creative processing
	final public static String CREATED = "CREATED"; 
	final public static String QUEUED = "QUEUED"; 
	final public static String PROCESSING = "PROCESSING"; 
	final public static String FINISHED = "FINISHED"; 
	final public static String ERROR = "ERROR"; 

	//URL path for creative images and tag pages
	final public static String CREATIVEURLPATH = "http://s3.amazonaws.com/" + ASRProperties.containerForCreativeImages() + "/";
	final public static String TAGPAGEURLPATH = "http://s3.amazonaws.com/" + ASRProperties.containerForTagPages() + "/";

	/**
	 * Retrieves and returns a Creative from the database with the provided ID.
	 * 
	 * @param creativeID	ID of the Creative
	 * @return				Creative on success and NULL on failure
	 */	
	public static Creative get(int creativeID) {
		
		//Check to see if a Creative with that ID exists in the database
		//Redundant check along with constructor
		try {
			ResultSet creativeResults = ASRDatabase.executeQuery("SELECT * FROM creatives WHERE CRV_id = " + creativeID);
	
			//If a match was found, instantiate the new Creative with it 
			if (creativeResults.next()) {
				return new Creative(creativeID);
			}	
			
			//Otherwise simply return null
			else {
				System.out.println("Unable to get Creative with ID: " + creativeID);
				return null;
			}
		}
		
		//If we couldn't query the database correctly or another error occurred, print it out and return null
		catch (Exception e) {
			e.printStackTrace();;
			return null;
		}		
	}

	/**
	 * ID of the creative
	 */
	final private int _id;

	/**
	 * UUID of the creative
	 */
	final private String _uuid;
	
	/**
	 * Filename of the creative image
	 */
	private String _imageFilename;
	
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
	final private int _priority;
	
	/**
	 * The tag script used to generate the Creative image. (Can be empty string)
	 */
	final private String _tagScript;

	/**
	 * The filename of the tag page used to generate the Creative image. (Can be empty string)
	 * The the tag page is the automatically generated HTML page with the tag script inside of it.
	 */
	final private String _tagPageFilename;
		
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
	 * The timestamp the Creative was inserted into the database
	 */
	private Timestamp _createdTimestamp;
	
	/**
	 * The timestamp the Creative status was set to QUEUED
	 */
	private Timestamp _queuedTimestamp;
	
	/**
	 * The UNIX timestamp the Creative status was set to PROCESSING
	 */
	private Timestamp _processingTimestamp;
	
	/**
	 * The UNIX timestamp the Creative status was set to FINISHED
	 */
	private Timestamp _finishedTimestamp;

	/**
	 * The UNIX timestamp the Creative status was set to ERROR when an error occurred
	 */
	private Timestamp _errorTimestamp;

	
	private Creative(int creativeID) throws SQLException {
		
		//Get the Creative information from the database
		ResultSet creativeResult = ASRDatabase.executeQuery("SELECT * FROM creatives WHERE CRV_id = " + creativeID);		
		
		//If a Creative was found with the passed ID, set the instance's members to its details
		if (creativeResult.next()) {
			
			_id = creativeResult.getInt("CRV_id");
			_uuid = creativeResult.getString("CRV_uuid");
			_imageFilename = creativeResult.getString("CRV_imageFilename");
			_width = creativeResult.getInt("CRV_width");
			_height = creativeResult.getInt("CRV_height");
			_priority = creativeResult.getInt("CRV_priority");
			_tagScript = creativeResult.getString("CRV_tagScript");
			_tagPageFilename = creativeResult.getString("CRV_tagPageFilename");
			_status = creativeResult.getString("CRV_status");
			_errorMessage = creativeResult.getString("CRV_errorMessage");
			_createdTimestamp = creativeResult.getTimestamp("CRV_createdTimestamp");
			_queuedTimestamp = creativeResult.getTimestamp("CRV_queuedTimestamp");
			_processingTimestamp = creativeResult.getTimestamp("CRV_processingTimestamp");
			_finishedTimestamp = creativeResult.getTimestamp("CRV_finishedTimestamp");
			_errorTimestamp = creativeResult.getTimestamp("CRV_errorTimestamp");
		}	
		
		//Otherwise throw an error
		else {
			throw new AdShotRunnerException("Could not find Creative with ID: " + creativeID);
		}
		
		//Get the image from the filename if one exists
		if (!_imageFilename.isEmpty()) {
			try {
				URL imageURL = new URL(CREATIVEURLPATH + _imageFilename);
				_image = ImageIO.read(imageURL); 
			}
	        catch (IOException e) {
	        	System.out.println("Could not load creative image from filename: " + _imageFilename);
	        	throw new AdShotRunnerException("Could not load creative image from filename: " + _imageFilename, e);
	        }
		}
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
	 * @return	Filename of the creative image
	 */
	public String imageFilename() {return _imageFilename;}
	
	/**
	 * @return	Full URL including protocol to image
	 */
	public String imageURL() {
		if (_imageFilename.isEmpty()) {return "";}
		return CREATIVEURLPATH + _imageFilename;
	}
	
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
	final public int priority() {return _priority;}
	
	/**
	 * @return	The tag script used to generate the Creative image. (Can be empty string)
	 */
	public String tagScript() {return _tagScript;}
	
	/**
	 * @return	The filename of the tag page used to generate the Creative image. (Can be empty string)
	 */
	public String tagPageFilename() {return _tagPageFilename;}
	
	/**
	 * @return	Full URL including protocol to tag page
	 */
	public String tagPageURL() {
		if (_tagPageFilename.isEmpty()) {return "";}
		return TAGPAGEURLPATH + _tagPageFilename;
	}
	
	/**
	 * @return	The current processing status of the Creative. (Options static members: QUEUED, PROCESSING, FINISHED, ERROR)
	 */
	public String status() {return _status;}
	
	/**
	 * @return	Error message if an error occurred while processing the Creative
	 */
	public String errorMessage() {return _errorMessage;}

	/**
	 * @return	The timestamp the Creative was inserted into the database
	 */
	public Timestamp createdTimestamp() {return _createdTimestamp;}

	/**
	 * @return	The timestamp the Creative status was set to QUEUED
	 */
	public Timestamp queuedTimestamp() {return _queuedTimestamp;}

	/**
	 * @return	The timestamp the Creative status was set to PROCESSING
	 */
	public Timestamp processingTimestamp() {return _processingTimestamp;}

	/**
	 * @return	The timestamp the Creative status was set to FINISHED
	 */
	public Timestamp finishedTimestamp() {return _finishedTimestamp;}

	/**
	 * @return	The timestamp the Creative status was set to ERROR when an error occurred
	 */
	public Timestamp errorTimestamp() {return _errorTimestamp;}

	/**
	 * Set the image URL of the Creative.
	 * 
	 * Retrieves the image at the passed URL. If successful, the image is stored in the
	 * object, the width and height are set based on it, and the Creative is updated
	 * in the database with the new information.
	 * 
	 * @param newImageURL	URL of the creative image
	 */
//	public void setImageURL(String newImageURL) {
//		
//		//If a null or empty string is passed, return an exception
//		if ((newImageURL == null) || (newImageURL.isEmpty())) {
//        	throw new AdShotRunnerException("NULL or Empty string passed to Creative.setImageURL");
//		}
//		
//		//Get the image from the new url
//		BufferedImage creativeImage = null; 
//		try {
//			URL imageURL = new URL(newImageURL);
//			creativeImage = ImageIO.read(imageURL); 
//		}
//        catch (IOException e) {
//        	throw new AdShotRunnerException("Could not load creative image from URL: " + imageURL, e);
//        }
//		
//		//Get the image width and height
//		int imageWidth = creativeImage.getWidth();
//		int imageHeight = creativeImage.getHeight();
//		
//		//Store the image URL, width, and height in the database
//		try {
//			ASRDatabase.executeUpdate("UPDATE creatives " +
//									 "SET CRV_imageURL = '" + newImageURL + "', " + 
//								 	 	 "CRV_height = '" + imageWidth + "', " +
//								 	 	 "CRV_width = '" + imageHeight + "' " +
//								 	  "WHERE CRV_id = " + _id);
//						
//		} catch (Exception e) {
//        	throw new AdShotRunnerException("Could not store Creative image URL in database: " + newImageURL, e);
//		}
//		
//		//Set the instance variables
//		_imageURL = newImageURL;
//		_image = creativeImage;
//		_width = imageWidth;
//		_height = imageHeight;
//	}
	
	/**
	 * Set the image of the Creative. (This function uploads the image to file storage)
	 * 
	 * Uploads the image to storage. If successful, the image is stored in the
	 * object, the width and height are set based on it, and the Creative is updated
	 * in the database with the new information.
	 * 
	 * @param newImageURL	URL of the creative image
	 */
	public void setImage(BufferedImage newImage) {
		
		//If a null image is passd, return an exception
		if (newImage == null) {
        	throw new AdShotRunnerException("NULL passed to Creative.setImage");
		}
		
		//Upload the image as a PNG to storage
		//Convert the image to a PNG and save it to the temporary directory
		String imageFilename = _uuid + ".png";
		try {
			ImageIO.write(newImage, "png", new File(ASRProperties.pathForTemporaryFiles() + imageFilename));
		} catch (IOException e) {
			e.printStackTrace();
        	throw new AdShotRunnerException("Could not convert image to PNG: " + imageFilename, e);			
		}         	
		
		//Save the image PNG file to storage
		try {
			FileStorageClient.saveFile(ASRProperties.containerForCreativeImages(), 
					   				   ASRProperties.pathForTemporaryFiles() + imageFilename, imageFilename);
		} catch (Exception e) {
        	throw new AdShotRunnerException("Could not upload Creative image: " + imageFilename, e);			
		}

		//Delete the local file
		File tagImageFile = new File(ASRProperties.pathForTemporaryFiles() + imageFilename);
		tagImageFile.delete();

		//Get the image width and height
		int imageWidth = newImage.getWidth();
		int imageHeight = newImage.getHeight();
		
		//Store the image filename, width, and height in the database
		try {
			ASRDatabase.executeUpdate("UPDATE creatives " +
									  "SET CRV_imageFilename = '" + imageFilename + "', " + 
								 	 	  "CRV_height = '" + imageWidth + "', " +
								 	 	  "CRV_width = '" + imageHeight + "' " +
								 	  "WHERE CRV_id = " + _id);
						
		} catch (Exception e) {
        	throw new AdShotRunnerException("Could not store Creative image URL in database: " + imageFilename, e);
		}
		
		//Set the instance variables
		_imageFilename = imageFilename;
		_image = newImage;
		_width = imageWidth;
		_height = imageHeight;
	}
	
	/**
	 * Sets the processing status of the Creative and sets the associated timestamp.
	 * 
	 * The options are the static members: QUEUED, PROCESSING, FINISHED
	 * 
	 * If a timestamp for the passed status already exists, this function will overwrite it
	 * with the new current time.
	 * 
	 * If the provided status is empty or does not exist, an exception is thrown 
	 * 
	 * The ERROR status cannot be set with this function. setError(...) should be used.
	 * 
	 * @param creativeStatus	Creative status has defined by static members: QUEUED, PROCESSING, FINISHED
	 */
	public void setStatus(String creativeStatus) {
		
		//If the status is an empty string, throw an error
		if (creativeStatus.isEmpty()) {
        	throw new AdShotRunnerException("Empty string passed as a Creative status");
		}
		
		//Determine the timestamp field to change based on the status.
		String timestampField = "";
		switch (creativeStatus) {
		
			case QUEUED: timestampField = "CRV_queuedTimestamp"; break;
			case PROCESSING: timestampField = "CRV_processingTimestamp"; break;
			case FINISHED: timestampField = "CRV_finishedTimestamp"; break;
			
			//If the passed status did not match an official status, return an exception
			default: 
	        	throw new AdShotRunnerException("Attempt to set non-permitted Creative status: " + creativeStatus);
		}
		
		//Update the status in the database
		try {
			ASRDatabase.executeUpdate("UPDATE creatives " +
									  "SET CRV_status = '" + creativeStatus + "', " + 
									 	   timestampField + " = CURRENT_TIMESTAMP " +
									  "WHERE CRV_id = " + _id);
			
			//Set the new status in the instance
			_status = creativeStatus;
			
			//Query the database for the new timestamp. This is to prevent localization errors.
			//Load all three timestamps to simplify code
			ResultSet creativeResult = ASRDatabase.executeQuery("SELECT * FROM creatives WHERE CRV_id = " + _id);		
			if (creativeResult.next()) {
				_queuedTimestamp = creativeResult.getTimestamp("CRV_queuedTimestamp");
				_processingTimestamp = creativeResult.getTimestamp("CRV_processingTimestamp");
				_finishedTimestamp = creativeResult.getTimestamp("CRV_finishedTimestamp");
			}	
			
		} catch (Exception e) {
			e.printStackTrace();
        	throw new AdShotRunnerException("Could not update Creative status in the database: " + creativeStatus, e);
		}
	}

	/**
	 * Sets the processing status of the Creative to ERROR, sets the error timestamp, and stores
	 * the passed error message.
	 * 
	 * If an error already exists for the Creative, this function will overwrite it and its
	 * timestamp.
	 * 
	 * @param errorMessage	Error message
	 */
	public void setError(String errorMessage) {
				
		//Update the status in the database
		try {
			ASRDatabase.executeUpdate("UPDATE creatives " +
									 "SET CRV_status = '" + ERROR + "', " + 
								 	 	 "CRV_errorMessage = '" + errorMessage + "', " +
								 	 	 "CRV_errorTimestamp = CURRENT_TIMESTAMP " +
									 "WHERE CRV_id = " + _id);
			
			//Set the instance's error message and status
			_errorMessage = errorMessage;
			_status = ERROR;
			
			//Query the database for the new timestamp. This is to prevent localization errors.
			ResultSet creativeResult = ASRDatabase.executeQuery("SELECT * FROM creatives WHERE CRV_id = " + _id);		
			if (creativeResult.next()) {
				_errorTimestamp = creativeResult.getTimestamp("CRV_errorTimestamp");
			}	
			
		} catch (Exception e) {
        	throw new AdShotRunnerException("Could not store Creative error in the database: " + errorMessage, e);
		}
	}
	
	/**
	 * Allows sorting Creatives based on priority
	 */
	@Override
	public int compareTo(Creative otherCreative) {
	    return Integer.compare(this._priority, otherCreative._priority);
	}
}
