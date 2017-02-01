/**
* Injects the passed tags into the URLs they are attached to. Full page ads and floating elements are removed.
*/ 

//Tags to be injected into the page. The line 'tags = [{id: '5f60af39-8d3a-4bf8-a683-5d83d9ee1db9', tag: 'http://s3.amazonaws.com/asr-tagimages/0b7df242-7e97-48a6-9e43-e3cb4222e5c2.png', placement: 0, width: 320, height: 50},{id: 'b7da488d-9824-4b78-9135-f1107e647844', tag: 'http://s3.amazonaws.com/asr-tagimages/1fb84f0e-7aae-4d60-be75-50824a3c8d6f.png', placement: 0, width: 300, height: 250},{id: '40bcb6d7-45c8-42a5-a8fc-ec58dd3a575f', tag: 'http://s3.amazonaws.com/asr-tagimages/fc534dc6-7836-4b00-a966-8c42cee088e9.png', placement: 0, width: 970, height: 250},{id: '2e756336-cdd4-4c5a-8e3f-f61e0e3972e3', tag: 'http://s3.amazonaws.com/asr-tagimages/1e220cfc-e9dd-453f-8260-011f4a4fee14.png', placement: 0, width: 300, height: 600},];' is necessary to get the
//tags from the calling java instance.
let tags = [];
//tags = [{id: '28577acb-9fbe-4861-a0ef-9d1a7397b4c9', tag: 'http://s3.amazonaws.com/asr-tagimages/07809e6b-9f3a-42aa-8fe0-6ba0adb102d0.png', placement: 0, width: 728, height: 90},{id: 'ab4ec323-f91b-4578-a6c8-f57e5fca5c87', tag: 'http://s3.amazonaws.com/asr-tagimages/f050eb7d-9a0c-4781-849c-bf34629e5695.png', placement: 0, width: 300, height: 250},{id: 'b722d748-dd25-493e-93c5-6fc1991f6392', tag: 'http://s3.amazonaws.com/asr-tagimages/074df31b-25d1-4a19-9b42-c8b8ab780738.png', placement: 0, width: 300, height: 50},{id: 'b4cce6c3-d68c-4cb4-b50c-6c567e0d3789', tag: 'http://s3.amazonaws.com/asr-tagimages/59b1ba0b-cf8a-4295-b578-fecefd91e907.png', placement: 0, width: 320, height: 50},{id: '312e383f-314e-4ba2-85f0-5f6937990fa6', tag: 'http://s3.amazonaws.com/asr-tagimages/aa0a39ab-1abb-48a3-a2c2-458ae0b54c4f.png', placement: 0, width: 300, height: 600},];
tags = [{id: '5f60af39-8d3a-4bf8-a683-5d83d9ee1db9', tag: 'http://s3.amazonaws.com/asr-tagimages/0b7df242-7e97-48a6-9e43-e3cb4222e5c2.png', placement: 0, width: 320, height: 50},{id: 'b7da488d-9824-4b78-9135-f1107e647844', tag: 'http://s3.amazonaws.com/asr-tagimages/1fb84f0e-7aae-4d60-be75-50824a3c8d6f.png', placement: 0, width: 300, height: 250},{id: '40bcb6d7-45c8-42a5-a8fc-ec58dd3a575f', tag: 'http://s3.amazonaws.com/asr-tagimages/fc534dc6-7836-4b00-a966-8c42cee088e9.png', placement: 0, width: 970, height: 250},{id: '2e756336-cdd4-4c5a-8e3f-f61e0e3972e3', tag: 'http://s3.amazonaws.com/asr-tagimages/1e220cfc-e9dd-453f-8260-011f4a4fee14.png', placement: 0, width: 300, height: 600},];

/**
*  wkyc.com exception script
*
*  On desktop Chrome, the top banner ad often is not displayed at all or is displayed 
*  as a much larger ad size. When the latter occurs, the image is replaced by the
*  Ad Injecter but is stretched to the larger size.
*
*  This script replaces the entire ad div element, and its children, with a dummy
*  holder div with the correct margin and 728x90 size. A second 728x90 div
*  is placed inside of it with the flood-opacity set so it will be replaced
*  by the ad injecter.
*
*  The div used on the page has the id 'layout-column_column-1'.
*/

