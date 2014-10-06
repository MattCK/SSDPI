<?PHP
/**
* This page setups the system's connection to the database, defines its paths, loads autoloaders, 
* and verifies/defines session data.
*
* @package QDROP
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
* Connect to the database
*/
require_once(RESTRICTEDPATH . 'databaseSetup.php');

/**
* Verify the session is valid and set the session constants
*/
//require_once(RESTRICTEDPATH . 'validateSession.php');

use Aws\Common\Aws;

function getAWSFactory() {
	return Aws::factory(RESTRICTEDPATH . 'awsFrontendProfile.php');
}


?>