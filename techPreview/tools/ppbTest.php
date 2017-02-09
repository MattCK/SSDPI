<?PHP
error_reporting(E_ALL);
ini_set('display_errors', 1);
/**
* Test powerpoint backgrounds
*
* @package AdShotRunner
* @subpackage Functions
*/
/**
* File to setup all the path definitions
*/
require_once('../systemSetup.php');

use AdShotRunner\PowerPoint\PowerPointBackground;

//header("Content-Type: text/plain");

$testBackground = PowerPointBackground::create("Test Background", "D7D7D7", "myBackground.jpg", "testBackground.jpg", 7);

$testBackground = PowerPointBackground::archive($testBackground->getID());

echo "ID: " . $testBackground->getID() . "\n<br>";
echo "Title: " . $testBackground->getTitle() . "\n<br>";
echo "Font Color: " . $testBackground->getFontColor() . "\n<br>";
echo "Original Filename: " . $testBackground->getOriginalFilename() . "\n<br>";
echo "Filename: " . $testBackground->getFilename() . "\n<br>";
echo "Thumbnail Filename: " . $testBackground->getThumbnailFilename() . "\n<br>";
echo "User ID: " . $testBackground->getUserID() . "\n<br>";
echo "Upload Timestamp: " . $testBackground->getUploadTimestamp() . "\n<br>";

//ec2-54-205-17-26.compute-1.amazonaws.com