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

$dfpCommunicator = DFPCommunicator::create("87580729053-tm802nbiqhf2o33eic38q8vvi688dsbp.apps.googleusercontent.com", 
										   "nYe5-OOiDXp4sntqydYyGWPC", 
										   "1/EmflFSwLOd-g-cQDpQ4ndtKbaeHZErtTmhRSGYVukKI", 
										   "4408", "AdShotRunner");
										   // "324288910", "AdShotRunner");

$dfpOrders = $dfpCommunicator->searchOrders("lein");
print_r($dfpOrders);