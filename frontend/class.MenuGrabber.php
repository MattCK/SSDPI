<?PHP
/*
*
* @package package
* @section section
* @page page
*/


require_once('class.WebCommunicator.php');

/**
* The Menu Grabber attempts to grab possible menus from a given webpage or webpages.
* It then scores each possible menu according to the predefined label weights in the
* database. The results are returned to the caller.
*
* Menus are determined by two methods: 1) Unordered lists and 2) Elements that have an 
* equal amount of anchors and divs in them.
*
* Results are automatically stored in the database and cached results given when 
* applicable. 
*
* This class requires the MySQLDatabase global $database to be instantiated. 
*
* @package package
* @section section
* @page page
*
*/
class MenuGrabber {


	//--------------------------------------------------------------------------------------
	//---------------------------------- Static Methods ------------------------------------
	//--------------------------------------------------------------------------------------
	//***************************** Public Static Methods **********************************


	//***************************** Private Static Methods *********************************



	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	* @var mixed		Associative array of label => weight.
	*/
	private $_menuWeights;






	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Instantiates class and grabs the menu label weights from the database.
	*
	* This class requires the MySQLDatabase global $database to be instantiated. 
	*/
	function __construct() {
		
		//Retrieve and store the menu label weight information
		$this->setMenuLabelWeights($this->retrieveMenuLabelWeightsFromDatabase());
	}





	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods *************************************
	/**
	* Returns a list of ranked menus sorted from highest score to lowest.
	*
	* The returned array consists for each key of associated arrays with two keys:
	* 'score' pointing to the numeric score of the menu and 'items' for the menu itself.
	* The 'items' points to an array of items. Each item is an associative array as
	* 'label' pointing to an individual label and 'url' pointing to that label's link.
	*
	* The key of the top-most array is the URL string.
	*
	* Array[url] -> 'score' => 12
	*		     -> 'items' => Array -> 'label'
	*			        			 -> 'url'
	*	
	* On failure, NULL is returned.
	*
	* @param 	mixed 		$domains 	String or array of the domains to get menus for
	* @retval 	mixed  					Array of ranked menus or NULL on failure
	*/
	public function getDomainMenus($domains) {
		
		//If necessary, make the passed variable an array
		if (!is_array($domains)) {$domains = [$domains];}

		//Check the database to see if the menus exist there
		$databaseDomainMenus = $this->retrieveManyDomainMenusFromDatabase($domains);

		//Loop through the domains and see if any don't exist in the database
		$missingDomains = array();
		foreach ($domains as $curDomain) {

			//Mark if the domain is not in the database finds.
			if (!array_key_exists($curDomain, $databaseDomainMenus)) {
				$missingDomains[] = $curDomain;
			}
		}

		//Grab the menus from the web, if necessary, and store them
		$webDomainMenus = array();
		if (count($missingDomains > 0)) {

			//Request the menus
			$webDomainMenus = $this->getRankedMenusFromManyURLs($missingDomains);

			//If any were found, insert them into the database
			if (count($webDomainMenus > 0)) {
				$this->insertDomainsWithMenus($webDomainMenus);
			}
		}

		//Merge the two sets of menu data and return it
		$finalDomainMenus = array_merge($databaseDomainMenus, $webDomainMenus);
		return $finalDomainMenus;
	}



	//******************************** Protected Methods ************************************

	/**
	* Returns a list of ranked possible menus sorted from highest score to lowest.
	*
	* The returned object consists of an array of associated arrays with two keys:
	* 'score' pointing to the numeric score of the menu and 'items' for the menu itself.
	* The 'items' points to an array of items. Each item is an associative array as
	* 'label' pointing to an individual label and 'url' pointing to that label's link.
	*
	* Array-> 'score' => 12
	*	   -> 'items' => Array -> 'label'
	*			        	    -> 'url'
	*	
	* On failure, NULL is returned.
	*
	* @param 	string 	$url 	URL to get menus from
	* @retval 	mixed  			Array of ranked menus or NULL on failure
	*/
	protected function getRankedMenusFromURL($url) {
		
		//Get the menus for the URL
		$urlMenus = $this->getRankedMenusFromManyURLs([$url]);

		//Return the menus only (not the topmost associative array)
		return $urlMenus[$url];
	}

