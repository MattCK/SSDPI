<?PHP

require 'vendor/autoload.php';

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
use Google\AdsApi\Dfp\v201702\InventoryService;
use Google\AdsApi\Dfp\v201702\PlacementService;


$resultsString = "";

// Generate a refreshable OAuth2 credential for authentication.
$oAuth2Credential = (new OAuth2TokenBuilder())
    ->fromFile()
    ->build();

// Construct an API session configured from a properties file and the OAuth2
// credentials above.
$session = (new DfpSessionBuilder())
    ->fromFile()
    ->withOAuth2Credential($oAuth2Credential)
    ->build();

$dfpServices = new DfpServices();

$orderService = $dfpServices->get($session, OrderService::class);

// Create a statement to select orders.
$pageSize = StatementBuilder::SUGGESTED_PAGE_LIMIT;
$statementBuilder = (new StatementBuilder())
    ->orderBy('id ASC')
    ->limit($pageSize);

$page = $orderService->getOrdersByStatement($statementBuilder->toStatement());

print_r($page->getResults()[7]);
exit;
$resultsString .= "Order: \n\n";
$resultsString .= print_r($page->getResults()[7], true) . "\n\n";

$companyService = $dfpServices->get($session, CompanyService::class);

$page = $companyService->getCompaniesByStatement($statementBuilder->toStatement());
//print_r($page->getResults()[10]);

$resultsString .= "Company: \n\n";
$resultsString .= print_r($page->getResults()[10], true) . "\n\n";


$lineItemService = $dfpServices->get($session, LineItemService::class);

$liStatementBuilder = new StatementBuilder();
$liStatementBuilder->Where('orderId = ' . "929655390")->OrderBy('id ASC');
$page = $lineItemService->getLineItemsByStatement($liStatementBuilder->ToStatement());

//print_r($page->getResults()[0]);
$resultsString .= "Line Item: \n\n";
$resultsString .= print_r($page->getResults()[0], true) . "\n\n";

$lineItemCreativeAssociationService = $dfpServices->get($session, LineItemCreativeAssociationService::class);

$liStatementBuilder = new StatementBuilder();
$liStatementBuilder->Where('lineItemId = ' . "2770625310")->OrderBy('id ASC');
$page = $lineItemCreativeAssociationService->getLineItemCreativeAssociationsByStatement($liStatementBuilder->ToStatement());

//print_r($page->getResults()[0]);
$resultsString .= "LICA: \n\n";
$resultsString .= print_r($page->getResults()[0], true) . "\n\n";

$creativeService = $dfpServices->get($session, CreativeService::class);
$creativeBuilder = new StatementBuilder();
$creativeBuilder->Where('id = ' . "114153447390")->OrderBy('id ASC');

$page = $creativeService->getCreativesByStatement($creativeBuilder->toStatement());
//print_r($page->getResults()[0]);

$resultsString .= "Creative: \n\n";
$resultsString .= print_r($page->getResults()[0], true) . "\n\n";


$inventoryService = $dfpServices->get($session, InventoryService::class);
$page = $inventoryService->getAdUnitsByStatement($statementBuilder->toStatement());
//print_r($page->getResults()[3]);

$resultsString .= "Ad Unit: \n\n";
$resultsString .= print_r($page->getResults()[3], true) . "\n\n";


$placementService = $dfpServices->get($session, PlacementService::class);
$page = $placementService->getPlacementsByStatement($statementBuilder->toStatement());
print_r($page->getResults()[0]);

$resultsString .= "Placement: \n\n";
$resultsString .= print_r($page->getResults()[0], true) . "\n\n";

file_put_contents("dfpAPIDump.txt", $resultsString);

//document.getElementsByTagName("script")[33].innerHTML