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

use AdShotRunner\System\ASRProperties;

session_start();
header("Cache-control: private"); 

?>

<?PHP include_once(BASEPATH . "header.php");?>

<body>
	
<div id="header">
	<div id="title">
		<a href="/"><img id="headerLogo" src="images/headerLogo.png"/></a>
	</div>
	<div id="logout">
		<a class="contactIdeaLink">Contact Us</a>&nbsp;&nbsp;&nbsp;&nbsp;
	</div>
</div>

<div id="mainContent">
	<div id="campaignSubmittedDiv">
		<h2>Campaign Submitted</h2>
		<div id="submittedDetailsDiv" class="section" style="display: flex;">
			<div style="width: 50%">
				Your campaign has been added to the queue.<br/><br/>
				You will receive an email when it is ready and the results will appear here.<br/><br/>
				<p><input class="button-tiny" type="button" value="Start Another Campaign in a New Tab" onclick="window.open('/', '_blank');"></p>
			</div>
			<div style="float: right; width: 50%; text-align: center; border-left: 1px solid;">
				<p style="font-weight: bold; font-size: 20px">
					<span id="submittedCustomerNameSpan"></span> - <span id="createdDateSpan"></span>
				</p>
				<p style="font-weight: bold; font-size: 16px">Status: 
					<span id="campaignStatusSpan"></span>
				</p>
			</div>
		</div>
	</div>

	<div id="campaignErrorDiv" style="display: none">
		<h2>Problem Processing Campaign</h2>
		<div id="errorDetailsDiv" class="section">
			Unfortunately, a problem occurred while processing your screenshots: 
				<span id="errorCustomerNameSpan" style="font-weight: bold"></span> - 
				<span id="errorDateSpan" style="font-weight: bold"></span>
			<br/><br/>
			We have been notified of this issue and are looking into it. We apologize 
			for this inconvenience and we appreciate your understanding while we investigate this matter. 
			If you have any questions or the problem persists, please email us at 
			<a href="mailto:<?PHP echo ASRProperties::emailAddressSupport()?>"><?PHP echo ASRProperties::emailAddressSupport()?></a> 
			or call (773) 295-2386. 
			
			<p><input class="button-tiny" type="button" value="Start Another Campaign in a New Tab" onclick="window.open('/', '_blank');"></p>
		</div>
	</div>


	<div id="campaignResultsDiv" style="display: none">

		<div>
			<div>
				<h2>Campaign Details</h2>
			</div>
			<div style="float:right; margin-top: -32px">
				<input class="button-tiny" type="button" value="Start a New Campaign" onclick="window.open('/', '_blank');">
			</div>
		</div>
		<div id="campaignDetailsDiv" class="section">
			<table>
				<tr><td>Customer: </td><td><span id="customerSpan">Name of Customer</span></td></tr>
				<tr style="display:none"><td>Publisher Site: </td><td><span id="domainSpan">somedomain.com</span></td></tr>
				<tr><td>Date: </td><td><span id="dateSpan"></span></td></tr>
				<tr><td>Screenshot Count: </td><td><span id="screenshotCountSpan">0</span></td></tr>
				<tr style="display:none"><td>Runtime: </td><td><span id="runtimeSpan">0</span> seconds</td></tr>
			</table>
		</div>

		<h2>PowerPoint</h2>
		<div class="problemLinkDiv">
			<a class="contactIssueLink">Need help?</a>
		</div>
		<div id="powerPointDiv" class="section">
			<a id="powerPointLink" href="#">Click to Download PowerPoint of Screenshots</a>
		</div>

		<div id="adShotIssuesDiv" style="display: none;">
			<h2>Issues (only viewable to logged in AdShotRunner users)</h2>
			<div class="section" style="background-color: #eac9c9;">
				Unfortunately, a problem occurred while processing the following screenshots.<br/><br/>
				We have been notified of this issue and are looking into it. We apologize for 
				this inconvenience and we appreciate your understanding while we investigate this matter.
				If you have any questions or the problem persists, please email us at 
			    <a href="mailto:<?PHP echo ASRProperties::emailAddressSupport()?>"><?PHP echo ASRProperties::emailAddressSupport()?></a> 
			    or call (773) 295-2386.
				<br/><br/>
				<table id="adShotIssuesTable" class="issuesTable">
					<tr><th>Page</th>
						<th>Device</th>
						<th>Story Finder</th>
						<th>Below Fold</th>
						<th>Creative Sizes</th>
					</tr>
				</table>
			</div>
		</div>

		<h2>Screenshots</h2>
		<div class="problemLinkDiv">
			<a class="contactIssueLink">Need help?</a>
		</div>
		<div id="screenshotsDiv" class="section" align="center">
			<table id="screenshotsTable"></table>
		</div>

		<div id="unusedAdShotsDiv" style="display: none;">
			<h2>Screenshots Not Included (only viewable to logged in AdShotRunner users)</h2>
			<div class="section" style="background-color: #ede9aa">
				<span id="unusedAdShotCountSpan" style="font-weight: bold"></span> page(s) 
				appear not to have an ad placement size matching 
				the Creative size(s). This might have occurred if "Individual Creative" was selected. 
				<a onclick="base.show('unusedAdShotsTable'); this.style.display = 'none';">View Pages</a>
				<table id="unusedAdShotsTable" class="issuesTable" style="display: none; margin-top: 15px;">
					<tr><th>Page</th>
						<th>Device</th>
						<th>Below Fold</th>
						<th>Creative Sizes</th>
					</tr>
				</table>
			</div>
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
	campaign.uuid = <?PHP echo "'" . $_GET['uuid'] . "'" ?>;
	campaign.li = <?PHP echo ($_SESSION['userID']) ? "true" : "false";?>;
	campaign.getResults();
	contactFormDialog = base.createDialog("contactFormDiv", "Contact Us", true, 650);
});
</script>
