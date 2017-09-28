/**
* Contains the AdShotRunner object (asr) that controls the main tech preview app
*
* @package AdShotRunner
* @subpackage JavaScript
*/
let lineItemsDialog: {open: () => void, close: () => void};
interface JQuery<TElement extends Node = HTMLElement> {
	tablesorter: (something: any) => void;
	spectrum: (something: any) => void;
}

interface ServerResponse {
	data: any;
	success: boolean;
	message: string;
}


/**
* The AdShotRunner object that controls the main tech preview app
*/
namespace asr {

	interface DFPOrder {
		name: string;
		notes: string;
		advertiserName: string;
		agencyName: string;
	}

	interface DFPCreative {
		type: string;
		tag: string;
		previewURL: string;
		width: number;
		height: number;
	}

	interface DFPLineItem {
		name: string;
		notes: string;
		status: string;
		creatives: number[];
	}

	//------------------------------------ Pages --------------------------------------
	/** Input element for user to enter publisher site */
	// let domainInputElement: HTMLInputElement;
	// $(function() {domainInputElement = <HTMLInputElement> base.nodeFromID("domain");});


	/** URL of request page used to retrieve the menu  */
	const getMenuURL = 'getMenu.php';				
	/** List of menu items returned from the get menu request */
	let menuItems : {[key: string]: string} = {};	
	/** HTML options string of menu labels and their URLs */		
	let menuOptions = "";								
	/** Stores the index for the next page row to define its array number */
	let rowIndex = 0;	
	/** Stores the sites the user has requested for screenshots. [domain => Site] */
	let sites = new Map<string, Site>();									

