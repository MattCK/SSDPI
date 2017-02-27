/**
* ---------------------------------------------------------------------------------------
* ----------------------------------- AdInjecter ----------------------------------------
* ---------------------------------------------------------------------------------------
*	
* This AdInjecter script inserts creative images in the running page while removing
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
	* @param {Number} 	width  		Width of the creative in pixels
	* @param {Number} 	height  	Height of the creative in pixels
	* @param {Number} 	priority  	Display priority for the creative (Lower numbers displayed first)
	*/
	constructor(id, imageURL, width, height, priority) {

		//Verify the passed arguments
		if ((typeof id !== 'string') || (id == "") ||				//ID must be a non-empty string
			(typeof imageURL !== 'string') || (imageURL == "") ||	//URL must be a non-empty string
			(!Number.isInteger(width)) || (width <= 0) ||			//Width must be a 
			(!Number.isInteger(height)) || (height <= 0) ||
			(!Number.isInteger(priority))) {
				throw "Creative: invalid constructor arguments";
		}

		// if ((typeof id !== 'string') || (id == "") ||				//ID must be a non-empty string
		// 	(typeof imageURL !== 'string') || (imageURL == "") ||	//URL must be a non-empty string
		// 	(Number.isInteger(width)) || (width <= 0) ||			//Width must be a 
		// 	(Number.isInteger(height)) || (height <= 0) ||
		// 	(Number.isInteger(priority))) {
		// 		throw "Creative: invalid constructor arguments";
		// }

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
	* @return {Number}	Width of the creative in pixels
	*/	
	width() {return this._width;}
	
	/**
	* @return {Number}	Height of the creative in pixels
	*/	
	height() {return this._height;}
	
	/**
	* @return {Number}	Display priority for the creative (Lower numbers displayed first)
	*/	
	priority() {return this._priority;}
	
}

/**
* The Creative list class stores a group of Creative objects and performs operations on them.
*/
class CreativeList {

	/**
	* Initializes the set to store the Creatives
	*/
	constructor() {
		this._creatives = new Set();
	}

	/**
	* Adds a Creative object to the CreativeList
	*
	* Accepts a Creative object or an array of Creative objects
	*
	* @param {Mixed} 	newCreative  	Creative or array of Creatives to add to the CreativeList
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
	* Removes a Creative object from the CreativeList
	*
	* @param {Creative} 	creativeToRemove	Creative to remove from the CreativeList  	
	*/
	removeCreative(creativeToRemove) {
		if (creativeToRemove instanceof Creative) {this._creatives.delete(creativeToRemove);}
	}

	/**
	* @return {Number}	Set of Creatives
	*/	
	getCreatives() {return this._creatives;}
}

let firstCreative = new Creative("abc", "http://url1", "301", 251, 1);
// console.log("Created first creative");
// console.log("ID: " + firstCreative.id());
// console.log("Image URL: " + firstCreative.imageURL());
// console.log("Width: " + firstCreative.width());
// console.log("Height: " + firstCreative.height());
// console.log("Priority: " + firstCreative.priority());

// let secondCreative = new Creative(2, "http://url2", 302, 252, 2);
// let thirdCreative = new Creative(3, "http://url3", 303, 253, 3);
// let fourthCreative = new Creative(4, "http://url4", 304, 254, 4);

// let firstCreativeList = new CreativeList();

// firstCreativeList.addCreative(firstCreative);
// console.log("Added single creative");
// for (let currentCreative of firstCreativeList.getCreatives()) {console.log(currentCreative.id());}

// firstCreativeList.addCreative([secondCreative, thirdCreative, fourthCreative]);
// console.log("Added array of creatives");
// for (let currentCreative of firstCreativeList.getCreatives()) {console.log(currentCreative.id());}

// firstCreativeList.removeCreative(thirdCreative);
// console.log("Removed array of creatives");
// for (let currentCreative of firstCreativeList.getCreatives()) {console.log(currentCreative.id());}



