<?PHP
/**
* Main app page that controls creation of screenshots
*
* @package AdShotRunner
*/
/**
* File to define paths, setup dependencies, and connect to database
*/
require_once('systemSetup.php');

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');

?>

<?PHP include_once(BASEPATH . "header.php");?>

<body>
	
<div id="header">
	<div id="title">
		<h1>AdShotRunner&trade;: Free Tech Preview</h1> 
	</div>
	<div id="logout">
		<a href="mailto:contact@dangerouspenguins.com">Contact Us</a>&nbsp;&nbsp;&nbsp;&nbsp;
		<a href="logout.php">Logout</a>
	</div>
</div>

<div id="mainContent">
		<form id="pagesForm">
			<h2>Customer</h2>
			<div id="customerDiv" class="section">
				<div class="textFieldName">Name:</div> 
				<input id="customer" name="customer" type="text">
			</div>

			<h2 id="campaignPagesHeader">Campaign Pages</h2>
			<div id="domainInputDiv" class="section">
				<div class="textFieldName">Publisher Site:</div>
				<input id="domain" name="domain" type="text">
				<input class="button-tiny" id="getMenuButton" type="button" value="Go!" onclick="asr.getMenu()">
				<!--input id="skipButton" type="button" value="Skip" onclick="asr.toggleDivs()"-->
			</div>

			<div id="pagesTableDiv" class="section" style="display: none">
				<!--div id="domainNameDiv"></div-->
				<input class="button-tiny"  id="addSiteSectionButton" type="button" value="Add Site Section" onclick="asr.addMenuSectionRow()">
				<input class="button-tiny"  type="button" value="Add URL" onclick="asr.addURLRow()">
				<span id="noMenuNotification" style="display: none;">Sections not available for this site</span>
				<!--input type="button" value="Add All Pages" onclick="asr.addAllPages()"-->
				<table id="pagesTable"></table>
				<!--input id="addTagsButton" type="button" value="Add Tags" onclick="asr.toggleDivs()" disabled-->
			</div>
		</form>


		<h2>Tags</h2>
		<div class="section">
		   	<div class="copyPasteTextTitle">Copy and Paste Tag Text:</div>
		   	<div id="tagTextDiv">
				<textarea id="tagTextTextbox" rows="10" cols="60"></textarea>
				<input class="button-tiny" id="tagTextTextboxButton" type="button" value="Add Tags">
			</div>
			<div>
				<div id="textFileDropZone" class="dropBox">Drop Text File(s)</div>
				<div id="zipFileDropZone" class="dropBox">Drop a Zip File</div>
			</div>
			<!--input type="button" value="Back to Page Selection" onclick="asr.toggleDivs()"-->
			
			<div id="queuedTagDiv" class="yellowBackground" align="center">
				<span id="queuedTagCountSpan">0</span> <span id="tagsQueuedSpan">Tags Queued</span> &nbsp; 
				<input class="button-tiny" id="getTagImagesButton" type="button" value="Get Tag Images" onclick="asr.getTagImages()" disabled>

			</div>

		<ul id="sortable"></ul>
	</div>

	<div align="center">
		<div id="getScreenShotsDiv" class="section" align="center">
			<input class="button-tiny" id="getScreenshotsButton" type="button" value="Get screenshots" onclick="asr.requestScreenshots()" disabled="">
		</div>
	</div>

</div>



</body>
</html>


<script>
//Setup listeners
let textFileDropZone = base.nodeFromID('textFileDropZone');
textFileDropZone.addEventListener('dragover', tagParser.handleDragOver, false);
textFileDropZone.addEventListener('drop', tagParser.handleTagTextFileDrop, false);
let zipFileDropZone = base.nodeFromID('zipFileDropZone');
zipFileDropZone.addEventListener('dragover', tagParser.handleDragOver, false);
zipFileDropZone.addEventListener('drop', tagParser.handleTagZipFileDrop, false);
let tagTextTextboxButton = base.nodeFromID("tagTextTextboxButton");
tagTextTextboxButton.addEventListener('click', tagParser.handleTagTextboxInput, false);
$(function() {
	$( "#sortable" ).sortable();
	//$( "#sortable" ).disableSelection();

	$( document ).tooltip({
		items: "img, [data-geo], [title]",
		content: function() {
			let element = $( this );
			/*if ( element.is( "[data-geo]" ) ) {
				let text = element.text();
				return "<img class='map' alt='" + text +
				"' src='http://maps.google.com/maps/api/staticmap?" +
				"zoom=11&size=350x350&maptype=terrain&sensor=false&center=" +
				text + "'>";
			}*/
			if ( element.is( "[rowTag]" ) ) {
				return "<img src='" + element.attr( "src" ) + "'/>";
			}
		}
	});
});

</script>