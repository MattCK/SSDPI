<?

error_reporting(E_ALL ^ (E_NOTICE | E_WARNING));

require_once('newsSiteGrabberLibrary.php');

header("Content-Type: text/plain");

//Clear out the response list
file_put_contents(dirname(__FILE__) . '/results/responseList.txt', "");

$startTime = time();

$citiesList = getCitiesFromCSV();
//$citiesList = array('sacramento', 'omaha', 'cincinnati', 'chicago', 
//					'milwaukee', 'washington', 'tulsa', 'maryville', 'dallas', 'rosemont');
//$citiesList = array('sacramento', 'omaha', 'washington d.c.');
//$citiesList = array('washington d.c.');

echo count($citiesList) . " cities...\n\n";

$results = getNewsDomains($citiesList);

$filename = dirname(__FILE__) . '/results/newsDomains-' . time() . '.csv';
writeArrayToCSV($filename, $results);

echo "Time: " . (time() - $startTime) . "\n\n";


?>