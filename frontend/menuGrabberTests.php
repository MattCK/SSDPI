<?PHP 

require_once('class.MySQLDatabase.php');
require_once('class.MenuGrabber.php');

header("Content-Type: text/plain");


$database = new MySQLDatabase('adshotrunner.c4gwips6xiw8.us-east-1.rds.amazonaws.com', 'adshotrunner', 'xbSAb2G92E', 'adshotrunner');

//$publishers = getAssociativeArrayFromCSV('hugePublisherList.csv');

$menuGrabber = new MenuGrabber();
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
