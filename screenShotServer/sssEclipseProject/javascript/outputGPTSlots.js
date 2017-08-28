/**
* The Coordinates class stores x and y coordinates.
*/
class Coordinates {

	/**
	* Initializes the Coordinates with its x and y positions.
	*
	* @param {Number} 	xPosition  		x position
	* @param {Number} 	yPosition  		y position
	*/
	constructor(xPosition, yPosition) {

		//If any of the arguments are missing, throw an error
		if ((xPosition == null) || (yPosition == null)) {
				throw "Coordinates.constructor: missing argument";
		}

		//Verify integers were passed and meet the correct criteria
		if (isNaN(xPosition)) {throw "Coordinates.constructor: xPosition must be a number";}
		if (isNaN(yPosition)) {throw "Coordinates.constructor: yPosition must be a number";}

		//Store the member properties
		this._x = xPosition;
		this._y = yPosition;
	}

	/**
	* @return {Number}	x position
	*/	
	x() {return this._x;}
	
	/**
	* @return {Number}	y position
	*/	
	y() {return this._y;}
	
}


/**
* The ElementInfo class contains a series of static function to obtain current
* information on an HTMLElement node..
*
* All information is calculated on the current state of the node.
*/
class ElementInfo {

	/**
	* @return {Number}	Current width of the element
	*/	
	static width(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return elementNode.offsetWidth;
	}

	/**
	* @return {Number}	Current width of the element not including the border
	*/	
	static widthWithoutBorder(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return elementNode.offsetWidth - 
			   ElementInfo.borderWidthLeft(elementNode) - ElementInfo.borderWidthRight(elementNode);
	}

	/**
	* @return {Number}	Current height of the element
	*/	
	static height(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return elementNode.offsetHeight;
	}

	/**
	* @return {Number}	Current height of the element not including the border
	*/	
	static heightWithoutBorder(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return elementNode.offsetHeight - 
			   ElementInfo.borderWidthTop(elementNode) - ElementInfo.borderWidthBottom(elementNode);
	}

	/**
	* @return {Number}	Current x position of the element
	*/	
	static xPosition(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return (ElementInfo.getScreenCoordinates(elementNode)).x();
	}

	/**
	* @return {Number}	Current y position of the element
	*/	
	static yPosition(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return (ElementInfo.getScreenCoordinates(elementNode)).y();
	}

	/**
	* @return {Number}	Current bottom border width of the element
	*/	
	static borderWidthBottom(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		let widthString = document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('border-bottom-width');
		return Number(widthString.slice(0, -2));
	}
	
	/**
	* @return {Number}	Current left border width of the element
	*/	
	static borderWidthLeft(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		let widthString = document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('border-left-width');
		return Number(widthString.slice(0, -2));
	}
	
	/**
	* @return {Number}	Current right border width of the element
	*/	
	static borderWidthRight(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		let widthString = document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('border-right-width');
		return Number(widthString.slice(0, -2));
	}
	
	/**
	* @return {Number}	Current top border width of the element
	*/	
	static borderWidthTop(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		let widthString = document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('border-top-width');
		return Number(widthString.slice(0, -2));
	}
	
	/**
	* @return {String}	Current flood-opacity of the element
	*/	
	static floodOpacity(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('flood-opacity');
	}
	
	/**
	* @return {Integer}	Current bottom margin of the element
	*/	
	static marginBottom(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return parseInt(document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('margin-bottom'), 10);
	}
	
	/**
	* @return {Integer}	Current left margin of the element
	*/	
	static marginLeft(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return parseInt(document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('margin-left'), 10);
	}
	
	/**
	* @return {Integer}	Current right margin of the element
	*/	
	static marginRight(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return parseInt(document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('margin-right'), 10);
	}
	
	/**
	* @return {Integer}	Current top margin of the element
	*/	
	static marginTop(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return parseInt(document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('margin-top'), 10);
	}
	
	/**
	* @return {Integer}	Current bottom padding of the element
	*/	
	static paddingBottom(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return parseInt(document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('padding-bottom'), 10);
	}
	
	/**
	* @return {Integer}	Current left padding of the element
	*/	
	static paddingLeft(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return parseInt(document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('padding-left'), 10);
	}
	
