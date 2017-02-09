<?PHP
/**
* Test system properties
*
* @package AdShotRunner
* @subpackage Functions
*/
/**
* File to setup all the path definitions
*/
require_once('../systemSetup.php');

use AdShotRunner\System\ASRProperties;

header("Content-Type: text/plain");

echo "Host: " . ASRProperties::asrDatabaseHost() . "\n";
echo "Username: " . ASRProperties::asrDatabaseUsername() . "\n";
echo "Password: " . ASRProperties::asrDatabasePassword() . "\n";
echo "Database: " . ASRProperties::asrDatabase() . "\n\n";

echo "Tag Image Requests Queue: " . ASRProperties::queueForTagImageRequests() . "\n";
echo "Screenshot Requests Queue: " . ASRProperties::queueForScreenshotRequests() . "\n";