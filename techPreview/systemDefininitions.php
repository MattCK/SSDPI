<?PHP
/**
* Contains the system paths and relative URL paths used throughout the software.
*
* @package bracket
* @subpackage System
*/
/**
*
*/

//---------------------------------------------------------------------------------------
//--------------------------- System Path Defintions --------------------------------
//---------------------------------------------------------------------------------------	

//Base path
define('BASEPATH', '/home/juicio/championship.juiciobrennan.com/');

//Path to restricted system files
define('SYSTEMPATH', BASEPATH . 'restricted/');

//Path to function files
define('FUNCTIONPATH', BASEPATH . 'functions/');

//Path to class files
define('CLASSPATH', BASEPATH . 'classes/');

//---------------------------------------------------------------------------------------
//--------------------------- Resource URL Defintions -------------------------------
//---------------------------------------------------------------------------------------	
//Path to CSS files
define('CSSURL', '/css/');

//Path to image files
define('IMAGESURL', '/images/');

//Path to javascript files
define('JAVASCRIPTURL', '/javascript/');

//Path to admin section
define('ADMINURL', '/admin/');

//---------------------------------------------------------------------------------------
//----------------------------- Tournament Data Include ---------------------------------
//---------------------------------------------------------------------------------------	

include_once(SYSTEMPATH . "tournamentData.php");



?>