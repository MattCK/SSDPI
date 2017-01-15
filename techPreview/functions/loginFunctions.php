<?PHP
/**
* Contains the functions for logging a user into the system and setting up a session.
*
* @package bracket
* @subpackage Functions
*/
/**
* 
*/

use AdShotRunner\Utilities\EmailClient;
use AdShotRunner\Users\User;

/**
* Attempts to log a user into the bracket system with the passed username and password.
*
* Attempts to log the user with the passed username and password. On success, true is returned. On failure, NULL is returned. If successful,
* the session cookies for the user are set.
*
* @param string $username  	The username of the user to login.
* @param string $password  	The password of the user to login.
* @return boolean  		 	True on success and NULL on failure
*/
function loginUser($username, $password) {

	//Verify a username and password were passed. If not, return NULL.
	if ((!$username) || (!$password)) {return NULL;}
	
	//Encode the password using sha1 hashing and salt.
	$hashedPassword = sha1("hyph3n@t1on" . $password);
	
	//Look for the user and password in the database. If not found, not verified, or archived, return NULL.
	$curUser = User::findUser($username, $hashedPassword);
	if ((!$curUser) || (!$curUser->isVerified()) || ($curUser->isArchived())) {return NULL;}
	
	//Setup the user's cookies.
	session_start();
	header("Cache-control: private"); 
	$_SESSION['userID'] = $curUser->getID();
	$_SESSION['username'] = $curUser->getUsername();
	$_SESSION['userFirstName'] = $curUser->getFirstName();
	$_SESSION['userLastName'] = $curUser->getLastName();
	$_SESSION['userEmail'] = $curUser->getEmail();
	$_SESSION['userDFPNetworkCode'] = $curUser->getDFPNetworkCode();
	
	//Record the login time
	User::loginTimestamp($curUser->getID());
	
	//Return successful
	return TRUE;
}

/**
* Returns TRUE if the username is already taken by another user and FALSE if the username is available.
*
* @param string $username  The username to check.
* @return boolean  		   TRUE if the username is already taken and FALSE if it is available
*/
function usernameAlreadyTaken($username) {
	return (boolean) User::getUserByUsername($username);
}

/**
* Registers a new user into the system.
*
* Registers the new user into the system on success and returns the instance of the User. Returns NULL on failure.
*
* @param string $username  	The username of the user to create.
* @param string $password  	The plaint text password of the user to create. This will be hashed before database insertion.
* @param string $firstName  The first name of the user to create.
* @param string $lastName  	The last name of the user to create.
* @param string $company  	The company of the user to create.
* @param string $email  	The email of the user to create.
* @return User  		 	Instance of new User on success and NULL on failure.
*/
function registerUser($username, $password, $firstName, $lastName, $company, $email) {

	//Verify all the info was passed. If not, return NULL.
	if ((!$username) || (!$password) || (!$firstName) || (!$lastName) || (!$email)) {return NULL;}
	
	//Re-verify the username is available.
	if (usernameAlreadyTaken($username)) {return NULL;}
	
	//Encode the password using sha1 hashing and salt.
	$hashedPassword = sha1("hyph3n@t1on" . $password);
	
	//Create the new user and add the data
	$newUser = new User();
	$newUser->setUsername($_POST['email']);
	$newUser->setPassword($hashedPassword);
	$newUser->setFirstName($_POST['firstName']);
	$newUser->setLastName($_POST['lastName']);
	$newUser->setCompany($_POST['company']);
	$newUser->setEmail($_POST['email']);

	//Set the default PowerPoint background image. This should not be static here in the production version.
	$newUser->setPowerPointBackground("DefaultBackground.jpg");

	//Insert the user into the database
	$newUser = User::insert($newUser);

	//Does this do anything? Is it for versioning in the history table?a
	$curUser = User::getUser($newUser->getID());
	$curUser = User::update($curUser);
	
	//Return the new user
	return $newUser;
}

/**
* Changes a user password to the new passed in password.
*
* Sets the user's password to the new password on success and returns TRUE. Returns NULL on failure.
*
* @param string $userID  		The ID of the user to modify.
* @param string $newPassword  	The plaint text password of the user to create. This will be hashed before database insertion.
* @return boolean  		 		TRUE on success and NULL on failure.
*/
function changeUserPassword($userID, $newPassword) {

	//Verify all the info was passed. If not, return NULL.
	if ((!$userID) || (!$newPassword)) {return NULL;}
	
	//Encode the password using sha1 hashing and salt.
	$hashedPassword = sha1("hyph3n@t1on" . $newPassword);
	
	//Get the User, change its password, and update the record in the database.
	$curUser = User::getUser($userID);
	$curUser->setPassword($hashedPassword);
	$curUser = User::update($curUser);
	
	//Return success
	return TRUE;
}

