<?PHP

function getUSNPLNewsSites($stateAbbreviations) {
	
	$finalDomainsList = array();
	foreach ($stateAbbreviations as $curAbbreviation) {
		
		//Grab the link info from the state
		$stateDomains = getStateSites($curAbbreviation);
		
		$finalDomainsList = array_merge($finalDomainsList, $stateDomains);
		
		//For each domain, either add it to the array or increment it
		/*foreach ($stateDomains as $curDomain) {
			if ($finalDomainsList[$curDomain]) {
				$finalDomainsList[$curDomain] += 1;
			}
			else {
				$finalDomainsList[$curDomain] = 1;
			}
		}*/
		
		//---------------------------------------
		//writeArrayToCSV('runningWrite.csv', $finalDomainsList);
		//---------------------------------------
	}
	
	//Return the array of domain => count
	return $finalDomainsList;
}

function getStateSites($stateName) {

	echo "Currenent state: $stateName \n";
	
	//Create the query string
	$usnplSite = 'http://www.usnpl.com/';
	$usnplQueryString = strtolower($stateName) . "news.php";
	
	//Get the html from USNPL
	$responseHTML = getPageHTML($usnplSite . $usnplQueryString);
	//print_r(json_decode($responseHTML));
		
	//Find the news site URLs in the page
	preg_match_all('@<b>(\w*?)</b>\s*?\&nbsp<a href="(.*?)">(.*?)</a>@s', $responseHTML, $sitesFound);
	//print_r($siteMatches);exit();
	

	
	//echo "$cityName: ";
	//print_r($searchObject);
	/*echo $responseString = "$cityName: " . $searchObject->responseStatus . " (" . $searchObject->responseDetails . ") \n";
	file_put_contents('responseList.txt', $responseString, FILE_APPEND);
	file_put_contents(dirname(__FILE__). 'results/relativeTest.txt', $responseString, FILE_APPEND);
	//echo dirname(__FILE__). '/results/relativeTest.txt' . "\n\n";
		
	//echo $responseHTML = getPageHTML($googleURL . $googleQueryString);
	$foundLinks = array();
	foreach($searchObject->responseData->results as $curResult) {
		$foundLinks[] = $curResult->url;
	}*/
	
	//print_r($foundLinks);
	
	//Create the array of domain => (siteLocation, siteName, siteURL)
	$siteInfoArray = array();
	foreach($sitesFound[2] as $curKey => $curSite) {
		
		//Cleane the link and if it works, add it to the array
		$cleanLink = parse_url($curSite, PHP_URL_HOST);
		if ($cleanLink) {
			$siteInfoArray[$cleanLink] = array(
											'siteDomain' => $cleanLink,
											'siteLocation' => $sitesFound[1][$curKey],
											'siteName' => $sitesFound[3][$curKey]);
		}
	}
	
	//print_r($siteInfoArray); exit();
	
	//Return the cleaned domains array
	return $siteInfoArray;
}

function getStateAbbreviationsFromCSV() {
	return getAssociativeArrayFromCSV("stateAbbreviations.csv");
}


?>