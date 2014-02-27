<?PHP

/**
* Gets all of the lists from the submitted URL. The final object is an array of
* lists. Each list has an array of associated data ('title' => listTitle, 'link' => listLink).
*
* @param string $url  	    URL to parse for unordered lists
* @return mixed  		 	Array of list information
*/
function getLists($url, $html = "") {

	//Grab the url html and load it into a domdocument.
	$receivedHTML = ($html) ? $html : getPageHTML($url);
	$targetHTML = new DOMDocument;
	$targetHTML->loadHTML($receivedHTML);	
	$targetXPath = new DOMXpath($targetHTML);
	
	//Get all the lists of anchors
	$ulLists = getULAnchorElements($targetHTML, $targetXPath);
	$divLists = getDIVListAnchorElements($targetHTML, $targetXPath);
	$anchorLists = array_merge($ulLists, $divLists);
	//$anchorLists = $ulLists;
	
	//Loop through the lists and create an array as described above as the output
	$finalArrayOfAnchorInfoArrays = array();
	foreach($anchorLists as $curAnchorList) {
	
		//Loop through the list and add each validated anchor to an array
		$curListArray = array();
		foreach($curAnchorList as $curAnchor) {
		
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
			//$anchorLabel = trim($anchorLabel);
			$anchorLabel = trim(str_replace("\n", ' ', $anchorLabel)); 
			if (($anchorLabel != "") && ($anchorHREF != "")) {
				$curListArray[] = array('title' => $anchorLabel, 'link' => $anchorHREF);
			}
		}
		
		//Add the array of UL LI info to the final return array 
		$finalArrayOfAnchorInfoArrays[] = $curListArray;
	}
	
	//Return the array of UL objects, if any exist
	return $finalArrayOfAnchorInfoArrays;
}

//Searches the current url html for div lists. This is where a div has underneath it equal divs and anchors.
function getDIVListAnchorElements($targetDOMDocument, $targetDOMXPath) {

	//Grab all the divs. Loop through and look for child divs and anchor counts that are equal.
	$arrayOfDivLists = array();
	$allDIVElements = $targetDOMDocument->getElementsByTagName('div');
	foreach ($allDIVElements as $curDiv) {
	
		//Grab the child divs and anchors
		$divList = $targetDOMXPath->query('div', $curDiv);
		$anchorList = $targetDOMXPath->query('a', $curDiv);
		
		//If the count of each is equal, store the anchor list
		if (count($divList) == count($anchorList)) {
			$arrayOfDivLists[] = $anchorList;
		}
	}
	//Return the array of UL objects, if any exist
	return $arrayOfDivLists;
}

function getULAnchorElements($targetDOMDocument, $targetDOMXPath) {

	//Grab all the unordered elements. Create an individual ul object and insert it into the final array.
	$arrayOfUILists = array();
	$allULElements = $targetDOMDocument->getElementsByTagName('ul');
	foreach ($allULElements as $curUL) {
	
		//Loop through the the list's items (li)
		$curLIList = array();
		foreach ($targetDOMXPath->query('li', $curUL) as $curLI) {
					
			//Grab the anchor for the menu item
			$anchorList = $curLI->getElementsByTagName('a');
			if ($anchorList->item(0)) {
				$curLIList[] = $anchorList->item(0);
			}
			
		}
		
		//Add the array of UL LI info to the final return array 
		$arrayOfUILists[] = $curLIList;
	}
	
	//Return the array of UL lists, if any exist
	return $arrayOfUILists;
}

/**
* Returns an array of rankings respective to each list passed. 
* The single parameter is an array of lists as defined by getLists(...).
* The array indices are identical to those of the passed array. 
*
* @param string $foundLists	  	    	Array of lists
* @return mixed  		 				Array of list rankings
*/
function getListRankings($foundLists) {

	//Get the positive and negative rankings
	$positiveTitles = getPositiveTitles();
	$negativeTitles = getNegativeTitles();

	//Loop through the lists and rank each accordingly
	$listRankings = array();
	foreach($foundLists as $currentList) {
	
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

function getListsFromManyURLs($urls, $menuDepth = null) {

	//Create a standard index array with the urls
	$urls = array_values($urls);
	
	//Grab the html responses from all the URLs at once
	$responses = getMultipleURLResponses($urls);
	
	//Get the lists from the pages
	$allLists = array();
	foreach($responses as $curIndex => $curHTML) {
	
		//If the HTML is missing (probably connect timeout), refetch it
		if (!$curHTML) {$curHTML = getPageHTML($urls[$curIndex]); echo "-Refetched " . $urls[$curIndex] . "\n";}
		
		//Get all the lists from the currently passed HTML page
		$foundLists = getLists(null, $curHTML);	
		
		//If a depth was passed, rank the lists and only send the 'menuDepth' highest

		if ($menuDepth) {
			$listRankings = getListRankings($foundLists);
			$rankedLists = array();
			arsort($listRankings);
			foreach($listRankings as $indexKey => $curRank) {
				$rankedLists[] = $foundLists[$indexKey];
			}
			$foundLists	= array_splice($rankedLists, 0, $menuDepth);
		}		
		$allLists[$urls[$curIndex]] = $foundLists;
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

//Returns the HTML of the passed URL.
function getPageHTML($url, $postFields = null) {

	$session = getCurlHandle($url, $postFields);
	$responseText = curl_exec($session);
	curl_close($session);
	return $responseText;
}


//Returns a specific CURL handle to be used with a multiple request call
function getCurlHandle($url, $postFields = null) {
	
	//Initiate the session and set basic conditions
	$session = curl_init($url);
	curl_setopt($session, CURLOPT_HEADER, false);
	curl_setopt($session, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($session, CURLOPT_FAILONERROR, false);
	curl_setopt($session, CURLOPT_FOLLOWLOCATION, true);
	curl_setopt($session, CURLOPT_CONNECTTIMEOUT, 20);
	curl_setopt($session, CURLOPT_TIMEOUT, 20);
	
	//If post fields were passed, send them
	if ($postFields) {
		curl_setopt($session, CURLOPT_POST, true);
		curl_setopt($session, CURLOPT_POSTFIELDS, $postFields);	
	}
	
	return $session;
}


/*function getDivLists($url, $html = "") {

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
}		*/
/**
* Gets all of the unordered lists from the submitted URL. The final object is an array of
* ULs. Each UL has an array of associated LI data ('title' => curLITitle, 'link' => curLILink).
*
* @param string $url  	    URL to parse for unordered lists
* @return mixed  		 	Array of UL information
*/
/*function getLists($url, $html = "") {

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
		
			//If the list has a nested unordered list, set the overall label to the child's first list element
			$liLabel = $curLI->nodeValue;
			
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
*/
?>