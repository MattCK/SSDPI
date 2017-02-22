<?PHP
/**
* Contains the class for retrieving orders and creatives using Doubleclick-For-Publishers API
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\DFP;

use Google\AdsApi\Common\OAuth2TokenBuilder;
use Google\AdsApi\Dfp\DfpServices;
use Google\AdsApi\Dfp\DfpSession;
use Google\AdsApi\Dfp\DfpSessionBuilder;
use Google\AdsApi\Dfp\Util\v201702\StatementBuilder;
use Google\AdsApi\Dfp\v201702\OrderService;
use Google\AdsApi\Dfp\v201702\CompanyService;
use Google\AdsApi\Dfp\v201702\LineItemService;
use Google\AdsApi\Dfp\v201702\LineItemCreativeAssociationService;
use Google\AdsApi\Dfp\v201702\CreativeService;

//require_once 'Google/Api/Ads/Dfp/Lib/DfpUser.php';
//require_once 'Google/Api/Ads/Dfp/Util/v201608/StatementBuilder.php';


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
			// $outh2Credentials = array(
			// 	'client_id' => $clientID,
			// 	'client_secret' => $clientSecret,
			// 	'refresh_token' => $refreshToken
			// );

			// //Attempt to initialize the user and connect it to DFP
			// $user = new \DfpUser(null, $applicationName, $networkCode, null, $oauth2Credentials);

			// //Set logging to default. Strange: there does not seem to be at the moment a way to turn it off if desired.
			// $user->LogDefault();

			//Generate the OAuth token for authentication
			$dfpOAuth2Credential = (new OAuth2TokenBuilder())
									->withClientId($clientID)
									->withClientSecret($clientSecret)
									->withRefreshToken($refreshToken)
									->build();

			//Construct an API session for the selected network
			$dfpSession = (new DfpSessionBuilder())
							->withNetworkCode($networkCode)
							->withApplicationName($applicationName)
							->withOAuth2Credential($dfpOAuth2Credential)
							->build();

			//Return the DFPCommunicator with the initialized session
			return new DFPCommunicator($dfpSession);

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
	* @var DfpSession		Initialized DFP session used to retrieve orders, line items, and creatives
	*/
	private $_dfpSession;


	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Sets private members of newly constructed object.
	*
	* @param 	DfpSession		initializedDFPSession	Initialized and functional DfpSession used to connect to DFP API
	*/
	private function __construct($initializedDFPSession) {
		
		$this->setDFPSession($initializedDFPSession);
	}


	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods *************************************
	/**
	* Returns all the orders for the network associated with the instance's DfpUser
	*
	* The returned object consists of an array of order IDs pointing to an associated array with
	* 'name', 'notes', 'advertiserName', and 'agencyName' properties. Example:
	*
	*		[0123456] => ['name' 			=> 'Order #1 Name',
	*					  'notes' 			=> 'Notes for order #1,
	*					  'advertiserName' 	=> 'Advertiser Name for order #1,
	*					  'agencyName' 		=> 'Agency Name for order #1],
	*		[9876543] => ['name' 			=> 'Order #2 Name',
	*					  'notes' 			=> 'Notes for order #2,
	*					  'advertiserName' 	=> 'Advertiser Name for order #2,
	*					  'agencyName' 		=> 'Agency Name for order #2],
	*		...
	*
	* @return 	mixed  	Associative array containing order ID, order name, order notes, advertiser name, and agency name as stated in main description
	*/
	public function getOrders() {

		//Get the DfpSession instantiated for this instance and a services object
		$dfpSession = $this->getDFPSession();
		$dfpServices = new DfpServices();

		//Get the order service for the network
		//$orderService = $user->GetService('OrderService', 'v201608');
		$orderService = $dfpServices->get($dfpSession, OrderService::class);

		//Create the statement to select all orders
		$statementBuilder = new StatementBuilder();
		$statementBuilder->Where('status = :status AND isArchived = :isArchived')->OrderBy('id ASC')->WithBindVariableValue('status', 'APPROVED')->WithBindVariableValue('isArchived', 0);

		//Get the orders from DFP
		$orderResults = $orderService->getOrdersByStatement($statementBuilder->ToStatement());

		//Query DFP for all active line items for the orders.
		//We only want to return orders with READY line items.
		$lineItemService = $dfpServices->get($dfpSession, LineItemService::class);
		$lineItemWhereClause = "";
		foreach ($orderResults->getResults() as $order) {
			if ($lineItemWhereClause != "") {$lineItemWhereClause .= " OR ";}
			$lineItemWhereClause .= "(orderID = " . $order->getId() . ")";
		}
		$statementBuilder = new StatementBuilder();
		$statementBuilder->Where("($lineItemWhereClause) AND status = 'READY'");
		$lineItemResults = $lineItemService->getLineItemsByStatement($statementBuilder->ToStatement());

		//Put all the ids of orders with READY line items into an array
		$ordersWithREADYLineItems = [];
		foreach ($lineItemResults->getResults() as $currentLineItem) {
			$ordersWithREADYLineItems[] = $currentLineItem->getOrderId();
		}
		$ordersWithREADYLineItems = array_unique($ordersWithREADYLineItems);

		//----------------------------------------------------------------//
		//Some DFP clients do not allow access to their client companies
		//information. In such a case, the DFP call will fail. 
		//If this occurs, the advertiserName and agencyName in the final
		//object will be set to an empty string
		//----------------------------------------------------------------//
		$companyService = $dfpServices->get($dfpSession, CompanyService::class);
		$companyNames = [];
		if (USERDFPNETWORKCODE != 4408) {
			try { 

				//Build the where clause to get company information for the orders
				$companyWhereClause = "";
				if ($orderResults->getResults()) {
					foreach ($orderResults->getResults() as $order) {

						//If the order has READY line items, add it to the clause
						if (in_array($order->getId(), $ordersWithREADYLineItems)) {
						    if ($companyWhereClause != "") {$companyWhereClause .= " OR ";}
						    $companyWhereClause .= "(companyId = " . $order->getAdvertiserId() . ")";
						    if ($order->getAgencyId()) {$companyWhereClause .= " OR (companyId = " . $order->getAgencyId() . ")";}
						}
					 }
				}

				//Create the statement to get all companies for the orders
				if ($companyWhereClause != "") {
					$statementBuilder = new StatementBuilder();
					$statementBuilder->Where("(" . $companyWhereClause . ")");
					$companyResults = $companyService->
						              getCompaniesByStatement($statementBuilder->ToStatement());
				}

				//Format the company names
				$companyNames = [];
				if ($companyResults->getResults()) {
				    foreach ($companyResults->getResults() as $company) {
				    	$companyNames[$company->getId()] = $company->getName();
				    }
				}
			} catch (Exception $e) {
				//Fail silently
			}
		}

		//Format the returned orders
		$finalOrders = [];
		if ($orderResults->getResults()) {
		    foreach ($orderResults->getResults() as $order) {

				//Only include orders with READY line items
				if (in_array($order->getId(), $ordersWithREADYLineItems)) {
					$finalOrders[$order->getId()] = ['name' => $order->getName(), 'notes' => $order->getNotes()];

					if (count($companyNames) > 0) {
						$finalOrders[$order->getId()]['advertiserName'] = $companyNames[$order->getAdvertiserId()];
						$finalOrders[$order->getId()]['agencyName'] = ($order->getAgencyId()) ? $companyNames[$order->getAgencyId()] : "";
			    	}
			    }
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

		//Get the DfpSession instantiated for this instance and a services object
		$dfpSession = $this->getDFPSession();
		$dfpServices = new DfpServices();

		//Get the line item service for the network
		$lineItemService = $dfpServices->get($dfpSession, LineItemService::class);

		//Create the statement to select all line items for the passed order ID
		$statementBuilder = new StatementBuilder();
		$statementBuilder->Where("orderId = $orderID AND status = 'READY'")->OrderBy('id ASC');
		$lineItemResults = $lineItemService->getLineItemsByStatement($statementBuilder->ToStatement());

		//Store the line items by name => notes and separately store their IDs for LICA search
		$lineItemIDs = [];
		if ($lineItemResults->getResults()) {
		    foreach ($lineItemResults->getResults() as $lineItem) {
		        $lineItems[$lineItem->getName()] = ($lineItem->getNotes()) ? $lineItem->getNotes() : "";
		        $lineItemIDs[] = $lineItem->getId();
		    }
		}

		//Get the LICA service for the network
		$lineItemCreativeAssociationService = $dfpServices->get($dfpSession, LineItemCreativeAssociationService::class);

		//Build the where clause of line items to find LICAs for
		$licaWhereClause = "";
		foreach ($lineItemIDs as $lineItemID) {
		    if ($licaWhereClause != "") {$licaWhereClause .= " OR ";}
		    $licaWhereClause .= "(lineItemId = " . $lineItemID . ")";
		}

		//Create the statement to select all LICAs for the passed line items
		$statementBuilder = new StatementBuilder();
		$statementBuilder->Where("($licaWhereClause) AND status = 'ACTIVE'")
						 ->OrderBy('lineItemId ASC, creativeId ASC');
		$licaResults = $lineItemCreativeAssociationService->
		              getLineItemCreativeAssociationsByStatement(
		              $statementBuilder->ToStatement());

		//Store the creative IDs from the LICAs
		$creativeIDs = [];
		if ($licaResults->getResults()) {
		    foreach ($licaResults->getResults() as $lica) {
		        $creativeIDs[] = $lica->getCreativeId();
		    }
		}
		$creativeIDs = array_unique($creativeIDs);

		//Get the creative service for the network
		//$creativeService = $user->GetService('CreativeService', 'v201608');
		$creativeService = $dfpServices->get($dfpSession, CreativeService::class);

		//Build the where clause of creative IDs to find the creatives
		$creativeWhereClause = "";
		foreach ($creativeIDs as $creativeID) {
		    if ($creativeWhereClause != "") {$creativeWhereClause .= " OR ";}
		    $creativeWhereClause .= "(id = " . $creativeID . ")";
		}

		//Create the statement to select creatives for the passed IDs
		$statementBuilder = new StatementBuilder();
		$statementBuilder->Where("(" . $creativeWhereClause . ")")->OrderBy('id ASC');
		$creativeResults = $creativeService->getCreativesByStatement($statementBuilder->ToStatement());

		//Output the creative
		if ($creativeResults->getResults()) {
		    foreach ($creativeResults->getResults() as $creative) {

		    	//Get the creative object's class
		    	//Get the full class, separate it by \, take the last part and make it uppercase
		    	//This will remove the full class path and return only the class name in uppercase
		    	$creativeClass = strtoupper(array_pop(explode("\\", get_class($creative))));

		    	//Based on the creative class type, get the appropriate tag
		    	$tag = "";
		    	switch ($creativeClass) {
		    		case "IMAGECREATIVE":
		    			$tag = "<img src='" . $creative->getPrimaryImageAsset()->getAssetUrl() . "' />";
		    			break;
		    		case "THIRDPARTYCREATIVE":
		    			$htmlSnippet = $creative->getSnippet();
		    			$expandedSnippet = $creative->getExpandedSnippet();
		    			$tag = ($expandedSnippet) ? $expandedSnippet : $htmlSnippet;
		    			break;
		    		case "CUSTOMCREATIVE":
						$tag = $creative->getHtmlSnippet();
		    			break;
		    		case "TEMPLATECREATIVE":
						$templateVariables = $creative->getCreativeTemplateVariableValues();
						foreach($templateVariables as $currentVariable) {
							if (method_exists($currentVariable, "getAsset")) {
								$tag = "<img src='" . $currentVariable->getAsset()->getAssetURL() . "' />";
							}
						}
		    			break;
		    		case "INTERNALREDIRECTCREATIVE":
		    			$tag = "<img src='" . $creative->getInternalRedirectUrl() . "' />";
		    			break;
		    		case "FLASHCREATIVE":
		    			$tag = "<img src='" . $creative->getFallbackImageAsset()->getAssetUrl() . "' />";
		    			break;
		    		case "IMAGEREDIRECTCREATIVE":
		    			$tag = "<img src='" . $creative->getImageUrl() . "' />";
		    			break;
		    	}


		    	//If a tag/image was found, include it in the final list.
		    	//If not, ignore it for now (i.e. the UnsupportedCreative type)
		        if ($tag) {$creatives[$creative->getId()] = $tag;}
		    }
		}
	}


	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Returns the instantiated DfpSession of the instance
	*
	* @return DfpSession	Instantiated DfpSession of the instance
	*/
	private function getDFPSession(){
		return $this->_dfpSession;
	}
	/**
	* Sets the instantiated DfpSession of the instance
	*
	* @param DfpSession 	$newDFPSession  New instantiated DfpSession of the instance 
	*/
	private function setDFPSession($newDFPSession){
		$this->_dfpSession = $newDFPSession;
	}




















}