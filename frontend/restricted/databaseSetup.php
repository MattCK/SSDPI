<?PHP 
/**
* Connects the system to the database. This file should have a restricted view on it for security purposes.
*
* @package AdShotRunner
* @subpackage System
*/

use AdShotRunner\Database\MySQLDatabase;

//Database connection details
$host = 'adshotrunner.c4gwips6xiw8.us-east-1.rds.amazonaws.com';
$username = 'adshotrunner';
$password = 'xbSAb2G92E';
$database = 'adshotrunner';

//Connect to the server
$database = new MySQLDatabase($host, $username, $password, $database);

//The server should be set to GMT, but this confirms it will be by manually setting the environmental variable
putenv("TZ=GMT");

//---------------------------------------------------------------------------------------
//--------------------------- Database Global Wrappers-----------------------------------
//---------------------------------------------------------------------------------------


/**
* Executes a query on the primary global database and returns the result.
*
* @param 	string 	$query 	Query to run on the database stored at the global $database
* @retval 	mysqli_result  	Result object on success, FALSE on failure
*/
function databaseQuery($query) {

	global $database;
	if (!$database) {return NULL;}
	return $database->query($query);
}

/**
* Returns the last insert ID from the database, if one exists.
*
* @retval 	int  			Last insert ID from database query
*/
function lastInsertID() {

	global $database;
	if (!$database) {return NULL;}
	return $database->lastInsertID();
}

/**
* Cleanly escapes the passed string so it is safe for use in queries.
*
* @param 	string 		$unformattedString 	String to be escaped
* @retval 	string  						Escaped string
*/
function databaseEscape($unformattedString) {

	global $database;
	if (!$database) {return NULL;}
	return $database->escape($unformattedString);
}



?>