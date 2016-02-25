<?PHP
/**
* Attempts to verify an account with the passed information. On success, the user account verified status is set to true.
*
* @package adshotrunner
* @subpackage Pages
*/
/**
* Define paths to use throughout the system
*/
require_once('pathDefinitions.php');

/**
* Load AWS classes
*/
require_once(THIRDPARTYPATH . 'aws/aws-autoloader.php');

/**
* Load AdShotRunner classes
*/
require_once(CLASSPATH . 'adShotRunnerAutoloader.php');

/**
* Connect to the database
*/
require_once(RESTRICTEDPATH . 'databaseSetup.php');

/**
* Function library used to login a user
*/
require_once(FUNCTIONPATH . 'loginFunctions.php');

use AdShotRunner\Users\User;

//Verify the information passed.
$finalMessage = "";
$curUser = NULL;
if ((!$_GET['id']) || (!($curUser = User::getUser($_GET['id'])))) {
	$finalMessage = "Unable to retrieve user.";
}
else if (!$_GET['v']) {
	$finalMessage = "Unable to verify user due to missing verification code.";
}
else if ($_GET['v'] != md5('ver1f1c@t10n' . $curUser->getUsername() . $curUser->getEmail())) {
	$finalMessage = "Verification code is incorrect. Unable to verify the user.";
}

//If all the information was valid, go ahead and verify the user.
else {
	$curUser->setVerifiedStatus(true);
	$curUser = User::update($curUser);
	$finalMessage = "Thank you for verifying your account! You can now log into the AdShotRunner Tech Preview!";
}

?>

<?PHP include_once(BASEPATH . "header.php");?>

<body>
	<div align="center" style="padding-top: 50px">
		<?PHP echo $finalMessage ?>
		<br><br>
		<a href="/">Click here</a> to return to the homepage.
	</div>
</body>
</html>