<?PHP
/**
* Test for Campaign class
*
* @package AdShotRunner
* @subpackage Tests
*/
/**
* File to setup all the path definitions
*/
require_once('../systemSetup.php');

use AdShotRunner\Campaigns\Campaign;

$tempCampaign = Campaign::getCampaign(21);
$campaignJSON = $tempCampaign->toJSON();

// header("Content-Type: text/plain");

// echo "Get Campaign by ID(1): \n\n";

// $startTime = microtime(true);
// echo "Start time: " . $startTime . "\n";
// $testCampaign = Campaign::getCampaign(21);
// echo "End time: " . microtime(true) . "\n";
// echo "Total time: " . (microtime(true) - $startTime);

// $newCampaign = Campaign::create("Dangerous Penguins, INC.", 7);

// $newAdShot = $newCampaign->createAdShot("http://someurl.com", true, false, true);
// $newAdShot->addCreativeByID(6);
// $newAdShot->addCreativeByID(7);

// $newCampaign->queueForProcessing();

// print_r($newCampaign);

?>

<head>
	<script type="text/javascript" src="/javascript/classes/Creative.js"></script>
	<script type="text/javascript" src="/javascript/classes/AdShot.js"></script>
	<script type="text/javascript" src="/javascript/classes/Campaign.js"></script>

</head>

<body>

<pre id="output"></pre>

<script>

let campaignJSON = <?PHP echo json_encode(["jsonString" => $campaignJSON]) ?>;
document.getElementById("output").innerHTML = campaignJSON["jsonString"];
let testCampaign = new Campaign(campaignJSON["jsonString"]);
console.log(testCampaign);
</script>

</body>