	/**
	* @return {Integer}	Current right padding of the element
	*/	
	static paddingRight(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return parseInt(document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('padding-right'), 10);
	}
	
	/**
	* @return {Integer}	Current top padding of the element
	*/	
	static paddingTop(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return parseInt(document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('padding-top'), 10);
	}

	/**
	* @return {Integer}	Current "position" style of the element, such as 'fixed'
	*/	
	static positionStyle(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('position');
	}
	
	/**
	* @return {String}	Current z-index of the element
	*/	
	static zIndex(elementNode) {
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}
		return document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('z-index');
	}

	/**
	* Returns the x,y coordinates of the passed node in relation to the screen view.
	*
	* @param {HTMLElement}	elementNode		Node used to find smallest containing node
	* @return {Coordinates}  				Coordinates object with x,y set to node's screen position
	*/
	static getScreenCoordinates(elementNode) {

		//Return null if the passed node variable is an HTMLElement
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}

		//If the element display is none, set it to block for the position then set it back
		let displayStatus = document.defaultView.getComputedStyle(elementNode, null).getPropertyValue('display');
		let displayOriginallyNone = false;
		if (displayStatus == "none") {
			elementNode.style.display = "block";
			displayOriginallyNone = true;
		}

		//Grab the initial locations
		let boundingRectangle = elementNode.getBoundingClientRect();
		let xPosition = boundingRectangle.left;
		let yPosition = boundingRectangle.top;

		//For each frame we are in, add the frame's coordinates to the starting coordinates
		let currentFrameElement = elementNode;
		while (currentFrameElement = ElementInfo.getContainingFrame(currentFrameElement)) {
			boundingRectangle = currentFrameElement.getBoundingClientRect();
			xPosition += boundingRectangle.left;
			yPosition += boundingRectangle.top;
		}

		//If the element's display was 'none', set it back to 'none'
		if (displayOriginallyNone) {
			elementNode.style.display = "none";
		}

		//Return the Coordinates
		return new Coordinates(Math.round(xPosition), Math.round(yPosition));
	}

	/**
	* Returns the node's containing IFrame or null if not inside an IFrame.
	*
	* @param {HTMLElement}	containedNode	Node inside possible IFrame
	* @return {Iframe} 						IFrame containing node or null otherwise
	*/
	static getContainingFrame(containedNode) {

		//Return null if the passed node variable is null
		if (containedNode == null) {
			return null;
		}

		//If the current node is not a document node, grab the containing document
		let frameDocument = (containedNode.ownerDocument) ? containedNode.ownerDocument : containedNode;
		return frameDocument.defaultView.frameElement;
	}

	/**
	* Returns true if passed argument is an HTMLElement and false otherwise
	*
	* @param {HTMLElement}	elementNode		Possible HTMLElement
	* @return {Boolean} 					True if elementNode is an HTMLElement and false otherwise
	*/
	static isHTMLElement(elementNode) {

		//If the passed argument does not exist or is an HTMLElement, return false.
		if ((elementNode == null) || (!elementNode.tagName)) {
			return false;
		}

		//Otherwise, return true.
		return true;
	}
}

/**
* The CreativeSize class stores the width and height of a Creative.
*
* Used in the AdSelector class.
*/
class CreativeSize {

	/**
	* Initializes the CreativeSize with its width and height.
	*
	* @param {Integer} 	width  		Width of the creative in pixels (must be greater than 0)
	* @param {Integer} 	height  	Height of the creative in pixels (must be greater than 0)
	*/
	constructor(width, height) {

		//If any of the arguments are missing, throw an error
		if ((width == null) || (height == null)) {
				throw "CreativeSize.constructor: missing argument";
		}

		//Verify integers were passed and meet the correct criteria
		if ((!Number.isInteger(width)) || (width <= 0)) {throw "CreativeSize.constructor: width must be an integer greater than 0";}
		if ((!Number.isInteger(height)) || (height <= 0)) {throw "CreativeSize.constructor: height must be an integer greater than 0";}

		//Store the member properties
		this._width = width;
		this._height = height;
	}

	/**
	* @return {Integer}	Width in pixels
	*/	
	width() {return this._width;}
	
