<?PHP 

require_once('class.MySQLDatabase.php');
require_once('class.MenuGrabber.php');

Class menuGrabberProxy Extends menuGrabber {

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

$menuGrabber = new MenuGrabberProxy();

//$domainArray = array();
$domainAndTimeArray = $menuGrabber->retrieveDomainListFromDatabase();
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
$menuGrabber->InsertDomainsWithMenusFromList($menuGrabber->getRankedMenusFromURLList($domainArray));
print("Done with the oldest third of the domain list");
		
exit();



