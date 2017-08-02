"use strict";
/**
* The AdShotBuilder class is used to compile the necessary information to create an AdShot. Specifically,
* the requested URL, whether to use the StoryFinder, whether to use mobile, whether to use below-the-fold,
* and which Creative to inject.
*
* When finished, this class creates a JSON representation of itself that can be used by the server PHP
* Campaign class to create the new AdShot.
*/
class AdShotBuilder {
    //---------------------------------------------------------------------------------------
    //------------------------ Constructors/Copiers/Destructors -----------------------------
    //---------------------------------------------------------------------------------------
    /**
    * Initializes the AdShotBuilder with its requested URL and StoryFinder/mobile/below-the-fold flags.
    *
    * @param {String} 	requestedURL  		Requested URL of the screenshot. (This is the URL before the StoryFinder is ran if so flagged)
    * @param {String} 	storyFinder  		Flag for whether or not the StoryFinder should be used. (TRUE to use the StoryFinder)
    * @param {Integer} 	mobile  			Flag for whether or not the AdShot is for mobile (TRUE to use mobile)
    * @param {Integer} 	belowTheFold  		Flag for whether or not the AdShot should be taken below the fold (TRUE to take it below the fold)
    */
    constructor(requestedURL, storyFinder, mobile, belowTheFold) {
        //If any of the arguments are missing, throw an error
        if ((requestedURL == null) ||
            (storyFinder == null) ||
            (mobile == null) ||
            (belowTheFold == null)) {
            throw "AdShotBuilder constructor: missing argument";
        }
        //Verify the requested URL is a non-empty string
        if ((typeof requestedURL !== 'string') || (requestedURL == "")) {
            throw "AdShotBuilder.constructor: requestedURL must be a non-empty string";
        }
        //Store the member properties
        this._requestedURL = requestedURL;
        this._storyFinder = storyFinder;
        this._mobile = mobile;
        this._belowTheFold = belowTheFold;
        //Create the initial empty Creative set
        this._creatives = new Set();
    }
    //---------------------------------------------------------------------------------------
    //------------------------------- Modification Methods ----------------------------------
    //---------------------------------------------------------------------------------------
    /**
    * Adds a Creative to associate with and inject into the AdShot
    *
    * @param {Creative} 	newCreative  	Creative to add to the CreativeGroup
    */
    addCreative(newCreative) {
        if (newCreative instanceof Creative) {
            this._creatives.add(newCreative);
            return;
        }
    }
    /**
    * Returns a JSON string of the AdShot data that can be used by the server PHP Campaign class
    * to create the new AdShot.
    *
    * @return {String}		JSON string of instance data to be used on server to create new AdShot
    */
    toJSON() {
        //Build the JSON object with the member values
        let jsonObject = {
            requestedURL: this._requestedURL,
            storyFinder: this._storyFinder,
            mobile: this._mobile,
            belowTheFold: this._belowTheFold,
            creativeIDs: []
        };
        //Add any Creative IDs
        for (let currentCreative of this._creatives) {
            jsonObject.creativeIDs.push(currentCreative.id());
        }
        //Convert to JSON string and return
        return JSON.stringify(jsonObject);
    }
    //---------------------------------------------------------------------------------------
    //----------------------------------- Accessors -----------------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Accessors ************************************
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
    * @return {Set}		Creatives to associate with the AdShot
    */
    creatives() { return this._creatives; }
}
