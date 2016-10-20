<?PHP
/**
* Contains the class for retrieving orders and creatives using Doubleclick-For-Publishers API
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\DFP;

require_once 'Google/Api/Ads/Dfp/Lib/DfpUser.php';
require_once 'Google/Api/Ads/Dfp/Util/v201608/StatementBuilder.php';

/**
* The DFPCommunicator communicates with Google's Doubleclick-For-Publishers using their API.
* 
* At its core, it returns all of the orders for a given network (and its related information) and
* all of the line items and creatives for a given order (and their related information). 
*/
class DFPCommunicator {

	//--------------------------------------------------------------------------------------
	//---------------------------------- Static Methods ------------------------------------
	//--------------------------------------------------------------------------------------
	//***************************** Public Static Methods **********************************
	/**
	* Creates a DFPCommunicator object with the passed user (client), network, and application information.
	* 
	* @param 	string 	clientID				ID of client with permissions to access network
	* @param 	string 	clientSecret			"Secret" passcode of client with permissions to access network
	* @param 	string 	refreshToken			Refresh token of client
	* @param 	string 	networkCode				Network code for network to access (client must have authorized permissions on it)
	* @param 	string 	applicationName			Application of name connection to DFP API (Example: AdShotRunner)
	* @return 	DFPCommunicator					Initialized DFPCommunicator on success and NULL on failure		
	*/
	static public function create($clientID, $clientSecret, $refreshToken, $networkCode, $applicationName) {

		//Try to authorize the DFP user. On success, return it and on failure, return NULL
		try {

			//Store the DFP user (client) credentials into an associative array 
			$outh2Credentials = array(
				'client_id' => $clientID,
				'client_secret' => $clientSecret,
				'refresh_token' => $refreshToken
			);

			//Attempt to initialize the user and connect it to DFP
			$user = new \DfpUser(null, $applicationName, $networkCode, null, $oauth2Credentials);

			//Set logging to default. Strange: there does not seem to be at the moment a way to turn it off if desired.
			$user->LogDefault();

			//Return the DFPCommunicator with the initialized user
			return new DFPCommunicator($user);

		} 

		//If there was an error, return NULL
		catch (Exception $e) {
			return NULL;
		}
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	* @var DfpUser		Initialized DFP user used to retrieve orders, line items, and creatives
	*/
	private $_dfpUser;


	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Sets private members of newly constructed object.
	*
	* @param 	DfpUser		initializedDFPUser			Initialized and functional DfpUser used to connect to DFP API
	*/
	private function __construct($initializedDFPUser) {
		
		$this->setDFPUser($initializedDFPUser);
	}


	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods *************************************
	/**
	* Returns all the orders for the network associated with the instance's DfpUser
	*
	* The returned object consists of an array of order IDs pointing to an associated array with
	* 'name' and 'notes' properties. Example:
	*
	*		[0123456] => ['name' 	=> 'Order #1 Name',
	*					  'notes' 	=> 'Notes for order #1],
	*		[9876543] => ['name' 	=> 'Order #2 Name',
	*					  'notes' 	=> 'Notes for order #2],
	*		...
	*
	* @return 	mixed  					Associative array containing order ID, order name, and order notes as stated in main description
	*/
	public function getOrders() {

		//Get the DfpUser instantiated for this instance
		$user = $this->getDFPUser();

		//Get the order service for the network
		$orderService = $user->GetService('OrderService', 'v201608');

		//Create the statement to select all orders
		$statementBuilder = new \StatementBuilder();
		$statementBuilder->Where('status = :status AND isArchived = :isArchived')->OrderBy('id ASC')->WithBindVariableValue('status', 'APPROVED')->WithBindVariableValue('isArchived', 0);

		//Get the orders from DFP
		$orderResults = $orderService->getOrdersByStatement($statementBuilder->ToStatement());

		//Format the returned orders
		$finalOrders = [];
		if (isset($orderResults->results)) {
		    foreach ($orderResults->results as $order) {
		    	$finalOrders[$order->id] = ['name' => $order->name, 'notes' => $order->notes];
		    }
		}
		return $finalOrders;
	}

	/**
	* Places all the line items and creatives in the respective by reference variables for the passed order ID.
	*
	* The line items are set to an array of the line item names with the line item notes as the values.
	*
	* The creatives are set to an array of creative IDs with the tag script as the values.
	* 
	* @param 	string 	orderID				ID of order to get line items and creatives from
	* @param 	string 	&lineItems			By reference variable set to an array of line item names with their notes as values
	* @param 	string 	&creatives			By reference variable set to an array of creative IDs with their tag scripts as values
	*/
	public function getLineItemsAndCreative($orderID, &$lineItems, &$creatives) {

		//Get the DfpUser instantiated for this instance
		$user = $this->getDFPUser();

		//Get the line item service for the network
		$lineItemService = $user->GetService('LineItemService', 'v201608');

		//Create the statement to select all line items for the passed order ID
		$statementBuilder = new \StatementBuilder();
		$statementBuilder->Where('orderId = ' . $orderID)->OrderBy('id ASC');
		$lineItemResults = $lineItemService->getLineItemsByStatement($statementBuilder->ToStatement());

		//Store the line items by name => notes and separately store their IDs for LICA search
		$lineItemIDs = [];
		if (isset($lineItemResults->results)) {
		    foreach ($lineItemResults->results as $lineItem) {
		        $lineItems[$lineItem->name] = $lineItem->notes;
		        $lineItemIDs[] = $lineItem->id;
		    }
		}

		//Get the LICA service for the network
		$lineItemCreativeAssociationService = $user->GetService('LineItemCreativeAssociationService', 'v201608');

		//Build the where clause of line items to find LICAs for
		$licaWhereClause = "";
		foreach ($lineItemIDs as $lineItemID) {
		    if ($licaWhereClause != "") {$licaWhereClause .= " OR ";}
		    $licaWhereClause .= "(lineItemId = " . $lineItemID . ")";
		}

		//Create the statement to select all LICAs for the passed line items
		$statementBuilder = new \StatementBuilder();
		$statementBuilder->Where("(" . $licaWhereClause . ")")
		->OrderBy('lineItemId ASC, creativeId ASC');
		$licaResults = $lineItemCreativeAssociationService->
		              getLineItemCreativeAssociationsByStatement(
		              $statementBuilder->ToStatement());

		//Store the creative IDs from the LICAs
		$creativeIDs = [];
		if (isset($licaResults->results)) {
		    foreach ($licaResults->results as $lica) {
		        $creativeIDs[] = $lica->creativeId;
		    }
		}
		$creativeIDs = array_unique($creativeIDs);

		//Get the creative service for the network
		$creativeService = $user->GetService('CreativeService', 'v201608');

		//Build the where clause of creative IDs to find the creatives
		$creativeWhereClause = "";
		foreach ($creativeIDs as $creativeID) {
		    if ($creativeWhereClause != "") {$creativeWhereClause .= " OR ";}
		    $creativeWhereClause .= "(id = " . $creativeID . ")";
		}

		//Create the statement to select creatives for the passed IDs
		$statementBuilder = new \StatementBuilder();
		$statementBuilder->Where("(" . $creativeWhereClause . ")")->OrderBy('id ASC');
		$creativeResults = $creativeService->getCreativesByStatement($statementBuilder->ToStatement());

		//Output the creative
		if (isset($creativeResults->results)) {
		    foreach ($creativeResults->results as $creative) {

		    	$tag = "";
		    	if (isset($creative->expandedSnippet)) {$tag = $creative->expandedSnippet;}
		    	else if (isset($creative->htmlSnippet)) {$tag = $creative->htmlSnippet;}
		    	else if (isset($creative->primaryImageAsset)) {$tag = "<img src='" . $creative->primaryImageAsset->assetUrl . "' />";}

		        $creatives[$creative->id] = $tag;
		    }
		}
	}


	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Returns the instantiated DfpUser of the instance
	*
	* @return DfpUser	Instantiated DfpUser of the instance
	*/
	private function getDFPUser(){
		return $this->_dfpUser;
	}
	/**
	* Sets the instantiated DfpUser of the instance
	*
	* @param DfpUser 	$newDFPUser  New instantiated DfpUser of the instance 
	*/
	private function setDFPUser($newDFPUser){
		$this->_dfpUser = $newDFPUser;
	}




















}