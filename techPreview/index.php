<?PHP
/**
* Primary index page of the bracket system. Contains main interface for logging a user into the system or registering a new user.
*
* @package bracket
* @subpackage Pages
*/
/**
* File to define all the system paths and the tournament data
*/
require_once('systemDefininitions.php');

/**
* File to connect to database
*/
//require_once(SYSTEMPATH . 'databaseSetup.php');

/**
* Function library used to login a user
*/
require_once(FUNCTIONPATH . 'loginFunctions.php');

//If the tournament info is not available, send the user to the admin section
if (!$_TOURNAMENT) {header("Location: " . ADMINURL); exit;}

//If the login form was submitted, attempt to login using those credentials.
$loginError = false;
$registrationError = "";
$passwordResetError = "";

//Set the tab to open at the beginning
$openTab = 0;

//If the login form was submitted, attempt to login using those credentials.
if ($_POST['loginSubmit']) {

	//Attempt to login the user using the supplied username and password
	$successfullyLoggedIn = loginUser($_POST['username'], $_POST['password']);
	
	//If the user logged in successfully, go to the main app page.
	if ($successfullyLoggedIn) {
		header("Location: bracket.php");
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
	else if (strlen($_POST['desiredLogin']) == 0) {
		$registrationError = "Please enter a desired Login Name.";
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
	else if (usernameAlreadyTaken($_POST['desiredLogin'])) {
		$registrationError = "The desired username is not available. Please choose another.";
	}
	
	//If everything passes, add the user and send a validation email.
	else {
	
		//Insert the user into the system
		if (registerUser($_POST['desiredLogin'], $_POST['newPassword1'], $_POST['firstName'], $_POST['lastName'], $_POST['email'])) {
		
			//If the registration was successful, send an email to verify the account
			sendVerificationEmail($_POST['desiredLogin']);
			
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
		$passwordResetError = "Please enter a username.";
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
	if (($_SESSION['userID']) && (!$_TOURNAMENT['submissionsClosed'])){
		header("Location: bracket.php");
	}
	else if ($_TOURNAMENT['submissionsClosed']) {
		header("Location: results.php");
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



<body>
	<div align="center">
		
		<h1><?PHP echo $_TOURNAMENT['championshipName'];?></h1>
		
		<div id="tabsDiv" align="center" style="padding-top: 20px">
			 <ul>
				<li><a href="#tabs-1">Login</a></li>
				<li><a href="#tabs-2">Register</a></li>
				<li><a href="#tabs-3">Reset Password</a></li>
			</ul>
			<div id="tabs-1" align="center">
				<div align="center">

					<div id="loginErrorDiv" align="center" style="padding-bottom: 10px; display:<?PHP if (!$loginError) {echo 'none';}?>;">
						The username and password do not match. <br>Please re-enter your username and password.
					</div>
					
					<form action="index.php" id="login" name="login" method="POST" align="center" style="display:inline-block">
						<table cellspacing="1" cellpadding="5" style="text-align:left">
							<tr>
								<td><strong>Username:</strong></td>
								<td><input type="text" name="username" id="username" maxlength="16"></td>
							</tr>
							<tr>
								<td><strong>Password:</strong></td>
								<td><input type="password" id="password" name="password" maxlength="24"></td>
							</tr>
							<tr>
								<td colspan="2" align="center">
									<input type="submit" name="loginSubmit" value="login">
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
						<table cellspacing="1" cellpadding="5" style="text-align:left">
							<tr>
								<td><strong>First Name:</strong></td>
								<td><input type="text" name="firstName" id="firstName" maxlength="24" value="<? if ($registrationError) echo $_POST['firstName'];?>">*</td>
							</tr>
							<tr>
								<td><strong>Last Name:</strong></td>
								<td><input type="text" name="lastName" id="lastName" maxlength="24" value="<? if ($registrationError) echo $_POST['lastName'];?>">*</td>
							</tr>
							<tr>
								<td><strong>Email:</strong></td>
								<td><input type="text" name="email" id="email" maxlength="64" value="<? if ($registrationError) echo $_POST['email'];?>"></td>
							</tr>
							<tr>
								<td><strong>Desired Login Name:</strong></td>
								<td><input type="text" name="desiredLogin" id="desiredLogin" maxlength="16" value="<? if ($registrationError) echo $_POST['desiredLogin'];?>"></td>
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
									<input type="submit" name="registerSubmit"  value="register">
								</td>
							</tr>
							<tr>
								<td colspan="2">
									<span style="font-size:14px">* First and last name will be used as your display name</span>
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
						<table cellspacing="1" cellpadding="5" style="text-align:left">
							<tr>
								<td><strong>Username:</strong></td>
								<td><input type="text" name="resetUsername" id="resetUsername" maxlength="16"></td>
							</tr>
							<tr>
								<td colspan="2" align="center">
									<input type="submit" name="resetPasswordSubmit" value="Reset Password">
								</td>
							</tr>
						</table>
					</form>
				</div>
			</div>
		</div>
		
		<div style="font-size:24px;padding-top:10px"><a onclick='$( "#explanationDiv" ).dialog( "open" );'>Why such a minimalistic design?</a></div>
		<div id="explanationDiv" style="display:none">
			To make the site as intuitive and robust as possible.<br><br>
			
			Graphic design takes time - a lot of time. Instead, I focused on
			creating a secure payment system and simple but understandable interfaces.<br><br>
			
			Perhaps in the future a more advanced design will come about, but for now: minimalist.
		</div>
		
	</div>
</body>
</html>