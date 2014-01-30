<?PHP 
/**
* Connects the system to the database. This file should have a restricted view on it for security purposes.
*
* @package SSS
* @subpackage System
*/
/**
* File to get the database connection functions
*/
require_once('databaseFunctions.php');

//Database connection details
$server = 'localhost';
$username = 'root';
$password = 'qwas12';
$database = 'ssPrototype';

//Attempt to connect to server. If it fails, die.
dbConnect($server, $username, $password, $database) or die('Unable to connect to database server!');

//The server should be set to GMT, but this confirms it will be by manually setting the environmental variable
putenv("TZ=GMT");

?>