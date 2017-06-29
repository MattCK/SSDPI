<?PHP
/**
* Creates Campaigns for testing
*
* @package AdShotRunner
* @subpackage Tests
*/
/**
* File to setup all the path definitions
*/
require_once('../systemSetup.php');

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');


use AdShotRunner\Campaigns\Campaign;

header("Content-Type: text/plain");

//Create the test Campaign
$testCampaign = Campaign::create("Test Campaign.", 7);

//Create the AdShots
$newAdShot = $testCampaign->createAdShot("http://nytimes.com/business", false, false, false);
$newAdShot->addCreativeByID(9);
$newAdShot->addCreativeByID(10);
$newAdShot->addCreativeByID(11);

$newAdShot = $testCampaign->createAdShot("http://nytimes.com/business", true, false, false);
$newAdShot->addCreativeByID(9);
$newAdShot->addCreativeByID(10);
$newAdShot->addCreativeByID(11);

$newAdShot = $testCampaign->createAdShot("http://nytimes.com/business", true, false, true);
$newAdShot->addCreativeByID(9);
$newAdShot->addCreativeByID(10);
$newAdShot->addCreativeByID(11);

$newAdShot = $testCampaign->createAdShot("http://nytimes.com/business", false, true, false);
$newAdShot->addCreativeByID(10);

$newAdShot = $testCampaign->createAdShot("http://nytimes.com/business", true, true, false);
$newAdShot->addCreativeByID(10);

$newAdShot = $testCampaign->createAdShot("http://nytimes.com/business", true, true, true);
$newAdShot->addCreativeByID(10);


$testCampaign->queueForProcessing();

echo "Test Campaign Created. ID: " . $testCampaign->id();