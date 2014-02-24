<?PHP

function getNewsDomains($cityArray) {
	
	$finalDomainsList = array();
	foreach ($cityArray as $curCity) {
		
		//Grab the link domains from google for the city
		sleep(60 + rand(0, 45));
		$cityDomains = getCityDomains($curCity);
		
		//For each domain, either add it to the array or increment it
		foreach ($cityDomains as $curDomain) {
			if ($finalDomainsList[$curDomain]) {
				$finalDomainsList[$curDomain] += 1;
			}
			else {
				$finalDomainsList[$curDomain] = 1;
			}
		}
		
		//---------------------------------------
		writeArrayToCSV(dirname(__FILE__) . '/results/runningWrite.csv', $finalDomainsList);
		//---------------------------------------
	}
	
	//Return the array of domain => count
	return $finalDomainsList;
}

function getCityDomains($cityName) {
	
	//Create the query string
	/*$googleURL = 'https://www.google.com/search?';
	$googleQueryString = "q=$cityName+news+-site:topix.com";
	
	//Get the html from google
	echo $responseHTML = getPageHTML($googleURL . $googleQueryString);
	print_r(json_decode($responseHTML));
		
	//Find the 10 needed URLs in the page
	preg_match_all('@<h3 class="r"><a href="(.*?)">@s', $responseHTML, $urlMatches);*/
	
	//Get the html from google
	$googleURL = 'https://ajax.googleapis.com/ajax/services/search/web?';
	$googleQueryString = "v=1.0&q=" . htmlentities(str_replace(" ", "+", $cityName)) . "+News&rsz=8";
	
	$responseJSON = getPageHTML($googleURL . $googleQueryString);
	$searchObject = json_decode($responseJSON);
	
	//echo "$cityName: ";
	//print_r($searchObject);
	echo $responseString = "$cityName: " . $searchObject->responseStatus . " (" . $searchObject->responseDetails . ") \n";
	//file_put_contents('responseList.txt', $responseString, FILE_APPEND);
	file_put_contents(dirname(__FILE__) . '/results/responseList.txt', $responseString, FILE_APPEND);
	//echo dirname(__FILE__). '/results/relativeTest.txt' . "\n\n";
		
	//echo $responseHTML = getPageHTML($googleURL . $googleQueryString);
	$foundLinks = array();
	if ($searchObject->responseStatus == 200) {
		foreach($searchObject->responseData->results as $curResult) {
			$foundLinks[] = $curResult->url;
		}
	}
	
	//print_r($foundLinks);
	
	//Create the array of domain => url
	$cleanDomains = array();
	foreach($foundLinks as $curLink) {
		
		//Cleane the link and if it works, add it to the array
		$cleanLink = parse_url($curLink, PHP_URL_HOST);
		//$cleanLink = parse_url(substr($curLink, 7), PHP_URL_HOST);
		if ($cleanLink) {$cleanDomains[] = $cleanLink;}
	}
	
	//Return the cleaned domains array
	return $cleanDomains;
}


//Returns the HTML of the passed URL.
function getPageHTML($url, $postFields = null) {

	$session = curl_init($url);
	curl_setopt($session, CURLOPT_HEADER, false);
	curl_setopt($session, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($session, CURLOPT_FAILONERROR, false);
	curl_setopt($session, CURLOPT_FOLLOWLOCATION, true);
	curl_setopt($session, CURLOPT_AUTOREFERER, true);
	curl_setopt($session, CURLOPT_COOKIESESSION, true);
	curl_setopt($session, CURLOPT_COOKIEFILE, 'cookies.txt');
	curl_setopt($session, CURLOPT_COOKIEJAR, 'cookies.txt');	
	curl_setopt($session, CURLOPT_USERAGENT, 'Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)');
	//curl_setopt($session, CURLOPT_REFERER, "https://www.google.com");	
	
	//If post fields were passed, send them
	if ($postFields) {
		curl_setopt($session, CURLOPT_POST, true);
		curl_setopt($session, CURLOPT_POSTFIELDS, $postFields);	
	}
	
	$responseText = curl_exec($session);
	curl_close($session);
	return $responseText;
}

//Returns an associative array of the CSV file with the first column as keys and second as values
function getAssociativeArrayFromCSV($csvFile) {

	//Attempt to open the file
	$csvArray = array();
	if (($csvFileHandle = fopen($csvFile, "r")) !== FALSE) {
	
		while (($currentRowData = fgetcsv($csvFileHandle, 1000, ",")) !== FALSE) {
			$csvArray[$currentRowData[0]] = $currentRowData[1];
		}
		
		fclose($csvFileHandle);
	}
	
	//If file couldn't be opened, show error and exit
	else {echo "CSV couldn't be opened"; exit();}
	
	//Return the associative array of CSV data
	return $csvArray;
}

//Simple function calls to clean up requests for the respective CSV information
function getCitiesFromCSV() {
	return getAssociativeArrayFromCSV("cities.csv");
}

function writeArrayToCSV($fileName, $arrayToWrite) {

	$fileHandler = fopen($fileName, 'w');

	foreach ($arrayToWrite as $curKey => $curValue) {
		fputcsv($fileHandler, array($curKey, $curValue));
	}

	fclose($fileHandler);

}

?>