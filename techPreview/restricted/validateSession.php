<?PHP
/**
* Validates a session exists and sets up constants. If no valid session exists, it returns the user to the index page.
*
* @package Adshotrunner
* @subpackage System
*/
/**
* 
*/

//Start the session
session_start();
header("Cache-control: private"); 

//Verify a valid user session exists. If not, return the user to the index page.
if (!$_SESSION['userID']){
	header("Location: mainApp.php");
	exit;
}

//Define the session constants
define('USERID', $_SESSION['userID']);
define('USERNAME', $_SESSION['username']);
define('USERFIRSTNAME', $_SESSION['userFirstName']);
define('USERLASTNAME', $_SESSION['userLastName']);
define('USEREMAIL', $_SESSION['userEmail']);
define('USERDFPNETWORKCODE', $_SESSION['userDFPNetworkCode']);


?>