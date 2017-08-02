/**
* Contains the tooltip information to show to the user
*
* @package AdShotRunner
* @subpackage JavaScript
*/

$(function() {

	$( document ).tooltip({
		items: "[helpIcon], [rowTag]",
		content: function() {
			let element = $(this);
			let elementID = element.attr("id");

			//If the element is an image in the tags table, show the full size image
			if ( element.is( "[rowTag]" ) ) {
				return "<img src='" + element.attr( "src" ) + "'/>";
			}

			else if (elementID == "accountNumberHelpIcon") {
				return "The <strong>account number</strong> associated with your company<br><br> \
						The administrator in charge of your company's AdShotRunner account can provided it to you.";
			}
			else if (elementID == "dfpOrdersHelpIcon") {
				return "These are your active DFP orders.<br><br> \
						Use the filter bar above the orders to narrow your results.";
			}
			else if (elementID == "customerHelpIcon") {
				return "The customer name will appear in the final PowerPoint and in the campaign results email.";
			}
			else if (elementID == "powerPointBackgroundHelpIcon") {
				return "Choose the background image and font color to be used in the finished PowerPoint.<br><br>\
						<strong>Recommended image sizes: </strong>1280x720, 1920x1080<br><br>\
						The chosen font color should be easy to read against the background image.";
			}
			else if (elementID == "domainInputHelpIcon") {
				return "Enter the domain URL of the publisher.<br><br>(Ex: nytimes.com, chicagotribune.com)";
			}
			else if (elementID == "addPagesHelpIcon") {
				return "Choose the pages for screenshots.<br><br> \
						<strong>Add Site Section:</strong> Let's you choose a section from the site such as weather or news.<br><br>\
						<strong>Add URL:</strong> Paste in the URL of an exact page.<br><br>\
						<strong>Story:</strong> Automatically chooses a top story from the section.<br><br>\
						<strong>Mobile:</strong> Returns a mobile screenshot.<br><br>\
						<strong>Creative Type:</strong><br><br>\
						<i>All Creative:</i> All possible creative is used on the page.<br><br>\
						<i>Individual Creative:</i> A screenshot is taken for each individual creative.<br><br>\
						<i>No Creative:</i> No creative is used on the page for the screenshot.";
			}
			else if (elementID == "creativeHelpIcon") {
				return "Add creative for the screenshots.<br><br>\
						After text tags have been added, click <i>Get Tag Images</i> to turn them into images.<br><br>\
						<strong>Copy and Paste:</strong> Copy tag text from an email or file and paste into the text box. \
						Click <i>Add Tags</i> when finished.<br><br>\
						<strong>Drop Text or Image File(s):</strong> Drag and drop text files or image files to quickly add them.<br><br>\
						<strong>Drop a Zip File:</strong> Drag and drop a zip file of tag texts to quickly add them.";
			}
		}
	});
});
