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

	jobID: '',											//Stores the job ID for the campaign
	_campaignJobURL: 'getCampaignJob.php',				//URL of request page used to retrieve results of campaign
	_QUEUETIMEOUT: 5000,								//Timeout between requests to see if job is no longer queued (ms)


	/**
	* Requests the results for the passed campaign. If the job is still queued, it runs itself again in the time
	* alotted by the _QUEUETIMEOUT member variable. If the job is finished, it displays the results including
	* customer name, site domain, campaign date, link to powerpoint, and a list of images of the screenshots.
	*/
	getResults: function() {

		//Create the callback function that will show the table
		let callback = function(jobData) {

			//If the job is queued, run it again in the predefined timeout
			if (jobData.queued) {
				setTimeout(campaign.getResults, campaign._QUEUETIMEOUT);
			}

			//If the campaign results were successfully returned, display them
			else if (jobData.success) {
				
				//Set the campaign details
				base.nodeFromID("customerSpan").innerHTML = jobData.customer;
				base.nodeFromID("domainSpan").innerHTML = jobData.domain;
				base.nodeFromID("dateSpan").innerHTML = jobData.date;
				base.nodeFromID("runtimeSpan").innerHTML = jobData.runtime;

				//Set the powerpoint link
				base.nodeFromID("powerPointLink").href = jobData.powerPointURL;

				//Build the screenshot table rows and insert them into the page
				let imageTableRows = "";
				let screenshotCount = 0;
				for (var screenshotURL in jobData.screenshots) {
					if (jobData.screenshots.hasOwnProperty(screenshotURL)) {
						imageTableRows += "<tr><td><a href='" + jobData.screenshots[screenshotURL] + "' target='_blank'>" + jobData.screenshots[screenshotURL] + "</a><br><br>";
						imageTableRows += '<img style="max-width: 600px;" src="' + screenshotURL + '" /></td></tr>';
						++screenshotCount;
					}
				}
				base.nodeFromID("screenshotsTable").innerHTML = imageTableRows;
				base.nodeFromID("screenshotCountSpan").innerHTML = screenshotCount;

				//Hide the campaign submitted div and show the results div
				base.hide("campaignSubmittedDiv");
				base.show("campaignResultsDiv");
			}
						
			//If failure, show us the message returned from the server and focus on the selected element if returned. Also, re-enable the submit button.
			else {
				alert("Could not retrieve job data");
				console.log("error: " + jobData);
			}
		}
		
		//Make the request
		base.asyncRequest(campaign._campaignJobURL + '?jobID=' + campaign.jobID, '', callback);
	},

}