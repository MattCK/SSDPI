<?PHP 

require_once('systemSetup.php');

use AdShotRunner\Menu\MenuGrabber;



header("Content-Type: text/plain");

$menuGrabber = new MenuGrabber();
$domains = $menuGrabber->getDomainMenus(['boston.com', 'nytimes.com', 'cnn.com']);
//$domains = $menuGrabber->retrieveManyDomainMenusFromDatabase(['omahaworldherald.com', 'chicagotribune.com']);
print_r($domains);
exit;

$domains['omahaworldherald.com'] = [];
$domains['omahaworldherald.com'][] = ['score' => 12,
									  'items' => [
									 				['label' => 'label1', 'url' => '/sports'],
									 				['label' => 'label2', 'url' => '/news'],
									 				['label' => 'label3', 'url' => '/weather']
									 			]];
$domains['omahaworldherald.com'][] = ['score' => 10,
									  'items' => [
									 				['label' => 'label11', 'url' => '/sports2'],
									 				['label' => 'label12', 'url' => '/news2'],
									 				['label' => 'label13', 'url' => '/weather2']
									 			]];

$domains['chicagotribune.com'] = [];
$domains['chicagotribune.com'][] =  ['score' => 22,
									  'items' => [
									 				['label' => 'label31', 'url' => '/sports3'],
									 				['label' => 'label32', 'url' => '/news3'],
									 				['label' => 'label33', 'url' => '/weather3']
									 			]];
$domains['chicagotribune.com'][] =  ['score' => 20,
									  'items' => [
									 				['label' => 'label311', 'url' => '/sports25'],
									 				['label' => 'label312', 'url' => '/news25'],
									 				['label' => 'label313', 'url' => '/weather25']
									 			]];

$menuGrabber->insertDomainsWithMenus($domains);
exit();

//$publishers = getAssociativeArrayFromCSV('hugePublisherList.csv');

print_r($menuGrabber->getRankedMenusFromManyURLs(['nytimes.com','boston.com']));
/*print_r($menuGrabber->getRankedMenusFromManyURLs([
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
		]));*/


//Returns an associative array of the CSV file with the first column as keys and second as values
function getAssociativeArrayFromCSV($csvFile) {

	//Attempt to open the file
	$csvArray = array();
	if (($csvFileHandle = fopen($csvFile, "r")) !== FALSE) {
	
		while (($currentRowData = fgetcsv($csvFileHandle, 1000, ",")) !== FALSE) {
			$csvArray[$currentRowData[0]] = $currentRowData[1];
		}
		
		fclose($csvFileHandle);
	}
	
	//If file couldn't be opened, show error and exit
	else {echo "CSV couldn't be opened"; exit();}
	
	//Return the associative array of CSV data
	return $csvArray;
}
