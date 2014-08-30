/**
* Retrieves all the possible stories from the page and returns it in a JSON object. Each element of the array
* is an associative array that points to the links 'href', 'text', 'name', 'style', 'id', 'title', 'onclick',
* 'xPosition', and 'yPosition' respectivelly.
*/ 

console.log('test');

//Constants used throughout script
var WINDOWWIDTH = 1024;
var WINDOWHEIGHT = 768;
var MAXPARENTSEARCHHEIGHT = 10;

//Grab the passed target URL to grab stories from
var system = require('system');
var targetURL = system.args[1];
if (!targetURL) {console.log('FAILURE: No URL passed'); phantom.exit();}

//Create the phantomjs driver webpage and set the viewport to a standard monitor size 
var page = require('webpage').create();
page.viewportSize = {width: WINDOWWIDTH, height: WINDOWHEIGHT};

//Try to connect to and open the target URL
page.open(targetURL, function(status) {

	//If the connection failed, notify and end execution	
	if (status !== 'success') {
		console.log('FAILURE: Unable to connect to URL'); phantom.exit();
	} 

	//Otherwise, get the stories
	else {
	
		//Inject the function to grab the list of anchor info
		var anchorListJSON = page.evaluate(function() {
		
			//Scroll the window 1000 pixels down in order to make sure all anchors are loaded
			window.scrollBy(0,1000);

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
				if (!curClass) {
					var currentHeight = 0;
					var curParentNode = anchorElements[curIndex].parentNode;
					while ((!curClass) && (currentHeight < MAXPARENTSEARCHHEIGHT)) {
						curClass = curParentNode.getAttribute('class');;
						curParentNode = curParentNode.parentNode;
						++currentHeight;
					}
				}
				
				//If a class was found, add it to the info object. Otherwise, leave an empty string.
				if (curClass) {curAnchorInfo['class'] = curClass;}
				else {curAnchorInfo['class'] = "";}
				
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
				curAnchorInfo['yPos'] = Math.round(top);
				curAnchorInfo['xPos'] = Math.round(left);
				
				//Finaly, add the info to the main list
				anchorInfoList[curIndex] = curAnchorInfo;
					
			}
			
			//Convert the anchor info list into JSON and return it
			return JSON.stringify(anchorInfoList);
		});
		
		console.log(anchorListJSON);

	}
	
	//Completely terminate this running script
	phantom.exit();
});
