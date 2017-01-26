<?PHP
/**
* Menu Grabber stud for testing logic issues
*
* @package AdShotRunner
*/
/**
* File to define paths, setup dependencies, and connect to database
*/
require_once('systemSetup.php');

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');

use AdShotRunner\Menu\MenuGrabber;

header("Content-Type: text/plain");

$domainMenuGrabber = new MenuGrabber();
$domainMenuGrabber->deleteManyDomains(["nytimes.com"]);
$domainMenus = $domainMenuGrabber->getDomainMenus(["nytimes.com"]);

echo "\n\n\nDone!!!";


?>

