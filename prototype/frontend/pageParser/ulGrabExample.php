<?PHP

require_once('menuGrabLib.php');

header("Content-Type: text/plain");

//$result = getListsFromManyURLs(getSiteURLs());
//print_r($result);



$unorderedLists = getDivLists('www.fox47.com');
$listRankings = getUnorderedListRankings($unorderedLists);

$finalLists = array();
arsort($listRankings);
foreach($listRankings as $indexKey => $curRank) {
	//if ($curRank != 0) {
		$finalLists[] = array('rank' => $curRank, 'menu' => $unorderedLists[$indexKey]);
		//$unorderedLists[$indexKey] = $listRankings[$indexKey];
	//}
}

print_r($listRankings);
print_r($finalLists);
/*foreach($unorderedLists as $curList) {
	foreach($curList as $curItem) {
		echo "Title: '" . $curItem['title'] . "'\n";
	}
}*/

?>