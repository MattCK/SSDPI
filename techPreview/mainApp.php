<?PHP
/**
* Main app page that controls creation of screenshots
*
* @package AdShotRunner
*/
/**
* File to define paths, setup dependencies, and connect to database
*/
require_once('systemSetup.php');

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');

use AdShotRunner\System\ASRProperties;
use AdShotRunner\Users\User;
use AdShotRunner\PowerPoint\PowerPointBackground;
use AdShotRunner\DFP\DFPCommunicator;

//Get the powerpoint background and font info for the user
$currentUser = User::getUser(USERID);
$defaultBackground = PowerPointBackground::getPowerPointBackground($currentUser->getPowerPointBackgroundID());
$backgroundTitle = $defaultBackground->getTitle();
$powerPointFontColor = $defaultBackground->getFontColor();
$backgroundFilename = $defaultBackground->getFilename();
$backgroundThumbnailFilename = $defaultBackground->getThumbnailFilename();
$backgroundURL = "https://s3.amazonaws.com/" . ASRProperties::containerForPowerPointBackgrounds() . "/thumbnails/" . $backgroundThumbnailFilename;

//If the user has already ran a campaign during this session, show the last campaign domain
$campaignDomain = ($_SESSION['lastCampaignDomain']) ? $_SESSION['lastCampaignDomain'] : "";

?>

<?PHP include_once(BASEPATH . "header.php");?>

<body>
	
<div id="header">
	<div id="title">
		<a href="/"><img id="headerLogo" src="images/headerLogo.png"/></a>
	</div>
	<div id="logout">
		<a class="contactIdeaLink">Contact Us</a>&nbsp;&nbsp;&nbsp;&nbsp;
		<a href="logout.php">Logout</a>
	</div>
</div>

