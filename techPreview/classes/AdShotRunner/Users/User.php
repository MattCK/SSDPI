<?PHP
/**
* Contains the class for inserting, modifying, archiving, and retrieving users in the database
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\Users;

use AdShotRunner\Database\ASRDatabase;

/**
* The User class controls the flow of information between the system and the database concerning user information. 
*
* The User class controls the flow of information between the system and the database concerning user information. The tables it accesses
* are 'users' and 'userLogins'. This class can be used to retrieve, create, edit, or archive a user.  
*
* This class does not encrypt the user's passwords. The encryption should be done before the password is passed 
* to an instance of the class. 
*
* It is important to note that this class does not modify all the fields in the table. 
* 	- It does not modify the version numbers or access the historical tables as triggers should be taking care of all of those functions. 
* 	- It cannot modify the admin status of a user even though it will get the information from the table. 
* 	- It will not attempt to get or retrieve the timestamp of the database tuple since the table itself should take care of updating the timestamp. 
* 	- The class will modify the archived status through the archive() function but will not allow external calls to set the archive status, thus it cannot be set through the insert() or update() functions.
*	- The class will not allow external calls to change the user id. This will be set automatically through the insert() and get() functions. This is to help prevent crossed data corruption.
*/
class User {

	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Methods ------------------------------------
	//---------------------------------------------------------------------------------------	
	/**
	* Returns an instance of the user with the passed ID.
	*
	* Returns an instance of the user whose ID matches the passed ID. All the information is automatically loaded into the object. On failure, NULL is returned.
	*
	* In order to update a user in the system, an instance of the user must first be retrieved through this method.
	*
	* @param int $userID  ID of the user to retrieve
	* @return User  Instance of the user wish to be retrieved. NULL on failure.
	*/
	static public function getUser($userID) {
		
		//Verify the ID and that it's not less than 1. If not, return NULL.
		if ((!$userID) || (!is_numeric($userID)) || ($userID < 1)) {return NULL;}
		
		//Get the info from the users table
		$getUserQuery = "SELECT * FROM users WHERE USR_id = $userID";
		$userResult = ASRDatabase::executeQuery($getUserQuery);
		$userInfo = $userResult->fetch_assoc();

		//If no data was returned, return NULL.
		if (!$userInfo) {return NULL;}
		
		//Create an instance of the class to return and put the info into it
		$curUser = new User($userInfo);
		$curUser->setID($userID);
		$curUser->setArchiveStatus($userInfo['USR_archived']);
		
		//Return the user
		return $curUser;
	}

	/**
	* Returns an instance of the user with the passed username.
	*
	* Returns an instance of the user whose username matches the passed username. All the information is automatically loaded into the object. On failure, NULL is returned.
	*
	* @param string $username  Username of the user to retrieve
	* @return User  Instance of the user wish to be retrieved. NULL on failure.
	*/
	static public function getUserByUsername($username) {
		
		//Verify a username was passed. If not, return NULL.
		if (!$username) {return NULL;}
		
		//Get the info from the users table
		$getUserQuery = "SELECT * FROM users WHERE USR_username LIKE '" . ASRDatabase::escape($username) . "'";
		$userResult = ASRDatabase::executeQuery($getUserQuery);
		$userInfo = $userResult->fetch_assoc();

		//If no data was returned, return NULL.
		if (!$userInfo) {return NULL;}

		//Get the user's DFP network code if one exists
		$userDFPNetworkCode = NULL;
		$emailSubdomain = array_pop(explode("@", $userInfo['USR_email']));
		if ($emailSubdomain) {
			$getDFPNetworkCodeQuery = "SELECT * FROM dfpNetworkCodes WHERE DNC_subdomain LIKE '" . ASRDatabase::escape($emailSubdomain) . "'";
			$dfpNetworkCodeResult = ASRDatabase::executeQuery($getDFPNetworkCodeQuery);
			$networkCodeInfo = $dfpNetworkCodeResult->fetch_assoc();
			if ($networkCodeInfo) {$userDFPNetworkCode = $networkCodeInfo['DNC_networkCode'];}
		}
		
		//Create an instance of the class to return and put the info into it
		$curUser = new User($userInfo);
		$curUser->setID($userInfo['USR_id']);
		$curUser->setArchiveStatus($userInfo['USR_archived']);
		$curUser->setDFPNetworkCode($userDFPNetworkCode);
		
		//Return the user
		return $curUser;
	}

