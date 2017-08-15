<?PHP
/**
* Contains the functions for logging a user into the system, verifying a user, and resetting passwords.
*
* @package Adshotrunner
* @subpackage Functions
*/

use AdShotRunner\System\ASRProperties;
use AdShotRunner\Utilities\EmailClient;
use AdShotRunner\PowerPoint\PowerPointBackground;
use AdShotRunner\Clients\Client;
use AdShotRunner\Users\User;

/**
* Attempts to log a user into the system with the passed email address and plaintext password.
*
* On success, true is returned. On failure, NULL is returned. 
* If successful, the session cookies for the user are set.
*
* @param string $emailAddress  			The email address of the user to login.
* @param string $plaintextPassword  	The plaintext password of the user to login.
* @return boolean  		 				True on success and NULL on failure
*/
function loginUser($emailAddress, $plaintextPassword) {

	//Verify an email address and password were passed. If not, return NULL.
	if ((!$emailAddress) || (!$plaintextPassword)) {return NULL;}
		
	//Look for the user in the database. If not found, not verified, or archived, return NULL.
	$foundUser = User::findUser($emailAddress, $plaintextPassword);
	if ((!$foundUser) || (!$foundUser->isVerified()) || ($foundUser->isArchived())) {return NULL;}
	
	//Get the user's company information
	$userClient = Client::getClient($foundUser->getClientID());

	//Setup the user's session variables.
	session_start();
	header("Cache-control: private"); 
	$_SESSION['userID'] = $foundUser->getID();
	$_SESSION['userEmailAddress'] = $foundUser->getEmailAddress();
	$_SESSION['userFirstName'] = $foundUser->getFirstName();
	$_SESSION['userLastName'] = $foundUser->getLastName();
	$_SESSION['userDFPNetworkCode'] = $userClient->getDFPNetworkCode();
	
	//Record the login time
	User::setLoginTimestamp($foundUser->getID());
	
	//Return successful
	return TRUE;
}

/**
* Returns TRUE if the email address is already in use by another user and FALSE if not in use.
*
* @param string $emailAddress  	The email address to check.
* @return boolean  		   		TRUE if the email address is already in use and FALSE if it is not in use
*/
function emailAddressAlreadyInUse($emailAddress) {
	return (boolean) User::getUserByEmailAddress($emailAddress);
}

/**
* Registers a new user into the system.
*
* Registers the new user into the system on success and returns the instance of the User. Returns NULL on failure.
*
* @param string $accountNumber 			The client account number for the user.
* @param string $emailAddress  			The email address of the user to create.
* @param string $plaintextPassword  	The plaintext password of the user to create. This will be hashed before database insertion.
* @param string $firstName  			The first name of the user to create.
* @param string $lastName  				The last name of the user to create.
* @return User  		 				Instance of new User on success and NULL on failure.
*/
function registerUser($accountNumber, $emailAddress, $plaintextPassword, $firstName, $lastName) {

	//Verify all the info was passed. If not, return NULL.
	if ((!$emailAddress) || (!$plaintextPassword) || (!$firstName) || (!$lastName)) {return NULL;}
	
	//Get the client information. If none exist, return NULL
	$newUserClient = Client::getClientByAccountNumber($accountNumber);
	if (!$newUserClient) {return NULL;}

	//Re-verify the email address is available.
	if (emailAddressAlreadyInUse($emailAddress)) {return NULL;}
	
	//Create the new user and add the data
	$newUser = new User();
	$newUser->setClientID($newUserClient->getID());
	$newUser->setEmailAddress($emailAddress);
	$newUser->setPassword($plaintextPassword);
	$newUser->setFirstName($firstName);
	$newUser->setLastName($lastName);

	//Insert the user into the database
	$newUser = User::insert($newUser);

	//Get the default background for the client
	$backgroundURL = "https://s3.amazonaws.com/" . ASRProperties::containerForPowerPointBackgrounds() . 
					 "/clientDefaults/" . $newUserClient->getPowerPointBackground();
	$temporaryFile = RESTRICTEDPATH . 'temporaryFiles/' . time() . "-" . $newUserClient->getPowerPointBackground();;
	file_put_contents($temporaryFile, fopen($backgroundURL, 'r'));

	//Create the PowerPoint background for the user
	$newBackground = PowerPointBackground::create("Default", 
												  $newUserClient->getPowerPointFontColor(), 
												  $newUserClient->getPowerPointBackground(), 
												  $temporaryFile, 
												  $newUser->getID());
	$newUser->setPowerPointBackgroundID($newBackground->id());
	$newUser = User::update($newUser);

	//Delete the background temporary file
	unlink($temporaryFile);
	
	//Return the new user
	return $newUser;
}

/**
* Changes a user password to the new passed password.
*
* On success and returns TRUE. Returns NULL on failure.
*
* @param string $userID  		The ID of the user to modify.
* @param string $newPassword  	The plaintext password for the user. This will be hashed before database insertion.
* @return boolean  		 		TRUE on success and NULL on failure.
*/
function changeUserPassword($userID, $newPassword) {

	//Verify all the info was passed. If not, return NULL.
	if ((!$userID) || (!$newPassword)) {return NULL;}
	
	//Get the User. On failure, return NULL.
	$userToModify = User::getUser($userID);
	if (!$userToModify) {return NULL;}

	//Change the user's password and update the record in the database.
	$userToModify->setPassword($newPassword);
	$userToModify = User::update($userToModify);
	
	//Return success
	return TRUE;
}

