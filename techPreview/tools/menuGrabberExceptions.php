<?PHP

/**
* File to define paths, setup dependencies, and connect to database
*/
require_once('../systemSetup.php');

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');

use AdShotRunner\Menu\MenuGrabber;

if ($_POST['domain']) {

	//Remove the protocol from the domain if it exists
	$domain = preg_replace('#^https?://#', '', $_POST['domain']);

	ASRDatabase::executeQuery("DELETE FROM exceptionsMenuGrabberDomains WHERE EMD_domain = '$domain'");
	ASRDatabase::executeQuery("INSERT INTO exceptionsMenuGrabberDomains (EMD_domain) VALUES ('$domain')");
	$domainID = ASRDatabase::lastInsertID();

	//Create the value string of menu items
	$cleanMenuItems = [];
	foreach ($_POST['url'] as $urlIndex => $url) {

		if ($url) {

			//Remove the protocol from the url if it exists
			$url = preg_replace('#^https?://#', '', $url);

			$cleanMenuItems[] = "($domainID, '" . 
						  		  ASRDatabase::escape($_POST['label'][$urlIndex]) . "', '" . 
						  		  ASRDatabase::escape($url) . "')";
		}
	}
	$cleanMenuItemString = implode(',', $cleanMenuItems);


	if ($cleanMenuItemString) {
		ASRDatabase::executeQuery("INSERT IGNORE INTO exceptionsMenuGrabberItems (EMI_EMD_id, EMI_label, EMI_url) 
					   VALUES $cleanMenuItemString");
		echo "Query: " . "INSERT IGNORE INTO exceptionsMenuGrabberItems (EMI_EMD_id, EMI_label, EMI_url) 
					   VALUES $cleanMenuItemString";
	}

	echo "Inserted!!!<br><br>";
}

?>

<form action="menuGrabberExceptions.php" method="post">
	<strong>Domain: </strong><input type="text" name="domain" style="width:500px"><br><br>
	<table>
		<tr><th>Label</th><th>URL</th>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
		<tr><td><input type="text" name="label[]"></td><td><input type="text" name="url[]" style="width:700px"></td></tr>
	</table><br>
	<input type="submit" value="Submit">
</form> 