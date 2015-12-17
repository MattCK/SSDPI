<?PHP 
/**
* Connects the system to the database. This file should have a restricted view on it for security purposes.
*
* @package bracket
* @subpackage System
*/
/**
* File with the database connection functions
*/
require_once(FUNCTIONPATH . 'databaseFunctionLib.php');

//Database connection details
$server = 'mysql.juiciobrennan.com';
$username = 'juiciobracket';
$password = 'Champ1onsh1p';
$database = 'juiciobracket';

//Attempt to connect to server. If it fails, die.
dbConnect($server, $username, $password, $database) or die('Unable to connect to database server!');

//The server should be set to GMT, but this confirms it will be by manually setting the environmental variable
putenv("TZ=GMT");

?>