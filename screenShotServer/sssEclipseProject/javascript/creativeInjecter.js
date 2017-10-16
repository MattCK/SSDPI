"use strict";
/**
* This CreativeInjecter script inserts creative images in the running page while removing
* pop-up ads and overlays.
*
* The script is designed to be run by and return a JSON response to Selenium.
*/
//---------------------------------------------------------------------------------------
//------------------------------------ Classes ------------------------------------------
//---------------------------------------------------------------------------------------
/**
 * The Log class both outputs messages to the browser console and stores the messages. The stored log
 * is returned with the injected tags (and their locations) at the end of script execution.
 *
 * @class Log
 */
class Log {
    //---------------------------------------------------------------------------------------
    //--------------------------------- Static Methods --------------------------------------
    //---------------------------------------------------------------------------------------
    //***************************** Public Static Methods ***********************************
    /**
     * Outputs the passed message to the browser console and stores the message
     *
     * @static
     * @param {string} newMessage 	Message to output to browser console and store
     * @memberof Log
     */
    static output(newMessage) {
        //Output message to console. Used for testing.
        console.log(newMessage);
        //Store the message and add a newline to the end
        Log._messages += newMessage + "\n";
    }
    /**
     * Returns all the messages stored in the log (separated by newlines)
     *
     * @static
     * @returns
     * @memberof Log	All log messages (separated by newlines)
     */
    static getMessages() {
        return Log._messages;
    }
}
//---------------------------------------------------------------------------------------
//-------------------------------- Static Variables -------------------------------------
//---------------------------------------------------------------------------------------
//***************************** Private Static Variables ********************************
/**
 * List of messages in the Log. Each message is separated by a newline.
 *
 * @static
 * @memberof Log
 */
Log._messages = "";
/**
 * The Creative class stores the basic information for an ad creative image.
 *
 * It contains the creative unique ID, image URL, width and height in pixels,
 * and display priority.
 *
 * @class Creative
 */
class Creative {
    //---------------------------------------------------------------------------------------
    //------------------------ Constructors/Copiers/Destructors -----------------------------
    //---------------------------------------------------------------------------------------
    /**
     * Creates an instance of Creative.
     *
     * @param {string} id 			Unique ID (UUID) of the Creative
     * @param {string} imageURL 	URL of the Creative's image
     * @param {number} width 		Width of the creative in pixels (must be greater than 0)
     * @param {number} height 		Height of the creative in pixels (must be greater than 0)
     * @param {number} priority 	Priority of the Creative. A lower number means higher priority.
     * @memberof Creative
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
        //Verify arguments meet the correct criteria
        if (id == "") {
            throw "Creative.constructor: id must be a non-empty string";
        }
        if (imageURL == "") {
            throw "Creative.constructor: imageURL must be a non-empty string";
        }
        if (width <= 0) {
            throw "Creative.constructor: width must be an integer greater than 0";
        }
        if (height <= 0) {
            throw "Creative.constructor: height must be an integer greater than 0";
        }
        //Store the member properties
        this._id = id;
        this._imageURL = imageURL;
        this._width = width;
        this._height = height;
        this._priority = priority;
    }
    //---------------------------------------------------------------------------------------
    //----------------------------------- Accessors -----------------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Accessors ************************************
    /**
     * Returns unique ID (UUID) of the Creative
     *
     * @returns 			unique ID (UUID) of the Creative
     * @memberof Creative
     */
    id() { return this._id; }
    /**
     * Returns URL of the Creative's image
     *
     * @returns 			URL of the Creative's image
     * @memberof Creative
     */
    imageURL() { return this._imageURL; }
    /**
     * Returns width in pixels of the Creative's image
     *
     * @returns 			width in pixels of the Creative's image
     * @memberof Creative
     */
    width() { return this._width; }
    /**
     * Returns height in pixels of the Creative's image
     *
     * @returns 			height in pixels of the Creative's image
     * @memberof Creative
     */
    height() { return this._height; }
    /**
     * Returns the priority of the Creative. A lower number means higher priority.
     *
     * @returns 			Priority of the Creative. A lower number means higher priority.
     * @memberof Creative
     */
    priority() { return this._priority; }
}
/**
 * The Coordinates class stores x and y coordinates
 *
 * @class Coordinates
 */
class CICoordinates {
    //---------------------------------------------------------------------------------------
    //------------------------ Constructors/Copiers/Destructors -----------------------------
    //---------------------------------------------------------------------------------------
    /**
     * Creates an instance of Coordinates with the passed x and y positions.
     *
     * @param {number} xPosition  		x position
     * @param {number} yPosition  		y position
     * @memberof Coordinates
     */
    constructor(xPosition, yPosition) {
        //If any of the arguments are missing, throw an error
        if ((xPosition == null) || (yPosition == null)) {
            throw "Coordinates.constructor: missing argument";
        }
        //Store the member properties
        this._x = xPosition;
        this._y = yPosition;
    }
    //---------------------------------------------------------------------------------------
    //----------------------------------- Accessors -----------------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Accessors ************************************
    /**
     * Return the x-position of the Coordinates
     *
     * @returns 				The x-position of the Coordinates
     * @memberof Coordinates
     */
    x() { return this._x; }
    /**
     * Return the y-position of the Coordinates
     *
     * @returns 				The y-position of the Coordinates
     * @memberof Coordinates
     */
    y() { return this._y; }
}
/**
 * The CreativeGroup stores a group of Creative objects and tracks which are injected
 * and where they are injected.
 *
 * @class CreativeGroup
 */
class CreativeGroup {
    //---------------------------------------------------------------------------------------
    //------------------------ Constructors/Copiers/Destructors -----------------------------
    //---------------------------------------------------------------------------------------
    /**
     * Creates an instance of CreativeGroup.
     *
     * @memberof CreativeGroup
     */
    constructor() {
        //Creat the set to hold all the creatives
        this._creatives = new Set();
        //Create the map to mark all the creatives that were injected
        //and the locations they were injected. THe keys should be a
        //subset of or equivalent to _creatives
        this._injectedCreatives = new Map();
    }
    //---------------------------------------------------------------------------------------
    //------------------------------- Modification Methods ----------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Methods **************************************
    /**
     * Adds a Creative object to the CreativeGroup.
    *
    * If the Creative already exists in the CreativeGroup, nothing occurs.
    *
    * @param {Creative} newCreative 	New Creative to add.
    * @returns {void}
    * @memberof CreativeGroup
    */
    addCreative(newCreative) {
        this._creatives.add(newCreative);
    }
    /**
     * Removes a Creative object from the CreativeGroup.
     *
     * If the Creative does not exist in the CreativeGroup, nothing occurs.
     *
     * @param {Creative} creativeToRemove 	Creative to remove
     * @memberof CreativeGroup
     */
    removeCreative(creativeToRemove) {
        this._creatives.delete(creativeToRemove);
    }
    /**
     * Returns true if the instance contains a Creative with the passed dimensions
     * and false otherwise
     *
     * @param {number} width 	Width of to check all the Creatives against
     * @param {number} height 	Height of to check all the Creatives against
     * @returns 				TRUE if a Creative the dimensions exist, FALSE otherwise
     * @memberof CreativeGroup
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
     * @param {number} width 	Width of Creative
     * @param {number} height 	Height of Creative
     * @returns 				Uninjected Creative with highest priority of passed dimensions if exists or null
     * @memberof CreativeGroup
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
                if (nextUninjectedCreative == null) {
                    nextUninjectedCreative = currentCreative;
                }
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
     * @param {Creative} injectedCreative 	Creative to flag as injected
     * @param {number} xPosition 			x position of the injected Creative's location on the page
     * @param {number} yPosition 			y position of the injected Creative's location on the page
     * @memberof CreativeGroup
     */
    injected(injectedCreative, xPosition, yPosition) {
        //Verify the Creative exists in the CreativeGroup
        if (!this._creatives.has(injectedCreative)) {
            throw "CreativeGroup.injected: Creative does not exist in CreativeGroup";
        }
        //Verify it has not already been marked as injected
        if (this._injectedCreatives.has(injectedCreative)) {
            throw "CreativeGroup.injected: Creative already marked as injected";
        }
        //If either of the position arguments are missing, throw an error
        if ((xPosition == null) || (yPosition == null)) {
            throw "CreativeGroup.injected: missing position argument";
        }
        //Creat the Coordinates object and add it all to the injected map
        let injectionCoordinates = new CICoordinates(Math.round(xPosition), Math.round(yPosition));
        this._injectedCreatives.set(injectedCreative, injectionCoordinates);
    }
    //---------------------------------------------------------------------------------------
    //----------------------------------- Accessors -----------------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Accessors ************************************
    /**
     * Returns the set of Creatives in the CreativeGroup
     *
     * @returns 				Set of Creatives in the CreativeGroup
     * @memberof CreativeGroup
     */
    getCreatives() { return this._creatives; }
    /**
    * @return {Map}	Map of Creatives that have been flagged as injected with their page Coordinates
    */
    /**
     * Returns the map of Creatives flagged as injected paired with their coordinates
     *
     * @returns 					Map of Creatives flagged as injected paired with their coordinates
     * @memberof CreativeGroup
     */
    getInjectedCreatives() { return this._injectedCreatives; }
}
/**
 * The CreativeSize class stores the width and height of a Creative.
 *
 * @class CreativeSize
 */
