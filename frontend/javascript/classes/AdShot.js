"use strict";
/**
* The AdShot class allows retrieval of the basic information for an AdShot such as ID, UUID, image URL, and processing status.
*
* This is an immutable class. Its constructor takes an AdShot Object JSON string (generated on the server) and uses it
* to set the private member variables.
*
* This class mirrors the Java and PHP Creative classes
* ANY CHANGES TO THIS CLASS SHOULD BE MADE IN THOSE CLASSES AS WELL.
*/
class AdShot {
    //---------------------------------------------------------------------------------------
    //---------------------------------- Constants ------------------------------------------
    //---------------------------------------------------------------------------------------
    //Status constants for Campaign processing
    static get CREATED() { return 'CREATED'; }
    static get PROCESSING() { return 'PROCESSING'; }
    static get FINISHED() { return 'FINISHED'; }
    static get ERROR() { return 'ERROR'; }
    //Error constants
    static get URLNAVIGATION() { return "URLNAVIGATION"; }
    static get INJECTERCREATION() { return "INJECTERCREATION"; }
    static get INJECTEREXECUTION() { return "INJECTEREXECUTION"; }
    static get SCREENSHOTCAPTURE() { return "SCREENSHOTCAPTURE"; }
    static get SCREENSHOTCROP() { return "SCREENSHOTCROP"; }
    static get IMAGEUPLOAD() { return "IMAGEUPLOAD"; }
    static get CREATIVENOTINJECTED() { return "CREATIVENOTINJECTED"; }
    //---------------------------------------------------------------------------------------
    //------------------------ Constructors/Copiers/Destructors -----------------------------
    //---------------------------------------------------------------------------------------
    /**
    * Initializes the Creative using the provided Creative Object JSON
    *
    * @param {String} 	creativeJSON  		JSON of Creative object
    */
    constructor(adShotJSON) {
        //If the JSON is missing or not a string, throw an error
        if (adShotJSON == null) {
            throw "AdShot constructor: missing argument";
        }
        if ((typeof adShotJSON !== 'string') || (adShotJSON == "")) {
            throw "AdShot.constructor: argument must be a non-empty string";
        }
        //Convert the JSON to an object
        let baseAdShot = JSON.parse(adShotJSON);
        //Verify the AdShot object
        if (baseAdShot == null) {
            throw "AdShot constructor: unable to parse JSON";
        }
        if ((typeof baseAdShot.id === 'undefined') || (typeof baseAdShot.uuid === 'undefined')) {
            throw "AdShot.constructor: JSON object missing id or uuid";
        }
        //Store the AdShot properties
        this._id = baseAdShot.id;
        this._uuid = baseAdShot.uuid;
        this._campaignID = baseAdShot.campaignID;
        this._requestedURL = baseAdShot.requestedURL;
        this._storyFinder = (baseAdShot.storyFinder == 1);
        this._mobile = (baseAdShot.mobile == 1);
        this._belowTheFold = (baseAdShot.belowTheFold == 1);
        this._finalURL = baseAdShot.finalURL;
        this._pageTitle = baseAdShot.pageTitle;
        this._imageFilename = baseAdShot.imageFilename;
        this._imageURL = baseAdShot.imageURL;
        this._width = baseAdShot.width;
        this._height = baseAdShot.height;
        this._status = baseAdShot.status;
        this._errorMessage = baseAdShot.errorMessage;
        this._createdTimestamp = baseAdShot.createdTimestamp;
        this._processingTimestamp = baseAdShot.processingTimestamp;
        this._finishedTimestamp = baseAdShot.finishedTimestamp;
        this._errorTimestamp = baseAdShot.errorTimestamp;
        //Create the Creatives
        this._creatives = new Set();
        for (let currentCreativeJSON of baseAdShot.creatives) {
            let newCreative = new Creative(currentCreativeJSON);
            if (newCreative) {
                this._creatives.add(newCreative);
            }
        }
        //Build the injected Creatives from the Creatives set
        this._injectedCreatives = new Set();
        for (let injectedCreativeID of baseAdShot.injectedCreatives) {
            //Find the injected Creative (if it exists) in the Creatives set
            for (let currentCreative of this._creatives) {
                if (currentCreative.id() == injectedCreativeID) {
                    this._injectedCreatives.add(currentCreative);
                }
            }
        }
    }
    //---------------------------------------------------------------------------------------
    //----------------------------------- Accessors -----------------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Accessors ************************************
    /**
    * @return {Integer}	AdShot ID
    */
    id() { return this._id; }
    /**
    * @return {String}	AdShot UUID
    */
    uuid() { return this._uuid; }
    /**
    * @return {Integer}	ID of the campaign the AdShot is associated with
    */
    campaignID() { return this._campaignID; }
    /**
    * @return {String}	Requested URL of the screenshot. (This is the URL before the StoryFinder is ran if so flagged)
    */
    requestedURL() { return this._requestedURL; }
    /**
    * @return {boolean}	Flag for whether or not the StoryFinder should be used. (TRUE to use the StoryFinder)
    */
    storyFinder() { return this._storyFinder; }
    /**
    * @return {Boolean}	Flag for whether or not the AdShot is for mobile (TRUE to use mobile)
    */
    mobile() { return this._mobile; }
    /**
    * @return {Boolean}	Flag for whether or not the AdShot should be taken below the fold (TRUE to take it below the fold)
    */
    belowTheFold() { return this._belowTheFold; }
    /**
    * @return {Set}		Creatives associated with the AdShot
    */
    creatives() { return this._creatives; }
    /**
    * @return {String}	Final URL of the AdShot
    */
    finalURL() { return this._finalURL; }
    /**
    * @return {String}	Page title of the AdShot's final URL
    */
    pageTitle() { return this._pageTitle; }
    /**
    * @return {Set}		Creatives injected into the final AdShot image (This will always be the set or a subset of the associated Creatives)
    */
    injectedCreatives() { return this._injectedCreatives; }
    /**
    * @return {String}	Filename of the AdShot image
    */
    imageFilename() { return this._imageFilename; }
    /**
    * @return {String}	URL of the AdShot image
    */
    imageURL() { return this._imageURL; }
    /**
    * @return {Integer}	Width of the AdShot image in pixels
    */
    width() { return this._width; }
    /**
    * @return {Integer}	Height of the AdShot image in pixels
    */
    height() { return this._height; }
    /**
    * @return {String}	Current processing status of the AdShot. (Options class constants: CREATED, PROCESSING, FINISHED, ERROR)
    */
    status() { return this._status; }
    /**
    * @return {String}	Error message if an error occurred while processing the AdShot
    */
    errorMessage() { return this._errorMessage; }
    /**
    * @return {Integer}	UNIX Timestamp the AdShot was inserted into the database
    */
    createdTimestamp() { return this._createdTimestamp; }
    /**
    * @return {Integer}	UNIX Timestamp the AdShot status was set to PROCESSING
    */
    processingTimestamp() { return this._processingTimestamp; }
    /**
    * @return {Integer}	UNIX Timestamp the AdShot status was set to FINISHED
    */
    finishedTimestamp() { return this._finishedTimestamp; }
    /**
    * @return {Integer}	UNIX timestamp the AdShot status was set to ERROR when an error occurred
    */
    errorTimestamp() { return this._errorTimestamp; }
}
