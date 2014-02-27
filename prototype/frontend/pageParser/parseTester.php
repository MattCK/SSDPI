<?PHP

require_once('menuGrabLib.php');


if ($_GET['url']) {

	$foundLists = array();
	//$foundLists = getLists($_GET['url']);
	$manyFoundLists = getListsFromManyURLs(array($_GET['url'] => $_GET['url']), 5);
	$foundLists = current($manyFoundLists);
	//print_r(array_slice($foundLists, 0, 3));
	//print_r(array_slice($foundLists2, 0, 3));
	
	//print_r($foundLists[0]);
	//print_r($foundLists2);
	//print_r($foundLists[1]);
	
	$listRankings = getListRankings($foundLists);

	//print_r($listRankings);
	//print_r($listRankings2);
	//$foundLists = $foundLists2;
	//$listRankings = $listRankings2;
	
	$finalLists = array();
	arsort($listRankings);
	foreach($listRankings as $indexKey => $curRank) {
		$finalLists[] = array('rank' => $curRank, 'menu' => $foundLists[$indexKey]);
	}
}
?>

<div align="center">

	<form action="parseTester.php" id="urlSubmitForm" name="urlSubmitForm" method="GET" align="center">
		<input type="text" id="url" name="url" maxlength="256" style="width:300px" value="<?PHP echo $_GET['url']?>">
		<input type="submit" name="urlSubmit" value="Go!">
	</form><br><br>
	<hr>
	<div align="left">
		<pre>
		<?PHP if ($_GET['url']) {	
		
			echo "\nURL: " . $_GET['url'] . "\n\n";
			
			echo "Overall Rankins: \n";
			foreach($listRankings as $curKey => $curRank) {
				echo "	$curKey - $curRank \n";
			}
			echo "\n\n";
			
			echo "Lists by Rankings: \n";
			foreach($listRankings as $curKey => $curRank) {
				echo "	Rank $curRank: \n";
				foreach($foundLists[$curKey] as $curItem) {
					echo "		- " . $curItem['title'] . " (" . $curItem['link'] . ")\n";
				}
			}
			echo "\n\n";

			//print_r($finalLists);
		}?>
		</pre>
	</div>
</div>