package adshotrunner.campaigns;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import adshotrunner.errors.AdShotRunnerException;
import adshotrunner.powerpoint.CampaignPowerPoint;
import adshotrunner.powerpoint.PowerPointBackground;
import adshotrunner.system.ASRProperties;
import adshotrunner.utilities.ASRDatabase;
import adshotrunner.utilities.FileStorageClient;

/**
 * Class to retrieve and manipulate Campaigns.
 *
 * This class mirrors the PHP and Javascript AdShot classes.
 * ANY CHANGES TO THIS CLASS SHOULD BE MADE IN THOSE CLASSES AS WELL.
 */
public class Campaign {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	//Status constants for Campaign processing
	final public static String CREATED = "CREATED"; 
	final public static String READY = "READY"; 
	final public static String QUEUED = "QUEUED"; 
	final public static String PROCESSING = "PROCESSING"; 
	final public static String FINISHED = "FINISHED"; 
	final public static String ERROR = "ERROR"; 

	//Error constants (Note: String constants instead of Enum for symmetry with PHP/Javascript)
	final public static String SCREENSHOTCAPTURE = "SCREENSHOTCAPTURE"; 
	final public static String POWERPOINTGENERATION = "POWERPOINTGENERATION"; 
	final public static String CAMPAIGNEMAILSEND = "CAMPAIGNEMAILSEND"; 

