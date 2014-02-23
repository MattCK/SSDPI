<?PHP

/**
* Gets all of the unordered lists from the submitted URL. The final object is an array of
* ULs. Each UL has an array of associated LI data ('title' => curLITitle, 'link' => curLILink).
*
* @param string $url  	    URL to parse for unordered lists
* @return mixed  		 	Array of UL information
*/
function getUnorderedLists($url, $html = "") {

	//Grab the url html and load it into a domdocument.
	$receivedHTML = ($html) ? $html : getPageHTML($url);
	$targetHTML = new DOMDocument;
	$targetHTML->loadHTML($receivedHTML);	
	$targetXPath = new DOMXpath($targetHTML);

	//Grab all the unordered elements. Create an individual ul object and insert it into the final array.
	$finalArrayOfUIInfo = array();
	$allULElements = $targetHTML->getElementsByTagName('ul');
	foreach ($allULElements as $curUL) {
	
		//Loop through the the list's items (li)
		$curULObject = array();
		foreach ($targetXPath->query('li', $curUL) as $curLI) {
		//foreach ($curUL->getElementsByTagName('li') as $curLI) {
		
			//If the list has a nested unordered list, set the overall label to the child's first list element
			$liLabel = $curLI->nodeValue;
			//$childLIs = $curLI->getElementsByTagName('li');
			//if ($childLIs->item(0)) {$liLabel = $childLIs->item(0)->nodeValue;}	
			
			//Grab the anchor for the menu item
			$curAnchor = "";
			$anchorList = $curLI->getElementsByTagName('a');
			if ($anchorList->item(0)) {
				$liLabel = $anchorList->item(0)->nodeValue;
				$curAnchor = $anchorList->item(0)->getAttribute('href');
			}
			
			//If there is no value, grab the alt from the image if it exists
			if ($liLabel == "") {
				$imageList = $curLI->getElementsByTagName('img');
				if ($imageList->item(0)) {
					$liLabel = $imageList->item(0)->getAttribute('alt');
				}
			}
			
			//Insert the label and anchor information to return
			$liLabel = trim($liLabel);
			if (($liLabel != "") && ($curAnchor != "")) {
				$curULObject[] = array('title' => $liLabel, 'link' => $curAnchor);
			}
		}
		
		//Add the array of UL LI info to the final return array 
		$finalArrayOfUIInfo[] = $curULObject;
	}
	
	//Return the array of UL objects, if any exist
	return $finalArrayOfUIInfo;
}


