/**
 * The Site class creates, controls, and returns information from its DOM user interface,
 * which allows the user to add campaign pages for the Site's specific domain.
 * 
 * More specifically, on the main ASR app page, in the section entitled "Campaign Pages",
 * a Site is added to the div underneath the "Publisher Site" text field when the user
 * successfully adds the new domain to the campaign.
 * 
 * Upon creation, the Site is given its domain and a Map of menu items [Section Title => URL]
 * from the MenuGrabber (when successful). It inserts its own interface div in "Campaign Pages"
 * with buttons for the user to add pages for the domain (SitePages).
 * 
 * @class Site
 */
class Site {

	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * The mimimum number of menu items passed for the "Add Site Section" 
	 * button to show.
	 * 
	 * If the MenuGrabber only found one or two menu items, we do not want
	 * to allow the user to use the site section select menu.
	 * 
	 * @private
	 * @static
	 * @memberof Site
	 */
	private static readonly MINIMUMMENUITEMSFORSELECTION = 4;
	
	/**
	 * THe ID of the div where Sites need to append their user interfaces
	 * inside
	 * 
	 * @private
	 * @static
	 * @memberof Site
	 */
	private static readonly PUBLISHERSITESDIVID = "publisherSitesDiv";

	/**
	 * Prefix used when inserting nodes into the DOM that have IDs. 
	 * 
	 * In use, this prefix comes before the Site instance ID which is then followed
	 * by a suffix describing the node.
	 * 
	 * @private
	 * @static
	 * @memberof Site
	 */
	private static readonly UNIQUEHTMLIDPREFIX = "site-";

	/**
	 * ID suffix for the Site user interface Div element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof Site
	 */
	private static readonly  INTERFACEDIVSUFFIX = "-interfaceDiv";

	/**
	 * ID suffix for the Site add site section button element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof Site
	 */
	private static readonly  ADDSITESECTIONBUTTONSUFFIX = "-addSiteSectionButton";

	/**
	 * ID suffix for the Site add URL button element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof Site
	 */
	private static readonly  ADDURLBUTTONSUFFIX = "-addURLButton";

	/**
	 * ID suffix for the Site user interface pages table element inserted into the DOM
	 * 
	 * @private
	 * @static
	 * @memberof Site
	 */
	private static readonly  PAGESTABLESUFFIX = "-pagesTable";

	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Variables -------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Private Static Variables ********************************
	/**
	 * Internal incrementor used to generate unique IDs for each new Site instance
	 * 
	 * @private
	 * @static
	 * @memberof Site
	 */
	private static _idIncrementor = 0;

	/**
	 * Internal list of Site instances. Used to determine where and how to create new page 
	 * rows and delete Site DOM interfaces. 
	 * 
	 * @private
	 * @static
	 * @memberof Site
	 */
	private static _siteMap = new Map<number, Site>();


	//---------------------------------------------------------------------------------------
	//--------------------------------- Static Methods --------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Public Static Methods ***********************************
	/**
	 * Adds a SitePage to the specified Site.
	 * 
	 * If TRUE is passed as the 'useMenuItems' argument, the SitePage will show
	 * a select menu of the menu items for the user to choose from. On FALSE,
	 * a text field to input a URL will be displayed instead.
	 * 
	 * @static
	 * @param {number} siteID 					ID of the Site to add the SitePage to
	 * @param {boolean} [useMenuItems=true] 	TRUE to show menu item select menu, FALSE for URL input field
	 * @memberof Site
	 */
	public static addSitePage(siteID: number, useMenuItems = true): void {

		//If a site with the ID exists, use it to add a new section row
		let requestedSite = Site._siteMap.get(siteID);
		if (requestedSite) {requestedSite.addSitePageRow(useMenuItems);}
	}