	//URL path for PowerPoint files
	final public static String POWERPOINTURLPATH = "http://s3.amazonaws.com/" + ASRProperties.containerForPowerPoints() + "/";
	
	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	/**
	 * Retrieves and returns a Campaign from the database with the provided ID.
	 * 
	 * @param campaignID	ID of the Campaign
	 * @return				Campaign on success and NULL on failure
	 */	
	public static Campaign getCampaign(int campaignID) {
		
		//Check to see if a Campaign with that ID exists in the database
		//Redundant check along with constructor
		String getCampaignQuery = "SELECT * FROM campaigns WHERE CMP_id = " + campaignID;
		try (ResultSet campaignResults = ASRDatabase.executeQuery(getCampaignQuery)) {
	
			//If a match was found, return the Campaign 
			if (campaignResults.next()) {
				return new Campaign(campaignID);
			}	
			
			//Otherwise simply return null
			else {
				System.out.println("Unable to get Campaign with ID: " + campaignID);
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
	 * ID of the Campaign
	 */
	final private int _id;

	/**
	 * UUID of the Campaign
	 */
	final private String _uuid;
	
	/**
	 * Customer name for the campaign
	 */
	final private String _customerName;
	
	/**
	 * AdShots associated with the Campaign
	 */
	private Set<AdShot> _adShots;
	
	/**
	 * PowerPointBackground for the final PowerPoint
	 */
	private PowerPointBackground _powerPointBackground;
	
	/**
	 * Filename of the final PowerPoint
	 */
	private String _powerPointFilename;
	
	/**
	 * The current processing status of the Campaign.
	 * Possible values set by public static status members: CREATED, QUEUED, PROCESSING, FINISHED, ERROR
	 */
	private String _status;

	/**
	 * Error message if an error occurred while processing the Campaign
	 */
	private String _errorMessage;
	
	/**
	 * The timestamp the Campaign was inserted into the database
	 */
	private Timestamp _createdTimestamp;
	
	/**
	 * The timestamp the Campaign status was set to READY
	 */
	private Timestamp _readyTimestamp;
	
	/**
	 * The timestamp the Campaign status was set to QUEUED
	 */
	private Timestamp _queuedTimestamp;
	
	/**
	 * The UNIX timestamp the Campaign status was set to PROCESSING
	 */
	private Timestamp _processingTimestamp;
	
	/**
	 * The UNIX timestamp the Campaign status was set to FINISHED
	 */
	private Timestamp _finishedTimestamp;

	/**
	 * The UNIX timestamp the Campaign status was set to ERROR when an error occurred
	 */
	private Timestamp _errorTimestamp;
	
	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	private Campaign(int campaignID) {
		
		//Get the Campaign information from the database
		String getCampaignQuery = "SELECT * FROM campaigns WHERE CMP_id = " + campaignID;
		try (ResultSet campaignResult = ASRDatabase.executeQuery(getCampaignQuery)) {		
		
			//If a Creative was found with the passed ID, set the instance's members to its details
			if (campaignResult.next()) {
				
				_id = campaignResult.getInt("CMP_id");
				_uuid = campaignResult.getString("CMP_uuid");
				_customerName = campaignResult.getString("CMP_customerName");
				_powerPointFilename = campaignResult.getString("CMP_powerPointFilename");
				_status = campaignResult.getString("CMP_status");
				_errorMessage = campaignResult.getString("CMP_errorMessage");
				_createdTimestamp = campaignResult.getTimestamp("CMP_createdTimestamp");
				_readyTimestamp = campaignResult.getTimestamp("CMP_readyTimestamp");
				_queuedTimestamp = campaignResult.getTimestamp("CMP_queuedTimestamp");
				_processingTimestamp = campaignResult.getTimestamp("CMP_processingTimestamp");
				_finishedTimestamp = campaignResult.getTimestamp("CMP_finishedTimestamp");
				_errorTimestamp = campaignResult.getTimestamp("CMP_errorTimestamp");
			}	
			
			//Otherwise throw an error
			else {
				throw new AdShotRunnerException("Could not find Campaign with ID: " + campaignID);
			}
			
			//Get the PowerPointBackground for the Campaign
			_powerPointBackground = PowerPointBackground.getPowerPointBackground(campaignResult.getInt("CMP_PPB_id"));
			
		} catch (Exception e) {
			throw new AdShotRunnerException("Could not query database for Campaign with ID: " + campaignID, e);
		}				
		
		
		//Get the AdShots associated with the Campaign
		_adShots = new LinkedHashSet<AdShot>();
		String getAdShotsQuery = "SELECT * FROM adshots WHERE ADS_CMP_id = " + campaignID;
		try (ResultSet adshotsResult = ASRDatabase.executeQuery(getAdShotsQuery)) {
			while (adshotsResult.next()) {
				_adShots.add(AdShot.getAdShot(adshotsResult.getInt("ADS_id")));
			}
		} catch (Exception e) {
			throw new AdShotRunnerException("Could not query database for campaign AdShots: " + campaignID, e);
		}				
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************		
	/**
	 * Generates the final PowerPoint using the instance's AdShots, uploads it to storage,
	 * and stores its info in the database.
	 * 
	 * If there are no AdShots with a FINISHED status, an exception will be thrown.
	 * 
	 * @return		TRUE on success
	 */
	public boolean generatePowerPoint() throws Exception {
		
		//Verify at least one AdShot successfully finished. Throw an exception otherwise..
		boolean foundFinishedAdShot = false;
		for (AdShot currentAdShot : _adShots) {
			if (currentAdShot.status().equals(AdShot.FINISHED)) {foundFinishedAdShot = true;}
		}
		if (!foundFinishedAdShot) {throw new AdShotRunnerException("No finished AdShots available");}
		
		//Get the background. On failure, throw an exception.
		File backgroundImageFile = _powerPointBackground.file();
		if (backgroundImageFile == null) {throw new AdShotRunnerException("Could not download PowerPointBackground file");}
		
		//Get the current date as a MM/DD/YYYY string for the PowerPoint title page
		String campaignDate = new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime());
		
		//Create the PowerPoint
		CampaignPowerPoint powerPoint = new CampaignPowerPoint(_customerName, campaignDate, 
															   _powerPointBackground.fontColor(), 
															   backgroundImageFile);
		
		//Add a slide for each finished Desktop AdShot
		for (AdShot currentAdShot : _adShots) {
			if ((currentAdShot.status().equals(AdShot.FINISHED)) && (!currentAdShot.mobile())) {
				powerPoint.addSlide(currentAdShot);
			}
		}
		
		//Add a slide for each finished Mobile AdShot
		for (AdShot currentAdShot : _adShots) {
			if ((currentAdShot.status().equals(AdShot.FINISHED)) && (currentAdShot.mobile())) {
				powerPoint.addSlide(currentAdShot);
			}
		}
		
		//Create the PowerPoint filename
		String pptxCustomerName = _customerName.trim().replace(' ', '-');
		String pptxDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		String pptxTimestamp = Long.toString(Instant.now().getEpochSecond());
		pptxTimestamp = pptxTimestamp.substring(pptxTimestamp.length() - 6);
		String powerPointFilename = pptxCustomerName + "-" + pptxDate + "-" + pptxTimestamp + ".pptx";
		
		//Save the PowerPoint to a file and upload it
		powerPoint.save(ASRProperties.pathForTemporaryFiles() + powerPointFilename); 
		
    	try {Thread.sleep(250);} catch (InterruptedException e) {} //Make sure the file has time to save

		FileStorageClient.saveFile(ASRProperties.containerForPowerPoints(), 
				   				   ASRProperties.pathForTemporaryFiles() + powerPointFilename, powerPointFilename);
		
		//Store the new PowerPoint filename in the Campaign database entry
		ASRDatabase.executeUpdate("UPDATE campaigns " +
				  				  "SET CMP_powerPointFilename = '" + powerPointFilename + "' " +
				  				  "WHERE CMP_id = " + _id);
		
		//Store the filename in the instance
		_powerPointFilename = powerPointFilename;
		
		//Delete the files
		backgroundImageFile.delete();
		new File(ASRProperties.pathForTemporaryFiles() + powerPointFilename).delete();
		
		//Return true for success
		return true;
	}
	
	/**
	 * Returns the email address of the user who created the Campaign
	 * 
	 * @return 	Email address of the user who created the Campaign
	 */
	public String userEmailAddress() {
		
		//Query the database for the user
		try (ResultSet userResult = ASRDatabase.executeQuery("SELECT * " + 
															"FROM campaigns " + 
															"LEFT JOIN users ON CMP_USR_id = USR_id " +
															"WHERE CMP_id = " + _id)) {
			
			//Return the email address
			userResult.next();
			return userResult.getString("USR_emailAddress");
			
		} catch (Exception e) {
			e.printStackTrace();
	    	throw new AdShotRunnerException("Could not retrieve user email address for campgain: " + _id, e);
		}
	}
	
	/**
	 * Sets the processing status of the Campaign and sets the associated timestamp.
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
	 * @param campaignStatus	Campaign status has defined by static members: QUEUED, PROCESSING, FINISHED
	 */
	public void setStatus(String campaignStatus) {
		
		//If the status is an empty string, throw an error
		if (campaignStatus.isEmpty()) {
        	throw new AdShotRunnerException("Empty string passed as a Campaign status");
		}
		
		//Determine the timestamp field to change based on the status.
		String timestampField = "";
		switch (campaignStatus) {
		
			case READY: timestampField = "CMP_readyTimestamp"; break;
			case QUEUED: timestampField = "CMP_queuedTimestamp"; break;
			case PROCESSING: timestampField = "CMP_processingTimestamp"; break;
			case FINISHED: timestampField = "CMP_finishedTimestamp"; break;
			
			//If the passed status did not match an official status, return an exception
			default: 
	        	throw new AdShotRunnerException("Attempt to set non-permitted Campaign status: " + campaignStatus);
		}
		
		//Update the status in the database
		try {
			ASRDatabase.executeUpdate("UPDATE campaigns " +
									  "SET CMP_status = '" + campaignStatus + "', " + 
									 	   timestampField + " = CURRENT_TIMESTAMP " +
									  "WHERE CMP_id = " + _id);
			
			//Set the new status in the instance
			_status = campaignStatus;			
		} catch (Exception e) {
			e.printStackTrace();
        	throw new AdShotRunnerException("Could not update Campaign status in the database: " + campaignStatus, e);
		}
		
		//Query the database for the new timestamp. This is to prevent localization errors.
		//Load all three timestamps to simplify code
		String getCampaignQuery = "SELECT * FROM campaigns WHERE CMP_id = " + _id;
		try (ResultSet campaignResult = ASRDatabase.executeQuery(getCampaignQuery)) {		
			if (campaignResult.next()) {
				_readyTimestamp = campaignResult.getTimestamp("CMP_readyTimestamp");
				_queuedTimestamp = campaignResult.getTimestamp("CMP_queuedTimestamp");
				_processingTimestamp = campaignResult.getTimestamp("CMP_processingTimestamp");
				_finishedTimestamp = campaignResult.getTimestamp("CMP_finishedTimestamp");
			}	
		} catch (Exception e) {
        	throw new AdShotRunnerException("Could not retrieve updated Campaign statuses ", e);
		}		

	}

	/**
	 * Sets the processing status of the Campaign to ERROR, sets the error timestamp, and stores
	 * the passed error message.
	 * 
	 * If an error already exists for the Campaign, this function will overwrite it and its
	 * timestamp.
	 * 
	 * @param errorMessage	Error message
	 */
	public void setError(String errorMessage) {
				
		//Update the status in the database
		try {
			ASRDatabase.executeUpdate("UPDATE campaigns " +
									  "SET CMP_status = '" + ERROR + "', " + 
								 	 	  "CMP_errorMessage = '" + errorMessage + "', " +
								 	 	  "CMP_errorTimestamp = CURRENT_TIMESTAMP " +
									  "WHERE CMP_id = " + _id);
			
			//Set the instance's error message and status
			_errorMessage = errorMessage;
			_status = ERROR;
			
		} catch (Exception e) {
        	throw new AdShotRunnerException("Could not store Campaign error in the database: " + errorMessage, e);
		}
		
		//Query the database for the new timestamp. This is to prevent localization errors.
		String getCampaignQuery = "SELECT * FROM campaigns WHERE CMP_id = " + _id;
		try (ResultSet campaignResult = ASRDatabase.executeQuery(getCampaignQuery)) {		
			if (campaignResult.next()) {
				_errorTimestamp = campaignResult.getTimestamp("CMP_errorTimestamp");
			}	
		} catch (Exception e) {
        	throw new AdShotRunnerException("Could not retrieve updated Campaign error status ", e);
		}		

	}
	
	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	 * @return	ID of the Campaign
	 */
	public int id() {return _id;}
	
	/**
	 * @return	UUID of the Campaign
	 */
	public String uuid() {return _uuid;}
	
	/**
	 * @return	Customer name for the campaign
	 */
	public String customerName() {return _customerName;}
	
	/**
	 * @return	AdShots associated with the Campaign
	 */
	public Set<AdShot> adShots() {return _adShots;}
	
	/**
	 * @return	PowerPointBackground for the final PowerPoint
	 */
	public PowerPointBackground powerPointBackground() {return _powerPointBackground;}
	
	/**
	 * @return	Filename of the final PowerPoint
	 */
	public String powerPointFilename() {return _powerPointFilename;}
	
	/**
	 * @return	Full URL including protocol to final PowerPoint
	 */
	public String powerPointURL() {
		if (_powerPointFilename.isEmpty()) {return "";}
		return POWERPOINTURLPATH + _powerPointFilename;
	}
		
	/**
	 * @return	The current processing status of the Campaign. (Options static members: QUEUED, PROCESSING, FINISHED, ERROR)
	 */
	public String status() {return _status;}
	
	/**
	 * @return	Error message if an error occurred while processing the Campaign
	 */
	public String errorMessage() {return _errorMessage;}

	/**
	 * @return	The timestamp the Campaign was inserted into the database
	 */
	public Timestamp createdTimestamp() {return _createdTimestamp;}

	/**
	 * @return	The timestamp the Campaign status was set to READY
	 */
	public Timestamp readyTimestamp() {return _readyTimestamp;}

	/**
	 * @return	The timestamp the Campaign status was set to QUEUED
	 */
	public Timestamp queuedTimestamp() {return _queuedTimestamp;}

	/**
	 * @return	The timestamp the Campaign status was set to PROCESSING
	 */
	public Timestamp processingTimestamp() {return _processingTimestamp;}

	/**
	 * @return	The timestamp the Campaign status was set to FINISHED
	 */
	public Timestamp finishedTimestamp() {return _finishedTimestamp;}

	/**
	 * @return	The timestamp the Campaign status was set to ERROR when an error occurred
	 */
	public Timestamp errorTimestamp() {return _errorTimestamp;}

}
