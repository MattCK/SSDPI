<?PHP
/**
* Test for Creative class
*
* @package AdShotRunner
* @subpackage Tests
*/
/**
* File to setup all the path definitions
*/
require_once('../systemSetup.php');

use AdShotRunner\Campaigns\Creative;

// header("Content-Type: text/plain");

// echo "Get creative by ID(1): \n\n";

// $creative = Creative::getCreative(1);
// $creativeJSON = $creative->toJSON();

// echo "STATUS: " . $creative->status() . "\n";
// echo "Created: " . $creative->createdTimestamp() . "\n";
// echo "Queued: " . $creative->queuedTimestamp() . "\n";
// echo "Processing: " . $creative->processingTimestamp() . "\n";
// echo "Finished: " . $creative->finishedTimestamp() . "\n";
// echo "Error: " . $creative->errorTimestamp() . "\n\n";
// echo "Error Message: " . $creative->errorMessage() . "\n\n";

// $creative->setStatus(Creative::QUEUED);

// echo "STATUS: " . $creative->status() . "\n";
// echo "Created: " . $creative->createdTimestamp() . "\n";
// echo "Queued: " . $creative->queuedTimestamp() . "\n";
// echo "Processing: " . $creative->processingTimestamp() . "\n";
// echo "Finished: " . $creative->finishedTimestamp() . "\n";
// echo "Error: " . $creative->errorTimestamp() . "\n\n";
// echo "Error Message: " . $creative->errorMessage() . "\n\n";

// $creative->setStatus(Creative::PROCESSING);

// echo "STATUS: " . $creative->status() . "\n";
// echo "Created: " . $creative->createdTimestamp() . "\n";
// echo "Queued: " . $creative->queuedTimestamp() . "\n";
// echo "Processing: " . $creative->processingTimestamp() . "\n";
// echo "Finished: " . $creative->finishedTimestamp() . "\n";
// echo "Error: " . $creative->errorTimestamp() . "\n\n";
// echo "Error Message: " . $creative->errorMessage() . "\n\n";

// $creative->setStatus(Creative::FINISHED);

// echo "STATUS: " . $creative->status() . "\n";
// echo "Created: " . $creative->createdTimestamp() . "\n";
// echo "Queued: " . $creative->queuedTimestamp() . "\n";
// echo "Processing: " . $creative->processingTimestamp() . "\n";
// echo "Finished: " . $creative->finishedTimestamp() . "\n";
// echo "Error: " . $creative->errorTimestamp() . "\n\n";
// echo "Error Message: " . $creative->errorMessage() . "\n\n";

// $creative->setError("Some error message");

// echo "STATUS: " . $creative->status() . "\n";
// echo "Created: " . $creative->createdTimestamp() . "\n";
// echo "Queued: " . $creative->queuedTimestamp() . "\n";
// echo "Processing: " . $creative->processingTimestamp() . "\n";
// echo "Finished: " . $creative->finishedTimestamp() . "\n";
// echo "Error: " . $creative->errorTimestamp() . "\n\n";
// echo "Error Message: " . $creative->errorMessage() . "\n\n";

// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";
// echo Creative::getUUID() . "\n";

// $imageFile = fopen("creativeImage.jpg", "r") or die("Unable to open file!");
// print_r($imageFile); echo "\n\n";

// $newCreative = Creative::createFromImageFile("creativeImage.jpg");
// print_r($newCreative);

$newCreative = Creative::createFromImageURL(
		"https://s3.amazonaws.com/asr-development/creativeimages/a727b242-bf2e-4dff-9eb4-e041f3e108ff.png");
echo "Creative ID: " . $newCreative->id() . "\n";
echo "Creative URL: " . $newCreative->imageURL() . "\n\n";

$newCreative = Creative::createFromImageURL(
		"https://s3.amazonaws.com/asr-development/creativeimages/d053c506-7e1c-4338-8489-f79f8ac0c1f4.png");
echo "Creative ID: " . $newCreative->id() . "\n";
echo "Creative URL: " . $newCreative->imageURL() . "\n\n";

$newCreative = Creative::createFromImageURL(
		"https://s3.amazonaws.com/asr-development/creativeimages/43e83f36-b8c1-4d14-b082-a85a1cc8ff5a.png");
echo "Creative ID: " . $newCreative->id() . "\n";
echo "Creative URL: " . $newCreative->imageURL() . "\n\n";
// print_r($newCreative);

// $newCreative = Creative::createFromTagScript("some tag script");
// $newCreative->setPriority(14);
// print_r($newCreative);
// echo "\n\n";
// echo $newCreative->toJSON();

// fclose($imageFile);

// print_r($creative);

//https://s3.amazonaws.com/asr-development/creativeimages/a75e8e5d-929f-40e3-be36-72e5792b01ff.png

?>

<head>
	<script type="text/javascript" src="/javascript/Creative.js"></script>

</head>

<body>

<pre id="output"></pre>

<script>

let creativeJSON = <?PHP echo json_encode(["jsonString" => $creativeJSON]) ?>;
document.getElementById("output").innerHTML = creativeJSON["jsonString"];
let testCreative = new Creative(creativeJSON["jsonString"]);
console.log(testCreative);
</script>

</body>