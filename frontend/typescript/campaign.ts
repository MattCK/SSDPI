/**
* Contains the Campaign object (campaign) that checks for the results of a processed campaign and displays them.
*
* @package AdShotRunner
* @subpackage JavaScript
*/

/**
* The AdShotRunner object that checks for the results of a processed campaign and displays them
*/
namespace campaign {

	export let uuid = '';								//Stores the UUID for the campaign
	export let li = false;								//Stores whether or not user is logged in. Abbr. used for obfuscation.
	const _getCampaignURL = 'getCampaign.php';			//URL of request page used to retrieve results of campaign
	const _QUEUETIMEOUT = 5000;							//Timeout between requests to see if job is no longer queued (ms)


	/**
	* Requests the results for the passed campaign. If the job is still queued, it runs itself again in the time
	* alotted by the _QUEUETIMEOUT member variable. If the job is finished, it displays the results including
	* customer name, site domain, campaign date, link to powerpoint, and a list of images of the screenshots.
	*/
	export function getResults() {

		//Create the callback function that will show the table
		let onSuccessCallback = function(serverResponse: ServerResponse) {

			//If the campaign was successfully retrieved, display it or wait for it to finish
			if (serverResponse.success) {
				
				//Get the Campaign
				let currentCampaign = new Campaign(serverResponse.data.campaignJSON);

				//If there is an error, show the error div with the customer name and error date
				if (currentCampaign.status() == Campaign.ERROR) {

					base.nodeFromID("errorCustomerNameSpan").innerHTML = currentCampaign.customerName();
					base.nodeFromID("errorDateSpan").innerHTML = getFormattedDate(currentCampaign.errorTimestamp());
					base.hide("campaignSubmittedDiv");
					base.show("campaignErrorDiv");
				}

				//If it is not finished, show the status and set the refresh timeout
				else if (currentCampaign.status() != Campaign.FINISHED) {

					//Set the customer info
					base.nodeFromID("submittedCustomerNameSpan").innerHTML = currentCampaign.customerName();
					base.nodeFromID("createdDateSpan").innerHTML = getFormattedDate(currentCampaign.createdTimestamp());
					
					//Set the status
					switch(currentCampaign.status()) {

						case Campaign.CREATED: base.nodeFromID("campaignStatusSpan").innerHTML = "Created"; break;
						case Campaign.READY: base.nodeFromID("campaignStatusSpan").innerHTML = "Ready for Processing"; break;
						case Campaign.QUEUED: base.nodeFromID("campaignStatusSpan").innerHTML = "Queued"; break;
						case Campaign.PROCESSING: base.nodeFromID("campaignStatusSpan").innerHTML = "Processing"; break;	
					}

					//Check the Campaign status again in the set timeout period
					setTimeout(campaign.getResults, _QUEUETIMEOUT);
				}

				//Otherwise, show the results
				else {

					//Set the campaign details
					base.nodeFromID("customerSpan").innerHTML = currentCampaign.customerName();
					base.nodeFromID("dateSpan").innerHTML = getFormattedDate(currentCampaign.finishedTimestamp());

					//Set the powerpoint link
					(<HTMLAnchorElement> base.nodeFromID("powerPointLink")).href = currentCampaign.powerPointURL();

					//Build the finished screenshot table rows and insert them into the page
					let imageTableRows = "";
					let finishedScreenshotCount = 0;
					for (let currentAdShot of currentCampaign.adShots()) {
						if (currentAdShot.status() == AdShot.FINISHED) {
							imageTableRows += "<tr><td><a href='" + currentAdShot.finalURL() + "' target='_blank'>" + currentAdShot.finalURL() + "</a><br><br>";
							imageTableRows += '<img style="max-width: 600px;" src="' + currentAdShot.imageURL() + '" /></td></tr>';
							++finishedScreenshotCount;
						}
					}
					base.nodeFromID("screenshotsTable").innerHTML = imageTableRows;
					base.nodeFromID("screenshotCountSpan").innerHTML = finishedScreenshotCount.toString();

					//Hide the campaign submitted div and show the results div
					base.hide("campaignSubmittedDiv");
					base.show("campaignResultsDiv");

					//If the user is logged into the system, show possible errors
					if (campaign.li) {

						//If there were any AdShots with errors (not including creative not injected), show them
						for (let currentAdShot of currentCampaign.adShots()) {
							if ((currentAdShot.status() == AdShot.ERROR) && 
								(currentAdShot.errorMessage() != AdShot.CREATIVENOTINJECTED)) {

								//Add the problem AdShot to the table
								let issuesTable = <HTMLTableElement> base.nodeFromID("adShotIssuesTable");
								let newPageRow =  issuesTable.insertRow(issuesTable.rows.length);
								newPageRow.innerHTML = getIssueRowCells(currentAdShot);
								
								//Show the issues Div
								base.show("adShotIssuesDiv");
							}
						}

						//If there were any AdShots with no Creative injected, show them as unused
						let unusedAdShotsCount = 0;
						for (let currentAdShot of currentCampaign.adShots()) {
							if ((currentAdShot.status() == AdShot.ERROR) && 
								(currentAdShot.errorMessage() == AdShot.CREATIVENOTINJECTED)) {

								//Add the problem AdShot to the unused table
								let unusedTable = <HTMLTableElement> base.nodeFromID("unusedAdShotsTable");
								let newPageRow =  unusedTable.insertRow(unusedTable.rows.length);
								newPageRow.innerHTML = getUnusedRowCells(currentAdShot);
								
								//Show the issues Div
								base.show("unusedAdShotsDiv");
								++unusedAdShotsCount;
								base.nodeFromID("unusedAdShotCountSpan").innerHTML = unusedAdShotsCount.toString();
							}
						}
					}
				}
			}
						
			//If failure, show us the message returned from the server and focus on the selected element if returned. Also, re-enable the submit button.
			else {
				alert("Could not retrieve campaign information.");
				console.log("error: " + serverResponse);
			}
		}

		//If there was a problem contacting the server or getting malformed response, simply re-request the results
		let onFailureCallback = function() {
			console.log("Error getting campaign data");
			setTimeout(campaign.getResults, _QUEUETIMEOUT);
		}
		
		//Make the request
		base.asyncRequest(_getCampaignURL, 'uuid=' + campaign.uuid, onSuccessCallback, onFailureCallback);
	}

