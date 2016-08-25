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
	_requestScreenshotsURL: 'requestScreenshots.php',
	_menuItems: [],
	_menuOptions: "",
	_rowIndex: 0,

	getMenu: function() {

		//Remove the http/https from the domain if present
		base.nodeFromID('domain').value = base.nodeFromID('domain').value.replace(/^https?\:\/\//i, "");

		//Create the callback function that will show the table
		let callback = function(response) {
			
			//If successful, clear and hide the plan form, show the new table, and highlight the new table row
			if (response.success) {
				
				//Store the menu items and create the menu options
				//console.log(response.data);
				asr._menuItems = response.data;
				//asr._menuOptions = "<option value='/'>Main</option>";
				for (let menuTitle in asr._menuItems) {
					if (asr._menuItems.hasOwnProperty(menuTitle)) {
						asr._menuOptions += "<option value='" + asr._menuItems[menuTitle] + "'>" + menuTitle + "</option>";
					}
				}	
				//console.log(asr._menuOptions);

				//Store the domain and hide the input fields
				asr._domain = base.nodeFromID('domain').value;
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
		base.asyncRequest(asr._getMenuURL, 'domain=' + base.nodeFromID('domain').value, callback);
	},

	addPageRow: function() {

		let pageTable = base.nodeFromID("pagesTable");
		let newRow =  pageTable.insertRow(pageTable.rows.length);

		newRow.id = "pageRow" + asr._rowIndex;

		rowCells = "<td><select name='pages[" + asr._rowIndex + "]'>" + asr._menuOptions + "</select></td>";
		rowCells += "<td><select name='findStory[" + asr._rowIndex + "]'><option value=0>Front Page</option><option value=1>Find me a story</option></select></td>";
		rowCells += "<td><input type='checkbox' name='onlyScreenshot[" + asr._rowIndex + "]' value='1'>Take screenshot without inserting tags</td>";
		rowCells += "<td><input type='checkbox' name='useMobile[" + asr._rowIndex + "]' value='1'>Mobile</td>";
		rowCells += "<td><input type='button' value='Delete' onClick='asr.deletePageRow(" + asr._rowIndex + ")'></td>";
		newRow.innerHTML = rowCells;

		asr._rowIndex += 1;

		base.enable("addTagsButton");
	},

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

	addURLRow: function() {

		let pageTable = base.nodeFromID("pagesTable");
		let newRow =  pageTable.insertRow(pageTable.rows.length);

		newRow.id = "pageRow" + asr._rowIndex;

		rowCells = "<td>Page URL: </td>";
		rowCells += "<td><input type='text' name='pages[" + asr._rowIndex + "]'></td>";
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
    	console.log("In toggleDivs");
    	let $inner = $("#inner");
    	console.log("#inner: " + $inner);
    	console.log("#inner.position().left: " + $inner.position().left);

	    // See which <divs> should be animated in/out.
	    if ($inner.position().left == 0) {
            console.log("Animating to the right");
	        $inner.animate({
	            left: "-960px"
	        });
	    }
	    else {
            console.log("Animating to the left");
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

		    let $li = $("<li class='ui-state-default' id='tagLI" + newUUID + "' />").text('Queued...');
		    $("#sortable").append($li);
		    $("#sortable").sortable('refresh');

			loadImage("https://s3.amazonaws.com/asr-tagimages/" + newUUID + ".png", "tagLI" + newUUID);
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

	requestScreenshots: function() {

		//Generate a unique ID for the screenshots
		let jobID = getUUID();

		//Create the header of images in proper sort order
		let tagHeader = "";
		let sortedIDList = $("#sortable").sortable('toArray');
		for (let sortedIndex = 0; sortedIndex < sortedIDList.length; ++sortedIndex) {

			//Remove the http/https from the image src if present
			let currentImage = $("#" + sortedIDList[sortedIndex] + " img").attr('src').replace(/^https?\:\/\//i, "");

			//Add the tag image to the final header string
			tagHeader += "tagImages[" + sortedIndex + "]=" + currentImage + "&";
		}

		//Create the callback function that will show the table
		let callback = function(response) {
			
			//If successful, clear and hide the plan form, show the new table, and highlight the new table row
			if (response.success) {
				console.log('in success');
				//Store the menu items and create the menu options
				console.log(response.data);
				window.open('/campaignResults.php?jobID=' + jobID, '_blank');
			}
						
			//If failure, show us the message returned from the server and focus on the selected element if returned. Also, re-enable the submit button.
			else {
				console.log('in failure');
				console.log(response.data);
			}
		}
		
		//Make the request
		base.asyncRequest(asr._requestScreenshotsURL, 'jobID=' + jobID + '&' + tagHeader + '&' + base.serializeForm('pagesForm'), callback);
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


function loadImage(imageURL, tagLIID) {
	console.log("Checking: " + imageURL);

	let img = new Image();
	img.onload = function() {
    	console.log("FOUND: " + imageURL);
        let imagesDiv = base.nodeFromID("loadedImagesDiv");
        //imagesDiv.innerHTML += '<img src="' + imageURL + '" /><br><br>';

        $("#" + tagLIID).html('<img rowTag="" style="max-height: 120px;" src="' + imageURL + '" />');
        //console.log($("#" + tagLIID + " img").attr('src'));

	};
	img.onerror = function() {
		setTimeout(function() {
			loadImage(imageURL, tagLIID);
		}, 3000);
	};

	img.src = imageURL; // fires off loading of image

}

$.fn.serializeObject = function()
{
    let o = {};
    let a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

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
					for (let i = 0, entry; entry = entries[i]; i++) {

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
}

</script>

<body>
<div id="container">
    <div id="inner">
        <div id="home">
        	<form id="pagesForm">
				<div id="customerDiv">
					Customer: <input id="customer" name="customer" type="text">
				</div>
				<div id="domainInputDiv">
					Site Domain: <input id="domain" name="domain" type="text">
					<input type="button" value="Go!" onclick="asr.getMenu()">
					<input id="skipButton" type="button" value="Skip" onclick="asr.toggleDivs()">
				</div>
				<div id="domainNameDiv"></div>

				<div id="pagesTableDiv" style="display: none;">
					<input type="button" value="Add Page" onclick="asr.addPageRow()">
					<input type="button" value="Add URL" onclick="asr.addURLRow()">
					<input type="button" value="Add All Pages" onclick="asr.addAllPages()">
					<input type="button" value="Make screenshots" onclick="asr.requestScreenshots()"><br>
					<table id="pagesTable"></table><br><br>
					<input id="addTagsButton" type="button" value="Add Tags" onclick="asr.toggleDivs()" disabled>
				</div>
			</form>
        </div>
        <div id="member-home">
			<div id="textFileDropZone" class="dropBox">Drop Text File(s)</div>
			<div id="zipFileDropZone" class="dropBox">Drop a Zip File</div>
	       	Enter Tag Text: <br><br>
        	<textarea id="tagTextTextbox" rows="10" cols="100"></textarea><br><br>
			<input id="tagTextTextboxButton" type="button" value="Add Tags">
			<input type="button" value="Back to Page Selection" onclick="asr.toggleDivs()">
			<div id="loadedImagesDiv"></div>
			<ul id="sortable">
				
				<li class="ui-state-default ui-sortable-handle" id="tagLIcbb431fc-7960-4790-a3e4-0863c27c3d16"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/cbb431fc-7960-4790-a3e4-0863c27c3d16.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI5c910730-4a7d-4fe8-b500-4776cc05ce18"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/5c910730-4a7d-4fe8-b500-4776cc05ce18.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI40ec11bf-3cbf-4258-b374-1686f516cffd"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/40ec11bf-3cbf-4258-b374-1686f516cffd.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI4f8c29c4-839b-4c5b-8f51-6eed846fd6ee"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/4f8c29c4-839b-4c5b-8f51-6eed846fd6ee.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI1ac46431-8a3a-471e-b5fd-533680efc8ba"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/1ac46431-8a3a-471e-b5fd-533680efc8ba.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI3d5f7c81-11a1-409f-8c3c-4df7ab55ce56"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/3d5f7c81-11a1-409f-8c3c-4df7ab55ce56.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIf6f66468-13d4-41f1-aed3-367bb465da2d"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/f6f66468-13d4-41f1-aed3-367bb465da2d.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIfeb2826f-6402-4cf4-9ceb-f73607bafd3d"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/feb2826f-6402-4cf4-9ceb-f73607bafd3d.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI789a53c9-c1e4-4e0f-8a64-d93862962b4a"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/789a53c9-c1e4-4e0f-8a64-d93862962b4a.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI2be1c977-a4ac-4ef0-a1cd-82b3770aeb53"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/2be1c977-a4ac-4ef0-a1cd-82b3770aeb53.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIa15931ed-3db0-43c1-838b-a01fd96177fc"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/a15931ed-3db0-43c1-838b-a01fd96177fc.png"></li><!--li class="ui-state-default ui-sortable-handle" id="tagLI9eb8c8e7-eb81-4992-b55f-332d7264c1e7"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/9eb8c8e7-eb81-4992-b55f-332d7264c1e7.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLId7eb673b-d673-4e7e-81ba-396959fc73ab"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/d7eb673b-d673-4e7e-81ba-396959fc73ab.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI18fcbca5-c7c9-4de4-81f9-f841e60eae9a"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/18fcbca5-c7c9-4de4-81f9-f841e60eae9a.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI5c18ed3d-299c-4f52-9157-775b20e51c3d"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/5c18ed3d-299c-4f52-9157-775b20e51c3d.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI3db2322f-6de0-4baf-a900-3f1a00305763"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/3db2322f-6de0-4baf-a900-3f1a00305763.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI213385a2-c091-4e70-ab5b-8226e591e13f"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/213385a2-c091-4e70-ab5b-8226e591e13f.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI28d0c48b-b825-4cb6-863f-5c77380826e3"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/28d0c48b-b825-4cb6-863f-5c77380826e3.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIceb00d34-d516-4d03-9cde-bf8e0068d4bc"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/ceb00d34-d516-4d03-9cde-bf8e0068d4bc.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIcd13e319-3a95-4bfc-8b64-66ea680da80c"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/cd13e319-3a95-4bfc-8b64-66ea680da80c.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI3dec988a-7905-4fee-af45-cbefb9ab7d8d"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/3dec988a-7905-4fee-af45-cbefb9ab7d8d.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIa07a6fb4-e472-469e-a25e-79992c26b97d"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/a07a6fb4-e472-469e-a25e-79992c26b97d.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIf43232fd-6767-4df8-9c5a-f3697f9f6363"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/f43232fd-6767-4df8-9c5a-f3697f9f6363.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIacb3085d-7cfe-4d11-b114-54e260dc82ee"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/acb3085d-7cfe-4d11-b114-54e260dc82ee.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI05d10818-fc80-4504-8117-b14c34a486b1"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/05d10818-fc80-4504-8117-b14c34a486b1.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIaa0a452f-5692-49e2-8dd0-e602b61fcacf"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/aa0a452f-5692-49e2-8dd0-e602b61fcacf.png"></li-->

			</ul>
			<br>
			<input type="button" value="Make screenshots" onclick="asr.requestScreenshots()">
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
$(function() {
	$( "#sortable" ).sortable();
	$( "#sortable" ).disableSelection();

	$( document ).tooltip({
		items: "img, [data-geo], [title]",
		content: function() {
			let element = $( this );
			/*if ( element.is( "[data-geo]" ) ) {
				let text = element.text();
				return "<img class='map' alt='" + text +
				"' src='http://maps.google.com/maps/api/staticmap?" +
				"zoom=11&size=350x350&maptype=terrain&sensor=false&center=" +
				text + "'>";
			}*/
			if ( element.is( "[rowTag]" ) ) {
				return "<img src='" + element.attr( "src" ) + "'/>";
			}
		}
	});
});

</script>