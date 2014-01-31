<?PHP

require_once('databaseSetup.php');

$addTagQuery = "INSERT INTO tagInfo (TGI_name,
									TGI_fullTag,
									TGI_usedTag)
				 VALUES ('Proto Tag',
						'" . mysql_real_escape_string($_POST['tag']) . "',
						'" . mysql_real_escape_string($_POST['tag']) . "')";
dbQuery($addTagQuery);

$tagID = mysql_insert_id();

$session = curl_init('http://10.1.1.50/?tagID=' . $tagID);
curl_setopt($session, CURLOPT_HEADER, false);
curl_setopt($session, CURLOPT_RETURNTRANSFER, true);
curl_setopt($session, CURLOPT_FAILONERROR, false);
$responseText = curl_exec($session);
curl_close($session);

echo $tagID;