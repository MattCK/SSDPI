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

			else if (elementID == "dfpOrdersHelpIcon") {
				return "Informative help for DFP orders";
			}
			else if (elementID == "domainInputHelpIcon") {
				return "Informative help for entering in the domain";
			}
			else if (elementID == "addPagesHelpIcon") {
				return "Informative help for adding pages";
			}
			else if (elementID == "creativeHelpIcon") {
				return "Informative help for adding creative";
			}
		}
	});
});