	/**
	* Returns a list of ranked menus sorted from highest score to lowest
	* from many URLs.
	*
	* The returned array consists for each key of associated arrays with two keys:
	* 'score' pointing to the numeric score of the menu and 'items' for the menu itself.
	* The 'items' points to an array of items. Each item is an associative array as
	* 'label' pointing to an individual label and 'url' pointing to that label's link.
	*
	* The key of the top-most array is the URL string.
	*
	* Array[url] -> 'score' => 12
	*		     -> 'items' => Array -> 'label'
	*			        			 -> 'url'
	*	
	* On failure, NULL is returned.
	*
	* @param 	array 	$urls 	URLs to get menus from
	* @retval 	mixed  			Array of ranked menus or NULL on failure
	*/
	protected function getRankedMenusFromManyURLs($urls) {
		
		//Get the HTML response from the passed URL
		$URLCommunicator = new WebPageCommunicator();
		$urlResponses = $URLCommunicator->getManyURLResponses($urls);

		//Loop through each of the URL responses and build a final array
		$urlMenus = [];
		foreach ($urlResponses as $currentURL => $currentHTML) {

			//Grab the possible menus
			$possibleMenus = $this->grabPossibleMenusFromHTML($currentHTML);

			//Create a final array with the menu items and its overall score
			$rankedMenus = [];
			foreach ($possibleMenus as $curMenu) {
				$rankedMenus[] = ['score' => $this->scoreMenu($curMenu), 'items' => $curMenu];
			}

			//Sort the results to put the highest scores first in the array
			usort($rankedMenus, function($a, $b) {
			    return $b['score'] - $a['score'];
			});

			//Put the url menus into the final array
			$urlMenus[$currentURL] = $rankedMenus;
		}

		//Return the results
		return $urlMenus;
	}

	/**
	* Returns a list of ranked possible menus sorted from highest score to lowest out of the database.
	*
	* The returned object consists of an array of associated arrays with two keys:
	* 'score' pointing to the numeric score of the menu and 'items' for the menu itself.
	* The 'items' points to an array of items. Each item is an associative array as
	* 'label' pointing to an individual label and 'url' pointing to that label's link.
	*
	* Array-> 'score' => 12
	*	   -> 'items' => Array -> 'label'
	*			        	    -> 'url'
	*	
	* On failure, NULL is returned.
	*
	* @param 	string 		domain 		Domain to get menu for
	* @retval 	mixed  					Array of ranked menus or empty array on failure
	*/
	protected function retrieveDomainMenusFromDatabase($domain) {
		
		//Get the menus out of the database
		$domainMenus = $this->retrieveManyDomainMenusFromDatabase([$domain]);

		//Return the menus only (not the topmost associative array)
		if (!array_key_exists($domain, $domainMenus)) {return array();}
		return $domainMenus[$domain];
	}

