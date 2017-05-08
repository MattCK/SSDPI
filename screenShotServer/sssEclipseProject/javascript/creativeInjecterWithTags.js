/**
* ---------------------------------------------------------------------------------------
* ----------------------------------- CreativeInjecter ----------------------------------------
* ---------------------------------------------------------------------------------------
*	
* This CreativeInjecter script inserts creative images in the running page while removing
* pop-up ads and overlays. 
* 
* The script is designed to be run by and return a JSON response to PhantomJS.
*/

//---------------------------------------------------------------------------------------
//------------------------------------ Classes ------------------------------------------
//---------------------------------------------------------------------------------------
/**
* The Log class both outputs messages to the browser console and stores the messages. The stored log
* is returned with the injected tags (and their locations) at the end of script execution.
*/
class Log {

	/**
	* Outputs the passed message to the browser console and stores the message
	*
	* @param {String} 	message  		Message to output to browser console and store
	*/	
	static output(message) {
		console.log(message);
		Log.messages += message + "\n";
	}	

	/**
	* Returns all the messages stored in the log (separated by newlines)
	*
	* @return {String}			All log messages (separated by newlines)
	*/	
	static getMessages() {
		return Log.messages;
	}	
}
Log.messages = "";	//Static variables for the Log class to store all messages

/**
* The Creative class stores the basic information for an ad creative image. 
*
* It contains the creative unique ID, image URL, width and height in pixels,
* and display priority.
*/
class Creative {

	/**
	* Initializes the Creative with its ID, image URL, width, height, and priority.
	*
	* @param {String} 	id  		Unique ID for the creative
	* @param {String} 	imageURL  	URL of the creative image
	* @param {Integer} 	width  		Width of the creative in pixels (must be greater than 0)
	* @param {Integer} 	height  	Height of the creative in pixels (must be greater than 0)
	* @param {Integer} 	priority  	Display priority for the creative (Lower numbers displayed first)
	*/
	constructor(id, imageURL, width, height, priority) {

		//If any of the arguments are missing, throw an error
		if ((id == null) ||
			(imageURL == null) ||
			(width == null) ||
			(height == null) ||
			(priority == null)) {
				throw "Creative constructor: missing argument";
		}

		//Verify strings and integers were passed and meet the correct criteria
		if ((typeof id !== 'string') || (id == "")) {throw "Creative.constructor: id must be a non-empty string";}
		if ((typeof imageURL !== 'string') || (imageURL == "")) {throw "Creative.constructor: imageURL must be a non-empty string";}
		if ((!Number.isInteger(width)) || (width <= 0)) {throw "Creative.constructor: width must be an integer greater than 0";}
		if ((!Number.isInteger(height)) || (height <= 0)) {throw "Creative.constructor: height must be an integer greater than 0";}
		if ((!Number.isInteger(priority))) {throw "Creative.constructor: priority must be an integer";}

		//Store the member properties
		this._id = id;
		this._imageURL = imageURL;
		this._width = width;
		this._height = height;
		this._priority = priority;
	}

	/**
	* @return {String}	Creative ID
	*/	
	id() {return this._id;}

	/**
	* @return {String}	URL of the creative image
	*/	
	imageURL() {return this._imageURL;}
	
	/**
	* @return {Integer}	Width of the creative in pixels
	*/	
	width() {return this._width;}
	
	/**
	* @return {Integer}	Height of the creative in pixels
	*/	
	height() {return this._height;}
	
	/**
	* @return {Integer}	Display priority for the creative (Lower numbers displayed first)
	*/	
	priority() {return this._priority;}
	
}

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
* The CreativeGroup class stores a group of Creative objects tracks which are injected.
*/
class CreativeGroup {

	/**
	* Initializes the set to store the Creatives
	*/
	constructor() {

		//Creat the set to hold all the creatives
		this._creatives = new Set();

		//Create the map to mark all the creatives that were injected
		//and the locations they were injected. THe keys should be a 
		//subset of or equivalent to _creatives
		this._injectedCreatives = new Map();
	}

	/**
	* Adds a Creative object to the CreativeGroup
	*
	* Accepts a Creative object or an array of Creative objects
	*
	* @param {Mixed} 	newCreative  	Creative or array of Creatives to add to the CreativeGroup
	*/
	addCreative(newCreative) {

		//If a single creative was passed, just add it to the set
		if (newCreative instanceof Creative) {this._creatives.add(newCreative); return;}

		//Otherwise, verify we are working with an array
		if (Array.isArray(newCreative)) {

			//Add each Creative to the set
			for (let currentCreative of newCreative) {
				if (currentCreative instanceof Creative) {this._creatives.add(currentCreative);}
			}
		}
	}

	/**
	* Removes a Creative object from the CreativeGroup
	*
	* @param {Creative} 	creativeToRemove	Creative to remove from the CreativeGroup  	
	*/
	removeCreative(creativeToRemove) {
		if (creativeToRemove instanceof Creative) {this._creatives.delete(creativeToRemove);}
	}

	/**
	* Returns true if the instance contains a Creative with the passed dimensions
	* and false otherwise
	*
	* @param {Integer} 	width	Width of to check creatives against (must be greater than 0)  	
	* @param {Integer} 	height	Height of to check creatives against (must be greater than 0)  	
	* @return {Boolean}			True if a Creative of the dimensions exists, otherwise false
	*/
	hasCreativeWithDimensions(width, height) {
		
		//If any of the arguments are missing, throw an error
		if ((width == null) || (height == null)) {
				throw "CreativeGroup.hasCreativeWithDimensions: missing argument";
		}

		//Loop through the Creatives and return true if any match the passed width and height
		for (let currentCreative of this._creatives) {
			if ((currentCreative.width() == width) && (currentCreative.height() == height)) {
				return true;
			}
		}

		//If none matched, return false
		return false;
	}

