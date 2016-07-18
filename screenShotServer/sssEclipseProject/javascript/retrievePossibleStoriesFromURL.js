/**
* Retrieves all the possible stories from the page and returns it in a JSON object. Each element of the array
* is an associative array that points to the links 'href', 'text', 'name', 'style', 'id', 'title', 'onclick',
* 'xPosition', and 'yPosition' respectivelly.
*/ 

//Constants used throughout script
var WINDOWWIDTH = 1366;
var WINDOWHEIGHT = 768;

//Grab the passed target URL to grab stories from
var system = require('system');
var targetURL = system.args[1];
if (!targetURL) {console.log('FAILURE: No URL passed'); phantom.exit();}

//Grab the screen height and width. If not available, use 1366 x 768
var WINDOWWIDTH = (system.args[2]) ? system.args[2] : 1366;
var WINDOWHEIGHT = (system.args[3]) ? system.args[3] : 768;

//Create the phantomjs driver webpage and set the viewport to a standard monitor size 
var page = require('webpage').create();
page.viewportSize = {width: WINDOWWIDTH, height: WINDOWHEIGHT};

//This suppresses all error messages. Comment out to view console errors.
page.onError = function(msg, trace) {};

page.onResourceError = function(resourceError) {
    page.reason = resourceError.errorString;
    page.reason_url = resourceError.url;
};

//Try to connect to and open the target URL
page.open(targetURL, function(status) {

	//If the connection failed, notify and end execution	
	if (status !== 'success') {
		console.log('FAILURE: Unable to connect to URL- ' + page.reason_url + " reason: " + page.reason + " targetURL:" + targetURL); phantom.exit();
	} 

	//Otherwise, get the stories
	else {

		//Inject the function to grab the list of anchor info
		var anchorListJSON = page.evaluate(function() {
		
			//Scroll the window 1000 pixels down in order to make sure all anchors are loaded
			window.scrollBy(0,1000);
			var MAXPARENTSEARCHHEIGHT = 10;

			//Grab all anchor elements from the page
			var anchorElements = document.getElementsByTagName('a');
			
			//Loop through each elements and grab its own attributes and info
			var anchorInfoList = [];
			for (var curIndex = 0; curIndex < anchorElements.length; curIndex++) {
					
				//Store the element's basic attributes 
				var curAnchorInfo = {};
				curAnchorInfo['id'] = anchorElements[curIndex].getAttribute('id');
				curAnchorInfo['href'] = anchorElements[curIndex].getAttribute('href');
				curAnchorInfo['name'] = anchorElements[curIndex].getAttribute('name');
				curAnchorInfo['onclick'] = anchorElements[curIndex].getAttribute('onclick');
				curAnchorInfo['text'] = anchorElements[curIndex].text.replace(/(\r\n|\n|\r)/gm,"").trim();
				curAnchorInfo['style'] = anchorElements[curIndex].getAttribute('style');
				curAnchorInfo['title'] = anchorElements[curIndex].getAttribute('title');
				
				//Store the element's class. If none exist, grab the first available parent class within reasonable crawl.
				var curClass = anchorElements[curIndex].getAttribute('class');
				var currentHeight = 0;
				var curParentNode = anchorElements[curIndex].parentNode;
				while ((!curClass) && (currentHeight < MAXPARENTSEARCHHEIGHT)) {
					curClass = curParentNode.getAttribute('class');;
					curParentNode = curParentNode.parentNode;
					++currentHeight;
				}
				
				//If a class was found, add it to the info object. Otherwise, leave an empty string.
				if (curClass) {curAnchorInfo['className'] = curClass;}
				else {curAnchorInfo['className'] = "";}
				
				//Determine the anchor's location and add it to the info object
				var box = anchorElements[curIndex].getBoundingClientRect();
				var body = document.body;
				var docElem = document.documentElement;
				var scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop;
				var scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft;
				var clientTop = docElem.clientTop || body.clientTop || 0;
				var clientLeft = docElem.clientLeft || body.clientLeft || 0;
				var top  = box.top +  scrollTop - clientTop;
				var left = box.left + scrollLeft - clientLeft;
				curAnchorInfo['yPosition'] = Math.round(top);
				curAnchorInfo['xPosition'] = Math.round(left);
				
				//Finaly, add the info to the main list
				anchorInfoList[curIndex] = curAnchorInfo;
					
			}
			
			//Convert the anchor info list into JSON and return it
			return JSON.stringify(anchorInfoList);
		});
		
		//Print the final JSON to the command terminal
		console.log(anchorListJSON);
	}
	
	//Completely terminate this running script
	phantom.exit();
});
