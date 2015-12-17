<?PHP
/**
* Page to log user out of system and return the user to the index page.
*
* @package bracket
* @subpackage Pages
*/
/**
*
*/

//Start the session
session_start();
header("Cache-control: private"); 

//Clear the info
$_SESSION = array(); 

//Destroy the session
session_destroy();

//Send the user to the index page
header("Location: /");

?>