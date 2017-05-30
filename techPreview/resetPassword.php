<?PHP
/**
* Allows a user to reset their password. 
*
* @package bracket
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
$passwordResetError = "";
$verificationError = "";
$curUser = NULL;
if ((!$_GET['id']) || (!($curUser = User::getUser($_GET['id'])))) {
	$verificationError = "Unable to retrieve user.";
}
else if (!$_GET['v']) {
	$verificationError = "Unable to verify user due to missing verification code.";
}
else if ($_GET['v'] != md5($curUser->getID() . $curUser->getEmailAddress() . $curUser->getHashedPassword())) {
	$verificationError = "Verification code is incorrect. Unable to reset the password.";
}
//If the register form was submitted, attempt to register the user.
else if ($_POST['resetPasswordSubmit']) {
	
	//First, verify valid information was submitted.
	if ((strlen($_POST['newPassword1']) == 0) && (strlen($_POST['newPassword2']) == 0)) {
		$passwordResetError = "Please enter a password.<br>";
	}
	else if (strlen($_POST['newPassword1']) < 8) {
		$passwordResetError = "The password must be at least 8 characters long.<br>";
	}
	else if ($_POST['newPassword1'] != $_POST['newPassword2']) {
		$passwordResetError = "The passwords do not match. Please re-enter the passwords.";
	}
	
	//If everything passes, save the new password, logout, and sent the user to the login screen.
	else {	
		changeUserPassword($_GET['id'], $_POST['newPassword1']);

		//Start the session to modify it then destroy its stored information
		session_start();
		header("Cache-control: private"); 
		$_SESSION = array(); 
		session_destroy();

		//Send the user to the index page
		header("Location: /?resetPasswordSuccess=true");

		// header("Location: logout.php");
	}
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

<div id="mainContent" style="width: 400px;">

	<h2>Reset Password</h2>
	<div id="resetPasswordDiv" class="section" style="text-align: center;">

		<div align="center">
			<div style="display:<?PHP if (!$verificationError) {echo 'none';}?>">
				<?PHP echo $verificationError ?>
				<br><br>
				<a href="/">Click here</a> to return to the homepage.
			</div>
			
			<div style="display:<?PHP if ($verificationError) {echo 'none';}?>">
				<div align="center">

					<div id="passwordResetErrorDiv" align="center" style="padding-bottom: 5px; display:<?PHP if (!$passwordResetError) {echo 'none';}?>;">
						<?PHP echo $passwordResetError; ?>
					</div>

					<form action="resetPassword.php?<?PHP echo 'id=' . $_GET['id'] . '&v=' . $_GET['v'] ?>" id="reset" name="reset" method="POST" align="center" style="display:inline-block">
						<table cellspacing="1" cellpadding="5">
							<tr>
								<td><strong>Choose a Password:</strong></td>
								<td><input type="password" id="newPassword1" name="newPassword1" maxlength="24" value="<? if ($passwordResetError) echo $_POST['newPassword1'];?>"></td>
							</tr>
							<tr>
								<td><strong>Re-enter Password:</strong></td>
								<td><input type="password" id="newPassword2" name="newPassword2" maxlength="24" value="<? if ($passwordResetError) echo $_POST['newPassword2'];?>"></td>
							</tr>
							<tr>
								<td colspan="2" align="center">
									<input class="button-tiny indexPageButton" type="submit" name="resetPasswordSubmit"  value="Save New Password">
								</td>
							</tr>
						</table>
					</form>
				</div>
			</div>
		</div>	
	</div>
</div>
</body>
</html>