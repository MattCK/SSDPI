<?PHP
/**
* Functions to send and interact with emails
*
* @package AdShotRunner
* @subpackage Functions
*/
/**
* File to setup all the path definitions
*/
require_once('../systemSetup.php');

use AdShotRunner\Users\User;

header("Content-Type: text/plain");

$foundUser = User::getUserByUsername("juicio@juiciobrennan.com");

$myPassword = "750e84df61256d688af91da2e5aa4ce1109d9072";
$myUser = User::findUser("juicio@juiciobrennan.com", $myPassword);

echo "Set code: " . $myUser->getDFPNetworkCode() . "\n";