	function getIssueRowCells(problemAdShot: AdShot): string {

		//Get the Creative sizes
		let creativeSizes = "";
		for (let currentCreative of problemAdShot.creatives()) {

			if (creativeSizes != "") {creativeSizes += ", ";}
			creativeSizes += currentCreative.width() + "x" + currentCreative.height();
		}

		//Get the cell text
		let deviceText = (problemAdShot.mobile()) ? "Mobile" : "Desktop";
		let storyFinderText = (problemAdShot.storyFinder()) ? "✓" : "";
		let belowFoldText = (problemAdShot.belowTheFold()) ? "✓" : "";
		
		//Return the row cells
		return 	"<td>" + problemAdShot.requestedURL() + "</td>" +
				"<td>" + deviceText + "</td>" +
				"<td>" + storyFinderText + "</td>" +
				"<td>" + belowFoldText + "</td>" +
				"<td>" + creativeSizes + "</td>";
	}

	function getUnusedRowCells(problemAdShot: AdShot): string {
		
		//Get the Creative sizes
		let creativeSizes = "";
		for (let currentCreative of problemAdShot.creatives()) {

			if (creativeSizes != "") {creativeSizes += ", ";}
			creativeSizes += currentCreative.width() + "x" + currentCreative.height();
		}

		//Get the cell text
		let deviceText = (problemAdShot.mobile()) ? "Mobile" : "Desktop";
		let belowFoldText = (problemAdShot.belowTheFold()) ? "✓" : "";
		
		//Return the row cells
		return 	"<td>" + problemAdShot.finalURL() + "</td>" +
				"<td>" + deviceText + "</td>" +
				"<td>" + belowFoldText + "</td>" +
				"<td>" + creativeSizes + "</td>";
	}
		
		
	/**
	 * Returns the timestamp in a formatted date string as MM/DD/YYYY (i.e. 02/03/2014)
	 * 
	 * @param timestamp 	Timestamp to convert to date string
	 * @return {string}  	Formatted MM/DD/YYYY date string of timestamp
	 */
	function getFormattedDate(timestamp: number): string {

		let newDate = new Date(timestamp * 1000);
		let newDateString = (newDate.getMonth() + 1) + "/" +
							 newDate.getDate() + "/" +
							 newDate.getFullYear();

		return newDateString;
	}

}