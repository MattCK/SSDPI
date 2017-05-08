/**
* --PhantomJS Script--
*
* Attempts to connect to the provided URL and return the rendered html/text response
*/ 

//Define driver constants
var DEFAULTVIEWPORTWIDTH = 1366;
var DEFAULTVIEWPORTHEIGHT = 768;
var DEFAULTUSERAGENT = "Mozilla/5.0 (compatible; Googlebot/2.1; http://www.google.com/bot.html)";
var REFERRER = "https://www.google.com/";
var RESOURCETIMEOUT = 10000;

//Get and verify the passed target URL to connect to. Return failure if error and exit.
var phantomJSSystem = require('system');
var targetURL = phantomJSSystem.args[1];
if (!targetURL) {
	console.log('FAILURE: No URL passed'); phantom.exit();
}

//Get the user agent string. If none exist, use the default.
var userAgent = (phantomJSSystem.args[2]) ? phantomJSSystem.args[2] : DEFAULTUSERAGENT;

//Get the screen height and width. If not available, use default constants
var viewportHeight = (phantomJSSystem.args[3]) ? phantomJSSystem.args[3] : DEFAULTVIEWPORTWIDTH;
var viewportWidth = (phantomJSSystem.args[4]) ? phantomJSSystem.args[4] : DEFAULTVIEWPORTHEIGHT;

//Create the phantomjs driver and set the viewport to a standard monitor size
//with proper useragent and referrer
var driver = require('webpage').create();
driver.viewportSize = {width: viewportHeight, height: viewportWidth};
driver.settings.resourceTimeout = RESOURCETIMEOUT;
driver.settings.userAgent = userAgent;
driver.customHeaders = {"Referer": REFERRER};  //Typo in original HTML specs spelled referrer wrong with only one 'r'

//Once the page has started to load, we clear the custom headers so the page itself can set them.
//In this instance, we want it to look like we are coming from the default referrer (i.e. google),
//but asset requests to look like they are coming from the page just navigated to.
driver.onLoadStarted = function() {
    driver.customHeaders = {};
};

//If an error occurs, store the error info in the page object
driver.onResourceError = function(resourceError) {
    driver.reason = resourceError.errorString;
    driver.reason_url = resourceError.url;
};

//This suppresses all error messages.
////////////// COMMENT OUT TO VIEW CONSOLE ERRORS /////////////////////////
//driver.onError = function(msg, trace) {};
function click(el){
    var ev = document.createEvent("MouseEvent");
    ev.initMouseEvent(
        "click",
        true /* bubble */, true /* cancelable */,
        window, null,
        0, 0, 0, 0, /* coordinates */
        false, false, false, false, /* modifier keys */
        0 /*left*/, null
    );
    el.dispatchEvent(ev);
}

function sendRenderedContent() {

	//click(document.getElementById());
	//document.elementFromPoint(85, 45).click();

	console.log(driver.content);
	
	//Completely terminate this running script
	//driver.render('screenshot.png');
	phantom.exit();
}

//Try to connect to and open the target URL
driver.open(targetURL, function(status) {

	//If the connection failed, notify and end execution	
	if (status !== 'success') {
		console.log(status + ': FAILURE: Unable to connect to URL- ' + driver.reason_url + " reason: " + driver.reason + " targetURL:" + targetURL); phantom.exit();
	} 
			
	//Otherwise, get the stories
	else {
		//console.log(driver.content);
		setTimeout(sendRenderedContent, 500);
	}
	
	//Completely terminate this running script
	//driver.render('screenshot.png');
	//phantom.exit();
});
