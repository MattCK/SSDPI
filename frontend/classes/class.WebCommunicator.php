<?PHP

/**
* Contains the class for communicating with  the web
*
* @package Adshotrunner
* @subpackage Classes
*/

/**
* The WebPageCommunicator class communicates with remote URL resources. 
*
* This class can be used to query a single resource or many. If many requests are made at the same time, the class is 
* optimized to connect to multiple resources at once. The amount of simultaneous connections can be controlled for a 
* given instance.
*/
class WebPageCommunicator {

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	* @var boolean		Flags whether or not to retrieve the header. DEFAULT: FALSE
	*/
	private $_header;

	/**
	* @var boolean		Flags whether or not to return the transfer as a string instead of outputting it out directly. DEFAULT: TRUE
	*/
	private $_returnTransfer;

	/**
	* @var boolean		Flags whether to fail verbosely if the HTTP code returned is greater than or equal to 400. DEFAULT: FALSE
	*/
	private $_failOnError;

	/**
	* @var boolean		Flags whether to follow any "Location: " header that the server sends as part of the HTTP header. DEFAULT: TRUE
	*/
	private $_followLocation;

	/**
	* @var int			Connection timeout in seconds. DEFAULT: 30
	*/
	private $_timeout;

	/**
	* @var	boolean		Flags whether or not to use SOCKS5 with a proxy, instead of simple HTTP. DEFAULT: TRUE
	*/
	private $_useSocks5;

