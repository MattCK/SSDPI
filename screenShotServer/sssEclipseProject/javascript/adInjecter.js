/**
* Injects the passed tags into the URLs they are attached to. Full page ads and floating elements are removed.
*/ 

//Tags to be injected into the page. The line '//INSERT TAGS OBJECT//' is necessary to get the
//tags from the calling java instance.
var tags = [];
//INSERT TAGS OBJECT//

//Retrieve the viewport size. If not available, use 1024 x 768
var viewportWidth = 1024;//(system.args[1]) ? system.args[1] : 1024;
var viewportHeight = 768;//(system.args[2]) ? system.args[2] : 768;

//alert('beginning');
//Initialize and get the adInjecter object
var adInjecter = initializeAdInjecter(tags, viewportWidth, viewportHeight);
adInjecter.injectAdsIntoPage();
return adInjecter.outputString;



/**
* Initializes and returns the AdInjecter.
*
* Builds the object and sets the passed arguments.
*
* @param {Object} 	tags  				Tags object, array of associative arrays {tag:...,placement...,width:...,height:...}
* @param {Integer} 	viewportWidth  		Width of browser viewport in pixels
* @param {Integer} 	viewportHeight  	Height of browser viewport in pixels
* @return {Object}						AdInjecter object
*/
function initializeAdInjecter(tags, viewportWidth, viewportHeight) {

	//Create the AdInjecter object
	var adInjecter = {

		//Set the object variables
		_tags: tags,											//Tags to inject into page
		_viewportWidth: viewportWidth,							//Width of browser viewport in pixels
		_viewportHeight: viewportHeight,						//Height of browser viewport in pixels
		_ads: [],												//List of ads found on the page
		outputString: "",										//String of output from the different called functions
		_LARGEADWIDTH: 750,										//Width in pixels for an ad to be considered very large
		_LARGEADHEIGHT: 249,									//Height in pixels for an ad to be considered very large
		_LARGENODEWIDTH1: 900,									//Width in pixels for an individual node to be considered large
		_LARGENODEHEIGHT1: 300,									//Height in pixels for an individual node to be considered large
		_LARGENODEWIDTH2: 400,									//Width in pixels for an individual node to be considered large
		_LARGENODEHEIGHT2: 700,									//Height in pixels for an individual node to be considered large

		/**
		* Injects the object's tags into the current page.
		*/
		injectAdsIntoPage: function() {

			//Get all of the ad elements from the page
			adInjecter._removeScreenStealersAndRetrieveAds(document);

			//Get the sorted ads and tags
			var sortedAds = adInjecter._getAdsSortedByPosition(); 
			var sortedTags = adInjecter._getTagsSortedByPlacement(); 
			adInjecter.outputString += "Tag dimensions: ";

			//Loop through tag array and insert tags where ads of the same dimensions exist
			Object.keys(sortedTags).forEach(function (currentTagKey) {
			   
				adInjecter.outputString += currentTagKey + ": ";
				//If ads of the same dimensions exist, replace them with the tags
				if (sortedAds[currentTagKey] != null) {

					//Loop through each ad and replace it with a tag
					var currentAdArray = sortedAds[currentTagKey];
					var currentTagArray = sortedTags[currentTagKey];
					var currentTagIterator = 0;
					for (var adIndex = 0; adIndex < currentAdArray.length; ++adIndex) {

						adInjecter.outputString += adIndex + ", ";


						//If another tag exists in the array and the current ad position is
						//not the same as the last, increase the iterator
						if ((adIndex != 0) &&
							(currentTagArray[currentTagIterator + 1] != null) &&
							((currentAdArray[adIndex].xPosition != currentAdArray[adIndex - 1].xPosition) ||
							(currentAdArray[adIndex].yPosition != currentAdArray[adIndex - 1].yPosition))) {
							++currentTagIterator;
						}

						var currentAd = currentAdArray[adIndex];
						var currentTag = currentTagArray[currentTagIterator];


						/*adInjecter.outputString += "\n";
						if (currentTagKey == "300x250") {
							adInjecter.outputString += "Iterator: " + currentTagIterator + " - " + currentAd.xPosition + ", " + currentAd.yPosition + "\n";

						}*/

						//Remove all the child nodes of the ad element
						while (currentAd.element.firstChild) {
							currentAd.element.removeChild(currentAd.element.firstChild);
						}
						currentAd.element.innerHTML = "";


						//Replace the ad with the tag
						var tagImage = document.createElement('img');
						tagImage.src = currentTag.tag;
						tagImage.style.floodOpacity = "0.9898";
						currentAd.element.parentNode.replaceChild(tagImage, currentAd.element);

						//nodeBounds = tagImage.getBoundingClientRect();
						//adInjecter.outputString += "After Replace: " + nodeBounds.left + ", " +nodeBounds.top+ ", " + nodeBounds.right + ", " + nodeBounds.bottom + "\n";

						//Change parents' width/height if need be
						if (currentAd.element.parentNode) {
							adInjecter._expandParents(currentAd.element.parentNode, currentAd.width, currentAd.height);
						}
					} 

				}
			});
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
		_removeScreenStealersAndRetrieveAds: function(curDocument, xOffset, yOffset) {

			//If the current document doesn't have a body, return out of recursion
			if (curDocument.body == null) {
				adInjecter.outputString += "!!No document body found!!"; 
				return;
			}

			//Set the offsets to 0 if not defined
			if (!xOffset) {xOffset = 0;}
			if (!yOffset) {yOffset = 0;}

			//Get all the tags in the passed document
			var nodes = curDocument.body.getElementsByTagName("*");

			//Loop through for each element
			Array.prototype.forEach.call(nodes, function(curNode) {

				//adInjecter.outputString += "Node Type: " + curNode.nodeName + "\n";

				//Remove any border from the node
				curNode.style.setProperty('border', 0);

				//Get the basic info from the node
				var floodOpacity = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('flood-opacity');
				var nodeBounds = curNode.getBoundingClientRect();
				var nodeWidth = curNode.offsetWidth;
				var nodeHeight = curNode.offsetHeight;
				var zIndex = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('z-index');
				var position = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('position');
				

				/*if (curNode.id == 'hymnal-container') {
					adInjecter.outputString += "Hymnal Found: " + nodeWidth + ", " + nodeHeight + "\n";
					//adInjecter._hideAdElement(curNode);
				}*/

				//Remove any margin from the top HTML element
				curDocument.getElementsByTagName('html')[0].style.margin = 0;

				//If the flood value is recognized, store the ad
				if (floodOpacity == '0.9898') {

					adInjecter._ads.push({
						element: curNode,
						xPosition: Math.round(nodeBounds.left + xOffset),
						yPosition: Math.round(nodeBounds.top + yOffset),
						width: nodeWidth,
						height: nodeHeight,
						bottom: nodeBounds.bottom,
					});

					if ((nodeWidth > 5) && (nodeHeight > 5)) {
						adInjecter.outputString += "Ad Position (" + nodeWidth + "x" + nodeHeight + "): " + Math.round(nodeBounds.left + xOffset) + ", " + Math.round(nodeBounds.top + yOffset) + ", " + curNode.nodeName + "\n";
					}

					//If it very large and not a tag dimension, hide it
					if ((nodeWidth > adInjecter._LARGEADWIDTH) && (nodeHeight > adInjecter._LARGEADHEIGHT)) {
						adInjecter.outputString += "--LARGE AD--\n";
						if (!adInjecter._areDimensionsOfATag(nodeWidth, nodeHeight)) {adInjecter._hideAdElement(curNode);}
					}			
				}

				//If the node has a fixed position and is a screen stealer, hide it
				if (adInjecter._isFixedPositionScreenStealer(curNode)) {
					adInjecter.outputString += "--FIXED SCREEN STEALER--\n";
					adInjecter._hideAdElement(curNode);
				}

				//If the node is an iframe, find the ads in it too
				if ((curNode.nodeName == "IFRAME") && (curNode.contentDocument)) {

					//Get the offsets of the iframe in relation to the containing frame and add them to the total offsets
					iframeBounds = curNode.getBoundingClientRect();
					iframeXOffset = Math.round(iframeBounds.left) + xOffset;
					iframeYOffset = Math.round(iframeBounds.top) + yOffset;

					//Call the function on the new iframe with the new offsets
					adInjecter._removeScreenStealersAndRetrieveAds(curNode.contentDocument, iframeXOffset, iframeYOffset);
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

			var curParent = curNode.parentNode;
			curNode.style.setProperty('border', 0);
			curParent.style.setProperty('border', 0);

			adInjecter.outputString += "Hiding (" + curNode.offsetWidth + ", " + curNode.offsetHeight + "): " + curNode.nodeName + "\n";
			adInjecter.outputString += "    Parent (" + curParent.offsetWidth + ", " + curParent.offsetHeight + "): " + curParent.nodeName + "\n";

			//If the parent has the same dimensions, hide the parent too
			if ((curNode.offsetWidth == curParent.offsetWidth) &&
				(curNode.offsetHeight == curParent.offsetHeight)) {
				adInjecter._hideAdElement(curParent);
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
			for (var i in adInjecter._tags) {
				if ((width == adInjecter._tags[i].width) && (height == adInjecter._tags[i].height)) {
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
			var nodeBounds = curNode.getBoundingClientRect();
			var nodeWidth = curNode.offsetWidth;
			var nodeHeight = curNode.offsetHeight;
			var zIndex = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('z-index');
			var position = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('position');

			//If the node has a positive index, a fixed position, 
			//and is not the dimensions of a tag, see if its a screen stealer
			if ((zIndex > 1) && (position == 'fixed') && 
				(!adInjecter._areDimensionsOfATag(nodeWidth, nodeHeight))) {

				//If the node is fixed anywhere other than the top left corner, return it is a screen stealer
				if ((nodeBounds.top > 0) || (nodeBounds.left > 0)) {return true;}

				//Otherwise, if it is very large, return that it is a screen stealer
				else if (((nodeWidth > adInjecter._LARGENODEWIDTH1) && (nodeHeight > adInjecter._LARGENODEHEIGHT1)) ||
					((nodeWidth > adInjecter._LARGENODEWIDTH2) && (nodeHeight > adInjecter._LARGENODEHEIGHT2))) {
					return true;
				}
			}

			//Otherwise, it is not a screen stealer
			return false;
		},

		/**
		* Returns an object of all the ads where arrays of the ads are keyed by their dimensions. The
		* arrays themselves are sorted by best page location with the most prominent first. 
		*
		* The keys are strings of WIDTHxHEIGHT.
		*
		* @return boolean 					Sorted ads object
		*/
		_getAdsSortedByPosition: function() {
			
			//Define the sort function that will be used for the ad arrays.
			//Ads closer to the top right of the screen are put closer to the beginning of the array.
			//Distance from the top of the screen always takes presidence over distance from the left.
			var adSortFunction = function(firstAd, secondAd) {
			    
			    firstAdFactor = firstAd.yPosition + firstAd.xPosition/1000;
			    secondAdFactor = secondAd.yPosition + secondAd.xPosition/1000;
			    
			    return firstAdFactor - secondAdFactor;
			};

			//Loop through the ads and add them to the final associative object
			adInjecter.outputString += "Ad dimensions: ";
			var sortedAds = {};
			for (adIndex in adInjecter._ads) {

				//Get the current ad and figure out its key
			    var currentAd = adInjecter._ads[adIndex];
			    var currentAdKey = currentAd.width + 'x' + currentAd.height;

			    //If that key does not exist in the object yet, create it and its associated array
			    if (sortedAds[currentAdKey] == null) {
			    	adInjecter.outputString += currentAdKey + ", ";
			    	sortedAds[currentAdKey] = [];
			    }

			    //See if the ad already exists in the array
			    /*var alreadyExists = false;
			    var adArray = sortedAds[currentAdKey];
			    if (adArray.length > 0 && (currentAdKey != "0x0")) {
			    	for (var t = 0; t < adArray.length; ++t) {
			    		if ((currentAd.xPosition == adArray[t].xPosition) &&
			    			(currentAd.yPosition == adArray[t].yPosition)) {
			    			alreadyExists = true;
			    			/*adInjecter.outputString += "\nAd already exists (" + currentAdKey + "): \n";
			    			adInjecter.outputString += "A: " + currentAd.xPosition + ", " + currentAd.yPosition + "\n";
			    			adInjecter.outputString += "B: " + adArray[t].xPosition + ", " + adArray[t].yPosition + "\n";
			    		}
			    	}
			    }

			    //Add the current ad to the array
			    if (!alreadyExists) {sortedAds[currentAdKey].push(currentAd);}*/
			    sortedAds[currentAdKey].push(currentAd);

			    //Sort the array. While unnecessary to do everytime here, it's simple and clear
			    sortedAds[currentAdKey].sort(adSortFunction);
			}
			adInjecter.outputString += "\n";

			/*adInjecter.outputString += "Ads - 300x250 - (y,x): ";
			if (sortedAds['300x250']) {
				var standardArray = sortedAds['300x250'];
				for (var i = 0; i < standardArray.length; ++i) {
					adInjecter.outputString += standardArray[i].yPosition + ", " + standardArray[i].xPosition + " - ";
				}	
			}
			adInjecter.outputString += "\n";*/


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
			var tagSortFunction = function(firstAd, secondAd) {
			    return firstAd.placement - secondAd.placement;
			};

			//Loop through the tags and add them to the final associative object
			var sortedTags = {};
			for (tagIndex in adInjecter._tags) {

				//Get the current tag and figure out its key
			    var currentTag = adInjecter._tags[tagIndex];
			    var currentTagKey = currentTag.width + 'x' + currentTag.height;

			    //If that key does not exist in the object yet, create it and its associated array
			    if (sortedTags[currentTagKey] == null) {
			    	sortedTags[currentTagKey] = [];
			    }

			    //Add the current tag to the array
			    sortedTags[currentTagKey].push(currentTag);

			    //Sort the array. While unnecessary to do everytime here, it's simple and clear
			    sortedTags[currentTagKey].sort(tagSortFunction);
			}

			/*adInjecter.outputString += "Tags: ";
			var standardArray = sortedTags['300x250'];
			for (var i = 0; i < standardArray.length; ++i) {
				adInjecter.outputString += standardArray[i].placement + ", ";
			}	
			adInjecter.outputString += "\n";*/

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
			var nodeWidth = currentNode.offsetWidth;
			var nodeHeight = currentNode.offsetHeight;

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


	}

	//Return the initialized object
	return adInjecter;
}

function getCoordinates(element) {
    var top = 0, left = 0;
 	adInjecter.outputString += 'Position Elements: ';
	do {
    	adInjecter.outputString += element.nodeName + ', ';
        top += element.offsetTop  || 0;
        left += element.offsetLeft || 0;
        element = element.parentNode;
        //if (element.nodeName == 'BODY') {element = '';}
    } while(element);
 	adInjecter.outputString += "\n";

    return {
        top: top,
        left: left
    };
};