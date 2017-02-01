<?PHP
/**
* Contains the class for communicating with a MySQL database
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\Database;

use AdShotRunner\System\ASRProperties;

//The server should be set to GMT, but this confirms it will be by manually setting the environmental variable
putenv("TZ=GMT");

/**
* The ASRDatabase class controls connecting to the database and executing queries on it. 
*
* @package Adshotrunner
* @subpackage Classes
*/
class ASRDatabase {

	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Variables ------------------------------------
	//---------------------------------------------------------------------------------------
	//***************************** Private Static Variables ********************************
	/**
	* @var resource		Link to the connected database
	*/
	private static $_databaseConnection = NULL;

	//---------------------------------------------------------------------------------------
	//---------------------------------- Static Methods -------------------------------------
	//---------------------------------------------------------------------------------------
	//***************************** Public Static Methods ***********************************
	/**
	* Executes a query and returns the result.
	*
	* @param 	string 	$query 	Query to run
	* @retval 	mysqli_result  	Result object on success, FALSE on failure
	*/
	public static function executeQuery($query) {
		$databaseLink = self::getConnection();
		$result = mysqli_query($databaseLink, $query) or 
					die('<span style="color: black;"><b>' . $databaseLink->errno . ' - ' . $databaseLink->error . '<br><br>' . $query . '</b></span>');
		return $result;
	}


	/**
	* Returns the last insert ID from the database, if one exists.
	*
	* @retval 	int  			Last insert ID from database query
	*/
	public static function lastInsertID() {
		$databaseLink = self::getConnection();
		return mysqli_insert_id($databaseLink);
	}

	/**
	* Escapes the passed string.
	*
	* @param 	string 	$unformattedString	 	String to escape
	* @retval 	string  						Escaped string
	*/
	public static function escape($unformattedString) {
		$databaseLink = self::getConnection();
		return mysqli_real_escape_string($databaseLink, $unformattedString);
	}

	//***************************** Private Static Methods **********************************
	/**
	* Returns the connection to the database
	*
	* @return resource  Connection to the database
	*/
	private static function getConnection() {

		if (self::$_databaseConnection == NULL) {

			//Get the connection details
			$host = ASRProperties::asrDatabaseHost();
			$username = ASRProperties::asrDatabaseUsername();
			$password = ASRProperties::asrDatabasePassword();
			$databaseName = ASRProperties::asrDatabase();

			//Attempt to connect to the database
			self::$_databaseConnection = mysqli_connect($host, $username, $password, $databaseName);
			
			//If the connection failed, output the reason and die
			if (self::$_databaseConnection->connect_error) {
				die('Connect Error (' . self::$_databaseConnection->connect_errno . ') ' . self::$_databaseConnection->connect_error);
			}
		}

		return self::$_databaseConnection;
	}
}

//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------