	/**
	* @var int			Determines the maximum amount of connections made at one time. HTTP. DEFAULT: 8
	*/
	private $_maxGroupSize;

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Instantiates class and sets default values.
	*/
	function __construct() {
				
		$this->setRetrieveHeader(FALSE);
		$this->setReturnTransfer(TRUE);
		$this->setFailOnError(FALSE);
		$this->setFollowLocation(TRUE);
		$this->setTimeout(20);
		$this->setSocks5(TRUE);
		$this->setMaxGroupSize(35);
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods *************************************
	/**
	* Returns the response from a URL resource connected to with passed parameters.
	*
	* @param 	string 			$url 			URL to connect to
	* @param 	mixed 			$postFields 	Optional associative array or query string to send as POST with request
	* @param 	boolean			$useProxy 		Flags whether or not to use a proxy
	* @param 	string 			$proxy 			Proxy address to use if $useProxy is set to TRUE
	* @retval 	string  						String response from URL resource
	*/
	public function getURLResponse($url, $postFields = null, $useProxy = false, $proxy = null) {

		$session = $this->getCurlHandle($url, $postFields, $useProxy, $proxy);
		$responseText = curl_exec($session);
		curl_close($session);
		return $responseText;
	}

	/**
	* Returns responses from multiple URLs. It returns the associative array of URLs pointing to their responses.
	*
	* @param 	mixed 			$urlArray 		Array of string URLs for which to connect
	* @retval 	mixed  							Associative array of URLs pointing to their response
	*/
	public function getManyURLResponses($urlArray) {
		
		//If no array or an empty one is passed, return an empty array
		if ((!$urlArray) || (count($urlArray) == 0)) {return array();}

		//Get the number of URLs and determine the number of groups we're going to be requesting
		$numberOfURLs = count($urlArray);
		$maxGroupSize = $this->getMaxGroupSize();
		$numberOfGroups = ceil($numberOfURLs/$maxGroupSize);

		//Divide the array up into groups and request their responses
		$urlResponses = array();
		for ($i = 0; $i < $numberOfGroups; ++$i) {
			
			//Determine the current group
			$curURLGroup = array_slice($urlArray, ($i * $maxGroupSize), $maxGroupSize, true);
			
			//Get the responses for the group and add them to the overall set
			$returnedResponses = $this->getURLGroupResponses($curURLGroup);
			$urlResponses += array_merge($urlResponses, $returnedResponses);
		}

		//Check all of the responses. If a response was empty, request it again but by itself.
		foreach ($urlResponses as $curURL => $curResponse) {
			if (!$curResponse) {
				$urlResponses[$curURL] = $this->getURLResponse($curURL);
			}
		}

		//Finally, return the completed list
		return $urlResponses;

	}


	//********************************* Private Methods ************************************

	/**
	* Returns responses from multiple URLs. It returns the associative array of URLs pointing to their responses.
	*
	* @param 	mixed 			$urlArray 		Array of string URLs for which to connect
	* @retval 	mixed  							Associative array of URLs pointing to their response
	*/
	private function getURLGroupResponses($urlArray) {
		
		//First, create the curl multi-handle
		$multiHandle = curl_multi_init();

		//Create a handle for each URL and add it to the multi-handle
		$allHandles = array();
		foreach ($urlArray as $curURL) {
			$curURLHandle = $this->getCurlHandle($curURL);
			curl_multi_add_handle($multiHandle, $curURLHandle);
			$allHandles[] = $curURLHandle;
		}
		
		//Execute the multi-handle call
		$isRunning = null;
		do {
			curl_multi_exec($multiHandle, $isRunning);
			usleep (250000); //Sleep for 0.25 seconds to reduce load
		} while ($isRunning > 0);

		//Store the response info for each handle
		$responsesArray = array();
		foreach ($allHandles as $curHandle) {
		
			//Decode and store the response
			$responsesArray[] = curl_multi_getcontent($curHandle);

			//Remove the handle from the multi-handle and close it.
			curl_multi_remove_handle($multiHandle, $curHandle);
			curl_close($curHandle);
		}
		
		//Close the multi-handle for resources
		curl_multi_close($multiHandle);
		
		//Merge the URL and response arrays into one
		$urlsWithResponses = array();
		foreach ($urlArray as $curKey => $curURL) {
			$urlsWithResponses[$curURL] = $responsesArray[$curKey];
		}

		//Return the array of responses
		return $urlsWithResponses;	
	}

	/**
	* Returns a CURL handle for connecting to a remote resource.
	*
	* The following parameters are set from the class instance: CURLOPT_HEADER, CURLOPT_RETURNTRANSFER,
	* CURLOPT_FAILONERROR, CURLOPT_FOLLOWLOCATION, CURLOPT_PROXYTYPE.
	*
	* @param 	string 			$url 			Resource to connect to
	* @param 	mixed 			$postFields 	Optional associative array or query string to send with request. DEFAULT: null
	* @param 	boolean 		$useProxy 		Flags whether or not a proxy should be used. DEFAULT: false
	* @param 	string 			$proxy 			Proxy to use if $useProxy is set to TRUE. DEFAULT: 127.0.0.1:9050
	* @retval 	cURLHandle   					cURL handle used to connect to the remote resource.
	*/
	private function getCurlHandle($url, $postFields = null, $useProxy = false, $proxy = "127.0.0.1:9050") {
		
		//Initiate the session and set basic conditions
		$session = curl_init($url);
		curl_setopt($session, CURLOPT_HEADER, $this->retrieveHeader());
		curl_setopt($session, CURLOPT_RETURNTRANSFER, $this->returnTransfer());
		curl_setopt($session, CURLOPT_FAILONERROR, $this->failOnError());
		curl_setopt($session, CURLOPT_FOLLOWLOCATION, $this->followLocation());
		curl_setopt($session, CURLOPT_TIMEOUT, $this->getTimeout());
		
		//If going through a proxy, set it up
		if ($useProxy) {
			if ($this->socks5()) {
				curl_setopt($session, CURLOPT_PROXYTYPE, CURLPROXY_SOCKS5);
			}
			curl_setopt($session, CURLOPT_PROXY, $proxy);
		}
		
		//If post fields were passed, send them
		if ($postFields) {
			curl_setopt($session, CURLOPT_POST, true);
			curl_setopt($session, CURLOPT_POSTFIELDS, $postFields);	
		}
		
		return $session;
	}



	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* Returns whether or not the instance is to retrieve the header.
	*
	* @retval boolean  Flag of whether or not to retrieve the header
	*/
	public function retrieveHeader() {
		return $this->_header;
	}
	/**
	* Sets whether or not the instance is to retrieve the header.
	*
	* @param boolean $retrieve  Flags whether or not the instance should retrieve the header
	*/
	private function setRetrieveHeader($retrieve) {
		$this->_header = $retrieve;
	}

	/**
	* Returns whether or not to return the transfer as a string instead of outputting it out directly.
	*
	* @retval boolean  Flag of whether or not to transfer as a string instead of outputting it out directly
	*/
	public function returnTransfer() {
		return $this->_returnTransfer;
	}
	/**
	* Sets whether or not the instance is to return the transfer as a string instead of outputting it out directly.
	*
	* @param boolean $retrieve  Flags whether or not the instance should return the transfer as a string instead of outputting it out directly
	*/
	private function setReturnTransfer($useReturnTransfer) {
		$this->_returnTransfer = $useReturnTransfer;
	}

	/**
	* Returns whether to fail verbosely if the HTTP code returned is greater than or equal to 400.
	*
	* @retval boolean  Flag of whether to fail verbosely if the HTTP code returned is greater than or equal to 400
	*/
	public function failOnError() {
		return $this->_failOnError;
	}
	/**
	* Sets whether to fail verbosely if the HTTP code returned is greater than or equal to 400.
	*
	* @param boolean $setFail  Flags whether to fail verbosely if the HTTP code returned is greater than or equal to 400
	*/
	private function setFailOnError($setFail) {
		$this->_failOnError = $setFail;
	}
	
	/**
	* Returns whether to follow any "Location: " header that the server sends as part of the HTTP header.
	*
	* @retval boolean  Flag of whether to follow any "Location: " header that the server sends as part of the HTTP header
	*/
	public function followLocation() {
		return $this->_followLocation;
	}
	/**
	* Sets whether to follow any "Location: " header that the server sends as part of the HTTP header.
	*
	* @param boolean $setFollow  Flags whether to follow any "Location: " header that the server sends as part of the HTTP header
	*/
	private function setFollowLocation($setFollow) {
		$this->_followLocation = $setFollow;
	}

	/**
	* Returns the connection timeout in seconds
	*
	* @retval int  Connection timeout in seconds
	*/
	public function getTimeout() {
		return $this->_timeout;
	}
	/**
	* Sets the connection timeout in seconds
	*
	* @param int $newTimeout  Connection timeout in seconds
	*/
	public function setTimeout($newTimeout) {
		$this->_timeout = $newTimeout;
	}

	/**
	* Returns whether or not to use SOCKS5 with a proxy, instead of simple HTTP.
	*
	* @retval boolean  Flag of whether or not to use SOCKS5 with a proxy, instead of simple HTTP
	*/
	public function socks5() {
		return $this->_useSocks5;
	}
	/**
	* Sets whether or not to use SOCKS5 with a proxy, instead of simple HTTP.
	*
	* @param boolean $asdf  Flags whether or not to use SOCKS5 with a proxy, instead of simple HTTP
	*/
	private function setSocks5($useSocks5) {
		$this->_useSocks5 = $useSocks5;
	}
	
	/**
	* Returns the max group size, the maximum amount of connections made at one time.
	*
	* @retval int  Max group size, the maximum amount of connections made at one time.
	*/
	public function getMaxGroupSize() {
		return $this->_maxGroupSize;
	}
	/**
	* Sets the max group size, the maximum amount of connections made at one time.
	*
	* @param int $newMaxGroupSize  Max group size, the maximum amount of connections made at one time.
	*/
	public function setMaxGroupSize($newMaxGroupSize) {
		$this->_maxGroupSize = $newMaxGroupSize;
	}
	
		
	//********************************* Private Accessors ***********************************


}

//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------

