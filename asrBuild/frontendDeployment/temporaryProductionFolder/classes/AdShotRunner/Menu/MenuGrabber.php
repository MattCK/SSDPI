<?PHP
/**
* Contains the class for discerning and retrieving menus from URLs
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\Menu;

use AdShotRunner\Database\ASRDatabase;
use AdShotRunner\Utilities\WebPageCommunicator;
use AdShotRunner\PhantomJS\PhantomJSCommunicator;

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
*/
class MenuGrabber {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	const FOUNDLINKMINIMUM = 10;

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
	* Returns the most likely top menu for the passed domain.
	*
	* The returned associative array consists of the menu label as the key and URL as value.
	*
	* @param 	string 		$domains 	Domain to get best menu for
	* @retval 	mixed  					Associative array consisting of the menu label as key and URL as value.
	*/
	public function getBestDomainMenu($domain) {

		//Get the list of menus
		$domainMenus = $this->getDomainMenus($domain);

		//If no menus were returned, return an empty array.
		if (count($domainMenus) == 0) {return [];}

		//Grab the top most menu items
		$menuList = reset($domainMenus);
		$topMenu = reset($menuList);
		$menuItems = $topMenu['items'];

		//Put the top menu items into the final array
		$bestMenuItems = [];
		foreach($menuItems as $currentSet) {
			if ($currentSet['label']) {
				$bestMenuItems[$currentSet['label']] = $currentSet['url'];
			}
		}

		return $bestMenuItems;
	}

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
	* Array[url] -> MenuID -> 'score' => 12
	*		               -> 'items' => Array -> 'label'
	*			        	  				   -> 'url'
	*	
	* On failure, NULL is returned.
	*
	* @param 	mixed 		$domains 	String or array of the domains to get menus for
	* @retval 	mixed  					Array of ranked menus or NULL on failure
	*/
	public function getDomainMenus($domains) {
		
		//If necessary, make the passed variable an array
		if (!is_array($domains)) {$domains = [$domains];}

		//Remove any http/https from the domains
		foreach ($domains as $curKey => $rawDomain) {
			$domains[$curKey] = preg_replace('#^https?://#', '', $rawDomain);
		}

		//Check the database to see if the menus exist there
		$databaseDomainMenus = $this->retrieveManyDomainMenusFromDatabase($domains);

		//Grab any exception menus. Merge the two arrays.
		//The merge will cause the original domains to be overwritten.
		$domainMenuExceptions = $this->retrieveMenuExceptions($domains);
		$databaseDomainMenus = array_merge($databaseDomainMenus, $domainMenuExceptions);

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
		
		//Get the HTML responses from the passed URL
		//$URLCommunicator = new WebPageCommunicator();
		//$urlResponses = $URLCommunicator->getManyURLResponses($urls);
		$urlResponses = $this->getURLResponses($urls);

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
	* Returns a list menu exceptions for multiple domains out of the database.
	*
	* The returned array consists for each key of associated arrays with two keys:
	* 'score' pointing to the numeric score of the menu and 'items' for the menu itself.
	* The 'items' points to an array of items. Each item is an associative array as
	* 'label' pointing to an individual label and 'url' pointing to that label's link.
	*
	* The key of the top-most array is the URL string.
	*
	* Array[url] -> 0 -> 'score' => 12
	*		     	  -> 'items' => Array -> 'label'
	*			        			 	  -> 'url'
	*	
	* On failure, an empty array is returned.
	*
	* @param 	array 	$domains 	Domains to get menus from
	* @retval 	mixed  				Array of ranked menus or empty array on failure
	*/
	protected function retrieveMenuExceptions($domains) {
		
		//If no domains were passed, return an empty array
		if ((!$domains) || (count($domains) < 1)) {return array();}

		//Clean up the domains for the query
		$cleanDomains = [];
		foreach ($domains as $curDomain) {$cleanDomains[] = "'" . ASRDatabase::escape($curDomain) . "'";}
		$cleanDomainString = implode(',', $cleanDomains);

		//Get the domains' menus from the database (if any exist)
		$menuResults = ASRDatabase::executeQuery("	SELECT *
										FROM exceptionsMenuGrabberDomains
										LEFT JOIN exceptionsMenuGrabberItems ON EMI_EMD_id = EMD_id
										WHERE EMD_domain IN ($cleanDomainString)");

		//Create the final array and loop through the results, creating the menu structures
		$domainMenus = [];
		while ($curMenuItem = $menuResults->fetch_assoc()) {

			//If we haven't seen this domain yet, add it final results array
		    if (!array_key_exists($curMenuItem['EMD_domain'], $domainMenus)) {
		    	$domainMenus[$curMenuItem['EMD_domain']] = array(0 => array());
			    $domainMenus[$curMenuItem['EMD_domain']][0]['score'] = 0;
		    	$domainMenus[$curMenuItem['EMD_domain']][0]['items'] = array();
		    }

		    //Finally, add the actual item
		    $domainMenus[$curMenuItem['EMD_domain']][0]['items'][] = ['label' => $curMenuItem['EMI_label'],
		    														  'url' => $curMenuItem['EMI_url']];
		}

		//Return the menus, if any exist
		return $domainMenus;
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
	* Array[url] -> Menu ID -> 'score' => 12
	*		     	  		-> 'items' => Array -> 'label'
	*			        			 	  		-> 'url'
	*	
	* On failure, an empty array is returned.
	*
	* @param 	array 	$domains 	Domains to get menus from
	* @retval 	mixed  				Array of ranked menus or empty array on failure
	*/
	protected function retrieveManyDomainMenusFromDatabase($domains) {
		
		//If no domains were passed, return an empty array
		if ((!$domains) || (count($domains) < 1)) {return array();}

		//Clean up the domains for the query
		$cleanDomains = [];
		foreach ($domains as $curDomain) {$cleanDomains[] = "'" . ASRDatabase::escape($curDomain) . "'";}
		$cleanDomainString = implode(',', $cleanDomains);

		//Get the domains' menus from the database (if any exist)
		$menuResults = ASRDatabase::executeQuery("	SELECT *
										FROM menuGrabberDomains
										LEFT JOIN menuGrabberMenus ON MGM_MGD_id = MGD_id
										LEFT JOIN menuGrabberItems ON MGI_MGM_id = MGM_id
										WHERE MGD_domain IN ($cleanDomainString)
										ORDER BY MGM_score DESC, MGI_label ASC");

		//Create the final array and loop through the results, creating the menu structures
		$domainMenus = [];
		while ($curMenuItem = $menuResults->fetch_assoc()) {

			//If we haven't seen this domain yet, add it to the top level of the results
		    if (!array_key_exists($curMenuItem['MGD_domain'], $domainMenus)) {
		    	$domainMenus[$curMenuItem['MGD_domain']] = array();
		    }

		    //Same again but for the menu
		    if (!array_key_exists($curMenuItem['MGM_id'], $domainMenus[$curMenuItem['MGD_domain']])) {
		    	$domainMenus[$curMenuItem['MGD_domain']][$curMenuItem['MGM_id']] = array();
		    }

		    //Enter the menu score
		    $domainMenus[$curMenuItem['MGD_domain']][$curMenuItem['MGM_id']]['score'] = $curMenuItem['MGM_score'];

		    //If the items array has not been added, do so.
		    if (!array_key_exists('items', $domainMenus[$curMenuItem['MGD_domain']][$curMenuItem['MGM_id']])) {
		    	$domainMenus[$curMenuItem['MGD_domain']][$curMenuItem['MGM_id']]['items'] = array();
		    }

		    //Finally, add the actual item
		    $domainMenus[$curMenuItem['MGD_domain']][$curMenuItem['MGM_id']]['items'][] = ['label' => $curMenuItem['MGI_label'],
		    																   'url' => $curMenuItem['MGI_url']];
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
	protected function insertDomainsWithMenus(&$domainInfo) {
		
		//Verify domain info was passed
		if ((!$domainInfo) || (count($domainInfo) < 1)) {return;}

		//Loop through each domain: run the delete command on it, add the domain, add the domain menus, add the menu items.
		foreach ($domainInfo as $domain => $domainMenus) {

			//Delete the domain, if it exists, in the database.
			$this->deleteDomain($domain);

			//Add the domain to the database
			$cleanDomainString = "'" . ASRDatabase::escape($domain) . "'";
			ASRDatabase::executeQuery("INSERT INTO menuGrabberDomains (MGD_domain) VALUES ($cleanDomainString)");

			//Get the domain insert ID
			$domainID = ASRDatabase::lastInsertID();

			//Loop through the menus, adding each to the database with their items
			foreach ($domainMenus as $menuKey => $menu) {

				//Insert the menu and its score
				$cleanScoreString = "'" . ASRDatabase::escape($menu['score']) . "'";
				ASRDatabase::executeQuery("INSERT INTO menuGrabberMenus (MGM_MGD_id, MGM_score) VALUES ($domainID, $cleanScoreString)");

				//Get the menu insert ID
				$menuID = ASRDatabase::lastInsertID();

				//Create the value string of menu items
				$cleanMenuItems = [];
				foreach ($menu['items'] as $itemKey => $item) {

					//Either remove the http/https or add the domain to the menu item URL
					$curURL = $item['url'];
					if (substr($curURL, 0, 4) == "http") {
						$curURL = preg_replace('#^https?://#', '', $curURL);
					}

					//Some sites put a double slash before the subdomain
					else if (substr($curURL, 0, 2) == "//") {
						$curURL = substr($curURL, 2);
					}
					else {
						if (substr($curURL, 0, 1) != "/") {$curURL = "/" . $curURL;}
						$curURL = $domain . $curURL;
					}

					$domainInfo[$domain][$menuKey]['items'][$itemKey]['url'] = $curURL;

					$cleanMenuItems[] = "($menuID, '" . 
								  		  ASRDatabase::escape($item['label']) . "', '" . 
								  		  ASRDatabase::escape($curURL) . "')";
				}
				$cleanMenuItemString = implode(',', $cleanMenuItems);

				//Insert the items into the database
				if ($cleanMenuItemString) {
					ASRDatabase::executeQuery("INSERT IGNORE INTO menuGrabberItems (MGI_MGM_id, MGI_label, MGI_url) 
								   VALUES $cleanMenuItemString");
				}
			}

			//THIS IS FOR TESTING ONLY. PREVENTS DATABASE ENTRY.
			//$this->deleteDomain($domain);
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
		$htmlDocument = new \DOMDocument();
		$htmlDocument->loadHTML($pageHTML);	
		$htmlXPath = new \DOMXpath($htmlDocument);

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

					//If there is no label but a second link, use the second link
					if (($anchorLabel == "") && ($anchorList->item(1))) {
						$curAnchor = $anchorList->item(1);
						$anchorHREF = $curAnchor->getAttribute('href');
						$anchorLabel = $curAnchor->nodeValue;
					}
					//echo $anchorLabel . ": " . $anchorHREF . "\n";
					
					//If the label already exists in the menu, make the link empty
					foreach ($curULAnchorInformation as $currentLinkSet) {
						if (($currentLinkSet['label'] == $anchorLabel) ||
							($currentLinkSet['url'] == $anchorHREF)) {
							$anchorLabel = "";
						}
					}
					
					//If there is no label, grab the alt from the image if it exists
					if ($anchorLabel == "") {
						$imageList = $curAnchor->getElementsByTagName('img');
						if ($imageList->item(0)) {
							$anchorLabel = $imageList->item(0)->getAttribute('alt');
						}
					}

					//If the anchor's link begins with "javascript:", attempt to extract the URL if it exists
					if (substr($anchorHREF, 0, 11) == "javascript:") {
						$anchorHREF = $this->getURLFromJavascriptLink($anchorHREF);
					}
					
					//Insert the label and anchor information to return
					$anchorLabel = trim(str_replace("\n", ' ', $anchorLabel)); 
					if (($anchorLabel != "") && ($anchorHREF != "")) {
						$curULAnchorInformation[] = array('label' => $anchorLabel, 'url' => $anchorHREF);
					}
				}

			}
			//echo "\n-------------\n";
			
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

					//If the label already exists in the menu, make the link empty
					foreach ($curDIVAnchorInformation as $currentLinkSet) {
						if (($currentLinkSet['label'] == $anchorLabel) ||
							($currentLinkSet['url'] == $anchorHREF)) {
							$anchorLabel = "";
						}
					}
					
					//If the anchor's link begins with "javascript:", attempt to extract the URL if it exists
					if (substr($anchorHREF, 0, 11) == "javascript:") {
						$anchorHREF = $this->getURLFromJavascriptLink($anchorHREF);
					}
					
					//Insert the label and anchor information to return
					$anchorLabel = trim(str_replace("\n", ' ', $anchorLabel)); 
					if (($anchorLabel != "") && ($anchorHREF != "")) {
						$curDIVAnchorInformation[] = array('label' => $anchorLabel, 'url' => $anchorHREF);
					}
				}
			}

			//Add the array of DIV info to the final return array 
			if (count($curDIVAnchorInformation)) {$possibleDivMenus[] = $curDIVAnchorInformation;}
		}
		
		//Return the array of DIV sets, if any exist
		return $possibleDIVMenus;
	}

	private function getURLFromJavascriptLink($javascriptLink) {
		preg_match_all('!https?://\S+!', $javascriptLink, $matches);
		return ($matches[0] && $matches[0][0]) ? $matches[0][0] : "";	
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
		$menuWeightsResult = ASRDatabase::executeQuery("SELECT * FROM menuGrabberLabelWeights");
		$labelWeights = array();
		while ($curRow = $menuWeightsResult->fetch_assoc()) {
		    $labelWeights[strtolower($curRow['MLW_name'])] = $curRow['MLW_weight'];
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
			$possibleWeight = $labelWeights[strtolower($curItem['label'])];
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
		foreach ($domains as $curDomain) {$cleanDomains[] = "'" . ASRDatabase::escape($curDomain) . "'";}
		$cleanDomainString = implode(',', $cleanDomains);

		//Delete all the domains, their menus, and their menu items
		ASRDatabase::executeQuery("DELETE FROM menuGrabberDomains WHERE MGD_domain IN ($cleanDomainString)");
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

	//**********************************************************************************//
	//********************************TEMPORARY CODE************************************//
	//People say that and then it becomes permanent.

	private function getURLResponses($urls) {

		//echo "Begin to get URL\n";
		//print_r($urls); echo "\n";

		//SHOULD COME FROM DATABASE LATER DATE
		$userAgents = [
							'Mozilla/5.0 (compatible; Googlebot/2.1; http://www.google.com/bot.html)',  //Googlebot
							'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0',	//Firefox
							'msnbot/1.1 (+http://search.msn.com/msnbot.htm)',							//MSNBot
							'Mozilla/5.0 (X11; Linux x86_64; rv:47.0) Gecko/20100101 Firefox/47.0'		//Firefox Linux
					  ];

		//Loop through the URLs and get the web response for each
		$urlResponses = [];
		foreach ($urls as $targetURL) {

			$userAgentIndex = 0;
			$foundLinksCount = 0;
			$urlResponse = "";
			// echo "Attempting to get: " . $targetURL . "\n";
			// echo "User agent count: " . count($userAgents) . "\n";
			// echo "User agent index: " . $userAgentIndex . "\n";
			// echo "Found links count: " . $foundLinksCount . "\n";
			// echo "Links found minimum: " . MenuGrabber::FOUNDLINKMINIMUM . "\n";
			while (($userAgentIndex < count($userAgents)) && ($foundLinksCount < MenuGrabber::FOUNDLINKMINIMUM)) {

				//Get the URL response
				//echo "Before phantomjs grab\n";
				$urlResponse = PhantomJSCommunicator::getResponse("http://" . $targetURL); //, $userAgents[$userAgentIndex]);
				//$urlResponse = PhantomJSCommunicator::getResponse("http://nytimes.com");
				//echo "After phantomjs grab\n";
				//echo "URL response: " . substr($urlResponse, 0, 200) . "\n";

				//Get amount of links found
				$htmlDocument = new \DomDocument();
				$htmlDocument->loadHTML($urlResponse);
				$foundLinks = $htmlDocument->getElementsByTagName('a');
				$foundLinksCount = count($foundLinks);
				//echo $userAgents[$userAgentIndex];
				//echo "Links found: " . count($foundLinks);

				++$userAgentIndex;
			}

			//Store the response
			$urlResponses[$targetURL] = $urlResponse;
		}

		return $urlResponses;
	}

}


//---------------------------------------------------------------------------------------
//-------------------------- Helper Functions and Wrappers-------------------------------
//---------------------------------------------------------------------------------------