	/**
	* Requests the menu for the domain stored in the domain input field. If a menu is returned, its items are
	* stored and an HTML options string for them is stored for inserting into menus. Finally, the domain
	* input box is removed and the add pages table is displayed.
	*/
	export function getMenu() {

		//Disable the call button
		base.disable("getMenuButton");
		base.disable("domain");

		//Remove the http/https from the domain if present
		let domainInputElement = <HTMLInputElement> base.nodeFromID("domain");
		let targetDomain = domainInputElement.value.replace(/^https?\:\/\//i, "");
		let domainParts = targetDomain.split("/");
		if (domainParts.length > 0) {targetDomain = domainParts[0];}
		domainInputElement.value = targetDomain;

		//If that domain already exists, re-enable the field and button do nothing
		if (sites.has(targetDomain)) {
			base.enable("getMenuButton");
			base.enable("domain");
			return;
		}

		//Callback that on success stores menu items, puts them into and HTML options string, and shows pages table
		let onSuccessCallback = function(response: ServerResponse) {
			
			//If successful, store the items, create the HTML options, and show the pages table
			if (response.success) {
				
				//Convert the menu items to a Map and add a "Main Page" entry at the beginning
				let menuItemsMap = new Map<string, string>();
				menuItemsMap.set("Main Page", targetDomain + "/");
				for (let menuTitle of Object.keys(response.data) ) {
					menuItemsMap.set(menuTitle, response.data[menuTitle]);
				}	

				//Create the new site and store it with its domain as key
				let newSite = new Site(targetDomain, menuItemsMap);
				sites.set(targetDomain, newSite);
			}
						
			//If failure, show us the message returned from the server.
			else {
				alert(response.message);
			}

			//Re-enable the call button
			base.enable("getMenuButton");
			base.enable("domain");
		}

		//If there was a problem connecting with the server, notify the user and enable the input field/button
		let onFailCallback = function(textStatus: string, errorThrown: string) {

			//Show the error message
			showErrorMessage("trying to get the site sections.");

			//Re-enable the call button
			base.enable("getMenuButton");
			base.enable("domain");
		}
		
		//Make the request
		base.asyncRequest(getMenuURL, 'domain=' + targetDomain, onSuccessCallback, onFailCallback);
	}




	const _getTagImagesURL = 'getTagImage2.php';				//URL of request page to turn tags into images
	const _uploadTagImageURL = 'uploadTagImage2.php';			//URL of request page to upload tag image and store it
	const _requestScreenshotsURL = 'requestScreenshots2.php';	//URL of request page to send information to in order to create screenshots
	const _uploadBackgroundImageURL = 'uploadBackgroundImage.php';	//URL of request page to upload PowerPoint background image and store it
	const _storeTagTextURL = 'storeTagText.php';				//URL of request page that stores tag text for analysis
	const _searchOrdersURL = 'searchDFPOrders.php';			//URL of request page to get line items and creatives for an order
	const _getOrderDataURL = 'getOrderData.php';				//URL of request page to get line items and creatives for an order
	const _getCreativeURL = 'getCreative.php';					//URL of request page to get Creatives
	const _creativeRefreshTimeout = 3000;							//Interval the client should check to see if a tag image is ready
	const _tagCountDisplayTime = 5000;						//The amount of time to show the message of how many tags were added

	let _tagsBeingProcessed = 0;								//Number of tags being processed into images
	let _matchingOrders: {[key: number]: DFPOrder} = {};		//Retrieved DFP orders matching the latest search term
	let _lineItems: {[key: number]: DFPLineItem} = {};		//Array of line item names and their descriptions
	let _dfpCreatives: {[key: number]: DFPCreative} = {};	//Array of creative IDs and their content
	let _creatives = new Map<number, Creative>();			//Map of Creative IDs and their Creatives available to inject into pages


	function showTagCountAdded(numberOfTags: number): void {

		console.log("In show tag count"); 
		//Set the message
		let tagCountMessageDiv = base.nodeFromID("tagCountMessageDiv");
		if (numberOfTags == 0) {
			tagCountMessageDiv.innerHTML = "No usable tags detected. Please check the text and try again.";
		}
		else if (numberOfTags == 1) {
			tagCountMessageDiv.innerHTML = "1 Tag Added";
		}
		else {
			tagCountMessageDiv.innerHTML = numberOfTags + " Tags Added";
		}

		//Show the message
		base.show("tagCountMessageDiv");

		//Hide the message after the display timeout
		setTimeout(function() {$("#tagCountMessageDiv").slideUp();}, _tagCountDisplayTime);
	}

	/**
	* Sends the queued tags to the server for imaging
	*/
	export function getTagImages(tagTextArray: string[]) {

		//Show the message for the amount of tags passed/found
		showTagCountAdded(tagTextArray.length);

		//If no tags were queued, do nothing
		if (tagTextArray.length == 0) {return;}

		//If there was a problem connecting with the server, notify the user
		let onFailCallback = function(textStatus: string, errorThrown: string) {

			//Show the error message
			showErrorMessage("trying to process image of tag.");
		}
		
		//Make a request for each tag
		base.disable("getTagImagesButton");
		for (let tagIndex = 0; tagIndex < tagTextArray.length; ++tagIndex) {
		    ++_tagsBeingProcessed;
			base.asyncRequest(_getTagImagesURL, {'tag': tagTextArray[tagIndex]}, getCreative, onFailCallback);
		}
	}

	/**
	* Uploads the tag image to the server where it is placed in official tag image storage
	*/
	export function uploadTagImage(tagImageData: File) {

		//If no tag image was passed, do nothing
		if (!tagImageData) {return;}
		
		//If there was a problem connecting with the server, notify the user and enable the input field/button
		let onFailCallback = function(textStatus: string, errorThrown: string) {

			//Show the error message
			showErrorMessage("trying to upload the tag image.");
		}
		
		//Create the request object for the raw form data
		let formData = new FormData();
		formData.append('image', tagImageData);
		base.asyncRequest(_uploadTagImageURL, formData, getCreative, onFailCallback, true);

		//Note we are processing a new tag
        ++_tagsBeingProcessed;
	}

	export function getCreative(serverResponse: ServerResponse) {

		//If successful, create or update the Creative in the Creatives sortable list. Query again if the Creative is not finished
		if (serverResponse.success) {
			
			//Convert the JSON to a Creative object and store it
			let currentCreative = new Creative(serverResponse.data.creativeJSON);

			//If this is a new Creative, create the table row
			if (!_creatives.has(currentCreative.id())) {

				let $li = $("<li class='ui-state-default' id='tagLI" + currentCreative.id() + "' />").html('');
			    $("#sortable").append($li);
			    $("#sortable").sortable('refresh');	
			}

			//Store the new or updated Creative
		    _creatives.set(currentCreative.id(), currentCreative);			

			//Determine the table row contents based on the Creative status
			if ((currentCreative.status() == Creative.READY) || (currentCreative.status() == Creative.QUEUED)) {
				base.nodeFromID("tagLI" + currentCreative.id()).innerHTML = '<div class="queuedTagDiv">Creative Status: Queued</div>';
			}

			else if (currentCreative.status() == Creative.PROCESSING) {
				base.nodeFromID("tagLI" + currentCreative.id()).innerHTML = 
											'<div class="queuedTagDiv">Creative Status: Processing (Waiting for Animations to Finish)</div>';
			}

			else if ((currentCreative.status() == Creative.ERROR) && (currentCreative.finalError())) {
				base.nodeFromID("tagLI" + currentCreative.id()).innerHTML = 
											'<div class="queuedTagDiv">Creative Status: Error - Unable to Process Tag Image</div>';
		        --_tagsBeingProcessed;
			}

			else if (currentCreative.status() == Creative.FINISHED) {
				let imageLIHTML = "";
				imageLIHTML += 	'<div class="tagImageRowDiv">';
				imageLIHTML += 		'<div class="tagImageInfoDiv">';
				imageLIHTML += 			'<div class="tagDimensionsDiv">' + currentCreative.width() + 'x' + currentCreative.height() + '</div>';
				imageLIHTML += 			'<div class="tagImageDiv"><img rowTag="" style="max-height: 120px;" src="' + currentCreative.imageURL() + '" /></div>';
				imageLIHTML += 		'</div>';
				imageLIHTML += 		'<div class="deleteButtonDiv">';
				imageLIHTML +=  		"<input type='button' class='button-tiny' value='Remove' onClick='asr.deleteTagImageListItem(" + currentCreative.id() + ")'>";
				imageLIHTML += 		'</div>';
				imageLIHTML += 	'</div>';
		        base.nodeFromID("tagLI" + currentCreative.id()).innerHTML = imageLIHTML;
		        --_tagsBeingProcessed;
			}

			//If the Creative is not FINISHED, get the Creative again in a few seconds
			if ((currentCreative.status() != Creative.FINISHED) ||
				((currentCreative.status() == Creative.ERROR) && (!currentCreative.finalError()))) {

				//If there was a failure getting the response, do nothing in case of user internet outage and try getting Creative again
				let onFailCallback = function(textStatus: string, errorThrown: string) {

					//Show the error message
					showErrorMessage("trying to get Creative image.");
				}

				//Get the Creative again in a few seconds
				setTimeout(function() {
					base.asyncRequest(_getCreativeURL, 'id=' + currentCreative.id(), getCreative, onFailCallback);
				}, _creativeRefreshTimeout);

			}
		}
					
		//If failure, show error
		else {
			showErrorMessage("trying to get Creative image.");
		}
	}

	function verifyCampaignFormData(): boolean {

		//Verify a customer name was entered
		let customerNameInput = <HTMLInputElement> base.nodeFromID("customerName");
		if (customerNameInput.value == "") {
			base.nodeFromID("customerHeader").scrollIntoView();
			customerNameInput.focus();
			base.show("emptyCustomerFieldMessage");
			return false;
		}

		//Verify pages have been entered by querying the sites
		let totalPageCount = 0;
		for (let currentSite of sites.values()) {
			totalPageCount += currentSite.pageCount();
		}
		if (totalPageCount == 0) {
			base.nodeFromID("campaignPagesHeader").scrollIntoView();
			base.hide("emptyURLFieldsMessage");
			base.show("noPagesAddedMessage");
			return false;
		}

		//Verify all the pages with manual URL text fields have had them filled out
		for (let currentSite of sites.values()) {
			if (currentSite.hasEmptyURLField()) {
				base.nodeFromID("campaignPagesHeader").scrollIntoView();
				base.hide("noPagesAddedMessage");
				base.show("emptyURLFieldsMessage");
				return false;
			}
		}

		//If images are still processing, ask the user to wait
		if (_tagsBeingProcessed > 0) {
			base.showMessage("Please wait for all tags to finish being imaged before continuing.", "Tags Are Still Being Imaged");
			return false;
		}

		return true;
	}

	/**
	* Sends a screenshot job request to the server with the job ID, campaign, and tag information.
	*
	* On success, the user is sent to the job queued page
	*/
	export function requestScreenshots() {

		//If the form is incomplete, do nothing
		if (!verifyCampaignFormData()) {return;}

		//Loop through the sites and get the AdShotBuilderJSON for each AdShot
		let adShots: string[] = [];
		for (let currentSite of sites.values()) {
			adShots = adShots.concat(currentSite.getAdShotBuilderJSON(_creatives));
		}

		//Get the creative priorities
		let creativePriorities: {[key: string]: number} = {};
		let sortedIDList = $("#sortable").sortable('toArray');
		for (let sortedIndex = 0; sortedIndex < sortedIDList.length; ++sortedIndex) {

			let creativeID = sortedIDList[sortedIndex].substring(5);
			creativePriorities[creativeID] = sortedIndex;
		}

		//Create the onSuccessCallback function that will navigate to the job queued page
		let onSuccessCallback = function(response: ServerResponse) {
			
			//If successful, navigate to the queued job page
			if (response.success) {
				window.open('/campaignResults.php?uuid=' + response.data.uuid, '_blank');
			}
						
			//If failure, simply output to the console for the time being
			else {
				console.log('in failure');
				console.log(response.data);
			}
		}

		//If there was a problem connecting with the server, notify the user and enable the input field/button
		let onFailCallback = function(textStatus: string, errorThrown: string) {

			//Show the error message
			showErrorMessage("trying to request screenshots.");
		}
		

		//Make the request
		base.asyncRequest(_requestScreenshotsURL, 
						 {adShots: adShots, 
						  creativePriorities: creativePriorities,
						  customerName: (<HTMLInputElement> base.nodeFromID("customerName")).value, 
						  backgroundID: (<HTMLInputElement> base.nodeFromID("backgroundID")).value}, 
						  onSuccessCallback, onFailCallback);
	}



	/**
	* Deletes the list item in the tag images list with the passed ID
	*
	* @param {Integer} creativeID  	ID of the list item in the tag images list to delete
	*/
	export function deleteTagImageListItem(creativeID: number) {

		//Remove the row
		let liToDelete = base.nodeFromID("tagLI" + creativeID);
		if (liToDelete.parentNode) {liToDelete.parentNode.removeChild(liToDelete);}
		let result = _creatives.delete(creativeID);
	}

	export function searchOrders() {

		//Check to see if a search term has been entered
		let searchTerm = (<HTMLInputElement> base.nodeFromID("orderSearchTerm")).value;
		if (searchTerm.length < 3) {return;}

		//Disable the search button
		base.disable("orderSearchButton");

		//Create the onSuccessCallback function that will display the information
		let onSuccessCallback = function(response: ServerResponse) {

			//Enable the search button
			base.enable("orderSearchButton");
			
			//If successful, show the information
			if (response.success) {

				//Store the order data
				_matchingOrders = response.data;

				//Create the order options
				let orderOptions = "";
				for (let orderID in _matchingOrders) {
					if (_matchingOrders.hasOwnProperty(orderID)) {

						//Create the order label
						let orderName = _matchingOrders[orderID].name;
						if (_matchingOrders[orderID].advertiserName) {
							orderName += " - " + _matchingOrders[orderID].advertiserName;
							if (_matchingOrders[orderID].agencyName) {orderName += " (" + _matchingOrders[orderID].agencyName + ")";}
						}
						orderName += " - " + orderID;
						orderOptions += "<option value='" + orderID + "'>" + orderName + "</option>";
					}
				}

				//Add the order options to the select menu
				base.nodeFromID("orderSelect").innerHTML = orderOptions;
				base.nodeFromID("orderNotesDiv").innerHTML = "";

			}
						
			//If failure, simply output to the console for the time being and enable the submit button
			else {
				console.log('in search order data failure');
				console.log(response.data);
				base.enable("orderSearchButton");
			}
		}
		
		//If there was a problem connecting with the server, notify the user and enable the input field/button
		let onFailCallback = function(textStatus: string, errorThrown: string) {

			//Show the error message
			showErrorMessage("trying to search orders.");

			console.log('in search order data failure');

			base.enable("orderSearchButton");
		}

		//Make the request
		base.asyncRequest(_searchOrdersURL, 'searchTerm=' + searchTerm, onSuccessCallback, onFailCallback);
	}


	export function displayOrderNotes() {
		let orderID = (<HTMLSelectElement> base.nodeFromID("orderSelect")).value;
		if (orderID) {
			base.nodeFromID("orderNotesDiv").innerHTML = _matchingOrders[parseInt(orderID)].notes;
		}
	}

	export function requestOrderData() {

		//Check to see if an order has been selected
		let orderID = (<HTMLSelectElement> base.nodeFromID("orderSelect")).value;
		if (!orderID) {return;}
		base.disable("getOrderDataButton");

		//Create the onSuccessCallback function that will display the information
		let onSuccessCallback = function(response: ServerResponse) {

			base.enable("getOrderDataButton");

			base.disable("lineItemsButton");
			base.hide("tooManyCreativeDiv");
			
			//If successful, show the information
			if (response.success) {

				//Store the order data
				_lineItems = response.data.lineItems;
				_dfpCreatives = response.data.creatives;

				//Create the line items table headers
				let tableHeaders =  "<thead><tr>";
				tableHeaders += 	"<th>&nbsp;</th>";
				tableHeaders += 	"<th>Name</th>";
				tableHeaders += 	"<th>Status</th>";
				tableHeaders += 	"<th>Creatives</th>";
				tableHeaders += 	"</tr></thead>";

				//Put the line items in the line items table
				let lineItemRows = "";
				let lineItemCount = 0;
				for (let lineItemID in _lineItems) {
					if (_lineItems.hasOwnProperty(lineItemID)) {

						//Get the line item
						let currentLineItem = _lineItems[lineItemID];

						//Create the checkbox ID
						let checkboxID = "lineItemCheckBox_" + lineItemID;

						lineItemRows += "<tr><td><input type='checkbox' id='" + checkboxID + "' value='" + lineItemID + "'></td>";
						lineItemRows += "<td><label for='" + checkboxID + "'>" + currentLineItem.name + "</label></td>";
						lineItemRows += "<td><label for='" + checkboxID + "'>" + currentLineItem.status + "</label></td>";
						lineItemRows += "<td><label for='" + checkboxID + "'>" + currentLineItem.creatives.length + "</label></td></tr>";

						++lineItemCount;
					}
				}
				base.nodeFromID("lineItemsTableDiv").innerHTML = '<table id="lineItemsTable" class="tablesorter">' +
				 												tableHeaders + "<tbody>" + lineItemRows + "</tbody></table>";
				if (lineItemCount > 0) {
					$("#lineItemsTable").tablesorter({sortList: [[1,0]]}); 
		}
				$("#lineItemsTable input:checkbox").click(onLineItemSelection);
				lineItemsDialog.open();

				//Place the advertiser name in the customer field
				if (_matchingOrders[parseInt(orderID)].advertiserName) {
					(<HTMLInputElement> base.nodeFromID("customerName")).value = _matchingOrders[parseInt(orderID)].advertiserName;
				}
			}
						
			//If failure, simply output to the console for the time being and enable the submit button
			else {
				console.log('in get order data failure');
				console.log(response.data);
				base.enable("getOrderDataButton");
			}
		}
		
		//If there was a problem connecting with the server, notify the user and enable the input field/button
		let onFailCallback = function(textStatus: string, errorThrown: string) {

			//Show the error message
			showErrorMessage("trying to get the line items and creative.");

			console.log('in get order data failure');
			base.enable("getOrderDataButton");
		}

		//Make the request
		base.asyncRequest(_getOrderDataURL, 'orderID=' + orderID, onSuccessCallback, onFailCallback);
	}


	export function useSelectedLineItems() {

		//Get the selected line item IDs
		let lineItemIDs: number[] = [];
		$("#lineItemsTable input:checked").each(function( index ) {
			lineItemIDs.push(<number> $(this).val());
		});

		//If none were selected, do nothing
		if (lineItemIDs.length == 0) {return;}

		//Fill out the line items div and put together their creatives
		let creativeIDList = [];
		base.nodeFromID("lineItemsDiv").innerHTML = "";
		for (let index in lineItemIDs) {

			//Get the line item
			let currentLineItem = _lineItems[lineItemIDs[index]];

			//If there is already a line item in the div, add breaks
			if (base.nodeFromID("lineItemsDiv").innerHTML != "") {base.nodeFromID("lineItemsDiv").innerHTML += "<br><br>";}

			//Add the line item name
			base.nodeFromID("lineItemsDiv").innerHTML += "<strong>" + currentLineItem.name + " (" + currentLineItem.status + ") </strong>";

			//If a description was included, add it as well
			if (currentLineItem.notes) {
				base.nodeFromID("lineItemsDiv").innerHTML += " - " + currentLineItem.notes;
			}

			//Add the creative IDs to the list
			for (let index in currentLineItem.creatives) {
				let currentCreativeID = currentLineItem.creatives[index];
				if (creativeIDList.indexOf(currentCreativeID) < 0) {creativeIDList.push(currentCreativeID);}
				else {console.log("Found: " + currentCreativeID);}
			}
		}

		//Add the creatives to the queue if 15 or less exist
		if (creativeIDList.length <= 15) {
			for (let index in creativeIDList) {
				let currentCreativeID = creativeIDList[index];
				getTagImages(tagParser.getTags(_dfpCreatives[currentCreativeID].tag));
			}
		}

		//Hide the orders and show the line items
		base.hide("dfpOrdersHeader");
		base.hide("dfpOrdersHelpIcon");
		base.hide("dfpOrdersDiv");
		base.show("lineItemsHeader");
		base.show("lineItemsDiv");

		//Close the dialog
		lineItemsDialog.close();
	}

	export function selectAllLineItems() {
		$('#lineItemsTable input:checkbox').prop('checked', true);
		onLineItemSelection();
	}

	export function unselectAllLineItems() {
		$('#lineItemsTable input:checkbox').prop('checked', false);
		onLineItemSelection();
	}

	export function onLineItemSelection() {
		
		//Get the selected line item IDs
		let lineItemIDs: number[] = [];
		$("#lineItemsTable input:checked").each(function( index ) {
			lineItemIDs.push(<number> $(this).val());
		});

		//If none were selected, disable the button and hide the too many creatives div
		if (lineItemIDs.length == 0) {
			base.disable("lineItemsButton");
			base.hide("tooManyCreativeDiv");
			return;
		}

		//Otherwise, enable the line items button
		base.enable("lineItemsButton");

		//Get the amount of creatives selected
		let creativeIDList = [];
		for (let index in lineItemIDs) {

			//Add the creative IDs to the list
			let currentLineItem = _lineItems[lineItemIDs[index]];
			for (let index in currentLineItem.creatives) {
				let currentCreativeID = currentLineItem.creatives[index];
				if (creativeIDList.indexOf(currentCreativeID) < 0) {creativeIDList.push(currentCreativeID);}
			}
		}

		//If there are more than 15 creatives selected, show the too many div, otherwise hide it
		if (creativeIDList.length >= 15) {base.show("tooManyCreativeDiv");}
		else {base.hide("tooManyCreativeDiv");}
	}

	/**
	* Uploads the PowerPoint background image to the server where it is placed in official tag image storage
	*/
	export function uploadPowerPointBackground() {

		//Verify the fields were filled in
		if ((<HTMLInputElement> base.nodeFromID("newBackgroundName")).value.length == 0) {alert("Enter a name for the PowerPoint"); return;}
		if ((<HTMLInputElement> base.nodeFromID("newBackgroundImage")).value.length == 0) {alert("Choose an image for the background"); return;}

		//Disable the save button
		base.disable("uploadBackgroundButton");

		//Do nothing for now
		let onSuccessCallback = function(response: ServerResponse) {

			//If successful, store the items, create the HTML options, and show the pages table
			if (response.success) {
				
				//Set the background info to the newly uploaded one
				let newBackgroundInfo = response.data;
				base.nodeFromID("backgroundNameDiv").innerHTML = newBackgroundInfo.name;
				base.nodeFromID("fontColorDiv").style.backgroundColor = "#" + newBackgroundInfo.fontColor;
				(<HTMLImageElement> base.nodeFromID("backgroundThumbnailImage")).src = newBackgroundInfo.thumbnailURL;
	
				//Set the hidden input fields
				(<HTMLInputElement> base.nodeFromID("backgroundID")).value = newBackgroundInfo.id;
				(<HTMLInputElement> base.nodeFromID("backgroundName")).value = newBackgroundInfo.name;
				(<HTMLInputElement> base.nodeFromID("backgroundFontColor")).value = newBackgroundInfo.fontColor;
				(<HTMLInputElement> base.nodeFromID("backgroundFilename")).value = newBackgroundInfo.filename;
			}
						
			//If failure, show us the message returned from the server.
			else {
				alert(response.message);
			}

			//Re-enable the save button, hide the upload form, and show the change button
			base.enable("uploadBackgroundButton");
			base.hide("uploadBackgroundDiv");
			base.show("changeBackgroundButtonDiv");
		}

		//If there was a problem connecting with the server, notify the user and enable the submit button
		let onFailCallback = function(textStatus: string, errorThrown: string) {

			//Show the error message
			showErrorMessage("trying to upload the PowerPoint background image.");

			//Re-enable the submit button
			base.enable("uploadBackgroundButton");
		}

		//Create the request object for the image raw data
		let fileNode = <HTMLInputElement> base.nodeFromID("newBackgroundImage");
		let imageFile = (<FileList> fileNode.files)[0];
		let formData = new FormData();
		formData.append('backgroundName', (<HTMLInputElement> base.nodeFromID("newBackgroundName")).value);
		formData.append('backgroundFontColor',(<HTMLInputElement>  base.nodeFromID("newBackgroundFontColor")).value.substring(1));
		formData.append('backgroundImage', imageFile);
		base.asyncRequest(_uploadBackgroundImageURL, formData, onSuccessCallback, onFailCallback, true);
	}

	/**
	* Display a "problem communicating with the server" error message.
	*
	* The displayed message includes a link to open the contact form.
	*
	* If a "while" clause is returned, it is placed after the word "while" in the first line.
	* Example: 	"There was difficulty communicating with the server."
	* 			"There was difficulty communicating with the server while trying to get site sections."
	*
	* In the second example, "trying to get site sections" is passed. Including the word "while"
	* is not necessary.
	*
	* @param {string} whileClause  	Description of the error to be placed after the word "while"
	*/
	export function showErrorMessage(whileClause: string) {

		//If a "while" clause was passed, create the proper text
		let whileText = "";
		if (whileClause) {whileText = " while " + whileClause;}

		//Build the error message and show it
		let errorHTML = "There was difficulty communicating with the server" + whileText + ".<br/><br/>";
		errorHTML += "Check your internet connection and try refreshing your browser.<br/><br/>";
		errorHTML += "If the problem persists, please <a onclick='contactForm.reset(); contactFormDialog.open()'>Contact Us</a>.";
		base.showMessage(errorHTML, "A Problem has Occurred");
	}

	/**
	* Enables all of the submit buttons on the page. This is useful for when the user refreshes the page.
	* Firefox and Chrome do not re-enable the buttons if they were disabled by Javascript.
	*/
	export function enableSubmitButtons() {

		base.enable("getMenuButton");
		base.enable("domain");
		base.enable("getTagImagesButton");
		base.enable("getOrderDataButton");
		base.enable("uploadBackgroundButton");
		// base.disable("getScreenshotsButton");
		base.enable("getScreenshotsButton"); //Testing
	}

	export function addTestPages() {

		//If no sites have been created, do nothing
		if (sites.size == 0) {console.log("No domain added."); return;}

		//Add pages for each menu item
		let testSite: Site = sites.values().next().value;
		let menuItemCount: number = testSite.menuItems().size;
		for (let menuIndex: number = 0; menuIndex < menuItemCount; ++menuIndex) {
			for (let pageIndex: number = 0; pageIndex < 4; ++pageIndex) {
				testSite.addSitePageRow();
			}
		}

		//Set the menu item and options for each page
		let allRowElements: NodeList = document.querySelectorAll(".siteDiv tr");
		let rowIndex: number = 0;
		let menuIndex: number = 0;
		while (rowIndex < allRowElements.length) {

			//Set the first page simply to the menu item
			let currentRow = <HTMLTableRowElement> allRowElements[rowIndex];
			let currentPrefix = currentRow.id.substr(0, currentRow.id.length - 4);
			let rowMenu = <HTMLSelectElement> document.getElementById(currentPrefix + "-URLInput");
			rowMenu.selectedIndex = menuIndex;
			++rowIndex;

			//Set the second page to the menu item and top story
			currentRow = <HTMLTableRowElement> allRowElements[rowIndex];
			currentPrefix = currentRow.id.substr(0, currentRow.id.length - 4);
			rowMenu = <HTMLSelectElement> document.getElementById(currentPrefix + "-URLInput");
			rowMenu.selectedIndex = menuIndex;
			let storyCheckbox = <HTMLInputElement> document.getElementById(currentPrefix + "-findStoryCheckbox");
			storyCheckbox.checked = true;
			++rowIndex;
			
			//Set the third page to the menu item and mobile
			currentRow = <HTMLTableRowElement> allRowElements[rowIndex];
			currentPrefix = currentRow.id.substr(0, currentRow.id.length - 4);
			rowMenu = <HTMLSelectElement> document.getElementById(currentPrefix + "-URLInput");
			rowMenu.selectedIndex = menuIndex;
			let mobileCheckbox = <HTMLInputElement> document.getElementById(currentPrefix + "-mobileCheckbox");
			mobileCheckbox.checked = true;
			++rowIndex;
			
			//Set the fourth page to the menu item, mobile, and top story
			currentRow = <HTMLTableRowElement> allRowElements[rowIndex];
			currentPrefix = currentRow.id.substr(0, currentRow.id.length - 4);
			rowMenu = <HTMLSelectElement> document.getElementById(currentPrefix + "-URLInput");
			rowMenu.selectedIndex = menuIndex;
			storyCheckbox = <HTMLInputElement> document.getElementById(currentPrefix + "-findStoryCheckbox");
			storyCheckbox.checked = true;
			mobileCheckbox = <HTMLInputElement> document.getElementById(currentPrefix + "-mobileCheckbox");
			mobileCheckbox.checked = true;
			++rowIndex;
			++menuIndex;
		}

	}

	$(function() {

		//Enable all of the submit buttons in case they were disabled and the user did a refresh
		asr.enableSubmitButtons();

		//Make the tag image list sortable
		$( "#sortable" ).sortable();

		//Make the line items form a "pop-up" dialog
		lineItemsDialog = base.createDialog("lineItemsDialogDiv", "Line Items", true, 940);

		//Create the color selector for the final PowerPoint
		$("#newBackgroundFontColor").spectrum({
			color: "#000000",
			preferredFormat: "hex",
			showPaletteOnly: true,
			showPalette: true,
		});

	});
}

