<?PHP
/**
* This page defines the system paths and loads autoloaders.
*
* @package AdShotRunner
* @subpackage System
*/
/**
* Define paths to use throughout the system
*/
require_once('pathDefinitions.php');

/**
* Load AWS classes
*/
require_once(THIRDPARTYPATH . 'aws/aws-autoloader.php');

/**
* Load AdShotRunner classes
*/
require_once(CLASSPATH . 'adShotRunnerAutoloader.php');

/**
* Add third-party folder to path for Google DFP
*/
require THIRDPARTYPATH . '/GoogleAds/vendor/autoload.php';

use Aws\Common\Aws;

function getAWSFactory() {
	return Aws::factory(RESTRICTEDPATH . 'awsFrontendProfile.php');
}


?>