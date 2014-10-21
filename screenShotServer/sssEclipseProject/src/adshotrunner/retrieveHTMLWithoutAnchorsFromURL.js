/**
* Removes all anchors from the page and returns the remaining HTML.
*/ 

//Constants used throughout script
var WINDOWWIDTH = 1024;
var WINDOWHEIGHT = 768;

//Grab the passed target URL to grab stories from
var system = require('system');
var targetURL = system.args[1];
if (!targetURL) {console.log('FAILURE: No URL passed'); phantom.exit();}

//Create the phantomjs driver webpage and set the viewport to a standard monitor size 
var page = require('webpage').create();
page.viewportSize = {width: WINDOWWIDTH, height: WINDOWHEIGHT};

//This suppresses all error messages. Comment out to view console errors.
page.onError = function(msg, trace) {};

//Set timeout instead of waiting for all resources to load
//page.settings.javascriptEnabled = false;
page.settings.loadImages = false;
page.settings.resourceTimeout = 3000;

//Try to connect to and open the target URL
page.open(targetURL, function(status) {

	//If the connection failed, notify and end execution	
	if (status !== 'success') {
		console.log('FAILURE: Unable to connect to URL'); phantom.exit();
	} 

	//Otherwise, get the stories
	else {

		//Inject the function to grab the list of anchor info
		var urlText = page.evaluate(function() {
		
			//Scroll the window 1000 pixels down in order to make sure all anchors are loaded
			window.scrollBy(0,1000);

			//Remove all the anchors
			var anchorElements = document.getElementsByTagName('a');
			for (var i = anchorElements.length; i-- > 0;) {
			    var element = anchorElements[i];
			    element.parentNode.removeChild(element);
			}

			//Convert the anchor info list into JSON and return it
			return document.documentElement.innerHTML;
		});
		
		//Print the final JSON to the command terminal
		console.log(urlText);
	}
	
	//Completely terminate this running script
	phantom.exit();
});
