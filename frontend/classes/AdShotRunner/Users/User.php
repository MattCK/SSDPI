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
* The User class allows for inserting, modifying, archiving, and retrieving users in the database. 
*
* This class controls the encryption of user passwords. When it receives a plaintext password,
* it hashes it using the PHP Hashing API. Thus, only plaintext passwords should be passed to it. 
*/
class User {

	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Methods ------------------------------------
	//---------------------------------------------------------------------------------------	
	/**
	* Returns an instance of the user with the passed ID. On failure, NULL is returned.
	*
	* In order to update a user in the system, an instance of the user must first be retrieved through this method.
	*
	* @param int 	$userID  	ID of the user to retrieve
	* @return User  			Instance of the user with the matching ID. NULL on failure.
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
		
		//Create an instance of the User and put the retrieved info into it
		$retrievedUser = new User();
		$retrievedUser->setID($userID);
		$retrievedUser->setClientID($userInfo['USR_CLN_id']);
		$retrievedUser->setEmailAddress($userInfo['USR_emailAddress']);
		$retrievedUser->setHashedPassword($userInfo['USR_hashedPassword']);
		$retrievedUser->setFirstName($userInfo['USR_firstName']);
		$retrievedUser->setLastName($userInfo['USR_lastName']);
		$retrievedUser->setPowerPointBackgroundID($userInfo['USR_PPB_id']);
		$retrievedUser->setVerifiedStatus($userInfo['USR_verified']);
		$retrievedUser->setArchiveStatus($userInfo['USR_archived']);
		
		//Return the user
		return $retrievedUser;
	}

	/**
	* Returns an instance of the user with the passed email address. On failure, NULL is returned.
	*
	* @param string $emailAddress  	Username of the user to retrieve
	* @return User  				Instance of the user with the matching ID. NULL on failure.
	*/
	static public function getUserByEmailAddress($emailAddress) {
		
		//Verify an email address was passed. If not, return NULL.
		if (!$emailAddress) {return NULL;}
		
		//Get the info from the users table
		$getUserQuery = "SELECT * FROM users WHERE USR_emailAddress LIKE '" . ASRDatabase::escape($emailAddress) . "'";
		$userResult = ASRDatabase::executeQuery($getUserQuery);
		$userInfo = $userResult->fetch_assoc();

		//If no data was returned, return NULL.
		if (!$userInfo) {return NULL;}
		
		//Return the user
		return User::getUser($userInfo['USR_id']);
	}

	/**
	* Attempts to find the user with the passed email address and plaintext password.
	*
	* On success, returns an instance of the matching user. On failure, NULL is returned. If the user matching the 
	* has been archived, failure will occur.
	*
	* The password must be the user's plaintext password. This function will automatically check it against
	* the hashed version in the database using the PHP Hashing API.
	*
	* @param string $emailAddress  			The email address of the user to find.
	* @param string $plaintextPassword  	The plaintext password of the user to find.
	* @return User  						Instance of the user matching the email address and password. NULL on failure.
	*/
	static public function findUser($emailAddress, $plaintextPassword) {
		
		//Verify an email address and password were passed. If not, return NULL.
		if ((!$emailAddress) || (!$plaintextPassword)) {return NULL;}
		
		//Get the user with the matching email address. If none was found, return NULL.
		$matchingUser = User::getUserByEmailAddress($emailAddress);
		if (!$matchingUser) {return NULL;}

		//Compare the plaintext password of the user with the stored hash. 
		//If they match, return the User, otherwise return NULL.
		if (password_verify($plaintextPassword, $matchingUser->getHashedPassword())) {return $matchingUser;}
		else {return NULL;}
	}

