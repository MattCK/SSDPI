<?

require_once('newsSiteGrabberLibrary.php');
require_once('usnplLibrary.php');

header("Content-Type: text/plain");

$startTime = time();

$stateAbbreviations = getStateAbbreviationsFromCSV();

$results = getUSNPLNewsSites($stateAbbreviations);

$filename = dirname(__FILE__) . '/results/newsTypesUsnplDomains-' . time() . '.csv';
$fp = fopen($filename, 'w');
foreach ($results as $curFieldURL => $curFieldInfo) {
    fputcsv($fp, array($curFieldURL, $curFieldInfo['siteName'], $curFieldInfo['siteLocation'], $curFieldInfo['type']));
}
fclose($fp);

echo "Time: " . (time() - $startTime) . "\n\n";


?>