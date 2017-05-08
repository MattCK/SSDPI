<?PHP
/**
* Contains the class for retrieving orders and creatives using Doubleclick-For-Publishers API
*
* @package Adshotrunner
* @subpackage Classes
*/

namespace AdShotRunner\DFP;

require_once('../../../systemSetup.php');


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

//header("Content-Type: plain/text"); 

// Generate a refreshable OAuth2 credential for authentication.
$oAuth2Credential = (new OAuth2TokenBuilder())
->withClientId("87580729053-tm802nbiqhf2o33eic38q8vvi688dsbp.apps.googleusercontent.com")
->withClientSecret("nYe5-OOiDXp4sntqydYyGWPC")
->withRefreshToken("1/EmflFSwLOd-g-cQDpQ4ndtKbaeHZErtTmhRSGYVukKI")
->build();


// Construct an API session configured from a properties file and the OAuth2
// credentials above.
$session = (new DfpSessionBuilder())
//->withNetworkCode("324288910")
->withNetworkCode("4408")
->withApplicationName("AdShotRunner")
->withOAuth2Credential($oAuth2Credential)
->build();

  echo "Session class: " . get_class($session) . "\n";

$dfpServices = new DfpServices();

$orderService = $dfpServices->get($session, OrderService::class);

//Create the statement to select all orders
$statementBuilder = new StatementBuilder();
$statementBuilder->Where('status = :status AND isArchived = :isArchived')->OrderBy('id ASC')->WithBindVariableValue('status', 'APPROVED')->WithBindVariableValue('isArchived', 0);

//Get the orders from DFP
$orderResults = $orderService->getOrdersByStatement($statementBuilder->ToStatement());


$lineItemService = $dfpServices->get($session, LineItemService::class);
    $lineItemWhereClause = "";
    foreach ($orderResults->getResults() as $order) {
        if ($lineItemWhereClause != "") {$lineItemWhereClause .= " OR ";}
        $lineItemWhereClause .= "(orderID = " . $order->getId() . ")";
    }
    $lineItemWhereClause = "($lineItemWhereClause) AND status = 'READY'";
    //echo "Line item Query: $lineItemWhereClause \n";
     $statementBuilder = new StatementBuilder();
   $statementBuilder->Where($lineItemWhereClause);
    $lineItemResults = $lineItemService->getLineItemsByStatement($statementBuilder->ToStatement());
    $ordersWithLineItems = [];
    foreach ($lineItemResults->getResults() as $currentLineItem) {
        $ordersWithLineItems[] = $currentLineItem->getOrderId();
    }
    $ordersWithLineItems = array_unique($ordersWithLineItems);
    //print_r($ordersWithLineItems);


//print_r($orderResults);

// $companyService = $dfpServices->get($session, CompanyService::class);

// //Build the where clause to get company information for the orders
// $companyWhereClause = "";
// //if (isset($orderResults->results)) {
//   foreach ($orderResults->getResults() as $order) {
//     if ($companyWhereClause != "") {$companyWhereClause .= " OR ";}
//     $companyWhereClause .= "(companyId = " . $order->getAdvertiserId() . ")";
//     if ($order->getAgencyId()) {$companyWhereClause .= " OR (companyId = " . $order->getAgencyId() . ")";}
//   }
// //}

// //Create the statement to get all companies for the orders
// if ($companyWhereClause != "") {
// 	$statementBuilder = new StatementBuilder();
// 	$statementBuilder->Where("(" . $companyWhereClause . ")");

// 	$companyResults = $companyService->
// 		              getCompaniesByStatement($statementBuilder->ToStatement());
// }

//       //Format the company names
//       $companyNames = [];
//       //if (isset($companyResults->results)) {
//           foreach ($companyResults->getResults() as $company) {
//             $companyNames[$company->getId()] = $company->getName();
//           }
//       //}

//print_r($companyResults);

// $pageSize = StatementBuilder::SUGGESTED_PAGE_LIMIT;
// $statementBuilder = (new StatementBuilder())
//     ->orderBy('id ASC')
//     ->limit($pageSize);

// $companyService = $dfpServices->get($session, CompanyService::class);

// $page = $companyService->getCompaniesByStatement($statementBuilder->toStatement());
// print_r($page->getResults());

    $finalOrders = [];
    //if (isset($orderResults->results)) {
        foreach ($orderResults->getResults() as $order) {
          if (in_array($order->getId(), $ordersWithLineItems)) {
            //echo $order->getId() . " has line items \n";
            $finalOrders[$order->getId()] = ['name' => $order->getName(), 'notes' => $order->getNotes()];

            if (count($companyNames) > 0) {
              $finalOrders[$order->getId()]['advertiserName'] = $companyNames[$order->getAdvertiserId()];
              $finalOrders[$order->getId()]['agencyName'] = ($order->getAgencyId()) ? $companyNames[$order->getAgencyId()] : "";
            }
          }
          else {
            //echo $order->getId() . " has no line items \n";
          }
        }
    //}
    //print_r($finalOrders);
   // exit();

$lineItemService = $dfpServices->get($session, LineItemService::class);