//-------------------------- Testing Tags (Comment out before upload!!!)
/*
tags = [
	{id: '28577acb-9fbe-4861-a0ef-9d1a7397b4c9', tag: 'http://s3.amazonaws.com/asr-tagimages/07809e6b-9f3a-42aa-8fe0-6ba0adb102d0.png', placement: 0, width: 728, height: 90},
	{id: 'ab4ec323-f91b-4578-a6c8-f57e5fca5c87', tag: 'http://s3.amazonaws.com/asr-tagimages/f050eb7d-9a0c-4781-849c-bf34629e5695.png', placement: 0, width: 300, height: 250},
	//{id: 'b722d748-dd25-493e-93c5-6fc1991f6392', tag: 'http://s3.amazonaws.com/asr-tagimages/074df31b-25d1-4a19-9b42-c8b8ab780738.png', placement: 0, width: 300, height: 50},
	//{id: 'b4cce6c3-d68c-4cb4-b50c-6c567e0d3789', tag: 'http://s3.amazonaws.com/asr-tagimages/59b1ba0b-cf8a-4295-b578-fecefd91e907.png', placement: 0, width: 320, height: 50},
	//{id: '312e383f-314e-4ba2-85f0-5f6937990fa6', tag: 'http://s3.amazonaws.com/asr-tagimages/aa0a39ab-1abb-48a3-a2c2-458ae0b54c4f.png', placement: 0, width: 300, height: 600}
];//*/

//Check to see if a 728x90 or 320x50 tag has been passed by the AdShotter
let found728x90 = false;
let found320x50 = false;
let found300x250 = false;
let found300x600 = false;
for (tagIndex in tags) {

    let currentTag = tags[tagIndex];
    if ((currentTag.width == 728) && (currentTag.height == 90)) {
    	found728x90 = true;
    }
    if ((currentTag.width == 320) && (currentTag.height == 50)) {
    	found320x50 = true;
    }
    if ((currentTag.width == 300) && (currentTag.height == 250)) {
    	found300x250 = true;
    }
    if ((currentTag.width == 300) && (currentTag.height == 600)) {
    	found300x600 = true;
    }
}

//Get the div holding the banner ad
//this selector works for mobile and desktop
let bannerAdDiv = document.querySelector("#layout-column_column-1 div.portlet-body");

//Get the desktop right column ad if it exists
let columnAd = document.querySelector("#layout-column_column-3 .mod-wrapper.ad-300");

//See if this is a desktop story article
let isStoryArticle = (document.querySelector(".story-utility-bar")) ? true: false;