	/**
	* Inserts the instance of a User into the users table as a new user.
	*
	* On success, the User object is returned with its new ID set. On failure,
	* NULL is returned. 
	*
	* Since the table has a unique index on email addresses, this function
	* will return NULL if a user with the same email address already exists
	*
	* @param User 	$newUser  	User to insert into the database
	* @return User  			Instance of the user passed with its new ID set. NULL on failure.
	*/
	static public function insert(User $newUser) {
		
		//Verify a User was passed. If not, return NULL.
		if (!$newUser) {return NULL;}

		//If a user with the same email address already exists, return NULL
		if (User::getUserByEmailAddress($newUser->getEmailAddress())) {return NULL;}
		
		//Add the info to the users table
		$addUserQuery = "INSERT INTO users (USR_CLN_id,
											USR_emailAddress,
											USR_hashedPassword,
											USR_firstName,
											USR_lastName,
											USR_PPB_id,
											USR_verified)
						 VALUES ('" . ASRDatabase::escape($newUser->getClientID()) . "',
								'" .  ASRDatabase::escape($newUser->getEmailAddress()) . "',
								'" .  ASRDatabase::escape($newUser->getHashedPassword()) . "',
								'" .  ASRDatabase::escape($newUser->getFirstName()) . "',
								'" .  ASRDatabase::escape($newUser->getLastName()) . "',
								'" .  ASRDatabase::escape($newUser->getPowerPointBackgroundID()) . "',
								'" .  ASRDatabase::escape($newUser->isVerified()) . "')";
		ASRDatabase::executeQuery($addUserQuery);
		
		//Set the new ID of the User
		$newUser->setID(ASRDatabase::lastInsertID());

