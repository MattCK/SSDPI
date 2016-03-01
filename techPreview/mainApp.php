<?PHP
/**
* Main app page that controls creation of screenshots
*
* @package AdShotRunner
*/
/**
* File to define paths, setup dependencies, and connect to database
*/
require_once('systemSetup.php');

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');

?>

<?PHP include_once(BASEPATH . "header.php");?>

<script>

function getMenu() {

	//Create the callback function that will show the table
	var callback = function(response) {
		
		//If successful, clear and hide the plan form, show the new table, and highlight the new table row
		if (response.success) {
			
			console.log(response.data);
		}
		
		//If failure, show us the message returned from the server and focus on the selected element if returned. Also, re-enable the submit button.
		else {
			alert(response.message);
		}
	}
	
	//Make the request
	base.asyncRequest("getMenu.php", 'domain=' + base.nodeFromID('adSiteDomain').value, callback);
}

</script>

<body>
	<div id="container">
		Site Domain: <input id="adSiteDomain" type="text">
		<input type="button" value="Go!" onclick="getMenu()">
	</div>
</body>
</html>
