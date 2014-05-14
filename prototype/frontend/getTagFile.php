<?PHP

require_once('databaseSetup.php');

$getTagQuery = "SELECT * FROM tagAdClips WHERE TGA_TGI_id = " . $_GET['tagID'];
$getTagResult = dbQuery($getTagQuery);
$tagInfo = mysql_fetch_array($getTagResult);

//If no data was returned, return NULL.
if (!$tagInfo) {exit(0);}

echo 'adClips/' . $tagInfo['TGA_imageFile'];

exit(0);

$fileURI = '/var/www/adClips/' . $_GET['tagID'] . '.png';
if (file_exists($fileURI)) {
	echo '/adClips/' . $_GET['tagID'] . '.png';
}
else {
	exit(0);
}
exit(0);

$fileURI = 'http://10.1.1.59/tempClips/' . $_GET['tagID'] . '.png';
if (checkurl($fileURI)) {
	echo $fileURI;
	exit(0);
}
else {
	//echo 'no file. looking for: ' . $fileURI; 
	exit(0);
}


function url_exists($url) {
    if (!$fp = curl_init($url)) return false;
    return true;
}


 function checkurl($url)
{
    // Simple check
    if (!$url)
    {
        return FALSE;
    }

    // Create cURL resource using the URL string passed in
    $curl_resource = curl_init($url);

    // Set cURL option and execute the "query"
    curl_setopt($curl_resource, CURLOPT_RETURNTRANSFER, true);
    curl_exec($curl_resource);

    // Check for the 404 code (page must have a header that correctly display 404 error code according to HTML standards
    if(curl_getinfo($curl_resource, CURLINFO_HTTP_CODE) == 404)
    {
        // Code matches, close resource and return false
        curl_close($curl_resource);
        return FALSE;
    }
    else
    {
        // No matches, close resource and return true
        curl_close($curl_resource);
        return TRUE;
    }

    // Should never happen, but if something goofy got here, return false value
    return FALSE;
}

$fileURI = '/var/www/sshfs/adClips/' . $_GET['tagID'] . '.png';
if (file_exists($fileURI)) {
	echo '/sshfs/adClips/' . $_GET['tagID'] . '.png';
}
else {
	echo 'no file. looking for: ' . $fileURI; 
	exit(0);
}

