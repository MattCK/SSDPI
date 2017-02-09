<?PHP

/**
* File to define paths, setup dependencies, and connect to database
*/
require_once('../systemSetup.php');

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');

?>

Publisher Domains (Comma or whitespace delineated)<br><br>
<textarea id="siteDomains" rows="4" cols="50"></textarea><br>
Order ID: <input id="orderID" type="text">
<input type="button" onclick="openCampaigns()" value="Open Campaigns">

<script>

function openCampaigns() {
	let domains = document.getElementById("siteDomains").value.split(/[ ,]+/).filter(Boolean);
	let orderID = document.getElementById("orderID").value;
	domains.forEach(function(site) {

	  window.open('/mainApp.php?domain=' + site + '&orderID=' + orderID, '_blank');

	});
}
</script>