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

var USERAGENT = (system.args[4]) ? system.args[4] : "googlebot";
//var REFERRER = (system.args[5]) ? system.args[5] : "google";

var DESIREDELEMENTID = (system.args[5]) ? system.args[5] : '';
var DESIREDELEMENTCLASS = (system.args[6]) ? system.args[6] : '';

//Create the phantomjs driver webpage and set the viewport to a standard monitor size 
var page = require('webpage').create();
page.viewportSize = {width: WINDOWWIDTH, height: WINDOWHEIGHT};
page.settings.resourceTimeout = 10000;
//set browser user agent
//page.settings.userAgent = 'Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36';


switch (USERAGENT) {
  case "googlebot":
    page.settings.userAgent = 'Mozilla/5.0 (compatible; Googlebot/2.1; http://www.google.com/bot.html)';
    break;
  case "chrome":
    page.settings.userAgent = 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36';
    break;
  case "firefox":
    page.settings.userAgent = 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0';
    break;
  case "msnbot":
    page.settings.userAgent = 'msnbot/1.1 (+http://search.msn.com/msnbot.htm)';
    break;
  case "bingbot":
  	page.settings.userAgent = 'Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)';
    break;
  case "firefoxlinux":
  	page.settings.userAgent = 'Mozilla/5.0 (X11; Linux x86_64; rv:47.0) Gecko/20100101 Firefox/47.0';
    break;
  case "chromelinux":
  	page.settings.userAgent = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36';
    break;
  default:
    page.settings.userAgent = 'Mozilla/5.0 (compatible; Googlebot/2.1; http://www.google.com/bot.html)';
}



page.customHeaders = {
  "Referer": "https://www.google.com/"
};
 
page.onLoadStarted = function() {
    page.customHeaders = {};
};

//This suppresses all error messages. Comment out to view console errors.
page.onError = function(msg, trace) {

	/*console.log("there was an error on the page");
	console.log("m: " + msg);
	console.log("trace: " + trace);
	trace.forEach(function(item) {
        console.log('  ', item.file, ':', item.line);
    });*/
};

page.onResourceError = function(resourceError) {
    page.reason = resourceError.errorString;
    page.reason_url = resourceError.url;
};

function returnDesiredClass() {
    return DESIREDELEMENTCLASS;
}
function returnDesiredID() {
    return DESIREDELEMENTID;
}

page.onInitialized = function () {
    page.evaluate(function () {
        Math.random = function() {
            return 42 / 100;
        };
    });
};


