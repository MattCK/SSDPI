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


//Set the tab to open at the beginning
$openTab = 0;

//If the login form was submitted, attempt to login using those credentials.
$loginError = false;
$registrationError = "";
$passwordResetError = "";
if ($_POST['loginSubmit']) {

	//Attempt to login the user using the supplied username and password
	$successfullyLoggedIn = loginUser($_POST['username'], $_POST['password']);
	
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
	if ((strlen($_POST['firstName']) == 0) || (strlen($_POST['lastName']) == 0)) {
		$registrationError = "Please enter a First and Last Name.";
	}
	/*else if (strlen($_POST['desiredLogin']) == 0) {
		$registrationError = "Please enter a desired Login Name.";
	}*/
	else if (strlen($_POST['company']) == 0) {
		$registrationError = "Please enter your company name.";
	}
	else if (strlen($_POST['email']) == 0) {
		$registrationError = "Please enter an email address.";
	}
	else if (!emailAddressIsValid($_POST['email'])) {
		$registrationError = "Please enter a valid email address.";
	}
	else if ((strlen($_POST['newPassword1']) == 0) && (strlen($_POST['newPassword2']) == 0)) {
		$registrationError = "Please enter a password.";
	}
	else if ($_POST['newPassword1'] != $_POST['newPassword2']) {
		$registrationError = "The passwords do not match. Please re-enter the passwords.";
	}
	
	//If the info was successfully validated, check to see if the username is already taken.
	else if (usernameAlreadyTaken($_POST['email'])) {
		$registrationError = "The email address has already been registered. Please enter another.";
	}
	
	//If everything passes, add the user and send a validation email.
	else {
	
		//Insert the user into the system
		if (registerUser($_POST['email'], $_POST['newPassword1'], $_POST['firstName'], $_POST['lastName'], $_POST['company'], $_POST['email'])) {
		
			//If the registration was successful, send an email to verify the account
			sendVerificationEmail($_POST['email']);
			
			$registrationError = "<span style='color:blue'>A verification email has been sent to you. Please check your mail and follow the instructions.</span>";
		
			//Really dirty but quick was to empty fields
			$_POST = array();
		}
	}
}

//If the reset password form was submitted, attempt to send a password reset email.
else if ($_POST['resetPasswordSubmit']) {
	
	//Return to this tab on refresh
	$openTab = 2;
	
	if (strlen($_POST['resetUsername']) == 0) {
		$passwordResetError = "Please enter an email.";
	}
	
	//If everything passes, send a password reset email.
	else {
		sendPasswordResetEmail($_POST['resetUsername']);
		$passwordResetError = "<span style='color:blue'>An email to reset your password has been sent. Please check your mail and follow the instructions.</span>";
	}
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

<script>
$(function() {
	$( "#tabsDiv" ).tabs({active: <?PHP echo $openTab ?>});
	
	$( "#explanationDiv" ).dialog({
		autoOpen: false,
		modal: true,
		width: 500
	});
});
</script>

<div id="header">
	<div id="title">
		<h1>AdShotRunner&trade;: Free Tech Preview</h1> 
	</div>
	<div id="logout">
		<a href="mailto:contact@dangerouspenguins.com">Contact Us</a>&nbsp;&nbsp;&nbsp;&nbsp;
	</div>
</div>


<body>

<div id="mainContent">

	<div align="center">
		
		<div id="tabsDiv" align="center" style="padding-top: 20px">
			 <ul>
				<li><a href="#tabs-1">Login</a></li>
				<li><a href="#tabs-2">Register</a></li>
				<li><a href="#tabs-3">Reset Password</a></li>
			</ul>
			<div id="tabs-1" align="center">
				<div align="center">

					<div id="loginErrorDiv" align="center" style="padding-bottom: 10px; display:<?PHP if (!$loginError) {echo 'none';}?>;">
						The email and password do not match. <br>Please re-enter your email and password.
					</div>
					
					<form action="index.php" id="login" name="login" method="POST" align="center" style="display:inline-block">
						<table class="indexPageTable" cellspacing="1" cellpadding="5" style="text-align:left">
							<tr>
								<td><strong>Email:</strong></td>
								<td><input type="text" name="username" id="username" maxlength="64"></td>
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
			
			<div id="tabs-2">
				<div align="center">

					<div id="registrationErrorDiv" align="center" style="padding-bottom: 10px; display:<?PHP if (!$registrationError) {echo 'none';}?>;">
						<?PHP echo $registrationError; ?>
					</div>

					<form action="index.php" id="register" name="register" method="POST" align="center" style="display:inline-block">
						<table class="indexPageTable" cellspacing="1" cellpadding="5" style="text-align:left">
							<tr>
								<td><strong>Email:</strong></td>
								<td><input type="text" name="email" id="email" maxlength="64" value="<? if ($registrationError) echo $_POST['email'];?>"></td>
							</tr>
							<tr>
								<td><strong>First Name:</strong></td>
								<td><input type="text" name="firstName" id="firstName" maxlength="24" value="<? if ($registrationError) echo $_POST['firstName'];?>">*</td>
							</tr>
							<tr>
								<td><strong>Last Name:</strong></td>
								<td><input type="text" name="lastName" id="lastName" maxlength="24" value="<? if ($registrationError) echo $_POST['lastName'];?>">*</td>
							</tr>
							<tr>
								<td><strong>Company:</strong></td>
								<td><input type="text" name="company" id="company" maxlength="24" value="<? if ($registrationError) echo $_POST['company'];?>">*</td>
							</tr>
							<!--tr>
								<td><strong>Desired Login Name:</strong></td>
								<td><input type="text" name="desiredLogin" id="desiredLogin" maxlength="16" value="<? if ($registrationError) echo $_POST['desiredLogin'];?>"></td>
							</tr-->
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
									<input class="button-tiny indexPageButton" type="submit" name="registerSubmit"  value="Register">
								</td>
							</tr>
						</table>
					</form>
				</div>
			</div>
			
			<div id="tabs-3">
				<div align="center">
					
					<div id="passwordResetErrorDiv" align="center" style="padding-bottom: 10px; display:<?PHP if (!$passwordResetError) {echo 'none';}?>;">
						<?PHP echo $passwordResetError; ?>
					</div>	
					
					<form action="index.php" id="resetPassword" name="resetPassword" method="POST" align="center" style="display:inline-block">
						<table class="indexPageTable" cellspacing="1" cellpadding="5" style="text-align:left">
							<tr>
								<td><strong>Email:</strong></td>
								<td><input type="text" name="resetUsername" id="resetUsername" maxlength="64"></td>
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
	</div>
</div>
</body>
</html>