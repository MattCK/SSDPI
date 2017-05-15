console.log("Overriding addEventListener");
// window.Element.prototype.originalAddEventListener = Element.prototype.addEventListener;
// window.Element.prototype.addEventListener = function(event, handler, useCapture){

// 	//if ((event.toLowerCase() == 'visibilitychange') || (event.toLowerCase() == 'webkitvisibilitychange')) {} //Do nothing
// 	//else {Element.prototype.originalAddEventListener(event, handler, useCapture);}
// };

let disableListenersScript = 'Window.prototype.addEventListener = function(event, handler, useCapture){console.log("Event: " + event);};' + 
							 'HTMLDocument.prototype.addEventListener = function(event, handler, useCapture){console.log("Event: " + event);};' + 
							 'Element.prototype.addEventListener = function(event, handler, useCapture){console.log("Event: " + event);};' + 
							 'Document.prototype.__defineGetter__("hidden", function() {return true;}); console.log("document.hidden: " + document.hidden);Document.prototype.__defineGetter__("webkitHidden", function() {return false;}); console.log("document.webkitHidden: " + document.hidden);Document.prototype.__defineGetter__("visibilityState", function() {return "visible";}); console.log("document.visibilityState: " + document.visibilityState);Document.prototype.__defineGetter__("webkitVisibilityState", function() {return "visible";}); console.log("document.webkitVisibilityState: " + document.webkitVisibilityState);;' +
							 'function _crawlDocumentHTMLElements(documentToCrawl, nodeFunction) {	if ((!documentToCrawl) || (!(document instanceof HTMLDocument)) || (!documentToCrawl.body)) {return;}	if ((!nodeFunction) || (!(nodeFunction instanceof Function))) {return;}	let headHTEMLElementNodes = [].slice.call(documentToCrawl.head.getElementsByTagName("*"));	let bodyHTMLElementNodes = [].slice.call(documentToCrawl.body.getElementsByTagName("*"));	let allHTMLElementNodes = headHTEMLElementNodes.concat(bodyHTMLElementNodes);	for (let currentNode of allHTMLElementNodes) {		nodeFunction(currentNode);		try {			if ((currentNode.nodeName == "IFRAME") && (currentNode.contentDocument)) {				this._crawlDocumentHTMLElements(currentNode.contentDocument, nodeFunction);			}		}		catch(error) {} 	}}function removeListeners(node) {	var old_element = node;	var new_element = old_element.cloneNode(true);	old_element.parentNode.replaceChild(new_element, old_element);}window.onload = function() {_crawlDocumentHTMLElements(document, removeListeners);};' + 
							 'console.log("successful overwrite");';
var script = document.createElement('script');
script.textContent = disableListenersScript;
// (document.head||document.documentElement).appendChild(script);
(document.head||document.documentElement).insertBefore(script, (document.head||document.documentElement).firstChild);
script.remove();
console.log("addEventListener overridden");
