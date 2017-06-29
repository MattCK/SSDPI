/**
* Contains the Campaign object (campaign) that checks for the results of a processed campaign and displays them.
*
* @package AdShotRunner
* @subpackage JavaScript
*/

/**
* The AdShotRunner object that checks for the results of a processed campaign and displays them
*/
let campaign = {

	uuid: '',											//Stores the UUID for the campaign
	_getCampaignURL: 'getCampaign.php',					//URL of request page used to retrieve results of campaign
	_QUEUETIMEOUT: 5000,								//Timeout between requests to see if job is no longer queued (ms)


	/**
	* Requests the results for the passed campaign. If the job is still queued, it runs itself again in the time
	* alotted by the _QUEUETIMEOUT member variable. If the job is finished, it displays the results including
	* customer name, site domain, campaign date, link to powerpoint, and a list of images of the screenshots.
	*/
	getResults: function() {

		//Create the callback function that will show the table
		let onSuccessCallback = function(serverResponse) {

			//If the campaign was successfully retrieved, display it or wait for it to finish
			if (serverResponse.success) {
				
				//Get the Campaign
				let currentCampaign = new Campaign(serverResponse.data.campaignJSON);

				//If it is not finished, check again after the timeout
				if (currentCampaign.status() != Campaign.FINISHED) {
					setTimeout(campaign.getResults, campaign._QUEUETIMEOUT);
				}

				//Otherwise, show the results
				else {

					//Get the finish date
					let finishedDate = new Date(currentCampaign.finishedTimestamp() * 1000);
					let finishedDateString = (finishedDate.getMonth() + 1) + "/" +
											  finishedDate.getDate() + "/" +
											  finishedDate.getFullYear();

					//Set the campaign details
					base.nodeFromID("customerSpan").innerHTML = currentCampaign.customerName();
					// base.nodeFromID("domainSpan").innerHTML = jobData.domain;
					base.nodeFromID("dateSpan").innerHTML = finishedDateString;
					// base.nodeFromID("runtimeSpan").innerHTML = jobData.runtime;

					//Set the powerpoint link
					base.nodeFromID("powerPointLink").href = currentCampaign.powerPointURL();

					//Build the screenshot table rows and insert them into the page
					let imageTableRows = "";
					for (var currentAdShot of currentCampaign.adShots()) {
						imageTableRows += "<tr><td><a href='" + currentAdShot.finalURL() + "' target='_blank'>" + currentAdShot.finalURL() + "</a><br><br>";
						imageTableRows += '<img style="max-width: 600px;" src="' + currentAdShot.imageURL() + '" /></td></tr>';
					}
					base.nodeFromID("screenshotsTable").innerHTML = imageTableRows;
					base.nodeFromID("screenshotCountSpan").innerHTML = currentCampaign.adShots().size;

					//Hide the campaign submitted div and show the results div
					base.hide("campaignSubmittedDiv");
					base.show("campaignResultsDiv");
				}
			}
						
			//If failure, show us the message returned from the server and focus on the selected element if returned. Also, re-enable the submit button.
			else {
				alert("Could not retrieve campaign data");
				console.log("error: " + serverResponse);
			}
		}

		//If there was a problem contacting the server or getting malformed response, simply re-request the results
		let onFailureCallback = function() {
			console.log("Error getting campaign data");
			setTimeout(campaign.getResults, campaign._QUEUETIMEOUT);
		}
		
		//Make the request
		base.asyncRequest(campaign._getCampaignURL, 'uuid=' + campaign.uuid, onSuccessCallback, onFailureCallback);
	},

}