		//Return the newly inserted user
		return $newUser;

	}

	/**
	* Updates the information of a user in the database. 
	*
	* On success, the passed User object is returned. On failure, NULL is returned. 
	*
	* The User instance passed to this method must first be retrieved using the User::getUser(...)
	* method.
	*
	* @param User 	$modifiedUser  	User to update in the table
	* @return User 					Instance of the user passed. NULL on failure.
	*/
	static public function update(User $modifiedUser) {
		
		//Verify a User was passed. If not, return NULL.
		if (!$modifiedUser) {return NULL;}
		
		//Verify the User has an ID and it's not less than 1. If not, return NULL.
		$userID = $modifiedUser->getID();
		if ((!$userID) || (!is_numeric($userID)) || ($userID < 1)) {return NULL;}
		
		//Update the info in the table
		$updateUserQuery = "UPDATE users 
							SET USR_CLN_id = '" . ASRDatabase::escape($modifiedUser->getClientID()) . "',
								USR_emailAddress = '" . ASRDatabase::escape($modifiedUser->getEmailAddress()) . "',
								USR_hashedPassword = '" . ASRDatabase::escape($modifiedUser->getHashedPassword()) . "',
								USR_firstName = '" . ASRDatabase::escape($modifiedUser->getFirstName()) . "',
								USR_lastName = '" . ASRDatabase::escape($modifiedUser->getLastName()) . "',
								USR_PPB_id = '" . ASRDatabase::escape($modifiedUser->getPowerPointBackgroundID()) . "',
								USR_verified = '" . ASRDatabase::escape($modifiedUser->isVerified()) . "'
							WHERE USR_id = " . $modifiedUser->getID();
		ASRDatabase::executeQuery($updateUserQuery);
		
		//Return the passed user
		return $modifiedUser;
	}

	/**
	* Archives the User in the system with the passed ID.
	*
	* Archives the User in the users table that corresponds to the user ID. 
	* On success, the User object is returned. On failure, NULL is returned. 
	*
	* @param int 	$userID  	ID of the user to archive
	* @return User  			Instance of the user archived
	*/
	static public function archive($userID) {
		
		//Verify and ID was passed and it's not less than 1. If not, return NULL.
		if ((!$userID) || (!is_numeric($userID)) || ($userID < 1)) {return NULL;}
		
		//Verify user in the database is not archived. If it is, do nothing and return the User.
		$userToArchive = User::getUser($userID);
		if ($userToArchive->isArchived()) {return $userToArchive;}
				
		//Set the archive status of the user in the database
		$archiveUserQuery = "UPDATE users 
							 SET USR_archived = 1,
							 WHERE USR_id = " . $userID;
		ASRDatabase::executeQuery($archiveUserQuery);
		
		//Send the archived user
		return $userToArchive;
	}	
	
	/**
	* Sets the login timestamp to the current time for the passed user ID.
	*
	* @param int 		$userID  	ID of the user who is logging in
	* @return boolean  				TRUE on success. NULL on failure.
	*/
	static public function setLoginTimestamp($userID) {
		
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
	* @var int  Client ID of the user's company
	*/
	private $clientID;

	/**
	* @var string  Email address of the user
	*/
	private $emailAddress;

	/**
	* @var string  Hashed password of the user
	*/
	private $hashedPassword;

	/**
	* @var string  First name of the user
	*/
	private $firstName;

	/**
	* @var string  Last name of the user
	*/
	private $lastName;

	/**
	* @var int  ID of the background image to use in Powerpoints (in powerPointBackgrounds table)
	*/
	private $powerPointBackgroundID;

	/**
	* @var boolean  Flags whether or not the user has been verified. TRUE if it has been verified.
	*/
	private $verified;

	/**
	* @var boolean  Flags whether or not the user has been archived. TRUE if it has been archived.
	*/
	private $archived;


	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
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
	public function getID() {
		return $this->id;
	}
	
	/**
	* Returns the client ID of the user
	*
	* @return int	client ID of the user
	*/
	public function getClientID(){
		return $this->clientID;
	}
	/**
	* Sets the client ID of the user
	*
	* @param int 	$newClientID  New client ID of the user 
	*/
	public function setClientID($newClientID){
		$this->clientID = $newClientID;
	}

	/**
	* Returns the email address of the user
	*
	* @return string  Email address of the user
	*/
	public function getEmailAddress() {
		return $this->emailAddress;
	}
	/**
	* Sets the email address of the user
	*
	* @param string $newEmailAddress  New email address for the user 
	*/
	public function setEmailAddress($newEmailAddress) {
		$this->emailAddress = $newEmailAddress;
	}
	
	/**
	* Sets the password of the user. The password must be plaintext.
	* This function hashes it using the PHP Hashing API.
	*
	* @param string $newPlaintextPassword  New plaintext password for the user 
	*/
	public function setPassword($newPlaintextPassword) {
		$this->hashedPassword = password_hash($newPlaintextPassword, PASSWORD_DEFAULT);
	}
	
	/**
	* Returns the first name of the user
	*
	* @return string  First name of the user
	*/
	public function getFirstName() {
		return $this->firstName;
	}
	/**
	* Sets the first name of the user
	*
	* @param string $newFirstName  New first name for the user 
	*/
	public function setFirstName($newFirstName) {
		$this->firstName = $newFirstName;
	}
	
	/**
	* Returns the last name of the user
	*
	* @return string  Last name of the user
	*/
	public function getLastName() {
		return $this->lastName;
	}
	/**
	* Sets the last name of the user
	*
	* @param string $newLastName  New last name for the user 
	*/
	public function setLastName($newLastName) {
		$this->lastName = $newLastName;
	}
	
	/**
	* Returns the ID of the PowerPoint background image (in powerPointBackgrounds table)
	*
	* @return string  ID of the PowerPoint background image (in powerPointBackgrounds table)
	*/
	public function getPowerPointBackgroundID() {
		return $this->powerPointBackgroundID;
	}
	/**
	* Sets the ID of the PowerPoint background image (in powerPointBackgrounds table)
	*
	* @param string $newPowerPointBackground  New ID of the PowerPoint background image (in powerPointBackgrounds table)
	*/
	public function setPowerPointBackgroundID($newPowerPointBackgroundID) {
		$this->powerPointBackgroundID = $newPowerPointBackgroundID;
	}
	
	/**
	* Returns the whether or not the user has been verified.
	*
	* @return int  Verified status of the user.
	*/
	public function isVerified() {
		return $this->verified;
	}
	/**
	* Sets the verified status of the user
	*
	* @param int $newVerifiedStatus  New verified status for the user 
	*/
	public function setVerifiedStatus($newVerifiedStatus) {
		$this->verified = $newVerifiedStatus;
	}
	
	/**
	* Returns the archive status of the user
	*
	* @return string  Archive status of the user
	*/
	public function isArchived() {
		return $this->archived;
	}
		
	//********************************* Private Accessors ***********************************
	/**
	* Sets the ID of the user
	*
	* @param int $newID  New ID for the user 
	*/
	private function setID($newID) {
		$this->id = $newID;
	}

	/**
	* Returns the hashed password of the user
	*
	* @return string  Hashed password of the user
	*/
	public function getHashedPassword() {
		return $this->hashedPassword;
	}

	/**
	* Sets the hashed password of the user. 
	*
	* This function is used by the factory to store the hashed password
	* from the database. The function setPassword(...) hashes a plaintext
	* password passed and should only be used with plaintext passwords.
	*
	* @param string $newHashedPassword  New hashed password for the user 
	*/
	private function setHashedPassword($newHashedPassword) {
		$this->hashedPassword = $newHashedPassword;
	}

	/**
	* Sets the the archive status of the user
	*
	* @param boolean $newArchiveStatus  New archive status of the user 
	*/
	private function setArchiveStatus($newArchiveStatus) {
		$this->archived = $newArchiveStatus;
	}	
}

//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------

