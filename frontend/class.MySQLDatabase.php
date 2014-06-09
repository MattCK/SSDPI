<?PHP
namespace AdShotRunner/Classes;

/**
* The MySQLDatabase class controls connecting to the database and executing queries on it. 
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

	* On success, the connection occurred without error and the object is instantiated. 
	* On failure, die() is called and the MySQL error outputted.
	*
	* @param string $host  			The name or IP of the host to connect to
	* @param string $username  		The username of the MySQL user to use
	* @param string $password  		The password of the MySQL user to use
	* @param string $databaseName  	The name of the database to connect to
	*/
	function __construct($host, $username, $password, $databaseName){
		
		//Attempt to connect to the database
		$mysqlLink = mysqli_connect($host, $username, $password, $database);
		
		//If the connection failed, output the reason and die
		if ($mysqli->connect_error) {
			die('Connect Error (' . $mysqli->connect_errno . ') ' . $mysqli->connect_error);
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

