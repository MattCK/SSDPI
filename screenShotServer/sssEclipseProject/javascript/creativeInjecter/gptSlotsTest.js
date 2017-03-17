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

		//Create the class constants
		this._TARGETWIDTH = 8888;
		this._TARGETHEIGHT = 9999;

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
			this._widthProperty = propertyInfo.widthKey;
			this._heightProperty = propertyInfo.heightKey;

			//Get the slot sizes property for a slot created with a SizeMapping object
			let sizeMappingSlot = this._getDummySlotWithMapping(this._TARGETWIDTH, this._TARGETHEIGHT);
			let mappingPropertyInfo = this._getSizesProperties(sizeMappingSlot, this._TARGETWIDTH, this._TARGETHEIGHT);
			this._mappingPropertyKey = mappingPropertyInfo.containingPropertyKey;

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
	* @return {Set}	Set of AdSelectors for all of the Google.Slots
	*/	
	adSelectors() {

		//Loop through the slots and create an accessor for each one
		let slotAdSelectors = new Set();
		for (let [currentSlot, currentCreativeSizes] of this._slotCreativeSizes) {

			//Create the selector string. If any forward slashes exist, put backslashes before them
			let slotSelector = "#" + currentSlot.getSlotElementId();
			slotSelector = slotSelector.replace("/", "\\/");

			//Create the AdSelector and add each CreativeSize width and height to it
			let currentAdSelector = new AdSelector(slotSelector);
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
		let primaryCreativeSizes = this._getCreativeSizesInProperty(slot[this._sizesPropertyKey], 
																	this._sizesClass, 
																	this._widthProperty, 
																	this._heightProperty);

		//Get any creative sizes in the size mapping Slot property (created by passing SizeMapping after constructor)
		let mappingCreativeSizes = this._getCreativeSizesInProperty(slot[this._mappingPropertyKey], 
																	this._sizesClass, 
																	this._widthProperty, 
																	this._heightProperty);

		//Remove any CreativeSize duplicates
		//As of writing this, javascript allows overriding all operands except ==, thus requiring a loop
		let allCreativeSizes = new Set([...primaryCreativeSizes, ...mappingCreativeSizes]);
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
	* Returns a set of CreativeSizes for each possible size in the passed object
	*
	* @param {googletag.Slot}	slotObject		Slot property object to find sizes in
	* @param {googletag.Slot}	sizesClass		Name of the class that holds the creative's possible size
	* @param {googletag.Slot}	widthKey		Key in sizes class for the width value
	* @param {googletag.Slot}	heightKey		Key in sizes class for the height value
	* @return {Set} 		 					Set of CreativeSizes for all the sizes found in the passed object
	*/
	_getCreativeSizesInProperty(slotObject, sizesClass, widthKey, heightKey) {

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
								if (value.constructor.name == sizesClass) {
									let currentCreativeSize = new CreativeSize(value[widthKey], value[heightKey]);
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
		findSizes(slotObject);

		//Return the found sizes
		return sizes;
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
	_getDummySlotWithMapping(targetWidth, targetHeight) {

		//Create a dummy slot with no sizes
		let dummySlot = googletag.defineSlot('/dummyWithMappingPath' + Date.now(), [], 'dummmyWithMappingElementID' + Date.now());

		//Create and add a size mapping to the slot using the size arguments and return it
		let mapping = googletag.sizeMapping().
		    addSize([100, 100], [targetWidth, targetHeight]).build();
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
	console.log(document.querySelector(currentAdSelector.selector()));
}