<?PHP

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

$creativeService = $dfpServices->get($session, CreativeService::class);
    $statementBuilder = new StatementBuilder();
    $statementBuilder->Where("id = 47529485737")->OrderBy('id ASC');
    $creativeResults = $creativeService->getCreativesByStatement($statementBuilder->ToStatement());

print_r($creativeResults->getResults());

		    foreach ($creativeResults->getResults() as $creative) {

		    	//Get the creative object's class
		    	//Get the full class, separate it by \, take the last part and make it uppercase
		    	//This will remove the full class path and return only the class name in uppercase
		    	$creativeClass = strtoupper(array_pop(explode("\\", get_class($creative))));
		    	echo "Class: $creativeClass \n";

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

		    	echo "Tag: $tag \n";





		    	/*//DFP returns an array of different creative objects. In order to know
		    	//what type of object each is, some reflection is necessary. For the time
		    	//being, checking the methods is the clearest since sets of classes share
		    	//the same methods. In the future, using the class name might become preferrable.
				$createiveHasHTML = method_exists($creative, "getHtmlSnippet");
				$creativeHasExpanded = method_exists($creative, "getExpandedSnippet");
				$creativeHasPrimaryImageAsset = method_exists($creative, "getPrimaryImageAsset");
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

				//Get the tag URL/code snippet
		    	$tag = "";
		    	if ($creativeHasExpanded && $creative->getExpandedSnippet()) {$tag = $creative->getExpandedSnippet();}
		    	else if ($createiveHasHTML && $creative->getHtmlSnippet()) {$tag = $creative->getHtmlSnippet();}
		    	else if ($creativeHasPrimaryImageAsset && $creative->getPrimaryImageAsset()) {
		    		$tag = "<img src='" . $creative->getPrimaryImageAsset()->getAssetUrl() . "' />";
		    	}
		    	else if ($creativeHasVariableAsset) {
		    		$tag = "<img src='" . $creativeVariableAssetURL . "' />";
		    	}

		        $creatives[$creative->getId()] = $tag;*/
		    }


/*

Array
(
    [Google\AdsApi\Dfp\v201702\CustomCreative] => 50786591377
    [Google\AdsApi\Dfp\v201702\ImageCreative] => 50864131057
    [Google\AdsApi\Dfp\v201702\InternalRedirectCreative] => 50863917217
    [Google\AdsApi\Dfp\v201702\ThirdPartyCreative] => 50863880257
    [Google\AdsApi\Dfp\v201702\TemplateCreative] => 50856919777
    [Google\AdsApi\Dfp\v201702\FlashCreative] => 47529169897
    [Google\AdsApi\Dfp\v201702\VastRedirectCreative] => 50848846417
    [Google\AdsApi\Dfp\v201702\UnsupportedCreative] => 47529489337
    [Google\AdsApi\Dfp\v201702\VideoCreative] => 47041475377
    [Google\AdsApi\Dfp\v201702\FlashRedirectCreative] => 47529485737
    [Google\AdsApi\Dfp\v201702\ImageRedirectCreative] => 50855884177
    [Google\AdsApi\Dfp\v201702\VideoRedirectCreative] => 50853435697
)
Array
(
    [Google\AdsApi\Dfp\v201702\CustomCreative] => 2892
    [Google\AdsApi\Dfp\v201702\ImageCreative] => 12781
    [Google\AdsApi\Dfp\v201702\InternalRedirectCreative] => 572
    [Google\AdsApi\Dfp\v201702\ThirdPartyCreative] => 5482
    [Google\AdsApi\Dfp\v201702\TemplateCreative] => 2126
    [Google\AdsApi\Dfp\v201702\FlashCreative] => 156
    [Google\AdsApi\Dfp\v201702\VastRedirectCreative] => 9
    [Google\AdsApi\Dfp\v201702\UnsupportedCreative] => 6
    [Google\AdsApi\Dfp\v201702\VideoCreative] => 4
    [Google\AdsApi\Dfp\v201702\FlashRedirectCreative] => 2
    [Google\AdsApi\Dfp\v201702\ImageRedirectCreative] => 44
    [Google\AdsApi\Dfp\v201702\VideoRedirectCreative] => 22

*/