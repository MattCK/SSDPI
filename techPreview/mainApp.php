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

?>

<?PHP include_once(BASEPATH . "header.php");?>

<style>
	.dropBox {
	    border: 2px dashed #bbb;
	    border-radius: 5px;
	    color: #bbb;
	    padding: 25px;
	    text-align: center;
	}
</style>

<script>

let asr = {

	_domain: '',
	_getMenuURL: 'getMenu.php',
	_storeTagTextURL: 'storeTagText.php',
	_getTagImagesURL: 'getTagImages.php',
	_menuItems: [],
	_menuOptions: "",
	_rowIndex: 0,

	getMenu: function() {

		//Create the callback function that will show the table
		let callback = function(response) {
			
			//If successful, clear and hide the plan form, show the new table, and highlight the new table row
			if (response.success) {
				
				//Store the menu items and create the menu options
				//console.log(response.data);
				asr._menuItems = response.data;
				asr._menuOptions = "<option value='/'>Main</option>";
				for (let menuTitle in asr._menuItems) {
					if (asr._menuItems.hasOwnProperty(menuTitle)) {
						asr._menuOptions += "<option value='" + asr._menuItems[menuTitle] + "'>" + menuTitle + "</option>";
					}
				}	
				//console.log(asr._menuOptions);

				//Store the domain and hide the input fields
				asr._domain = base.nodeFromID('adSiteDomain').value;
				base.hide("domainInputDiv");
				base.nodeFromID("domainNameDiv").innerHTML = asr._domain;
				base.show("domainNameDiv");
				base.show("pagesTableDiv");
			}
						
			//If failure, show us the message returned from the server and focus on the selected element if returned. Also, re-enable the submit button.
			else {
				alert(response.message);
			}
		}
		
		//Make the request
		base.asyncRequest(asr._getMenuURL, 'domain=' + base.nodeFromID('adSiteDomain').value, callback);
	},

	addPageRow: function() {

		let pageTable = base.nodeFromID("pagesTable");
		let newRow =  pageTable.insertRow(pageTable.rows.length);

		newRow.id = "pageRow" + asr._rowIndex;

		rowCells = "<td><select name='pageMenuItem[" + asr._rowIndex + "]'>" + asr._menuOptions + "</select></td>";
		rowCells += "<td><select name='pageStoryType[" + asr._rowIndex + "]'><option value='front'>Front Page</option><option value='find'>Find me a story</option></select></td>";
		rowCells += "<td><input type='checkbox' name='onlyScreenshot[" + asr._rowIndex + "]' value='1'>Take screenshot without inserting tags</td>";
		rowCells += "<td><input type='button' value='Delete' onClick='asr.deletePageRow(" + asr._rowIndex + ")'></td>";
		newRow.innerHTML = rowCells;

		asr._rowIndex += 1;

		base.enable("addTagsButton");
	},

	addURLRow: function() {

		let pageTable = base.nodeFromID("pagesTable");
		let newRow =  pageTable.insertRow(pageTable.rows.length);

		newRow.id = "pageRow" + asr._rowIndex;

		rowCells = "<td>Page URL: </td>";
		rowCells += "<td><input type='text' name='pageURL[" + asr._rowIndex + "]'></td>";
		rowCells += "<td><input type='checkbox' name='onlyScreenshot[" + asr._rowIndex + "]' value='1'>Take screenshot without inserting tags</td>";
		rowCells += "<td><input type='button' value='Delete' onClick='asr.deletePageRow(" + asr._rowIndex + ")'></td>";
		newRow.innerHTML = rowCells;

		asr._rowIndex += 1;

		base.enable("addTagsButton");
	},

	deletePageRow: function(rowID) {
		let rowToDelete = base.nodeFromID("pageRow" + rowID);
		rowToDelete.parentNode.removeChild(rowToDelete);

		if (base.nodeFromID("pagesTable").rows.length == 0) {base.disable("addTagsButton");}
	},

	toggleDivs: function() {
    	let $inner = $("#inner");

	    // See which <divs> should be animated in/out.
	    if ($inner.position().left == 0) {
	        $inner.animate({
	            left: "-960px"
	        });
	    }
	    else {
	        $inner.animate({
	            left: "0px"
	        });
	    }
	},

	processNewTags: function(newTags) {

		//If no tags were passed, do nothing
		if (!newTags.length) {return;}

		//Generate a unique ID for each tag
		let tagsByID = {};
		for (let tagIndex = 0; tagIndex < newTags.length; ++tagIndex) {
			let newUUID = getUUID();
			tagsByID[newUUID] = newTags[tagIndex];
			loadImage("https://s3.amazonaws.com/asr-tagimages/" + newUUID + ".png");
		}	

		//Create the callback function that will show the table
		let callback = function(response) {
			
			//If successful, clear and hide the plan form, show the new table, and highlight the new table row
			if (response.success) {
				
				//Store the menu items and create the menu options
				console.log(response.data);
			}
						
			//If failure, show us the message returned from the server and focus on the selected element if returned. Also, re-enable the submit button.
			else {
				console.log(response.data);
			}
		}
		
		//Make the request
		console.log(tagsByID);
		base.asyncRequest(asr._getTagImagesURL, {'tags': tagsByID}, callback);
	},

	storeTagText: function(tagText) {

		//Create the callback function that will show the table
		let callback = function(response) {
			
			//Regardless of success, act silent
			console.log(response);
		}
		
		//Make the request
		base.asyncRequest(asr._storeTagTextURL, {tagText: tagText}, callback);
	},
	
}