/**
* Returns an array of rankings respective to each list passed. 
* The single parameter is an array of lists as defined by getUnorderedLists(...).
* The array indices are identical to those of the passed array. 
*
* @param string $unorderedLists  	    Array of unordered lists
* @return mixed  		 				Array of list rankings
*/
function getUnorderedListRankings($unorderedLists) {

	//Get the positive and negative rankings
	$positiveTitles = getPositiveTitles();
	$negativeTitles = getNegativeTitles();

	//Loop through the lists and rank each accordingly
	$listRankings = array();
	foreach($unorderedLists as $currentList) {
	
		//Only consider the first 25 elements. Penalize if more exist.
		$curRanking = 0;
		$usedWords = array();
		$listCount = count($currentList);
		if ($listCount > 25) {$curRanking += -20; $listCount = 25;}
		
		//Loop through items. Matching titles modify the overall ranking.
		for ($index = 0; $index < $listCount; ++$index) {
		
			$currentItem = $currentList[$index];
			
			foreach($positiveTitles as $curPosTitle => $curPosRanking) {
				if ((!$usedWords[$curPosTitle]) && (strcasecmp($currentItem['title'], $curPosTitle)) == 0) {
					$curRanking += $curPosRanking;
					$usedWords[$curPosTitle] = true;
				}
			}
			foreach($negativeTitles as $curNegTitle => $curNegRanking) {
				if ((!$usedWords[$curNegTitle]) && (strcasecmp($currentItem['title'], $curNegTitle)) == 0) {
					$curRanking += $curNegRanking;
					$usedWords[$curNegTitle] = true;
				}
			}
		}
		
		//Finally, add the ranking to the returnable array
		$listRankings[] = $curRanking;
	}
	
	//Return the final array of rankings
	return $listRankings;
}
/**
* Returns an array of rankings respective to each list passed. 
* The single parameter is an array of lists as defined by getUnorderedLists(...).
* The array indices are identical to those of the passed array. 
*
* @param string $unorderedLists  	    Array of unordered lists
* @return mixed  		 				Array of list rankings
*/
function getAnchorListRankings($anchorLists) {

	//Get the positive and negative rankings
	//$positiveTitles = getPositiveTitles();
	//$negativeTitles = getNegativeTitles();

	//Loop through the lists and rank each accordingly
	$listRankings = array();
	foreach($anchorLists as $currentAnchor) {
	
		//Only consider the first 25 elements. Penalize if more exist.
		$curRanking = 0;
		$usedWords = array();
		$anchorCount = count($currentAnchor);
		/*
		if ($currentAnchor['title'] == "NBC 5 Investigates"){
		$curRanking = 11;
		}
		if ($currentAnchor['title'] == "Video"){
		$curRanking = 12;
		}
		*/
		if ( strlen($currentAnchor['title']) > 80){
		$curRanking = $curRanking + 10;
		}
		
		

		
		
		//Finally, add the ranking to the returnable array
		$listRankings[] = $curRanking;
	}
	
	//Return the final array of rankings
	return $listRankings;
}

