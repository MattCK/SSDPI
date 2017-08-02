
/**
* Contains the tag parser object used to parse tags out of text, text files, or zip files
*
* @package AdShotRunner
* @subpackage JavaScript
*/

/**
* The tagParser object used to parse tags out of text, text files, or zip files
*/
let tagParser = {

	/**
	* Parses the tags out of the passed text and returns them as an array of strings
	*
	* @param {String} adTagText  	Text to parse for tags
	* @return Array[String]  		Array of parsed tag strings
	*/
	getTags: function(adTagText) {

		//test for scripts pasted in from excel
		//adjust accordingly
		// if(adTagText.includes("\"<script language=\"\"javascript\"\"")){
  //           //this regex is to replace all occurences not just the first
  //           adTagText = adTagText.replace(new RegExp('\"<script', 'g'), '<script');
  //           adTagText = adTagText.replace(new RegExp('script>\"', 'g'), 'script>');
  //           adTagText = adTagText.replace(new RegExp('\"\"', 'g'), '\"');
		// }

		//Remove double "double quotes"
		//When pasting a tag from excel, a single "double quote" is inserted as two "double quotes"
		// adTagText = adTagText.replace(/""/g, '"');
		//scriptText.match(/([^"]|^)"([^"]|$)/gmi);
		//replace(/(?!=?""\s+\w)("")/g, '"')

		//Loop through the ad tag text and grab the HTML parts along with their type
		let adTagRegularExpression = /<(\w*)\b[^>]*>[\s\S]*?<\/\1>/gmi;
		let textHTMLParts = [];
		let currentHTMLPart = [];
		while ((currentHTMLPart = adTagRegularExpression.exec(adTagText)) !== null) {

			//Store the parts as named variables for clarity
			let htmlPart = currentHTMLPart[0];
			let htmlPartType = currentHTMLPart[1].toLowerCase();

			//If there are no single double-quotes, replace all double quotes
			//with single quotes. This check is due to Excel. Copying text
			//from excel automatically turns all single double-quotes into
			//double double-quotes. i.e. "mytext" => ""mytext""
			if (htmlPart.match(/([^"]|^)"([^"]|$)/gmi) == null) {
				htmlPart = htmlPart.replace(/""/g, '"');
			}

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

		//Loop through the text again and this time grab img tags.
		let imgRegularExpression = /<img[^>]+src="?[^"\s]+"?\s*\/>/g;
		let currentIMGTag;
		while ((currentIMGTag = imgRegularExpression.exec(adTagText)) !== null) {

			textHTMLParts.push({
				html: currentIMGTag[0],
				type: 'img',
				isSource: false,
			});
		}


		//If no parts were found, return an empty array
		if (textHTMLParts.length == 0) {return [];}
		
		//To simplify code below, let's mark each type of HTML tags we are working with
		let hasHTMLScriptTag, hasHTMLIFrameTag, hasHTMLNoscriptTag, hasHTMLAnchorTag, hasHTMLImageTag;
		for (let htmlPartIndex = 0; htmlPartIndex < textHTMLParts.length; ++htmlPartIndex) {
			switch (textHTMLParts[htmlPartIndex].type) {
				case 'script': hasHTMLScriptTag = true; break;
				case 'iframe': hasHTMLIFrameTag = true; break;
				case 'noscript': hasHTMLNoscriptTag = true; break;
				case 'a': hasHTMLAnchorTag = true; break;
				case 'img': hasHTMLImageTag = true; break;
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

					//If the tag is an LCI marketing tag, simply ignore it
					if (tagParser.isLCIScriptTag(textHTMLParts[htmlPartIndex].html)) {
						//Ignore the tag
					}

					//If it is a script part without a source (generally used to set variables),
					//simply concatenate it to the existing tag
					else if (!textHTMLParts[htmlPartIndex].isSource) {
						currentScriptTag += textHTMLParts[htmlPartIndex].html;
					}

					//Otherwise, concatenate it and store it as a tag then clear the current tag string
					else {
						currentScriptTag += textHTMLParts[htmlPartIndex].html;
						adTags.push(currentScriptTag);
                      //check if this tag is a media math bidding tag
                        //and if so removes it from the list
                        // if(currentScriptTag.includes("Enter LCI parameters") || currentScriptTag.includes("Do no modify below this line")|| currentScriptTag.includes("lcip = {")){
                        //     //just ignore the tag and don't add it to the list
                        // }
                        // else{
                        //     adTags.push(currentScriptTag);
                        // }
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

			//Otherwise, if no script, iframe, noscript, or anchor tags but an anchor, only store img tags
			else if (hasHTMLImageTag) {
				if (textHTMLParts[htmlPartIndex].type == 'img') {
					adTags.push(textHTMLParts[htmlPartIndex].html);
				}
			}

			//And if no correct tag types are found, simply do nothing
		}

		//Return all found tags
		return adTags;
	},

	isLCIScriptTag: function(scriptText) {

		return (scriptText.match(/lci parameter|lcip =|ninthdecimal/ig)) ? true :false;
	},

	/**
	* Determines if the passed HTML <script> element includes a source
	*
	* @param {String} scriptHTML  	HTML of <script> element
	* @return {Boolean}		  		True if the <script> element includes a source and false otherwise
	*/
	scriptHasSource: function(scriptHTML) {

		let hasSource = false;
		if (scriptHTML.toLowerCase().indexOf('src=') > -1) {hasSource = true;}
		if (scriptHTML.toLowerCase().indexOf('document.write') > -1) {hasSource = true;}
		return hasSource;
	},

	/**
	* Parses the tag text files passed through the file drop event object and sends the tags
	* to the main asr object for tag imaging.
	*
	* @param {String} event  	File drop event
	*/
	handleTagTextFileDrop: function(event) {

		//Prepare for the file processing
		event.stopPropagation();
		event.preventDefault();

		//Remove the drag styling
		base.nodeFromID(this.id).classList.remove("dropBoxHasDragOver");

		//Store the files
		let files = event.dataTransfer.files;

		//If no files were dropped, do nothing
		if (files.length == 0) {return;}

		//Loop through the files and get their text
		for (let i = 0, currentFile; currentFile = files[i]; i++) {
	
			//Create the reader to read the current file
			let reader = new FileReader();

			//If the current file is a text file, get the ad tags and add them to the queue
			if (currentFile.type.match('text.*')) {

				//For each file, add its tags to the queue and store the text for future analysis
				reader.onload = (function(theFile) {
					return function(e) {
						asr.addTagsToQueue(tagParser.getTags(e.target.result));
						asr.storeTagText(e.target.result);
					};
				})(currentFile);

				//Read the file as text
				reader.readAsText(currentFile);
			}

			//If the current file is an image, get the ad tags and add them to the queue
			else if (currentFile.type.match('image.*')) {

				//For each file, add its tags to the queue and store the text for future analysis
				/*reader.onload = (function(theFile) {
					return function(e) {
						console.log("Dropped image file");
						asr.uploadTagImage(e.target.result);
					};
				})(currentFile);

				//Read the file as text
				reader.readAsDataURL(currentFile);*/

				asr.uploadTagImage(currentFile);
			}

		}
	},

	/**
	* Parses the tag zip file passed through the file drop event object and sends the tags
	* to the main asr object for tag imaging.
	*
	* @param {String} event  	File drop event
	*/
	handleTagZipFileDrop: function(event) {

		//Prepare for the file processing
		event.stopPropagation();
		event.preventDefault();

		//Remove the drag styling
		base.nodeFromID(this.id).classList.remove("dropBoxHasDragOver");

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

						console.log(entry);

						//Get and store entry content as text
						entry.getData(new zip.TextWriter(), function(text) {

							//For each file, add its tags to the queue and store the text for future analysis
							asr.addTagsToQueue(tagParser.getTags(text));
							asr.storeTagText(text);

							// close the zip reader
							reader.close();
						});
					}
				}
			});
		});
	},

	/**
	* Parses the tag text in the tag text box and sends the tags
	* to the main asr object for tag imaging.
	*/
	handleTagTextboxInput: function() {

		//Get the textbox element
		let tagTextTextbox = base.nodeFromID("tagTextTextbox");
		let tagText = tagTextTextbox.value;

		//If there is nothing entered, do nothing
		if (tagText == "") {return;}

		//Add the found tags to the queue and save the tag text
		asr.addTagsToQueue(tagParser.getTags(tagText));
		asr.storeTagText(tagText);
	},

	/**
	* Stops any propogation on a file drop area
	*/
	handleDragOver: function(event) {
		base.nodeFromID(this.id).classList.add("dropBoxHasDragOver");
		event.stopPropagation();
		event.preventDefault();
	},


	/**
	* Handles the drag leave. Used for CSS styling.
	*/
	handleDragLeave: function(event) {
		base.nodeFromID(this.id).classList.remove("dropBoxHasDragOver");
		event.stopPropagation();
		event.preventDefault();
	},

}
