<?PHP
/**
* This page setups the bracket system's connection to the database, defines its paths, and verifies the user session data.
*
* @package bracket
* @subpackage System
*/
/**
* File to setup all the system definitions and paths
*/
require_once('systemDefininitions.php');

/**
* File to connect to database
*/
require_once(SYSTEMPATH . 'databaseSetup.php');

//Start the session
session_start();
header("Cache-control: private"); 

//Verify a valid user session exists and we're not out of submission period. If not, return the user to the index page.
if ((!$_SESSION['userID']) && (!$_TOURNAMENT['submissionsClosed'])){
	header("Location: /index.php");
	exit;
}

//Define the session constants
define('CURUSERID', $_SESSION['userID']);
define('CURUSERNAME', $_SESSION['userName']);
if (($_TOURNAMENT) && (CURUSERID == $_TOURNAMENT['adminUserID'])) {
	define('CURUSERISADMIN', true);
}
else {
	define('CURUSERISADMIN', false);
}

?>