/**
* The Creative class allows retrieval of the basic information for a creative such as ID, UUID, image URL, and processing status. 
*
* This is an immutable class. Its constructor takes a Creative Object JSON string (generated on the server) and uses it
* to set the private member variables.
*
* This class mirrors the Java and PHP Creative classes
* ANY CHANGES TO THIS CLASS SHOULD BE MADE IN THOSE CLASSES AS WELL.
*/
class Creative {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------
	//Status constants for Creative processing
	static get CREATED() {return "CREATED";}
	static get READY() {return "READY";}
	static get QUEUED() {return "QUEUED";}
	static get PROCESSING() {return "PROCESSING";}
	static get FINISHED() {return "FINISHED";}
	static get ERROR() {return "ERROR";}

	//Error constants
	static get URLNAVIGATION() {return "URLNAVIGATION";}
	static get SCREENSHOTCAPTURE() {return "SCREENSHOTCAPTURE";}
	static get SCREENSHOTCROP() {return "SCREENSHOTCROP";}
	static get IMAGEUPLOAD() {return "IMAGEUPLOAD";}

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	readonly _id: number;
	readonly _uuid: string;
	readonly _imageFilename: string;
	readonly _imageURL: string;
	readonly _width: number;
	readonly _height: number;
	readonly _priority: number;
	readonly _tagScript: string;
	readonly _tagPageFilename: string;
	readonly _tagPageURL: string;
	readonly _status: string;
	readonly _errorMessage: string;
	readonly _finalError: boolean;
	readonly _createdTimestamp: number;
	readonly _readyTimestamp: number;
	readonly _queuedTimestamp: number;
	readonly _processingTimestamp: number;
	readonly _finishedTimestamp: number;
	readonly _errorTimestamp: number;

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	* Initializes the Creative using the provided Creative Object JSON
	*
	* @param {String} 	creativeJSON  		JSON of Creative object
	*/
	constructor(creativeJSON: string) {

		//If the JSON is missing or not a string, throw an error
		if (creativeJSON == null) {throw "Creative constructor: missing argument";}
		if ((typeof creativeJSON !== 'string') || (creativeJSON == "")) {
			throw "Creative.constructor: argument must be a non-empty string";
		}

		//Convert the JSON to an object
		let baseCreative = JSON.parse(creativeJSON);

		//Verify the creative object
		if (baseCreative == null) {throw "Creative constructor: unable to parse JSON";}
		if ((typeof baseCreative.id === 'undefined') || (typeof baseCreative.uuid === 'undefined')) {
			throw "Creative.constructor: JSON object missing id or uuid";
		}

		//Store the creative properties
		this._id = baseCreative.id;
		this._uuid = baseCreative.uuid;
		this._imageFilename = baseCreative.imageFilename;
		this._imageURL = baseCreative.imageURL;
		this._width = baseCreative.width;
		this._height = baseCreative.height;
		this._priority = baseCreative.priority;
		this._tagScript = baseCreative.tagScript;
		this._tagPageFilename = baseCreative.tagPageFilename;
		this._tagPageURL = baseCreative.tagPageURL;
		this._status = baseCreative.status;
		this._errorMessage = baseCreative.errorMessage;
		this._finalError = baseCreative.finalError;
		this._createdTimestamp = baseCreative.createdTimestamp;
		this._readyTimestamp = baseCreative.readyTimestamp;
		this._queuedTimestamp = baseCreative.queuedTimestamp;
		this._processingTimestamp = baseCreative.processingTimestamp;
		this._finishedTimestamp = baseCreative.finishedTimestamp;
		this._errorTimestamp = baseCreative.errorTimestamp;
	}

	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* @return {Integer}	Creative ID
	*/	
	id() {return this._id;}

	/**
	* @return {String}	Creative UUID
	*/	
	uuid() {return this._uuid;}

	/**
	* @return {String}	Filename of the creative image
	*/	
	imageFilename() {return this._imageFilename;}
	
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

	/**
	* @return {String}	Tag script used to generate the Creative image
	*/	
	tagScript() {return this._tagScript;}
	
	/**
	* @return {String}	Filename of the tag page used to generate the Creative image
	*/	
	tagPageFilename() {return this._tagPageFilename;}
	
	/**
	* @return {String}	URL of the Creative tag page
	*/	
	tagPageURL() {return this._tagPageURL;}
	
	/**
	* @return {String}	Current processing status of the Creative. (Options class constants: CREATED, QUEUED, PROCESSING, FINISHED, ERROR)
	*/	
	status() {return this._status;}
	
	/**
	* @return {String}	Error message if an error occurred while processing the Creative
	*/	
	errorMessage() {return this._errorMessage;}
	
	/**
	* @return {String}	TRUE if an error occurred during processing and no more attempts will be made, FALSE otherwise
	*/	
	finalError() {return this._finalError;}
	
	/**
	* @return {Integer}	UNIX Timestamp the Creative was inserted into the database
	*/	
	createdTimestamp() {return this._createdTimestamp;}
	
	/**
	* @return {Integer}	UNIX Timestamp the Creative status was set to READY
	*/	
	readyTimestamp() {return this._readyTimestamp;}
	
	/**
	* @return {Integer}	UNIX Timestamp the Creative status was set to QUEUED
	*/	
	queuedTimestamp() {return this._queuedTimestamp;}
	
	/**
	* @return {Integer}	UNIX Timestamp the Creative status was set to PROCESSING
	*/	
	processingTimestamp() {return this._processingTimestamp;}
	
	/**
	* @return {Integer}	UNIX Timestamp the Creative status was set to FINISHED
	*/	
	finishedTimestamp() {return this._finishedTimestamp;}
	
	/**
	* @return {Integer}	UNIX timestamp the Creative status was set to ERROR when an error occurred
	*/	
	errorTimestamp() {return this._errorTimestamp;}
}