	/**
	* Returns the Creative of the passed dimensions with the highest priority (lowest number)
	* that has not been marked as injected. Returns null if none exists.
	*
	* @param {Integer} 	width	Width of Creative (must be greater than 0)
	* @param {Integer} 	height	Height of Creative (must be greater than 0)  	
	* @return {Creative}		Uninjected Creative with highest priority of passed dimensions if exists or null
	*/
	getNextUninjectedCreative(width, height) {

		//If any of the arguments are missing, throw an error
		if ((width == null) || (height == null)) {
				throw "CreativeGroup.getNextUninjectedCreative: missing argument";
		}

		//Loop through the Creatives and store the matching Creative, if it exists
		let nextUninjectedCreative = null;
		for (let currentCreative of this._creatives) {

			//If the current creative has the passed width and height and is not injected
			if ((currentCreative.width() == width) && (currentCreative.height() == height) &&
				(!this._injectedCreatives.has(currentCreative))) {
				
				//If this is the first Creative found, store it
				if (nextUninjectedCreative == null) {nextUninjectedCreative = currentCreative;}

				//Else if it has a higher priority (lower number), replace the existing Creative
				else if (currentCreative.priority() < nextUninjectedCreative.priority()) {
					nextUninjectedCreative = currentCreative;
				}
			}
		}

		//Return the next uninjected Creative of highest priority or null if none found
		return nextUninjectedCreative;
	}


	/**
	* Flags the passed Creative as injected and stores its location on the page.
	*
	* If the Creative does not exist in the CreativeGroup instance, an error is thrown.
	*
	* If the Creative has already been maked as injected, an error is thrown.
	*
	* @param {Creative} 	injectedCreative	Creative to flag as injected
	* @param {Integer} 		xPosition			x position of the injected Creative's location on the page
	* @param {Integer} 		yPosition			y position of the injected Creative's location on the page
	*/
	injected(injectedCreative, xPosition, yPosition) {

		//Verify argument is a Creative
		if (!(injectedCreative instanceof Creative)) {throw "CreativeGroup.injected: argument must be of type Creative";}

		//Verify the Creative exists in the CreativeGroup
		if (!this._creatives.has(injectedCreative)) {throw "CreativeGroup.injected: Creative does not exist in CreativeGroup";}

		//Verify it has not already been marked as injected
		if (this._injectedCreatives.has(injectedCreative)) {throw "CreativeGroup.injected: Creative already marked as injected";}

		//If either of the position arguments are missing, throw an error
		if ((xPosition == null) || (yPosition == null)) {
				throw "CreativeGroup.injected: missing position argument";
		}

		//Creat the Coordinates object and add it all to the injected map
		let injectionCoordinates = new Coordinates(Math.round(xPosition), Math.round(yPosition));
		this._injectedCreatives.set(injectedCreative, injectionCoordinates);
	}

	/**
	* @return {Set}	Set of Creatives
	*/	
	getCreatives() {return this._creatives;}

