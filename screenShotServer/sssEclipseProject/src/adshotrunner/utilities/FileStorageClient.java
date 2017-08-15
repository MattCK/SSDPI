package adshotrunner.utilities;

import java.io.File;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * 
 * The FileStorageClient saves and deletes files for system wide (and optionally public) access
 *
 */
public class FileStorageClient {
	
	//--------------------------------------------------------------------------------------
	//---------------------------------- Static Methods ------------------------------------
	//--------------------------------------------------------------------------------------
	//***************************** Public Static Methods **********************************
	/**
	 * Saves a file in the chosen container with the supplied filename.
	 * 
	 * @param containerName		Name of the container to save the file in
	 * @param fileToSave		Name and optional path of file to save
	 * @param newFilename		Name that the file to save should be stored under
	 */
	public static void saveFile(String containerName, String fileToSave, String newFilename) {
        
        //Instantiate an Amazon S3 client using AdShotRunner's AWS credentials
        AmazonS3 s3client = new AmazonS3Client(AWSConnector.getCredentials());
        
        //Upload the file
        File fileToUpload = new File(fileToSave);
        s3client.putObject(new PutObjectRequest(containerName, newFilename, fileToUpload));        
	}
	
	/**
	 * Deletes a file from the chosen container
	 * 
	 * @param containerName		Name of the container the file resides in
	 * @param filename			Name of the file to delete
	 */
	public static void deleteFile(String containerName, String filename) {
        
        //Instantiate an Amazon S3 client using AdShotRunner's AWS credentials
        AmazonS3 s3client = new AmazonS3Client(AWSConnector.getCredentials());
        
        //Delete the file
        s3client.deleteObject(new DeleteObjectRequest(containerName, filename));	        
	}
	
	
}
