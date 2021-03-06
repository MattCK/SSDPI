package adshotrunner.utilities;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import adshotrunner.system.ASRProperties;

/**
 * The AWSConnector class generates credentials to connect to the Amazon Web Services API
 */
public class AWSConnector {
	
	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	final private static String ACCESSKEY = ASRProperties.awsAccessKey();
	final private static String SECRETKEY = ASRProperties.awsSecretKey();
	
	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Variables -------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Private Static Variables ********************************
	private static AWSCredentials asrAWSCredentials = null;
	
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
		if (asrAWSCredentials == null) {
			asrAWSCredentials = new BasicAWSCredentials(ACCESSKEY, SECRETKEY);
		}
		return asrAWSCredentials;
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
	/*private static AWSCredentials getBasicCredentials() {
		AWSCredentials credentials = new BasicAWSCredentials(ACCESSKEY, SECRETKEY);
		return credentials;
	}*/

	/**
	 * Returns credentials based on the profile credential file.
	 * 
	 * The file MUST be stored as aws/credentials.
	 * 
	 * @return				AWS credentials for API interaction (set to null on failure)
	 */
	/*private static AWSCredentials getCredentialsFromFile() {
		File credentialsFile = new File(CREDENTIALSPATH);  
		AWSCredentials credentials = null;
		
		//Try to open the file and use it to create the credentials. On fail, toss a runtime exception.
		try {credentials = new PropertiesCredentials(credentialsFile);} 
		catch (IOException e) {throw new AdShotRunnerException("Could not open AWS credentials file", e);}
		
		//Return the final credentials
		return credentials;
	}*/


}
