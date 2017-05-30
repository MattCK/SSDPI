<?PHP
/**
* Contains the Client class used to retrieve information for a specified client
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\Clients;

use AdShotRunner\Database\ASRDatabase;

/**
* The Client class can be used to retrieve information for a specified client such as name,
* account number, or default PowerPoint background image filename.  
*/
class Client {

	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Methods ------------------------------------
	//---------------------------------------------------------------------------------------	
	/**
	* Returns an instance of the Client with the passed ID.
	*
	* @param int 		$clientID  	Database ID of the client to retrieve
	* @return Client 				Instance of the client with the provided database ID. NULL on failure.
	*/
	static public function getClient($clientID) {
		
		//Verify the ID and that it's not less than 1. If not, return NULL.
		if ((!$clientID) || (!is_numeric($clientID)) || ($clientID < 1)) {return NULL;}
		
		//Get the info from the clients table
		$getClientQuery = "SELECT * FROM clients WHERE CLN_id = $clientID";
		$clientResult = ASRDatabase::executeQuery($getClientQuery);
		$clientInfo = $clientResult->fetch_assoc();

		//If no data was returned, return NULL.
		if (!$clientInfo) {return NULL;}
		
		//Create an instance of the class and set its information
		$retrievedClient = new Client();
		$retrievedClient->setID($clientID);
		$retrievedClient->setName($clientInfo["CLN_name"]);
		$retrievedClient->setAccountNumber($clientInfo["CLN_accountNumber"]);
		$retrievedClient->setVerifyUserEmailAddress($clientInfo["CLN_verifyUserEmailAddress"]);
		$retrievedClient->setDfpNetworkCode($clientInfo["CLN_dfpNetworkCode"]);
		$retrievedClient->setPowerPointBackground($clientInfo["CLN_powerPointBackground"]);
		$retrievedClient->setPowerPointFontColor($clientInfo["CLN_powerPointFontColor"]);
		$retrievedClient->setContactName($clientInfo["CLN_contactName"]);
		$retrievedClient->setContactPhoneNumber($clientInfo["CLN_contactPhoneNumber"]);
		$retrievedClient->setContactEmailAddress($clientInfo["CLN_contactEmailAddress"]);
		$retrievedClient->setPrimaryWebsite($clientInfo["CLN_primaryWebsite"]);
		
		//Return the client
		return $retrievedClient;
	}