//Try to connect to and open the target URL
page.open(targetURL, function(status) {
	//If the connection failed, notify and end execution	
	if (status !== 'success') {
		console.log(status + ': FAILURE: Unable to connect to URL- ' + page.reason_url + " reason: " + page.reason + " targetURL:" + targetURL); phantom.exit();
	} 
			
	//Otherwise, get the stories
	else {
		//Inject the function to grab the list of anchor info
		var anchorListJSON = page.evaluate(function(DESIREDELEMENTCLASS, DESIREDELEMENTID) {
		
			//Scroll the window 1000 pixels down twice in order to make sure all anchors are loaded
			window.scrollBy(0,1000);
			window.scrollBy(0,1000);
			var MAXPARENTSEARCHHEIGHT = 10;

			//var desiredElClas = returnDesiredClass.apply();
			//var desiredElID = returnDesiredID.apply();

			var sendBack = "cl:" + DESIREDELEMENTCLASS + "-ID:" + DESIREDELEMENTID + "-";
			//var sendBack = "cl:" + desiredElClas + "-ID:" + desiredElID + "-";
			//return sendBack;
			//return "hello";
			
			//Grab all anchor elements from the page
			//This is where the class and ID Override take place
			var anchorElements;
			if (DESIREDELEMENTCLASS == '' && DESIREDELEMENTID == ''){
				anchorElements = document.getElementsByTagName('a');
				var sendBack = sendBack + ": inside get by tagname";
			}
			else if (DESIREDELEMENTID != ''){
				var currentElement = document.getElementById(DESIREDELEMENTID);
				anchorElements = currentElement.getElementsByTagName('a');
				var sendBack = sendBack + ": inside get by ID";
			}
			//this one's compliated because you hace to loop through each element and grab the anchors
			//then you have to add those lists of anchors together without duplicating elements
			else if (DESIREDELEMENTCLASS != ''){
				var sendBack = sendBack + ": inside get by classname";
				var currentElements = document.getElementsByClassName(DESIREDELEMENTCLASS);
				
				var anchorElementList = [];
				for (var curIndex = 0; curIndex < currentElements.length; curIndex++) {

				  	if(currentElements[curIndex] != null){
					    var tempAnchorElements = currentElements[curIndex].getElementsByTagName('a');
					    var arrayBuilder = [];
					    //this loop builds a temp array of all the elements not already in
					    //the ongoing anchor elements
					    for(var i in tempAnchorElements){       
					        if (anchorElementList.length > 0){
					           for (var j in anchorElementList)
					              if (tempAnchorElements[i]!= anchorElementList[j]) {
					                 arrayBuilder.push(tempAnchorElements[i])
					              }
					        }
					        else{
					            arrayBuilder = tempAnchorElements;
					        }
					    }
					    //concat can not be assigned either of the lists in the concat operation
					    //the array.prototype turns a list of elements into an array so it can be concatenated
					    var concatArray = Array.prototype.slice.call(arrayBuilder).concat(Array.prototype.slice.call(anchorElementList));
					    anchorElementList = concatArray;
				   	}


				}
				anchorElements = anchorElementList;
			}


			//console.log(" elements: " + anchorElements.length);
			//Loop through each elements and grab its own attributes and info
			var anchorInfoList = [];
			for (var curIndex = 0; curIndex < anchorElements.length; curIndex++) {
				//console.log("link found");
				//Store the element's basic attributes 
				var curAnchorInfo = {};
				curAnchorInfo['id'] = anchorElements[curIndex].getAttribute('id');
				curAnchorInfo['href'] = anchorElements[curIndex].getAttribute('href');
				curAnchorInfo['name'] = anchorElements[curIndex].getAttribute('name');
				curAnchorInfo['onclick'] = anchorElements[curIndex].getAttribute('onclick');
				curAnchorInfo['text'] = anchorElements[curIndex].text.replace(/(\r\n|\n|\r)/gm,"").trim();
				curAnchorInfo['style'] = anchorElements[curIndex].getAttribute('style');
				curAnchorInfo['title'] = anchorElements[curIndex].getAttribute('title');
				//curAnchorInfo['linktext'] = anchorElements[curIndex].innerText;
				
				//Store the element's class. If none exist, grab the first available parent class within reasonable crawl.
				var curClass = anchorElements[curIndex].getAttribute('class');
				var currentHeight = 0;
				var curParentNode = anchorElements[curIndex].parentNode;
				while ((!curClass) && (currentHeight < MAXPARENTSEARCHHEIGHT)) {
					
					try{
						curClass = curParentNode.getAttribute('class');
					}
					catch(err){

					}
					if ((curParentNode) && (curParentNode.parentNode)){
						curParentNode = curParentNode.parentNode;
					}
					++currentHeight;
				}
				
				//If a class was found, add it to the info object. Otherwise, leave an empty string.
				if (curClass) {curAnchorInfo['className'] = curClass;}
				else {curAnchorInfo['className'] = "";}
				
				//Determine the anchor's location and size then add it to the info object
				var box = anchorElements[curIndex].getBoundingClientRect();
				var body = document.body;
				var docElem = document.documentElement;
				var scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop;
				var scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft;
				var clientTop = docElem.clientTop || body.clientTop || 0;
				var clientLeft = docElem.clientLeft || body.clientLeft || 0;
				var top  = box.top +  scrollTop - clientTop;
				var left = box.left + scrollLeft - clientLeft;
				var currWidth = box.right - left;
				var currHeight = box.bottom - box.top;
				curAnchorInfo['yPosition'] = Math.round(top);
				curAnchorInfo['xPosition'] = Math.round(left);
				curAnchorInfo['width'] = Math.round(currWidth);
				curAnchorInfo['height'] = Math.round(currHeight);
				
				//Finaly, add the info to the main list
				anchorInfoList[curIndex] = curAnchorInfo;
					
			}
			
			//Convert the anchor info list into JSON and return it
			return JSON.stringify(anchorInfoList);
			//var retMe = "retVal:";
			//return retMe;
			

		} , DESIREDELEMENTCLASS, DESIREDELEMENTID);

		//page.render( "xx" + Math.random().toString(36).slice(2) + 'currPhantomPage.png');
		
		//var pageContent = page.content;
		//console.log('FullDoc:' + pageContent);
		//Print the final JSON to the command terminal
		//console.log("about to output the anchor list");
		console.log(anchorListJSON);
	}
	
	//Completely terminate this running script
	phantom.exit();
	
} );


