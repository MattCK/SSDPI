<?PHP
/**
* Attempts to verify an account with the passed information. On success, the user account verified status is set to true.
*
* @package adshotrunner
* @subpackage Pages
*/
/**
* File to define paths, setup dependencies, and connect to database
*/
require_once('systemSetup.php');

/**
* Function library used to login a user
*/
require_once(FUNCTIONPATH . 'loginFunctions.php');

use AdShotRunner\Users\User;

//Verify the information passed.
$finalMessage = "";
$currentUser = NULL;
if ((!$_GET['id']) || (!($currentUser = User::getUser($_GET['id'])))) {
	$finalMessage = "Unable to retrieve user.";
}
else if (!$_GET['v']) {
	$finalMessage = "Unable to verify user due to missing verification code.";
}
else if ($_GET['v'] != md5($currentUser->getID() . $currentUser->getClientID() . $currentUser->getEmailAddress())) {
	$finalMessage = "Verification code is incorrect. Unable to verify the user.";
}

//If all the information was valid, go ahead and verify the user.
else {
	$currentUser->setVerifiedStatus(true);
	$currentUser = User::update($currentUser);
	sendActivationEmail($currentUser->getEmailAddress());
	$finalMessage = $currentUser->getFirstName() . " " . $currentUser->getLastName() . " (" . $currentUser->getEmailAddress() . ") has been activated as a user on your account and will receive a notification email shortly.";
}

?>

<?PHP include_once(BASEPATH . "header.php");?>

<body>
	
<div id="header" class="loginPage">
	<div id="title">
		<a href="/"><img id="headerLogo" src="images/headerLogo.png"/></a>
	</div>
	<div id="logout" class="loginPage">
		<a href="/contactUs.php">Contact Us</a>&nbsp;&nbsp;&nbsp;&nbsp;
	</div>
</div>

<div id="mainContent">

	<h2>Account Verification</h2>
	<div id="verificationDiv" class="section" style="text-align: center;">
		<?PHP echo $finalMessage ?>
		<br><br>
		<a href="/">Click here</a> to return to the Login Page.
	</div>

</div>

</body>
</html>