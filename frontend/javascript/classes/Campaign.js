"use strict";
/**
* The Campaign class allows retrieval of the basic information for a Campaign such as ID, UUID, PowerPoint URL, and processing status.
*
* This is an immutable class. Its constructor takes a Campaign Object JSON string (generated on the server) and uses it
* to set the private member variables.
*
* This class mirrors the Java and PHP Creative classes
* ANY CHANGES TO THIS CLASS SHOULD BE MADE IN THOSE CLASSES AS WELL.
*/
class Campaign {
    //---------------------------------------------------------------------------------------
    //---------------------------------- Constants ------------------------------------------
    //---------------------------------------------------------------------------------------
    //Status constants for Capaign processing
    static get CREATED() { return "CREATED"; }
    static get READY() { return "READY"; }
    static get QUEUED() { return "QUEUED"; }
    static get PROCESSING() { return "PROCESSING"; }
    static get FINISHED() { return "FINISHED"; }
    static get ERROR() { return "ERROR"; }
    //Error constants
    static get SCREENSHOTCAPTURE() { return "SCREENSHOTCAPTURE"; }
    static get POWERPOINTGENERATION() { return "POWERPOINTGENERATION"; }
    static get CAMPAIGNEMAILSEND() { return "CAMPAIGNEMAILSEND"; }
    //---------------------------------------------------------------------------------------
    //------------------------ Constructors/Copiers/Destructors -----------------------------
    //---------------------------------------------------------------------------------------
    /**
    * Initializes the Creative using the provided Creative Object JSON
    *
    * @param {String} 	creativeJSON  		JSON of Creative object
    */
    constructor(campaignJSON) {
        //If the JSON is missing or not a string, throw an error
        if (campaignJSON == null) {
            throw "Campaign constructor: missing argument";
        }
        if ((typeof campaignJSON !== 'string') || (campaignJSON == "")) {
            throw "Campaign.constructor: argument must be a non-empty string";
        }
        //Convert the JSON to an object
        let baseCampaign = JSON.parse(campaignJSON);
        //Verify the Campaign object
        if (baseCampaign == null) {
            throw "Campaign constructor: unable to parse JSON";
        }
        if ((typeof baseCampaign.id === 'undefined') || (typeof baseCampaign.uuid === 'undefined')) {
            throw "Campaign.constructor: JSON object missing id or uuid";
        }
        //Store the Campaign properties
        this._id = baseCampaign.id;
        this._uuid = baseCampaign.uuid;
        this._customerName = baseCampaign.customerName;
        this._powerPointFilename = baseCampaign.powerPointFilename;
        this._powerPointURL = baseCampaign.powerPointURL;
        this._status = baseCampaign.status;
        this._errorMessage = baseCampaign.errorMessage;
        this._createdTimestamp = baseCampaign.createdTimestamp;
        this._readyTimestamp = baseCampaign.readyTimestamp;
        this._queuedTimestamp = baseCampaign.queuedTimestamp;
        this._processingTimestamp = baseCampaign.processingTimestamp;
        this._finishedTimestamp = baseCampaign.finishedTimestamp;
        this._errorTimestamp = baseCampaign.errorTimestamp;
        //Create the AdShots
        this._adShots = new Set();
        for (let currentAdShotJSON of baseCampaign.adShots) {
            let newAdShot = new AdShot(currentAdShotJSON);
            if (newAdShot) {
                this._adShots.add(newAdShot);
            }
        }
    }
    //---------------------------------------------------------------------------------------
    //----------------------------------- Accessors -----------------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Accessors ************************************
    /**
    * @return {Integer}	Campaign ID
    */
    id() { return this._id; }
    /**
    * @return {String}	Campaign UUID
    */
    uuid() { return this._uuid; }
    /**
    * @return {String}	Customer name for the Campaign
    */
    customerName() { return this._customerName; }
    /**
    * @return {Set}		AdShots associated with the Campaign
    */
    adShots() { return this._adShots; }
    /**
    * @return {String}	Filename of the final PowerPoint
    */
    powerPointFilename() { return this._powerPointFilename; }
    /**
    * @return {String}	URL of the final PowerPoint
    */
    powerPointURL() { return this._powerPointURL; }
    /**
    * @return {String}	Current processing status of the Campaign. (Options class constants: CREATED, QUEUED, PROCESSING, FINISHED, ERROR)
    */
    status() { return this._status; }
    /**
    * @return {String}	Error message if an error occurred while processing the Campaign
    */
    errorMessage() { return this._errorMessage; }
    /**
    * @return {Integer}	UNIX Timestamp the Campaign was inserted into the database
    */
    createdTimestamp() { return this._createdTimestamp; }
    /**
    * @return {Integer}	UNIX Timestamp the Campaign status was set to READY
    */
    readyTimestamp() { return this._readyTimestamp; }
    /**
    * @return {Integer}	UNIX Timestamp the Campaign status was set to QUEUED
    */
    queuedTimestamp() { return this._queuedTimestamp; }
    /**
    * @return {Integer}	UNIX Timestamp the Campaign status was set to PROCESSING
    */
    processingTimestamp() { return this._processingTimestamp; }
    /**
    * @return {Integer}	UNIX Timestamp the Campaign status was set to FINISHED
    */
    finishedTimestamp() { return this._finishedTimestamp; }
    /**
    * @return {Integer}	UNIX timestamp the Campaign status was set to ERROR when an error occurred
    */
    errorTimestamp() { return this._errorTimestamp; }
}