class CreativeSize {
    //---------------------------------------------------------------------------------------
    //------------------------ Constructors/Copiers/Destructors -----------------------------
    //---------------------------------------------------------------------------------------
    /**
     * Creates a CreativeSize instance with its width and height.
     *
     * @param {number} width 	Width of the creative in pixels (must be greater than 0)
     * @param {number} height 	Height of the creative in pixels (must be greater than 0)
     * @memberof CreativeSize
     */
    constructor(width, height) {
        //If any of the arguments are missing, throw an error
        if ((width == null) || (height == null)) {
            throw "CreativeSize.constructor: missing argument";
        }
        //Verify integers were passed and meet the correct criteria
        if ((!Number.isInteger(width)) || (width <= 0)) {
            throw "CreativeSize.constructor: width must be an integer greater than 0";
        }
        if ((!Number.isInteger(height)) || (height <= 0)) {
            throw "CreativeSize.constructor: height must be an integer greater than 0";
        }
        //Store the member properties
        this._width = width;
        this._height = height;
    }
    //---------------------------------------------------------------------------------------
    //----------------------------------- Accessors -----------------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Accessors ************************************
    /**
     * Returns the width of the Creative in pixels
     *
     * @returns 				The width of the Creative in pixels
     * @memberof CreativeSize
     */
    width() { return this._width; }
    /**
     * Returns the height of the Creative in pixels
     *
     * @returns 				The height of the Creative in pixels
     * @memberof CreativeSize
     */
    height() { return this._height; }
}
/**
 * The AdSelector class stores a selector that points to an ad element on the page and
 * the possible creative sizes that the element accepts.
 *
 * @class AdSelector
 */
