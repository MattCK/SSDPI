<?PHP

error_reporting(E_ALL ^ (E_NOTICE | E_WARNING));

/* General functions for parsing for menus, making url requests, and reading/writing CSVs.  */
require_once('menuGrabLib.php');

//Let's just use text output since we need no formatting
header("Content-Type: text/plain");

//Defines how many requests will be made at a time
define('MAX_GROUP_SIZE', 60);

//Begin recording time for the overall period
$requestBeginTime = time();

//Get the list of urls to retrieve. This needs to be associative as 'sameURL' => 'sameURL'
//$urlList = getSiteURLs();
$urlList = getAssociativeArrayFromCSV("hugePublisherList.csv");

$allSites = array();
$urlCount = count($urlList);
$groupCount = ceil($urlCount/MAX_GROUP_SIZE);
echo "Beginning to get html for $groupCount groups of at most " . MAX_GROUP_SIZE . ". \n";
for ($i = 0; $i < $groupCount; ++$i) {
	
	//Notify our position if necessary
	echo "Beginning group " . ($i + 1) . "/$groupCount... "; 
	$requestBeginTime2 = time();
	
	//Get the current group
	$curURLGroup = array_slice($urlList, ($i * MAX_GROUP_SIZE), MAX_GROUP_SIZE, true);
	
	//Make all of the votes
	$returnedLists = getListsFromManyURLs($curURLGroup, 2);
	$allSites += array_merge($allSites, $returnedLists);
	echo (time() - $requestBeginTime2) . " seconds total running time\n";
}
echo "\n\n";

//$allSites = getListsFromManyURLs(getSiteURLs());

$finalMenus = array();
$csvFinalMenus = array();
foreach($allSites as $targetURL => $currentLists) {

	$listRankings = getListRankings($currentLists);
	
	

	//Sort the rankings from highest to lowest and output the menus.
	arsort($listRankings);
	$neededIndex = null;
	foreach ($listRankings as $curIndex => $curRanking) {
		
		if ($neededIndex === null) {$neededIndex = $curIndex;}		
		
	}
	$finalMenus[$targetURL . " (" . $listRankings[$neededIndex] . ")"] = $currentLists[$neededIndex];
	$csvFinalMenus[$targetURL] = array('rank' => $listRankings[$neededIndex], 'menu' => $currentLists[$neededIndex]);
}
echo (time() - $requestBeginTime) . " seconds total request time\n\n\n";

foreach($finalMenus as $urlKey => $curMenu) {
	echo "$urlKey: \n";
	foreach ($curMenu as $curItem) {
		echo "	- " . $curItem['title'] . " (" . $curItem['link'] . ")\n"; 		
	}
	echo "\n";
}

//Put menu results in csv
$filename = dirname(__FILE__) . '/results/publisherMenus-' . time() . '.csv';
$fp = fopen($filename, 'w');
$wordFrequencyList = array();
echo "Writing menu csv: $filename... \n\n";
foreach($csvFinalMenus as $urlKey => $curInfo) {

	$allMenuFields = array($urlKey, $curInfo['rank']);
	foreach ($curInfo['menu'] as $curItem) {
		$allMenuFields[] = $curItem['title'];
		
		$titleLower = strtolower($curItem['title']);
		$wordFrequencyList[$titleLower] = ($wordFrequencyList[$titleLower]) ? ($wordFrequencyList[$titleLower] + 1) : 1;
		
	}

    fputcsv($fp, $allMenuFields);
}
fclose($fp);

$frequencyFilename = dirname(__FILE__) . '/results/menuWordFrequencies-' . time() . '.csv';
arsort($wordFrequencyList);
echo "Writing word frequency csv: $filename... \n\n";
$fp = fopen($frequencyFilename, 'w');
foreach($wordFrequencyList as $curWord => $frequency) {

    fputcsv($fp, array($curWord, $frequency));
}
fclose($fp);



echo "Time: " . (time() - $startTime) . "\n\n";



//print_r($finalMenus);

?>