	/**
	* Inserts the instance of a User into the users table as a new user in the system.
	*
	* Inserts the instance of a User into the users table as a new user in the system. On success, the User object is returned with its new ID set. On failure,
	* NULL is returned. All of the fields are sanitized against SQL injection attacks before insertion.
	*
	* @param User $newUser  User to insert into the table
	* @return User  Instance of the user passed with its new ID set. NULL on failure.
	*/
	static public function insert(User $newUser) {
		
		//Verify a User was passed. If not, return NULL.
		if (!$newUser) {return NULL;}
		
		//Add the info to the table
		$addUserQuery = "INSERT INTO users (USR_username,
											USR_password,
											USR_firstName,
											USR_lastName,
											USR_company,
											USR_email,
											USR_PPB_id,
											USR_verified)
						 VALUES ('" . ASRDatabase::escape($newUser->getUsername()) . "',
								'" . ASRDatabase::escape($newUser->getPassword()) . "',
								'" . ASRDatabase::escape($newUser->getFirstName()) . "',
								'" . ASRDatabase::escape($newUser->getLastName()) . "',
								'" . ASRDatabase::escape($newUser->getCompany()) . "',
								'" . ASRDatabase::escape($newUser->getEmail()) . "',
								'" . ASRDatabase::escape($newUser->getPowerPointBackgroundID()) . "',
								'" . ASRDatabase::escape($newUser->isVerified()) . "')";
		ASRDatabase::executeQuery($addUserQuery);
		
		//Set the id in the User
		$newUser->setID(ASRDatabase::lastInsertID());

		//Get the user's DFP network code if one exists and set it
		$userDFPNetworkCode = NULL;
		$emailSubdomain = array_pop(explode("@", $newUser->getEmail()));
		if ($emailSubdomain) {
			$getDFPNetworkCodeQuery = "SELECT * FROM dfpNetworkCodes WHERE DNC_subdomain LIKE '" . ASRDatabase::escape($emailSubdomain) . "'";
			$dfpNetworkCodeResult = ASRDatabase::executeQuery($getDFPNetworkCodeQuery);
			$networkCodeInfo = $dfpNetworkCodeResult->fetch_assoc();
			if ($networkCodeInfo) {$userDFPNetworkCode = $networkCodeInfo['DNC_networkCode'];}
		}
		$newUser->setDFPNetworkCode($userDFPNetworkCode);