class AdSelector {
    //---------------------------------------------------------------------------------------
    //------------------------ Constructors/Copiers/Destructors -----------------------------
    //---------------------------------------------------------------------------------------
    /**
     * Creates an instance of AdSelector.
     *
     * Receives the optional argument 'hideIfNotReplaced' to flag whether or not
     * the selector element should be hidden if it is not replaced with a Creative.
     * Default: FALSE (Used by CreativeInjecter)
     *
     * @param {string} selector 			The selector to the ad element
     * @param {boolean} hideIfNotReplaced 	TRUE to hide if AdSelector not used, FALE otherwise
     * @memberof AdSelector
     */
    constructor(selector, hideIfNotReplaced = false) {
        //Verify the selector is a non-empty string
        if ((selector == null) || (typeof selector !== 'string') || (selector == "")) {
            throw "AdSelector.constructor: selector must be a non-empty string";
        }
        //Store the selector and initialize the sizes array
        this._selector = selector;
        this._sizes = new Set();
        //Flag whether or not to hide the element if not replaced by a creative
        this._hideIfNotReplaced = hideIfNotReplaced;
    }
    //---------------------------------------------------------------------------------------
    //------------------------------- Modification Methods ----------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Methods **************************************
    /**
     * Adds the passed width and height as a possible creative size for the ad element
     *
     * @param {number} width 	Width of the creative in pixels (must be greater than 0)
     * @param {number} height 	Height of the creative in pixels (must be greater than 0)
     * @returns 				This AdSelector instance
     * @memberof AdSelector
     */
    addSize(width, height) {
        //If any of the arguments are missing, throw an error
        if ((width == null) || (height == null)) {
            throw "AdSelector addSize: missing argument";
        }
        //Verify integers were passed and meet the correct criteria
        if ((!Number.isInteger(width)) || (width <= 0)) {
            throw "AdSelector.addSize: width must be an integer greater than 0";
        }
        if ((!Number.isInteger(height)) || (height <= 0)) {
            throw "AdSelector.addSize: height must be an integer greater than 0";
        }
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
     * @param {Array<Array<number>>} sizesArray 	Array of [width, height] arrays
     * @returns 									This AdSelector instance
     * @memberof AdSelector
     */
    addSizes(sizesArray) {
        //Verify an array was passed
        if (!Array.isArray(sizesArray)) {
            throw "AdSelector.addSizes: argument must be an array";
        }
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
    //---------------------------------------------------------------------------------------
    //----------------------------------- Accessors -----------------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Accessors ************************************
    /**
     * Returns the selector to the ad element
     *
     * @returns 				Selector to the ad element
     * @memberof AdSelector
     */
    selector() { return this._selector; }
    /**
     * Returns the set of CreativeSizes the ad element will accept
     *
     * @returns 				Set of CreativeSizes the ad element will accept
     * @memberof AdSelector
     */
    sizes() { return this._sizes; }
    /**
     * Returns flag whether or not the ad element should be hidden if AdSelector is unused. TRUE to hide.
     *
     * @returns 			 	Flag for whether or not the ad element should be hidden if AdSelector is unused. TRUE to hide.
     * @memberof AdSelector
     */
    hideIfNotReplaced() { return this._hideIfNotReplaced; }
}
/**
 * The ElementInfo class contains a series of static functions to obtain current
 * information on an HTMLElement node.
 *
 * All information is calculated on the current state of the node.
 *
 * @class ElementInfo
 */
class ElementInfo {
    //---------------------------------------------------------------------------------------
    //--------------------------------- Static Methods --------------------------------------
    //---------------------------------------------------------------------------------------
    //***************************** Public Static Methods ***********************************
    /**
    * @return {Number}	Current width of the element
    */
    /**
     * Returns the current width of the element (including border)
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							Current width of the element (including border)
     * @memberof ElementInfo
     */
    static width(elementNode) {
        //Get the computed width of the string
        let elementWidth = document.defaultView.getComputedStyle(elementNode).getPropertyValue('width');
        //If the width is either a % or auto, get the parent width
        if ((elementWidth.slice(-1) == "%") || (elementWidth == "auto")) {
            //Loop through the parent node until one with a px width is found
            let currentParentElement = elementNode.parentElement;
            let containerWidth = null;
            while ((containerWidth == null) && (currentParentElement)) {
                //If the parent width is not a % or auto, use its width as the final containerWidth
                let currentWidth = document.defaultView.getComputedStyle(currentParentElement).getPropertyValue('width');
                if ((currentWidth.slice(-1) != "%") && (currentWidth != "auto")) {
                    containerWidth = currentWidth;
                }
                //Increment to the next parent element for the loop
                currentParentElement = currentParentElement.parentElement;
            }
            //If a non % or auto container width was found, set the element width to it
            if (containerWidth != null) {
                //If the original width was a percentage, multiply the container width by it
                if (elementWidth.slice(-1) == "%") {
                    let percentage = Number(elementWidth.slice(0, -1)) / 100;
                    containerWidth = (Number(containerWidth.slice(0, -2)) * percentage) + 'px';
                }
                //Set the element's calculated width to the container's calculated width
                elementWidth = containerWidth;
            }
        }
        //Return the width with the pixels removed and as a number
        return Number(elementWidth.slice(0, -2));
    }
    /**
     * Returns the current width of the element (not including border)
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							Current width of the element (not including border)
     * @memberof ElementInfo
     */
    static widthWithoutBorder(elementNode) {
        return ElementInfo.width(elementNode) -
            ElementInfo.borderWidthLeft(elementNode) - ElementInfo.borderWidthRight(elementNode);
    }
    /**
     * Returns the current height of the element (including border)
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							Current height of the element (including border)
     * @memberof ElementInfo
     */
    static height(elementNode) {
        //Get the computed height of the string
        let elementHeight = document.defaultView.getComputedStyle(elementNode).getPropertyValue('height');
        //If the height is either a % or auto, get the parent height
        if ((elementHeight.slice(-1) == "%") || (elementHeight == "auto")) {
            //Loop through the parent node until one with a px height is found
            let currentParentElement = elementNode.parentElement;
            let containerHeight = null;
            while ((containerHeight == null) && (currentParentElement)) {
                //If the parent height is not a % or auto, use its height as the final containerHeight
                let currentHeight = document.defaultView.getComputedStyle(currentParentElement).getPropertyValue('height');
                if ((currentHeight.slice(-1) != "%") && (currentHeight != "auto")) {
                    containerHeight = currentHeight;
                }
                //Increment to the next parent element for the loop
                currentParentElement = currentParentElement.parentElement;
            }
            //If a non % or auto container height was found, set the element height to it
            if (containerHeight != null) {
                //If the original height was a percentage, multiply the container height by it
                if (elementHeight.slice(-1) == "%") {
                    let percentage = Number(elementHeight.slice(0, -1)) / 100;
                    containerHeight = (Number(containerHeight.slice(0, -2)) * percentage) + 'px';
                }
                //Set the element's calculated height to the container's calculated height
                elementHeight = containerHeight;
            }
        }
        //Return the height with the pixels removed and as a number
        return Number(elementHeight.slice(0, -2));
    }
    /**
     * Returns the current height of the element (not including border)
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							Current height of the element (not including border)
     * @memberof ElementInfo
     */
    static heightWithoutBorder(elementNode) {
        return ElementInfo.height(elementNode) -
            ElementInfo.borderWidthTop(elementNode) - ElementInfo.borderWidthBottom(elementNode);
    }
    /**
     * Returns the current x-position of the element
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							Current x-position of the element
     * @memberof ElementInfo
     */
    static xPosition(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return (ElementInfo.getScreenCoordinates(elementNode)).x();
    }
    /**
     * Returns the current y-position of the element
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							Current y-position of the element
     * @memberof ElementInfo
     */
    static yPosition(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return (ElementInfo.getScreenCoordinates(elementNode)).y();
    }
    /**
     * Returns the current bottom border of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current bottom border of the element in pixels
     * @memberof ElementInfo
     */
    static borderWidthBottom(elementNode) {
        let widthString = document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('border-bottom-width');
        return Number(widthString.slice(0, -2));
    }
    /**
     * Returns the current left border of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current left border of the element in pixels
     * @memberof ElementInfo
     */
    static borderWidthLeft(elementNode) {
        let widthString = document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('border-left-width');
        return Number(widthString.slice(0, -2));
    }
    /**
     * Returns the current right border of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current right border of the element in pixels
     * @memberof ElementInfo
     */
    static borderWidthRight(elementNode) {
        let widthString = document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('border-right-width');
        return Number(widthString.slice(0, -2));
    }
    /**
     * Returns the current top border of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current top border of the element in pixels
     * @memberof ElementInfo
     */
    static borderWidthTop(elementNode) {
        let widthString = document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('border-top-width');
        return Number(widthString.slice(0, -2));
    }
    /**
     * Returns the current flood-opacity of the element
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current flood-opacity of the element
     * @memberof ElementInfo
     */
    static floodOpacity(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('flood-opacity');
    }
    /**
     * Returns the current bottom margin of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current bottom margin of the element in pixels
     * @memberof ElementInfo
     */
    static marginBottom(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return parseInt(document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('margin-bottom'), 10);
    }
    /**
     * Returns the current left margin of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current left margin of the element in pixels
     * @memberof ElementInfo
     */
    static marginLeft(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return parseInt(document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('margin-left'), 10);
    }
    /**
     * Returns the current right margin of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current right margin of the element in pixels
     * @memberof ElementInfo
     */
    static marginRight(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return parseInt(document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('margin-right'), 10);
    }
    /**
     * Returns the current top margin of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current top margin of the element in pixels
     * @memberof ElementInfo
     */
    static marginTop(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return parseInt(document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('margin-top'), 10);
    }
    /**
     * Returns the current bottom padding of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current bottom padding of the element in pixels
     * @memberof ElementInfo
     */
    static paddingBottom(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return parseInt(document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('padding-bottom'), 10);
    }
    /**
     * Returns the current left padding of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current left padding of the element in pixels
     * @memberof ElementInfo
     */
    static paddingLeft(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return parseInt(document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('padding-left'), 10);
    }
    /**
     * Returns the current right padding of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current right padding of the element in pixels
     * @memberof ElementInfo
     */
    static paddingRight(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return parseInt(document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('padding-right'), 10);
    }
    /**
     * Returns the current top padding of the element in pixels
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current top padding of the element in pixels
     * @memberof ElementInfo
     */
    static paddingTop(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return parseInt(document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('padding-top'), 10);
    }
    /**
     * Returns the current "position" style of the element, such as 'fixed'
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current "position" style of the element, such as 'fixed'
     * @memberof ElementInfo
     */
    static positionStyle(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('position');
    }
    /**
     * Returns the current z-index of the element
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							The current z-index of the element
     * @memberof ElementInfo
     */
    static zIndex(elementNode) {
        if (!ElementInfo.isHTMLElement(elementNode)) {
            return null;
        }
        return document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('z-index');
    }
    static backgroundAlpha(elementNode) {
        let backgroundColors = document.defaultView.getComputedStyle(elementNode).getPropertyValue('background-color');
        let colorValues = backgroundColors.slice(5, -1).split("");
        if (colorValues.length < 4) {
            return 1;
        }
        else {
            return colorValues[3];
        }
    }
    /**
     * Returns the x,y coordinates of the passed node in relation to the screen view.
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							Coordinates object with x,y set to node's screen position
     * @memberof ElementInfo
     */
    static getScreenCoordinates(elementNode) {
        //If the element display is none, set it to block for the position then set it back
        let displayStatus = document.defaultView.getComputedStyle(elementNode, undefined).getPropertyValue('display');
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
        return new CICoordinates(Math.round(xPosition), Math.round(yPosition));
    }
    /**
     * Returns the element's containing IFrame or null if not inside an IFrame.
     *
     * @static
     * @param {HTMLElement} containedNode 	Element inside possible IFrame
     * @returns 							IFrame containing element or null otherwise
     * @memberof ElementInfo
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
     * Returns TRUE if passed argument is an HTMLElement and FALSE otherwise
     *
     * @static
     * @param {HTMLElement} elementNode 	Target element
     * @returns 							TRUE if passed argument is an HTMLElement and FALSE otherwise
     * @memberof ElementInfo
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
    //---------------------------------------------------------------------------------------
    //------------------------ Constructors/Copiers/Destructors -----------------------------
    //---------------------------------------------------------------------------------------
    /**
     * Creates an instance of GPTSlots and loads it with any Google tag slots
     * currently on the page.
     *
     * @memberof GPTSlots
     */
    constructor() {
        //Begin by instantiating the slots and slots sizes members regardless if the googletag object exists
        this._slots = [];
        this._slotCreativeSizes = new Map();
        //If the googletag object exists, instantiate the class using its information
        if ((typeof googletag !== 'undefined') && (typeof googletag.pubads !== 'undefined')) {
            //Store any googletag.Slot objects
            this._slots = googletag.pubads().getSlots();
            //Get the slot object properties for a slot with sizes passed to the constructor.
            let dummySlot = this._getDummySlot(GPTSlots._TARGETWIDTH, GPTSlots._TARGETHEIGHT);
            let propertyInfo = this._getSizesProperties(dummySlot, GPTSlots._TARGETWIDTH, GPTSlots._TARGETHEIGHT);
            this._sizesPropertyKey = propertyInfo.containingPropertyKey;
            this._sizesClass = propertyInfo.sizesClass;
            this._sizesWidthKey = propertyInfo.widthKey;
            this._sizesHeightKey = propertyInfo.heightKey;
            //Get the slot size properties for a slot created with a SizeMapping object
            let sizeMappingSlot = this._getDummySlotWithMapping(GPTSlots._TARGETWIDTH, GPTSlots._TARGETHEIGHT, GPTSlots._TARGETVIEWPORTWIDTH, GPTSlots._TARGETVIEWPORTHEIGHT);
            let mappingPropertyInfo = this._getMappingProperties(sizeMappingSlot, GPTSlots._TARGETVIEWPORTWIDTH, GPTSlots._TARGETVIEWPORTHEIGHT);
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
    //---------------------------------------------------------------------------------------
    //----------------------------------- Accessors -----------------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Accessors ************************************
    /**
     * Returns the set of AdSelectors for all of the Google.Slots.
     *
     * The hide if not replaced flag is set to true for all AdSelectors.
     *
     * @returns
     * @memberof GPTSlots	Set of AdSelectors for all of the Google.Slots.
     */
    adSelectors() {
        //Get the screen width
        let screenWidth = window.innerWidth;
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
                //If the size width can fit in the screen, add it
                if (slotCreativeSize.width() <= screenWidth) {
                    currentAdSelector.addSize(slotCreativeSize.width(), slotCreativeSize.height());
                }
            }
            //Add the current selector to the overall set
            slotAdSelectors.add(currentAdSelector);
        }
        //Return the set of AdSelectors
        return slotAdSelectors;
    }
    //---------------------------------------------------------------------------------------
    //------------------------------- Modification Methods ----------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Private Methods *************************************
    /**
     * Returns a Set of CreativeSizes for all the possible sizes of the passed slot
     *
     * @private
     * @param {*} slot 		Slot to get CreativeSizes for
     * @returns 			Set of CreativeSizes for the passed slot
     * @memberof GPTSlots
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
            //Get the viewport width and height
            let viewportWidth = currentViewport.get("width");
            let viewportHeight = currentViewport.get("height");
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
            if (!sizeFound) {
                uniqueCreativeSizes.add(currentCreativeSize);
            }
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
     * @private
     * @param {*} slot 					Slot to get property information for
     * @param {number} targetWidth 		Width of the predefined size to look for
     * @param {number} targetHeight 	Height of the predefined size to look for
     * @returns 						Property and class information for the target size (See description for details)
     * @memberof GPTSlots
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
        let findSize = function (currentObject) {
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
                            if (typeof value == "object") {
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
                                            widthKey = firstKey;
                                            heightKey = secondKey;
                                        }
                                        else {
                                            widthKey = secondKey;
                                            heightKey = firstKey;
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
                                }
                                ;
                            }
                        }
                        catch (e) { }
                    }
                }
            }
        };
        //Call the findSize recursive function on the passed slot
        findSize(slot);
        //Return the found information
        return { containingPropertyKey: containingPropertyKey, sizesClass: sizesClass,
            widthKey: widthKey, heightKey: heightKey };
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
     * @private
     * @param {*} slot 							Slot with size mapping to get property information for
     * @param {number} targetViewportWidth 		Width of the predefined viewport to look for
     * @param {number} targetViewportHeight 	Height of the predefined viewport to look for
     * @returns 								Property and class information for the target mapping (See description for details)
     * @memberof GPTSlots
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
        let findViewport = function (currentObject) {
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
                            if (typeof value == "object") {
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
                                            viewportWidthKey = firstKey;
                                            viewportHeightKey = secondKey;
                                        }
                                        else {
                                            viewportWidthKey = secondKey;
                                            viewportHeightKey = firstKey;
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
                                }
                                ;
                            }
                        }
                        catch (e) { }
                    }
                }
            }
        };
        //Call the findSize recursive function on the passed slot
        findViewport(slot);
        //Return the found information
        return { containingPropertyKey: containingPropertyKey,
            mappingClass: mappingClass,
            viewportClass: viewportClass,
            viewportWidthKey: viewportWidthKey,
            viewportHeightKey: viewportHeightKey };
    }
    /**
     * Returns a set of CreativeSizes for each possible size in the passed object
     *
     * @private
     * @param {string} slotProperty 	Slot object property to find sizes in
     * @returns 						Set of CreativeSizes for all the sizes found in the passed object
     * @memberof GPTSlots
     */
    _getCreativeSizesInProperty(slotProperty) {
        //Place this instance into its own variable for use in the recursive loop
        let thisInstance = this;
        //Recursively traverse the slot and store any found sizes
        let cache = [];
        let sizes = new Set();
        let findSizes = function (currentObject) {
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
                            if (typeof value == "object") {
                                //If the object is a sizes class, store the size
                                if (value.constructor.name == thisInstance._sizesClass) {
                                    let currentCreativeSize = new CreativeSize(value[thisInstance._sizesWidthKey], value[thisInstance._sizesHeightKey]);
                                    sizes.add(currentCreativeSize);
                                }
                                else {
                                    findSizes(value);
                                }
                                ;
                            }
                        }
                        catch (e) { }
                    }
                }
            }
        };
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
        let findViewport = function (currentObject) {
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
                            if (typeof value == "object") {
                                //If the object is a viewport class, store the dimensions and return
                                if (value.constructor.name == thisInstance._viewportClass) {
                                    viewportDimensions.set("width", value[thisInstance._viewportWidthKey]);
                                    viewportDimensions.set("height", value[thisInstance._viewportHeightKey]);
                                    return true;
                                }
                                else if (findViewport(value)) {
                                    return true;
                                }
                            }
                        }
                        catch (e) { }
                    }
                }
            }
        };
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
     * @private
     * @param {any} slotProperty 	Slot object property to find sizes in
     * @returns 					Map of viewports to their Set of CreativeSizes for all the sizes found in the passed object
     * @memberof GPTSlots
     */
    _getMappedSizesInProperty(slotProperty) {
        //Place this instance into its own variable for use in the recursive loop
        let thisInstance = this;
        //Recursively traverse the slot and store any found sizes
        let cache = [];
        let mappedSizes = new Map();
        let findSizes = function (currentObject) {
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
                            if (typeof value == "object") {
                                //If the object is a size mapping class, store its viewport and sizes
                                if (value.constructor.name == thisInstance._mappingClass) {
                                    let currentViewport = thisInstance._getViewportInProperty(value);
                                    let viewportSizes = thisInstance._getCreativeSizesInProperty(value);
                                    mappedSizes.set(currentViewport, viewportSizes);
                                }
                                else {
                                    findSizes(value);
                                }
                                ;
                            }
                        }
                        catch (e) { }
                    }
                }
            }
        };
        //Call the findSizes recursive function on the passed slot
        findSizes(slotProperty);
        //Return the found sizes
        return mappedSizes;
    }
    /**
     * Creates a googletag slot with the size arguments passed to the Slot constructor
     *
     * @private
     * @param {number} targetWidth 		Width of the slot
     * @param {number} targetHeight 	Height of the slot
     * @returns 						Newly created googletag slot with the size arguments passed to the Slot constructor
     * @memberof GPTSlots
     */
    _getDummySlot(targetWidth, targetHeight) {
        return googletag.defineSlot('/dummyPath' + Date.now(), [[targetWidth, targetHeight]], 'dummmyElementID' + Date.now());
    }
    /**
    * @return {googletag.Slot} 		Newly created slot with the size arguments added to the slot through a size mapping
    */
    /**
     * Creates a google slot with the size arguments and using a size mapping
     *
     * @private
     * @param {number} targetWidth 				Width of the slot
     * @param {number} targetHeight 			Height of the slot
     * @param {number} targetViewPortWidth 		Width of the viewport
     * @param {number} targetViewPortHeight 	Height of the viewport
     * @returns 								Newly created slot with the size arguments added to the slot through a size mapping
     * @memberof GPTSlots
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
//-------------------------------- Static Variables -------------------------------------
//---------------------------------------------------------------------------------------
//***************************** Private Static Variables ********************************
/**
 * Used to flag the width of a created dummy slot
 *
 * @private
 * @static
 * @memberof GPTSlots
 */