/**
* Sends a user an email to verify their account/email address.
*
* Returns TRUE on success and NULL on failure.
*
* @param string $username  	The username of the user to send the validation email.
* @return Boolean  		 	TRUE on success and NULL on failure.
*/
function sendVerificationEmail($username) {

	//Verify a username was passed. If not, return NULL.
	if (!$username) {return NULL;}
	
	//Get the user. If not found, return NULL.
	if (!($curUser = User::getUserByUsername($username))) {return NULL;}
	
	//Create the verification code specific for this user.
	$verificationCode = md5('ver1f1c@t10n' . $curUser->getUsername() . $curUser->getEmail());
	
	//Create the email subject
	$emailSubject = "Welcome to the AdShotRunner Tech Preview!"; 

	//Create the email body
	$emailBody = "Thank you for registering for the AdShotRunner Tech Preview. In order to verify your account, please click the following link or copy and paste it into your browser:\n\n";
	$emailBody .= "http://adshotrunnertechbeta.elasticbeanstalk.com/verifyAccount.php?id=" . $curUser->getID() . "&v=" . $verificationCode;
	$emailBody .= "\n\nIf you believe you received this email in error, please ignore it. You will receive no future emails.";

	//Set the email addresses
	$fromEmailAddress = "donotreply@adshotrunner.com";
	$toEmailAddress = $curUser->getEmail();

	//Send the email
	EmailClient::sendEmail($fromEmailAddress, $toEmailAddress, $emailSubject, $emailBody);
	
	//Return successful.
	return true;
}

/**
* Sends a user an email to reset their password.
*
* Returns TRUE on success and NULL on failure.
*
* @param string $username  	The username of the user to send the password reset email.
* @return Boolean  		 	TRUE on success and NULL on failure.
*/
function sendPasswordResetEmail($username) {

	//Verify a username was passed. If not, return NULL.
	if (!$username) {return NULL;}
	
	//Get the user. If not found, return NULL.
	if (!($curUser = User::getUserByUsername($username))) {return NULL;}
	
	//Create the reset verification code specific for this user.
	$resetVerificationCode = md5('r3s3TP@55' . $curUser->getUsername() . $curUser->getPassword());
	
	//Create the email subject
	$emailSubject = "AdShotRunner Tech Preview: Password Reset"; 

	//Create the email body
	$emailBody = "You have requested to reset your password. In order to proceed, please click the following link or copy and paste it into your browser:\n\n";
	$emailBody .= "http://adshotrunnertechbeta.elasticbeanstalk.com/resetPassword.php?id=" . $curUser->getID() . "&v=" . $resetVerificationCode;
	$emailBody .= "\n\nIf you believe you received this email in error, please ignore it. You will receive no future emails.";

	//Set the email addresses
	$fromEmailAddress = "donotreply@adshotrunner.com";
	$toEmailAddress = $curUser->getEmail();

	//Send the email
	EmailClient::sendEmail($fromEmailAddress, $toEmailAddress, $emailSubject, $emailBody);

	//Return successful.
	return true;
}

/**
* Validates the passed email address.
*
* Returns TRUE if the passed address follows international guidelines and the domain is valid and active. On failure,
* NULL is returned.
*
* @param string $emailAddress  	The email address to validate
* @return Boolean  		 		TRUE if the email address is valid and NULL otherwise.
*/
function emailAddressIsValid($emailAddress) {
   
	//Check to make sure there is an @ symbol. If so, grab the last one. Otherwise, return NULL.
	$atSignPosition = strrpos($emailAddress, "@");
	if (is_bool($atSignPosition) && (!$atSignPosition)) {return NULL;}
   
	//Split the string up at the @ sign into local and domain parts for easier validation. Also, grab their lengths.
	$localPart = substr($emailAddress, 0, $atSignPosition);
	$domainPart = substr($emailAddress, $atSignPosition + 1);
	$localLength = strlen($localPart);
	$domainLength = strlen($domainPart);

	//Verifiy the length of the local part is between 1 and 65
	if ($localLength < 1 || $localLength > 64) {return NULL;}

	//Verifiy the length of the domain part is between 1 and 255	  
	else if ($domainLength < 1 || $domainLength > 255) {return NULL;}

	//Verify the local part does not begin or end with a period
	else if (($localPart[0] == '.') || ($localPart[$localLength - 1] == '.')) {return NULL;}

	//Verify the local part does not have two consecutive periods
	else if (preg_match('/\\.\\./', $localPart)) {return NULL;}

	//Verify the local part does not have two consecutive periods
	else if (preg_match('/\\.\\./', $domainPart)) {return NULL;}

	//Verify only valid domain characters are used in the domain part
	else if (!preg_match('/^[A-Za-z0-9\\-\\.]+$/', $domainPart)) {return NULL;}
	
	//Check that normally invalid characters in the local part are surrounded by quotation marks
	else if (!preg_match('/^(\\\\.|[A-Za-z0-9!#%&`_=\\/$\'*+?^{}|~.-])+$/', str_replace("\\\\","",$localPart))) {
		if (!preg_match('/^"(\\\\"|[^"])+"$/', str_replace("\\\\","",$localPart))) {return NULL;}
	}
	
	//Finally, if everything passed, check the DNS to verify the domain exists
	if ((!checkdnsrr($domainPart,"MX")) || (!checkdnsrr($domainPart,"A"))) {return NULL;}
	
	//Everything passed so return true
	return TRUE;
}

?>