	/**
	* @return {Integer}	Height in pixels
	*/	
	height() {return this._height;}
	
}

/**
* The AdSelector class stores a selector that points to an ad element on the page and
* the possible creative sizes that the element accepts.
*/
class AdSelector {

	/**
	* Initializes the AdSelector with its selector.
	*
	* Receives the optional argument 'hideIfNotReplaced' to flag whether or not
	* the selector element should be hidden if it is not replaced with a Creative.
	* Default: false
	*
	* @param {String} 		selector			Selector to the ad element
	* @param {Booelan} 		hideIfNotReplaced	Optional: Hide the ad element if not replaced with a Creative. Default: false
	*/
	constructor(selector, hideIfNotReplaced) {

		//Verify the selector is a non-empty string
		if ((selector == null) ||(typeof selector !== 'string') || (selector == "")) {
			throw "AdSelector.constructor: selector must be a non-empty string";
		}

		//Store the selector and initialize the sizes array
		this._selector = selector;
		this._sizes = new Set();

		//Flag whether or not to hide the element if not replaced by a creative
		this._hideIfNotReplaced = (hideIfNotReplaced) ? true : false;
	}

	/**
	* Adds the passed width and height as a possible creative size for the ad element
	*
	* @param {Integer} 	width  		Width of the creative in pixels (must be greater than 0)
	* @param {Integer} 	height  	Height of the creative in pixels (must be greater than 0)
	* @return {AdSelector}			This AdSelector instance
	*/	
	addSize(width, height) {

		//If any of the arguments are missing, throw an error
		if ((width == null) || (height == null)) {
				throw "AdSelector addSize: missing argument";
		}

		//Verify integers were passed and meet the correct criteria
		if ((!Number.isInteger(width)) || (width <= 0)) {throw "AdSelector.addSize: width must be an integer greater than 0";}
		if ((!Number.isInteger(height)) || (height <= 0)) {throw "AdSelector.addSize: height must be an integer greater than 0";}

		//Add the CreativeSize to the set
		this._sizes.add(new CreativeSize(width, height));

		//Return this AdSelector instance
		return this;
	}

	/**
	* Adds an array of possible sizes to the AdSelector.
	*
	* The argument must be an array of two element arrays, the latter having only two
	* integers greater than 0 representing the width and height of the creative respectively.
	*
	* Ex: [[300,250],[300,600]]
	*
	* Any [width, height] array not meeting the width and height integer greater than 0 requirement
	* will be ignored.
	*
	* @param {Array} 	sizesArray  Array of [width, height] arrays
	* @return {AdSelector}			This AdSelector instance
	*/	
	addSizes(sizesArray) {

		//Verify an array was passed
		if (!Array.isArray(sizesArray)) {throw "AdSelector.addSizes: argument must be an array";}

		//Add each set of sizes
		for (let currentSize of sizesArray) {
			if ((Array.isArray(currentSize)) && (currentSize.length == 2) &&
				(Number.isInteger(currentSize[0])) && (currentSize[0] > 0) &&
				(Number.isInteger(currentSize[1])) && (currentSize[1] > 0)) {
					this._sizes.add(new CreativeSize(currentSize[0], currentSize[1]));
			}
		}

		//Return this AdSelector instance
		return this;
	}

	/**
	* @return {String}	Selector
	*/	
	selector() {return this._selector;}

	/**
	* @return {Array}	Possible creative sizes associated with the AdSelector. (Array of CreativeSize objects)
	*/	
	sizes() {return this._sizes;}

	/**
	* @return {Boolean}	Whether or not the selector element should be hidden if not replaced by a Creative
	*/	
	hideIfNotReplaced() {return this._hideIfNotReplaced;}

}




/**
* The GPTSlots class stores the googletag.Slot objects on the page at the time of instantiation
* and provides slot size information or AdSelectors for the slots.
*
* Since the googletag.Slot objects are obfuscated, the constructor creates dummy slots with
* a target size to find the size properties in the Slot object. It then uses this information
* to get each Slot's sizes.
*
* If the googletag object does not exist on the page, the functions return empty results.
*/
class GPTSlots {