//Run exception on desktop browsers (no mobile)
if ((!navigator.userAgent.toLowerCase().includes("mobile"))) {

	//////////////////////////// Banner Ad Desktop ///////////////////////////////

	//If a 728x90 tag has been passed, replace the header ad with a 728x90 div
	//and then place a second div inside of the first for the ad injecter to
	// replace
	if (found728x90) {

		//remove all of the elements children
		while (bannerAdDiv.firstChild) {
			bannerAdDiv.removeChild(bannerAdDiv.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		bannerAdDiv.style.visibility = 'visible';
		bannerAdDiv.style.width = '728px';
		bannerAdDiv.style.height = '90px';
		bannerAdDiv.style.margin = "0 auto";
		bannerAdDiv.style.marginBottom = "10px";

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '728px';
		adFillerDiv.style.height = '90px';
		bannerAdDiv.appendChild(adFillerDiv);
	}

	//Otherwise, hide the 728x90 in case it doesn't load correctly
	else {
		bannerAdDiv.parentElement.parentElement.parentElement.style.display = 'none';
	}

	//////////////////////////// Column Ad Desktop ///////////////////////////////

	//If there is a side column, a 300x250 tag, BUT NOT a 300x600, 
	//replace the ad element with a filler
	if ((columnAd) && (found300x250) && (!found300x600)) {

		console.log("Column and 300x50");
		//remove all of the elements children
		while (columnAd.firstChild) {
			columnAd.removeChild(columnAd.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		columnAd.style.visibility = 'visible';
		columnAd.style.width = '300px';
		columnAd.style.height = '250px';
		columnAd.style.margin = "0 auto";
		columnAd.style.marginLeft = "20px";
		columnAd.parentElement.style.marginLeft = "20px";
		//columnAd.style.marginBottom = "10px";

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '300px';
		adFillerDiv.style.height = '250px';
		columnAd.appendChild(adFillerDiv);
	}

	//If there is a side column, a 300x600 tag, BUT NOT a 300x250, 
	//replace the ad element with a filler
	else if ((columnAd) && (found300x600) && (!found300x250)) {

		//remove all of the elements children
		while (columnAd.firstChild) {
			columnAd.removeChild(columnAd.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		columnAd.style.visibility = 'visible';
		columnAd.style.width = '300px';
		columnAd.style.height = '600px';
		columnAd.style.margin = "0 auto";
		columnAd.style.marginLeft = "20px";
		columnAd.parentElement.style.marginLeft = "20px";
		//columnAd.style.marginBottom = "10px";

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '300px';
		adFillerDiv.style.height = '600px';
		columnAd.appendChild(adFillerDiv);
	}
}

//Run exception on mobile
if ((navigator.userAgent.toLowerCase().includes("mobile"))) {

	//If a 320x50 tag has been passed, replace the header ad with a 320x50 div
	//and then place a second div inside of the first for the ad injecter to
	// replace
	if (found320x50) {

		//Remove all of the elements children
		while (bannerAdDiv.firstChild) {
			bannerAdDiv.removeChild(bannerAdDiv.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		bannerAdDiv.style.visibility = 'visible';
		bannerAdDiv.style.width = '320px';
		bannerAdDiv.style.height = '50px';
		bannerAdDiv.style.margin = "0 auto";
		bannerAdDiv.style.marginBottom = "10px";
		bannerAdDiv.style.martinTop = "10px"

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '320px';
		adFillerDiv.style.height = '50px';
		bannerAdDiv.appendChild(adFillerDiv);
	}
}



//Remove the scrollbars
document.documentElement.style.overflow = 'hidden';

//Initialize and get the adInjecter object
let adInjecter = initializeAdInjecter(tags);
adInjecter.injectTagsIntoPage();

console.log(adInjecter.injectedTags);

//Return the list of injected tags and the lowest tag image bottom position. The position is returned as an array to help clarify java code.
//return JSON.stringify({"injectedTagIDs": adInjecter.injectedTagIDs, "lowestTagPosition": [adInjecter.lowestTagPosition]});
return JSON.stringify(adInjecter.injectedTags);
//console.log(adInjecter.outputString);



/**
* Initializes and returns the AdInjecter.
*
* Builds the object and sets the passed arguments.
*
* @param {Object} 	tags  				Tags object, array of associative arrays {tag:...,placement...,width:...,height:...}
* @return {Object}						AdInjecter object
*/
function initializeAdInjecter(tags) {

	//Create the AdInjecter object
	let adInjecter = {

		//Set the object variables
		_tags: tags,											//Tags to inject into page
		_ads: [],												//List of ads found on the page
		_possibleAdElements: [],								//List of unmarked elements the size of a tag. Used if tag not injected.
		injectedTagIDs: [],										//List of the IDs of the tags that were injected into the page
		injectedTags: {},										//List of the tags injected into the page and their positions
																//Associative array of tag IDs to array of x,y position
																//{TAGID1:[[100,200]], TAGID2:[[200,300],[300,400]]}
		lowestTagPosition: 0,									//The bottom position of the lowest tag image. Used for screenshot cropping.
		outputString: "",										//String of output from the different called functions
		_LARGEADWIDTH: 971,										//Width in pixels for an ad to be considered very large
		_LARGEADHEIGHT: 274,									//Height in pixels for an ad to be considered very large
		_LARGENODEWIDTH1: 900,									//Width in pixels for an individual node to be considered large
		_LARGENODEHEIGHT1: 300,									//Height in pixels for an individual node to be considered large
		_LARGENODEWIDTH2: 400,									//Width in pixels for an individual node to be considered large
		_LARGENODEHEIGHT2: 700,									//Height in pixels for an individual node to be considered large

		/**
		* Injects the object's tags into the current page.
		*/
		injectTagsIntoPage: function() {

			//Get all of the ad elements from the page
			adInjecter._removeScreenStealersAndRetrieveAds(document);

			//Get the sorted ads and tags
			let sortedAds = adInjecter._getAdElementsSortedByPosition(adInjecter._ads); 
			let sortedPossibleAdElements = adInjecter._getAdElementsSortedByPosition(adInjecter._possibleAdElements); 
			let sortedTags = adInjecter._getTagsSortedByPlacement(); 

			//Inject the tags
			adInjecter._replaceAdsWithTags(sortedTags, sortedAds);

			//Remove the tags that have been injected and replace the possible ad elements with the rest
			let anAdWasInjected = false;
			Object.keys(sortedTags).forEach(function (currentTagKey) {
				for (let tagIndex = sortedTags[currentTagKey].length - 1; tagIndex >= 0; tagIndex--) {
					if (sortedTags[currentTagKey][tagIndex].injected) {
						sortedTags[currentTagKey].splice(tagIndex, 1);
						anAdWasInjected = true;
					}
				}
			});

			//If any tags have not been injected, do so on possible ad elements
			if (!anAdWasInjected) {adInjecter._replaceAdsWithTags(sortedTags, sortedPossibleAdElements);}
		},

		_replaceAdsWithTags: function(sortedTags, sortedAds) {

			adInjecter.outputString += "Replacing ads: ";

			//Loop through tag array and insert tags where ads of the same dimensions exist
			Object.keys(sortedTags).forEach(function (currentTagKey) {

				adInjecter.outputString += currentTagKey + ": ";
			   
				//If ads of the same dimensions exist, replace them with the tags
				if (sortedAds[currentTagKey] != null) {

					//Loop through each ad and replace it with a tag
					let currentAdArray = sortedAds[currentTagKey];
					let currentTagArray = sortedTags[currentTagKey];
					let currentTagIterator = 0;
					if (currentTagArray.length > 0) {

						for (let adIndex = 0; adIndex < currentAdArray.length; ++adIndex) {

							adInjecter.outputString += adIndex + ", ";

							//If another tag exists in the array and the current ad position is
							//not the same as the last, increase the iterator
							if ((adIndex != 0) &&
								(currentTagArray[currentTagIterator + 1] != null) &&
								((currentAdArray[adIndex].xPosition != currentAdArray[adIndex - 1].xPosition) ||
								(currentAdArray[adIndex].yPosition != currentAdArray[adIndex - 1].yPosition))) {
								++currentTagIterator;
							}

							//Use unique variables for readability
							let currentAd = currentAdArray[adIndex];
							let currentTag = currentTagArray[currentTagIterator];

							//If the current ad has a valid element
							if (currentAd.element) {

								console.log("Replacing: " + currentAd.width + "x" + currentAd.height + " - " + currentAd.element.nodeName + " - " + currentAd.element.id);

								//Remove all the child nodes of the ad element
								while (currentAd.element.firstChild) {
									currentAd.element.removeChild(currentAd.element.firstChild);
								}
								currentAd.element.innerHTML = "";

								//Replace the ad with the tag
								let tagImage = document.createElement('img');
								tagImage.src = currentTag.tag;
								tagImage.style.width = currentAd.width + 'px';
								tagImage.style.height = currentAd.height + 'px';
								tagImage.style.floodOpacity = "0.9898";
								if (currentAd.element && currentAd.element.parentNode) {
									currentAd.element.parentNode.replaceChild(tagImage, currentAd.element);

									//Mark the tag as injected and store it in the injected tags ID array.
									//The maximum top limit fixes the issue when a top ad element does not
									//load correctly or is not flagged with the flood-opacity tag but
									//gets injected lower in the page with the flood-opacity tag. It forces
									//the AdInjecter to replace the element even  								
									let tagImageBox = tagImage.getBoundingClientRect();
									//if (tagImageBox.top < 500) {
										
										//Mark the tag as injected so it does not get replaced by the
										//second run through matching only position and not flood-opacity
										sortedTags[currentTagKey][currentTagIterator].injected = true;

										//If the ad has not been stored as injected, add it
										if (adInjecter.injectedTagIDs.indexOf(currentTag.id) <= -1) {
											adInjecter.injectedTagIDs[adInjecter.injectedTagIDs.length] = currentTag.id;
										}

										//If the tag has not been added to the injected tags object, add it
										if (!adInjecter.injectedTags.hasOwnProperty(currentTag.id)) {
											adInjecter.injectedTags[currentTag.id] = [];
										}

										//Add the position of the injected tag to the injected tags object
										let injectedTagInstances = adInjecter.injectedTags[currentTag.id].length;
										adInjecter.injectedTags[currentTag.id][injectedTagInstances] = [tagImageBox.left, tagImageBox.top];
									//}

									//If this tag image is the lowest on the page so far, store its lower position
									if (tagImageBox.bottom > adInjecter.lowestTagPosition) {
										adInjecter.lowestTagPosition = Math.round(tagImageBox.bottom);
									}

								}
								else if (currentAd.element) {
									console.log("Couldn't replace - " + currentAd.element.nodeName + ": " + currentAd.element.id);
								}
								else {console.log("element gone");}

								//Change parents' width/height if need be
								//-------------------Seems possibly broken-------------------------
								if (currentAd.element.parentNode) {
									adInjecter._expandParents(currentAd.element.parentNode, currentAd.width, currentAd.height);

									let targetNode = currentAd.element;
									targetNode.style.display = '';
									while (targetNode = targetNode.parentNode) {
										targetNode.style.display = '';
									}

								}
							}
						} 
					}
				}
			});
			adInjecter.outputString += "\n\n";

		},

		/**
		* Loops through the page, grabs and places ads in object variable, and removes
		* screen stealers. As it reaches a frame, it calls itself on the frame document recursively.
		*
		* A screen stealer is an ad or otherwise that popups, covers the page, takes up
		* a large portion of screen space, or fixes itself to the screen.
		*
		* @param {DocumentElement} 	curDocument  	Document to process
		*/
		_removeScreenStealersAndRetrieveAds: function(curDocument) {

			//If the current document doesn't have a body, return out of recursion
			if (curDocument.body == null) {
				adInjecter.outputString += "!!No document body found!!"; 
				return;
			}

			//Get all of the nodes in the passed document
			let headNodes = [].slice.call(curDocument.head.getElementsByTagName("*"));
			let bodyNodes = [].slice.call(curDocument.body.getElementsByTagName("*"));
			let nodes = headNodes.concat(bodyNodes);

			//Loop through for each element
			Array.prototype.forEach.call(nodes, function(curNode) {

				//adInjecter.outputString += "Node Type: " + curNode.nodeName + " (" + curNode.id + ")\n";

				//Remove any border from the node
				//-----------------------Should we be doing this?------------------------
				//curNode.style.setProperty('border', 0);

				//Get the basic info from the node
				let floodOpacity = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('flood-opacity');
				let nodeBounds = curNode.getBoundingClientRect();
				let nodeWidth = curNode.offsetWidth;
				let nodeHeight = curNode.offsetHeight;
				let zIndex = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('z-index');
				let xyPosition = adInjecter._getNodePosition(curNode);

				//Remove any margin from the top HTML element
				//---------------------------Should this be here?-----------------------------
				//-------------------------Yes, weird chicago eatery things-------------------
				//curDocument.getElementsByTagName('html')[0].style.margin = 0;

				//If the flood value is recognized, store the ad OR
				//If the node is an uncrawlable IFRAME, use it if its dimensions match a tag
				if ((floodOpacity == '0.9898') ||
					((curNode.nodeName == "IFRAME") && //(!curNode.contentDocument) && 
					 (adInjecter._areDimensionsOfATag(nodeWidth, nodeHeight)))) {

					console.log("Ad found: " + curNode.nodeName + " - " + nodeWidth + "x" + nodeHeight);

					//Store the ad and its info
					adInjecter._ads.push({
						element: curNode,
						xPosition: xyPosition.x,
						yPosition: xyPosition.y,
						width: nodeWidth,
						height: nodeHeight,
						bottom: nodeBounds.bottom,
					});

					//Gets the smallest viewable size of the node
					//-------------------------------------Clean up this and function-----------------------------
					let smallestContainerNode = adInjecter._getSmallestContainingNode(curNode);
					nodeWidth = smallestContainerNode.offsetWidth;
					nodeHeight = smallestContainerNode.offsetHeight;

					//If it very large and not a tag dimension, hide it
					//-------------------------------Check hide ad element function-----------------------
					//--------------------------------Check/clean up sizes.node hide----------------------
					if ((nodeWidth > adInjecter._LARGEADWIDTH) && (nodeHeight > adInjecter._LARGEADHEIGHT)) {
						if (!adInjecter._areDimensionsOfATag(nodeWidth, nodeHeight)) {
							adInjecter.outputString += "Hiding large frame ---\n";
							let farthestIFrame = adInjecter._getFarthestSameDimensionsIFrame(curNode);
							if (farthestIFrame) {
								farthestIFrame.style.display = 'none';
								adInjecter.outputString += "Large IFrame found \n";
							}
							adInjecter._hideAdElement(curNode);
							if (curNode != smallestContainerNode) {
								smallestContainerNode.style.display = 'none';
							}
						}
					}			
				}

				//Otherwise, if the element is the same size as a tag but not flagged or an iframe,
				//store it as a possible ad element.
				else if ((curNode.nodeName != "IFRAME") &&  
					 	 (adInjecter._areDimensionsOfATag(nodeWidth, nodeHeight))) {

					//Store the ad and its info
					adInjecter._possibleAdElements.push({
						element: curNode,
						xPosition: xyPosition.x,
						yPosition: xyPosition.y,
						width: nodeWidth,
						height: nodeHeight,
						bottom: nodeBounds.bottom,
					});
				}

				//If the node has a fixed position and is a screen stealer, hide it
				//------------------Check out both functions--------------------
				if (adInjecter._isFixedPositionScreenStealer(curNode)) {
					console.log("Removing fixed position node: " + curNode.id);
					adInjecter._hideAdElement(curNode);
				}


				try {
					//If the node is an iframe, find the ads in it too
					if ((curNode.nodeName == "IFRAME") && (curNode.contentDocument)) {

						//Call the function on the new iframe
						adInjecter._removeScreenStealersAndRetrieveAds(curNode.contentDocument);
					}
				}
				catch(err) {
				}


			});
		},


		/**
		* Hides the passed node. If the node's parent has the same dimensions, it gets
		* hidden as well. This continues recursively until a new node size is reached.
		*
		* Hiding consists of setting the display style to 'none'.
		*
		* @param {HTMLElement} 	curNode  		Node to hide
		*/
		_hideAdElement: function(curNode) {

			//Hide the passed element node
			//curNode.style.display = 'none';

			let curParent = curNode.parentNode;
			curNode.style.setProperty('border', 0);

			if (curParent) {
				//curParent.style.setProperty('border', 0);

				//If the parent has the same dimensions, hide the parent too
				if ((curNode.offsetWidth == curParent.offsetWidth) &&
					(curNode.offsetHeight == curParent.offsetHeight)) {
					adInjecter._hideAdElement(curParent);
				}
				else if ((curNode.nodeName == "A") || (curNode.nodeName == "OBJECT")) {
					adInjecter._hideAdElement(curParent);
				}
			}

			curNode.style.display = 'none';

		},


		/**
		* Returns true if some tag has the passed dimensions and false otherwise.
		*
		* @param int 		width  			Width in pixels to search for
		* @param int 		height  		Height in pixels to search for
		*
		* @return boolean 					TRUE if a tag exists with the passed dimensions, FALSE otherwise
		*/
		_areDimensionsOfATag: function(width, height) {
			for (let i = 0; i < adInjecter._tags.length; ++i) {
				if ((width == adInjecter._tags[i].width) && (height == adInjecter._tags[i].height)) {
					//console.log("Are Dimensions: " + width + ", " + height + " (" + adInjecter._tags[i].tag + ")");
					return true;
				}
			}
			return false;
		},

		/**
		* Determines whether or not the passed node is a fixed position screen stealer. A fixed position
		* screen stealer is an element with a fixed position, a positive z-index, is not the dimension,
		* of any tag stored in the object AND EITHER is fixed other than 0,0 OR is very large as
		* defined by the LARGENODE dimensions stored as as constants in the object.
		*
		* @param HTMLElement	curNode  	Node to examine
		*
		* @return boolean 					TRUE if a tag is fixed position screen stealer, FALSE otherwise
		*/
		_isFixedPositionScreenStealer: function(curNode) {

			//Get the node info
			let nodeBounds = curNode.getBoundingClientRect();
			let nodeWidth = curNode.offsetWidth;
			let nodeHeight = curNode.offsetHeight;
			let zIndex = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('z-index');
			let position = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('position');
			let topMargin = parseInt(document.defaultView.getComputedStyle(curNode,null).getPropertyValue('margin-top'), 10);
			let leftMargin = parseInt(document.defaultView.getComputedStyle(curNode,null).getPropertyValue('margin-left'), 10);
			let topPosition = nodeBounds.top - topMargin;
			let leftPosition = nodeBounds.left - leftMargin;

			//If the node has a positive index, a fixed position, 
			//and is not the dimensions of a tag, see if its a screen stealer
			if ((zIndex > 1) && (position == 'fixed') && 
				(!adInjecter._areDimensionsOfATag(nodeWidth, nodeHeight))) {

				//If the node is fixed anywhere other than the top left corner, return it is a screen stealer
				if ((topPosition > 0) || (leftPosition > 0)) {return true;}

				//Otherwise, if it is very large, return that it is a screen stealer
				else if (((nodeWidth > adInjecter._LARGENODEWIDTH1) && (nodeHeight > adInjecter._LARGENODEHEIGHT1)) ||
					((nodeWidth > adInjecter._LARGENODEWIDTH2) && (nodeHeight > adInjecter._LARGENODEHEIGHT2)) ||
					(((nodeWidth/window.innerWidth) > 0.96) && (nodeHeight > adInjecter._LARGENODEHEIGHT2))) {
					return true;
				}
			}

			//Otherwise, it is not a screen stealer
			return false;
		},

		/**
		* Returns an object of all the ad elements where arrays of the ad elements are keyed by their dimensions. The
		* arrays themselves are sorted by best page location with the most prominent first. 
		*
		* The keys are strings of WIDTHxHEIGHT.
		*
		* @param HTMLElement	adElements  	Array of ad elements including their dimensions
		*
		* @return boolean 						Sorted ads object
		*/
		_getAdElementsSortedByPosition: function(adElements) {
			
			//console.log(adElements);
			//console.log("Length: " + Object.keys(adElements).length);
			//Define the sort function that will be used for the ad arrays.
			//Ads closer to the top right of the screen are put closer to the beginning of the array.
			//Distance from the top of the screen always takes presidence over distance from the left.
			let adSortFunction = function(firstAd, secondAd) {
			    
			    firstAdFactor = firstAd.yPosition + firstAd.xPosition/1000;
			    secondAdFactor = secondAd.yPosition + secondAd.xPosition/1000;
			    
			    return firstAdFactor - secondAdFactor;
			};

			//Loop through the ads and add them to the final associative object
			adInjecter.outputString += "Ad dimensions: ";
			let sortedAds = {};
			for (adIndex in adElements) {

				//Get the current ad and figure out its key
			    let currentAd = adElements[adIndex];
			    let currentAdKey = currentAd.width + 'x' + currentAd.height;

			    //If that key does not exist in the object yet, create it and its associated array
			    if (sortedAds[currentAdKey] == null) {
			    	adInjecter.outputString += currentAdKey + ", ";
			    	sortedAds[currentAdKey] = [];
			    }

			    //Add the current ad to the array
			    sortedAds[currentAdKey].push(currentAd);

			    //Sort the array. While unnecessary to do everytime here, it's simple and clear
			    sortedAds[currentAdKey].sort(adSortFunction);
			}
			adInjecter.outputString += "\n\n";

			//Return the final sorted object
			return sortedAds;
		},

		/**
		* Returns an object of all the tags where arrays of the ads are keyed by their placement. The
		* arrays themselves are sorted by placement with lowest placement number first. 
		*
		* The keys are strings of WIDTHxHEIGHT.
		*
		* @return boolean 					Sorted tags object
		*/
		_getTagsSortedByPlacement: function() {
			
			//Define the sort function that will be used for the tag arrays.
			//Tags are sorted by their placement with lower numbers being placed first in arrays
			let tagSortFunction = function(firstAd, secondAd) {
			    return firstAd.placement - secondAd.placement;
			};

			//Loop through the tags and add them to the final associative object
			let sortedTags = {};
			for (tagIndex in adInjecter._tags) {

				//Get the current tag and figure out its key
			    let currentTag = adInjecter._tags[tagIndex];
			    let currentTagKey = currentTag.width + 'x' + currentTag.height;

			    //If that key does not exist in the object yet, create it and its associated array
			    if (sortedTags[currentTagKey] == null) {
			    	sortedTags[currentTagKey] = [];
			    }

			    //Add the current tag to the array
			    sortedTags[currentTagKey].push(currentTag);

			    //Sort the array. While unnecessary to do everytime here, it's simple and clear
			    sortedTags[currentTagKey].sort(tagSortFunction);
			}

			//Return the final sorted object
			return sortedTags;
		},

		/**
		* Sets the element to the passed dimensions if the element is of a smaller dimension. It then
		* calls itself recursively on subsequent parents until an element of equal or greater size is
		* encountered.
		*
		* @param HTMLElement	currentNode 	Node to examine and resize if necessary
		* @param int 			width  			Width in pixels to compare and size to
		* @param int 			height  		Height in pixels to compare and size to
		*/
		_expandParents: function(currentNode, width, height) {

			//Grab the passed node's dimensions
			currentNode.style.setProperty('border', 0);
			let nodeWidth = currentNode.offsetWidth;
			let nodeHeight = currentNode.offsetHeight;

			//If the element is smaller than the passed dimensions, increase its size and check its parent
			if ((nodeWidth < width) || (nodeHeight < height)) {
				currentNode.style.width = width;
				currentNode.style.height = height;

				if (currentNode.parentNode) {
					adInjecter.outputString += "Expanding parent (" + width + ", " + height + "): " + currentNode.nodeName + "\n";
					adInjecter._expandParents(currentNode.parentNode, width, height);
				}	
			}
		},

		/**
		* Returns the smallest and farthest containing node holding the passed node in the passed node's frame.
		*
		* This function is used in the situation where an ad might have rather large non-visible dimensions.
		* It's displayed size is then determined by the smallest containing node. 
		*
		* The returned node can be the same as the passed node.
		*
		* Nodes with a width and height less than 5px, anchors, or objects are not counted.
		*
		* @param HTMLElement	containedNode	Node used to find smallest containing node
		* @return HTMLElement 					Smallest farthest containing node. Can be same as passed node.
		*/
		_getSmallestContainingNode: function(containedNode) {

			//Start by getting passed node's width and height
			let smallestNode = containedNode;
			let smallestNodeWidth = containedNode.offsetWidth;
			let smallestNodeHeight = containedNode.offsetHeight;
			let currentNode = smallestNode;

			//adInjecter.outputString += "Smallest begin: " + currentNode.nodeName + " - " + smallestNodeWidth + 
			//							"x" + smallestNodeHeight + "\n";

			//Loop through the node's parents and find the smallest farthest node
			currentNode = currentNode.parentNode;
			while (currentNode) {

				//Get the width and height of the currentNode
				let currentNodeWidth = currentNode.offsetWidth;
				let currentNodeHeight = currentNode.offsetHeight;

				//If the node is greater than 5x5, is not an anchor or object,
				//and is smaller or equal to previous smallest node, mark it as the smallest
				if ((currentNodeWidth > 5) && (currentNodeHeight > 5)) {
					if ((currentNode.nodeName != "A") && (currentNode.nodeName != "OBJECT")) {
						if ((currentNodeWidth <= smallestNodeWidth) && 
							(currentNodeHeight <= smallestNodeHeight)) {

							let smallestNode = currentNode;
							let smallestNodeWidth = currentNodeWidth;
							let smallestNodeHeight = currentNodeHeight;
						}
					}
				}

				//Set the node to its parent or containing frame
				if (currentNode.parentNode) {currentNode = currentNode.parentNode;}
				else {currentNode = adInjecter._getContainingFrame(currentNode);}
				/*if (currentNode) {
					adInjecter.outputString += "Current node: " + currentNode.nodeName + " - " + currentNodeWidth + 
										"x" + currentNodeHeight + "(" + smallestNodeWidth + "x" + smallestNodeHeight + ")\n";
				}
				else {
					adInjecter.outputString += "Current node is null\n";
				}*/

			}

			//Finally, return the smallest farthest containing node (can be same as passed node)
			return smallestNode;
		},

		/**
		* Returns the node closest up the tree that has the dimensions of a tag. If there are none, the
		* originally passed tag is returned.
		*
		* @param HTMLElement	startNode		Node used to find closest tag sized node
		* @return HTMLElement 					Closest tag sized node or originally passed node on none found
		*/
		_getClosestTagSizedNode: function(startNode) {

			//Loop through the node's parents and find the closest tag sized node
			currentNode = startNode.parentNode;
			while (currentNode && (!adInjecter._areDimensionsOfATag(currentNode.offsetWidth, currentNode.offsetHeight))) {

				//Set the node to its parent or containing frame
				if (currentNode.parentNode) {currentNode = currentNode.parentNode;}
				else {currentNode = adInjecter._getContainingFrame(currentNode);}
			}

			//Return either the tag sized node or the original node
			if (!currentNode) {return startNode;}
			else if (adInjecter._areDimensionsOfATag(currentNode.offsetWidth, currentNode.offsetHeight)) {
				return currentNode;
			}
			else {return startNode;}

		},

		/**
		* Returns the IFrame farthest from the passed node with the same dimensions. If there are none, null
		* originally passed tag is returned.
		*
		* @param HTMLElement	startNode		Node used to find farthest IFrame of same dimensions
		* @return HTMLElement 					Farthest IFrame of same dimensions as passed node or null on failure
		*/
		_getFarthestSameDimensionsIFrame: function(startNode) {

			//Loop through the node's iframes and find the farthest with the same dimensions
			let nodeWidth = startNode.offsetWidth;
			let nodeHeight = startNode.offsetHeight;
			let farthestFrame = null;
			currentFrame = adInjecter._getContainingFrame2(startNode);
			adInjecter.outputString += "Starting frame: " + currentFrame + " from " + startNode + "\n";
			while (currentFrame) {

				adInjecter.outputString += "IFrame comparison: " + nodeWidth + "x" + nodeHeight + " vs " +
											currentFrame.offsetWidth + "x" + currentFrame.offsetHeight + "\n";
				if ((currentFrame.offsetWidth == nodeWidth) && (currentFrame.offsetHeight == nodeHeight)) {
					farthestFrame = currentFrame;
				}
				currentFrame = adInjecter._getContainingFrame2(currentFrame);
			}

			//Return the farthest IFrame of same dimensions or null if not found
			return farthestFrame;
		},

		/**
		* Returns the node's containing IFrame or null if not inside an IFrame.
		*
		* @param HTMLElement	containedNode	Node inside possible IFrame
		* @return IFrame 						IFrame containing node or null otherwise
		*/
		_getContainingFrame: function(containedNode) {

			//If the current node is not a document node, grab the containing document
			let frameDocument = (containedNode.ownerDocument) ? containedNode.ownerDocument : containedNode;

			//Get the name of the frame if one exists
			let frameName = frameDocument.defaultView.name;
			//adInjecter.outputString += "Frame name: " + frameName + "\n";

			//If there is a name, return the frame. Otherwise, return null.
			if (window.frames[frameName]) {
				try {
					return window.frames[frameName].frameElement;
				}
				catch(err) {
					return null;
				}
			}
			else {return null;}
		},
		_getContainingFrame2: function(containedNode) {

			//If the current node is not a document node, grab the containing document
			let frameDocument = (containedNode.ownerDocument) ? containedNode.ownerDocument : containedNode;

			//Get the name of the frame if one exists
			return frameDocument.defaultView.frameElement;

			let frameName = frameDocument.defaultView.name;
			//adInjecter.outputString += "Frame name: " + frameName + "\n";

			//If there is a name, return the frame. Otherwise, return null.
			if (window.frames[frameName]) {return window.frames[frameName].frameElement;}
			else {return null;}
		},

		/**
		* Returns the x,y coorindates of the passed node in relation to the screen view.
		*
		* @param HTMLElement	positionedNode		Node used to find smallest containing node
		* @return Object  							Object with 'x','y' coordinates of node and 'element' as node, as properties
		*/
		_getNodePosition: function(positionedNode) {

			//Grab the initial locations
			let boundingRectangle = positionedNode.getBoundingClientRect();
			let xPosition = boundingRectangle.left;
			let yPosition = boundingRectangle.top;

			//For each frame we are in, add the frame's coordinates to the base coordinates
			let curFramElement = positionedNode;
			while (curFramElement = adInjecter._getContainingFrame(curFramElement)) {
				boundingRectangle = curFramElement.getBoundingClientRect();
				xPosition += boundingRectangle.left;
				yPosition += boundingRectangle.top;
			}

			//Return the object
			return {
				'x': Math.round(xPosition), 
				'y': Math.round(yPosition), 
				'element': positionedNode
			};
		},
	}

	//Return the initialized object
	return adInjecter;
}