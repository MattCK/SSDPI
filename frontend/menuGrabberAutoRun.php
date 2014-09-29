<?PHP 

require_once('class.MySQLDatabase.php');
require_once('class.MenuGrabber.php');

Class menuGrabberProxy Extends menuGrabber {

	public function getRankedMenusFromURLList($urlList) {
		
		return parent::getRankedMenusFromManyURLs($urlList)

	}
	
	
}

header("Content-Type: text/plain");


$database = new MySQLDatabase('adshotrunner.c4gwips6xiw8.us-east-1.rds.amazonaws.com', 'adshotrunner', 'xbSAb2G92E', 'adshotrunner');

$menuGrabber = new MenuGrabberProxy();
$domains = $menuGrabber->getDomainMenus(['boston.com', 'nytimes.com', 'cnn.com']);
//$domains = $menuGrabber->retrieveManyDomainMenusFromDatabase(['omahaworldherald.com', 'chicagotribune.com']);



print_r($menuGrabber->getRankedMenusFromURLList([
			'nytimes.com', 
			'boston.com',
			'omahaworldherald.com',
			'nypost.com',
			'latimes.com',
			'cnn.com',
			'theguardian.com',
			'chicagotribune.com',
			'nypost.com',
			'bbc.com',
			'newyorker.com',
			'newyorker.com',
			'abcnews.go.com',
			'bismarktribune.com',
			'cincinnati.com'
		]));
		
exit();



