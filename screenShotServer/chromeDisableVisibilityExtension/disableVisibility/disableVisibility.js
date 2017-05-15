/**
* ---------------------------------------------------------------------------------------
* ------------------------------ DisableVisibility.js -----------------------------------
* ---------------------------------------------------------------------------------------
*	
* This DisableVisibility script disables the Visibility API, removes all listeners from
* all nodes, and turns Tween off (if it exists).
*/

//Disable all of the types of addEventListeners
Window.prototype.addEventListener = function(event, handler, useCapture){}; 
HTMLDocument.prototype.addEventListener = function(event, handler, useCapture){}; 
Element.prototype.addEventListener = function(event, handler, useCapture){}; 

//Override hidden states so they always return visible
Document.prototype.__defineGetter__("hidden", function() {return false;}); 
Document.prototype.__defineGetter__("webkitHidden", function() {return false;});
Document.prototype.__defineGetter__("visibilityState", function() {return "visible";}); 
Document.prototype.__defineGetter__("webkitVisibilityState", function() {return "visible";}); 

//One the window has been loaded with all javascript run, remove the listeners from each node
//Also remove Tween if it exists
window.onload = function() {
	crawlDocumentHTMLElements(document, removeListeners); 
}; 
setTimeout(function() { 
	if (typeof TweenLite !== 'undefined') {TweenLite.lagSmoothing(0);}
	if (typeof TweenMax !== 'undefined') {TweenMax.lagSmoothing(0);}
}, 2000);

/**
* Crawls the DOM document, including any iFrames, and calls the passed function on 
* each HTMLElement node.
*
* The function receives a single argument of the HTMLElement node.
*
* @param {HTMLDocument} 	documentToCrawl		The DOM document to crawl
* @param {Function}			nodeFunction		Function to call on each HTMLElement node. Receives HTMLElement node as argument.
*/
function crawlDocumentHTMLElements(documentToCrawl, nodeFunction) {

	//If the document does not exist, is not a HTMLDocument, or does not have a "body" property, exit the function
	if ((!documentToCrawl) || (!(document instanceof HTMLDocument)) || (!documentToCrawl.body)) {return;}

	//If the passed function is null or not a function, exit the function
	if ((!nodeFunction) || (!(nodeFunction instanceof Function))) {return;}

	//Get all of the HTMLElement nodes in the passed document
	let headHTEMLElementNodes = [].slice.call(documentToCrawl.head.getElementsByTagName("*"));
	let bodyHTMLElementNodes = [].slice.call(documentToCrawl.body.getElementsByTagName("*"));
	let allHTMLElementNodes = headHTEMLElementNodes.concat(bodyHTMLElementNodes);

	//For each node, apply the passed function it and crawl any iFrames
	for (let currentNode of allHTMLElementNodes) {

		//Apply the passed function on the node
		nodeFunction(currentNode);

		//If the node is an IFrame, crawl it as well. 
		//Try-Catch is used in case crawling it is not permitted by browser security.
		try {
			//If the node is an iframe, find the ads in it too
			if ((currentNode.nodeName == "IFRAME") && (currentNode.contentDocument)) {

				//Call the function on the new iframe
				this.crawlDocumentHTMLElements(currentNode.contentDocument, nodeFunction);
			}
		}
		catch(error) {} //Do nothing on error
	}
}

/**
* Removes all listeners from a node by cloning it and replacing it with the clone.
*
* @param {HTMLElement}	node 	Node to remove listeners from
*/
function removeListeners(node) {
	var originalNode = node;	
	var clonedNode = originalNode.cloneNode(true);	
	originalNode.parentNode.replaceChild(clonedNode, originalNode);
}