<?PHP

require_once('menuGrabLib.php');

header("Content-Type: text/plain");

//Set the target URL to find menus
//$targetURL = "www.boston.com";
//$targetURL = "www.chron.com";
//---------------------------------

//Get all the sites to visit from the CSV file
$siteURLs = getSiteURLs();
/*$siteURLs = array(
	'www.nytimes.com' => 'www.nytimes.com',
	'www.boston.com' => 'www.boston.com',
	'www.chron.com' => 'www.chron.com'
);*/
$finalMenus = array();

$requestBeginTime = time();
foreach($siteURLs as $targetURL) {

	//Grab the ULs from the target URL and rank them
	$unorderedLists = getUnorderedLists($targetURL);
	$listRankings = getUnorderedListRankings($unorderedLists);

	//Sort the rankings from highest to lowest and output the menus.
	arsort($listRankings);
	$neededIndex = null;
	foreach ($listRankings as $curIndex => $curRanking) {
		
		if ($neededIndex === null) {$neededIndex = $curIndex;}		
		
	}
	$finalMenus[$targetURL . " (" . $listRankings[$neededIndex] . ")"] = $unorderedLists[$neededIndex];
}
echo (time() - $requestBeginTime) . " seconds total request time\n";


foreach($finalMenus as $urlKey => $curMenu) {
	echo "$urlKey: \n";
	foreach ($curMenu as $curItem) {
		echo "	- " . $curItem['title'] . " (" . $curItem['link'] . ")\n"; 
	}
	echo "\n";
}

//print_r($finalMenus);

?>