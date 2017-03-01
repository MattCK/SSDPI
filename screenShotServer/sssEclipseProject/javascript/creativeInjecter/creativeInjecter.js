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
		this._creatives = new Set();
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
	* Returns true if the instance contains a creative with the passed dimensions
	* and false otherwise
	*
	* @param {Integer} 	width	Width of to check creatives against  	
	* @param {Integer} 	height	Height of to check creatives against  	
	*/
	hasCreativeWithDimensions(width, height) {
		
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
	* @return {Set}	Set of Creatives
	*/	
	getCreatives() {return this._creatives;}
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
	* @param {String} 	selector	Selector to the ad element
	*/
	constructor(selector) {

		//Verify the selector is a non-empty string
		if ((selector == null) ||(typeof selector !== 'string') || (selector == "")) {
			throw "AdSelector.constructor: selector must be a non-empty string";
		}

		//Store the selector and initialize the sizes array
		this._selector = selector;
		this._sizes = new Set();
	}

	/**
	* Adds the passed width and height as a possible creative size for the ad element
	*
	* @param {Integer} 	width  		Width of the creative in pixels (must be greater than 0)
	* @param {Integer} 	height  	Height of the creative in pixels (must be greater than 0)
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
	}

	/**
	* @return {String}	Selector
	*/	
	selector() {return this._selector;}

	/**
	* @return {String}	Possible creative sizes associated with the AdSelector. (Array of CreativeSize objects)
	*/	
	sizes() {return this._sizes;}
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
	* Returns the x,y coorindates of the passed node in relation to the screen view.
	*
	* @param {HTMLElement}	elementNode		Node used to find smallest containing node
	* @return {Coordinates}  				Coordinates object with x,y set to node's screen position
	*/
	static getScreenCoordinates(elementNode) {

		//Return null if the passed node variable is an HTMLElement
		if (!ElementInfo.isHTMLElement(elementNode)) {return null;}

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
		this._adSelectors = new Set();
		if (adSelectors != null) {
			for (let currentAdSelector of adSelectors) {
				if (currentAdSelector instanceof AdSelector) {this._adSelectors.add(currentAdSelector);}
			}
		}

		//Setup the rest of the instance member properties. This is done in a separate function at the
		//top of the class code for clarity and to resemble more standard object-oriented design.
		this._initializeMemberProperties();
	}

	injectCreativeIntoPage() {

		//Begin my removing all large ads and overlays
		this._hideLargeAdsAndOverlays();

		//Get the elements from the page that are the size of an instance creative, 
		//both marked by AdMarker and unmarked
		let elementsOfCreativeSizes = this._getPageElementsOfCreativeSizes();

		//Sort the set of marked elements and replace them
		let markedAdElements = Array.from(elementsOfCreativeSizes.get("markedAdElements"));
		this._sortElementsByPosition(markedAdElements);
		for (let currentElement of markedAdElements) {

		}

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

				//Remove the had if it is bigger than the allowed 'keep' size
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
			else if ((nodePositionStyle == 'fixed') && (nodeZIndex > 1) && 
					(!creatives.hasCreativeWithDimensions(nodeWidth, nodeHeight))) {

				//If the node is fixed anywhere other than the top left corner, hide it
				if ((nodeXPosition > 0) || (nodeYPosition > 0)) {thisCreativeInjecter._hideElement(currentNode);}

				//Otherwise, if it is very large, hide it.
				//The check on screen width is for mobile sites.
				//These checks have been put together over time and should be reviewed.
				let nodeScreenWidthPercentage = (nodeWidth/window.innerWidth);
				if (((nodeWidth > CreativeInjecter._MAXIMUMFIXEDKEEPWIDTH1) && (nodeHeight > CreativeInjecter._MAXIMUMFIXEDKEEPHEIGHT1)) ||
					((nodeWidth > CreativeInjecter._MAXIMUMFIXEDKEEPWIDTH2) && (nodeHeight > CreativeInjecter._MAXIMUMFIXEDKEEPHEIGHT2)) ||
					(((nodeScreenWidthPercentage) > 0.96) && (nodeHeight > CreativeInjecter._MAXIMUMFIXEDKEEPHEIGHT2))) {
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
					markedAdElements.add(currentNode);
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
}









window.onload = function() {

let firstCreative = new Creative("1", "http://myurl.com", 301, 251, 1);
// console.log("Created first creative");
// console.log("ID: " + firstCreative.id());
// console.log("Image URL: " + firstCreative.imageURL());
// console.log("Width: " + firstCreative.width());
// console.log("Height: " + firstCreative.height());
// console.log("Priority: " + firstCreative.priority());

let secondCreative = new Creative("2", "http://url2", 302, 252, 2);
let thirdCreative = new Creative("3", "http://url3", 303, 253, 3);
let fourthCreative = new Creative("4", "http://url4", 304, 254, 4);

let firstCreativeGroup = new CreativeGroup();

firstCreativeGroup.addCreative(firstCreative);
// console.log("Added single creative");
// for (let currentCreative of firstCreativeGroup.getCreatives()) {console.log(currentCreative.id());}

firstCreativeGroup.addCreative([secondCreative, thirdCreative, fourthCreative]);
// console.log("Added array of creatives");
// for (let currentCreative of firstCreativeGroup.getCreatives()) {console.log(currentCreative.id());}

// console.log(firstCreativeGroup.hasCreativeWithDimensions(303, 253));
// console.log(firstCreativeGroup.hasCreativeWithDimensions(444, 444));

firstCreativeGroup.removeCreative(thirdCreative);
// console.log("Removed creative");
// for (let currentCreative of firstCreativeGroup.getCreatives()) {console.log(currentCreative.id());}


let firstSelector = new AdSelector("test");
firstSelector.addSize(3, 3);
firstSelector.addSizes([[4,4],[5,5]]);
let secondSelector = new AdSelector("test2");
secondSelector.addSizes([[66,66],[77,77]]);

//console.log(theCreativeInjecter);
// console.log("Top-level Div:");
// let testInfoDiv = document.getElementById("testElement");
// console.log("Width: " + ElementInfo.width(testInfoDiv));
// console.log("Height: " + ElementInfo.height(testInfoDiv));
// console.log("x: " + ElementInfo.xPosition(testInfoDiv));
// console.log("y: " + ElementInfo.yPosition(testInfoDiv));
// console.log("flood-opacity: " + ElementInfo.floodOpacity(testInfoDiv));
// console.log("Margin top: " + ElementInfo.marginTop(testInfoDiv));
// console.log("Margin right: " + ElementInfo.marginRight(testInfoDiv));
// console.log("Margin bottom: " + ElementInfo.marginBottom(testInfoDiv));
// console.log("Margin left: " + ElementInfo.marginLeft(testInfoDiv));
// console.log("Position Style: " + ElementInfo.positionStyle(testInfoDiv));
// console.log("z-index: " + ElementInfo.zIndex(testInfoDiv));

// console.log("Iframe div:");
// let iframeDiv = document.getElementById("testIframe").contentDocument.getElementById("iframeTestElement");
// let iframeDivInfo = new ElementInfo(iframeDiv);
// console.log("Width: " + iframeDivInfo.width());
// console.log("Height: " + iframeDivInfo.height());
// console.log("x: " + iframeDivInfo.xPosition());
// console.log("y: " + iframeDivInfo.yPosition());
// console.log("flood-opacity: " + iframeDivInfo.floodOpacity());
// console.log("Margin top: " + iframeDivInfo.marginTop());
// console.log("Margin right: " + iframeDivInfo.marginRight());
// console.log("Margin bottom: " + iframeDivInfo.marginBottom());
// console.log("Margin left: " + iframeDivInfo.marginLeft());
// console.log("Position Style: " + iframeDivInfo.positionStyle());
// console.log("z-index: " + iframeDivInfo.zIndex());
// let theCreativeInjecter = new CreativeInjecter(firstCreativeGroup, [firstSelector, secondSelector]);
// theCreativeInjecter._crawlDocumentHTMLElements(document, function(currentNode) {
// 	currentNode.style.border = "thick solid #FF0000";
// });

// let iframeTestDiv = document.getElementById("testIFrame").contentDocument.getElementById("iframeTestElement");
// let iframeSameSizeDiv = document.getElementById("sameSizeIFrame").contentDocument.getElementById("sameSizeTestDiv");

// console.log(theCreativeInjecter._getHighestSameSizeContainingFrame(iframeTestDiv));
// console.log(theCreativeInjecter._getHighestSameSizeContainingFrame(iframeSameSizeDiv));
// theCreativeInjecter._crawlParentHTMLElements(iframeTestDiv, function(currentNode) {
// 	console.log("Inside function node: " + currentNode.tagName);
// 	if (currentNode.style) {
// 		currentNode.style.border = "thick solid #00FF00";
// 	}
// });
// console.log(theCreativeInjecter._getSmallestContainingParent(iframeTestDiv));

//let adHideElement = document.getElementById("adElementToHide");
//theCreativeInjecter._hideLargeAdsAndOverlays();
let creative300x250 = new Creative("300x250", "http://url2", 300, 250, 1);
let creative728x90 = new Creative("728x90", "http://url2", 728, 90, 1);
let creative320x50 = new Creative("320x50", "http://url2", 320, 50, 1);
let creative300x50 = new Creative("300x50", "http://url2", 300, 50, 1);

let testCreativeGroup = new CreativeGroup();
testCreativeGroup.addCreative([creative300x250, creative728x90, creative320x50, creative300x50]);
let adSizes = "";
for (let currentCreative of testCreativeGroup.getCreatives()) {
	adSizes += currentCreative.id() + ", ";
}
console.log("Ad Sizes: " + adSizes);

let theCreativeInjecter = new CreativeInjecter(testCreativeGroup, []);
let foundElements = theCreativeInjecter._getPageElementsOfCreativeSizes();
for (let [setName, elementSet] of foundElements) {
	console.log("Current set: " + setName);
	for (let currentElement of elementSet) {
		console.log("--- " + currentElement.id);
	}
}
console.log("");

let markedElements = Array.from(foundElements.get("markedAdElements")).reverse();
console.log("Marked element positions before sort: ");
for (let element of markedElements) {
	console.log("--- " + element.id + " (" + ElementInfo.xPosition(element) + ", " + ElementInfo.yPosition(element) + ")");
}

theCreativeInjecter._sortElementsByPosition(markedElements);
console.log("Marked element positions after sort: ");
for (let element of markedElements) {
	console.log("--- " + element.id + " (" + ElementInfo.xPosition(element) + ", " + ElementInfo.yPosition(element) + ")");
}

};