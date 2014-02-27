<?PHP

function getUSNPLNewsSites($stateAbbreviations) {
	
	$finalDomainsList = array();
	foreach ($stateAbbreviations as $curAbbreviation) {
		
		//Grab the link info from the state
		$stateDomains = getStateSites($curAbbreviation);
		
		$finalDomainsList = array_merge($finalDomainsList, $stateDomains);
	}
	
	//Return the array of domain => count
	return $finalDomainsList;
}

function getStateSites($stateAbbreviation) {

	echo "Currenent state: $stateAbbreviation \n";
	
	//Create the query string
	$usnplSite = 'http://www.usnpl.com/';
	$newsQueryString = strtolower($stateAbbreviation) . "news.php";
	$tvQueryString = "tv/" . strtolower($stateAbbreviation) . "tv.php";
	$radioQueryString = "radio/" . strtolower($stateAbbreviation) . "radio.php";
	$collegeQueryString = "college/" . strtolower($stateAbbreviation) . "coll.php";
	
	//Get the html from USNPL
	$newsHTML = getPageHTML($usnplSite . $newsQueryString);
	$tvHTML = getPageHTML($usnplSite . $tvQueryString);
	$radioHTML = getPageHTML($usnplSite . $radioQueryString);
	$collegeHTML = getPageHTML($usnplSite . $collegeQueryString);
	
	//-----------------------------------------------------------------------------------
	//------------------------Too tired to do this right-----------------------
	//-----------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------
	
	
	//Find the news site URLs in the page
	preg_match_all('@<b>(\w*?)</b>\s*?\&nbsp\s*?<a href="(.*?)">(.*?)</a>@s', $newsHTML, $newsSites);
	preg_match_all('@<b>(\w*?)</b>\s*?\&nbsp\s*?<a href="(.*?)">(.*?)</a>@s', $tvHTML, $tvSites);
	preg_match_all('@<b>(\w*?)</b>\s*?\<a href="(.*?)">(.*?)</a>@s', $radioHTML, $radioSites);
	preg_match_all('@<b>(\w*?)</b>\s*?\&nbsp\s*?<a href="(.*?)">(.*?)</a>@s', $collegeHTML, $collegeSites);
		
	
	//Create the array of domain => (siteLocation, siteName, siteURL)
	$siteInfoArray = array();
	foreach($newsSites[2] as $curKey => $curSite) {
		
		//Cleane the link and if it works, add it to the array
		$cleanLink = parse_url($curSite, PHP_URL_HOST);
		if ($cleanLink) {
			$siteInfoArray[$cleanLink] = array(
											'siteDomain' => $cleanLink,
											'siteLocation' => $newsSites[1][$curKey],
											'siteName' => $newsSites[3][$curKey],
											'type' => 'news');
		}
	}
	foreach($tvSites[2] as $curKey => $curSite) {
		
		//Cleane the link and if it works, add it to the array
		$cleanLink = parse_url($curSite, PHP_URL_HOST);
		if ($cleanLink) {
			$siteInfoArray[$cleanLink] = array(
											'siteDomain' => $cleanLink,
											'siteLocation' => $tvSites[1][$curKey],
											'siteName' => $tvSites[3][$curKey],
											'type' => 'tv');
		}
	}
	foreach($radioSites[2] as $curKey => $curSite) {
		
		//Cleane the link and if it works, add it to the array
		$cleanLink = parse_url($curSite, PHP_URL_HOST);
		if ($cleanLink) {
			$siteInfoArray[$cleanLink] = array(
											'siteDomain' => $cleanLink,
											'siteLocation' => $radioSites[1][$curKey],
											'siteName' => $radioSites[3][$curKey],
											'type' => 'radio');
		}
	}
	foreach($collegeSites[2] as $curKey => $curSite) {
		
		//Cleane the link and if it works, add it to the array
		$cleanLink = parse_url($curSite, PHP_URL_HOST);
		if ($cleanLink) {
			$siteInfoArray[$cleanLink] = array(
											'siteDomain' => $cleanLink,
											'siteLocation' => $collegeSites[1][$curKey],
											'siteName' => $collegeSites[3][$curKey],
											'type' => 'college');
		}
	}
		
	//Return the cleaned domains array
	return $siteInfoArray;
}

function getStateAbbreviationsFromCSV() {
	return getAssociativeArrayFromCSV("stateAbbreviations.csv");
}


?>