<div id="mainContent">
	<form id="pagesForm">

		<?PHP if (!USERDFPNETWORKCODE): ?>
		<div id="dfpInfoDiv" class="section"> 
			<strong>Google DFP Users:</strong> 
			AdShotRunner&trade; can retrieve orders, line notes, and creatives from your account. 
			For more information, call (773) 295-2386 or <a href="mailto:<?PHP echo ASRProperties::emailAddressInfo()?>">email us</a>
		</div>
		<?PHP endif; ?>

		<?PHP if (USERDFPNETWORKCODE): ?>

		<h2 id="dfpOrdersHeader">DFP Orders</h2>
		<h2 id="lineItemsHeader" style="display: none;">Line Items</h2>
		<img helpIcon="" id="dfpOrdersHelpIcon" class="helpIcon titleHelpIcon" src="images/helpIcon.png" />
		<div class="problemLinkDiv">
			<a class="contactIssueLink">Problem?</a>
		</div>
		
		<div id="dfpOrdersDiv" class="section"> 
			<table>
				<tr>
					<td style="width: 100px; font-weight: bold;">Orders:</td>				
					<td colspan="2">
						Enter a <strong>full</strong> order ID or <strong>part</strong> of an order name, advertiser name, or agency name:
						<input id="orderSearchTerm" name="orderSearchTerm" type="text" style="width: 525px; margin-top: 5px;">
						<input id="orderSearchButton" type="button" class="button-tiny" value="Search">
					</td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td><select id="orderSelect" name="orderSelect" size="8" style="width: 600px;"></select></td>
					<td style="vertical-align: top"><div id="orderNotesDiv" style="padding: 2px 30px"></div></td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td colspan="2">
						<input id="getOrderDataButton" type="button"  class="button-tiny" value="Get Line Items and Creatives" >
					</td>
				</tr>
			</table>
		</div>

		<div id="lineItemsDiv" class="section" style="display: none;"></div> 



		<?PHP endif; ?>

		<h2>Customer</h2>
		<img helpIcon="" id="customerHelpIcon" class="helpIcon titleHelpIcon" src="images/helpIcon.png" />
		<div id="customerDiv" class="section">
			<div class="textFieldName">Name:</div> 
			<input id="customer" name="customer" type="text">
		</div>

		<h2>PowerPoint Background</h2>
		<img helpIcon="" id="powerPointBackgroundHelpIcon" class="helpIcon titleHelpIcon" src="images/helpIcon.png" />
		<div class="problemLinkDiv">
			<a class="contactIssueLink">Problem?</a>
		</div>
		<div id="powerPointBackgroundDiv" class="section">
			<input id="backgroundTitle" name="backgroundTitle" type="hidden" value="<?PHP echo $backgroundTitle?>">
			<input id="backgroundFontColor" name="backgroundFontColor" type="hidden" value="<?PHP echo $powerPointFontColor?>">
			<input id="backgroundFilename" name="backgroundFilename" type="hidden" value="<?PHP echo $backgroundFilename?>">

			<div id="currentBackgroundDiv">
				<div id="backgroundImageDiv">
					<img id="backgroundThumbnailImage" rowTag="" style="max-height: 80px;" src="<?PHP echo $backgroundURL?>" /><br/>
				</div>
				<div id="backgroundInfoDiv">
					<div id="backgroundTitleDiv" class="backgroundTitle"><?PHP echo $backgroundTitle?></div>
					<div class="backgroundFontColor">
						<div class="fontColorText">Font Color: </div>
						<div id="fontColorDiv" class="fontColorDiv" style="background-color: #<?PHP echo $powerPointFontColor?>"></div>
					</div>
				</div>
			</div>

			<div id="changeBackgroundButtonDiv">
				<input  id="changeBackgroundButton" class="button-tiny" type="button" value="Change">
			</div>

			<div id="uploadBackgroundDiv" style="display: none">
				<table>
					<tr><td>Name:</td>
						<td><input id="newBackgroundTitle" name="newBackgroundTitle" type="text" maxlength="64"></td></tr>
					<tr><td>Font Color:</td>
						<td><input id="newBackgroundFontColor" name="newBackgroundFontColor" type="text" value="#000000"></td></tr>
					<tr><td>Image:</td>
						<td><input type="file" id="newBackgroundImage" name="newBackgroundImage" accept="image/*"></td></tr>
					<tr><td>&nbsp;</td>
						<td><input class="button-tiny" id="uploadBackgroundButton" type="button" value="Save"></td></tr>
				</table>
			</div>
		</div>

		<h2 id="campaignPagesHeader">Campaign Pages</h2>
		<div id="domainInputDiv" class="section">
			<div class="textFieldName">Publisher Site:</div>
			<input id="domain" name="domain" type="text" value="<?PHP echo $campaignDomain ?>">
			<input id="getMenuButton" class="button-tiny" type="button" value="Go!">
			<img helpIcon="" id="domainInputHelpIcon" class="helpIcon" src="images/helpIcon.png" />
			<div class="problemLinkDiv">
				<a class="contactIssueLink">Problem?</a>
			</div>
		</div>

		<div id="pagesTableDiv" class="section" style="display: none">
			<input id="addSiteSectionButton" class="button-tiny" type="button" value="Add Site Section">
			<input id="addURLButton" class="button-tiny" type="button" value="Add URL">
			<span id="noMenuNotification" style="display: none;">Sections not available for this site</span>
			<img helpIcon="" id="addPagesHelpIcon" class="helpIcon" src="images/helpIcon.png" />
			<div class="problemLinkDiv">
				<a class="contactIssueLink">Problem?</a>
			</div>
			<table id="pagesTable"></table>
		</div>
	</form>


	<h2>Creative</h2>
	<img helpIcon="" id="creativeHelpIcon" class="helpIcon titleHelpIcon" src="images/helpIcon.png" />
	<div class="problemLinkDiv">
		<a class="contactIssueLink">Problem?</a>
	</div>
	<div class="section">
	   	<div class="copyPasteTextTitle">Copy and Paste Tag Text:</div>
	   	<div id="tagTextDiv">
			<textarea id="tagTextTextbox" rows="10" cols="60"></textarea>
			<input class="button-tiny" id="tagTextTextboxButton" type="button" value="Add Tags">
		</div>
		<div>
			<div id="textFileDropZone" class="dropBox">Drop Text or Image File(s)</div>
			<div id="zipFileDropZone" class="dropBox">Drop a Zip File</div>
		</div>
		
		<div id="queuedTagDiv" class="yellowBackground" align="center">
			<span id="queuedTagCountSpan">0</span> <span id="tagsQueuedSpan">Tags Queued</span> &nbsp; 
			<input class="button-tiny" id="getTagImagesButton" type="button" value="Get Tag Images" disabled>
		</div>

		<div class="priorityDescriptionDiv">Drag and drop creative images to change priority. (Nearer to the top has higher priority)</div>
		<ul id="sortable"></ul>
	</div>

	<div align="center">
		<div id="getScreenShotsDiv" class="section" align="center">
			<input class="button-tiny" id="getScreenshotsButton" type="button" value="Get screenshots")" disabled>
		</div>
	</div>

</div>

<!-- ******************************** Contact Form Div ******************************** -->

<div style="display: none;">
	<?PHP include("contactForm.php");?>
</div>

<!-- ********************************************************************************** -->

<div style="display: none;">
	<div id="lineItemsDialogDiv">
		<div id="lineItemsTableDiv" style="">
			<table id="lineItemsTable" class="tablesorter">
			</table>
		</div>
		<input id="selectAllLineItemsButton" type="button" class="button-tiny" value="Select All">
		<input id="unselectAllLineItemsButton" type="button" class="button-tiny" value="Select None">
		<input id="lineItemsButton" type="button" class="button-tiny" style="margin-left: 40px;" value="Use Selected Line Items" disabled>
		<div id="tooManyCreativeDiv" style="display: none; padding-top: 15px;">
			More than 15 creative selected. Creative will not be loaded.
		</div>
	</div>
</div>


</body>
</html>


<script>


//Reset the contact form in case the user pressed the refresh button
contactForm.reset();

