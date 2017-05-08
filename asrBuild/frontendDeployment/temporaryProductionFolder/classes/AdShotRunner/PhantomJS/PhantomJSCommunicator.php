<?PHP
/**
* Contains the class for retrieving web HTML/Text responses using the PhantomJS system
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\PhantomJS;

//PhantomJSCommunicator::getResponse("http://nytimes.com");

/**
* The PhantomJSCommunicator returns the HTML/Text response from a provided URL
*/
class PhantomJSCommunicator {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	

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
	static public function getResponse($targetURL, $userAgent = "", $viewPortWidth = "", $viewPortHeight = "") {

		//If no target URL was passed, return NULL
		if (!$targetURL) {return NULL;}

		//In the case any arguments are NULL, make them empty strings
		$userAgent = (!$userAgent) ? "" : $userAgent;
		$userAgent = (!$viewPortWidth) ? "" : $viewPortWidth;
		$userAgent = (!$viewPortHeight) ? "" : $viewPortHeight;

		//Create the system call
		$phantomJSCall = __DIR__ . '/phantomjs ' . __DIR__ . '/retrieveHTML.js' . ' "' . $targetURL . '" "' . $userAgent . '" "' . 
																		 $viewPortWidth . '" "' . $viewPortHeight . '"';
		//echo "\nPhantomJSCall: $phantomJSCall \n";

		//Run the call and return the output
		return shell_exec($phantomJSCall);
	}
}
