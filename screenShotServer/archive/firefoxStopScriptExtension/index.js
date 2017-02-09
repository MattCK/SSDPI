var self = require("sdk/self");

// // a dummy function, to show how tests work.
// // to see how to test this function, look at test/test-index.js
// function dummy(text, callback) {
//   callback(text);
// }

// exports.dummy = dummy;

var buttons = require('sdk/ui/button/action');
var tabs = require("sdk/tabs");

var button = buttons.ActionButton({
  id: "mozilla-link",
  label: "Visit Mozilla",
  icon: {
    "16": "./icon-16.png",
    "32": "./icon-32.png",
    "64": "./icon-64.png"
  },
  onClick: handleClick
});

function handleClick(state) {
  //tabs.open("https://developer.mozilla.org/");
  var windowUtils = require("sdk/window/utils");
  var gBrowser = windowUtils.getMostRecentBrowserWindow().getBrowser();
  gBrowser.docShell.allowJavascript = false;
}


//   var windowUtils = require("sdk/window/utils");
//   var gBrowser = windowUtils.getMostRecentBrowserWindow().getBrowser();

var { Hotkey } = require("sdk/hotkeys");

// var showHotKey = Hotkey({
//   combo: "accel-shift-o",
//   onPress: function() {
//     console.log("hotkey 1");
//   }
// });
// var hideHotKey = Hotkey({
//   combo: "accel-alt-shift-o",
//   onPress: function() {
//     console.log("hotkey 2");
//   }
// });

var stopScriptsKey = Hotkey({
	combo: "accel-shift-e",
	onPress: function() {
		var windowUtils = require("sdk/window/utils");
		var gBrowser = windowUtils.getMostRecentBrowserWindow().getBrowser();
		gBrowser.docShell.allowJavascript = false;

		console.log("stopped scripts");
	}
});

var startScriptsKey = Hotkey({
	combo: "accel-shift-alt-e",
	onPress: function() {
		var windowUtils = require("sdk/window/utils");
		var gBrowser = windowUtils.getMostRecentBrowserWindow().getBrowser();
		gBrowser.docShell.allowJavascript = true;

		console.log("started scripts");
	}
});

/*
require("sdk/tabs").on("ready", logURL);
 
function logURL(tab) {
	console.log(tab.url);
	console.log("Tab before: " + tab);



	var output = '';
	for (var property in tab) {
	  output += property + "\n";
	}
	console.log(output);

	console.log("\n --------------------------- \n");

	var output = '';
	for (var property in tab.window) {
	  output += property + "\n";
	}
	console.log(output);



	var { setTimeout } = require("sdk/timers");

	setTimeout(function() {
		console.log("Tab after:" + JSON.stringify(tab));
		console.log("Linked browser:" + tab.linkedBrowser);

		var output = '';
		for (var property in tab) {
		  output += property + "\n";
		}
		//console.log(output);
		console.log("Window: " + tab.window);


		var windowUtils = require("sdk/window/utils");
		var gBrowser = windowUtils.getMostRecentBrowserWindow().getBrowser();
		//gBrowser.docShell.allowJavascript = false;
		var browser = require("sdk/tabs/utils").getTabBrowser(tab.window);
		//var browser = gBrowser.getBrowserForTab(tab);
		console.log("Browser from tab: " + browser);
		//console.log("Browser from tab: " + gBrowser.getBrowserForTab(tab);
		console.log("Actual browser: " + browser);
		browser.docShell.allowJavascript = false;
		console.log(windowUtils.getToplevelWindow(tab));
		console.log("stopped scripts");
	}, 1000)  
}
*/

// var pageMod = require("sdk/page-mod");
// var self = require("sdk/self");

// // Create a page-mod
// // It will run a script whenever a ".org" URL is loaded
// // The script replaces the page contents with a message
// pageMod.PageMod({
//   	include: "*",
// 	contentScriptFile: self.data.url("scriptStopper.js"),
// 	contentScriptWhen: "end"
// });
//var newTabBrowser = gBrowser.getBrowserForTab(gBrowser.addTab("http://www.google.com/"));
//newTabBrowser.addEventListener("load", function () {
//  newTabBrowser.contentDocument.body.innerHTML = "<div>hello world</div>";
//}, true);








/*var myExtension = {
    init: function() {
        // The event can be DOMContentLoaded, pageshow, pagehide, load or unload.
        if(gBrowser) gBrowser.addEventListener("DOMContentLoaded", this.onPageLoad, false);
    },
    onPageLoad: function(aEvent) {
        var doc = aEvent.originalTarget; // doc is document that triggered the event
        var win = doc.defaultView; // win is the window for the doc
        // test desired conditions and do something
        // if (doc.nodeName != "#document") return; // only documents
        // if (win != win.top) return; //only top window.
        // if (win.frameElement) return; // skip iframes/frames
        alert("page is loaded \n" +doc.location.href);
    }
}
window.addEventListener("load", function load(event){
    window.removeEventListener("load", load, false); //remove listener, no longer needed
    myExtension.init();  
},false);*/