/**
* Sends a new user a welcome email and send the client's administrator an email to verify the new user's account.
*
* Returns TRUE on success and NULL on failure.
*
* @param string 	$userEmailAddress  	The email address of the new user
* @return Boolean  		 				TRUE on success and NULL on failure.
*/
function sendWelcomeAndVerificationEmails($userEmailAddress) {

	//Verify a user email address was passed. If not, return NULL.
	if (!$userEmailAddress) {return NULL;}
	
	//Get the user. If not found, return NULL.
	if (!($newUser = User::getUserByEmailAddress($userEmailAddress))) {return NULL;}

	//Get the user's client
	$newUserClient = Client::getClient($newUser->getClientID());

	//---------------------------------- Welcome Email ----------------------------------

	//Create the welcome email subject
	$welcomeEmailSubject = "AdShotRunner: Thank You for Registering!"; 

	//Create the welcome email body
	$welcomeEmailBody = $newUser->getFirstName() . " " . $newUser->getLastName() . ",\n\n";
	$welcomeEmailBody .= "Thank you for registering with AdShotRunner. An email has been sent to your Company Administrator to request access. In the meantime, find more information at: https://www.adshotrunner.com/ \n\n";
	$welcomeEmailBody .= "We look forward to having you on board!\n\n";
	$welcomeEmailBody .= "If you believe you received this email in error, you may ignore it. You will receive no future emails.";

	//Set the email addresses
	$fromEmailAddress = ASRProperties::emailAddressDoNotReply();
	$toEmailAddress = $newUser->getEmailAddress();

	//Send the welcome email
	EmailClient::sendEmail($fromEmailAddress, $toEmailAddress, $welcomeEmailSubject, $welcomeEmailBody);

	//---------------------------------- Verification Email ----------------------------------

	//Create the verification code specific for this user.
	$verificationCode = md5($newUser->getID() . $newUser->getClientID() . $newUser->getEmailAddress());

	//Create the subject line for the verification email
	$verificationSubject = "AdShotRunner: New User Request (" . $newUser->getFirstName() . " " . $newUser->getLastName() . ")";
	
	//Create the verification email body
	$verificationBody = "The following new user has requested access to your AdShotRunner account: \n\n";
	$verificationBody .= "Name: " . $newUser->getFirstName() . " " . $newUser->getLastName() . "\n";
	$verificationBody .= "Email Address: " . $newUser->getEmailAddress() . "\n\n";
	$verificationBody .= "If you would like to give this user access to your account, please click the link below or paste it into your browser: \n\n";
	$verificationBody .= "http://" . ASRProperties::asrDomain() . "/verifyAccount.php?id=" . $newUser->getID() . "&v=" . $verificationCode;
	$clientVerifyAddress = $newUserClient->getVerifyUserEmailAddress();
	EmailClient::sendEmail($fromEmailAddress, $clientVerifyAddress, $verificationSubject, $verificationBody);

	//Return successful.
	return true;
}

/**
* Sends a user an email to reset their password.
*
* Returns TRUE on success and NULL on failure.
*
* @param string $emailAddress  	The email address of the user to send the password reset email.
* @return Boolean  		 		TRUE on success and NULL on failure.
*/
function sendPasswordResetEmail($emailAddress) {

	//Verify an email address was passed. If not, return NULL.
	if (!$emailAddress) {return NULL;}
	
	//Get the user. If not found, return NULL.
	if (!($currentUser = User::getUserByEmailAddress($emailAddress))) {return NULL;}
	
	//Create the reset verification code specific for this user.
	$resetVerificationCode = md5($currentUser->getID() . $currentUser->getEmailAddress() . $currentUser->getHashedPassword());
	
	//Create the email subject
	$emailSubject = "AdShotRunner: Password Reset"; 

	//Create the email body
	$emailBody = "You have requested to reset your password. In order to proceed, please click the following link or copy and paste it into your browser:\n\n";
	$emailBody .= "http://" . ASRProperties::asrDomain() . "/resetPassword.php?id=" . $currentUser->getID() . "&v=" . $resetVerificationCode;
	$emailBody .= "\n\nIf you believe you received this email in error, you may ignore it. You will receive no future emails.";

	//Set the email addresses
	$fromEmailAddress = ASRProperties::emailAddressDoNotReply();
	$toEmailAddress = $currentUser->getEmailAddress();

	//Send the email
	EmailClient::sendEmail($fromEmailAddress, $toEmailAddress, $emailSubject, $emailBody);

	//Return successful.
	return true;
}

/**
* Sends a user an email notifying them their account has been activated by their company administrator.
*
* Returns TRUE on success and NULL on failure.
*
* @param string $emailAddress  	The email address of the user to send the activation email
* @return Boolean  		 		TRUE on success and NULL on failure.
*/
function sendActivationEmail($emailAddress) {

	//Verify an email address was passed. If not, return NULL.
	if (!$emailAddress) {return NULL;}
	
	//Get the user. If not found, return NULL.
	if (!($currentUser = User::getUserByEmailAddress($emailAddress))) {return NULL;}
		
	//Create the email subject
	$emailSubject = "AdShotRunner: Access Granted!"; 

	//Create the email body
	$emailBody = "Your company administrator has granted you access to AdShotRunner! Go here to login: ";
	$emailBody .= "http://" . ASRProperties::asrDomain();
	$emailBody .= "\n\nWelcome aboard!";

	//Set the email addresses
	$fromEmailAddress = ASRProperties::emailAddressDoNotReply();
	$toEmailAddress = $currentUser->getEmailAddress();

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