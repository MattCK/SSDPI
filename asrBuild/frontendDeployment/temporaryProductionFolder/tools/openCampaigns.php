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
<table>
	<tr>
		<td>Order ID: </td>
		<td><input id="orderID" type="text" value="1444827870"></td>
	</tr>
	<tr>
		<td>Line Item ID: </td>
		<td><input id="lineItemID" type="text" value="3060237990"></td>
	</tr>
</table>
<input type="button" onclick="openCampaigns()" value="Open Campaigns">

<script>

function openCampaigns() {
	let domains = document.getElementById("siteDomains").value.split(/[ ,]+/).filter(Boolean);
	let orderID = document.getElementById("orderID").value;
	let lineItemID = document.getElementById("lineItemID").value;
	domains.forEach(function(site) {

	  window.open('/mainApp.php?domain=' + site + '&orderID=' + orderID + '&lineItemID=' + lineItemID, '_blank');

	});
}
</script>