//Returns the HTML of the passed URL.
function getPageHTML($url) {

	$session = curl_init($url);
	curl_setopt($session, CURLOPT_HEADER, false);
	curl_setopt($session, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($session, CURLOPT_FAILONERROR, false);
	curl_setopt($session, CURLOPT_FOLLOWLOCATION, true);
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
function getSiteURLs() {
	return getAssociativeArrayFromCSV("publishersList.csv");
}
function getPositiveTitles() {
	return getAssociativeArrayFromCSV("SectionHeadings.csv");
}
function getNegativeTitles() {
	return getAssociativeArrayFromCSV("UnrelatedWords.csv");
}

function getListsFromManyURLs($urls) {
	$urls = array_values($urls);
	$responses = getMultipleURLResponses($urls);
	$allLists = array();
	foreach($responses as $curIndex => $curHTML) {
		if (!$curHTML) {$curHTML = getPageHTML($urls[$curIndex]); echo "-Refetched " . $urls[$curIndex] . "\n";}
		$allLists[$urls[$curIndex]] = getUnorderedLists(null, $curHTML);
	}
	return $allLists;
}

function getMultipleURLResponses($urlArray) {
	
	//First, create the curl multi-handle
	$multiHandle = curl_multi_init();

	//Create a handle for each URL and add it to the multi-handle
	$allHandles = array();
	foreach ($urlArray as $curURL) {
		$curURLHandle = getCurlHandle($curURL);
		curl_multi_add_handle($multiHandle, $curURLHandle);
		$allHandles[] = $curURLHandle;
	}
	
	//Execute the multi-handle call
	$isRunning = null;
	do {
		curl_multi_exec($multiHandle, $isRunning);
		usleep (250000); //Sleep for 0.25 seconds to reduce load
	} while ($isRunning > 0);

	//Store the response info for each handle
	$responsesArray = array();
	foreach ($allHandles as $curHandle) {
	
		//Decode and store the response
		$responsesArray[] = curl_multi_getcontent($curHandle);

		//Remove the handle from the multi-handle and close it.
		curl_multi_remove_handle($multiHandle, $curHandle);
		curl_close($curHandle);
	}
	
	//Close the multi-handle for resources
	curl_multi_close($multiHandle);
	
	//Return the array of learning info
	return $responsesArray;	
}

//Returns a specific CURL handle to be used with a multiple request call
function getCurlHandle($url, $postFields = null) {
	
	//Initiate the session and set basic conditions
	$session = curl_init($url);
	curl_setopt($session, CURLOPT_HEADER, false);
	curl_setopt($session, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($session, CURLOPT_FAILONERROR, false);
	curl_setopt($session, CURLOPT_FOLLOWLOCATION, true);
	curl_setopt($session, CURLOPT_CONNECTTIMEOUT, 0);
	curl_setopt($session, CURLOPT_TIMEOUT, 900);
	
	//If post fields were passed, send them
	if ($postFields) {
		curl_setopt($session, CURLOPT_POST, true);
		curl_setopt($session, CURLOPT_POSTFIELDS, $postFields);	
	}
	
	return $session;
}


function getDivLists($url, $html = "") {

	//Grab the url html and load it into a domdocument.
	$receivedHTML = ($html) ? $html : getPageHTML($url);
	$targetHTML = new DOMDocument;
	$targetHTML->loadHTML($receivedHTML);	
	$targetXPath = new DOMXpath($targetHTML);

	//Grab all the unordered elements. Create an individual ul object and insert it into the final array.
	$finalArrayOfUIInfo = array();
	$allULElements = $targetHTML->getElementsByTagName('div');
	foreach ($allULElements as $curDiv) {
	
		//Loop through the the list's items (li)
		$curDivObject = array();
		$divList = $targetXPath->query('div', $curDiv);
		$anchorList = $targetXPath->query('a', $curDiv);
		
		if (count($divList) == count($anchorList)) {
			foreach($anchorList as $curAnchor) {
				$anchorHREF = $curAnchor->getAttribute('href');
				$anchorLabel = $curAnchor->nodeValue;
				
				//If there is no value, grab the alt from the image if it exists
				if ($anchorLabel == "") {
					$imageList = $curAnchor->getElementsByTagName('img');
					if ($imageList->item(0)) {
						$anchorLabel = $imageList->item(0)->getAttribute('alt');
					}
				}
				
				//Insert the label and anchor information to return
				$anchorLabel = trim($anchorLabel);
				if (($anchorLabel != "") && ($anchorHREF != "")) {
					$curDivObject[] = array('title' => $anchorLabel, 'link' => $anchorHREF);
				}

				//Add the array of UL LI info to the final return array 
				$finalArrayOfDivInfo[] = $curDivObject;
			}
		}
	}
	//Return the array of UL objects, if any exist
	return $finalArrayOfDivInfo;
}

function getAnchorLists($url, $html = "") {

	//Grab the url html and load it into a domdocument.
	$receivedHTML = ($html) ? $html : getPageHTML($url);
	$targetHTML = new DOMDocument;
	$targetHTML->loadHTML($receivedHTML);	
	$targetXPath = new DOMXpath($targetHTML);

	//Grab all the unordered elements. Create an individual ul object and insert it into the final array.
	$finalArrayOfAnchorInfo = array();
	$allAnchorElements = $targetHTML->getElementsByTagName('a');

	foreach ($allAnchorElements as $curAnchor) {
	
		$curAnchorObject = array();
		
		$anchorHREF = $curAnchor->getAttribute('href');
		$anchorLabel = $curAnchor->nodeValue;
		
		//Insert the label and anchor information to return
		$anchorLabel = trim($anchorLabel);
		if (($anchorLabel != "") && ($anchorHREF != "")) {
			$curAnchorObject = array('title' => $anchorLabel, 'link' => $anchorHREF);
		}

		$titleMatch = false;
		foreach($finalArrayOfAnchorInfo as $asdfasdf){
			if (((strcasecmp($asdfasdf['title'], $anchorLabel)) == 0) || (strcasecmp($asdfasdf['link'], $anchorHREF) == 0)){
				$titleMatch = true;
			}
		}
		//Add the array of UL LI info to the final return array 
		if (($curAnchorObject != null) && ($titleMatch == false)){
			$finalArrayOfAnchorInfo[] = $curAnchorObject;
		}
		
		
	}
	return $finalArrayOfAnchorInfo;
}		

?>