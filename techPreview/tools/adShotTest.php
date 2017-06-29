<?PHP
/**
* Test for AdShot class
*
* @package AdShotRunner
* @subpackage Tests
*/
/**
* File to setup all the path definitions
*/
require_once('../systemSetup.php');

use AdShotRunner\Campaigns\AdShot;
use AdShotRunner\Campaigns\Creative;

// header("Content-Type: text/plain");

// echo "Get AdShot by ID(1): \n\n";

$tempAdShot = AdShot::getAdShot(67);
$adShotJSON = $tempAdShot->toJSON();

// $newAdShot = AdShot::create(1, "http://requestedURL", false, false, true);

// $tempCreative1 = Creative::getCreative(1);
// $tempCreative2 = Creative::getCreative(2);
// $tempCreative3 = Creative::getCreative(3);
// $newAdShot->addCreative($tempCreative1);
// $newAdShot->addCreative($tempCreative2);
// $newAdShot->addCreative($tempCreative3);

// print_r($newAdShot);

?>

<head>
	<script type="text/javascript" src="/javascript/classes/Creative.js"></script>
	<script type="text/javascript" src="/javascript/classes/AdShot.js"></script>

</head>

<body>

<pre id="output"></pre>

<script>

let adShotJSON = <?PHP echo json_encode(["jsonString" => $adShotJSON]) ?>;
document.getElementById("output").innerHTML = adShotJSON["jsonString"];
let testAdShot = new AdShot(adShotJSON["jsonString"]);
console.log(testAdShot);
</script>

</body>