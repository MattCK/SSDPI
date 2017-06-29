package adshotrunner.campaigns;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.storyfinder.StoryFinder;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.ASRDatabase;
import adshotrunner.utilities.FileStorageClient;
import adshotrunner.utilities.URLTool;

/**
 * Class to retrieve and manipulate AdShots.
 *
 * This class mirrors the PHP and Javascript AdShot classes.
 * ANY CHANGES TO THIS CLASS SHOULD BE MADE IN THOSE CLASSES AS WELL.
 */
public class AdShot {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//Status constants for AdShot processing
	final public static String CREATED = "CREATED"; 
	final public static String PROCESSING = "PROCESSING"; 
	final public static String FINISHED = "FINISHED"; 
	final public static String ERROR = "ERROR"; 

	//URL path for creative images and tag pages
	final public static String SCREENSHOTURLPATH = "http://s3.amazonaws.com/" + ASRProperties.containerForScreenshots() + "/";
	
	//Number of stories the StoryFinder should return
	final public static int NUMBEROFSTORIES = 3;
	
	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	/**
	 * Retrieves and returns an AdShot from the database with the provided ID.
	 * 
	 * @param adShotID		ID of the AdShot
	 * @return				AdShot on success and NULL on failure
	 */	
	public static AdShot getAdShot(int adShotID) {
		
		//Check to see if an AdShot with that ID exists in the database
		//Redundant check along with constructor
		try {
			ResultSet adshotResult = ASRDatabase.executeQuery("SELECT * FROM adshots WHERE ADS_id = " + adShotID);
	
			//If a match was found, return the Creative 
			if (adshotResult.next()) {
				return new AdShot(adShotID);
			}	
			
			//Otherwise simply return null
			else {
				System.out.println("Unable to get AdShot with ID: " + adShotID);
				return null;
			}
		}
		
		//If we couldn't query the database correctly or another error occurred, print it out and return null
		catch (Exception e) {
			e.printStackTrace();;
			return null;
		}		
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	 * ID of the AdShot
	 */
	final private int _id;

	/**
	 * UUID of the AdShot
	 */
	final private String _uuid;
			
	/**
	 * ID of the campaign the AdShot is associated with
	 */
	final private int _campaignID;

	/**
	 * Requested URL of the screenshot. (This is the URL before the StoryFinder is ran if so flagged)
	 */
	final private String _requestedURL;
			
	/**
	 * Flags whether or not the StoryFinder should be used. (TRUE to use the StoryFinder)
	 */
	final private boolean _storyFinder;
			
	/**
	 * Flags whether or not the AdShot is for mobile (TRUE to use mobile)
	 */
	final private boolean _mobile;
			
	/**
	 * Flags whether or not the AdShot should be taken below the fold (TRUE to take it below the fold)
	 */
	final private boolean _belowTheFold;
			
	/**
	 * Creatives associated with the AdShot
	 */
	private Set<Creative> _creatives;
			
	/**
	 * List of candidate target URLs. Includes either the requested URL or the set of possible URLs from the StoryFinder.
	 */
	private List<String> _candidateURLs;
			
	/**
	 * Final Current target URL index of candidate URLs
	 */
	private int _candidateIndex;
	
	/**
	 * Final URL of the AdShot
	 */
	private String _finalURL;
	
	/**
	 * Page title of the AdShot's final URL
	 */
	private String _pageTitle;
	
	/**
	 * Creatives injected into the final AdShot image (This will always be the set or a subset of the associated Creatives)
	 */
	private Set<Creative> _injectedCreatives;
	
	/**
	 * Filename of the AdShot image
	 */
	private String _imageFilename;
			
	/**
	 * Image of the AdShot 
	 */
	private BufferedImage _image;
	
	/**
	 * Width of the AdShot image in pixels
	 */
	private int _width;
	
	/**
	 * Height of the AdShot image in pixels
	 */
	private int _height;
	
	/**
	 * The current processing status of the AdShot.
	 * Possible values set by public static status members: CREATED, PROCESSING, FINISHED, ERROR
	 */
	private String _status;

	/**
	 * Error message if an error occurred while processing the AdShot
	 */
	private String _errorMessage;
	
	/**
	 * The timestamp the AdShot was inserted into the database
	 */
	private Timestamp _createdTimestamp;
	
	/**
	 * The UNIX timestamp the AdShot status was set to PROCESSING
	 */
	private Timestamp _processingTimestamp;
	
	/**
	 * The UNIX timestamp the AdShot status was set to FINISHED
	 */
	private Timestamp _finishedTimestamp;

	/**
	 * The UNIX timestamp the AdShot status was set to ERROR when an error occurred
	 */
	private Timestamp _errorTimestamp;

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	private AdShot(int adShotID) throws SQLException {
		
		//Get the AdShot information from the database
		ResultSet adshotResult = ASRDatabase.executeQuery("SELECT * FROM adshots WHERE ADS_id = " + adShotID);		
		
		//If a Creative was found with the passed ID, set the instance's members to its details
		if (adshotResult.next()) {
			
			_id = adshotResult.getInt("ADS_id");
			_uuid = adshotResult.getString("ADS_uuid");
			_campaignID = adshotResult.getInt("ADS_CMP_id");
			_requestedURL = adshotResult.getString("ADS_requestedURL");
			_storyFinder = adshotResult.getBoolean("ADS_storyFinder");
			_mobile = adshotResult.getBoolean("ADS_mobile");
			_belowTheFold = adshotResult.getBoolean("ADS_belowTheFold");
			_finalURL = adshotResult.getString("ADS_finalURL");
			_pageTitle = adshotResult.getString("ADS_pageTitle");
			_imageFilename = adshotResult.getString("ADS_imageFilename");
			_width = adshotResult.getInt("ADS_width");
			_height = adshotResult.getInt("ADS_height");
			_status = adshotResult.getString("ADS_status");
			_errorMessage = adshotResult.getString("ADS_errorMessage");
			_createdTimestamp = adshotResult.getTimestamp("ADS_createdTimestamp");
			_processingTimestamp = adshotResult.getTimestamp("ADS_processingTimestamp");
			_finishedTimestamp = adshotResult.getTimestamp("ADS_finishedTimestamp");
			_errorTimestamp = adshotResult.getTimestamp("ADS_errorTimestamp");
		}	
		
		//Otherwise throw an error
		else {
			throw new AdShotRunnerException("Could not find AdShot with ID: " + adShotID);
		}	

		//Get the AdShot Creatives from the database
		ResultSet creativesResult = ASRDatabase.executeQuery("SELECT * FROM adshotCreatives WHERE ASC_ADS_id = " + adShotID);		
		
		//For each Creative ID found, add the Creative to the instance
		_creatives = new HashSet<Creative>();
		Map<Integer, Creative> creativesByID = new HashMap<Integer, Creative>();
		while (creativesResult.next()) {
			Creative adShotCreative = Creative.getCreative(creativesResult.getInt("ASC_CRV_id"));
			if (adShotCreative != null) {
				_creatives.add(adShotCreative);
				creativesByID.put(creativesResult.getInt("ASC_CRV_id"), adShotCreative);
			}
		}	
		
		//Get the injected Creatives from the database
		ResultSet injectedResult = ASRDatabase.executeQuery("SELECT * FROM injectedCreatives WHERE IJC_ADS_id = " + adShotID);		
		
		//For each injected Creative ID found, add the injected Creative to the instance
		_injectedCreatives = new HashSet<Creative>();
		while (injectedResult.next()) {
			if (creativesByID.containsKey(injectedResult.getInt("IJC_CRV_id"))) {
				_injectedCreatives.add(creativesByID.get(injectedResult.getInt("IJC_CRV_id")));
			}
		}	
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************		
	/**
	 * Returns the current target URL of the AdShot.
	 * 
	 * If story finder is set to false, this will always return the requested URL.
	 * 
	 * If story finder is set to true, this will return current candidate URL returned from the
	 * StoryFinder. If another alternate URL exists, calling nextCandidateURL() will make it
	 * the new target URL. hasNextCandidateURL will return TRUE if another candidate URL
	 * is available. 
	 * 
	 * @return		Current AdShot target URL
	 */
	public String targetURL() {
				
		/******** If Not Initialized, Get the Candidate URLs ********/
		if (_candidateURLs == null) {

			//Initialize the empty list
			_candidateURLs = new ArrayList<String>();
			_candidateIndex = 0;
			
			//If _storyFinder is false, just use the requested URL
			if (!_storyFinder) {
				_candidateURLs.add(URLTool.setProtocol("http",_requestedURL));
			}
			
			//If _storyFinder is true, get the stories
			else {
				try {
					_candidateURLs = new StoryFinder(URLTool.setProtocol("http", _requestedURL)).Scorer().getStories(NUMBEROFSTORIES);
				}
				catch (Exception e) {
					System.out.println("Unable to find stories for Adshot");
		        	throw new AdShotRunnerException("Unable to find stories for Adshot", e);
				}
			}
		}
		
		//Return the current candidate URL.
		return _candidateURLs.get(_candidateIndex);
	}
	
	/**
	 * Returns whether or not another candidate URL is available. TRUE if one exists,
	 * False otherwise.
	 * 
	 * @return		TRUE if another candidate URL is available, FALSE otherwise
	 */
	public boolean hasNextCandidateURL() {
		return (_candidateURLs.size() > (_candidateIndex + 1));
	}
	
	/**
	 * Iterates the AdShot to the next candidate URL (if one exists) and returns it.
	 * 
	 * If there is no candidate URL to iterate to, returns the current candidate/target URL.
	 * 
	 * @return		Next candidate URL if one exists, current candidate URL otherwise
	 */
	public String nextCandidateURL() {
		if (hasNextCandidateURL()) {_candidateIndex++;}
		return _candidateURLs.get(_candidateIndex);
	}

	/**
	 * Marks the passed Creative as injected into the AdShot.
	 * 
	 * The passed Creative must be in the AdShot's list of creatives. If
	 * it is not, FALSE is returned.
	 * 
	 * If the Creative is already marked as injected, nothing occurs.
	 * 
	 * @param injectedCreative
	 * @return
	 */
	public boolean creativeInjected(Creative injectedCreative) {
		
		//If the Creative is already marked as injected, just return true
		if (_injectedCreatives.contains(injectedCreative)) {return true;}
		
		//If the passed Creative exists in the AdShot's Creative set,
		//add it to the database and the injected Creatives set
		if (_creatives.contains(injectedCreative)) {
			try {
				ASRDatabase.executeUpdate("INSERT IGNORE INTO injectedCreatives (IJC_ADS_id, IJC_CRV_id)" +
										  "VALUES (" + _id + ", " + injectedCreative.id() + ")");
				
				//Add the Creative to the injected set
				_injectedCreatives.add(injectedCreative);
				return true;
				
			} catch (Exception e) {
	        	throw new AdShotRunnerException("Could not store injected AdShot Creative in the database: " + injectedCreative.id(), e);
			}
		}
		
		//If it is not in the Creatives set, return false
		return false;
	}
	
	/**
	 * Sets the final URL of the AdShot
	 * 
	 * @param finalURL		Final URL of the AdShot
	 */
	public void setFinalURL(String finalURL) {
				
		//Update the final URL in the database
		try {
			ASRDatabase.executeUpdate("UPDATE adshots " +
									  "SET ADS_finalURL = '" + finalURL + "' " +
									  "WHERE ADS_id = " + _id);
			
			//Set the instance's final URL
			_finalURL = finalURL;	
			
		} catch (Exception e) {
        	throw new AdShotRunnerException("Could not store AdShot final URL in the database: " + finalURL, e);
		}
	}

	/**
	 * Sets the page title of the final URL of the AdShot
	 * 
	 * @param pageTitle		Page title of the final URL of the AdShot
	 */
	public void setPageTitle(String pageTitle) {
		
		//Update the final URL in the database
		try {
			ASRDatabase.executeUpdate("UPDATE adshots " +
									  "SET ADS_pageTitle = '" + pageTitle.replaceAll("'", "\\\\'") + "' " +
									  "WHERE ADS_id = " + _id);
			
			//Set the instance's final URL
			_pageTitle = pageTitle;	
			
		} catch (Exception e) {
        	throw new AdShotRunnerException("Could not store AdShot page title in the database: " + pageTitle, e);
		}
	}

	/**
	 * Set the image of the AdShot. (This function uploads the image to file storage)
	 * 
	 * Uploads the image to storage. If successful, the image is stored in the
	 * object, the width and height are set based on it, and the AdShot is updated
	 * in the database with the new information.
	 * 
	 * @param newImage			New AdShot image
	 */
	public void setImage(BufferedImage newImage) {
		
		//If a null image is passd, return an exception
		if (newImage == null) {
        	throw new AdShotRunnerException("NULL passed to AdShot.setImage");
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
			FileStorageClient.saveFile(ASRProperties.containerForScreenshots(), 
					   				   ASRProperties.pathForTemporaryFiles() + imageFilename, imageFilename);
		} catch (Exception e) {
        	throw new AdShotRunnerException("Could not upload AdShot image: " + imageFilename, e);			
		}

		//Delete the local file
		File tagImageFile = new File(ASRProperties.pathForTemporaryFiles() + imageFilename);
		tagImageFile.delete();

		//Get the image width and height
		int imageWidth = newImage.getWidth();
		int imageHeight = newImage.getHeight();
		
		//Store the image filename, width, and height in the database
		try {
			ASRDatabase.executeUpdate("UPDATE adshots " +
									  "SET ADS_imageFilename = '" + imageFilename + "', " + 
								 	 	  "ADS_width = '" + imageWidth + "', " +
								 	 	  "ADS_height = '" + imageHeight + "' " +
								 	  "WHERE ADS_id = " + _id);
						
		} catch (Exception e) {
        	throw new AdShotRunnerException("Could not store AdShot image URL in database: " + imageFilename, e);
		}
		
		//Set the instance variables
		_imageFilename = imageFilename;
		_image = newImage;
		_width = imageWidth;
		_height = imageHeight;
	}
	
	/**
	 * Sets the processing status of the AdShot and sets the associated timestamp.
	 * 
	 * The options are the static members: PROCESSING, FINISHED
	 * 
	 * If a timestamp for the passed status already exists, this function will overwrite it
	 * with the new current time.
	 * 
	 * If the provided status is empty or does not exist, an exception is thrown 
	 * 
	 * The ERROR status cannot be set with this function. setError(...) should be used.
	 * 
	 * @param adShotStatus	AdShot status has defined by static members: PROCESSING, FINISHED
	 */
	public void setStatus(String adShotStatus) {
		
		//If the status is an empty string, throw an error
		if (adShotStatus.isEmpty()) {
        	throw new AdShotRunnerException("Empty string passed as an AdShot status");
		}
		
		//Determine the timestamp field to change based on the status.
		String timestampField = "";
		switch (adShotStatus) {
		
			case PROCESSING: timestampField = "ADS_processingTimestamp"; break;
			case FINISHED: timestampField = "ADS_finishedTimestamp"; break;
			
			//If the passed status did not match an official status, return an exception
			default: 
	        	throw new AdShotRunnerException("Attempt to set non-permitted AdShot status: " + adShotStatus);
		}
		
		//Update the status in the database
		try {
			ASRDatabase.executeUpdate("UPDATE adshots " +
									  "SET ADS_status = '" + adShotStatus + "', " + 
									 	   timestampField + " = CURRENT_TIMESTAMP " +
									  "WHERE ADS_id = " + _id);
			
			//Set the new status in the instance
			_status = adShotStatus;
			
			//Query the database for the new timestamp. This is to prevent localization errors.
			//Load all three timestamps to simplify code
			ResultSet adShotResult = ASRDatabase.executeQuery("SELECT * FROM adshots WHERE ADS_id = " + _id);		
			if (adShotResult.next()) {
				_processingTimestamp = adShotResult.getTimestamp("ADS_processingTimestamp");
				_finishedTimestamp = adShotResult.getTimestamp("ADS_finishedTimestamp");
			}	
			
		} catch (Exception e) {
			e.printStackTrace();
        	throw new AdShotRunnerException("Could not update AdShot status in the database: " + adShotStatus, e);
		}
	}

	/**
	 * Sets the processing status of the AdShot to ERROR, sets the error timestamp, and stores
	 * the passed error message.
	 * 
	 * If an error already exists for the AdShot, this function will overwrite it and its
	 * timestamp.
	 * 
	 * @param errorMessage	Error message
	 */
	public void setError(String errorMessage) {
				
		//Update the status in the database
		try {
			ASRDatabase.executeUpdate("UPDATE adshots " +
									  "SET ADS_status = '" + ERROR + "', " + 
								 	 	 "ADS_errorMessage = '" + errorMessage + "', " +
								 	 	 "ADS_errorTimestamp = CURRENT_TIMESTAMP " +
									 "WHERE ADS_id = " + _id);
			
			//Set the instance's error message and status
			_errorMessage = errorMessage;
			_status = ERROR;
			
			//Query the database for the new timestamp. This is to prevent localization errors.
			ResultSet adShotResult = ASRDatabase.executeQuery("SELECT * FROM adshots WHERE ADS_id = " + _id);		
			if (adShotResult.next()) {
				_errorTimestamp = adShotResult.getTimestamp("ADS_errorTimestamp");
			}	
			
		} catch (Exception e) {
        	throw new AdShotRunnerException("Could not store AdShot error in the database: " + errorMessage, e);
		}
	}

	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	 * @return	ID of the AdShot
	 */
	public int id() {return _id;}
	
	/**
	 * @return	UUID of the AdShot
	 */
	public String uuid() {return _uuid;}
	
	/**
	 * @return	ID of the campaign the AdShot is associated with
	 */
	public int campaignID() {return _campaignID;}
	
	/**
	 * @return	Requested URL of the screenshot. (This is the URL before the StoryFinder is ran if so flagged)
	 */
	public String requestedURL() {return _requestedURL;}
	
	/**
	 * @return	Flag for whether or not the StoryFinder should be used. (TRUE to use the StoryFinder)
	 */
	public boolean storyFinder() {return _storyFinder;}
	
	/**
	 * @return	Flag for whether or not the AdShot is for mobile (TRUE to use mobile)
	 */
	public boolean mobile() {return _mobile;}
	
	/**
	 * @return	Flag for whether or not the AdShot should be taken below the fold (TRUE to take it below the fold)
	 */
	public boolean belowTheFold() {return _belowTheFold;}
	
	/**
	 * @return	Creatives associated with the AdShot
	 */
	public Set<Creative> creatives() {return _creatives;}
	
	/**
	 * @return	Final URL of the AdShot
	 */
	public String finalURL() {return _finalURL;}
	
	/**
	 * @return	Page title of the AdShot's final URL
	 */
	public String pageTitle() {return _pageTitle;}
	
	/**
	 * @return	Creatives injected into the final AdShot image (This will always be the set or a subset of the associated Creatives)
	 */
	public Set<Creative> injectedCreatives() {return _injectedCreatives;}
	
	/**
	 * @return	Filename of the AdShot image
	 */
	public String imageFilename() {return _imageFilename;}
	
	/**
	 * @return	Full URL including protocol to image
	 */
	public String imageURL() {
		if (_imageFilename.isEmpty()) {return "";}
		return SCREENSHOTURLPATH + _imageFilename;
	}
	
	/**
	 * @return	Image of the AdShot
	 */
	public BufferedImage image() {
		
		//If the image is NULL but a filename exists, get the image
		if ((_image == null) && (!_imageFilename.isEmpty())) {
			try {
				URL imageURL = new URL(SCREENSHOTURLPATH + _imageFilename);
				_image = ImageIO.read(imageURL); 
			}
	        catch (IOException e) {
	        	System.out.println("Could not load AdShot image from filename: " + _imageFilename);
	        	throw new AdShotRunnerException("Could not load AdShot image from filename: " + _imageFilename, e);
	        }
		}
		return _image;
	}
	
	/**
	 * @return	Width of AdShot image in pixels
	 */
	public int width() {return _width;}
	
	/**
	 * @return	Height of AdShot image in pixels
	 */
	public int height() {return _height;}
		
	/**
	 * @return	The current processing status of the AdShot. (Options static members: PROCESSING, FINISHED, ERROR)
	 */
	public String status() {return _status;}
	
	/**
	 * @return	Error message if an error occurred while processing the AdShot
	 */
	public String errorMessage() {return _errorMessage;}

	/**
	 * @return	The timestamp the AdShot was inserted into the database
	 */
	public Timestamp createdTimestamp() {return _createdTimestamp;}

	/**
	 * @return	The timestamp the AdShot status was set to PROCESSING
	 */
	public Timestamp processingTimestamp() {return _processingTimestamp;}

	/**
	 * @return	The timestamp the AdShot status was set to FINISHED
	 */
	public Timestamp finishedTimestamp() {return _finishedTimestamp;}

	/**
	 * @return	The timestamp the AdShot status was set to ERROR when an error occurred
	 */
	public Timestamp errorTimestamp() {return _errorTimestamp;}
	
	
//	/**
//	 * Allows sorting AdShots based on CREATED timestamp
//	 */
//	@Override
//	public int compareTo(AdShot2 otherAdShot) {
//		System.out.println(_createdTimestamp + " and " + otherAdShot.createdTimestamp() + ": " + _createdTimestamp.compareTo(otherAdShot.createdTimestamp()));
//		return _createdTimestamp.compareTo(otherAdShot.createdTimestamp());
//	}

}
