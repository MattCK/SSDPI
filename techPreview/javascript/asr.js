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

	_domain: '',										//Stores the domain to get screenshots for
	_getMenuURL: 'getMenu.php',							//URL of request page used to retrieve the menu for the domain
	_getTagImagesURL: 'getTagImages.php',				//URL of request page to turn tags into images
	_requestScreenshotsURL: 'requestScreenshots.php',	//URL of request page to send information to in order to create screenshots
	_storeTagTextURL: 'storeTagText.php',				//URL of request page that stores tag text for analysis
	_menuItems: [],										//List of menu items returned from the get menu request
	_menuOptions: "",									//HTML options string of menu labels and their URLs
	_imageLoadTimeout: 3000,							//Interval the client should check to see if a tag image is ready
	_rowIndex: 0,										//Stores the index for the next page row to define its array number. 
														//(Possibly unnecessary but more robust)
	_queuedTags: [],									//Array of tags that need to be processed into images
	_tagsBeingProcessed: 0,								//Number of tags being processed into images

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
		let callback = function(response) {
			
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
				base.nodeFromID("campaignPagesHeader").innerHTML = "Campaign Pages: " + asr._domain;
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
		
		//Make the request
		base.asyncRequest(asr._getMenuURL, 'domain=' + base.nodeFromID('domain').value, callback);
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
		rowCells += "<td><select name='findStory[" + asr._rowIndex + "]'><option value=0>Front Page</option><option value=1>Find me a story</option></select></td>";
		rowCells += "<td><label><input type='checkbox' name='onlyScreenshot[" + asr._rowIndex + "]' value='1'>Get screenshot without inserting tags</label></td>";
		rowCells += "<td><label><input type='checkbox' name='useMobile[" + asr._rowIndex + "]' value='1'>Mobile</label></td>";
		rowCells += "<td><input class='button-tiny' type='button' value='Delete' onClick='asr.deletePageRow(" + asr._rowIndex + ")'></td>";
		newRow.innerHTML = rowCells;

		//Increment the row index for the next inserted row
		asr._rowIndex += 1;

		//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
		if ((base.nodeFromID("pagesTable").rows.length > 0) && (asr._tagsBeingProcessed == 0) && (asr._queuedTags.length == 0)) {
			base.enable("getScreenshotsButton");}
		else {base.disable("getScreenshotsButton");}
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
		rowCells = "<td class='pageURLTitle'>Page URL: </td>";
		rowCells += "<td><input type='text' name='pages[" + asr._rowIndex + "]'></td>";
		rowCells += "<td><label><input type='checkbox' name='onlyScreenshot[" + asr._rowIndex + "]' value='1'>Get screenshot without inserting tags</label></td>";
		rowCells += "<td><label><input type='checkbox' name='useMobile[" + asr._rowIndex + "]' value='1'>Mobile</label></td>";
		rowCells += "<td><input class='button-tiny' type='button' value='Delete' onClick='asr.deletePageRow(" + asr._rowIndex + ")'></td>";
		newRow.innerHTML = rowCells;

		//Increment the row index for the next inserted row
		asr._rowIndex += 1;

		//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
		if ((base.nodeFromID("pagesTable").rows.length > 0) && (asr._tagsBeingProcessed == 0) && (asr._queuedTags.length == 0)) {
			base.enable("getScreenshotsButton");}
		else {base.disable("getScreenshotsButton");}
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
		if ((base.nodeFromID("pagesTable").rows.length > 0) && (asr._tagsBeingProcessed == 0) && (asr._queuedTags.length == 0)) {
			base.enable("getScreenshotsButton");}
		else {base.disable("getScreenshotsButton");}
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
		    let $li = $("<li class='ui-state-default' id='tagLI" + newUUID + "' />").text('Queued...');
		    $("#sortable").append($li);
		    $("#sortable").sortable('refresh');

		    //Increase the count of tags being processed into images
		    ++asr._tagsBeingProcessed;

		    //Start checking for the image to be done.
			asr.loadTagImage("https://s3.amazonaws.com/asr-tagimages/" + newUUID + ".png", "tagLI" + newUUID);
		}	

		//Remove the queued tags
		let callback = function(response) {
			asr._queuedTags = [];
			base.nodeFromID("queuedTagCountSpan").innerHTML = 0;
			console.log(response.data);
		}
		
		//Make the request to get images for the tags
		base.disable("getTagImagesButton");
		base.asyncRequest(asr._getTagImagesURL, {'tags': tagsByID}, callback);

		//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
		if ((base.nodeFromID("pagesTable").rows.length > 0) && (asr._tagsBeingProcessed == 0) && (asr._queuedTags.length == 0)) {
			base.enable("getScreenshotsButton");}
		else {base.disable("getScreenshotsButton");}
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

		//Create the callback function that will navigate to the job queued page
		let callback = function(response) {
			
			//If successful, navigate to the queued job page
			if (response.success) {
				window.open('/campaignResults.php?jobID=' + jobID, '_blank');
			}
						
			//If failure, simply output to the console for the time being
			else {
				console.log('in failure');
				console.log(response.data);
			}
		}
		
		//Make the request
		base.asyncRequest(asr._requestScreenshotsURL, 'jobID=' + jobID + '&' + tagHeader + '&' + base.serializeForm('pagesForm'), callback);
	},

	addTagsToQueue: function(tagTextArray) {
		asr._queuedTags = asr._queuedTags.concat(tagTextArray);
		base.nodeFromID("queuedTagCountSpan").innerHTML = asr._queuedTags.length;
		if (asr._queuedTags.length > 0) {base.enable("getTagImagesButton");}

		//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
		if ((base.nodeFromID("pagesTable").rows.length > 0) && (asr._tagsBeingProcessed == 0) && (asr._queuedTags.length == 0)) {
			base.enable("getScreenshotsButton");}
		else {base.disable("getScreenshotsButton");}
	},

	/**
	* Sends the tag text to the server for storage and analysis
	*
	* @param {String} tagText  	Text of tag
	*/
	storeTagText: function(tagText) {

		//Regardless of response, stay silent
		let callback = function(response) {
			
			//Regardless of success, act silent
			console.log(response);
		}
		
		//Make the request
		base.asyncRequest(asr._storeTagTextURL, {tagText: tagText}, callback);
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
			imageLIHTML =  '<div><img rowTag="" style="max-height: 120px;" src="' + imageURL + '" /></div>';
			imageLIHTML += '<div>' + tagImage.naturalWidth + 'x' + tagImage.naturalHeight + '</div>';
			imageLIHTML += "<input type='button' value='Delete' onClick='asr.deleteTagImageListItem(\"" + tagLIID + "\")'>";
	        $("#" + tagLIID).html(imageLIHTML);
	        --asr._tagsBeingProcessed;

			//Enable the make screenshots button or disable it depending on pages added, tags queued, and tags being processed
			if ((base.nodeFromID("pagesTable").rows.length > 0) && (asr._tagsBeingProcessed == 0) && (asr._queuedTags.length == 0)) {
				base.enable("getScreenshotsButton");}
			else {base.disable("getScreenshotsButton");}
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
