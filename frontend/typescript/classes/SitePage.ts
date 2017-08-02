/**
 * The SitePage class creates, controls, and returns information from its user interface
 * table row, which allows a user to choose a site section or enter a URL, set options
 * like 'mobile' or 'below the fold', and choose Creative type for a campaign page.
 * 
 * Upon creation, the SitePage inserts its interface table row into the table element
 * provided by the constructor. The row includes all the options needed for a user
 * to define a page to be run for the campaign.
 * 
 * The SitePage can receive a list of Creative and create AdShotBuilders representing the
 * user choices in its interface. It then returns an array of the AdShotBuilder JSON.
 * 
 * @class SitePage
 */
class SitePage {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Prefix used when inserting nodes into the DOM that have IDs. 
	 * 
	 * In use, this prefix comes before the SitePage instance ID which is then followed
	 * by a suffix describing the node.
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static readonly UNIQUEHTMLIDPREFIX = "sitePage-";

	/**
	 * ID suffix for the SitePage tabe row inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static readonly ROWSUFFIX = "-row";

	/**
	 * ID suffix for the SitePage URL Input/Select element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static readonly URLINPUTSUFFIX = "-URLInput";

	/**
	 * ID suffix for the SitePage "find story" checkbox element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static readonly FINDSTORYCHECKBOXSUFFIX = "-findStoryCheckbox";

	/**
	 * ID suffix for the SitePage mobile checkbox element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static readonly MOBILECHECKBOXSUFFIX = "-mobileCheckbox";

	/**
	 * ID suffix for the SitePage below-the-fold checkbox element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static readonly BELOWTHEFOLDCHECKBOXSUFFIX = "-belowTheFoldCheckbox";

	/**
	 * ID suffix for the SitePage all creative radio element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static readonly ALLCREATIVERADIOSUFFIX = "-allCreativeRadio";

	/**
	 * ID suffix for the SitePage individual creative radio element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static readonly INDIVIDUALCREATIVERADIOSUFFIX = "-individualCreativeRadio";

	/**
	 * ID suffix for the SitePage no creative radio element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static readonly NOCREATIVERADIOSUFFIX = "-noCreativeRadio";

	/**
	 * Tag name suffix for the SitePage creative selection radio elements inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static readonly CREATIVERADIOTAGNAMESUFFIX = "-creativeRadios";

	/**
	 * ID suffix for the SitePage remove button element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static readonly REMOVEBUTTONSUFFIX = "-removeButton";

	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Variables -------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Private Static Variables ********************************
	/**
	 * Internal incrementor used to generate unique IDs for each new SitePage instance
	 * 
	 * @private
	 * @static
	 * @memberof SitePage
	 */
	private static _idIncrementor = 0;

	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	 * Unique ID of the SitePage instance
	 * 
	 * @private
	 * @type {number}
	 * @memberof SitePage
	 */
	private readonly _id: number;

	/**
	 * ID of the Site the SitePage belongs to
	 * 
	 * @private
	 * @type {number}
	 * @memberof SitePage
	 */
	private readonly _parentSiteID: number;