	/**
	* Returns an instance of the Client with the passed account number.
	*
	* @param int 		$accountNumber  	Account number of the client to retrieve
	* @return Client 						Instance of the client with the provided account number. NULL on failure.
	*/
	static public function getClientByAccountNumber($accountNumber) {
		
		//Verify the ID and that it's not less than 1. If not, return NULL.
		if ((!$accountNumber) || (!is_numeric($accountNumber)) || ($accountNumber < 1)) {return NULL;}
		
		//Get the info from the clients table
		$getClientQuery = "SELECT * FROM clients WHERE CLN_accountNumber = $accountNumber";
		$clientResult = ASRDatabase::executeQuery($getClientQuery);
		$clientInfo = $clientResult->fetch_assoc();

		//If no data was returned, return NULL.
		if (!$clientInfo) {return NULL;}
				
		//Return the client
		return Client::getClient($clientInfo["CLN_id"]);
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	* @var int  Unique ID of the client in the database
	*/
	private $id;

	/**
	* @var string  Name of the client company
	*/
	private $name;	

	/**
	* @var string  Client account number used to register new users
	*/
	private $accountNumber;

	/**
	* @var string  The email address that gets sent a "verify user" email when a new user registers
	*/
	private $verifyUserEmailAddress;

	/**
	* @var string  The network code of the client if they have given DFP access. (Empty string otherwise)
	*/
	private $dfpNetworkCode;

	/**
	* @var string  Filename of default PowerPoint background for new users
	*/
	private $powerPointBackground;

	/**
	* @var string  Six character hex color to be used as the default PowerPoint font color for new users
	*/
	private $powerPointFontColor;

	/**
	* @var string  Name of the client's contact person 
	*/
	private $contactName;

	/**
	* @var string  Phone number of the client's contact person 
	*/
	private $contactPhoneNumber;

	/**
	* @var string  Email address of the client's contact person 
	*/
	private $contactEmailAddress;

	/**
	* @var string  Primary website of the client, used generally for individual publishers (Empty string otherwise)
	*/
	private $primaryWebsite;

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Make the constructor private so only the static methods of the class can instantiate
	* a new instance
	*/
	private function __construct() {}

	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* Returns the database ID of the client
	*
	* @return int	database ID of the client
	*/
	public function getId(){
		return $this->id;
	}
	/**
	* Returns the name of the client's company
	*
	* @return string	name of the client's company
	*/
	public function getName(){
		return $this->name;
	}

	/**
	* Returns the account number of the client used to register new users
	*
	* @return string	account number of the client used to register new users
	*/
	public function getAccountNumber(){
		return $this->accountNumber;
	}

	/**
	* Returns the email address to send new user verification emails
	*
	* @return string	email address to send new user verification emails
	*/
	public function getVerifyUserEmailAddress(){
		return $this->verifyUserEmailAddress;
	}

	/**
	* Returns the DFP network code of the client if access was given
	*
	* @return string	DFP network code of the client if access was given
	*/
	public function getDfpNetworkCode(){
		return $this->dfpNetworkCode;
	}

	/**
	* Returns the filename of the default PowerPoint background for new users
	*
	* @return string	filename of the default PowerPoint background for new users
	*/
	public function getPowerPointBackground(){
		return $this->powerPointBackground;
	}

	/**
	* Returns the six character hex color to use as the default font color for PowerPoints
	*
	* @return string	six character hex color to use as the default font color for PowerPoints
	*/
	public function getPowerPointFontColor(){
		return $this->powerPointFontColor;
	}
	/**
	* Returns the name of the client's contact person
	*
	* @return string	name of the client's contact person
	*/
	public function getContactName(){
		return $this->contactName;
	}

	/**
	* Returns the phone number of the client's contact person
	*
	* @return string	phone number of the client's contact person
	*/
	public function getContactPhoneNumber(){
		return $this->contactPhoneNumber;
	}

	/**
	* Returns the email address of the client's contact person
	*
	* @return string	email address of the client's contact person
	*/
	public function getContactEmailAddress(){
		return $this->contactEmailAddress;
	}

	/**
	* Returns the primary website of the client if one exists (empty string otherwise)
	*
	* @return string	primary website of the client if one exists (empty string otherwise)
	*/
	public function getPrimaryWebsite(){
		return $this->primaryWebsite;
	}

	//********************************* Private Accessors ***********************************
	/**
	* Sets the database ID of the client
	*
	* @param int 	$newId  New database ID of the client 
	*/
	private function setId($newId){
		$this->id = $newId;
	}

	/**
	* Sets the name of the client's company
	*
	* @param string 	$newName  New name of the client's company 
	*/
	private function setName($newName){
		$this->name = $newName;
	}

	/**
	* Sets the account number of the client used to register new users
	*
	* @param string 	$newAccountNumber  New account number of the client used to register new users 
	*/
	private function setAccountNumber($newAccountNumber){
		$this->accountNumber = $newAccountNumber;
	}

	/**
	* Sets the email address to send new user verification emails
	*
	* @param string 	$newVerifyUserEmailAddress  New email address to send new user verification emails 
	*/
	private function setVerifyUserEmailAddress($newVerifyUserEmailAddress){
		$this->verifyUserEmailAddress = $newVerifyUserEmailAddress;
	}

	/**
	* Sets the DFP network code of the client if access was given
	*
	* @param string 	$newDfpNetworkCode  New DFP network code of the client if access was given
	*/
	private function setDfpNetworkCode($newDfpNetworkCode){
		$this->dfpNetworkCode = $newDfpNetworkCode;
	}

	/**
	* Sets the filename of the default PowerPoint background for new users
	*
	* @param string 	$newPowerPointBackground  New filename of the default PowerPoint background for new users 
	*/
	private function setPowerPointBackground($newPowerPointBackground){
		$this->powerPointBackground = $newPowerPointBackground;
	}

	/**
	* Sets the six character hex color to use as the default font color for PowerPoints
	*
	* @param string 	$newPowerPointFontColor  New six character hex color to use as the default font color for PowerPoints 
	*/
	private function setPowerPointFontColor($newPowerPointFontColor){
		$this->powerPointFontColor = $newPowerPointFontColor;
	}

	/**
	* Sets the name of the client's contact person
	*
	* @param string 	$newContactName  New name of the client's contact person 
	*/
	private function setContactName($newContactName){
		$this->contactName = $newContactName;
	}

	/**
	* Sets the phone number of the client's contact person
	*
	* @param string 	$newContactPhoneNumber  New phone number of the client's contact person 
	*/
	private function setContactPhoneNumber($newContactPhoneNumber){
		$this->contactPhoneNumber = $newContactPhoneNumber;
	}

	/**
	* Sets the email address of the client's contact person
	*
	* @param string 	$newContactEmailAddress  New email address of the client's contact person 
	*/
	private function setContactEmailAddress($newContactEmailAddress){
		$this->contactEmailAddress = $newContactEmailAddress;
	}

	/**
	* Sets the primary website of the client if one exists (empty string otherwise)
	*
	* @param string 	$newPrimaryWebsite  New primary website of the client if one exists (empty string otherwise) 
	*/
	private function setPrimaryWebsite($newPrimaryWebsite){
		$this->primaryWebsite = $newPrimaryWebsite;
	}

}