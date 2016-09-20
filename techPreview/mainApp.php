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

<style>
	.dropBox {
	    border: 2px dashed #bbb;
	    border-radius: 5px;
	    color: #bbb;
	    padding: 25px;
	    text-align: center;
	}
</style>

<body>

<div>
	<form id="pagesForm">
		<div id="customerDiv">
			Customer Name: <input id="customer" name="customer" type="text">
		</div>

		<div id="domainInputDiv" style="padding-top: 20px">
			Publisher Site: <input id="domain" name="domain" type="text">
			<input id="getMenuButton" type="button" value="Go!" onclick="asr.getMenu()">
			<!--input id="skipButton" type="button" value="Skip" onclick="asr.toggleDivs()"-->
		</div>
		<div id="domainNameDiv" style="padding-top: 20px; font-weight: bold; font-size: 16px;"></div>

		<div id="pagesTableDiv" style="display: none; padding-top: 20px">
			<input id="addSiteSectionButton" type="button" value="Add Site Section" onclick="asr.addMenuSectionRow()">
			<input type="button" value="Add URL" onclick="asr.addURLRow()">
			<span id="noMenuNotification" style="display: none;">Sections not available for this site</span>
			<!--input type="button" value="Add All Pages" onclick="asr.addAllPages()"-->
			<table id="pagesTable"></table><br><br>
			<!--input id="addTagsButton" type="button" value="Add Tags" onclick="asr.toggleDivs()" disabled-->
		</div>
	</form>
</div>

<div style="padding-top: 80px;">
	<div id="textFileDropZone" class="dropBox">Drop Text File(s)</div>
	<div id="zipFileDropZone" class="dropBox">Drop a Zip File</div>
   	Enter Tag Text: <br><br>
	<textarea id="tagTextTextbox" rows="10" cols="100"></textarea><br><br>
	<input id="tagTextTextboxButton" type="button" value="Add Tags">
	<!--input type="button" value="Back to Page Selection" onclick="asr.toggleDivs()"-->

	<div id="queuedTagCountDiv" style="padding-top: 10px">

		<span id="queuedTagCountSpan">0</span> Tags Queued &nbsp; 
		<input id="getTagImagesButton" type="button" value="Get Tag Images" onclick="asr.getTagImages()" disabled>

	</div>

	<ul id="sortable">
		
		<!--li class="ui-state-default ui-sortable-handle" id="tagLIcbb431fc-7960-4790-a3e4-0863c27c3d16"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/cbb431fc-7960-4790-a3e4-0863c27c3d16.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI5c910730-4a7d-4fe8-b500-4776cc05ce18"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/5c910730-4a7d-4fe8-b500-4776cc05ce18.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI40ec11bf-3cbf-4258-b374-1686f516cffd"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/40ec11bf-3cbf-4258-b374-1686f516cffd.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI4f8c29c4-839b-4c5b-8f51-6eed846fd6ee"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/4f8c29c4-839b-4c5b-8f51-6eed846fd6ee.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI1ac46431-8a3a-471e-b5fd-533680efc8ba"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/1ac46431-8a3a-471e-b5fd-533680efc8ba.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI3d5f7c81-11a1-409f-8c3c-4df7ab55ce56"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/3d5f7c81-11a1-409f-8c3c-4df7ab55ce56.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIf6f66468-13d4-41f1-aed3-367bb465da2d"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/f6f66468-13d4-41f1-aed3-367bb465da2d.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIfeb2826f-6402-4cf4-9ceb-f73607bafd3d"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/feb2826f-6402-4cf4-9ceb-f73607bafd3d.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI789a53c9-c1e4-4e0f-8a64-d93862962b4a"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/789a53c9-c1e4-4e0f-8a64-d93862962b4a.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI2be1c977-a4ac-4ef0-a1cd-82b3770aeb53"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/2be1c977-a4ac-4ef0-a1cd-82b3770aeb53.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIa15931ed-3db0-43c1-838b-a01fd96177fc"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/a15931ed-3db0-43c1-838b-a01fd96177fc.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI9eb8c8e7-eb81-4992-b55f-332d7264c1e7"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/9eb8c8e7-eb81-4992-b55f-332d7264c1e7.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLId7eb673b-d673-4e7e-81ba-396959fc73ab"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/d7eb673b-d673-4e7e-81ba-396959fc73ab.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI18fcbca5-c7c9-4de4-81f9-f841e60eae9a"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/18fcbca5-c7c9-4de4-81f9-f841e60eae9a.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI5c18ed3d-299c-4f52-9157-775b20e51c3d"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/5c18ed3d-299c-4f52-9157-775b20e51c3d.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI3db2322f-6de0-4baf-a900-3f1a00305763"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/3db2322f-6de0-4baf-a900-3f1a00305763.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI213385a2-c091-4e70-ab5b-8226e591e13f"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/213385a2-c091-4e70-ab5b-8226e591e13f.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI28d0c48b-b825-4cb6-863f-5c77380826e3"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/28d0c48b-b825-4cb6-863f-5c77380826e3.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIceb00d34-d516-4d03-9cde-bf8e0068d4bc"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/ceb00d34-d516-4d03-9cde-bf8e0068d4bc.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIcd13e319-3a95-4bfc-8b64-66ea680da80c"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/cd13e319-3a95-4bfc-8b64-66ea680da80c.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI3dec988a-7905-4fee-af45-cbefb9ab7d8d"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/3dec988a-7905-4fee-af45-cbefb9ab7d8d.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIa07a6fb4-e472-469e-a25e-79992c26b97d"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/a07a6fb4-e472-469e-a25e-79992c26b97d.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIf43232fd-6767-4df8-9c5a-f3697f9f6363"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/f43232fd-6767-4df8-9c5a-f3697f9f6363.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIacb3085d-7cfe-4d11-b114-54e260dc82ee"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/acb3085d-7cfe-4d11-b114-54e260dc82ee.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLI05d10818-fc80-4504-8117-b14c34a486b1"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/05d10818-fc80-4504-8117-b14c34a486b1.png"></li><li class="ui-state-default ui-sortable-handle" id="tagLIaa0a452f-5692-49e2-8dd0-e602b61fcacf"><img rowtag="" style="max-height: 120px;" src="https://s3.amazonaws.com/asr-tagimages/aa0a452f-5692-49e2-8dd0-e602b61fcacf.png"></li-->

	</ul>
	<br>
</div>


<input id="getScreenshotsButton" type="button" value="Get screenshots" onclick="asr.requestScreenshots()">


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
	$( "#sortable" ).disableSelection();

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