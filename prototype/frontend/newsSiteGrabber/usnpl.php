<?

require_once('newsSiteGrabberLibrary.php');
require_once('usnplLibrary.php');

header("Content-Type: text/plain");

$startTime = time();

$stateAbbreviations = getStateAbbreviationsFromCSV();

$results = getUSNPLNewsSites($stateAbbreviations);

print_r($results);

$csvArray = array();
foreach($results as $curSite => $curInfo) {
	$csvArray[$curSite] = $curInfo['siteLocation'] . "," . $curInfo['siteName'];
}


$filename = dirname(__FILE__) . '/results/usnplDomains-' . time() . '.csv';
$fp = fopen($filename, 'w');
foreach ($results as $curFieldURL => $curFieldInfo) {
    fputcsv($fp, array($curFieldURL, $curFieldInfo['siteName'], $curFieldInfo['siteLocation']));
}
fclose($fp);

echo "Time: " . (time() - $startTime) . "\n\n";


?>