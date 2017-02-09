<?PHP

require_once('../systemSetup.php');

use AdShotRunner\Database\ASRDatabase;

		$domainGrabResult = ASRDatabase::executeQuery("SELECT * FROM menuGrabberDomains");
		$domainList = array();
		while ($curRow = $domainGrabResult->fetch_assoc()) {
		    echo $curRow['MGD_domain'] . ", ";
		}