function loadImage(imageURL) {
	console.log("Checking: " + imageURL);

	var img = new Image();
	img.onload = function() {
    	console.log("FOUND: " + imageURL);
        let imagesDiv = base.nodeFromID("loadedImagesDiv");
        imagesDiv.innerHTML += '<img src="' + imageURL + '" /><br><br>';
	};
	img.onerror = function() {
		setTimeout(function() {
			loadImage(imageURL);
		}, 3000);
	};

	img.src = imageURL; // fires off loading of image

	/*$.get(imageURL)
    .done(function() { 
    	console.log("FOUND: " + imageURL);
        let imagesDiv = base.nodeFromID("loadedImagesDiv");
        imagesDiv.innerHTML += '<img src="' + imageURL + '" />';

    }).fail(function() { 
		setTimeout(function() {
			loadImage(imageURL);
		}, 3000)
    });*/
}

let tagParser = {

	getTags: function(adTagText) {

		//Loop through the ad tag text and grab the HTML parts along with their type
		let adTagRegularExpression = /<(\w*)\b[^>]*>[\s\S]*?<\/\1>/gmi;
		let textHTMLParts = [];
		let currentHTMLPart = [];
		while ((currentHTMLPart = adTagRegularExpression.exec(adTagText)) !== null) {

			//Store the parts as named variables for clarity
			let htmlPart = currentHTMLPart[0];
			let htmlPartType = currentHTMLPart[1].toLowerCase();

			//If it is an HTML script part, determine if it includes a source
			let isSourceScript = false;
			if (htmlPartType == "script") {
				isSourceScript = tagParser.scriptHasSource(htmlPart);
			}		

			//Store the results 
			textHTMLParts.push({
				html: htmlPart,
				type: htmlPartType,
				isSource: isSourceScript,
			});
		}

		//If no parts were found, return an empty array
		if (textHTMLParts.length == 0) {return [];}
		
		//To simplify code below, let's mark each type of HTML tags we are working with
		let hasHTMLScriptTag, hasHTMLIFrameTag, hasHTMLNoscriptTag, hasHTMLAnchorTag, hasHTMLIMGTag;
		for (let htmlPartIndex = 0; htmlPartIndex < textHTMLParts.length; ++htmlPartIndex) {
			switch (textHTMLParts[htmlPartIndex].type) {
				case 'script': hasHTMLScriptTag = true; break;
				case 'iframe': hasHTMLIFrameTag = true; break;
				case 'noscript': hasHTMLNoscriptTag = true; break;
				case 'a': hasHTMLAnchorTag = true; break;
			}
		}

		//Grab the tags depending on what html elements are present
		let adTags = [];

		//If script tags are present, only return them. If a non-sourced script element appears before a
		//sourced one, concatenate them together
		let currentScriptTag = "";
		for (let htmlPartIndex = 0; htmlPartIndex < textHTMLParts.length; ++htmlPartIndex) {

			//If the set of HTML parts has a script tag, only store script tags
			if (hasHTMLScriptTag) {
				if (textHTMLParts[htmlPartIndex].type == 'script') {

					//If it is a script part without a source (generally used to set variables),
					//simply concatenate it to the existing tag
					if (!textHTMLParts[htmlPartIndex].isSource) {
						currentScriptTag += textHTMLParts[htmlPartIndex].html;
					}

					//Otherwise, concatenate it and store it as a tag then clear the current tag string
					else {
						currentScriptTag += textHTMLParts[htmlPartIndex].html;
						adTags.push(currentScriptTag);
						currentScriptTag = "";			
					}
				}
			}

			//Otherwise, if it has no script tags but an iframe tag, only store iframe tags
			else if (hasHTMLIFrameTag) {
				if (textHTMLParts[htmlPartIndex].type == 'iframe') {
					adTags.push(textHTMLParts[htmlPartIndex].html);
				}
			}

			//Otherwise, if no script or iframe tags but a noscript, only store noscript tags
			else if (hasHTMLNoscriptTag) {
				if (textHTMLParts[htmlPartIndex].type == 'noscript') {
					adTags.push(textHTMLParts[htmlPartIndex].html);
				}
			}

			//Otherwise, if no script, iframe, or noscript tags but an anchor, only store anchor tags
			else if (hasHTMLAnchorTag) {
				if (textHTMLParts[htmlPartIndex].type == 'a') {
					adTags.push(textHTMLParts[htmlPartIndex].html);
				}
			}

			//And if no correct tag types are found, simply do nothing
		}

		//Return all found tags
		return adTags;
	},

	scriptHasSource: function(scriptHTML) {

		let hasSource = false;
		if (scriptHTML.toLowerCase().indexOf('src=') > -1) {hasSource = true;}
		if (scriptHTML.toLowerCase().indexOf('document.write') > -1) {hasSource = true;}
		return hasSource;
	},

	handleDragOver: function(event) {
		event.stopPropagation();
		event.preventDefault();
	},


	handleTextFileDrop: function(event) {

		//Prepare for the file processing
		event.stopPropagation();
		event.preventDefault();

		//Store the files
		let files = event.dataTransfer.files;

		//If no files were dropped, do nothing
		if (files.length == 0) {return;}

		//Loop through the files and get their text
		let allTagsText = "";
		let textCount = files.length;
		for (let i = 0, currentFile; currentFile = files[i]; i++) {
	
			//Create the reader to read the current file
			let reader = new FileReader();

			// Closure to capture the file information.
			reader.onload = (function(theFile) {
				return function(e) {
					allTagsText += e.target.result;
					--textCount;
					asr.storeTagText(e.target.result);

					if (textCount == 0) {
						asr.processNewTags(tagParser.getTags(allTagsText));
					}
				};
			})(currentFile);

			//Read the file as text
			reader.readAsText(currentFile);
		}
	},

	handleZipFileDrop: function(event) {

		//Prepare for the file processing
		event.stopPropagation();
		event.preventDefault();

		//Store the files
		let files = event.dataTransfer.files;

		//If no files were dropped, do nothing
		if (files.length == 0) {return;}

		//Verify the file is a zip file
		let zipFile = files[0];
		let zipFileType = zipFile.type.toLowerCase();
		if (zipFileType.indexOf("zip") <= -1) {return;}

		//Create a BlobReader to uncompress and read the zipfile
		zip.createReader(new zip.BlobReader(zipFile), function(reader) {

			// get all entries from the zip
			reader.getEntries(function(entries) {


				//If entries were found, read each one 
				let zipText = "";
				let zipEntryCount = entries.length;
				if (entries.length) {

					//Loop through each entry and try to read it as text
					for (var i = 0, entry; entry = entries[i]; i++) {

						//Get and store entry content as text
						entry.getData(new zip.TextWriter(), function(text) {

							//Add the text to the overall string and decrement the counter
							zipText += text;
							--zipEntryCount;
							asr.storeTagText(text);

							if (zipEntryCount == 0) {
								asr.processNewTags(tagParser.getTags(zipText));
							}

							// close the zip reader
							reader.close();

						});
					}
				}
			});
		});
	},

	handleTextboxInput: function() {

		//Get the textbox element
		let tagTextTextbox = base.nodeFromID("tagTextTextbox");
		let tagText = tagTextTextbox.value;

		//If there is nothing entered, do nothing
		if (tagText == "") {return;}
		asr.storeTagText(tagText);

		//Otherwise, get the tags. If some exist, remove the text. Otherwise, leave it.
		foundTags = tagParser.getTags(tagText);
		//if (foundTags.length > 0) {tagTextTextbox.value = "";}

		//Process the newly found tags
		asr.processNewTags(foundTags);
	},
}

