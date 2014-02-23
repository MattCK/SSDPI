<?PHP

require_once('menuGrabLib.php');

header("Content-Type: text/plain");

define('MAX_GROUP_SIZE', 60);

$requestBeginTime = time();

$urlList = getSiteURLs();

$allSites = array();
$urlCount = count($urlList);
$groupCount = ceil($urlCount/MAX_GROUP_SIZE);
echo "Beginning to get html for $groupCount groups of at most " . MAX_GROUP_SIZE . ". \n";
for ($i = 0; $i < $groupCount; ++$i) {
	
	//Notify our position if necessary
	echo "Beginning group $i/$groupCount... "; $requestBeginTime2 = time();
	
	//Get the current group
	$curURLGroup = array_slice($urlList, ($i * MAX_GROUP_SIZE), MAX_GROUP_SIZE, true);
	
	//Make all of the votes
	$returnedResponses = getListsFromManyURLs($curURLGroup);
	$allSites += array_merge($allSites, $returnedResponses);
	echo (time() - $requestBeginTime2) . " seconds total running time\n";
}



//$allSites = getListsFromManyURLs(getSiteURLs());

$finalMenus = array();
foreach($allSites as $targetURL => $unorderedLists) {

	$listRankings = getUnorderedListRankings($unorderedLists);

	//Sort the rankings from highest to lowest and output the menus.
	arsort($listRankings);
	$neededIndex = null;
	foreach ($listRankings as $curIndex => $curRanking) {
		
		if ($neededIndex === null) {$neededIndex = $curIndex;}		
		
	}
	$finalMenus[$targetURL . " (" . $listRankings[$neededIndex] . ")"] = $unorderedLists[$neededIndex];
}
echo (time() - $requestBeginTime) . " seconds total request time\n\n\n";


foreach($finalMenus as $urlKey => $curMenu) {
	echo "$urlKey: \n";
	foreach ($curMenu as $curItem) {
		echo "	- " . $curItem['title'] . " (" . $curItem['link'] . ")\n"; 
	}
	echo "\n";
}

//print_r($finalMenus);

?>