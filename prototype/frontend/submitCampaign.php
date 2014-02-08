<?PHP

require_once('databaseSetup.php');

$campaignName = $_POST['campaignName'];
$campaignURLs = $_POST['url'];

if (!$campaignName) {echo 'no campaign name passed'; exit();}
if (!$campaignURLs) {echo 'no url fields passed. this is weird.'; exit();}

$actualURLs = array();
foreach ($campaignURLs as $currentURL) {
	
	if ($currentURL != '') {$actualURLs[] = $currentURL;}

}

if (count($actualURLs) == 0) {echo 'no valid urls'; exit();}


$addCampaignQuery = "INSERT INTO campaigns (CAM_name)
					 VALUES ('$campaignName')";
dbQuery($addCampaignQuery);
$campaignID = mysql_insert_id();

$insertValues = "";
foreach ($actualURLs as $currentURL) {
	if ($insertValues != "") {$insertValues .= ',';}
	$insertValues .= "($campaignID, '$currentURL')";
}

$addURLsQuery = "INSERT INTO urls (URL_CAM_id, URL_url)
					 VALUES $insertValues";
dbQuery($addURLsQuery);

echo 'success';


$session = curl_init('http://10.1.1.50/runCampaign.jsp?campaignID=' . $campaignID);
curl_setopt($session, CURLOPT_HEADER, false);
curl_setopt($session, CURLOPT_RETURNTRANSFER, true);
curl_setopt($session, CURLOPT_FAILONERROR, false);
$responseText = curl_exec($session);
curl_close($session);