//From: http://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
function getUUID(){
    var d = new Date().getTime();
    if(window.performance && typeof window.performance.now === "function"){
        d += performance.now(); //use high-precision timer if available
    }
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
    });
    return uuid;
}

</script>

<body>
<div id="container">
    <div id="inner">
        <div id="home">
			<div id="domainInputDiv">
				Site Domain: <input id="adSiteDomain" type="text">
				<input type="button" value="Go!" onclick="asr.getMenu()">
				<input id="addTagsButton" type="button" value="Skip" onclick="asr.toggleDivs()">
			</div>
			<div id="domainNameDiv"></div>
			<div id="pagesTableDiv" style="display: none;">
				<input type="button" value="Add page" onclick="asr.addPageRow()">
				<input type="button" value="Add URL" onclick="asr.addURLRow()"><br>
				<table id="pagesTable"></table><br><br>
				<input id="addTagsButton" type="button" value="Add Tags" onclick="asr.toggleDivs()" disabled>
			</div>
        </div>
        <div id="member-home">
			<div id="textFileDropZone" class="dropBox">Drop Text File(s)</div>
			<div id="zipFileDropZone" class="dropBox">Drop a Zip File</div>
	       	Enter Tag Text: <br><br>
        	<textarea id="tagTextTextbox" rows="10" cols="100"></textarea><br><br>
			<input id="tagTextTextboxButton" type="button" value="Add Tags">
			<input type="button" value="Back to Page Selection" onclick="asr.toggleDivs()">
			<div id="loadedImagesDiv"></div>
        </div>
    </div> 
</div>
</body>
</html>

<script>
//Setup listeners
let textFileDropZone = base.nodeFromID('textFileDropZone');
textFileDropZone.addEventListener('dragover', tagParser.handleDragOver, false);
textFileDropZone.addEventListener('drop', tagParser.handleTextFileDrop, false);
let zipFileDropZone = base.nodeFromID('zipFileDropZone');
zipFileDropZone.addEventListener('dragover', tagParser.handleDragOver, false);
zipFileDropZone.addEventListener('drop', tagParser.handleZipFileDrop, false);
let tagTextTextboxButton = base.nodeFromID("tagTextTextboxButton");
tagTextTextboxButton.addEventListener('click', tagParser.handleTextboxInput, false);

</script>