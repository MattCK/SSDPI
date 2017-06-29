package adshotrunner.powerpoint;

import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.io.FileUtils;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.ASRDatabase;

/**
 * Class to retrieve PowerPoint background information
 */
public class PowerPointBackground {

	//URL path for background and thumbnail images
	final public static String BACKGROUNDURLPATH = "http://s3.amazonaws.com/" + 
												    ASRProperties.containerForPowerPointBackgrounds() + "/";
	final public static String THUMBNAILURLPATH = "http://s3.amazonaws.com/" + 
												    ASRProperties.containerForPowerPointBackgrounds() + "/thumbnails/";
	
	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	/**
	 * Retrieves and returns a PowerPointBackground from the database with the provided ID.
	 * 
	 * @param backgroundID		ID of the PowerPointBackground
	 * @return					PowerPointBackground on success and NULL on failure
	 */	
	public static PowerPointBackground getPowerPointBackground(int backgroundID) {
		
		//Check to see if a PowerPointBackground with that ID exists in the database
		//Redundant check along with constructor
		try {
			ResultSet backgroundResults = ASRDatabase.executeQuery("SELECT * " + 
																   "FROM powerPointBackgrounds " + 
																   "WHERE PPB_id = " + backgroundID);
	
			//If a match was found, return the background 
			if (backgroundResults.next()) {
				return new PowerPointBackground(backgroundID);
			}	
			
			//Otherwise simply return null
			else {
				System.out.println("Unable to get PowerPointBackground with ID: " + backgroundID);
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
	 * ID of the PowerPoint background
	 */
	final private int _id;

	/**
	 * User defined name of the PowerPoint background
	 */
	final private String _name;
	
	/**
	 * Six character color for the font used with the background. (i.e. FFDDEE)
	 */
	final private String _fontColor;
	
	/**
	 * The original filename of the uploaded PowerPoint background 
	 */
	final private String _originalFilename;
	
	/**
	 * Filename of the PowerPoint background as it appears in storage
	 */
	final private String _filename;
	
	/**
	 * Filename of the background image thumbnail
	 */
	final private String _thumbnailFilename;
	
	/**
	 * UNIX timestamp of when the background was uploaded
	 */
	final private Timestamp _timestamp;
	
	/**
	 * Flags whether or not PowerPoint background has been archived. TRUE if PowerPoint background has been archived.
	 */
	final private boolean _archived;

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	private PowerPointBackground(int backgroundID) throws SQLException {
		
		//Get the PowerPointBackground information from the database
		ResultSet backgroundResult = ASRDatabase.executeQuery("SELECT * " +
															  "FROM powerPointBackgrounds " + 
															  "WHERE PPB_id = " + backgroundID);		
		
		//If a PowerPointBackground was found with the passed ID, set the instance's members to its details
		if (backgroundResult.next()) {
			
			_id = backgroundResult.getInt("PPB_id");
			_name = backgroundResult.getString("PPB_name");
			_fontColor = backgroundResult.getString("PPB_fontColor");
			_originalFilename = backgroundResult.getString("PPB_originalFilename");
			_filename = backgroundResult.getString("PPB_filename");
			_thumbnailFilename = backgroundResult.getString("PPB_thumbnailFilename");
			_timestamp = backgroundResult.getTimestamp("PPB_timestamp");
			_archived = backgroundResult.getBoolean("PPB_archived");
		}	
		
		//Otherwise throw an error
		else {
			throw new AdShotRunnerException("Could not find PowerPointBackground with ID: " + backgroundID);
		}
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************		
	/**
	 * @return	Downloads the background image file and returns it (Should be deleted after use)
	 */
	public File file() {
		
		//If the background filename is empty, simply return null
		if (_filename.isEmpty()) {return null;}
		
		//Download the file
		try {
			URL backgroundURL = new URL(url());		
			String backgroundFilename = ASRProperties.pathForTemporaryFiles() + _filename;
			File backgroundFile = new File(backgroundFilename);		
			FileUtils.copyURLToFile(backgroundURL, backgroundFile);
	
			//Return the downloaded background file
			return backgroundFile;
		}
		catch (Exception e) {return null;}
	}
	

	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	 * @return	ID of the PowerPoint background
	 */
	public int id() {return _id;}
	
	/**
	 * @return	User defined name of the PowerPoint background
	 */
	public String name() {return _name;}
	
	/**
	 * @return	Six character color for the font used with the background. (i.e. FFDDEE)
	 */
	public String fontColor() {return _fontColor;}
	
	/**
	 * @return	The original filename of the uploaded PowerPoint background
	 */
	public String originalFilename() {return _originalFilename;}
	
	/**
	 * @return	Filename of the PowerPoint background as it appears in storage
	 */
	public String filename() {return _filename;}
	
	/**
	 * @return	Full URL including protocol to the background image
	 */
	public String url() {
		if (_filename.isEmpty()) {return "";}
		return BACKGROUNDURLPATH + _filename;
	}
	
	/**
	 * @return	Filename of the background image thumbnail
	 */
	public String thumbnailFilename() {return _thumbnailFilename;}
	
	/**
	 * @return	Full URL including protocol to the background thumbnail image
	 */
	public String thumbnailURL() {
		if (_thumbnailFilename.isEmpty()) {return "";}
		return THUMBNAILURLPATH + _thumbnailFilename;
	}
	
	/**
	 * @return	UNIX timestamp of when the background was uploaded
	 */
	final public Timestamp timestamp() {return _timestamp;}
	
	/**
	 * @return	Flags whether or not PowerPoint background has been archived. TRUE if PowerPoint background has been archived.
	 */
	public boolean archived() {return _archived;}
	
}
