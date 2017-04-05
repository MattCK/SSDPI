/**
* Contains the AdShotRunner object (asr) that controls the main tech preview app
*
* @package AdShotRunner
* @subpackage JavaScript
*/

/**
* The AdShotRunner object that controls the main tech preview app
*/
let asr = {

	tagImagesURL: '',									//URL to where tag images are stored
	powerPointBackgroundsURL: '',						//URL to where PowerPoint background images are stored
	_domain: '',										//Stores the domain to get screenshots for
	_getMenuURL: 'getMenu.php',							//URL of request page used to retrieve the menu for the domain
	_getTagImagesURL: 'getTagImages.php',				//URL of request page to turn tags into images
	_uploadTagImageURL: 'uploadTagImage.php',			//URL of request page to upload tag image and store it
	_requestScreenshotsURL: 'requestScreenshots.php',	//URL of request page to send information to in order to create screenshots
	_uploadBackgroundImageURL: 'uploadBackgroundImage.php',	//URL of request page to upload PowerPoint background image and store it
	_storeTagTextURL: 'storeTagText.php',				//URL of request page that stores tag text for analysis
	_searchOrdersURL: 'searchDFPOrders.php',			//URL of request page to get line items and creatives for an order
	_getOrderDataURL: 'getOrderData.php',				//URL of request page to get line items and creatives for an order
	_menuItems: [],										//List of menu items returned from the get menu request
	_menuOptions: "",									//HTML options string of menu labels and their URLs
	_imageLoadTimeout: 3000,							//Interval the client should check to see if a tag image is ready
	_rowIndex: 0,										//Stores the index for the next page row to define its array number. 
														//(Possibly unnecessary but more robust)
	_queuedTags: [],									//Array of tags that need to be processed into images
	_tagsBeingProcessed: 0,								//Number of tags being processed into images
	_matchingOrders: [],								//Retrieved DFP orders matching the latest search term
	_lineItems: [],										//Array of line item names and their descriptions
	_creatives: [],										//Array of creative IDs and their content
	orders: {},											//Array of order IDs with 'name' and 'notes' properties

	checkCustomerCompletion: function() {

		/*if (base.nodeFromID("customer").value == "") {
			base.nodeFromID("customer").classList.add("customerFieldEmpty");
		}
		else {
			base.nodeFromID("customer").classList.remove("customerFieldEmpty");
		}*/
		asr.setGetScreenshotsButtonStatus();

		
	},

	/**
	* Requests the menu for the domain stored in the domain input field. If a menu is returned, its items are
	* stored and an HTML options string for them is stored for inserting into menus. Finally, the domain
	* input box is removed and the add pages table is displayed.
	*/
	getMenu: function() {

		//Disable the call button
		base.disable("getMenuButton");
		base.disable("domain");

		//Remove the http/https from the domain if present
		let targetDomain = base.nodeFromID('domain').value.replace(/^https?\:\/\//i, "");
		let domainParts = targetDomain.split("/");
		if (domainParts.length > 0) {targetDomain = domainParts[0];}
		base.nodeFromID('domain').value = targetDomain;

		//Callback that on success stores menu items, puts them into and HTML options string, and shows pages table
		let onSuccessCallback = function(response) {
			
			//If successful, store the items, create the HTML options, and show the pages table
			if (response.success) {
				
				//Store the menu items and create the menu options
				asr._menuItems = response.data;
				let menuItemCount = 0;
				asr._menuOptions = "<option value='" + base.nodeFromID('domain').value + "/'>Main Page</option>";
				for (let menuTitle in asr._menuItems) {
					if (asr._menuItems.hasOwnProperty(menuTitle)) {
						asr._menuOptions += "<option value='" + asr._menuItems[menuTitle] + "'>" + menuTitle + "</option>";
						++menuItemCount;
					}
				}	

				//Store the domain, hide it, and show the add pages table
				asr._domain = base.nodeFromID('domain').value;
				base.hide("domainInputDiv");
				base.nodeFromID("campaignPagesHeader").innerHTML = "Campaign Pages: <a href='http://" + asr._domain + "' target='_blank'>" + asr._domain + "</a>";
				base.show("domainNameDiv");
				base.show("pagesTableDiv");

				//If there are less than 2 menu items, disable the add site section button and show the notification
				if (menuItemCount <= 2) {
					base.disable("addSiteSectionButton");
					base.show("noMenuNotification");
				}
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
		let onFailCallback = function(textStatus, errorThrown) {

			//Show the error message
			asr.showErrorMessage("trying to get the site sections.");

			//Re-enable the call button
			base.enable("getMenuButton");
			base.enable("domain");
		}
		
		//Make the request
		base.asyncRequest(asr._getMenuURL, 'domain=' + base.nodeFromID('domain').value, onSuccessCallback, onFailCallback);
	},

	/**
	* Adds a row to the pages table that allows the user to choose a site menu section and its options
	*/
	addMenuSectionRow: function() {

		//Insert a new row into the pages table
		let pageTable = base.nodeFromID("pagesTable");
		let newRow =  pageTable.insertRow(pageTable.rows.length);

		//Create the id used to reference the row in the future
		newRow.id = "pageRow" + asr._rowIndex;

		//Build the row cells and insert them
		rowCells =  "<td><select name='pages[" + asr._rowIndex + "]'>" + asr._menuOptions + "</select></td>";
		rowCells += "<td><label><input type='checkbox' name='findStory[" + asr._rowIndex + "]' value='1'>Story</label></td>";
		rowCells += "<td><label><input type='checkbox' name='useMobile[" + asr._rowIndex + "]' value='1'>Mobile</label></td>";
		
		rowCells += "<td><label><input type='radio' name='screenshotType[" + asr._rowIndex + "]' value='all' checked>All Creative</label>";
		rowCells += "    <label><input type='radio' name='screenshotType[" + asr._rowIndex + "]' value='individual'>Individual Creative Screenshots</label>";
		rowCells += "    <label><input type='radio' name='screenshotType[" + asr._rowIndex + "]' value='none'>No Creative</label></td>";
		
		rowCells += "<td><input class='button-tiny' type='button' value='Remove' onClick='asr.deletePageRow(" + asr._rowIndex + ")'></td>";
		newRow.innerHTML = rowCells;

		//Increment the row index for the next inserted row
		asr._rowIndex += 1;

		//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
		asr.setGetScreenshotsButtonStatus();
	},

	/**
	* Adds a row to the pages table that allows the user to enter a specific URL
	*/
	addURLRow: function() {

		//Insert a new row into the pages table
		let pageTable = base.nodeFromID("pagesTable");
		let newRow =  pageTable.insertRow(pageTable.rows.length);

		//Create the id used to reference the row in the future
		newRow.id = "pageRow" + asr._rowIndex;

		//Build the row cells and insert them
		rowCells = "<td colspan='2' class='pageURLTitle'>Page URL: ";
		rowCells += "<input type='text' name='pages[" + asr._rowIndex + "]'></td>";
		rowCells += "<td><label><input type='checkbox' name='useMobile[" + asr._rowIndex + "]' value='1'>Mobile</label></td>";

		rowCells += "<td><label><input type='radio' name='screenshotType[" + asr._rowIndex + "]' value='all' checked>All Creative</label>";
		rowCells += "    <label><input type='radio' name='screenshotType[" + asr._rowIndex + "]' value='individual'>Individual Creative Screenshots</label>";
		rowCells += "    <label><input type='radio' name='screenshotType[" + asr._rowIndex + "]' value='none'>No Creative</label></td>";
		
		rowCells += "<td><input class='button-tiny' type='button' value='Remove' onClick='asr.deletePageRow(" + asr._rowIndex + ")'></td>";
		newRow.innerHTML = rowCells;

		//Increment the row index for the next inserted row
		asr._rowIndex += 1;

		//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
		asr.setGetScreenshotsButtonStatus();
	},

	/*
	addAllPages: function() {

		let pageIndex = 0;

		for (let menuTitle in asr._menuItems) {

			if (asr._menuItems.hasOwnProperty(menuTitle)) {

				let pageTable = base.nodeFromID("pagesTable");
				let newRow =  pageTable.insertRow(pageTable.rows.length);

				newRow.id = "pageRow" + asr._rowIndex;

				rowCells = "<td><select id='pageSelect" + asr._rowIndex + "' name='pages[" + asr._rowIndex + "]'>" + asr._menuOptions + "</select></td>";
				rowCells += "<td><select name='findStory[" + asr._rowIndex + "]'><option value=0>Front Page</option><option value=1>Find me a story</option></select></td>";
				rowCells += "<td><input type='checkbox' name='onlyScreenshot[" + asr._rowIndex + "]' value='1'>Take screenshot without inserting tags</td>";
				rowCells += "<td><input type='button' value='Delete' onClick='asr.deletePageRow(" + asr._rowIndex + ")'></td>";
				newRow.innerHTML = rowCells;

				base.nodeFromID("pageSelect" + asr._rowIndex).selectedIndex = pageIndex;

				asr._rowIndex += 1;
				pageIndex += 1;
			}
		}

		base.enable("addTagsButton");
	},
	//*/

	/**
	* Deletes the row in the pages table with the passed ID number
	*
	* @param {Integer} rowID  	ID of the row in the pages table to delete
	*/
	deletePageRow: function(rowID) {

		//Remove the row
		let rowToDelete = base.nodeFromID("pageRow" + rowID);
		rowToDelete.parentNode.removeChild(rowToDelete);
	
		//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
		asr.setGetScreenshotsButtonStatus();
	},

	/**
	* Sends the queued tags to the server for imaging
	*/
	getTagImages: function() {

		//If no tags were queued, do nothing
		if (!asr._queuedTags.length) {return;}

		//Generate a unique ID for each tag and create a "queued" table row
		let tagsByID = {};
		for (let tagIndex = 0; tagIndex < asr._queuedTags.length; ++tagIndex) {

			//Create the tag UUID and place it in the object to be sent to the server
			let newUUID = asr.getUUID();
			tagsByID[newUUID] = asr._queuedTags[tagIndex];

			//Add the "queued" row to the tags table
		    let $li = $("<li class='ui-state-default' id='tagLI" + newUUID + "' />").html('<div class="queuedTagDiv">Queued...</div>');
		    $("#sortable").append($li);
		    $("#sortable").sortable('refresh');

		    //Increase the count of tags being processed into images
		    ++asr._tagsBeingProcessed;

		    //Start checking for the image to be done.
			asr.loadTagImage(asr.tagImagesURL + newUUID + ".png", "tagLI" + newUUID);
		}	

		//Remove the queued tags
		let onSuccessCallback = function(response) {
			asr._queuedTags = [];
			base.nodeFromID("queuedTagCountSpan").innerHTML = 0;
			console.log(response.data);
			base.nodeFromID("queuedTagDiv").className = "yellowBackground";
		}
		
		//If there was a problem connecting with the server, notify the user
		let onFailCallback = function(textStatus, errorThrown) {

			//Show the error message
			asr.showErrorMessage("trying to get tag images.");
		}
		
		//Make the request to get images for the tags
		base.disable("getTagImagesButton");
		base.asyncRequest(asr._getTagImagesURL, {'tags': tagsByID}, onSuccessCallback, onFailCallback);

		//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
		asr.setGetScreenshotsButtonStatus();
	},

	/**
	* Uploads the tag image to the server where it is placed in official tag image storage
	*/
	uploadTagImage: function(tagImageData) {

		//If no tag image was passed, do nothing
		if (!tagImageData) {return;}

		//Create the tag UUID
		let newUUID = asr.getUUID();

		//Add the "queued" row to the tags table
	    let $li = $("<li class='ui-state-default' id='tagLI" + newUUID + "' />").html('<div class="queuedTagDiv">Queued...</div>');
	    $("#sortable").append($li);
	    $("#sortable").sortable('refresh');

	    //Start checking for the image to be done.
		asr.loadTagImage(asr.tagImagesURL + newUUID + ".png", "tagLI" + newUUID);

		//Do nothing for now
		let onSuccessCallback = function(response) {
			console.log("Image uploaded");
		}
		
		//If there was a problem connecting with the server, notify the user and enable the input field/button
		let onFailCallback = function(textStatus, errorThrown) {

			//Show the error message
			asr.showErrorMessage("trying to upload the tag image.");
		}
		
		//Create the request object for the raw form data
		let formData = new FormData();
		formData.append('imageID', newUUID);
		formData.append('image', tagImageData);
		base.asyncRequest(asr._uploadTagImageURL, formData, onSuccessCallback, onFailCallback, true);

		//Note we are processing a new tag
        ++asr._tagsBeingProcessed;

		//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
		asr.setGetScreenshotsButtonStatus();
	},

	/**
	* Sends a screenshot job request to the server with the job ID, campaign, and tag information.
	*
	* On success, the user is sent to the job queued page
	*/
	requestScreenshots: function() {

		//Generate a unique ID for the screenshots
		let jobID = asr.getUUID();

		//Create the header of images in proper sort order
		let tagHeader = "";
		let sortedIDList = $("#sortable").sortable('toArray');
		for (let sortedIndex = 0; sortedIndex < sortedIDList.length; ++sortedIndex) {

			//Remove the http/https from the image src if present
			let currentImage = $("#" + sortedIDList[sortedIndex] + " img").attr('src').replace(/^https?\:\/\//i, "");

			//Add the tag image to the final header string
			tagHeader += "tagImages[" + sortedIndex + "]=" + currentImage + "&";
		}

		//Create the onSuccessCallback function that will navigate to the job queued page
		let onSuccessCallback = function(response) {
			
			//If successful, navigate to the queued job page
			if (response.success) {
				window.location.href = '/campaignResults.php?jobID=' + jobID;
				//window.open('/campaignResults.php?jobID=' + jobID, '_blank');
			}
						
			//If failure, simply output to the console for the time being
			else {
				console.log('in failure');
				console.log(response.data);
			}
		}

		//If there was a problem connecting with the server, notify the user and enable the input field/button
		let onFailCallback = function(textStatus, errorThrown) {

			//Show the error message
			asr.showErrorMessage("trying to request screenshots.");
		}
		

		//Make the request
		base.asyncRequest(asr._requestScreenshotsURL, 
						 'jobID=' + jobID + '&' + tagHeader + '&' + base.serializeForm('pagesForm'), 
						  onSuccessCallback, onFailCallback);
	},

	addTagsToQueue: function(tagTextArray) {
		asr._queuedTags = asr._queuedTags.concat(tagTextArray);
		base.nodeFromID("queuedTagCountSpan").innerHTML = asr._queuedTags.length;
		if (asr._queuedTags.length > 0) {base.enable("getTagImagesButton");}

		//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
		asr.setGetScreenshotsButtonStatus();

		//Turn the tag queue process tag div green
		base.nodeFromID("queuedTagDiv").className = "greenBackground";
	},

	/**
	* Sends the tag text to the server for storage and analysis
	*
	* @param {String} tagText  	Text of tag
	*/
	storeTagText: function(tagText) {

		//Regardless of response, stay silent
		let onSuccessCallback = function(response) {
			
			//Regardless of success, act silent
			console.log(response);
		}

		//If there was a problem connecting with the server, do nothing
		let onFailCallback = function(textStatus, errorThrown) {

			//Regardless of failure, act silent
			console.log(textStatus + ": " + errorThrown);
		}
		
		//Make the request
		base.asyncRequest(asr._storeTagTextURL, {tagText: tagText}, onSuccessCallback, onFailCallback);
	},

	/**
	* Attempts to load the tag image with the passed URL. If successful, the image is placed
	* in the tag table in the passed li id. On failure, the function is ran again in the
	* time stated in the _imageLoadTimeout property.
	*
	* @param {String} imageURL  	URL of the image to try to load
	* @param {String} tagLIID  		List item for the queued image in the tags table
	*/
	loadTagImage: function(imageURL, tagLIID) {

		//Create a new image node to try loading the image into
		let tagImage = new Image();

		//If the image is loaded, place it into its tags table row
		tagImage.onload = function() {
			let imageLIHTML = "";
			imageLIHTML += 	'<div class="tagImageRowDiv">';
			imageLIHTML += 		'<div class="tagImageInfoDiv">';
			imageLIHTML += 			'<div class="tagDimensionsDiv">' + tagImage.naturalWidth + 'x' + tagImage.naturalHeight + '</div>';
			imageLIHTML += 			'<div class="tagImageDiv"><img rowTag="" style="max-height: 120px;" src="' + imageURL + '" /></div>';
			imageLIHTML += 		'</div>';
			imageLIHTML += 		'<div class="deleteButtonDiv">';
			imageLIHTML +=  		"<input type='button' class='button-tiny' value='Remove' onClick='asr.deleteTagImageListItem(\"" + tagLIID + "\")'>";
			imageLIHTML += 		'</div>';
			imageLIHTML += 	'</div>';
	        $("#" + tagLIID).html(imageLIHTML);
	        --asr._tagsBeingProcessed;

			//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
			asr.setGetScreenshotsButtonStatus();
		};

		//If the image was not loaded, check again in a few seconds
		tagImage.onerror = function() {
			setTimeout(function() {
				asr.loadTagImage(imageURL, tagLIID);
			}, asr._imageLoadTimeout);
		};

		//Try loading the image
		tagImage.src = imageURL; 	//fires off loading of image

	},

	/**
	* Deletes the list item in the tag images list with the passed ID
	*
	* @param {Integer} tagLIID  	ID of the list item in the tag images list to delete
	*/
	deleteTagImageListItem: function(tagLIID) {

		//Remove the row
		let liToDelete = base.nodeFromID(tagLIID);
		liToDelete.parentNode.removeChild(liToDelete);
	},

	filterOrders: function() {

		//Loop through the orders and add each whose name matches the filter field
		let filterText = base.nodeFromID("orderFilter").value;
		let orderOptions = "";
		for (let orderID in asr.orders) {
			if (asr.orders.hasOwnProperty(orderID)) {

				//Create the order label
				let orderName = asr.orders[orderID].name;
				if (asr.orders[orderID].advertiserName) {
					orderName += " - " + asr.orders[orderID].advertiserName;
					if (asr.orders[orderID].agencyName) {orderName += " (" + asr.orders[orderID].agencyName + ")";}
				}
				orderName += " - " + orderID;

				//If there is no filter text or the filter text is in the order's name, add the order
				if ((filterText == "") || (orderName.toLowerCase().indexOf(filterText.toLowerCase()) !== -1)) {
					orderOptions += "<option value='" + orderID + "'>" + orderName + "</option>";
				}
			}
		}

		//Add the order options to the select menu
		base.nodeFromID("orderSelect").innerHTML = orderOptions;
		base.nodeFromID("orderNotesDiv").innerHTML = "";
	},

	displayOrderNotes: function() {
		let orderID = base.nodeFromID("orderSelect").value;
		if (orderID) {
			base.nodeFromID("orderNotesDiv").innerHTML = asr._matchingOrders[orderID].notes;
		}
	},

	requestOrderData: function() {

		//Check to see if an order has been selected
		let orderID = base.nodeFromID("orderSelect").value;
		if (!orderID) {return;}
		base.disable("getOrderDataButton");

		//Create the onSuccessCallback function that will display the information
		let onSuccessCallback = function(response) {

			base.enable("getOrderDataButton");

			base.disable("lineItemsButton");
			base.hide("tooManyCreativeDiv");
			
			//If successful, show the information
			if (response.success) {

				//Store the order data
				asr._lineItems = response.data.lineItems;
				asr._creatives = response.data.creatives;

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
				for (let lineItemID in asr._lineItems) {
					if (asr._lineItems.hasOwnProperty(lineItemID)) {

						//Get the line item
						let currentLineItem = asr._lineItems[lineItemID];

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
				$("#lineItemsTable input:checkbox").click(asr.onLineItemSelection);
				lineItemsDialog.open();

				//Place the advertiser name in the customer field
				if (asr._matchingOrders[orderID].advertiserName) {
					base.nodeFromID("customer").value = asr._matchingOrders[orderID].advertiserName;
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
		let onFailCallback = function(textStatus, errorThrown) {

			//Show the error message
			asr.showErrorMessage("trying to get the line items and creative.");

			console.log('in get order data failure');
			console.log(response.data);
			base.enable("getOrderDataButton");
		}

		//Make the request
		base.asyncRequest(asr._getOrderDataURL, 'orderID=' + orderID, onSuccessCallback, onFailCallback);
	},

	searchOrders: function() {

		//Check to see if a search term has been entered
		let searchTerm = base.nodeFromID("orderSearchTerm").value;
		if (searchTerm.length < 3) {return;}

		//Disable the search button
		base.disable("orderSearchButton");

		//Create the onSuccessCallback function that will display the information
		let onSuccessCallback = function(response) {

			base.enable("orderSearchButton");
			
			//If successful, show the information
			if (response.success) {

				//Store the order data
				asr._matchingOrders = response.data;

				//Create the order options
				let orderOptions = "";
				for (let orderID in asr._matchingOrders) {
					if (asr._matchingOrders.hasOwnProperty(orderID)) {

						//Create the order label
						let orderName = asr._matchingOrders[orderID].name;
						if (asr._matchingOrders[orderID].advertiserName) {
							orderName += " - " + asr._matchingOrders[orderID].advertiserName;
							if (asr._matchingOrders[orderID].agencyName) {orderName += " (" + asr._matchingOrders[orderID].agencyName + ")";}
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
		let onFailCallback = function(textStatus, errorThrown) {

			//Show the error message
			asr.showErrorMessage("trying to search orders.");

			console.log('in search order data failure');
			console.log(response.data);

			base.enable("orderSearchButton");
		}

		//Make the request
		base.asyncRequest(asr._searchOrdersURL, 'searchTerm=' + searchTerm, onSuccessCallback, onFailCallback);
	},

	useSelectedLineItems: function() {

		//Get the selected line item IDs
		let lineItemIDs = [];
		$("#lineItemsTable input:checked").each(function( index ) {
			lineItemIDs.push($(this).val());
		});

		//If none were selected, do nothing
		if (lineItemIDs.length == 0) {return;}

		//Fill out the line items div and put together their creatives
		let creativeIDList = [];
		base.nodeFromID("lineItemsDiv").innerHTML = "";
		for (let index in lineItemIDs) {

			//Get the line item
			let currentLineItem = asr._lineItems[lineItemIDs[index]];

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
				asr.addTagsToQueue(tagParser.getTags(asr._creatives[currentCreativeID].tag));
			}
			asr.getTagImages();
		}

		//Hide the orders and show the line items
		base.hide("dfpOrdersHeader");
		base.hide("dfpOrdersHelpIcon");
		base.hide("dfpOrdersDiv");
		base.show("lineItemsHeader");
		base.show("lineItemsDiv");

		//Close the dialog
		lineItemsDialog.close();
	},

	selectAllLineItems: function() {
		$('#lineItemsTable input:checkbox').prop('checked', true);
		asr.onLineItemSelection();
	},

	unselectAllLineItems: function() {
		$('#lineItemsTable input:checkbox').prop('checked', false);
		asr.onLineItemSelection();
	},

	onLineItemSelection: function() {
		
		//Get the selected line item IDs
		let lineItemIDs = [];
		$("#lineItemsTable input:checked").each(function( index ) {
			lineItemIDs.push($(this).val());
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
			let currentLineItem = asr._lineItems[lineItemIDs[index]];
			for (let index in currentLineItem.creatives) {
				let currentCreativeID = currentLineItem.creatives[index];
				if (creativeIDList.indexOf(currentCreativeID) < 0) {creativeIDList.push(currentCreativeID);}
			}
		}

		//If there are more than 15 creatives selected, show the too many div, otherwise hide it
		if (creativeIDList.length >= 15) {base.show("tooManyCreativeDiv");}
		else {base.hide("tooManyCreativeDiv");}
	},

	/**
	* Uploads the PowerPoint background image to the server where it is placed in official tag image storage
	*/
	uploadPowerPointBackground: function() {

		//Verify the fields were filled in
		if (base.nodeFromID("newBackgroundTitle").value.length == 0) {alert("Enter a name for the PowerPoint"); return;}
		if (base.nodeFromID("newBackgroundImage").value.length == 0) {alert("Choose an image for the background"); return;}

		//Disable the save button
		base.disable("uploadBackgroundButton");

		//Do nothing for now
		let onSuccessCallback = function(response) {

			//If successful, store the items, create the HTML options, and show the pages table
			if (response.success) {
				
				//Set the background info to the newly uploaded one
				let newBackgroundInfo = response.data;
				base.nodeFromID("backgroundTitleDiv").innerHTML = newBackgroundInfo.title;
				base.nodeFromID("fontColorDiv").style.backgroundColor = "#" + newBackgroundInfo.fontColor;
				base.nodeFromID("backgroundThumbnailImage").src = asr.powerPointBackgroundsURL + "thumbnails/" + newBackgroundInfo.thumbnailFilename;
	
				//Set the hidden input fields
				base.nodeFromID("backgroundTitle").value = newBackgroundInfo.title;
				base.nodeFromID("backgroundFontColor").value = newBackgroundInfo.fontColor;
				base.nodeFromID("backgroundFilename").value = newBackgroundInfo.filename;
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
		let onFailCallback = function(textStatus, errorThrown) {

			//Show the error message
			asr.showErrorMessage("trying to upload the PowerPoint background image.");

			//Re-enable the submit button
			base.enable("uploadBackgroundButton");
		}

		//Create the request object for the image raw data
		let formData = new FormData();
		formData.append('backgroundTitle', base.nodeFromID("newBackgroundTitle").value);
		formData.append('backgroundFontColor', base.nodeFromID("newBackgroundFontColor").value.substring(1));
		formData.append('backgroundImage', base.nodeFromID("newBackgroundImage").files[0]);
		base.asyncRequest(asr._uploadBackgroundImageURL, formData, onSuccessCallback, onFailCallback, true);
	},

	setGetScreenshotsButtonStatus: function() {

		//Enable the request screenshots button if:
		//		- A customer name has been entered
		//		- At least one page has been added
		//		- There are no tags being processed
		//		- There are no tags in the queue
		if ((base.nodeFromID("customer").value != "") && 
			(base.nodeFromID("pagesTable").rows.length > 0) && 
			(asr._tagsBeingProcessed == 0) && 
			(asr._queuedTags.length == 0)) {

			base.enable("getScreenshotsButton");
		}

		//Otherwise, disable it
		else {base.disable("getScreenshotsButton");}
	},

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
	* @param {Integer} whileClause  	Description of the error to be placed after the word "while"
	*/
	showErrorMessage: function(whileClause) {

		//If a "while" clause was passed, create the proper text
		let whileText = "";
		if (whileClause) {whileText = " while " + whileClause;}

		//Build the error message and show it
		let errorHTML = "There was difficulty communicating with the server" + whileText + ".<br/><br/>";
		errorHTML += "Check your internet connection and try refreshing your browser.<br/><br/>";
		errorHTML += "If the problem persists, please <a onclick='contactForm.reset(); contactFormDialog.open()'>Contact Us</a> us.";
		base.showMessage(errorHTML, "A Problem has Occurred");
	},

	/**
	* Enables all of the submit buttons on the page. This is useful for when the user refreshes the page.
	* Firefox and Chrome do not re-enable the buttons if they were disabled by Javascript.
	*/
	enableSubmitButtons: function() {

		base.enable("getMenuButton");
		base.enable("domain");
		base.enable("getTagImagesButton");
		base.enable("getOrderDataButton");
		base.enable("uploadBackgroundButton");
		base.disable("getScreenshotsButton");
	},

	/**
	* Returns a random UUID
	*
	* From: http://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
	*
	* @return {String}  Random UUID.
	*/
	getUUID: function() {
	    let d = new Date().getTime();
	    if(window.performance && typeof window.performance.now === "function"){
	        d += performance.now(); //use high-precision timer if available
	    }
	    let uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	        let r = (d + Math.random()*16)%16 | 0;
	        d = Math.floor(d/16);
	        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
	    });
	    return uuid;
	},

}