$orderID = "210376057"; //"210217177"; //"209058337"; //"189447817"; //"180872017"; //"180808537"; //"778532670";

      //Create the statement to select all line items for the passed order ID
    $statementBuilder = new StatementBuilder();
    //$statementBuilder->Where('orderId = ' . $orderID)->OrderBy('id ASC');
    // $statementBuilder->Where('orderID = :orderID AND status = :status')->WithBindVariableValue('orderID', $orderID)->WithBindVariableValue('status', "READY");

    $statementBuilder->Where('orderID = :orderID')->WithBindVariableValue('orderID', $orderID);

    $lineItemResults = $lineItemService->getLineItemsByStatement($statementBuilder->ToStatement());
    echo "Line item count: " . count($lineItemResults->getResults()) . "\n";

    //Store the line items by name => notes and separately store their IDs for LICA search
    $lineItemIDs = [];
    //if (isset($lineItemResults->getResults())) {
        foreach ($lineItemResults->getResults() as $lineItem) {
            $lineItems[$lineItem->getName()] = $lineItem->getNotes();
            $lineItemIDs[] = $lineItem->getId();
        }
    //}

//print_r($lineItems); print_r($lineItemIDs);

    //Get the LICA service for the network
$lineItemCreativeAssociationService = $dfpServices->get($session, LineItemCreativeAssociationService::class);

    //Build the where clause of line items to find LICAs for
    $licaWhereClause = "";
    foreach ($lineItemIDs as $lineItemID) {
        if ($licaWhereClause != "") {$licaWhereClause .= " OR ";}
        $licaWhereClause .= "(lineItemId = " . $lineItemID . ")";
    }

    //Create the statement to select all LICAs for the passed line items
    $statementBuilder = new StatementBuilder();
    // $statementBuilder->Where("(" . $licaWhereClause . ")")
    // ->OrderBy('lineItemId ASC, creativeId ASC');
    $statementBuilder->Where("(" . $licaWhereClause . ") AND status = 'ACTIVE'")
    ->OrderBy('lineItemId ASC, creativeId ASC');
    $licaResults = $lineItemCreativeAssociationService->
                  getLineItemCreativeAssociationsByStatement(
                  $statementBuilder->ToStatement());

    //Store the creative IDs from the LICAs
    $creativeIDs = [];
    //if (isset($licaResults->results)) {
        foreach ($licaResults->getResults() as $lica) {
            $creativeIDs[] = $lica->getCreativeId();
        }
    //}
    $creativeIDs = array_unique($creativeIDs);
    //print_r($creativeIDs);

    //Build the where clause of creative IDs to find the creatives
    $creativeWhereClause = "";
    foreach ($creativeIDs as $creativeID) {
        if ($creativeWhereClause != "") {$creativeWhereClause .= " OR ";}
        $creativeWhereClause .= "(id = " . $creativeID . ")";
    }

    //Create the statement to select creatives for the passed IDs
$creativeService = $dfpServices->get($session, CreativeService::class);
    $statementBuilder = new StatementBuilder();
    $statementBuilder->Where("(" . $creativeWhereClause . ")")->OrderBy('id ASC');
    $creativeResults = $creativeService->getCreativesByStatement($statementBuilder->ToStatement());

//print_r($creativeResults->getResults());
//$creativeClass = get_class($creativeResults->getResults()[2]);
//print_r(get_class_methods($creativeClass));
//print_r($creativeResults->getResults()[2]->getCreativeTemplateVariableValues());
//print_r($creativeResults->getResults()[2]->getCreativeTemplateVariableValues()[0]->getAsset()->getAssetUrl());
//print_r(get_class_methods($creativeResults->getResults()[2]->getCreativeTemplateVariableValues()[0]->getAsset()));


    if ($creativeResults->getResults()) {
        foreach ($creativeResults->getResults() as $creative) {

          echo "Creative class: " . get_class($creative) . "\n";
          $classParts = explode("\\", get_class($creative));
          echo array_pop($classParts) . "\n";

          //DFP returns an array of different creative objects. In order to know
          //what type of object each is, some reflection is necessary. For the time
          //being, checking the methods is the clearest. In the future, using the
          //class name might become prefer.
          rable.
        $createiveHasHTML = method_exists($creative, "getHtmlSnippet");
        $creativeHasExpanded = method_exists($creative, "getExpandedSnippet");
        $creativeHasPrimaryImageAsset = method_exists($creative, "getPrimaryImageAsset");
        $creativeHasImageURL = method_exists($creative, "getImageURL");
        $creativeHasVariableAsset = false;
        $creativeVariableAssetURL = "";
        if (method_exists($creative, "getCreativeTemplateVariableValues")) {
          $templateVariables = $creative->getCreativeTemplateVariableValues();
          foreach($templateVariables as $currentVariable) {
            if (method_exists($currentVariable, "getAsset")) {
              $creativeAsset = $currentVariable->getAsset();
              if (method_exists($creativeAsset, "getAssetUrl")) {
                $creativeHasVariableAsset = true;
                $creativeVariableAssetURL = $creativeAsset->getAssetURL();
              }
            }
          }
        }

          $tag = "";
          if ($creativeHasExpanded && $creative->getExpandedSnippet()) {$tag = $creative->getExpandedSnippet();}
          else if ($createiveHasHTML && $creative->getHtmlSnippet()) {$tag = $creative->getHtmlSnippet();}
          else if ($creativeHasPrimaryImageAsset && $creative->getPrimaryImageAsset()) {
            $tag = "<img src='" . $creative->getPrimaryImageAsset()->getAssetUrl() . "' />";
          }
          else if ($creativeHasImageURL && $creative->getImageURL()) {
            $tag = "<img src='" . $creative->getImageURL() . "' />";
          }
          else if ($creativeHasVariableAsset) {
            $tag = "<img src='" . $creativeVariableAssetURL . "' />";
          }

            $creatives[$creative->getId()] = $tag;
        }
    }

        print_r($creatives);

