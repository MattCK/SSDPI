<?PHP
/**
* Menu Grabber testing page
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

let domainMenus = {};
let menuIndex = 0;

function getDomainMenus() {

	//Create the callback function that will show the table
	let callback = function(response) {
		
		base.enable("getMenusButton");
		if (response.success) {
			

			if (response.data.length > 0) {

				domainMenus = response.data;

				base.nodeFromID("domainName").innerHTML = domainMenus[0].domain;
				base.nodeFromID("domainMenus").innerHTML = domainMenus[0].menus;
				base.nodeFromID("domainLink").href = "http://" + domainMenus[0].domain;
				base.nodeFromID("domainIFrame").src = "http://" + domainMenus[0].domain;

				menuIndex = 0;

			}
		}
					
		//If failure, show us the message returned from the server and focus on the selected element if returned. Also, re-enable the submit button.
		else {
			alert(response.message);
		}
	}
	
	//Make the request
	base.disable("getMenusButton");
	base.asyncRequest("getDomainMenus.php", 'domains=' + base.nodeFromID('domainList').value, callback);
}

function rotatePage(rotateAmount) {

	menuIndex += rotateAmount;
	if (menuIndex < 0) {menuIndex = domainMenus.length - 1;}
	else if (menuIndex >= domainMenus.length) {menuIndex = 0;}

	base.nodeFromID("domainName").innerHTML = domainMenus[menuIndex].domain;
	base.nodeFromID("domainMenus").innerHTML = domainMenus[menuIndex].menus;
	base.nodeFromID("domainLink").href = "http://" + domainMenus[menuIndex].domain;
	base.nodeFromID("domainIFrame").src = "http://" + domainMenus[menuIndex].domain;
}

</script>
<body>

<div>

	Domains: 
	<textarea id="domainList" style="height: 32px; width: 950px;"></textarea>
	<input id="getMenusButton" type="button" value="Get Menus" onclick="getDomainMenus()">
</div>

<hr/>

<div style="height: 90px; overflow-y: scroll; margin-bottom: 10px;">
	<span id="domainName" style="font-weight: bold;">Domain Name</span>
	&nbsp;
	<a href="#" onclick="rotatePage(-1)">Previous</a>
	&nbsp;
	<a href="#" onclick="rotatePage(1)">Next</a>
	&nbsp;
	<a id="domainLink" href="#" >Link</a>
	<br>
	<span id="domainMenus">menu one<br>menu two<br>menu three<br>menu four<br>menu five<br></span>
</div>

<iframe id="domainIFrame" style="position: absolute; height: 100%; width: 100%; border: none;"></iframe>

</body>
</html>