	/**
	* Returns a list of ranked menus sorted from highest score to lowest
	* from multiple domains out of the database.
	*
	* The returned array consists for each key of associated arrays with two keys:
	* 'score' pointing to the numeric score of the menu and 'items' for the menu itself.
	* The 'items' points to an array of items. Each item is an associative array as
	* 'label' pointing to an individual label and 'url' pointing to that label's link.
	*
	* The key of the top-most array is the URL string.
	*
	* Array[url] -> 'score' => 12
	*		     -> 'items' => Array -> 'label'
	*			        			 -> 'url'
	*	
	* On failure, an empty array is return.
	*
	* @param 	array 	$domains 	Domains to get menus from
	* @retval 	mixed  				Array of ranked menus or empty array on failure
	*/
	protected function retrieveManyDomainMenusFromDatabase($domains) {
		
		//If no domains were passed, return an empty array
		if ((!$domains) || (count($domains) < 1)) {return array();}

		//Clean up the domains for the query
		$cleanDomains = [];
		foreach ($domains as $curDomain) {$cleanDomains[] = "'" . databaseEscape($curDomain) . "'";}
		$cleanDomainString = implode(',', $cleanDomains);

		//Get the domains' menus from the database (if any exist)
		$menuResults = databaseQuery("	SELECT *
										FROM menuDomains
										LEFT JOIN menus ON MNU_MND_id = MND_id
										LEFT JOIN menuItems ON MNI_MNU_id = MNU_id
										WHERE MND_domain IN ($cleanDomainString)
										ORDER BY MNU_score DESC");

		//Create the final array and loop through the results, creating the menu structures
		$domainMenus = [];
		while ($curMenuItem = $menuResults->fetch_assoc()) {

			//If we haven't seen this domain yet, add it to the top level of the results
		    if (!array_key_exists($curMenuItem['MND_domain'], $domainMenus)) {
		    	$domainMenus[$curMenuItem['MND_domain']] = array();
		    }

		    //Same again but for the menu
		    if (!array_key_exists($curMenuItem['MNU_id'], $domainMenus[$curMenuItem['MND_domain']])) {
		    	$domainMenus[$curMenuItem['MND_domain']][$curMenuItem['MNU_id']] = array();
		    }

		    //Enter the menu score
		    $domainMenus[$curMenuItem['MND_domain']][$curMenuItem['MNU_id']]['score'] = $curMenuItem['MNU_score'];

		    //If the items array has not been added, do so.
		    if (!array_key_exists('items', $domainMenus[$curMenuItem['MND_domain']][$curMenuItem['MNU_id']])) {
		    	$domainMenus[$curMenuItem['MND_domain']][$curMenuItem['MNU_id']]['items'] = array();
		    }

		    //Finally, add the actual item
		    $domainMenus[$curMenuItem['MND_domain']][$curMenuItem['MNU_id']]['items'][] = ['label' => $curMenuItem['MNI_label'],
		    																   'url' => $curMenuItem['MNI_url']];
		}

		//Return the menus, if any exist
		return $domainMenus;
	}

	/**
	* Inserts the domain into the database with its associated menus and menu items.
	*
	* THIS WILL DELETE THE CURRENT DOMAIN AND CURRENT MENUS FROM THE DATABASE.
	*
	* The passed array is very specific. It is the structure as the returned array from getRankedMenusFromManyURLs(...).
	* The follows as:
	*
	* Array[url] -> 'score' => 12
	*		     -> 'items' => Array -> 'label'
	*			        			 -> 'url'
	*
	* @param 	array  	$domainInfo 		Array of domains and their associated menus. See main description for format.
	*/
	protected function insertDomainsWithMenus($domainInfo) {
		
		//Verify domain info was passed
		if ((!$domainInfo) || (count($domainInfo) < 1)) {return;}

		//Loop through each domain: run the delete command on it, add the domain, add the domain menus, add the menu items.
		foreach ($domainInfo as $curDomain => $domainMenus) {

			//Delete the domain, if it exists, in the database.
			$this->deleteDomain($curDomain);

			//Add the domain to the database
			$cleanDomainString = "'" . databaseEscape($curDomain) . "'";
			databaseQuery("INSERT INTO menuDomains (MND_domain) VALUES ($cleanDomainString)");

			//Get the domain insert ID
			$domainID = lastInsertID();

			//Loop through the menus, adding each to the database with their items
			foreach ($domainMenus as $curMenu) {

				//Insert the menu and its score
				$cleanScoreString = "'" . databaseEscape($curMenu['score']) . "'";
				databaseQuery("INSERT INTO menus (MNU_MND_id, MNU_score) VALUES ($domainID, $cleanScoreString)");

				//Get the menu insert ID
				$menuID = lastInsertID();

				//Create the value string of menu items
				$cleanMenuItems = [];
				foreach ($curMenu['items'] as $curItem) {
					$cleanMenuItems[] = "($menuID, '" . 
										  databaseEscape($curItem['label']) . "', '" . 
										  databaseEscape($curItem['url']) . "')";
				}
				$cleanMenuItemString = implode(',', $cleanMenuItems);

				//Insert the items into the database
				if ($cleanMenuItemString) {
					databaseQuery("INSERT INTO menuItems (MNI_MNU_id, MNI_label, MNI_url) 
								   VALUES $cleanMenuItemString");
				}
			}
		}
	}

	//********************************* Private Methods *************************************


	/**
	* Returns an array of possible menus from the passed HTML.
	*
	* The returned object is an array of menus. Each menu points to an array of items.
	* Each item is an associative array as 'label' pointing to an individual label and 
	* 'url' pointing to that label's link.
	*
	* Array => Array -> 'label'
	*				 -> 'url'
	*	
	* On failure, NULL is returned.
	*
	* @param 	string 	$pageHTML 	HTML to grab possible menus out of
	* @retval 	mixed  				Array of possible menus or NULL on failure
	*/
	private function grabPossibleMenusFromHTML($pageHTML) {
		
		//Parse the passed HTML into document objects for easy utilization.
		$htmlDocument = new DOMDocument;
		$htmlDocument->loadHTML($pageHTML);	
		$htmlXPath = new DOMXpath($htmlDocument);

		//Grab all the possible menus from the unique patterns and return their merged array
		$possibleULMenus = $this->grabPossibleULMenus($htmlDocument, $htmlXPath);
		$possibleDIVMenus = $this->grabPossibleDIVMenus($htmlDocument, $htmlXPath);
		return array_merge($possibleULMenus, $possibleDIVMenus);
	}


	/**
	* Returns an array of possible menus using the UL method from the passed HTML elements.
	*
	* The UL method is very simple: return all ULs with lis that contain an Anchor.
	*
	* The returned object is an array of menus. Each menu points to an array of items.
	* Each item is an associative array as 'label' pointing to an individual label and 
	* 'url' pointing to that label's link.
	*
	* Array => Array -> 'label'
	*				 -> 'url'
	*	
	* On failure, NULL is returned.
	*
	* @param 	DOMDocument 	$htmlDocument 	DOMDocument to traverse in order to find elements
	* @param 	DOMXpath 		$htmlXPath 		DOMXPath to search for groups of elements
	* @retval 	mixed  							Array of possible menus or NULL on failure
	*/
	private function grabPossibleULMenus($htmlDocument, $htmlXPath) {
		
		//Grab all the unordered list elements.
		$possibleULMenus = array();
		$allULElements = $htmlDocument->getElementsByTagName('ul');

		//For each unordered list, grab the anchor information and put them into an array
		foreach ($allULElements as $curUL) {
		
			//Loop through each unordered list and store the anchor information of each list item
			$curULAnchorInformation = array();
			foreach ($htmlXPath->query('li', $curUL) as $curLI) {
						
				//Grab the anchor for the menu item, if it exists
				$anchorList = $curLI->getElementsByTagName('a');
				if ($anchorList->item(0)) {
					
					//Get the anchor's link and label
					$curAnchor = $anchorList->item(0);
					$anchorHREF = $curAnchor->getAttribute('href');
					$anchorLabel = $curAnchor->nodeValue;
					
					//If there is no label, grab the alt from the image if it exists
					if ($anchorLabel == "") {
						$imageList = $curAnchor->getElementsByTagName('img');
						if ($imageList->item(0)) {
							$anchorLabel = $imageList->item(0)->getAttribute('alt');
						}
					}
					
					//Insert the label and anchor information to return
					$anchorLabel = trim(str_replace("\n", ' ', $anchorLabel)); 
					if (($anchorLabel != "") && ($anchorHREF != "")) {
						$curULAnchorInformation[] = array('label' => $anchorLabel, 'url' => $anchorHREF);
					}
				}
			}
			
			//Add the array of UL LI info to the final return array if it is not empty
			if (count($curULAnchorInformation)) {$possibleULMenus[] = $curULAnchorInformation;}
		}
		
		//Return the array of UL lists, if any exist
		return $possibleULMenus;
	}

	/**
	* Returns an array of possible menus using the DIV method from the passed HTML elements.
	*
	* The DIV method consists of returning the labels and links from any element that
	* contains the same amount of DIVs and Anchors.
	*
	* The returned object is an array of menus. Each menu points to an array of items.
	* Each item is an associative array as 'label' pointing to an individual label and 
	* 'url' pointing to that label's link.
	*
	* Array => Array -> 'label'
	*				 -> 'url'
	*	
	* On failure, NULL is returned.
	*
	* @param 	DOMDocument 	$htmlDocument 	DOMDocument to traverse in order to find elements
	* @param 	DOMXpath 		$htmlXPath 		DOMXPath to search for groups of elements
	* @retval 	mixed  							Array of possible menus or NULL on failure
	*/
	private function grabPossibleDIVMenus($htmlDocument, $htmlXPath) {
		
		//Grab all the divs. Loop through and look for child divs and anchor counts that are equal.
		$possibleDIVMenus = array();
		$allDIVElements = $htmlDocument->getElementsByTagName('div');

		//For each matching DIV set, grab the anchor information and put them into an array
		foreach ($allDIVElements as $curDiv) {

			//Grab the child divs and anchors
			$childDIVElements = $htmlXPath->query('div', $curDiv);
			$childAnchorElements = $htmlXPath->query('a', $curDiv);
			
			//If the count of each is equal, store the anchor information from each anchor element
			if (count($childDIVElements) == count($childAnchorElements)) {

				$curDIVAnchorInformation = array();
				foreach ($childAnchorElements as $curAnchor) {

					//Get the anchor's link and label
					$anchorHREF = $curAnchor->getAttribute('href');
					$anchorLabel = $curAnchor->nodeValue;
					
					//If there is no label, grab the alt from the image if it exists
					if ($anchorLabel == "") {
						$imageList = $curAnchor->getElementsByTagName('img');
						if ($imageList->item(0)) {
							$anchorLabel = $imageList->item(0)->getAttribute('alt');
						}
					}
					
					//Insert the label and anchor information to return
					$anchorLabel = trim(str_replace("\n", ' ', $anchorLabel)); 
					if (($anchorLabel != "") && ($anchorHREF != "")) {
						$curDIVAnchorInformation[] = array('label' => $anchorLabel, 'url' => $anchorHREF);
					}
				}
			}

			//Add the array of DIV info to the final return array 
			if (count($curDIVAnchorInformation)) {$possibleULMenus[] = $curDIVAnchorInformation;}
		}
		
		//Return the array of DIV sets, if any exist
		return $possibleDIVMenus;
	}


	/**
	* Returns an associative array of the labels and weights in the database.
	*
	* The associative array consists of labels and their weights as label => weight
	*
	* @retval 	mixed  		Associative array of labels and their weights as label => weight
	*/
	private function retrieveMenuLabelWeightsFromDatabase() {
		
		//Retrieve the labels and weights from the database table and return them in an associative array
		$menuWeightsResult = databaseQuery("SELECT * FROM menuLabelWeights");
		$labelWeights = array();
		while ($curRow = $menuWeightsResult->fetch_assoc()) {
		    $labelWeights[$curRow['MLW_name']] = $curRow['MLW_weight'];
		}
		return $labelWeights;
	}


	/**
	* Returns a score for a given menu.
	*
	* The passed menu must be an array of menu items. Each item must be an associative array
	* as 'label' => label and 'url' => Anchor.
	*
	* @param 	mixed 	$menuItems 		Array of menu items. See main description.
	* @retval 	int  					Weighted score for the passed menu
	*/
	private function scoreMenu($menuItems) {
		
		//Loop through each item and add its possible weight to the total menu score, then return the score.
		$score = 0;
		$labelWeights = $this->getMenuLabelWeights();
		foreach ($menuItems as $curItem) {
			$possibleWeight = $labelWeights[$curItem['label']];
			$score += ($possibleWeight) ? $possibleWeight : 0;
		}
		return $score;
	}


	/**
	* Deletes the single domain, all of its menus, and all of the menu items.
	*
	* @param 	string  	$domain 		Domain to delete (with all its menus and menu items)
	*/
	public function deleteDomain($domain) {
		
		$this->deleteManyDomains([$domain]);
	}

	/**
	* Deletes the domains, all of their menus, and all of the menu items.
	*
	* @param 	array  	$domains 		Array of domains to delete (with all their menus and menu items)
	*/
	public function deleteManyDomains($domains) {
		
		//Clean up the domains for the query
		$cleanDomains = [];
		foreach ($domains as $curDomain) {$cleanDomains[] = "'" . databaseEscape($curDomain) . "'";}
		$cleanDomainString = implode(',', $cleanDomains);

		//Delete all the domains, their menus, and their menu items
		databaseQuery("DELETE FROM menuDomains WHERE MND_domain IN ($cleanDomainString)");
	}
		
	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* Returns the array of menu labels and their weights
	*
	* @return mixed  Associative array of labels pointing to their weights
	*/
	public function getMenuLabelWeights() {
		return $this->_menuWeights;
	}


	//********************************* Private Accessors ***********************************
	/**
	* Sets the array of menu labels and their weights
	*
	* @param mixed 	$newMenuWeights  New array of menu labels and their weights 
	*/
	private function setMenuLabelWeights($newMenuWeights) {
		$this->_menuWeights = $newMenuWeights;
	}


}


//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------

