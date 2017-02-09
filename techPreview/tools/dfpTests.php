<?PHP
/**
* Functions to send and interact with emails
*
* @package AdShotRunner
* @subpackage Functions
*/
/**
* File to setup all the path definitions
*/
require_once('../systemSetup.php');

use AdShotRunner\DFP\DFPCommunicator;

header("Content-Type: text/plain");

$dfpCommunicator = DFPCommunicator::create("1041453671893-6u2tkvf48t1d761c40niul48e94f27pr.apps.googleusercontent.com", 
										   "VdC_QJfGyZCt0Q-dUJl47CnQ", 
										   "1/YI_KIXNvTmidUf756JE_4bu9qXlD_j_1azY_E6iLfb0", 
										   "324288910", "AdShotRunner");

print_r($dfpCommunicator->getOrders());

$dfpCommunicator->getLineItemsAndCreative("778532670", $lineItems, $creatives);
print_r($lineItems);
print_r($creatives);