	/**
	* Initializes the GPTSlots with any Google tag slots currently on the page.
	*/
	constructor() {

		//Create the class constants used to set slot properties and then be searched for
		this._TARGETWIDTH = 6420;
		this._TARGETHEIGHT = 7531;
		this._TARGETVIEWPORTWIDTH = 8642;
		this._TARGETVIEWPORTHEIGHT = 9753; 

		//Begin by instantiating the slots and slots sizes members regardless if the googletag object exists
		this._slots = [];
		this._slotCreativeSizes = new Map();

		//If the googletag object exists, instantiate the class using its information
		if (googletag != null) {

			//Store any googletag.Slot objects
			this._slots = googletag.pubads().getSlots();

			//Get the slot object properties for a slot with sizes passed to the constructor.
			let dummySlot = this._getDummySlot(this._TARGETWIDTH, this._TARGETHEIGHT);
			let propertyInfo = this._getSizesProperties(dummySlot, this._TARGETWIDTH, this._TARGETHEIGHT);
			this._sizesPropertyKey = propertyInfo.containingPropertyKey;
			this._sizesClass = propertyInfo.sizesClass;
			this._sizesWidthKey = propertyInfo.widthKey;
			this._sizesHeightKey = propertyInfo.heightKey;

			//Get the slot size properties for a slot created with a SizeMapping object
			let sizeMappingSlot = this._getDummySlotWithMapping(this._TARGETWIDTH, this._TARGETHEIGHT,
																this._TARGETVIEWPORTWIDTH, this._TARGETVIEWPORTHEIGHT);
			let mappingPropertyInfo = this._getMappingProperties(sizeMappingSlot, this._TARGETVIEWPORTWIDTH, this._TARGETVIEWPORTHEIGHT);
			this._mappingPropertyKey = mappingPropertyInfo.containingPropertyKey;
			this._mappingClass = mappingPropertyInfo.mappingClass;
			this._viewportClass = mappingPropertyInfo.viewportClass;
			this._viewportWidthKey = mappingPropertyInfo.viewportWidthKey;
			this._viewportHeightKey = mappingPropertyInfo.viewportHeightKey;

			//For each slot, get the CreativeSizes
			for (let currentSlot of this._slots) {

				//Add the current slot and its CreativeSizes to the member Map
				this._slotCreativeSizes.set(currentSlot, this._getCreativeSizes(currentSlot));
			}
		}
	}

	/**
	* @return {Array}	Array of Google.Slots on the page when the GPTSlots object was instantiated
	*/	
	slots() {return this._slots;}

	/**
	* @return {Map}	Map of Google.Slots with the values the Set of CreativeSizes for each slot
	*/	
	creativeSizes() {return this._slotCreativeSizes;}

