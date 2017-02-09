console.log("in script stopper");
//console.log("gBrowser: " + gBrowser);
console.log("Window: " + window);

	var output = '';
	for (var property in window) {
	  output += property + "\n";
	}
	//console.log(output);

//console.log(tabsUtils.getTabBrowser(window));
this.onload = function() {
	console.log("onload");
	gBrowser.docShell.allowJavascript = false;
	console.log("finishedload");
}