var version = (function () {
	var xhr = new XMLHttpRequest();
	xhr.open('GET', chrome.extension.getURL('manifest.json'), false);
	xhr.send(null);
	return JSON.parse(xhr.responseText).version;
}());

function optionExists(opt) {
	return (typeof localStorage[opt] != "undefined");
}
function defaultOptionValue(opt, val) {
	if (!optionExists(opt)) localStorage[opt] = val;
}
function setDefaultOptions() {
	defaultOptionValue("version", version);
	defaultOptionValue("delay", "7000");
}
chrome.extension.onRequest.addListener(function(request, sender, sendResponse) {
	if (request.reqtype == 'get-settings') {
		sendResponse({delay: localStorage['delay']});
	} else
		sendResponse({});
});

setDefaultOptions();

if (!optionExists("version") || localStorage["version"] != version) {
	localStorage["version"] = version;
}