	/**
	* @return {Set}	Set of AdSelectors for all of the Google.Slots. The hide if not replaced flag is set to true.
	*/	
	adSelectors() {

		//Loop through the slots and create an accessor for each one
		let slotAdSelectors = new Set();
		for (let [currentSlot, currentCreativeSizes] of this._slotCreativeSizes) {

			//Create the selector string. If any forward slashes exist, put backslashes before them
			// let slotSelector = "#" + currentSlot.getSlotElementId() + " iframe";
			let slotSelector = "#" + currentSlot.getSlotElementId() + "";
			slotSelector = slotSelector.replace(/\//g, "\\/");
			slotSelector = slotSelector.replace(/\./g, "\\.");

			//Create the AdSelector and add each CreativeSize width and height to it
			let currentAdSelector = new AdSelector(slotSelector, true);
			for (let slotCreativeSize of currentCreativeSizes) {
				currentAdSelector.addSize(slotCreativeSize.width(), slotCreativeSize.height());
			}

			//Add the current selector to the overall set
			slotAdSelectors.add(currentAdSelector);
		}

		//Return the set of AdSelectors
		return slotAdSelectors;
	}


	/**
	* Returns a Set of CreativeSizes for all the possible sizes of the passed slot
	*
	* @param {googletag.Slot}	slot		Slot to get CreativeSizes for
	* @return {Set} 		 				Set of CreativeSizes for the passed slot
	*/
	_getCreativeSizes(slot) {

		//Get any creative sizes in the primary Slot property (created by passing sizes to constructor)
		let primaryCreativeSizes = this._getCreativeSizesInProperty(slot[this._sizesPropertyKey]);

		//Get any creative sizes in the size mapping Slot property (created by passing SizeMapping after constructor)
		let mappedSizes = this._getMappedSizesInProperty(slot[this._mappingPropertyKey]);

		//Get the current browser viewport
		let browserViewportWidth = document.documentElement.clientWidth;
		let browserViewportHeight = document.documentElement.clientHeight;

		//If mapped sizes exist, get the ones matching the viewport
		let noViewportSizes = new Set();
		let viewportSizes = new Set();
		let largestViewportWidth = 0;
		let largestViewportHeight = 0;
		for (let [currentViewport, currentSizes] of mappedSizes) {
			let viewportWidth = currentViewport.get("width");
			let viewportHeight = currentViewport.get("height");

			// //If the viewport is 0x0, the sizes apply to all viewports
			// if ((viewportWidth == 0) && (viewportHeight == 0)) {
			// 	noViewportSizes = currentSizes;
			// }

			//If the viewport is smaller than the browser viewport but
			//larger than the current largest, use its sizes
			//else if ((viewportWidth <= browserViewportWidth) && (viewportHeight <= browserViewportHeight) &&
			if ((viewportWidth <= browserViewportWidth) && (viewportHeight <= browserViewportHeight) &&
					 (viewportWidth >= largestViewportWidth) && (viewportHeight >= largestViewportHeight)) {
				viewportSizes = currentSizes;
				largestViewportWidth = viewportWidth;
				largestViewportHeight = viewportHeight;
			}
		}

		//Remove any CreativeSize duplicates
		//As of writing this, javascript allows overriding all operands except ==, thus requiring a loop
		let allCreativeSizes = new Set([...primaryCreativeSizes, ...viewportSizes, ...noViewportSizes]);
		let uniqueCreativeSizes = new Set();
		for (let currentCreativeSize of allCreativeSizes) {

			//See if a CreativeSize in the Slot creatives set already exists in the unique set
			let sizeFound = false;
			for (let uniqueSize of uniqueCreativeSizes) {
				if ((uniqueSize.width() == currentCreativeSize.width()) &&
					(uniqueSize.height() == currentCreativeSize.height())) {
					sizeFound = true;
				}
			}

			//If the size does not exist in the unique set, add it
			if (!sizeFound) {uniqueCreativeSizes.add(currentCreativeSize);}
		}

		//Return all the unique CreativeSizes for the slot
		return uniqueCreativeSizes;
	}

	/**
	* Returns the property key holding the target size, the sizes class name, as well
	* as the height and width keys in the sizes class.
	*
	* The returned object uses the following structure:
	*
	*		{containingPropertyKey: containingPropertyKey, 
	* 		 sizesClass: sizesClass,
	*		 widthKey: widthKey, 
	* 		 heightKey: heightKey}
	*
	* @param {googletag.Slot}	slot			Slot to get property information for
	* @param {googletag.Slot}	targetWidth		Width of the predefined size to look for
	* @param {googletag.Slot}	targetHeight	Height of the predefined size to look for
	* @return {Object} 		 					Property and class information for the target size (See description for details)
	*/
	_getSizesProperties(slot, targetWidth, targetHeight) {

		//Define the information to retrieve
		let containingPropertyKey = "";
		let sizesClass = "";
		let widthKey = "";
		let heightKey = "";

		//Recursively traverse the slot until the width and height values are met
		//Return after the first matching set is found.
		let cache = [];
		let findSize = function(currentObject) {

			//If we have already seen this object, ignore it and return to prevent circular reference
		    if (typeof currentObject === 'object' && currentObject !== null) {
		        if (cache.indexOf(currentObject) !== -1) {
		            return;
		        }

		        //Store this object in the cache to prevent circular references to it
		        cache.push(currentObject);

		        //Loop through the object's properties, traversing them if necessary, while looking
		        //for a two-property object with the width and height values as the values
				for (var key in currentObject) {
					if (currentObject.hasOwnProperty(key)) {
						let value = currentObject[key];

			        	//Surround the calls in a try catch to prevent IFrame security issues
			        	try {

			        		//If the current value is an object, check it for the widthxheight values
			        		//then recursively traverse it if they are not found
						    if (typeof value == "object" ) {

						    	//If the object has two properties, see if they are the widthxheight values
								if (Object.keys(value).length == 2) {

									//Store the keys for reference
									let firstKey = Object.keys(value)[0];
									let secondKey = Object.keys(value)[1];

									//Check if the properties equal the passed widthxheight values
									if (((value[firstKey] == targetWidth) && (value[secondKey] == targetHeight)) ||
										((value[firstKey] == targetHeight) && (value[secondKey] == targetWidth))) {

											//Store the sizes object class name
											sizesClass = value.constructor.name;

											//Store which key is for width and which for height
											if (value[firstKey] == targetWidth) {
												widthKey = firstKey; heightKey = secondKey;
											}
											else {
												widthKey = secondKey; heightKey = firstKey;
											}

											//Break out of the function now that the size has been found
											return true;
									}
								}

								//Otherwise, traverse the object. If true is returned, set the containing
								//key to the current key and return true as well.
						    	if (findSize(value)) {
						    		containingPropertyKey = key;
						    		return true;
						    	};

						    }
			        	} catch(e) {}
			        }
		        }
			}
		}

		//Call the findSize recursive function on the passed slot
		findSize(slot);

		//Return the found information
		return {containingPropertyKey: containingPropertyKey, sizesClass: sizesClass,
				widthKey: widthKey, heightKey: heightKey};
	}

	/**
	* Returns the property key holding the size mappings with the mapping class, viewport
	* class, and viweport keys.
	*
	* The returned object uses the following structure:
	*
	*		{containingPropertyKey: containingPropertyKey, 
	* 		 mappingClass: mapping class,
	* 		 viewportClass: viewport class,
	*		 viewportWidthKey: viewport width key, 
	* 		 viewportHeightKey: viewport height key}
	*
	* @param {googletag.Slot}	slot					Slot with size mapping to get property information for
	* @param {googletag.Slot}	targetViewportWidth		Width of the predefined viewport to look for
	* @param {googletag.Slot}	targetViewportHeight	Height of the predefined viewport to look for
	* @return {Object} 		 							Property and class information for the target mapping (See description for details)
	*/
	_getMappingProperties(slot, targetViewportWidth, targetViewportHeight) {

		//Define the information to retrieve
		let containingPropertyKey = "";
		let mappingClass = "";
		let viewportClass = "";
		let viewportWidthKey = "";
		let viewportHeightKey = "";

		//Recursively traverse the slot until the width and height values are met
		//Return after the first matching set is found.
		let cache = [];
		let findViewport = function(currentObject) {

			//If we have already seen this object, ignore it and return to prevent circular reference
		    if (typeof currentObject === 'object' && currentObject !== null) {
		        if (cache.indexOf(currentObject) !== -1) {
		            return;
		        }

		        //Store this object in the cache to prevent circular references to it
		        cache.push(currentObject);

		        //Loop through the object's properties, traversing them if necessary, while looking
		        //for a two-property object with the viewport width and height values as the values
				for (var key in currentObject) {
					if (currentObject.hasOwnProperty(key)) {
						let value = currentObject[key];

			        	//Surround the calls in a try catch to prevent IFrame security issues
			        	try {

			        		//If the current value is an object, check it for the widthxheight values
			        		//then recursively traverse it if they are not found
						    if (typeof value == "object" ) {

						    	//If the object has two properties, see if they are the viewport widthxheight values
								if (Object.keys(value).length == 2) {

									//Store the keys for reference
									let firstKey = Object.keys(value)[0];
									let secondKey = Object.keys(value)[1];

									//Check if the properties equal the passed widthxheight values
									if (((value[firstKey] == targetViewportWidth) && (value[secondKey] == targetViewportHeight)) ||
										((value[firstKey] == targetViewportHeight) && (value[secondKey] == targetViewportWidth))) {

											//Store the viewport object class name
											viewportClass = value.constructor.name;

											//Store which key is for width and which for height
											if (value[firstKey] == targetViewportWidth) {
												viewportWidthKey = firstKey; viewportHeightKey = secondKey;
											}
											else {
												viewportWidthKey = secondKey; viewportHeightKey = firstKey;
											}

											//Store the mapping class name
											mappingClass = currentObject.constructor.name;

											//Break out of the function now that the size has been found
											return true;
									}
								}

								//Otherwise, traverse the object. If true is returned, set the containing
								//key to the current key and return true as well.
						    	if (findViewport(value)) {
						    		containingPropertyKey = key;
						    		return true;
						    	};

						    }
			        	} catch(e) {}
			        }
		        }
			}
		}

		//Call the findSize recursive function on the passed slot
		findViewport(slot);

		//Return the found information
		return {containingPropertyKey: containingPropertyKey, 
				mappingClass: mappingClass,
				viewportClass: viewportClass,
				viewportWidthKey: viewportWidthKey, 
				viewportHeightKey: viewportHeightKey};
	}

	/**
	* Returns a set of CreativeSizes for each possible size in the passed object
	*
	* @param {googletag.Slot}	slotProperty	Slot object property to find sizes in
	* @return {Set} 		 					Set of CreativeSizes for all the sizes found in the passed object
	*/
	_getCreativeSizesInProperty(slotProperty) {

		//Place this instance into its own variable for use in the recursive loop
		let thisInstance = this;

		//Recursively traverse the slot and store any found sizes
		let cache = [];
		let sizes = new Set();
		let findSizes = function(currentObject) {

			//If we have already seen this object, ignore it and return to prevent circular reference
		    if (typeof currentObject === 'object' && currentObject !== null) {
		        if (cache.indexOf(currentObject) !== -1) {
		            return;
		        }

		        //Store this object in the cache to prevent circular references to it
		        cache.push(currentObject);

		        //Loop through the object's properties and store any found sizes
				for (var key in currentObject) {
					if (currentObject.hasOwnProperty(key)) {
						let value = currentObject[key];

			        	//Surround the calls in a try catch to prevent IFrame security issues
			        	try {

			        		//If the current value is an object, store the sizes or traverse it, whichever applicable
						    if (typeof value == "object" ) {

						    	//If the object is a sizes class, store the size
								if (value.constructor.name == thisInstance._sizesClass) {
									let currentCreativeSize = new CreativeSize(value[thisInstance._sizesWidthKey], 
																			   value[thisInstance._sizesHeightKey]);
									sizes.add(currentCreativeSize);
								}

								//Otherwise, traverse the object. 
						    	else {findSizes(value);};

						    }
			        	} catch(e) {}
			        }
		        }
			}
		}
		//Call the findSizes recursive function on the passed slot
		findSizes(slotProperty);

		//Return the found sizes
		return sizes;
	}


	/**
	* Returns a Map with the viewport width and height in the passed Slot property.
	*
	* The Map keys are 'width' and 'height' respectively.
	*
	* @param {googletag.Slot}	slotProperty			Slot property to find the viewport in
	* @return {Map} 		 							Map of viewport width and height with 'width' and 'height' keys respectively
	*/
	_getViewportInProperty(slotProperty) {

		//Place this instance into its own variable for use in the recursive loop
		let thisInstance = this;

		//Recursively traverse the slot and store the first found viewport
		let cache = [];
		let viewportDimensions = new Map();
		let findViewport = function(currentObject) {

			//If we have already seen this object, ignore it and return to prevent circular reference
		    if (typeof currentObject === 'object' && currentObject !== null) {
		        if (cache.indexOf(currentObject) !== -1) {
		            return;
		        }

		        //Store this object in the cache to prevent circular references to it
		        cache.push(currentObject);

		        //Loop through the object's properties and store any found sizes
				for (var key in currentObject) {
					if (currentObject.hasOwnProperty(key)) {
						let value = currentObject[key];

			        	//Surround the calls in a try catch to prevent IFrame security issues
			        	try {

			        		//If the current value is an object, store the sizes or traverse it, whichever applicable
						    if (typeof value == "object" ) {

						    	//If the object is a viewport class, store the dimensions and return
								if (value.constructor.name == thisInstance._viewportClass) {
									viewportDimensions.set("width", value[thisInstance._viewportWidthKey]);
									viewportDimensions.set("height", value[thisInstance._viewportHeightKey]);
									return true;
								}

								//Otherwise, traverse the object. 
						    	else if (findViewport(value)) {return true;}

						    }
			        	} catch(e) {}
			        }
		        }
			}
		}
		//Call the findViewport recursive function on the passed slot
		findViewport(slotProperty);

		//Return the found viewport
		return viewportDimensions;
	}


	/**
	* Returns a Map of viewports to its Set of CreativeSizes.
	*
	* Each key is a Map with 'width' and 'height' keys for the viewport. Each value is a Set of CreativeSizes
	* for the viewport.
	*
	* @param {googletag.Slot}	slotProperty	Slot object property to find sizes in
	* @return {Map} 		 					Map of viewports to their Set of CreativeSizes for all the sizes found in the passed object
	*/
	_getMappedSizesInProperty(slotProperty) {

		//Place this instance into its own variable for use in the recursive loop
		let thisInstance = this;

		//Recursively traverse the slot and store any found sizes
		let cache = [];
		let mappedSizes = new Map();
		let findSizes = function(currentObject) {

			//If we have already seen this object, ignore it and return to prevent circular reference
		    if (typeof currentObject === 'object' && currentObject !== null) {
		        if (cache.indexOf(currentObject) !== -1) {
		            return;
		        }

		        //Store this object in the cache to prevent circular references to it
		        cache.push(currentObject);

		        //Loop through the object's properties and store any found sizes
				for (var key in currentObject) {
					if (currentObject.hasOwnProperty(key)) {
						let value = currentObject[key];

			        	//Surround the calls in a try catch to prevent IFrame security issues
			        	try {

			        		//If the current value is an object, store the sizes or traverse it, whichever applicable
						    if (typeof value == "object" ) {

						    	//If the object is a size mapping class, store its viewport and sizes
								if (value.constructor.name == thisInstance._mappingClass) {
									let currentViewport = thisInstance._getViewportInProperty(value);
									let viewportSizes = thisInstance._getCreativeSizesInProperty(value);
									mappedSizes.set(currentViewport, viewportSizes);
								}

								//Otherwise, traverse the object. 
						    	else {
						    		findSizes(value);
						    	};

						    }
			        	} catch(e) {}
			        }
		        }
			}
		}
		//Call the findSizes recursive function on the passed slot
		findSizes(slotProperty);

		//Return the found sizes
		return mappedSizes;
	}

	/**
	* @return {googletag.Slot} 		Newly created slot with the size arguments passed to the Slot constructor
	*/
	_getDummySlot(targetWidth, targetHeight) {

		return googletag.defineSlot('/dummyPath' + Date.now(), [[targetWidth, targetHeight]], 'dummmyElementID' + Date.now());
	}

	/**
	* @return {googletag.Slot} 		Newly created slot with the size arguments added to the slot through a size mapping
	*/
	_getDummySlotWithMapping(targetWidth, targetHeight, targetViewPortWidth, targetViewPortHeight) {

		//Create a dummy slot with no sizes
		let dummySlot = googletag.defineSlot('/dummyWithMappingPath' + Date.now(), [], 'dummmyWithMappingElementID' + Date.now());

		//Create and add a size mapping to the slot using the size arguments and return it
		let mapping = googletag.sizeMapping().
		    addSize([targetViewPortWidth, targetViewPortHeight], [targetWidth, targetHeight]).build();
		dummySlot.defineSizeMapping(mapping);
		return dummySlot;
	}

}


//Get the current slots and their ad selectors
let someSlots = new GPTSlots();
let slotAdSelectors = someSlots.adSelectors();

//Output the selector, sizes, and node of each AdSelector
for (let currentAdSelector of slotAdSelectors) {
	console.log("---------------------------- Selector: " + currentAdSelector.selector() + "----------------------------");
	for (let currentSize of currentAdSelector.sizes()) {
		console.log("	" + currentSize.width() + "x" + currentSize.height());
	}
	console.log("");
	let slotCoordinates = ElementInfo.getScreenCoordinates(document.querySelector(currentAdSelector.selector()));
	if (slotCoordinates) {console.log("Position: " + slotCoordinates.x() + ", " + slotCoordinates.y());}
	console.log("");
	console.log(document.querySelector(currentAdSelector.selector()));
}