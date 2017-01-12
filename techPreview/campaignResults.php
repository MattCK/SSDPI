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

<body>
	
<div id="header">
	<div id="title">
		<h1>AdShotRunner&trade;: Free Tech Preview</h1> 
	</div>
	<div id="logout">
		<a onclick="contactForm.reset(); contactFormDialog.open()">Contact Us</a>&nbsp;&nbsp;&nbsp;&nbsp;
	</div>
</div>

<div id="mainContent">
	<div id="campaignSubmittedDiv">
		<h2>Campaign Submitted</h2>
		<div id="customerDiv" class="section">
			<p>Your campaign has been added to the queue.</p>
			<p>You will receive an email when it is ready and the results will appear here.</p>
			<p><input class="button-tiny" type="button" value="Start Another Campaign in a New Tab" onclick="window.open('/', '_blank');"></p>
		</div>
	</div>

	<div id="campaignResultsDiv" style="display: none">

		<h2>Campaign Details</h2>
		<div id="campaignDetailsDiv" class="section">
			<table>
				<tr><td>Customer: </td><td><span id="customerSpan">Name of Customer</span></td></tr>
				<tr><td>Publisher Site: </td><td><span id="domainSpan">somedomain.com</span></td></tr>
				<tr><td>Date: </td><td><span id="dateSpan">01/02/2003</span></td></tr>
				<tr><td>Screenshot Count: </td><td><span id="screenshotCountSpan">0</span></td></tr>
				<tr><td>Runtime: </td><td><span id="runtimeSpan">0</span> seconds</td></tr>
			</table>
		</div>

		<h2>PowerPoint</h2>
		<div class="problemLinkDiv">
			<a onclick="contactForm.reset(); contactForm.selectIssue(); contactFormDialog.open()">Problem?</a>
		</div>
		<div id="powerPointDiv" class="section">
			<a id="powerPointLink" href="#">Click to Download PowerPoint of Screenshots</a>
		</div>

		<h2>Screenshots</h2>
		<div class="problemLinkDiv">
			<a onclick="contactForm.reset(); contactForm.selectIssue(); contactFormDialog.open()">Problem?</a>
		</div>
		<div id="screenshotsDiv" class="section" align="center">
			<table id="screenshotsTable"></table>
		</div>

	</div>
</div>

<!-- ******************************** Contact Form Div ******************************** -->

<div style="display: none;">
	<?PHP include("contactForm.php");?>
</div>

<!-- ********************************************************************************** -->

</body>
</html>

<script>

//Reset the contact form in case the user pressed the refresh button
contactForm.reset();

//Set the job ID and start polling for the results JSON file. Turn contact form into dialog.
let contactFormDialog = null;
$(function() {
	campaign.jobID = <?PHP echo "'" . $_GET['jobID'] . "'" ?>;
	campaign.getResults();
	contactFormDialog = base.createDialog("contactFormDiv", "Contact Us", true, 650);
});
</script>