GPTSlots._TARGETWIDTH = 6420;
/**
 * Used to flag the height of a created dummy slot
 *
 * @private
 * @static
 * @memberof GPTSlots
 */
GPTSlots._TARGETHEIGHT = 7531;
/**
 * Used to flag the viewport width of a created dummy slot
 *
 * @private
 * @static
 * @memberof GPTSlots
 */
GPTSlots._TARGETVIEWPORTWIDTH = 8642;
/**
 * Used to flag the viewport height of a created dummy slot
 *
 * @private
 * @static
 * @memberof GPTSlots
 */
GPTSlots._TARGETVIEWPORTHEIGHT = 9753;
/**
 * The BingAds class returns AdSelectors on the MSN network.
 *
 * The single static function uses the site's "adsDivs" object to identify
 * the ads and their possible sizes.
 *
 * @class BingAds
 */
class BingAds {
    //---------------------------------------------------------------------------------------
    //----------------------------------- Accessors -----------------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Accessors ************************************
    /**
     * Returns the Set of BingAd AdSelectors if they exist on the current site.
     *
     * @static
     * @returns 			Set of BingAd AdSelectors if they exist on the current site.
     * @memberof BingAds
     */
    static getSelectors() {
        //If the BingAds "adsDivs" object exists, use it to create AdSelectors
        let bingAdSelectors = new Set();
        if (typeof adsDivs !== 'undefined') {
            //Loop through the divs and create an AdSelector for each
            for (let currentAdDiv of adsDivs) {
                //Make sure the sz size holding property is a non-empty string
                if ((typeof currentAdDiv.sz === 'string') && (currentAdDiv.sz.length >= 3)) {
                    //Create the new AdSelector with the div ID as the selector
                    let newAdSelector = new AdSelector("#" + currentAdDiv.id, true);
                    //Add the sizes to the AdSelector
                    let adSizes = currentAdDiv.sz.split(",");
                    for (let currentAdSize of adSizes) {
                        let sizeParts = currentAdSize.split("x");
                        newAdSelector.addSize(Number(sizeParts[0]), Number(sizeParts[1]));
                    }
                    //Add the AdSelector to the overall set
                    bingAdSelectors.add(newAdSelector);
                }
                else if ((currentAdDiv.h > 0) && (currentAdDiv.w > 0)) {
                    //Create the new AdSelector with the div ID as the selector
                    let newAdSelector = new AdSelector("#" + currentAdDiv.id, true);
                    newAdSelector.addSize(Number(currentAdDiv.w), Number(currentAdDiv.h));
                    //Add the AdSelector to the overall set
                    bingAdSelectors.add(newAdSelector);
                }
            }
        }
        //Return either the found AdSelectors or the initial empty set
        return bingAdSelectors;
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
 * @class CreativeInjecter
 */
class CreativeInjecter {
    //---------------------------------------------------------------------------------------
    //------------------------ Constructors/Copiers/Destructors -----------------------------
    //---------------------------------------------------------------------------------------
    /**
     * Creates an instance of CreativeInjecter.
     *
     * @param {CreativeGroup} creatives 			Creatives to inject into the running page
     * @param {Array<AdSelector>} adSelectors 		Array of AdSelectors for ad elements on the page
     * @param {number} injectionStartHeight 		Height at which creatives should be injected (this height and below)
     * @param {boolean} hideLargeFloatingElements 	If TRUE, hides large floating elements. (Default: TRUE)
     * 												WARNING: SETTING TO FALSE MAY PREVENT INTERSTITIALS FROM BEING HIDDEN
     * @memberof CreativeInjecter
     */
    constructor(creatives, adSelectors, injectionStartHeight, hideLargeFloatingElements) {
        //Verify creatives is a CreativeGroup
        if (!(creatives instanceof CreativeGroup)) {
            throw "CreativeInjecter.constructor: creatives must be of type CreativeGroup";
        }
        //Verify adSelectors is null or an array
        if ((adSelectors != null) && (!Array.isArray(adSelectors))) {
            throw "CreativeInjecter.constructor: adSelectors must be an array of AdSelector objects or null";
        }
        //If the start height is null or not a number, set it to 0
        if ((injectionStartHeight == null) || (isNaN(injectionStartHeight))) {
            injectionStartHeight = 0;
        }
        //If no hide large element argument was passed, set it to true
        if (hideLargeFloatingElements == null) {
            hideLargeFloatingElements = true;
        }
        //Store the creatives, start height, and hide floating argument
        this._creatives = creatives;
        this._injectionStartHeight = injectionStartHeight;
        this._hideLargeFloatingElements = hideLargeFloatingElements;
        //Store the AdSelectors
        this._adSelectors = [];
        if (adSelectors != null) {
            for (let currentAdSelector of adSelectors) {
                if (currentAdSelector instanceof AdSelector) {
                    this._adSelectors.push(currentAdSelector);
                }
            }
        }
    }
    //---------------------------------------------------------------------------------------
    //------------------------------- Modification Methods ----------------------------------
    //---------------------------------------------------------------------------------------
    //********************************* Public Methods **************************************
    /**
     * Injects all possible Creative into the page and stores the screen coordinates for each.
     *
     * @returns
     * @memberof CreativeInjecter
     */
    injectCreativesIntoPage() {
        //Sort the AdSelector elements by there positions. (Done here to prevent hiding large ads and overlays
        //from forcing positions of 0,0)
        this._sortAdSelectorsByPosition(this._adSelectors);
        //Begin my removing all large ads and overlays
        this._hideLargeAdsAndOverlays();
        //If no creatives were passed, simply exit at this point. This prevents non-replaced
        //ad selector elements from being hidden.
        if (this._creatives.getCreatives().size == 0) {
            return;
        }
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
                        //If the selector element exists and it does not have a negative y-coordinate, replace it with the creative
                        let currentElement = document.querySelector(currentAdSelector.selector());
                        let elementXPosition = ElementInfo.xPosition(currentElement);
                        let elementYPosition = ElementInfo.yPosition(currentElement);
                        if ((currentElement != null) && (elementXPosition != null) &&
                            (elementYPosition != null)) {
                            //If the y-position is positive (negative occurs when scrolled for below-the-fold)
                            if (elementYPosition >= this._injectionStartHeight) {
                                //Replace the element
                                this._replaceElementWithCreative(currentElement, creativeToInject);
                                this._creatives.injected(creativeToInject, elementXPosition, elementYPosition);
                                adSelectorReplaced = true;
                            }
                        }
                    }
                }
            }
            //If the element was not replaced, hide it if the AdSelector is flagged to hide if not replaced
            if (!adSelectorReplaced && currentAdSelector.hideIfNotReplaced()) {
                let currentElement = document.querySelector(currentAdSelector.selector());
                if (currentElement) {
                    if ((currentElement.offsetHeight > 0) && (currentElement.offsetWidth > 0)) {
                        this._hideElement(currentElement);
                    }
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
            let elementXPosition = ElementInfo.xPosition(currentElement);
            let elementYPosition = ElementInfo.yPosition(currentElement);
            //Make sure none of the element's properties are null
            if ((elementWidth != null) && (elementHeight != null) &&
                (elementXPosition != null) && (elementYPosition != null)) {
                //If an uninjected Creative of that size exists, replace the element with it
                let creativeToInject = this._creatives.getNextUninjectedCreative(elementWidth, elementHeight);
                if (creativeToInject) {
                    //If the y-position is positive (negative occurs when scrolled for below-the-fold)
                    if (elementYPosition >= this._injectionStartHeight) {
                        //Replace the element
                        this._replaceElementWithCreative(currentElement, creativeToInject);
                        this._creatives.injected(creativeToInject, elementXPosition, elementYPosition);
                    }
                }
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
                let elementXPosition = ElementInfo.xPosition(currentElement);
                let elementYPosition = ElementInfo.yPosition(currentElement);
                //Make sure none of the element's properties are null
                if ((elementWidth != null) && (elementHeight != null) &&
                    (elementXPosition != null) && (elementYPosition != null)) {
                    //If an uninjected Creative of that size exists, replace the element with it
                    let creativeToInject = this._creatives.getNextUninjectedCreative(elementWidth, elementHeight);
                    if (creativeToInject) {
                        //If the y-position is positive (negative occurs when scrolled for below-the-fold)
                        if (elementYPosition >= this._injectionStartHeight) {
                            //Replace the element
                            this._replaceElementWithCreative(currentElement, creativeToInject);
                            this._creatives.injected(creativeToInject, elementXPosition, elementYPosition);
                        }
                    }
                }
            }
        }
    }
    //********************************* Private Methods *************************************
    /**
     * Replaces the passed element with an image element referencing the image URL of the
     * passed Creative.
     *
     * @param {HTMLElement} elementNode  		Element to replace
     * @param {Creative} replacementCreative 	Creative to use for replacement image element
     * @returns
     * @memberof CreativeInjecter
     */
    _replaceElementWithCreative(elementNode, replacementCreative) {
        //If the element is not an HTMLElement or does not have a parent node, simply exit
        if ((elementNode == null) || (!ElementInfo.isHTMLElement(elementNode)) ||
            (!elementNode.parentNode)) {
            return;
        }
        //Store the original height and width of the node
        let originalNodeWidth = ElementInfo.widthWithoutBorder(elementNode);
        let originalNodeHeight = ElementInfo.heightWithoutBorder(elementNode);
        //Create the replacement image
        let creativeImage = document.createElement('img');
        creativeImage.src = replacementCreative.imageURL();
        // let replacementChild = null;
        // for (let currentChild of elementNode.children) {
        //     if (replacementChild == null) {
        // 		if ((currentChild.nodeName == "IFRAME") ||
        // 			(currentChild.nodeName == "DIV") ||
        // 			(currentChild.nodeName == "IMG")) {
        // 				replacementChild = currentChild;
        // 		}
        // 	}
        // }
        // if (replacementChild) {
        // 	creativeImage.style.cssText = document.defaultView.getComputedStyle(replacementChild, "").cssText;
        // 	Log.output("Got child styles for: " + replacementChild.nodeName);
        // }
        // styleObject = document.defaultView.getComputedStyle(elementNode, "");
        // for (let property in styleObject) {
        // 	creativeImage.style[property] = styleObject[property];
        // }
        creativeImage.style.width = replacementCreative.width() + 'px';
        creativeImage.style.height = replacementCreative.height() + 'px';
        creativeImage.style.maxWidth = replacementCreative.width() + 'px';
        creativeImage.style.maxHeight = replacementCreative.height() + 'px';
        creativeImage.style.margin = 'auto';
        creativeImage.style.display = 'flex';
        let elementChildren = elementNode.children;
        let headerNode = null;
        let firstFound = false;
        let headerIndex = 0;
        while ((!firstFound) && (headerIndex < elementChildren.length)) {
            let currentChild = elementChildren[headerIndex];
            if (currentChild != null) {
                if ((currentChild.nodeName == "A") || (currentChild.nodeName == "DIV")) {
                    firstFound = true;
                    if ((currentChild.innerHTML.toLowerCase().includes("adchoice")) ||
                        ((currentChild.textContent != null) &&
                            (currentChild.textContent.toLowerCase().includes("advertisement")))) {
                        headerNode = currentChild.cloneNode(true);
                    }
                }
                else if (currentChild.nodeName == "IFRAME") {
                    firstFound = true;
                }
            }
            ++headerIndex;
        }
        let footerNode = null;
        let lastFound = false;
        let footerIndex = elementChildren.length;
        while ((!lastFound) && (footerIndex >= 0)) {
            let currentChild = elementChildren[footerIndex];
            if (currentChild != null) {
                if ((currentChild.nodeName == "A") || (currentChild.nodeName == "DIV")) {
                    lastFound = true;
                    if ((currentChild.innerHTML.toLowerCase().includes("adchoice")) ||
                        ((currentChild.textContent != null) &&
                            (currentChild.textContent.toLowerCase().includes("advertisement")))) {
                        footerNode = currentChild.cloneNode(true);
                        console.log('______ Footer NOde FOund ____________');
                    }
                }
                else if (currentChild.nodeName == "IFRAME") {
                    lastFound = true;
                }
            }
            --footerIndex;
        }
        while (elementNode.hasChildNodes()) {
            if (elementNode.lastChild) {
                elementNode.removeChild(elementNode.lastChild);
            }
        }
        if (headerNode != null) {
            elementNode.appendChild(headerNode);
            creativeImage.style.clear = 'both';
        }
        elementNode.appendChild(creativeImage);
        if (footerNode != null) {
            footerNode.style.width = replacementCreative.width() + 'px';
            elementNode.appendChild(footerNode);
        }
        //Make sure the parents are displayed and at least as big as the Creative image
        // this._crawlParentHTMLElements(creativeIFrame, function(currentNode) {
        this._crawlParentHTMLElements(creativeImage, function (currentNode) {
            //Get the current node's width and height minus border width
            let currentNodeWidth = ElementInfo.widthWithoutBorder(currentNode);
            let currentNodeHeight = ElementInfo.heightWithoutBorder(currentNode);
            // Log.output(currentNode.id + ": " + currentNodeWidth + "x" + currentNodeHeight);
            //Make sure the current node is displayed
            //***************
            //Note: The LI Exception is for MSN. It should not be permanent. It could cause future errors.
            //***************
            let displayStatus = document.defaultView.getComputedStyle(currentNode, undefined).getPropertyValue('display');
            if ((displayStatus == "none") && (currentNode.nodeName != "LI")) {
                currentNode.style.display = "block";
            }
            currentNode.style.visibility = "visible";
            //Some ads use CSS animations to come in to view once they are loaded such as 'fade in'
            //Force any animation to run
            currentNode.style.animationPlayState = "running";
            //Make sure the width and height are not null
            if ((currentNodeWidth != null) && (currentNodeHeight != null)) {
                //If the node is the same size as the original ad element AND larger than the injected Creative,
                //set its width and height to the size of the injected Creative
                if ((currentNodeWidth == originalNodeWidth) && (currentNodeHeight == originalNodeHeight) &&
                    ((currentNodeWidth > replacementCreative.width()) || (currentNodeHeight > replacementCreative.height()))) {
                    //currentNode.style.minWidth = replacementCreative.width() + 'px';	//Left out until necessary so as not to
                    //unintentionally cause bugs
                    currentNode.style.minHeight = replacementCreative.height() + 'px';
                    //currentNode.style.width = replacementCreative.width() + 'px';
                    // currentNode.style.height = replacementCreative.height() + 'px';
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
            }
        });
    }
    /**
     * Hides any large elements that are likely ads
     *
     * @memberof CreativeInjecter
     */
    _hideLargeAdsAndOverlays() {
        //Get the highest z-index to flag overlays
        let highestZIndex = this._getHighestZIndex();
        console.log("Highest z-index: " + highestZIndex);
        //Crawl through the DOM and remove all large ads and fixed elements with matching criteria
        let thisCreativeInjecter = this; //For scope
        let creatives = this._creatives; //For scope
        let hideLargeFloatingElements = this._hideLargeFloatingElements; //For scope
        this._crawlDocumentHTMLElements(document, function (currentNode) {
            //Get the node size minus border width
            let nodeWidth = ElementInfo.widthWithoutBorder(currentNode);
            let nodeHeight = ElementInfo.heightWithoutBorder(currentNode);
            //Get the current node's flood-opacity, position style, position, and z-index
            let nodeFloodOpacity = ElementInfo.floodOpacity(currentNode);
            let nodePositionStyle = ElementInfo.positionStyle(currentNode);
            let nodeXPosition = ElementInfo.xPosition(currentNode);
            let nodeYPosition = ElementInfo.yPosition(currentNode);
            let nodeZIndex = Number(ElementInfo.zIndex(currentNode));
            let nodeAlpha = ElementInfo.backgroundAlpha(currentNode);
            let nodeContentLength = (currentNode.textContent != null) ? currentNode.textContent.length : 0;
            /**************************** Remove Large Ads ****************************/
            //If the node has been :
            //	- Marked by the AdMarker as an ad element
            //	- Is not the size of an instance Creative
            //	- Is equal to or larger than the size set by _MAXIMUMADKEEPWIDTH and _MAXIMUMADKEEPHEIGHT
            //hide it.
            if ((nodeFloodOpacity == CreativeInjecter._ADMARKERFLOODOPACITY) &&
                (nodeWidth != null) && (nodeHeight != null) &&
                (!creatives.hasCreativeWithDimensions(nodeWidth, nodeHeight))) {
                //Get the viewable width and height of the node minus the borders
                //Sometimes an ad element can have larger non-viewable dimensions than its parents
                let smallestParentNode = thisCreativeInjecter._getSmallestContainingParent(currentNode);
                let viewableWidth = ElementInfo.widthWithoutBorder(smallestParentNode);
                let viewableHeight = ElementInfo.heightWithoutBorder(smallestParentNode);
                //Remove the ad if it is bigger than the allowed 'keep' size
                if ((viewableWidth != null) && (viewableHeight != null) &&
                    (viewableWidth >= CreativeInjecter._MAXIMUMADKEEPWIDTH) &&
                    (viewableHeight >= CreativeInjecter._MAXIMUMADKEEPHEIGHT)) {
                    thisCreativeInjecter._hideElement(smallestParentNode);
                }
            }
            else if ((nodeZIndex != null) && ((nodeZIndex > 1) || (nodeZIndex < 0)) &&
                (nodeWidth != null) && (nodeHeight != null) &&
                (!creatives.hasCreativeWithDimensions(nodeWidth, nodeHeight)) &&
                (hideLargeFloatingElements)) {
                if ((nodeContentLength < 500) &&
                    ((nodeAlpha < 1) || (nodeZIndex >= highestZIndex))) {
                    let nodeScreenWidthPercentage = (nodeWidth / window.innerWidth);
                    let nodeScreenHeightPercentage = (nodeHeight / window.innerHeight);
                    if ((nodeScreenWidthPercentage > 0.96) && (nodeScreenHeightPercentage > 0.96)) {
                        thisCreativeInjecter._hideElement(currentNode);
                    }
                }
            }
        });
    }
    _getHighestZIndex() {
        let highestZIndex = 0;
        this._crawlDocumentHTMLElements(document, function (currentNode) {
            let nodeZIndex = Number(ElementInfo.zIndex(currentNode));
            let nodeWidth = ElementInfo.widthWithoutBorder(currentNode);
            let nodeHeight = ElementInfo.heightWithoutBorder(currentNode);
            let nodeScreenWidthPercentage = (nodeWidth / window.innerWidth);
            let nodeScreenHeightPercentage = (nodeHeight / window.innerHeight);
            if (nodeZIndex > highestZIndex) {
                if ((nodeScreenWidthPercentage > 0.96) && (nodeScreenHeightPercentage > 0.96)) {
                    if ((currentNode.offsetHeight > 0) && (currentNode.offsetWidth > 0)) {
                        highestZIndex = nodeZIndex;
                    }
                }
            }
        });
        return highestZIndex;
    }
    /**
     * Returns all elements on the page matching Creative sizes.
     *
     * The elements are returned in a map with the values as arrays
     * of HTMLElements.
     *
     * The items are labelled "markedAdElements", "unmarkedIFrames",
     * and "unmarkedElements", respectively. No individual element
     * exists in more than one array.
     *
     * @returns 					Map of arrays of elements matching Creative sizes (see description)
     * @memberof CreativeInjecter
     */
    _getPageElementsOfCreativeSizes() {
        //Initialize the sets to store the discovered ad elements
        let markedAdElements = new Set();
        let unmarkedIFrameElements = new Set();
        let unmarkedElementsOfCreativeSizes = new Set();
        //Crawl through the DOM and store all Creative size matching elements
        let thisCreativeInjecter = this; //For scope
        let creatives = this._creatives; //For scope
        this._crawlDocumentHTMLElements(document, function (currentNode) {
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
                if ((viewableWidth != null) && (viewableHeight != null) &&
                    (creatives.hasCreativeWithDimensions(viewableWidth, viewableHeight))) {
                    markedAdElements.add(smallestParentNode);
                }
            }
            else if ((currentNode.nodeName == "IFRAME") &&
                (nodeWidth != null) && (nodeHeight != null) &&
                (creatives.hasCreativeWithDimensions(nodeWidth, nodeHeight))) {
                unmarkedIFrameElements.add(currentNode);
            }
            else if ((nodeWidth != null) && (nodeHeight != null) &&
                (creatives.hasCreativeWithDimensions(nodeWidth, nodeHeight))) {
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
     * @param {HTMLElement} elementNode 	Node to hide
     * @returns
     * @memberof CreativeInjecter
     */
    _hideElement(elementNode) {
        //Verify the passed node exists and is an HTMLElement. If not, simply return.
        if ((elementNode == null) || (!ElementInfo.isHTMLElement(elementNode))) {
            return;
        }
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
     * @private
     * @param {HTMLElement} elementNode 	HTMLElement to check for an IFrame of the same size
     * @returns 							Topmost containing frame the same size of the node or null if none found
     * @memberof CreativeInjecter
     */
    _getHighestSameSizeContainingFrame(elementNode) {
        //Verify the passed node exists and is an HTMLElement. If not, simply return null.
        if ((elementNode == null) || (!ElementInfo.isHTMLElement(elementNode))) {
            return null;
        }
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
     * @private
     * @param {HTMLElement} startingNode 	Starting node of parents to crawl
     * @returns 							Smallest containing parent
     * @memberof CreativeInjecter
     */
    _getSmallestContainingParent(startingNode) {
        //Verify the passed node exists and is an HTMLElement. If not, simply return the argument.
        if ((startingNode == null) || (!ElementInfo.isHTMLElement(startingNode))) {
            return startingNode;
        }
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
        if ((startingNodeWidth != null) && (startingNodeHeight != null) &&
            (startingNodeWidth >= viewportWidth) && (startingNodeHeight >= viewportHeight)) {
            return startingNode;
        }
        //Crawl the node's parents and find the smallest one
        //Ignore anchors, objects, and nodes smaller than defined by
        //_SMALLNODEMINWIDTH and _SMALLNODEMINHEIGHT
        this._crawlParentHTMLElements(startingNode, function (currentNode) {
            //Get the current node's width and height
            let currentNodeWidth = ElementInfo.widthWithoutBorder(currentNode);
            let currentNodeHeight = ElementInfo.heightWithoutBorder(currentNode);
            //Make sure none of the properties are null
            if ((currentNodeWidth != null) && (currentNodeHeight != null) &&
                (smallestNodeWidth != null) && (smallestNodeHeight != null)) {
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
     * @private
     * @param {HTMLDocument} documentToCrawl 						The DOM document to crawl
     * @param {(nodeElement: HTMLElement) => void} nodeFunction 	Function to call on each HTMLElement node
     * @returns
     * @memberof CreativeInjecter
     */
    _crawlDocumentHTMLElements(documentToCrawl, nodeFunction) {
        //If the document does not exist, is not a HTMLDocument, or does not have a "body" property, exit the function
        if ((!documentToCrawl) || (!(document instanceof HTMLDocument)) || (!documentToCrawl.body)) {
            return;
        }
        //If the passed function is null or not a function, exit the function
        if ((!nodeFunction) || (!(nodeFunction instanceof Function))) {
            return;
        }
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
            catch (error) { } //Do nothing on error
        }
    }
    /**
     * Crawls the HTMLElement node's parents, including any frames, and calls the passed function on
     * each HTMLElement node.
     *
     * The function receives a single argument of the HTMLElement node. The passed function is not
     * applied to the starting node.
     *
     * @private
     * @param {HTMLElement} startingNode 							Starting node of parents to crawl
     * @param {(nodeElement: HTMLElement) => void} nodeFunction 	Function to call on each parent
     * @returns
     * @memberof CreativeInjecter
     */
    _crawlParentHTMLElements(startingNode, nodeFunction) {
        //Verify the passed node exists and is an HTMLElement. If not, simply return null.
        if ((startingNode == null) || (!ElementInfo.isHTMLElement(startingNode))) {
            return;
        }
        //If the passed function is null or not a function, exit the function
        if ((!nodeFunction) || (!(nodeFunction instanceof Function))) {
            return;
        }
        //Crawl through the node's parents
        let currentParentNode = startingNode.parentNode;
        while (currentParentNode) {
            //Apply the passed function on the node if it as HTMLElement
            if (ElementInfo.isHTMLElement(currentParentNode)) {
                nodeFunction(currentParentNode);
            }
            //If the current node has its own parent, set itself to the parent
            if (currentParentNode.parentNode) {
                currentParentNode = currentParentNode.parentNode;
            }
            else {
                currentParentNode = ElementInfo.getContainingFrame(currentParentNode);
            }
        }
    }
    /**
     * Sorts an array of Elements according to position from top-left to bottom-right
     *
     * @private
     * @param {Array<HTMLElement>} elements 	Elements to sort
     * @returns 								Sorted array of elements by position top-left to bottom-right
     * @memberof CreativeInjecter
     */
    _sortElementsByPosition(elements) {
        //If the argument is not an array, do nothing
        if (!Array.isArray(elements)) {
            return;
        }
        //Function that sorts array of HTMLElements from top-right screen position to bottom-left
        //If either element is not an HTMLElement, an error is thrown.
        let elementSortFunction = function (firstElement, secondElement) {
            //If either element is not an HTMLElement, throw an error
            if ((!ElementInfo.isHTMLElement(firstElement)) || (!ElementInfo.isHTMLElement(secondElement))) {
                throw "CreativeInjecter._sortElementsByPosition: array element not an HTMLElement";
            }
            //Get the element positions
            let firstElementX = ElementInfo.xPosition(firstElement);
            let firstElementY = ElementInfo.yPosition(firstElement);
            let secondElementX = ElementInfo.xPosition(secondElement);
            let secondElementY = ElementInfo.yPosition(secondElement);
            if ((firstElementX == null) || (firstElementY == null) ||
                (secondElementX == null) || (secondElementY == null)) {
                return 0;
            }
            //Calculate each elements position factor by adding its y position
            //to its x position. The x position is divided by 1000 in order
            //to decrease its importance compared to the y position factor.
            let firstPositionFactor = firstElementY + firstElementX / 1000;
            let secondPositionFactor = secondElementY + secondElementX / 1000;
            //Return the difference. If negative, the firstElement comes first, if positive, the second come first.
            return firstPositionFactor - secondPositionFactor;
        };
        //Sort the elements
        elements.sort(elementSortFunction);
    }
    /**
     * Sorts an array of AdSelectors according to its Element's position from top-left to bottom-right
     *
     * @private
     * @param {Array<HTMLElement>} elements 	Elements to sort
     * @returns 								Sorted array of AdSelectors according to its Element's top-left to bottom-right
     * @memberof CreativeInjecter
     */
    _sortAdSelectorsByPosition(adSelectors) {
        //If the argument is not an array, do nothing
        if (!Array.isArray(adSelectors)) {
            return;
        }
        //Function that sorts array of AdSelectors from top-right screen position to bottom-left
        //If either argument is not an AdSelector, an error is thrown.
        let adSelectorSortFunction = function (firstAdSelector, secondAdSelector) {
            //If either element is not an HTMLElement, throw an error
            if ((!(firstAdSelector instanceof AdSelector)) || (!(firstAdSelector instanceof AdSelector))) {
                throw "CreativeInjecter._sortAdSelectorsByPosition: array element not an AdSelector";
            }
            //Get the elements
            let firstElement = document.querySelector(firstAdSelector.selector());
            let secondElement = document.querySelector(secondAdSelector.selector());
            //If the first element is null but not the second, return a positive 1
            //to make the second element come first
            if ((firstElement == null) && (secondElement != null)) {
                return 1;
            }
            //If the first element is not null but the second is, return a negative -1
            //to make the first element come first
            if ((firstElement != null) && (secondElement == null)) {
                return -1;
            }
            //If both elements are null, return a 0 for no order change
            if ((firstElement == null) && (secondElement == null)) {
                return 0;
            }
            //Get the element positions
            let firstElementX = ElementInfo.xPosition(firstElement);
            let firstElementY = ElementInfo.yPosition(firstElement);
            let secondElementX = ElementInfo.xPosition(secondElement);
            let secondElementY = ElementInfo.yPosition(secondElement);
            if ((firstElementX == null) || (firstElementY == null) ||
                (secondElementX == null) || (secondElementY == null)) {
                return 0;
            }
            //Calculate each elements position factor by adding its y position
            //to its x position. The x position is divided by 1000 in order
            //to decrease its importance compared to the y position factor.
            let firstPositionFactor = firstElementY + (firstElementX / 1000);
            let secondPositionFactor = secondElementY + (secondElementX / 1000);
            //!!!------------------------------------------------------------------------!!!
            //On Desktop, some non-loaded fixed element GPT slots were registering a position of 0,0 even
            //though they were placed lower on the page. For now, if we are on a Desktop and the selector
            //has a position of 0,0 , sort it lower than the rest of the selectors.
            let browserViewportWidth = document.documentElement.clientWidth;
            if (browserViewportWidth > 450) {
                //If the first element's position is (0,0) but not the second, return a positive 1
                //to make the second element come first
                if ((firstPositionFactor == 0) && (secondPositionFactor != 0)) {
                    return 1;
                }
                //If the first element's position is not (0,0) but the second is, return a negative -1
                //to make the first element come first
                if ((firstPositionFactor != 0) && (secondPositionFactor == 0)) {
                    return -1;
                }
            }
            //Return the difference. If negative, the firstElement comes first, if positive, the second come first.
            return firstPositionFactor - secondPositionFactor;
        };
        //Sort the elements
        adSelectors.sort(adSelectorSortFunction);
    }
}
//---------------------------------------------------------------------------------------
//-------------------------------- Static Variables -------------------------------------
//---------------------------------------------------------------------------------------
//***************************** Private Static Variables ********************************
/**
 * The flood-opacity of ads marked by the AdMarker browser plugin
 *
 * @static
 * @memberof Log
 */
CreativeInjecter._ADMARKERFLOODOPACITY = '0.9898';
/**
 * Used by _getSmallestContainingParent. Any parent of a size smaller than
 * these minimums is ignored.
 *
 * @private
 * @static
 * @memberof CreativeInjecter
 */
CreativeInjecter._SMALLNODEMINWIDTH = 5;
/**
 * Used by _getSmallestContainingParent. Any parent of a size smaller than
 * these minimums is ignored.
 *
 * @private
 * @static
 * @memberof CreativeInjecter
 */
CreativeInjecter._SMALLNODEMINHEIGHT = 5;
/**
 * Any ads and iframes larger than these dimensions are removed
 *
 * @private
 * @static
 * @memberof CreativeInjecter
 */
CreativeInjecter._MAXIMUMADKEEPWIDTH = 971;
/**
 * Any ads and iframes larger than these dimensions are removed
 *
 * @private
 * @static
 * @memberof CreativeInjecter
 */
CreativeInjecter._MAXIMUMADKEEPHEIGHT = 971;
//window.onload = function() {
//Remove the scrollbars
//document.documentElement.style.overflow = 'hidden';
let creatives = [];
let injectionStartHeight = 0;
let hideLargeFloatingElements = true;
//creatives = [{id: '887', imageURL: 'http://s3.amazonaws.com/asr-development/creativeimages/65157428-d651-4a41-9ac4-7ed38913b6d3.png', width: 300, height: 50, priority: 3},{id: '884', imageURL: 'http://s3.amazonaws.com/asr-development/creativeimages/7342bc19-a3b7-473d-837f-4a6c3c14ccb3.png', width: 300, height: 250, priority: 0},{id: '888', imageURL: 'http://s3.amazonaws.com/asr-development/creativeimages/8a9640ea-e713-4f19-9262-5410b2df4482.png', width: 970, height: 250, priority: 4},{id: '885', imageURL: 'http://s3.amazonaws.com/asr-development/creativeimages/da6f1689-a606-45d2-8c9e-091e100eca14.png', width: 728, height: 90, priority: 1},{id: '889', imageURL: 'http://s3.amazonaws.com/asr-development/creativeimages/2941f395-f829-462f-8b99-bc240e510082.png', width: 320, height: 50, priority: 5},{id: '886', imageURL: 'http://s3.amazonaws.com/asr-development/creativeimages/9e60ee39-b7e5-4e77-82aa-5082dd56e83e.png', width: 300, height: 600, priority: 2},];
//INSERT CREATIVES OBJECT//
//Create the CreativesGroup and add each passed Creative to it
let allCreatives = new CreativeGroup();
for (let currentCreative of creatives) {
    allCreatives.addCreative((new Creative(currentCreative.id, currentCreative.imageURL, currentCreative.width, currentCreative.height, currentCreative.priority)));
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
let allSelectors = [];
if ((gptAdSelectors != null) && (gptAdSelectors.size > 0)) {
    for (let currentGPTSelector of gptAdSelectors) {
        allSelectors.push(currentGPTSelector);
    }
}
//Get any BingAds AdSelectors
let bingAdSelectors = BingAds.getSelectors();
if ((bingAdSelectors != null) && (bingAdSelectors.size > 0)) {
    for (let currentBingAdSelector of bingAdSelectors) {
        allSelectors.push(currentBingAdSelector);
    }
}
//Verify each selector points to an element then turn it into an AdSelector
for (let currentSelector of selectors) {
    let selectorElement = document.querySelector(currentSelector.selector);
    if (selectorElement) {
        allSelectors.push((new AdSelector(currentSelector.selector, currentSelector.hideIfNotReplaced)).addSizes(currentSelector.sizes));
    }
}
//INSERT EXCEPTION SCRIPT//
//Initialize the CreativeInjecter and inject the creatives
let injecter = new CreativeInjecter(allCreatives, allSelectors, injectionStartHeight, hideLargeFloatingElements);
injecter.injectCreativesIntoPage();
//Create the list of injected Creatives and their locations
//Format: {creativeID: {x: xPosition, y: yPosition}}
//Example: {"4ce6dca7-1d71-4e5b-9bfb-17a41190151a":{"x":312,"y":106},"1e678430-90b4-4c9f-ad81-739826d05c0e":{"x":942,"y":262}}
let injectedCreatives = allCreatives.getInjectedCreatives();
let injectedIDsAndLocations = {};
for (let [injectedCreative, location] of injectedCreatives) {
    injectedIDsAndLocations[injectedCreative.id()] = { 'x': location.x(), 'y': location.y() };
}
//Get the message log
Log.output("End of message log");
let messageLog = Log.getMessages();
//Return the injected creatives with their locations and any log messages
//Log.output(JSON.stringify(injectedIDsAndLocations));
//return JSON.stringify({ 'injectedCreatives': injectedIDsAndLocations, 'outputLog': messageLog });
