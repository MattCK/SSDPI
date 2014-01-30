<?PHP


/**
* Creates connection to mysql database.
*
* Creates connection to mysql database based on the arguments passed. If an argument is not passed, the function attempts to look for the following
* defined variables:
* 
*	- DB_SERVER: name of the server to connect to
*	- DB_SERVER_USERNAME: the username of the mysql user to use
*	- DB_SERVER_PASSWORD: the password of the mysql user to use
*	- DB_DATABASE: the name of the database to conenct to
* 
* as defined below. On success, the mysql connection link is returned. On failure, die() is called and the mysql error passed to it.
*
* @param string $server  The name of the server to connect to (i.e. localhost). The default is the PHP defined variable DB_SERVER if it exists.
* @param string $username  The username of the mysql user to use. The default is the PHP defined variable DB_SERVER_USERNAME if it exists.
* @param string $password  The password of the mysql user to use. The default is the PHP defined variable DB_SERVER_USERNAME if it exists.
* @param string $database  The name of the database to connect to. The default is the PHP defined variable DB_DATABASE if it exists.
* @return resource  MySQL connection link
*/
function dbConnect($server = DB_SERVER, $username = DB_SERVER_USERNAME, $password = DB_SERVER_PASSWORD, $database = DB_DATABASE, $link = 'db_link') {
	global $$link;
	$$link = mysql_connect($server, $username, $password) or die ('I cannot connect to the database because: ' . mysql_error());
	if ($$link){
		mysql_select_db($database);
		dbQuery('SET SESSION collation_connection =\'' . 'utf8_unicode_ci' . '\'');
	}
	return $$link;
}

/**
* Executes the passed mysql query.
*
* Attempts to execute the passed mysql query. dbConnect(...) should be called successfully before this function is used or a database link should
* be passed manually to it. On success, the result from mysql_query(...) is returned. On failure, dbError(...) is called.
*
* @param string $query  The query to be executed.
* @param resource $link  MySQL connection link
* @return mixed  On success, result from mysql_query(...)
*/
function dbQuery($query, $link = 'db_link') {
	global $$link;
	$result = mysql_query($query, $$link) or dbError($query, mysql_errno(), mysql_error());
	return $result;
}

/**
* Executes die() and passes to it the passed query and mysql errors.
*
* @param string $query  The query that caused the failure.
* @param string $errno  The string returned from mysql_errno().
* @param string $error  The string returned from mysql_error().
*/
function dbError($query, $errno, $error) { 
	die('<font color="#000000"><b>' . $errno . ' - ' . $error . '<br><br>' . $query . '</b></font>');
}

?>