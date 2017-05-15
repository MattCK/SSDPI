//Define driver constants
var DEFAULTVIEWPORTWIDTH = 1366;
var DEFAULTVIEWPORTHEIGHT = 768;
var DEFAULTUSERAGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
var REFERRER = "https://www.google.com/";
var RESOURCETIMEOUT = 10000;

var phantomJSSystem = require('system');
var targetURL = phantomJSSystem.args[1];
if (!targetURL) {
	console.log('FAILURE: No URL passed'); phantom.exit();
}

var targetImageName = phantomJSSystem.args[2];
if (!targetImageName) {
	console.log('FAILURE: No Image Name passed'); phantom.exit();
}

var page = require('webpage').create();
page.viewportSize = { width: DEFAULTVIEWPORTWIDTH, height: DEFAULTVIEWPORTHEIGHT };

page.open(targetURL, function () {
  setTimeout(function() {
    // Initial frame
    var frame = 0;
    // Add an interval
    setInterval(function() {
      // Render an image with the frame name
      
        var clipRect = page.evaluate(function() {
        return document.querySelector('div#adTagContainer').getBoundingClientRect();});
        page.clipRect = {
            top:    clipRect.top,
            left:   clipRect.left,
            width:  clipRect.width,
            height: clipRect.height
        };
        //the comemmented out line below will save an image every loop
      //page.render('frames/'+ targetImageName +(frame++)+'.png', { format: "png" });
      //because i turn off the increment i need to do it here
      console.log("loop: " + frame);
      frame = frame + 1;
      // Exit after 5 images
      if(frame > 4) {
        page.render(targetImageName);
        //console.log(document.getElementById('adTagContainer').offsetHeight + "px");
        phantom.exit();
      }
      //wait 3 seconds for each loop
    }, 3000);
    //start executing 2 seconds after phantomjs startup
  }, 2000);
});