	/**
	* @return {Map}	Map of Creatives that have been flagged as injected with their page Coordinates
	*/	
	getInjectedCreatives() {return this._injectedCreatives;}
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
		if (typeof googletag !== 'undefined') {

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


//---------------------------------------------------------------------------------------
//----------------------------------- CreativeInjecter Class ----------------------------------
//---------------------------------------------------------------------------------------
/**
* The CreativeInjecter replaces ad elements on the running page with the passed creative.
* It locates the ad elements first using any passed ad selectors and then using
* elements marked by the AdMarker extension. 
*
* Any non-AdSelector objects in the adSelectors array will be ignored.
*/
class CreativeInjecter {

/********************************Static Properties *************************************/
	//The Ad Marker sets an ad element's flood-opacity. This is the exact number.
	static get _ADMARKERFLOODOPACITY() {return '0.9898';}

	//Used by _getSmallestContainingParent. Any parent of a size small than
	//these minimums is ignored.
	static get _SMALLNODEMINWIDTH() {return 5;}		//in pixels
	static get _SMALLNODEMINHEIGHT() {return 5;}	//in pixels

	//Any ads and iframes larger than these dimensions should be removed
	static get _MAXIMUMADKEEPWIDTH() {return 971;}	//in pixels
	static get _MAXIMUMADKEEPHEIGHT() {return 274;}	//in pixels

	//Any (0,0) fixed elements larger than these dimensions should be removed
	//The first is very long and somewhat high, the second somewhat long and very high
	static get _MAXIMUMFIXEDKEEPWIDTH1() {return 900;}	//in pixels
	static get _MAXIMUMFIXEDKEEPHEIGHT1() {return 300;}	//in pixels
	static get _MAXIMUMFIXEDKEEPWIDTH2() {return 400;}	//in pixels
	static get _MAXIMUMFIXEDKEEPHEIGHT2() {return 700;}	//in pixels

	/**
	* Initializes the member properties of the instance. This is placed in its own
	* function at the top of the class code for clarity and to more closely resemble
	* standard object-oriented design.
	*/
	_initializeMemberProperties() {

	}

	/**
	* Initializes the CreativeInjecter with its creative and optional selectors.
	*
	* @param {CreativeGroup} 	creatives		Creatives to inject into the running page
	* @param {Array} 			adSelectors		Array of AdSelectors
	*/
	constructor(creatives, adSelectors) {

		//Verify creatives is a CreativeGroup
		if (!(creatives instanceof CreativeGroup)) {throw "CreativeInjecter.constructor: creatives must be of type CreativeGroup";}

		//Verify adSelectors is null or an array
		if ((adSelectors != null) && (!Array.isArray(adSelectors))) {
			throw "CreativeInjecter.constructor: adSelectors must be an array of AdSelector objects or null";
		}

		//Store the creatives
		this._creatives = creatives;

		//Store the AdSelectors
		this._adSelectors = [];
		if (adSelectors != null) {
			for (let currentAdSelector of adSelectors) {
				if (currentAdSelector instanceof AdSelector) {this._adSelectors.push(currentAdSelector);}
			}
		}

		//Setup the rest of the instance member properties. This is done in a separate function at the
		//top of the class code for clarity and to resemble more standard object-oriented design.
		this._initializeMemberProperties();
	}

	injectCreativesIntoPage() {

		//Sort the AdSelector elements by there positions. (Done here to prevent hiding large ads and overlays
		//from forcing positions of 0,0)
		this._sortAdSelectorsByPosition(this._adSelectors);

		//Begin my removing all large ads and overlays
		this._hideLargeAdsAndOverlays();

		//If no creatives were passed, simply exit at this point. This prevents non-replaced
		//ad selector elements from being hidden.
		if (this._creatives.getCreatives().size == 0) {return;}

		//--------------------- Ad Selector Elements -------------------------------
		//Sort the AdSelector elements by there positions
		//this._sortAdSelectorsByPosition(this._adSelectors);

		//Replace each AdSelector element with a matching creative of one of its
		//possible CreativeSizes, if a match exists
		//this._adSelectors = []; //testing
		for (let currentAdSelector of this._adSelectors) {

			//Keep track if whether or not this AdSelector is replaced with a Creative
			let adSelectorReplaced = false;

			for (let currentSize of currentAdSelector.sizes()) {

				//Only try a replace if nothing has been injected for the current AdSelector
				if (!adSelectorReplaced) {

					//If an uninjected Creative of that size exists, replace the element with it
					let creativeToInject = this._creatives.getNextUninjectedCreative(currentSize.width(), currentSize.height());
					if (creativeToInject) {

						//If the selector element exists, replace it with the creative
						let currentElement = document.querySelector(currentAdSelector.selector());
						if (currentElement) {

							// let parentElement = currentElement.parentElement;
							// parentElement.style.width = ElementInfo.width(currentElement) + "px";
							// parentElement.style.height = ElementInfo.height(currentElement) + "px";


							// let grandParentElement = parentElement.parentElement;
							// grandParentElement.style.width = ElementInfo.width(currentElement) + "px";
							// grandParentElement.style.height = ElementInfo.height(currentElement) + "px";

							this._replaceElementWithCreative(currentElement, creativeToInject)
							let elementXPosition = ElementInfo.xPosition(currentElement);
							let elementYPosition = ElementInfo.yPosition(currentElement);
							this._creatives.injected(creativeToInject, elementXPosition, elementYPosition);
							adSelectorReplaced = true;
						}
					}
				}
			}

			//If the element was not replaced, hide it if the AdSelector is flagged to hide if not replaced
			if (!adSelectorReplaced && currentAdSelector.hideIfNotReplaced()) {
				let currentElement = document.querySelector(currentAdSelector.selector());
				if (currentElement) {
					this._hideElement(currentElement);
				}
			}
		}

		// return; //testing

		//-------------------- Marked Creatives and IFrames ------------------------
		//Get the elements from the page that are the size of an instance creative, 
		//both marked by AdMarker and unmarked
		let elementsOfCreativeSizes = this._getPageElementsOfCreativeSizes();

		//For forgotten practical purposes from years of testing, we include
		//unmarked IFrames with the elements marked by the AdMarker
		let markedAdElements = Array.from(elementsOfCreativeSizes.get("markedAdElements"));
		let unmarkedIFrames = Array.from(elementsOfCreativeSizes.get("unmarkedIFrames"));
		markedAdElements = markedAdElements.concat(unmarkedIFrames);

		//Sort the set of marked ad elements by position and replace them
		this._sortElementsByPosition(markedAdElements);
		for (let currentElement of markedAdElements) {

			//Get the element size minus border width
			let elementWidth = ElementInfo.widthWithoutBorder(currentElement);
			let elementHeight = ElementInfo.heightWithoutBorder(currentElement);

			//If an uninjected Creative of that size exists, replace the element with it
			let creativeToInject = this._creatives.getNextUninjectedCreative(elementWidth, elementHeight);
			if (creativeToInject) {
				let elementXPosition = ElementInfo.xPosition(currentElement);
				let elementYPosition = ElementInfo.yPosition(currentElement);
				this._replaceElementWithCreative(currentElement, creativeToInject)
				this._creatives.injected(creativeToInject, elementXPosition, elementYPosition);
			}
		}

		//-------------------- Unmarked Creatives ------------------------

		//Finally, if none of the Creatives have been injected using the AdSelectors or 
		//elements marked by the AdMarker, replace any element on the page of a Creative size
		if (this._creatives.getInjectedCreatives().size == 0) {
			let unmarkedElements = Array.from(elementsOfCreativeSizes.get("unmarkedElements"));
			this._sortElementsByPosition(unmarkedElements);
			for (let currentElement of unmarkedElements) {

				//Get the element size minus border width
				let elementWidth = ElementInfo.widthWithoutBorder(currentElement);
				let elementHeight = ElementInfo.heightWithoutBorder(currentElement);

				//If an uninjected Creative of that size exists, replace the element with it
				let creativeToInject = this._creatives.getNextUninjectedCreative(elementWidth, elementHeight);
				if (creativeToInject) {
					this._replaceElementWithCreative(currentElement, creativeToInject)
					this._creatives.injected(creativeToInject);
				}
			}
		}
	}

	_replaceElementWithCreative(elementNode, replacementCreative) {

		//If the element is not an HTMLElement or does not have a parent node, simply exit
		if ((elementNode == null) || (!ElementInfo.isHTMLElement(elementNode)) ||
			(!elementNode.parentNode)) {return;}

		//Store the original height and width of the node
		let originalNodeWidth = ElementInfo.widthWithoutBorder(elementNode);
		let originalNodeHeight = ElementInfo.heightWithoutBorder(elementNode);

		// let creativeImage = document.createElement('img');
		// creativeImage.src = replacementCreative.imageURL();
		// creativeImage.style.width = replacementCreative.width() + 'px';
		// creativeImage.style.height = replacementCreative.height() + 'px';
		// elementNode.parentNode.replaceChild(creativeImage, elementNode);
		// return;

		//Create the replacement image
		let creativeImage = document.createElement('img');
		creativeImage.src = replacementCreative.imageURL();
		//creativeImage.style.maxWidth = 'none';
		// creativeImage.className = elementNode.className;
		// creativeImage.id = elementNode.id;
		creativeImage.style.width = replacementCreative.width() + 'px';
		creativeImage.style.height = replacementCreative.height() + 'px';
		creativeImage.style.maxWidth = replacementCreative.width() + 'px';
		creativeImage.style.maxHeight = replacementCreative.height() + 'px';
		creativeImage.style.margin = 'auto';

		while (elementNode.hasChildNodes()) {
            elementNode.removeChild(elementNode.lastChild);
        }

		elementNode.appendChild(creativeImage);


		//If the element node is an IFrame, replace it with another IFrame
		//if (elementNode.nodeName == "IFRAME") {

			// //Once the IFrame is loaded, add the image element
			// elementNode.onload = function()
			// {
			// 	//Set the body margin to zero because old HTML rules are dumb and append the image
			//     let iframeDocument = elementNode.contentDocument;
			//     iframeDocument.body.style.margin = "0px";
			//     iframeDocument.body.appendChild(creativeImage);
			// };

			// //Replace the element node with the new IFrame
			// elementNode.src = 'about:config';

			//Create the IFrame node
			// let creativeIFrame = document.createElement('iframe');
			// creativeIFrame.className = elementNode.className;
			// //creativeIFrame.id = elementNode.id;
			// creativeIFrame.style.border = '0px';
			// //creativeIFrame.style.cssText = document.defaultView.getComputedStyle(elementNode, "").cssText;
			// creativeIFrame.width = replacementCreative.width() + 'px';
			// creativeIFrame.height = replacementCreative.height() + 'px';
			// creativeIFrame.style.width = replacementCreative.width() + 'px';
			// creativeIFrame.style.height = replacementCreative.height() + 'px';
   //          creativeIFrame.style.overflow = "hidden";
   //          creativeIFrame.style.scrolling = "no";

			// //Once the IFrame is loaded, add the image element
			// creativeIFrame.onload = function()
			// {
			// 	//Set the body margin to zero because old HTML rules are dumb and append the image
			//     let iframeDocument = creativeIFrame.contentDocument;
			//     iframeDocument.body.style.margin = "0px";
			//     iframeDocument.body.appendChild(creativeImage);
			// };

			// //Replace the element node with the new IFrame
			// while (elementNode.hasChildNodes()) {
   //              elementNode.removeChild(elementNode.lastChild);
   //          }

			// elementNode.appendChild(creativeIFrame);
		//}


		//Make sure the parents are displayed and at least as big as the Creative image
		// this._crawlParentHTMLElements(creativeIFrame, function(currentNode) {
		this._crawlParentHTMLElements(creativeImage, function(currentNode) {

			//Get the current node's width and height minus border width
			let currentNodeWidth = ElementInfo.widthWithoutBorder(currentNode);
			let currentNodeHeight = ElementInfo.heightWithoutBorder(currentNode);
			// Log.output(currentNode.id + ": " + currentNodeWidth + "x" + currentNodeHeight);
			

			//Make sure the current node is displayed
			let displayStatus = document.defaultView.getComputedStyle(currentNode, null).getPropertyValue('display');
			if (displayStatus == "none") {
				currentNode.style.display = "block";
			}
			currentNode.style.visibility = "visible";

			//Some ads use CSS animations to come in to view once they are loaded such as 'fade in'
			//Force any animation to run
			currentNode.style.animationPlayState = "running";

			//If the node is the same size as the original ad element AND larger than the injected Creative,
			//set its width and height to the size of the injected Creative
			if ((currentNodeWidth == originalNodeWidth) && (currentNodeHeight == originalNodeHeight) &&
				((currentNodeWidth > replacementCreative.width()) || (currentNodeHeight > replacementCreative.height()))) {

				//currentNode.style.minWidth = replacementCreative.width() + 'px';	//Left out until necessary so as not to
																					//unintentionally cause bugs
				currentNode.style.minHeight = replacementCreative.height() + 'px';
				//currentNode.style.width = replacementCreative.width() + 'px';
				currentNode.style.height = replacementCreative.height() + 'px';
				// Log.output("Shrinking parent");
			}

			//If the current node is smaller than the Creative image, expand it
			//This occurs when the element has been hidden by the page.
			//For example, a containing div set to 0x0
			if (currentNodeWidth < replacementCreative.width()) {
				// let widthPadding = ElementInfo.paddingLeft(currentNode) + ElementInfo.paddingLeft(currentNode);
				// currentNode.style.width = (replacementCreative.width() + widthPadding) + 'px';
				currentNode.style.width = '100%';
				// Log.output("Expanding parent width");
			}
			if (currentNodeHeight < replacementCreative.height()) {
				// let heightPadding = ElementInfo.paddingTop(currentNode) + ElementInfo.paddingBottom(currentNode);
				// currentNode.style.height = (replacementCreative.height() + heightPadding) + 'px';
				currentNode.style.height = '100%';
				// Log.output("Expanding parent height");
			}
		});
	}

	/**
	* Hides any large ads marked by the AdMarker and any fixed elements either deemed too large
	* or fixed to a position other than (0,0).
	*/
	_hideLargeAdsAndOverlays() {

		//Crawl through the DOM and remove all large ads and fixed elements with matching criteria
		let thisCreativeInjecter = this; 	//For scope
		let creatives = this._creatives; 	//For scope
		this._crawlDocumentHTMLElements(document, function(currentNode) {

			//Get the node size minus border width
			let nodeWidth = ElementInfo.widthWithoutBorder(currentNode);
			let nodeHeight = ElementInfo.heightWithoutBorder(currentNode);

			//Get the current node's flood-opacity, position style, position, and z-index
			let nodeFloodOpacity = ElementInfo.floodOpacity(currentNode);
			let nodePositionStyle = ElementInfo.positionStyle(currentNode);
			let nodeXPosition = ElementInfo.xPosition(currentNode);
			let nodeYPosition = ElementInfo.yPosition(currentNode);
			let nodeZIndex = ElementInfo.zIndex(currentNode);

			/**************************** Remove Large Ads ****************************/
			//If the node has been :
			//	- Marked by the AdMarker as an ad element
			//	- Is not the size of an instance Creative
			//	- Is equal to or larger than the size set by _MAXIMUMADKEEPWIDTH and _MAXIMUMADKEEPHEIGHT
			//hide it.
			if ((nodeFloodOpacity == CreativeInjecter._ADMARKERFLOODOPACITY) &&
				(!creatives.hasCreativeWithDimensions(nodeWidth, nodeHeight))) {

				//Get the viewable width and height of the node minus the borders
				//Sometimes an ad element can have larger non-viewable dimensions than its parents
				let smallestParentNode = thisCreativeInjecter._getSmallestContainingParent(currentNode);
				let viewableWidth = ElementInfo.widthWithoutBorder(smallestParentNode);
				let viewableHeight = ElementInfo.heightWithoutBorder(smallestParentNode);

				//Remove the ad if it is bigger than the allowed 'keep' size
				if ((viewableWidth >= CreativeInjecter._MAXIMUMADKEEPWIDTH) && 
					(viewableHeight >= CreativeInjecter._MAXIMUMADKEEPHEIGHT)) {
					thisCreativeInjecter._hideElement(smallestParentNode);
				}
			}

			/*************************** Remove Fixed Elements ************************/
			//If the node is:
			//	- Fixed with a z-index greater than 1
			//	- Is not the size of an instance Creative
			//	- Is too large or fixed anywhere other than (0,0)
			//hide it
			// else if ((nodePositionStyle == 'fixed') && (nodeZIndex > 1) && 
			// 		(!creatives.hasCreativeWithDimensions(nodeWidth, nodeHeight))) {

			// 	//If the node is fixed anywhere other than the top left corner, hide it
			// 	// if ((nodeXPosition > 0) || (nodeYPosition > 0)) {thisCreativeInjecter._hideElement(currentNode);}

			// 	//Otherwise, if it is very large, hide it.
			// 	//The check on screen width is for mobile sites.
			// 	//These checks have been put together over time and should be reviewed.
			// 	let nodeScreenWidthPercentage = (nodeWidth/window.innerWidth);
			// 	if (((nodeWidth > CreativeInjecter._MAXIMUMFIXEDKEEPWIDTH1) && (nodeHeight > CreativeInjecter._MAXIMUMFIXEDKEEPHEIGHT1)) ||
			// 		((nodeWidth > CreativeInjecter._MAXIMUMFIXEDKEEPWIDTH2) && (nodeHeight > CreativeInjecter._MAXIMUMFIXEDKEEPHEIGHT2)) ||
			// 		(((nodeScreenWidthPercentage) > 0.96) && (nodeHeight > CreativeInjecter._MAXIMUMFIXEDKEEPHEIGHT2))) {
			// 		thisCreativeInjecter._hideElement(currentNode);
			// 	}
			// }
			else if ((nodeZIndex > 1) && (!creatives.hasCreativeWithDimensions(nodeWidth, nodeHeight))) {
				let nodeScreenWidthPercentage = (nodeWidth/window.innerWidth);
				let nodeScreenHeightPercentage = (nodeHeight/window.innerHeight);
				if ((nodeScreenWidthPercentage > 0.96) && (nodeScreenHeightPercentage > 0.96)) {
					thisCreativeInjecter._hideElement(currentNode);
				}
			}
		});
	}

	_getPageElementsOfCreativeSizes() {

		//Initialize the sets to store the discovered ad elements
		let markedAdElements = new Set();
		let unmarkedIFrameElements = new Set();
		let unmarkedElementsOfCreativeSizes = new Set();

		//Crawl through the DOM and store all Creative size matching elements
		let thisCreativeInjecter = this; 	//For scope
		let creatives = this._creatives; 	//For scope
		this._crawlDocumentHTMLElements(document, function(currentNode) {

			//Get the current node's size minus borders and flood opacity
			let nodeWidth = ElementInfo.widthWithoutBorder(currentNode);
			let nodeHeight = ElementInfo.heightWithoutBorder(currentNode);
			let nodeFloodOpacity = ElementInfo.floodOpacity(currentNode);

			//If the current node has been marked by the Ad Marker as an ad element and
			//it is the size of an instance Creative, store it
			if (nodeFloodOpacity == CreativeInjecter._ADMARKERFLOODOPACITY) {

				//Get the smallest parent node in case the marked element has
				//larger dimensions than is viewable
				let smallestParentNode = thisCreativeInjecter._getSmallestContainingParent(currentNode);
				let viewableWidth = ElementInfo.widthWithoutBorder(smallestParentNode);
				let viewableHeight = ElementInfo.heightWithoutBorder(smallestParentNode);

				//If the size of the node/smallest parent equals an instance Creative size, store it
				if (creatives.hasCreativeWithDimensions(viewableWidth, viewableHeight)) {
					markedAdElements.add(smallestParentNode);
				}
			}

			//Else if it is an IFrame and the size of an instance Creative, store it
			else if ((currentNode.nodeName == "IFRAME") && 
					 (creatives.hasCreativeWithDimensions(nodeWidth, nodeHeight))) {
					unmarkedIFrameElements.add(currentNode);
			}

			//Finally, if it is none of the above but the size of an instance Creative, store it
			else if (creatives.hasCreativeWithDimensions(nodeWidth, nodeHeight)) {
					unmarkedElementsOfCreativeSizes.add(currentNode);
			}
		});

		//Return the final sets
		let elementsOfCreativeSizes = new Map();
		elementsOfCreativeSizes.set("markedAdElements", markedAdElements);
		elementsOfCreativeSizes.set("unmarkedIFrames", unmarkedIFrameElements);
		elementsOfCreativeSizes.set("unmarkedElements", unmarkedElementsOfCreativeSizes);
		return elementsOfCreativeSizes;
	}

	/**
	* Hides the passed node and any parent nodes of the same or smaller size.
	*
	* Hiding consists of setting the display style to 'none'.
	*
	* @param {HTMLElement} 	elementNode  	Node to hide
	*/
	_hideElement(elementNode) {

		//Verify the passed node exists and is an HTMLElement. If not, simply return.
		if ((elementNode == null) || (!ElementInfo.isHTMLElement(elementNode))) {return;}

		//Get the smallest parent node of the element to hide. 
		//Sometimes an ad element can have larger non-viewable dimensions than its parents
		let smallestParentNode = this._getSmallestContainingParent(elementNode);

		//Hack for business insider
		// if ((ElementInfo.widthWithoutBorder(elementNode) > 1000) && 
		//     (ElementInfo.heightWithoutBorder(elementNode) > 1000)) {smallestParentNode = elementNode;}

		//Hide the final element (original or smallest highest parent)
		smallestParentNode.style.display = 'none';

		// return;

		// //Get the element size minus border width
		// let startingNodeWidth = ElementInfo.widthWithoutBorder(elementNode);
		// let startingNodeHeight = ElementInfo.heightWithoutBorder(elementNode);

		// //Hide the element
		// elementNode.style.display = 'none';

		// //Hide any parents of the same size
		// this._crawlParentHTMLElements(elementNode, function(currentNode) {

		// 	//Get the current node's width and height minus border width
		// 	let currentNodeWidth = ElementInfo.widthWithoutBorder(elementNode);
		// 	let currentNodeHeight = ElementInfo.heightWithoutBorder(elementNode);

		// 	//If the node is the same width and height as the starting node, hide it
		// 	if ((currentNodeWidth == startingNodeWidth) && (currentNodeHeight == startingNodeHeight)) {
		// 			currentNode.style.display = 'none';
		// 	}
		// });
	}

	/**
	*
	*	!!!!UNUSED!!!!
	*
	* Returns the highest containing frame of the passed node with the same size
	* of the node or null if none found.
	*
	* This function continues to crawl frame parents until no further frame is found or a
	* frame of a size different than the passed node is found.
	*
	* This is used to hide an ad element's entire IFrame. Hiding simply the ad can result in the
	* IFrame still taking up space on the page.
	*
	* @param {HTMLElement} 	elementNode		HTMLElement to check for an IFrame of the same size
	* @return {HTMLFrame}					Topmost containing frame the same size of the node or null if none found.
	*/
	_getHighestSameSizeContainingFrame(elementNode) {

		//Verify the passed node exists and is an HTMLElement. If not, simply return null.
		if ((elementNode == null) || (!ElementInfo.isHTMLElement(elementNode))) {return null;}

		//Get the element node's width and height
		let nodeWidth = ElementInfo.width(elementNode);
		let nodeHeight = ElementInfo.height(elementNode);

		//Crawl upwards through the containing frames. If a frame of the 
		//same size exists, store it or replace the previous stored one.
		let heighestContainingFrame = null;
		let currentContainingFrame = ElementInfo.getContainingFrame(elementNode);
		while (currentContainingFrame) {

			//Get the current frame's size
			let frameWidth = ElementInfo.width(currentContainingFrame);
			let frameHeight = ElementInfo.height(currentContainingFrame);

			if ((frameWidth == nodeWidth) && (frameHeight == nodeHeight)) {
				heighestContainingFrame = currentContainingFrame;
			}

			//Set the current containing frame to the present one's parent frame
			currentContainingFrame = ElementInfo.getContainingFrame(currentContainingFrame);
		}

		//Return the farthest frame of the same size or null if not found
		return heighestContainingFrame;
	}

	/**
	* Returns the smallest and highest containing parent holding the starting node.`
	*
	* This function is used in the situation where an ad might have rather large non-visible dimensions.
	* It's displayed size is then determined by the smallest containing node. 
	*
	* The returned node can be the same as the passed node.
	*
	* Nodes with a width and height less than _SMALLNODEMINWIDTH and _SMALLNODEMINHEIGHT, anchors,
	* or objects are not counted. The minimum height and width are required since sometimes containing
	* nodes register as 1x1 or similar and must be ignored.
	*
	* @param {HTMLElement} 		startingNode	Starting node of parents to crawl
	* @return HTMLElement 						Smallest containing parent
	*/
	_getSmallestContainingParent(startingNode) {

		//Verify the passed node exists and is an HTMLElement. If not, simply return the argument.
		if ((startingNode == null) || (!ElementInfo.isHTMLElement(startingNode))) {return startingNode;}

		//Get the element node's width and height minus the border
		let startingNodeWidth = ElementInfo.widthWithoutBorder(startingNode);
		let startingNodeHeight = ElementInfo.heightWithoutBorder(startingNode);

		//Set the smallest node to the starting node
		let smallestNode = startingNode;
		let smallestNodeWidth = startingNodeWidth;
		let smallestNodeHeight = startingNodeHeight;

		//If the node width is bigger than or equal to the viewport and
		//the node height is bigger than or equal to the viewport
		//simply return it as the smallest node without climbing the parents.
		//This usually refers to ad skins used for the background
		let viewportWidth = document.documentElement.clientWidth;
		let viewportHeight = document.documentElement.clientHeight;
		if ((startingNodeWidth >= viewportWidth) && (startingNodeHeight >= viewportHeight)) {
			return startingNode;
		}
		//Crawl the node's parents and find the smallest one
		//Ignore anchors, objects, and nodes smaller than defined by
		//_SMALLNODEMINWIDTH and _SMALLNODEMINHEIGHT
		this._crawlParentHTMLElements(startingNode, function(currentNode) {

			//Get the current node's width and height
			let currentNodeWidth = ElementInfo.widthWithoutBorder(currentNode);
			let currentNodeHeight = ElementInfo.heightWithoutBorder(currentNode);

			//If the node is smaller or the same size as the the current smallest, 
			//not an object, not an anchor, and above the minimum size requirements, 
			//store it as the smallest
			if ((currentNodeWidth > CreativeInjecter._SMALLNODEMINWIDTH) && (currentNodeHeight > CreativeInjecter._SMALLNODEMINHEIGHT) &&
				(currentNode.nodeName != "A") && (currentNode.nodeName != "OBJECT") &&
				(currentNodeWidth <= smallestNodeWidth) && (currentNodeHeight <= smallestNodeHeight)) {

					//Set this node as the smallest one
					smallestNode = currentNode;
					smallestNodeWidth = currentNodeWidth;
					smallestNodeHeight = currentNodeHeight;
			}
		});

		//Finally, return the smallest node (can be same as passed node)
		return smallestNode;
	}

	/**
	* Crawls the DOM document, including any iFrames, and calls the passed function on 
	* each HTMLElement node.
	*
	* The function receives a single argument of the HTMLElement node.
	*
	* @param {HTMLDocument} 	documentToCrawl		The DOM document to crawl
	* @param {Function}			nodeFunction		Function to call on each HTMLElement node. Receives HTMLElement node as argument.
	*/
	_crawlDocumentHTMLElements(documentToCrawl, nodeFunction) {

		//If the document does not exist, is not a HTMLDocument, or does not have a "body" property, exit the function
		if ((!documentToCrawl) || (!(document instanceof HTMLDocument)) || (!documentToCrawl.body)) {return;}

		//If the passed function is null or not a function, exit the function
		if ((!nodeFunction) || (!(nodeFunction instanceof Function))) {return;}

		//Get all of the HTMLElement nodes in the passed document
		let headHTEMLElementNodes = [].slice.call(documentToCrawl.head.getElementsByTagName("*"));
		let bodyHTMLElementNodes = [].slice.call(documentToCrawl.body.getElementsByTagName("*"));
		let allHTMLElementNodes = headHTEMLElementNodes.concat(bodyHTMLElementNodes);

		//For each node, apply the passed function it and crawl any iFrames
		for (let currentNode of allHTMLElementNodes) {

			//Apply the passed function on the node
			nodeFunction(currentNode);

			//If the node is an IFrame, crawl it as well. 
			//Try-Catch is used in case crawling it is not permitted by browser security.
			try {
				//If the node is an iframe, find the ads in it too
				if ((currentNode.nodeName == "IFRAME") && (currentNode.contentDocument)) {

					//Call the function on the new iframe
					this._crawlDocumentHTMLElements(currentNode.contentDocument, nodeFunction);
				}
			}
			catch(error) {} //Do nothing on error
		}
	}

	/**
	* Crawls the HTMLElement node's parents, including any frames, and calls the passed function on 
	* each HTMLElement node.
	*
	* The function receives a single argument of the HTMLElement node. The passed function is not 
	* applied to the starting node.
	*
	* @param {HTMLElement} 		startingNode		Starting node of parents to crawl
	* @param {Function}			nodeFunction		Function to call on each parent. Receives HTMLElement node as argument.
	*/
	_crawlParentHTMLElements(startingNode, nodeFunction) {

		//Verify the passed node exists and is an HTMLElement. If not, simply return null.
		if ((startingNode == null) || (!ElementInfo.isHTMLElement(startingNode))) {return null;}

		//If the passed function is null or not a function, exit the function
		if ((!nodeFunction) || (!(nodeFunction instanceof Function))) {return;}

		//Crawl through the node's parents
		let currentParentNode = startingNode.parentNode;
		while (currentParentNode) {

			//Apply the passed function on the node if it as HTMLElement
			if (ElementInfo.isHTMLElement(currentParentNode)) {nodeFunction(currentParentNode);}

			//If the current node has its own parent, set itself to the parent
			if (currentParentNode.parentNode) {currentParentNode = currentParentNode.parentNode;}

			//Otherwise, if it is a containing frame, make it the current parent node
			else {currentParentNode = ElementInfo.getContainingFrame(currentParentNode);}
		}
	}

	_sortElementsByPosition(elements) {

		//If the argument is not an array, do nothing
		if (!Array.isArray(elements)) {return;}

		//Function that sorts array of HTMLElements from top-right screen position to bottom-left
		//If either element is not an HTMLElement, an error is thrown.
		let elementSortFunction = function(firstElement, secondElement) {
		    
		    //If either element is not an HTMLElement, throw an error
			if ((!ElementInfo.isHTMLElement(firstElement)) || (!ElementInfo.isHTMLElement(secondElement))) {
				throw "CreativeInjecter._sortElementsByPosition: array element not an HTMLElement";
			}

		    //Calculate each elements position factor by adding its y position
		    //to its x position. The x position is divided by 1000 in order
		    //to decrease its importance compared to the y position factor.
		    let firstPositionFactor = ElementInfo.yPosition(firstElement) + ElementInfo.xPosition(firstElement)/1000;
		    let secondPositionFactor = ElementInfo.yPosition(secondElement) + ElementInfo.xPosition(secondElement)/1000;

			//Return the difference. If negative, the firstElement comes first, if positive, the second come first.    
		    return firstPositionFactor - secondPositionFactor;
		};

		//Sort the elements
		elements.sort(elementSortFunction);
	}


	_sortAdSelectorsByPosition(adSelectors) {

		//If the argument is not an array, do nothing
		if (!Array.isArray(adSelectors)) {return;}

		//Function that sorts array of AdSelectors from top-right screen position to bottom-left
		//If either argument is not an AdSelector, an error is thrown.
		let adSelectorSortFunction = function(firstAdSelector, secondAdSelector) {
		    
		    //If either element is not an HTMLElement, throw an error
			if ((!(firstAdSelector instanceof AdSelector)) || (!(firstAdSelector instanceof AdSelector))) {
				throw "CreativeInjecter._sortAdSelectorsByPosition: array element not an AdSelector";
			}

			//Get the elements
			let firstElement = document.querySelector(firstAdSelector.selector());
			let secondElement = document.querySelector(secondAdSelector.selector());

			//If the first element is null but not the second, return a positive 1
			//to make the second element come first
			if ((firstElement == null) && (secondElement != null)) {return 1;}

			//If the first element is not null but the second is, return a negative -1
			//to make the first element come first
			if ((firstElement != null) && (secondElement == null)) {return -1;}

			//If both elements are null, return a 0 for no order change
			if ((firstElement == null) && (secondElement == null)) {return 0;}

		    //Calculate each elements position factor by adding its y position
		    //to its x position. The x position is divided by 1000 in order
		    //to decrease its importance compared to the y position factor.
		    let firstPositionFactor = ElementInfo.yPosition(firstElement) + ElementInfo.xPosition(firstElement)/1000;
		    let secondPositionFactor = ElementInfo.yPosition(secondElement) + ElementInfo.xPosition(secondElement)/1000;

		    //!!!------------------------------------------------------------------------!!!
		    //On Desktop, some non-loaded fixed element GPT slots were registering a position of 0,0 even
		    //though they were placed lower on the page. For now, if we are on a Desktop and the selector
		    //has a position of 0,0 , sort it lower than the rest of the selectors.
			let browserViewportWidth = document.documentElement.clientWidth;
			if (browserViewportWidth > 450) {

				//If the first element's position is (0,0) but not the second, return a positive 1
				//to make the second element come first
				if ((firstPositionFactor == 0) && (secondPositionFactor != 0)) {return 1;}

				//If the first element's position is not (0,0) but the second is, return a negative -1
				//to make the first element come first
				if ((firstPositionFactor != 0) && (secondPositionFactor == 0)) {return -1;}
			}

			//Return the difference. If negative, the firstElement comes first, if positive, the second come first.    
		    return firstPositionFactor - secondPositionFactor;
		};

		//Sort the elements
		adSelectors.sort(adSelectorSortFunction);
	}
}

//window.onload = function() {

//Remove the scrollbars
// document.documentElement.style.overflow = 'hidden';

let creatives = [];

/*creatives = [
	{id: '28577acb-9fbe-4861-a0ef-9d1a7397b4c9', imageURL: 'https://s3.amazonaws.com/asr-images/fillers/nsfiller-994x250.jpg', priority: 0, width: 994, height: 250},
	{id: 'ab4ec323-f91b-4578-a6c8-f57e5fca5c87', imageURL: 'https://s3.amazonaws.com/asr-images/fillers/filler-300x250.jpg', priority: 0, width: 300, height: 250},
	{id: 'b4cce6c3-d68c-4cb4-b50c-6c567e0d3789', imageURL: 'https://s3.amazonaws.com/asr-images/fillers/nsfiller-970x250.jpg', priority: 0, width: 970, height: 250},
	{id: '312e383f-314e-4ba2-85f0-5f6937990fa6', imageURL: 'https://s3.amazonaws.com/asr-images/fillers/nsfiller-300x600.jpg', priority: 0, width: 300, height: 600}
];//*/
creatives = [{id: 'dd311801-a0af-4217-936b-b9d7124ac8be', imageURL: 'http://s3.amazonaws.com/asr-development/creativeimages/3a82a348-76f6-4d14-ae30-d17ed8949b9d.png', width: 300, height: 250, priority: 0},{id: '9b12e879-c9b4-465c-92f7-30f1916fe990', imageURL: 'http://s3.amazonaws.com/asr-development/creativeimages/375c6acf-275d-4074-a2bd-f3a638470736.png', width: 728, height: 90, priority: 0},{id: 'fa0e8046-4b6a-4ead-b915-69b4a02cccde', imageURL: 'http://s3.amazonaws.com/asr-development/creativeimages/9a8dbd1a-f3c3-451f-81d4-c66c9f99425c.png', width: 300, height: 600, priority: 0},];

//Create the CreativesGroup and add each passed Creative to it
let allCreatives = new CreativeGroup();
for (let currentCreative of creatives) {
	allCreatives.addCreative((new Creative(currentCreative.id, 
										   currentCreative.imageURL,
										   currentCreative.width,
										   currentCreative.height,
										   currentCreative.priority)));
}

let selectors = [];
// selectors = [
// 	{selector: "#selectorByID", sizes: [[300,600]], hideIfNotReplaced: true},
// 	{selector: "div.classOne.classTwo", sizes: [[728,90]], hideIfNotReplaced: true},
// 	{selector: "#outerDiv div.middleDiv div.innerDiv", sizes: [[300,250]], hideIfNotReplaced: true}
// ];


//INSERT ADSELECTORS OBJECT//

//Get any GPT AdSelectors
let gptAdSelectors = (new GPTSlots()).adSelectors();
let allSelectors = (gptAdSelectors.size > 0) ? Array.from(gptAdSelectors) : [];

//Verify each selector points to an element then turn it into an AdSelector
for (let currentSelector of selectors) {
	let selectorElement = document.querySelector(currentSelector.selector);
	if (selectorElement) {
		allSelectors.push(
			(new AdSelector(currentSelector.selector, currentSelector.hideIfNotReplaced)).addSizes(currentSelector.sizes)
		);
	}
}

allSelectors.push(new AdSelector("div.td-header-rec-wrap", true).addSize(728, 90));
allSelectors.push(new AdSelector("div#bsap_1282", true).addSize(300, 250));
allSelectors.push(new AdSelector("div#bsap_1280", true).addSize(300, 250));
if (document.documentElement.clientWidth <= 767) {

	setTimeout(function() { 
		if (document.querySelector(".td-header-rec-wrap img")) {

			document.querySelector(".td-header-rec-wrap img").style.width = "100%";
			document.querySelector(".td-header-rec-wrap img").style.height = "100%";
			document.querySelector(".td-header-rec-wrap img").style.margin = "10px auto";
		}
	}, 750);

}


//Initialize the CreativeInjecter and inject the creatives
let injecter = new CreativeInjecter(allCreatives, allSelectors);
injecter.injectCreativesIntoPage();

//Create the list of injected Creatives and their locations
//Format: {creativeID: {x: xPosition, y: yPosition}}
//Example: {"4ce6dca7-1d71-4e5b-9bfb-17a41190151a":{"x":312,"y":106},"1e678430-90b4-4c9f-ad81-739826d05c0e":{"x":942,"y":262}} 
let injectedCreatives = allCreatives.getInjectedCreatives();
let injectedIDsAndLocations = {};
for (let [injectedCreative, location] of injectedCreatives) {
	injectedIDsAndLocations[injectedCreative.id()] = {'x': location.x(), 'y': location.y()};
}

//Get the message log
Log.output("End of message log");
let messageLog = Log.getMessages();

//Return the injected creatives with their locations and any log messages
//Log.output(JSON.stringify(injectedIDsAndLocations));
// return JSON.stringify({'injectedCreatives': injectedIDsAndLocations, 'outputLog': {[messageLog]: {}}});

