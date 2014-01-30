<?PHP

require_once('databaseSetup.php');

$getTagQuery = "SELECT * FROM tagAdClips WHERE TGA_TGI_id = " . $_POST['tagID'];
$getTagResult = dbQuery($getTagQuery);
$tagInfo = mysql_fetch_array($getTagResult);

//If no data was returned, return NULL.
if (!$tagInfo) {exit(0);}

echo $tagInfo['TGA_imageFile'];