		//Return the newly inserted user
		return $newUser;

	}

	/**
	* Updates the instance of a User into the users table.
	*
	* Updates the instance of a User into the users table. On success, the User object is returned . On failure,
	* NULL is returned. All of the fields are sanitized against SQL injection attacks before the update.
	*
	* If there is no difference in the User passed and the information in the database, no UPDATE is run on the table
	* but the function returns the User as though successful. This prevents unnecessary records in the history table.
	*
	* @param User $modifiedUser  User to update in the table
	* @return User  Instance of the user passed. NULL on failure.
	*/
	static public function update(User $modifiedUser) {
		
		//Verify a User was passed. If not, return NULL.
		if (!$modifiedUser) {return NULL;}
		
		//Verify the User has an ID and it's not less than 1. If not, return NULL.
		$userID = $modifiedUser->getID();
		if ((!$userID) || (!is_numeric($userID)) || ($userID < 1)) {return NULL;}
		
		//Verify the modified User has different info than what is in the database. If not, do nothing and return the (so-called) modified User.
		//This prevents unnecessary duplicates in the history table.
		$curUser = User::getUser($modifiedUser->getID());
		if ($modifiedUser == $curUser) {return $modifiedUser;}

		//Update the info in the table
		$updateUserQuery = "UPDATE users 
							SET USR_username = '" . ASRDatabase::escape($modifiedUser->getUsername()) . "',
								USR_password = '" . ASRDatabase::escape($modifiedUser->getPassword()) . "',
								USR_firstName = '" . ASRDatabase::escape($modifiedUser->getFirstName()) . "',
								USR_lastName = '" . ASRDatabase::escape($modifiedUser->getLastName()) . "',
								USR_company = '" . ASRDatabase::escape($modifiedUser->getCompany()) . "',
								USR_email = '" . ASRDatabase::escape($modifiedUser->getEmail()) . "',
								USR_PPB_id = '" . ASRDatabase::escape($modifiedUser->getPowerPointBackgroundID()) . "',
								USR_verified = '" . ASRDatabase::escape($modifiedUser->isVerified()) . "'
							WHERE USR_id = " . $modifiedUser->getID();
		ASRDatabase::executeQuery($updateUserQuery);
		
		//Return the user
		return $modifiedUser;
	}

	/**
	* Archives the User in the system with the passed ID.
	*
	* Archives the User in the users table that corresponds to the user ID. On success, the User object is returned . On failure,
	* NULL is returned. 
	*
	* This function will not archive an admin user. 
	* If the user is already archived, no UPDATE is done to prevent unnecessary tuples in the history table.
	*
	* @param int $userID  ID of the user to archive
	* @return User  Instance of the user archived
	*/
	static public function archive($userID) {
		
		//Verify and ID was passed and it's not less than 1. If not, return NULL.
		if ((!$userID) || (!is_numeric($userID)) || ($userID < 1)) {return NULL;}
		
		//Verify user in the database is not archived. If it is, do nothing and return the User.
		$curUser = User::getUser($userID);
		if ($curUser->isArchived()) {return $curUser;}
				
		//Archive the user
		$archiveUserQuery = "UPDATE users 
							 SET USR_archived = 1,
							 	 USR_USR_id = '" . CURUSERID . "'
							 WHERE USR_id = " . $userID;
		ASRDatabase::executeQuery($archiveUserQuery);
		
		//Send the archived user
		$curUser = User::getUser($userID);
		return $curUser;
	}

	/**
	* Sets the login timestamp for a user who is currently logging in into the userLogins table.
	*
	* Enters the passed user ID and the current timestamp into the userLogins table.  
	*
	* @param int $userID  ID of the user who is logging in
	* @return boolean  TRUE on success. NULL on failure.
	*/
	static public function setLoginTimestamp($userID) {
		
		//Verify the User has an ID and it's not less than 1. If not, return NULL.
		if ((!$userID) || (!is_numeric($userID)) || ($userID < 1)) {return NULL;}
		
		//Verify the user exists in the users table. If not, return NULL.
		if (!User::getUser($userID)) {return NULL;}
		
		//Set the login info in the the userLogins table
		$userLoginDeleteQuery = "DELETE FROM userLogins WHERE LGN_USR_id = " . $userID;
		$userLoginQuery = "INSERT INTO userLogins (LGN_USR_id) VALUES ('$userID')"; 
		ASRDatabase::executeQuery($userLoginDeleteQuery);
		ASRDatabase::executeQuery($userLoginQuery);
		
		//Send success if we made it this far
		return TRUE;
	}
	
	/**
	* Attempts to find the user with the passed username and password.
	*
	* On success, returns an instance of the matching user. On failure, NULL is returned. If the user matching the 
	* has been archived, failure will occur.
	*
	* NOTE: If the password is encrypted in the database, the encrypted version must be passed
	* into this function.
	*
	* @param string $username  The username of the user to find.
	* @param string $password  The password of the user to find.
	* @return User  Instance of the user matching the username and password. NULL on failure.
	*/
	static public function findUser($username, $password) {
		
		//Verify a username and password were passed. If not, return NULL.
		if ((!$username) || (!$password)) {return NULL;}
		
		//Look for the user and password in the database
		$searchQuery = "SELECT * FROM users
					    WHERE USR_username LIKE '" . ASRDatabase::escape($username) . "' AND 
							  USR_password = '" . ASRDatabase::escape($password) . "' AND
							  USR_archived = 0";
		$userResult = ASRDatabase::executeQuery($searchQuery);
		$userInfo = $userResult->fetch_assoc();
		
		//If no data was returned, return NULL.
		if (!$userInfo) {return NULL;}
		
		//Get the user's DFP network code if one exists
		$userDFPNetworkCode = NULL;
		$emailSubdomain = array_pop(explode("@", $userInfo['USR_email']));
		if ($emailSubdomain) {
			$getDFPNetworkCodeQuery = "SELECT * FROM dfpNetworkCodes WHERE DNC_subdomain LIKE '" . ASRDatabase::escape($emailSubdomain) . "'";
			$dfpNetworkCodeResult = ASRDatabase::executeQuery($getDFPNetworkCodeQuery);
			$networkCodeInfo = $dfpNetworkCodeResult->fetch_assoc();
			if ($networkCodeInfo) {$userDFPNetworkCode = $networkCodeInfo['DNC_networkCode'];}
		}
		
		//Create an instance of the class to return and put the info into it
		$curUser = new User($userInfo);
		$curUser->setID($userInfo['USR_id']);
		$curUser->setArchiveStatus($userInfo['USR_archived']);
		$curUser->setDFPNetworkCode($userDFPNetworkCode);
	
		//Return the user
		return $curUser;
	}
	
	/**
	* Stores the login timestamp for a user into the userLogins table.
	*
	* Enters the passed user ID and the current timestamp into the userLogins table.  
	*
	* @param int $userID  ID of the user who is logging in
	* @return boolean  TRUE on success. NULL on failure.
	*/
	static public function loginTimestamp($userID) {
		
		//Verify the User has an ID and it's not less than 1. If not, return NULL.
		if ((!$userID) || (!is_numeric($userID)) || ($userID < 1)) {return NULL;}
		
		//Verify the user exists in the users table. If not, return NULL.
		if (!User::getUser($userID)) {return NULL;}
		
		//Set the login info in the the userlogins table
		$userLoginDeleteQuery = "DELETE FROM userlogins WHERE LGN_USR_id = " . $userID;
		$userLoginQuery = "INSERT INTO userlogins (LGN_USR_id) VALUES ('$userID')"; 
		ASRDatabase::executeQuery($userLoginDeleteQuery);
		ASRDatabase::executeQuery($userLoginQuery);
		
		//Send success if we made it this far
		return TRUE;
	}


	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	* @var int  Unique ID of the user
	*/
	private $id;

	/**
	* @var string  Username of the user
	*/
	private $username;

	/**
	* @var string  Password of the user. Should be encrypted (i.e. sha1).
	*/
	private $password;

	/**
	* @var string  First name of the user
	*/
	private $firstName;

	/**
	* @var string  Last name of the user
	*/
	private $lastName;

	/**
	* @var string  Company where the user is employed
	*/
	private $company;

	/**
	* @var string  Email of the user
	*/
	private $email;

	/**
	* @var int  ID of the background image to use in Powerpoints (in powerPointBackgrounds table)
	*/
	private $powerPointBackgroundID;

	/**
	* @var boolean  Flags whether or not the user has been verified. TRUE if it has been verified.
	*/
	private $verified;

	/**
	* @var string  DFP network code of the user if one exists
	*/
	private $dfpNetworkCode;

	/**
	* @var boolean  Flags whether or not the user has been archived. TRUE if it has been archived.
	*/
	private $archived;


	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Initializes the user object according to the details passed to it.
	*
	* The constructor will create a User instance without any details if no information is passed. Otherwise, it receives an
	* associative array with the names of the table fields and their corresponding data. Any subset of this array can be passed
	* and the information will be set in the instance of the variable.
	*
	* Ex:
	* <code>
	* array (
	* 	'USR_username' => 3,
	*	'USR_firstName' => 'Bob',
	*	...
	* )
	* <\/code>
	*
	* It will receive all the fields in the table that it has a public set accessor for. (i.e. One cannot set the id or archive status.)
	*
	* @param mixed $userInfo  Associative array with the names of the table fields and their corresponding data
	*/
	function __construct($userInfo = NULL) {
		
		if (($userInfo) && (is_array($userInfo))) {
			$this->setUsername($userInfo['USR_username']);
			$this->setPassword($userInfo['USR_password']);
			$this->setFirstName($userInfo['USR_firstName']);
			$this->setLastName($userInfo['USR_lastName']);
			$this->setCompany($userInfo['USR_company']);
			$this->setEmail($userInfo['USR_email']);
			$this->setPowerPointBackgroundID($userInfo['USR_PPB_id']);
			$this->setVerifiedStatus($userInfo['USR_verified']);
		}
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------


	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* Returns the ID of the user. NULL if an ID has not been set.
	*
	* @return int  ID of the user
	*/
	public function getID(){
		return $this->id;
	}
	
	/**
	* Returns the username of the user
	*
	* @return string  Username of the user
	*/
	public function getUsername(){
		return $this->username;
	}
	/**
	* Sets the username of the user
	*
	* @param string $newUsername  New username for the user 
	*/
	public function setUsername($newUsername){
		$this->username = $newUsername;
	}
	
	/**
	* Returns the password of the user
	*
	* @return string  Password of the user
	*/
	public function getPassword(){
		return $this->password;
	}
	/**
	* Sets the password of the user
	*
	* @param string $newPassword  New password for the user 
	*/
	public function setPassword($newPassword){
		$this->password = $newPassword;
	}
	
	/**
	* Returns the first name of the user
	*
	* @return string  First name of the user
	*/
	public function getFirstName(){
		return $this->firstName;
	}
	/**
	* Sets the first name of the user
	*
	* @param string $newFirstName  New first name for the user 
	*/
	public function setFirstName($newFirstName){
		$this->firstName = $newFirstName;
	}
	
	/**
	* Returns the last name of the user
	*
	* @return string  Last name of the user
	*/
	public function getLastName(){
		return $this->lastName;
	}
	/**
	* Sets the last name of the user
	*
	* @param string $newLastName  New last name for the user 
	*/
	public function setLastName($newLastName){
		$this->lastName = $newLastName;
	}
	
	/**
	* Returns the company of the user
	*
	* @return string  Company of the user
	*/
	public function getCompany(){
		return $this->company;
	}
	/**
	* Sets the company of the user
	*
	* @param string $newCompany  New company for the user 
	*/
	public function setCompany($newCompany){
		$this->company = $newCompany;
	}
	
	/**
	* Returns the email of the user
	*
	* @return string  Email of the user
	*/
	public function getEmail(){
		return $this->email;
	}
	/**
	* Sets the email of the user
	*
	* @param string $newEmail  New email for the user 
	*/
	public function setEmail($newEmail){
		$this->email = $newEmail;
	}
	
	/**
	* Returns the ID of the PowerPoint background image (in powerPointBackgrounds table)
	*
	* @return string  ID of the PowerPoint background image (in powerPointBackgrounds table)
	*/
	public function getPowerPointBackgroundID(){
		return $this->powerPointBackgroundID;
	}
	/**
	* Sets the ID of the PowerPoint background image (in powerPointBackgrounds table)
	*
	* @param string $newPowerPointBackground  New ID of the PowerPoint background image (in powerPointBackgrounds table)
	*/
	public function setPowerPointBackgroundID($newPowerPointBackgroundID){
		$this->powerPointBackgroundID = $newPowerPointBackgroundID;
	}
	
	/**
	* Returns the whether or not the user has been verified.
	*
	* @return int  Verified status of the user.
	*/
	public function isVerified(){
		return $this->verified;
	}
	/**
	* Sets the verified status of the user
	*
	* @param int $newVerifiedStatus  New verified status for the user 
	*/
	public function setVerifiedStatus($newVerifiedStatus){
		$this->verified = $newVerifiedStatus;
	}
	
	/**
	* Returns the archive status of the user
	*
	* @return string  Archive status of the user
	*/
	public function isArchived(){
		return $this->archived;
	}
		
	/**
	* Returns the DFP network code of the user
	*
	* @return string  DFP network code of the user
	*/
	public function getDFPNetworkCode(){
		return $this->dfpNetworkCode;
	}
		
	//********************************* Private Accessors ***********************************
	/**
	* Sets the ID of the user
	*
	* @param int $newID  New ID for the user 
	*/
	private function setID($newID){
		$this->id = $newID;
	}

	/**
	* Sets the the archive status of the user
	*
	* @param boolean $newArchiveStatus  New archive status of the user 
	*/
	private function setArchiveStatus($newArchiveStatus){
		$this->archived = $newArchiveStatus;
	}
	
	/**
	* Sets the the DFP network code of the user
	*
	* @param string $newDFPNetworkCode  New DFP network code of the user 
	*/
	private function setDFPNetworkCode($newDFPNetworkCode){
		$this->dfpNetworkCode = $newDFPNetworkCode;
	}
	
}

//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------

