<?PHP

function getNewsDomains($cityArray) {
	
	$finalDomainsList = array();
	foreach ($cityArray as $curCity) {
		
		//Grab the link domains from google for the city
		sleep(70 + rand(0, 60));
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
	$googleQueryString = "v=1.0&q=News+" . htmlentities(str_replace(" ", "+", $cityName)) . "&rsz=8&userip=" . getRandomIP();
	
	$responseJSON = getPageHTML($googleURL . $googleQueryString);
	$searchObject = json_decode($responseJSON);
	
	//echo "$cityName: ";
	//print_r($searchObject);
	echo $responseString = "$cityName: " . $searchObject->responseStatus . " (" . $searchObject->responseDetails . ") \n";
	echo "	URL: " . $googleURL . $googleQueryString . "  \n";
	file_put_contents(dirname(__FILE__) . '/results/responseList.txt', $responseString, FILE_APPEND);
		
	$foundLinks = array();
	if ($searchObject->responseStatus == 200) {
		foreach($searchObject->responseData->results as $curResult) {
			$foundLinks[] = $curResult->url;
		}
	}
	else {
		echo 'Error detected. Stopping'; exit();
	
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
	curl_setopt($session, CURLOPT_USERAGENT, 'Lynx/2.8.8dev.3 libwww-FM/2.14 SSL-MM/1.4.1');
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

function getRandomIP() {
	
		$ipArray = array(
			'22.81.19.74',
			'180.67.230.10',
			'101.8.96.251',
			'112.3.229.152',
			'155.60.162.118',
			'81.145.61.41',
			'83.171.29.254',
			'110.29.36.132',
			'109.54.206.35',
			'120.181.45.221',
			'188.141.217.45',
			'143.191.197.44',
			'251.105.162.77',
			'249.222.117.77',
			'139.145.76.248',
			'173.111.126.28',
			'165.77.63.31',
			'3.107.251.191',
			'247.213.235.135',
			'150.178.179.146',
			'28.86.222.22',
			'53.84.98.191',
			'229.174.185.148',
			'31.56.176.195',
			'132.238.225.135',
			'90.222.71.83',
			'180.52.218.76',
			'229.142.222.2',
			'227.189.23.26',
			'19.121.216.248'
		);
		
		return $ipArray[rand(0, (count($ipArray) - 1))];
}

?>