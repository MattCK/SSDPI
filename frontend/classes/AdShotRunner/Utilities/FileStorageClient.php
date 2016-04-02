<?PHP
/**
* Contains the class for storing and deleting files
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\Utilities;

/**
* The FileStorageClient saves and deletes files for system wide (and optionally public) access
*/
class FileStorageClient {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	const TAGIMAGESCONTAINER = "asr-tagimages";

	//--------------------------------------------------------------------------------------
	//---------------------------------- Static Methods ------------------------------------
	//--------------------------------------------------------------------------------------
	//***************************** Public Static Methods **********************************
	/**
	 * Saves a file in the chosen container with the supplied filename.
	 * 
	 * @param string 	containerName		Name of the container to save the file in
	 * @param string 	fileToSave			Name and optional path of file to save
	 * @param string 	newFilename			Name that the file to save should be stored under
	 */
	static public function saveFile($containerName, $fileToSave, $newFilename) {

		//Verify all arguments were not empty
		if (!$containerName || !$fileToSave || !$newFilename) {return FALSE;}

		//Create the S3 handler object
		$awsFactory = getAWSFactory();
		$s3Handler = $awsFactory->get('s3');

		//Store the file
		$s3Handler->putObject(array( 'Bucket'       => $containerName,
									 'SourceFile'   => $fileToSave,
									 'Key'          => $newFilename));
	}

	/**
	 * Deletes a file from the chosen container
	 * 
	 * @param string 	containerName		Name of the container the file resides in
	 * @param string 	filename			Name of the file to delete
	 */
	static public function deleteFile($containerName, $filename) {

		//Verify all arguments were not empty
		if (!$containerName || !$filename) {return FALSE;}

		//Create the S3 handler object
		$awsFactory = getAWSFactory();
		$s3Handler = $awsFactory->get('s3');

		//Delete the file
		$s3Handler->deleteObject(array( 'Bucket'       => $containerName,
									    'Key'          => $filename));
	}


}


//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------

