<?PHP 
/**
* File to define paths, setup dependencies, and connect to database
*/
require_once('systemSetup.php');

use AdShotRunner\System\ASRProperties;
use AdShotRunner\Utilities\WebPageCommunicator;

$URLCommunicator = new WebPageCommunicator();
echo $URLCommunicator->getURLResponse("https://s3.amazonaws.com/" . ASRProperties::containerForCampaignJobs() . "/" . $_REQUEST['jobID']);
