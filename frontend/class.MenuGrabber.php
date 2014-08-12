<?PHP

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
	* Returns a list of ranked possible menus sorted from highest score to lowest.
	*
	* The returned object consists of an array of associated arrays with two keys:
	* 'score' pointing to the numeric score of the menu and 'menu' for the menu itself.
	* The 'menu' points to an array of items. Each item is an associative array as
	* 'label' pointing to an individual label and 'link' pointing to that label's link.
	*
	* Array -> 'score' => 12
	*		-> 'menu' => Array -> 'label'
	*						   -> 'link'
	*	
	* On failure, NULL is returned.
	*
	* @param 	string 	$url 	URL to get menus from
	* @retval 	mixed  			Array of ranked menus or NULL on failure
	*/
	public function getRankedMenus($url) {
		
		//Get the HTML response from the passed URL
		$URLCommunicator = new WebPageCommunicator();
		$urlHTML = $URLCommunicator->getURLResponse($url);

		//Return the possible menus
		$possibleMenus = $this->grabPossibleMenusFromHTML($urlHTML);

		//Create a final array with the menu items and its overall score
		$rankedMenus = [];
		foreach ($possibleMenus as $curMenu) {
			$rankedMenus[] = ['score' => $this->scoreMenu($curMenu), 'menu' => $curMenu];
		}

		//Sort the results to put the highest scores first in the array
		usort($rankedMenus, function($a, $b) {
		    return $b['score'] - $a['score'];
		});

		//Return the results
		return $rankedMenus;
	}

	/**
	* Returns a list of ranked menus sorted from highest score to lowest
	* from many URLs.
	*
	* The returned array consists for each key of associated arrays with two keys:
	* 'score' pointing to the numeric score of the menu and 'menu' for the menu itself.
	* The 'menu' points to an array of items. Each item is an associative array as
	* 'label' pointing to an individual label and 'link' pointing to that label's link.
	*
	* The key of the top-most array is the URL string.
	*
	* Array -> 'score' => 12
	*		-> 'menu' => Array -> 'label'
	*						   -> 'link'
	*	
	* On failure, NULL is returned.
	*
	* @param 	array 	$urls 	URLs to get menus from
	* @retval 	mixed  			Array of ranked menus or NULL on failure
	*/
	public function getRankedMenusFromManyURLs($urls) {
		
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
				$rankedMenus[] = ['score' => $this->scoreMenu($curMenu), 'menu' => $curMenu];
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

	//********************************* Private Methods *************************************


	/**
	* Returns an array of possible menus from the passed HTML.
	*
	* The returned object is an array of menus. Each menu points to an array of items.
	* Each item is an associative array as 'label' pointing to an individual label and 
	* 'link' pointing to that label's link.
	*
	* Array => Array -> 'label'
	*				 -> 'link'
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
	* 'link' pointing to that label's link.
	*
	* Array => Array -> 'label'
	*				 -> 'link'
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
						$curULAnchorInformation[] = array('label' => $anchorLabel, 'link' => $anchorHREF);
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
	* 'link' pointing to that label's link.
	*
	* Array => Array -> 'label'
	*				 -> 'link'
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
						$curDIVAnchorInformation[] = array('label' => $anchorLabel, 'link' => $anchorHREF);
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
	* as 'label' => label and 'link' => Anchor.
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

