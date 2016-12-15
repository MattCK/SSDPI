chrome.extension.sendRequest({reqtype: "get-settings"}, function(response) {
	window.setTimeout("window.stop();", response.delay);
});