	/**
	 * Associative array object of menu item labels and urls. 
	 * 
	 * Example: {"Sports": "mydomain.com/sports", "News": "mydomain.com/news"}
	 * 
	 * @private
	 * @type {{[key: string]: string}}
	 * @memberof SitePage
	 */
	private readonly _menuItems: Map<string, string> | undefined;

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Creates and instance of a SitePage and inserts its table row into the passed table element.
	 * 
	 * @param {number} parentSiteID   				ID of the SitePage's parent site
	 * @param {HTMLTableElement} pagesTable 		Table element to insert the SitePage table row into
	 * @param {Map<string, string>} menuItems 		Menu items for the Site as [Menu Title => URL] (from MenuGrabber)
	 * @memberof SitePage
	 */
	constructor(parentSiteID: number, pagesTable: HTMLTableElement, menuItems?: Map<string, string>) {

		//Create a new unique ID for the instance
		this._id = this.getUniqueID();

		//Store the passed Site ID and menu items
		this._parentSiteID = parentSiteID;
		this._menuItems = menuItems;

		//Insert the new page row into the table
		let newPageRow =  pagesTable.insertRow(pagesTable.rows.length);
		newPageRow.id = this.rowID();
		newPageRow.innerHTML = this.getTableRowHTML();

		//Add the delete listener to the delete button
		let currentSitePageID = this._id;
		let removeButton = <HTMLInputElement> base.nodeFromID(this.removeButtonID());
		removeButton.addEventListener('click', function() {Site.removeSitePage(parentSiteID, currentSitePageID);}, false);
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************		
	/**
	 * Removes the SitePage's table row from the DOM
	 * 
	 * @memberof SitePage
	 */
	public removeFromDOM(): void {

		//Get the row element and remove it from the DOM
		let sitePageRow = <HTMLTableRowElement> base.nodeFromID(this.rowID());
		if (sitePageRow.parentNode) {sitePageRow.parentNode.removeChild(sitePageRow);}
	}

	/**
	 * Returns an array of AdShotBuilder JSON strings representing the user
	 * selections in the SitePage table row
	 * 
	 * @param {Map<number, Creative>} creatives 	Creatives to use
	 * @returns {string[]} 
	 * @memberof SitePage
	 */
	public getAdShotBuilderJSON(creatives: Map<number, Creative>): string[] {

		//Get the requested URL. If it is blank, return an empty array
		let requestedURL = (<HTMLInputElement> base.nodeFromID(this.urlInputID())).value;
		if (requestedURL == "") {return [];}

		//Get the StoryFinder status if the box exists (does not exist for manual urls)
		let useStoryFinder = (this._menuItems) ? base.isChecked(this.findStoryCheckboxID()) : false;

		//Get the mobile and below-the-fold selections
		let useMobile = base.isChecked(this.mobileCheckboxID());
		let useBelowTheFold = base.isChecked(this.belowTheFoldCheckboxID());

		//Create AdShotBuilders depending on the user's creative type selection
		//If all Creatives were selected, create one AdShotBuilder with all the Creative at once
		let adShots: string[] = [];
		if (base.isChecked(this.allCreativeRadioID())) {

			//Create the AdShotBuilder and add all the Creative to it
			let newAdShotBuilder = new AdShotBuilder(requestedURL, useStoryFinder, useMobile, useBelowTheFold);
			for (let currentCreative of creatives.values()) {

				//Add any creative that are finished processing
				if (currentCreative.status() == Creative.FINISHED) {
					newAdShotBuilder.addCreative(currentCreative);
				}
			}

			//Add the JSON string to the final array
			adShots.push(newAdShotBuilder.toJSON());
		}

		//If individual was chosen, create AdShotBuilders for each individual Creative
		else if (base.isChecked(this.individualCreativeRadioID())) {

			//Loop through each Creative
			for (let currentCreative of creatives.values()) {

				//Only use Creative that are FINISHED
				if (currentCreative.status() == Creative.FINISHED) { 

					//Create the AdShotBuilder with the single Creative and add its JSON to the final array
					let newAdShotBuilder = new AdShotBuilder(requestedURL, useStoryFinder, useMobile, useBelowTheFold);
					newAdShotBuilder.addCreative(currentCreative);
					adShots.push(newAdShotBuilder.toJSON());
				}
			}
		}

		//If 'none' was selected, do an AdShot without any Creative
		else if (base.isChecked(this.noCreativeRadioID())) {

			//Create the AdShotBuilder and add its JSON to the final array
			let newAdShotBuilder = new AdShotBuilder(requestedURL, useStoryFinder, useMobile, useBelowTheFold);
			adShots.push(newAdShotBuilder.toJSON());
		}

		//Return the AdShotBuilder JSON
		return adShots;
	}

	//********************************* Private Methods **************************************		
	/**
	 * Returns a unique ID for a new SitePage instance to use.
	 * 
	 * @private
	 * @returns {number} Unique ID for a new SitePage instance to use
	 * @memberof Site
	 */
	private getUniqueID(): number {
		SitePage._idIncrementor++;
		return SitePage._idIncrementor;
	}

	
	/**
	 * Creates and returns the HTML for inside the SitePage table row.
	 * 
	 * If menu items exist in the instance, a select menu will be generated for the user
	 * to use a menu section from. If not, a text field will be generated for the user
	 * to type the URL into manually.
	 * 
	 * @private
	 * @returns {string} 	HTML for inside the SitePage table row
	 * @memberof SitePage
	 */
	private getTableRowHTML(): string {

		//If menu items were passed, display them as a select element with the story finder checkbox 
		let pageRowHTML = "";
		if (this._menuItems) {
			pageRowHTML += "<td><select id='" + this.urlInputID() + "'>" + this.getMenuItemsAsSelectOptions() + "</select></td>";
			pageRowHTML += "<td><label><input type='checkbox' id='" + this.findStoryCheckboxID() + "' value='1'>Story</label></td>";

		}

		//Otherwise, show a blank URL text field
		else {
			pageRowHTML += "<td colspan='2' class='pageURLTitle'>Page URL: ";
			pageRowHTML += "<input type='text' id='" + this.urlInputID() + "'></td>";
		}

		//Add the mobile and below-the-fold checkboxes
		pageRowHTML += "<td><label><input type='checkbox' id='" + this.mobileCheckboxID() + "' value='1'>Mobile</label></td>";
		pageRowHTML += "<td style='display:none;'><label><input type='checkbox' id='" + this.belowTheFoldCheckboxID() + "' value='1'>Below Fold</label></td>";

		//Add the radio buttons for choosing all/inidiviual/no creative
		pageRowHTML += "<td><label><input type='radio' id='" + this.allCreativeRadioID() + "'  name='" + this.creativeRadioTagName() + "' value='all' checked>All Creative</label>";
		pageRowHTML += "    <label><input type='radio' id='" + this.individualCreativeRadioID() + "'  name='" + this.creativeRadioTagName() + "' value='individual'>Individual Creative Screenshots</label>";
		pageRowHTML += "    <label><input type='radio' id='" + this.noCreativeRadioID() + "'  name='" + this.creativeRadioTagName() + "' value='none'>No Creative</label></td>";

		//Add the delete button used to remove the SitePage and its row
		pageRowHTML += "<td><input class='button-tiny' type='button' id='" + this.removeButtonID() + "' value='Remove'></td>";

		//Return the finished row HTML
		return pageRowHTML;
	}


	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	 * Returns the ID of the SitePage instance
	 * 
	 * @returns {number} 	ID of the SitePage instance
	 * @memberof SitePage
	 */
	public id(): number {return this._id;}

	/**
	 * Returns the unique HTML Node ID to be used when inserting DOM nodes with IDs. Before
	 * insertion, this string should be given a suffix describing the node like '-findStoryCheckbox'
	 * 
	 * @returns {string} 	Unique HTML Node ID to be used when inserting DOM nodes with IDs
	 * @memberof SitePage
	 */
	public uniqueHTMLID(): string {return SitePage.UNIQUEHTMLIDPREFIX + this._id;}

	/**
	 * Returns DOM node ID for the SitePage table row element
	 * 
	 * @returns {string} 	DOM node ID for the SitePage table row element
	 * @memberof SitePage
	 */
	public rowID(): string {return this.uniqueHTMLID() + SitePage.ROWSUFFIX;}

	/**
	 * Returns DOM node ID for the SitePage URL Input/Select element
	 * 
	 * @returns {string} 	DOM node ID for the SitePage URL Input/Select element
	 * @memberof SitePage
	 */
	public urlInputID(): string {return this.uniqueHTMLID() + SitePage.URLINPUTSUFFIX;}

	/**
	 * Returns DOM node ID for the SitePage "find story" checkbox element
	 * 
	 * @returns {string} 	DOM node ID for the SitePage "find story" checkbox element
	 * @memberof SitePage
	 */
	public findStoryCheckboxID(): string {return this.uniqueHTMLID() + SitePage.FINDSTORYCHECKBOXSUFFIX;}

	/**
	 * Returns DOM node ID for the SitePage mobile checkbox element
	 * 
	 * @returns {string} 	DOM node ID for the SitePage mobile checkbox element
	 * @memberof SitePage
	 */
	public mobileCheckboxID(): string {return this.uniqueHTMLID() + SitePage.MOBILECHECKBOXSUFFIX;}

	/**
	 * Returns DOM node ID for the SitePage below-the-fold checkbox element
	 * 
	 * @returns {string} 	DOM node ID for the SitePage below-the-fold checkbox element
	 * @memberof SitePage
	 */
	public belowTheFoldCheckboxID(): string {return this.uniqueHTMLID() + SitePage.BELOWTHEFOLDCHECKBOXSUFFIX;}

	/**
	 * Returns DOM node ID for the SitePage all creative radio element
	 * 
	 * @returns {string} 	DOM node ID for the SitePage all creative radio element
	 * @memberof SitePage
	 */
	public allCreativeRadioID(): string {return this.uniqueHTMLID() + SitePage.ALLCREATIVERADIOSUFFIX;}

	/**
	 * Returns DOM node ID for the SitePage individual creative radio element
	 * 
	 * @returns {string} 	DOM node ID for the SitePage individual creative radio element
	 * @memberof SitePage
	 */
	public individualCreativeRadioID(): string {return this.uniqueHTMLID() + SitePage.INDIVIDUALCREATIVERADIOSUFFIX;}

	/**
	 * Returns DOM node ID for the SitePage no creative radio element
	 * 
	 * @returns {string} 	DOM node ID for the SitePage no creative radio element
	 * @memberof SitePage
	 */
	public noCreativeRadioID(): string {return this.uniqueHTMLID() + SitePage.NOCREATIVERADIOSUFFIX;}

	/**
	 * Returns DOM node tag name for the SitePage creative radio elements
	 * 
	 * @returns {string} 	DOM node tag name for the SitePage creative radio elements
	 * @memberof SitePage
	 */
	public creativeRadioTagName(): string {return this.uniqueHTMLID() + SitePage.CREATIVERADIOTAGNAMESUFFIX;}

	/**
	 * Returns DOM node ID for the SitePage remove button element
	 * 
	 * @returns {string} 	DOM node ID for the SitePage remove button element
	 * @memberof SitePage
	 */
	public removeButtonID(): string {return this.uniqueHTMLID() + SitePage.REMOVEBUTTONSUFFIX;}

	/**
	 * Returns the current selected or inputed URL for the page
	 * 
	 * If the menu items select menu is present, this will be the value
	 * of the currently chosen option. If not, this will be the value
	 * entered into the URL input text field.
	 * 
	 * @private
	 * @returns {string} 
	 * @memberof SitePage
	 */
	public url(): string {return (<HTMLInputElement> base.nodeFromID(this.urlInputID())).value;}

	/**
	 * Returns an HTML string of select element <option> tags representing the SitePage's
	 * menu items. The option item is the section title and the value is the section URL.
	 * 
	 * @private
	 * @returns {string} 
	 * @memberof SitePage
	 */
	private getMenuItemsAsSelectOptions(): string {

		//If there are no menu items, return an empty string
		if (!this._menuItems) {return "";}

		//Turn the menu items into Select element options and return them
		let menuItemOptions = ""; //"<option value='" + domain + "/'>Main Page</option>";
		for (let [title, url] of this._menuItems) {
			menuItemOptions += "<option value='" + url + "'>" + title + "</option>";
		}	
		return menuItemOptions;
	}

	
}