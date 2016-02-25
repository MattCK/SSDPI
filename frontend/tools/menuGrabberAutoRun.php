<?PHP 

//require_once('class.MySQLDatabase.php');
//require_once('class.MenuGrabber.php');
/**
* Define paths to use throughout the system
*/
require_once('../pathDefinitions.php');

/**
* Load AWS classes
*/
require_once(THIRDPARTYPATH . 'aws/aws-autoloader.php');

/**
* Load AdShotRunner classes
*/
require_once(CLASSPATH . 'adShotRunnerAutoloader.php');

/**
* Connect to the database
*/
require_once(RESTRICTEDPATH . 'databaseSetup.php');

use Aws\Common\Aws;

function getAWSFactory() {
	return Aws::factory(RESTRICTEDPATH . 'awsFrontendProfile.php');
}

use AdShotRunner\Menu\MenuGrabber;


Class menuGrabberProxyProxy Extends menuGrabberProxy {

	public function getRankedMenusFromURLList($urlList) {
		
		return parent::getRankedMenusFromManyURLs($urlList);

	}
	
	public function InsertDomainsWithMenusFromList($domainMenuList){
	
		return parent::InsertDomainsWithMenus($domainMenuList);
	
	}
	
	public function retrieveDomainListFromDatabase() {
		
		//Retrieve the 
		$domainGrabResult = databaseQuery("SELECT * FROM menuDomains");
		$domainList = array();
		while ($curRow = $domainGrabResult->fetch_assoc()) {
		    $domainList[$curRow['MND_domain']] = $curRow['MND_lastUpdated'];
		}
		return $domainList;
	}
	
}

header("Content-Type: text/plain");
$database = new MySQLDatabase('adshotrunner.c4gwips6xiw8.us-east-1.rds.amazonaws.com', 'adshotrunner', 'xbSAb2G92E', 'adshotrunner');

$menuGrabberProxy = new MenuGrabberProxy();

//$domainArray = array();
$domainAndTimeArray = $menuGrabberProxy->retrieveDomainListFromDatabase();
//print_r($domainAndTimeArray);
asort($domainAndTimeArray);
//$domainAndTimeArray = array_reverse($domainAndTimeArray);
//print_r($domainAndTimeArray);
$domainAndTimeArrayLength = count($domainAndTimeArray);
//since we'll be grabbing 1/3 of the list every 3 days we need to slice the array
// the plus one ensures we always get at least 1/3 of the domain list when it doesn't divide evenly
$thirdOfDomainLength = floor($domainAndTimeArrayLength/3) + 1;

$targetDomains = array_slice($domainAndTimeArray,0,  $thirdOfDomainLength);
//print_r($targetDomains);

$domainArray = array_keys($targetDomains);
$menuGrabberProxy->InsertDomainsWithMenusFromList($menuGrabberProxy->getRankedMenusFromURLList($domainArray));
print("Done with the oldest third of the domain list");
		
exit();



