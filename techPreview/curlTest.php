<?PHP

/**
* File to define all the system paths
*/
require_once('systemSetup.php');
use AdShotRunner\Menu\MenuGrabber;

use AdShotRunner\PhantomJS\PhantomJSCommunicator;

//echo PhantomJSCommunicator::getResponse("http://www.google.com");

$domainMenuGrabber = new MenuGrabber();
$domainMenu = $domainMenuGrabber->getBestDomainMenu("wkyc.com");
print_r($domainMenu);