<?PHP
/**
* Main user page where they can create their bracket, choose their tie breaker, and pay.
*
* @package bracket
*/
/**
* File to define all the system paths and the tournament data
*/
require_once('systemSetup.php');

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');

?>


<body>
	<div id="container">
		Site Domain: <input id="adSiteDomain" type="text">
		<input type="button" value"Go!">
	</div>
</body>
</html>