//Make the tag images table sortable, make the contact form a dialog, and, if DFP is enabled, sort and show the orders
let contactFormDialog = null;
let lineItemsDialog = null;
$(function() {

	//Setup drag and drop tag listeners
	// let textFileDropZone = base.nodeFromID('textFileDropZone');
	// textFileDropZone.addEventListener('dragover', tagParser.handleDragOver, false);
	// textFileDropZone.addEventListener('drop', tagParser.handleTagTextFileDrop, false);
	// textFileDropZone.addEventListener('dragleave', tagParser.handleDragLeave, false);

	// let zipFileDropZone = base.nodeFromID('zipFileDropZone');
	// zipFileDropZone.addEventListener('dragover', tagParser.handleDragOver, false);
	// zipFileDropZone.addEventListener('drop', tagParser.handleTagZipFileDrop, false);
	// zipFileDropZone.addEventListener('dragleave', tagParser.handleDragLeave, false);

	// let tagTextTextboxButton = base.nodeFromID("tagTextTextboxButton");
	// tagTextTextboxButton.addEventListener('click', tagParser.handleTagTextboxInput, false);

	// //Setup the page listeners
	// let orderSearchButton = base.nodeFromID("orderSearchButton");
	// orderSearchButton.addEventListener('click', asr.searchOrders, false);
	// let orderSearchTermInput = base.nodeFromID("orderSearchTerm");
	// orderSearchTermInput.addEventListener('keyup', function(event) {if(event.keyCode == 13){asr.searchOrders();}}, false);

	// let orderSelect = base.nodeFromID("orderSelect");
	// orderSelect.addEventListener('change', asr.displayOrderNotes, false);



	// let selectAllLineItemsButton = base.nodeFromID("selectAllLineItemsButton");
	// selectAllLineItemsButton.addEventListener('click', asr.selectAllLineItems, false);
	// let unselectAllLineItemsButton = base.nodeFromID("unselectAllLineItemsButton");
	// unselectAllLineItemsButton.addEventListener('click', asr.unselectAllLineItems, false);
	// let lineItemsButton = base.nodeFromID("lineItemsButton");
	// lineItemsButton.addEventListener('click', asr.useSelectedLineItems, false);

	// let customerInput = base.nodeFromID("customer");
	// customerInput.addEventListener('input', asr.checkCustomerCompletion, false);

	// let changeBackgroundButton = base.nodeFromID("changeBackgroundButton");
	// changeBackgroundButton.addEventListener('click', function() {base.hide('changeBackgroundButtonDiv'); base.show('uploadBackgroundDiv');}, false);
	// let uploadBackgroundButton = base.nodeFromID("uploadBackgroundButton");
	// uploadBackgroundButton.addEventListener('click', asr.uploadPowerPointBackground, false);

	// let domainInput = base.nodeFromID("domain");
	// domainInput.addEventListener('keyup', function(event) {if(event.keyCode == 13){asr.getMenu();}}, false);
	// let getMenuButton = base.nodeFromID("getMenuButton");
	// getMenuButton.addEventListener('click', asr.getMenu, false);

	// let addSiteSectionButton = base.nodeFromID("addSiteSectionButton");
	// addSiteSectionButton.addEventListener('click', asr.addMenuSectionRow, false);
	// let addURLButton = base.nodeFromID("addURLButton");
	// addURLButton.addEventListener('click', asr.addURLRow, false);

	// let getTagImagesButton = base.nodeFromID("getTagImagesButton");
	// getTagImagesButton.addEventListener('click', asr.getTagImages, false);

	// let getScreenshotsButton = base.nodeFromID("getScreenshotsButton");
	// getScreenshotsButton.addEventListener('click', asr.requestScreenshots, false);

	// $(".contactIssueLink").click(function() {contactForm.reset(); contactForm.selectIssue(); contactFormDialog.open();});
	// $(".contactIdeaLink").click(function() {contactForm.reset(); contactFormDialog.open();});

	//Setup the paths in the ASR javascript object to the tag and powerpoint background images
	asr.tagImagesURL = "<?PHP echo "https://s3.amazonaws.com/" . ASRProperties::containerForCreativeImages() ?>/";
	asr.powerPointBackgroundsURL = "<?PHP echo "https://s3.amazonaws.com/" . ASRProperties::containerForPowerPointBackgrounds() ?>/";

	//Enable all of the submit buttons in case they were disabled and the user did a refresh
	asr.enableSubmitButtons();

	//Make the tag image list sortable
	$( "#sortable" ).sortable();

	//Make the contact form a "pop-up" dialog
	contactFormDialog = base.createDialog("contactFormDiv", "Contact Us", true, 650);

	//Make the line items form a "pop-up" dialog
	lineItemsDialog = base.createDialog("lineItemsDialogDiv", "Line Items", true, 940);

	//Create the color selector for the final PowerPoint
	$("#newBackgroundFontColor").spectrum({
		color: "#000000",
		preferredFormat: "hex",
	    showPaletteOnly: true,
	    showPalette: true,
   	});

	<?PHP if ($_GET["domain"]): ?>

		base.nodeFromID("domain").value = "<?PHP echo $_GET["domain"] ?>";
		asr.getMenu();

	<?PHP endif; ?>

	<?PHP if ($_GET["orderID"]): ?>

		base.nodeFromID("orderSelect").value = "<?PHP echo $_GET["orderID"] ?>";
		asr.requestOrderData();

	<?PHP endif; ?>
});

</script>