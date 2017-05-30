<?PHP
/**
* Index page of AdShotRunner system. Contains main interface for logging a user into the system or registering a new user.
*
* @package AdShotRunner
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

use AdShotRunner\System\ASRProperties;
use AdShotRunner\Clients\Client;


//If the login form was submitted, attempt to login using those credentials.
$loginError = false;
$registrationError = "";
$passwordResetError = "";
if ($_POST['loginSubmit']) {

	//Attempt to login the user using the supplied email address and password
	$successfullyLoggedIn = loginUser($_POST['emailAddress'], $_POST['password']);
	
	//If the user logged in successfully, go to the main app page.
	if ($successfullyLoggedIn) {
		header("Location: mainApp.php");
		exit;
	}
	
	//Otherwise, display an error message to the user.
	else {
		$loginError = true;
	}
}

//If the register form was submitted, attempt to register the user.
else if ($_POST['registerSubmit']) {
	
	//Return to this tab on refresh
	$openTab = 1;
	
	//First, verify valid information was submitted.
	if (strlen($_POST['accountNumber']) == 0) {
		$registrationError = "Please enter the account number for your company.";
	}
	else if (!emailAddressIsValid($_POST['emailAddress'])) {
		$registrationError = "Please enter a valid email address.";
	}
	else if ((strlen($_POST['firstName']) == 0) || (strlen($_POST['lastName']) == 0)) {
		$registrationError = "Please enter a First and Last Name.";
	}
	else if ((strlen($_POST['newPassword1']) == 0) && (strlen($_POST['newPassword2']) == 0)) {
		$registrationError = "Please enter a password.";
	}
	else if (strlen($_POST['newPassword1']) < 8) {
		$registrationError = "The password must be at least 8 characters long.";
	}
	else if ($_POST['newPassword1'] != $_POST['newPassword2']) {
		$registrationError = "The passwords do not match. Please re-enter the passwords.";
	}
	
	//If the info was successfully validated, check to see if the email address is already taken.
	else if (emailAddressAlreadyInUse($_POST['emailAddress'])) {
		$registrationError = "The email address provided has already been registered.<br>Please enter another.";
	}

	//If the info was successfully validated, verify the account exists.
	else if (!Client::getClientByAccountNumber($_POST['accountNumber'])) {
		$registrationError = "No account matches the provided account number.";
	}
	
	//If everything passes, add the user and send a validation email.
	else {
	
		//Insert the user into the system
		if (registerUser($_POST['accountNumber'], $_POST['emailAddress'], $_POST['newPassword1'], $_POST['firstName'], $_POST['lastName'])) {
		
			//If the registration was successful, send the welcome and verification emails
			sendWelcomeAndVerificationEmails($_POST['emailAddress']);
			
			$popupTitle = "Registration Successful";
			$popupMessage = "<span style=''>Thank you for signing up!<br><br> An email has been sent to your company administrator to activate your account.<br><br>In the meantime, you can find more about us on our website: <a href='https://www.adshotrunner.com/'>www.adshotrunner.com</a></span>";
		
			//Really dirty but quick was to empty fields
			$_POST = array();
		}
	}
}

//If the reset password form was submitted, attempt to send a password reset email.
else if ($_POST['resetPasswordSubmit']) {
	
	//Return to this tab on refresh
	$openTab = 2;
	
	if (strlen($_POST['resetEmailAddress']) == 0) {
		$passwordResetError = "Please enter your email address.";
	}

	else if (!emailAddressAlreadyInUse($_POST['resetEmailAddress'])) {
		$popupTitle = "Password Reset error";
		$popupMessage = "<span style=''>There is no user associated with the provided email address: " . $_POST['resetEmailAddress'] . "</span>";

	}
	
	//If everything passes, send a password reset email.
	else {
		sendPasswordResetEmail($_POST['resetEmailAddress']);

		$popupTitle = "Password Reset";
		$popupMessage = "<span style=''>An email to reset your password has been sent to you. Please check your inbox.</span>";
	}
}

//If a password reset was successful, notify the user
else if ($_GET['resetPasswordSuccess']) {
	$popupTitle = "Password Changed";
	$popupMessage = "<span style=''>Your password has been successfully changed. Please use your new password to login to your account.</span>";

}	


//If nothing was passed, check to see if the user is already logged in. If so, send them directly to the main app page.
else {
	session_start();
	header("Cache-control: private"); 
	if ($_SESSION['userID']){
		header("Location: mainApp.php");
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



<div id="mainContent" style="width:400px">

	<h2>Login</h2>
	<div id="loginDiv" class="section">
		<div align="center">

			<div id="loginErrorDiv" align="center" style="color: FireBrick; padding-bottom: 10px; display:<?PHP if (!$loginError) {echo 'none';}?>;">
				The email and password do not match. <br>Please re-enter your email and password.
			</div>
			
			<form action="index.php" id="login" name="login" method="POST" align="center" style="display:inline-block">
				<table class="indexPageTable" cellspacing="1" cellpadding="5" style="text-align:left">
					<tr>
						<td><strong>Email:</strong></td>
						<td><input type="text" name="emailAddress" id="emailAddress" maxlength="64"></td>
					</tr>
					<tr>
						<td><strong>Password:</strong></td>
						<td><input type="password" id="password" name="password" maxlength="24"></td>
					</tr>
					<tr>
						<td colspan="2" align="center">
							<input class="button-tiny indexPageButton" type="submit" name="loginSubmit" value="Login">
						</td>
					</tr>
				</table>
			</form>
		</div>
	</div>

	<h2>Register as a User with Your Company</h2>
	<div id="registerDiv" class="section">
		<div align="center">

			<div id="registrationErrorDiv" align="center" style="color: FireBrick; padding-bottom: 10px; display:<?PHP if (!$registrationError) {echo 'none';}?>;">
				<?PHP echo $registrationError; ?>
			</div>

			<form action="index.php" id="register" name="register" method="POST" align="center" style="display:inline-block">
				<table class="indexPageTable" cellspacing="1" cellpadding="5" style="text-align:left">
					<tr>
						<td><strong>Account Number:</strong></td>
						<td><input type="text" name="accountNumber" id="accountNumber" maxlength="24" value="<? if ($registrationError) echo $_POST['accountNumber'];?>">
							<img helpIcon="" id="accountNumberHelpIcon" class="helpIcon titleHelpIcon" src="images/helpIcon.png" />
						</td>
					</tr>
					<tr>
						<td><strong>Email:</strong></td>
						<td><input type="text" name="emailAddress" id="emailAddress" maxlength="64" value="<? if ($registrationError) echo $_POST['emailAddress'];?>"></td>
					</tr>
					<tr>
						<td><strong>First Name:</strong></td>
						<td><input type="text" name="firstName" id="firstName" maxlength="24" value="<? if ($registrationError) echo $_POST['firstName'];?>"></td>
					</tr>
					<tr>
						<td><strong>Last Name:</strong></td>
						<td><input type="text" name="lastName" id="lastName" maxlength="24" value="<? if ($registrationError) echo $_POST['lastName'];?>"></td>
					</tr>
					<tr>
						<td><strong>Choose a Password:</strong></td>
						<td><input type="password" id="newPassword1" name="newPassword1" maxlength="24" value="<? if ($registrationError) echo $_POST['newPassword1'];?>"></td>
					</tr>
					<tr>
						<td><strong>Re-enter Password:</strong></td>
						<td><input type="password" id="newPassword2" name="newPassword2" maxlength="24" value="<? if ($registrationError) echo $_POST['newPassword2'];?>"></td>
					</tr>
					<tr>
						<td colspan="2" align="center">
							<input class="button-tiny indexPageButton" type="submit" name="registerSubmit"  value="Sign Up">
						</td>
					</tr>
				</table>
			</form>
		</div>
	</div>

	<h2>Reset Password</h2>
	<div id="resetPasswordDiv" class="section">
		<div align="center">

			<div id="passwordResetErrorDiv" align="center" style="color: FireBrick; padding-bottom: 10px; display:<?PHP if (!$passwordResetError) {echo 'none';}?>;">
				<?PHP echo $passwordResetError; ?>
			</div>	
			
			<form action="index.php" id="resetPassword" name="resetPassword" method="POST" align="center" style="display:inline-block">
				<table class="indexPageTable" cellspacing="1" cellpadding="5" style="text-align:left">
					<tr>
						<td><strong>Email:</strong></td>
						<td><input type="text" name="resetEmailAddress" id="resetEmailAddress" maxlength="64"></td>
					</tr>
					<tr>
						<td colspan="2" align="center" style="margin-top: 25px">
							<input class="button-tiny indexPageButton" type="submit" name="resetPasswordSubmit" value="Reset Password">
						</td>
					</tr>
				</table>
			</form>
		</div>
	</div>
</div>

<script>

	<?PHP if ($popupMessage): ?>

	base.onReady(function() {
		let dialogMessage = "<?PHP echo $popupMessage ?>";
		let dialogTitle = "<?PHP echo $popupTitle ?>";
		base.showMessage(dialogMessage, dialogTitle);
	});

	<?PHP endif; ?>

</script>

</body>
</html>