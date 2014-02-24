<?PHP

require_once('menuGrabLibMK.php');


if ($_GET['url']) {

	$unorderedLists = array();
	$listRankings;
	if ($_GET['searchType'] == 'div') {
	$unorderedLists = getDivLists($_GET['url']);
	$listRankings = getUnorderedListRankings($unorderedLists);
	}
	elseif($_GET['searchType'] == 'ul')  {
	$unorderedLists = getUnorderedLists($_GET['url']);
	$listRankings = getUnorderedListRankings($unorderedLists);
	}
	else {
	$unorderedLists = getAnchorLists($_GET['url']);
	//$printArray = getAnchorLists($_GET['url']);
	//echo $printArray;
	$listRankings = getAnchorListRankings($unorderedLists);
	}
	

	$finalLists = array();
	arsort($listRankings);
	foreach($listRankings as $indexKey => $curRank) {
		$finalLists[] = array('rank' => $curRank, 'menu' => $unorderedLists[$indexKey]);
	}
}
?>

<div align="center">

	<form action="parseTesterMK.php" id="urlSubmitForm" name="urlSubmitForm" method="GET" align="center">
		<input type="text" id="url" name="url" maxlength="256" style="width:300px" value="<?PHP echo $_GET['url']?>">
		<select name="searchType"><option value="a">Anchors</option><option value="ul">ULs</option><option value="div">Divs</option></select>
		<input type="submit" name="urlSubmit" value="Go!">
	</form><br><br>
	<hr>
	<div align="left">
		<pre>
		<?PHP if ($_GET['url']) {	
		
			echo "\nURL: <a href=\"". $_GET['url'] . "\">" . $_GET['url'] . "</a> \n\n";
			
			//echo "Overall Rankins: \n";
			//foreach($listRankings as $curKey => $curRank) {
			//	echo "	$curKey - $curRank \n";
			//}
			echo "\n\n";
			//echo $printArray;
			
			echo "Lists by Rankings: \n Items Count: " . count($finalLists) . "\n";
			foreach($finalLists as $rankAndLink){
				echo "Rank " . $rankAndLink['rank'] . "\n";
				$linkInfo = $rankAndLink['menu'];
				echo "      Link: " . $linkInfo['link'] . "\n";
				echo "      Title: " . $linkInfo['title'] . "\n";
			}
			
			echo "\n\n";

			//print_r($finalLists);
		}?>
		</pre>
	</div>
</div>