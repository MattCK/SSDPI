<?PHP

/**
* The MySQLDatabase class controls connecting to the database and executing queries on it. 
*
* @package Adshotrunner
* @subpackage Classes
*/
class MySQLDatabase {

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	* @var string		Host name or IP to connect to
	*/
	private $_host;

	/**
	* @var string		Username of the MySQL user used to login into the database
	*/
	private $_username;

	/**
	* @var string		Password of the MySQL user used to login into the database
	*/
	private $_password;

	/**
	* @var string		Name of the database to connect to
	*/
	private $_databaseName;

	/**
	* @var resource		Link to the connected database
	*/
	private $_databaseLink;

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Instantiates class and creates connection to MySQL database.
	*
	* Creates connection to MySQL database based on the arguments passed. 
	*
	* On success, the connection occurred without error and the object is instantiated. 
	* On failure, die() is called and the MySQL error outputted.
	*
	* @param string $host  			The name or IP of the host to connect to
	* @param string $username  		The username of the MySQL user to use
	* @param string $password  		The password of the MySQL user to use
	* @param string $databaseName  	The name of the database to connect to
	*/
	function __construct($host, $username, $password, $databaseName) {
		
		//Attempt to connect to the database
		$mysqlLink = mysqli_connect($host, $username, $password, $databaseName);
		
		//If the connection failed, output the reason and die
		if ($mysqlLink->connect_error) {
			die('Connect Error (' . $mysqlLink->connect_errno . ') ' . $mysqlLink->connect_error);
		}
		
		//Finally, store the connection information
		$this->setLink($mysqlLink);
		$this->setHost($host);
		$this->setUsername($username);
		$this->setPassword($password);
		$this->setDatabaseName($databaseName);
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods *************************************
	/**
	* Executes a query and returns the result.
	*
	* @param 	string 	$query 	Query to run
	* @retval 	mysqli_result  	Result object on success, FALSE on failure
	*/
	public function query($query) {
		$databaseLink = $this->getLink();
		$result = mysqli_query($databaseLink, $query) or 
					die('<span style="color: black;"><b>' . $databaseLink->errno . ' - ' . $databaseLink->error . '<br><br>' . $query . '</b></span>');
		return $result;
	}


	/**
	* Returns the last insert ID from the database, if one exists.
	*
	* @retval 	int  			Last insert ID from database query
	*/
	public function lastInsertID() {
		$databaseLink = $this->getLink();
		return mysqli_insert_id($databaseLink);
	}

	/**
	* Escapes the passed string.
	*
	* @param 	string 	$unformattedString	 	String to escape
	* @retval 	string  						Escaped string
	*/
	public function escape($unformattedString) {
		$databaseLink = $this->getLink();
		return mysqli_real_escape_string($databaseLink, $unformattedString);
	}


	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* Returns the name/IP of the host
	*
	* @retval string  Name/IP of the host
	*/
	public function getHost() {
		$databaseLink = $this->getLink();
		return $this->_host;
	}
	
	/**
	* Returns the username of the MySQL login user
	*
	* @retval string  Username of the MySQL login user
	*/
	public function getUsername() {
		return $this->_username;
	}
	
	/**
	* Returns the password of the MySQL login user
	*
	* @retval string  Password of the MySQL login user
	*/
	public function getPassword() {
		return $this->_password;
	}
	
	/**
	* Returns the database name
	*
	* @retval string  Database name
	*/
	public function getDatabaseName() {
		return $this->_databaseName;
	}
	
		
	//********************************* Private Accessors ***********************************
	/**
	* Sets the name/IP of the host
	*
	* @param string $newHost Name/IP of the host
	*/
	private function setHost($newHost) {
		$this->_host = $newHost;
	}
	
	/**
	* Sets the username of the MySQL login user
	*
	* @param string $newUsername  Username of the MySQL login user
	*/
	private function setUsername($newUsername) {
		$this->_username = $newUsername;
	}
	
	/**
	* Sets the password of the MySQL login user
	*
	* @param string $newPassword  Password of the MySQL login user
	*/
	private function setPassword($newPassword) {
		$this->_password = $newPassword;
	}
	
	/**
	* Sets the database name
	*
	* @param string $newDatabaseName Database name
	*/
	private function setDatabaseName($newDatabaseName) {
		$this->_databaseName = $newDatabaseName;
	}
	
	/**
	* Returns the link to the database
	*
	* @return resource  Link to the database
	*/
	private function getLink() {
		return $this->_databaseLink;
	}
	/**
	* Sets the link to the database
	*
	* @param resource $newUsername  New link to the database 
	*/
	private function setLink($newLink) {
		$this->_databaseLink = $newLink;
	}
}

//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------
/**
* Executes a query on the primary global database and returns the result.
*
* THIS IS A VERY LIMITED, NARROW, AND SPECIFIC FUNCTION. READ CAREFULLY!!!
*
* This function requires a database to be created and stored in the global variable
* named $database. If there is no global $database, it simply returns NULL. Otherwise,
* it will run the function on that global variable and return the result.
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
* THIS IS A VERY LIMITED, NARROW, AND SPECIFIC FUNCTION. READ CAREFULLY!!!
*
* This function requires a database to be created and stored in the global variable
* named $database. If there is no global $database, it simply returns NULL. Otherwise,
* it will run the function on that global variable and return the result.
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
* THIS IS A VERY LIMITED, NARROW, AND SPECIFIC FUNCTION. READ CAREFULLY!!!
*
* This function requires a database to be created and stored in the global variable
* named $database. If there is no global $database, it simply returns NULL. Otherwise,
* it will run the function on that global variable and return the result.
*
* @param 	string 		$unformattedString 	String to be escaped
* @retval 	string  						Escaped string
*/
function databaseEscape($unformattedString) {

	global $database;
	if (!$database) {return NULL;}
	return $database->escape($unformattedString);
}

