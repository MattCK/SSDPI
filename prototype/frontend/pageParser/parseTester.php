<?PHP

require_once('menuGrabLib.php');


if ($_GET['url']) {

	$unorderedLists = array();
	if ($_GET['searchType'] == 'div') {$unorderedLists = getDivLists($_GET['url']);}
	else {$unorderedLists = getUnorderedLists($_GET['url']);}

	$listRankings = getUnorderedListRankings($unorderedLists);

	$finalLists = array();
	arsort($listRankings);
	foreach($listRankings as $indexKey => $curRank) {
		$finalLists[] = array('rank' => $curRank, 'menu' => $unorderedLists[$indexKey]);
	}
}
?>

<div align="center">

	<form action="parseTester.php" id="urlSubmitForm" name="urlSubmitForm" method="GET" align="center">
		<input type="text" id="url" name="url" maxlength="256" style="width:300px" value="<?PHP echo $_GET['url']?>">
		<select name="searchType"><option value="ul">ULs</option><option value="div">Divs</option></select>
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
				foreach($unorderedLists[$curKey] as $curItem) {
					echo "		- " . $curItem['title'] . " (" . $curItem['link'] . ")\n";
				}
			}
			echo "\n\n";

			//print_r($finalLists);
		}?>
		</pre>
	</div>
</div>