	/**
	 * Removes the specified SitePage from its Site and removes it
	 * from the DOM.
	 * 
	 * @static
	 * @param {number} siteID 		ID of the Site the SitePage to remove belongs to
	 * @param {number} sitePageID 	ID of the SitePage to remove from Site and DOM
	 * @memberof Site
	 */
	public static removeSitePage(siteID: number, sitePageID: number): void {

		//If a site with the ID exists, use it to add delete the page
		let requestedSite = Site._siteMap.get(siteID);
		if (requestedSite) {requestedSite.removeSitePageRow(sitePageID);}
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	 * Unique ID of the Site instance
	 * 
	 * @private
	 * @type {number}
	 * @memberof Site
	 */
	private readonly _id: number;

	/**
	 * The domain of the publisher site
	 * 
	 * @private
	 * @type {string}
	 * @memberof Site
	 */
	private readonly _domain: string;

	/**
	 * Associative array object of menu item labels and urls. 
	 * 
	 * Example: {"Sports": "mydomain.com/sports", "News": "mydomain.com/news"}
	 * 
	 * @private
	 * @type {{[key: string]: string}}
	 * @memberof Site
	 */
	private readonly _menuItems: Map<string, string>;

	/**
	 * Map of all SitePages generated by the Site instance
	 * 
	 * @private
	 * @type {Map<number, SitePage>}
	 * @memberof Site
	 */
	private readonly _pages : Map<number, SitePage> = new Map();

	//---------------------------------------------------------------------------------------
	//------------------------ Constructors/Copiers/Destructors -----------------------------
	//---------------------------------------------------------------------------------------
	/**
	 * Creates a new instance of a Site with the passed domain and menu items and inserts
	 * its user interface into the DOM.
	 * 
	 * If the instance's menu items map has less than MINIMUMMENUITEMSFORSELECTION
	 * entries, the "Add Site Section" will not be shown and a warning message will be
	 * displayed
	 * 
	 * @param {string} siteDomain 					Domain of the Site
	 * @param {Map<string, string>} menuItems 		Menu items for the Site as [Menu Title => URL] (from MenuGrabber)
	 * @memberof Site
	 */
	constructor(siteDomain: string, menuItems: Map<string, string>) {

		//Verify the site domain is a non-empty string
		if ((typeof siteDomain !== 'string') || (siteDomain == "")) {
			throw "Site.constructor: siteDomain must be a non-empty string";
		}

		//Create a new unique ID for the instance
		this._id = this.getUniqueID();

		//Store the passed domain and menu items
		this._domain = siteDomain;
		this._menuItems = menuItems;

		//Insert the interface into the DOM
		let publisherSitesDiv = <HTMLDivElement> base.nodeFromID(Site.PUBLISHERSITESDIVID);
		publisherSitesDiv.appendChild(this.getUserInterfaceDiv());

		//Add the listener to the Add Section button if it exists
		let currentID = this._id;
		let addSiteSectionButton = <HTMLInputElement> base.nodeFromID(this.addSiteSectionButtonID());
		if (addSiteSectionButton) {
			addSiteSectionButton.addEventListener('click', function() {Site.addSitePage(currentID);}, false);
		}

		//Add the listener to the add URL button
		let addURLButton = <HTMLInputElement> base.nodeFromID(this.addURLButtonID());
		addURLButton.addEventListener('click', function() {Site.addSitePage(currentID, false);}, false);

		//Add this instance to the static map of Sites
		Site._siteMap.set(this._id, this);
	}

	//---------------------------------------------------------------------------------------
	//------------------------------- Modification Methods ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Methods **************************************
	/**
	 * Returns an array of AdShotBuilder JSON strings compiled from all of the instance's
	 * SitePages.
	 * 
	 * @param {Map<number, Creative>} creatives 	Creatives to use
	 * @returns {string[]} 
	 * @memberof Site
	 */
	public getAdShotBuilderJSON(creatives: Map<number, Creative>): string[] {

		//Loop through the pages and add each one's JSON to the array
		let adShots: string[] = [];
		for (let currentPage of this._pages.values()) {
			adShots = adShots.concat(currentPage.getAdShotBuilderJSON(creatives));
		}
		return adShots;
	}		
	//********************************* Private Methods *************************************		
	/**
	 * Returns a unique ID for a new Site instance to use.
	 * 
	 * @private
	 * @returns {number} Unique ID for a new Site instance to use
	 * @memberof Site
	 */
	private getUniqueID(): number {
		Site._idIncrementor++;
		return Site._idIncrementor;
	}

	/**
	 * Builds and returns the Site user interface Div element to insert into the DOM.
	 * 
	 * The returned Div includes buttons for adding site sections and manual URLs as
	 * well as an empty pages table.
	 * 
	 * NOTE: Listeners are not added to the buttons (Must be done after added to DOM)
	 * 
	 * @private
	 * @returns {HTMLDivElement}	Fully constructed user interface Div element for the Site
	 * @memberof Site
	 */
	private getUserInterfaceDiv(): HTMLDivElement {

		//Put the ID in a local variable for listener referencing
		let currentSiteID = this._id;

		//Create the containing div
		let interfaceDiv = document.createElement("div");
		interfaceDiv.id = this.interfaceDivID();
		interfaceDiv.className = "siteDiv";
		interfaceDiv.innerHTML = this.getUserInterfaceDivHTML();

		//Return the final constructed div
		return interfaceDiv;
	}

	/**
	 * Builds and returns the instance's user interface div HTML. 
	 * 
	 * This strucuture includes buttons to add SitePages and the table
	 * to insert them into.
	 * 
	 * If the instance's menu items map has less than MINIMUMMENUITEMSFORSELECTION
	 * entries, the "Add Site Section" will not be shown and a warning message will be
	 * displayed
	 * 
	 * @private
	 * @returns {string} 
	 * @memberof Site
	 */
	private getUserInterfaceDivHTML(): string {

		//Determine if enough menu items are present to allow the add section button
		let showAddSectionButton = (this._menuItems.size >= Site.MINIMUMMENUITEMSFORSELECTION);

		//Build the HTML
		let interfaceHTML =  `
			<div class="siteHeaderDiv">
				<div class="siteTitleDiv">
					<a href="http://` + this._domain + `/" target="_blank">` + this._domain + `</a>
				</div>
				<div class="siteButtonsDiv">`;
		if (showAddSectionButton) {interfaceHTML += 
					`<input id="` + this.addSiteSectionButtonID() + `" class="button-tiny addSiteSectionButton" value="Add Site Section" type="button">`;
		}
		if (!showAddSectionButton) {interfaceHTML += 
					`<span class="sectionsNotAvailableSpan">Sections not available for this site</span>`;
		}
		interfaceHTML +=			
					`<input id="` + this.addURLButtonID() + `" class="button-tiny addURLButton" value="Add URL" type="button">
				</div>
			</div>
			<table id="` + this.pagesTableID() + `" class="pagesTable"></table>`;

		//Return the built HTML
		return interfaceHTML;
	}

	/**
	 * Adds a SitePage row to the Site and its pages table
	 * 
	 * If TRUE is passed as the 'useMenuItems' argument, the SitePage will show
	 * a select menu of the menu items for the user to choose from. On FALSE,
	 * a text field to input a URL will be displayed instead.
	 * 
	 * NOTE: Made public for quickly adding and testing many pages at once
	 * 
	 * @private
	 * @param {boolean} [useMenuItems=true] TRUE to show list of menu items, FALSE for URL input
	 * @memberof Site
	 */
	public addSitePageRow(useMenuItems = true): void {

		//Get the Site's pages table to add the row to
		let pagesTable = <HTMLTableElement> base.nodeFromID(this.pagesTableID());

		//If the new row should display the menu items in a select element, pass them
		let newSitePage: SitePage;
		if (useMenuItems) {
			newSitePage = new SitePage(this._id, pagesTable, this._menuItems);
		}

		//Otherwise, create a page with a URL text field
		else {
			newSitePage = new SitePage(this._id, pagesTable);
		}

		//Store the newly created SitePage
		this._pages.set(newSitePage.id(), newSitePage);
	}

	/**
	 * Removes the specified SitePage from the Site and calls
	 * the SitePage's removeFromDOM(...) command.
	 * 
	 * @private
	 * @param {number} sitePageID 	ID of the SitePage to remove
	 * @memberof Site
	 */
	private removeSitePageRow(sitePageID: number): void {

		//If the SitePage exists, remove it from the DOM and the stored SitePages map
		if (this._pages.has(sitePageID)) {

			//Remove it from the DOM
			let sitePageToRemove = this._pages.get(sitePageID);
			if (sitePageToRemove) {
				sitePageToRemove.removeFromDOM();
				this._pages.delete(sitePageID);
			}
		}
	}

	//---------------------------------------------------------------------------------------
	//----------------------------------- Accessors -----------------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	 * Returns the unique HTML Node ID to be used when inserting DOM nodes with IDs. Before
	 * insertion, this string should be given a suffix describing the node like '-interfaceDiv'
	 * 
	 * @returns {string} 	Unique HTML Node ID to be used when inserting DOM nodes with IDs
	 * @memberof Site
	 */
	public uniqueHTMLID(): string {return Site.UNIQUEHTMLIDPREFIX + this._id;}

	/**
	 * Returns DOM node ID for the Site instance's user interface Div element
	 * 
	 * @returns {string} 	DOM node ID for the Site instance's user interface Div element
	 * @memberof Site
	 */
	public interfaceDivID(): string {return this.uniqueHTMLID() + Site.INTERFACEDIVSUFFIX;}

	/**
	 * Returns DOM node ID for the Site instance's add site section button element
	 * 
	 * @returns {string} 	DOM node ID for the Site add site section button element
	 * @memberof Site
	 */
	public addSiteSectionButtonID(): string {return this.uniqueHTMLID() + Site.ADDSITESECTIONBUTTONSUFFIX;}

	/**
	 * Returns DOM node ID for the Site instance's add URL button element
	 * 
	 * @returns {string} 	DOM node ID for the Site add URL button element
	 * @memberof Site
	 */
	public addURLButtonID(): string {return this.uniqueHTMLID() + Site.ADDURLBUTTONSUFFIX;}

	/**
	 * Returns DOM node ID for the Site instance's pages table element
	 * 
	 * @returns {string} 	DOM node ID for the Site instance's pages table element
	 * @memberof Site
	 */
	public pagesTableID(): string {return this.uniqueHTMLID() + Site.PAGESTABLESUFFIX;}

	/**
	 * Returns the number of SitePages associated with the Site instance
	 * 
	 * @returns {number} 	Number of SitePages associated with the Site instance
	 * @memberof Site	
	 */
	public pageCount(): number {return this._pages.size;}

	/**
	 * Returns the sites menu items (title => URL)
	 * 
	 * @returns {Map<string, string>} The sites menu items (title => URL)
	 * @memberof Site	
	 */
	public menuItems(): Map<string, string> {return this._menuItems;}
	
	/**
	 * Returns TRUE if any of the instance's SitePages have an empty URL field
	 * and FALSE otherwise.
	 * 
	 * In practice, this only pertains to SitePages with manual URL input
	 * text fields.
	 * 
	 * @returns {boolean} 
	 * @memberof Site
	 */
	public hasEmptyURLField(): boolean {

		//Loop through the pages and return true if any have an empty URL value
		for (let currentPage of this._pages.values()) {
			if (currentPage.url() == "") {return true;}
		}

		//Otherwise, return false that no empty URLs were found
		return false;
	}
}