<?PHP
/**
* Allows a user to reset their password. 
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
require_once(SYSTEMPATH . 'databaseSetup.php');

/**
* Function library used to login a user
*/
require_once(FUNCTIONPATH . 'loginFunctionsLib.php');

//If the tournament info is not available, send the user to the admin section
if (!$_TOURNAMENT) {header("Location: " . ADMINURL);}

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
else if ($_GET['v'] != md5('r3s3TP@55' . $curUser->getUsername() . $curUser->getPassword())) {
	$verificationError = "Verification code is incorrect. Unable to reset the password.";
}
//If the register form was submitted, attempt to register the user.
else if ($_POST['resetPasswordSubmit']) {
	
	//First, verify valid information was submitted.
	if ((strlen($_POST['newPassword1']) == 0) && (strlen($_POST['newPassword2']) == 0)) {
		$passwordResetError = "Please enter a password.";
	}
	else if ($_POST['newPassword1'] != $_POST['newPassword2']) {
		$passwordResetError = "The passwords do not match. Please re-enter the passwords.";
	}
	
	//If everything passes, save the new password and logout.
	else {	
		changeUserPassword($_GET['id'], $_POST['newPassword1']);
		header("Location: logout.php");
	}
}

?>

<?PHP include_once(BASEPATH . "header.php");?>

<body>
	<div align="center" style="padding-top: 50px">
		<div style="display:<?PHP if (!$verificationError) {echo 'none';}?>">
			<?PHP echo $verificationError ?>
			<br><br>
			<a href="/">Click here</a> to return to the homepage.
		</div>
		
		<div style="display:<?PHP if ($verificationError) {echo 'none';}?>">
			<div align="center">

				<div id="passwordResetErrorDiv" align="center" style="display:<?PHP if (!$passwordResetError) {echo 'none';}?>;">
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
								<input type="submit" name="resetPasswordSubmit"  value="Save New Password">
							</td>
						</tr>
					</table>
				</form>
			</div>
		</div>
	</div>	
</body>
</html>