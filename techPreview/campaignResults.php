<?PHP
/**
* Displays campaign results
*
* @package AdShotRunner
*/
/**
* File to define paths, setup dependencies, and connect to database
*/
require_once('systemSetup.php');

?>

<?PHP include_once(BASEPATH . "header.php");?>

<script>

let campaignDisplay = {

	_jobID: <?PHP echo "'" . $_GET['jobID'] . "'" ?>,
	_campaignJobURL: 'getCampaignJob.php',

	getResults: function() {

		//Create the callback function that will show the table
		let callback = function(response) {
			
			console.log(response);
			let jobData = response;

			//If the job is queued, run again
			if (jobData.queued) {
				setTimeout(campaignDisplay.getResults, 5000);
				console.log("queued: " + jobData);
			}

			//If successful, show the data
			else if (jobData.success) {
				
				//Get the powerpoint link
				let powerPointHTML = "Powerpoint: <a href='" + jobData.powerPointURL + "'>Click Here</a><br><br><br>";

				//Build the screenshot list
				let imageListHTML = "";
				for (var pageURL in jobData.screenshots) {
					if (jobData.screenshots.hasOwnProperty(pageURL)) {
						imageListHTML += pageURL + "<br>";
						imageListHTML += '<img style="max-width: 600px;" src="' + jobData.screenshots[pageURL] + '" /><br><br>';
					}
				}
				base.nodeFromID("resultDiv").innerHTML = powerPointHTML + imageListHTML;

				//Store the menu items and create the menu options
				console.log(jobData);
			}
						
			//If failure, show us the message returned from the server and focus on the selected element if returned. Also, re-enable the submit button.
			else {
				alert("Could not retrieve job data");
				console.log("error: " + jobData);
			}
		}
		
		//Make the request
		base.asyncRequest(campaignDisplay._campaignJobURL + '?jobID=' + campaignDisplay._jobID, '', callback);
	},
}

$(function() {
	campaignDisplay.getResults();
});

</script>

<body>
<div id="resultDiv">
Queued
</div>
</body>
</html>

