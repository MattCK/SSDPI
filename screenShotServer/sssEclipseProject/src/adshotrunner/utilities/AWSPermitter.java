package adshotrunner.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import adshotrunner.errors.AdShotRunnerException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

/**
 * The AWSPermitter class generates credentials to connect to the Amazon Web Services API
 */
public class AWSPermitter {
	
	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	final private static String CREDENTIALSPATH = "config/awsCredentials";
	final private static String ACCESSKEY = "----";
	final private static String SECRETKEY = "----";
	
	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	/**
	 * Returns credentials with permissions to interacts with pre-approved AWS services
	 * 
	 * @return				AWS credentials for API interaction
	 */
	public static AWSCredentials getCredentials() {
		AWSCredentials credentials = getCredentialsFromFile();
		return credentials;
	}

	//***************************** Private Static Methods ***********************************
	/**
	 * Returns credentials based on the access key and secret key stored in the class constants 
	 * ACCESSKEY and SECRETKEY.
	 * 
	 * This is not the preferred method since it hardcodes the credential information into a
	 * piece of code.
	 * 
	 * @return				AWS credentials for API interaction
	 */
	private static AWSCredentials getBasicCredentials() {
		AWSCredentials credentials = new BasicAWSCredentials(ACCESSKEY, SECRETKEY);
		return credentials;
	}

	/**
	 * Returns credentials based on the profile credential file.
	 * 
	 * The file MUST be stored as aws/credentials.
	 * 
	 * @return				AWS credentials for API interaction (set to null on failure)
	 */
	private static AWSCredentials getCredentialsFromFile() {
		File credentialsFile = new File(CREDENTIALSPATH);  
		AWSCredentials credentials = null;
		
		//Try to open the file and use it to create the credentials. On fail, toss a runtime exception.
		try {credentials = new PropertiesCredentials(credentialsFile);} 
		catch (IOException e) {throw new AdShotRunnerException("Could not open AWS credentials file", e);}
		
		//Return the final credentials
		return credentials;
	}


}
