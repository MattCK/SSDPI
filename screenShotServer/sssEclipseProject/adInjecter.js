var returnString = "";

var tags = [];
//INSERT TAGS OBJECT//


return (function() {

	

	findAdsInDocument(document);

	return returnString;
})();

function findAdsInDocument(curDocument) {

	//Get all the tags in the passed document
	var nodes = curDocument.body.getElementsByTagName("*");
	
	//Loop through for each element
	Array.prototype.forEach.call(nodes, function(curNode) {
	
		//If the flood value is set correctly, mark the element
		var floodOpacity = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('flood-opacity');
		if (floodOpacity == '0.9898') {
			markAd(curNode);
		}
		
		//If the element is an iframe, find the ads in it too
		if ((curNode.nodeName == "IFRAME") && (curNode.contentDocument)) {
			findAdsInDocument(curNode.contentDocument);
		}
	});
}

function markAd(adElement) {
	adElement.style.setProperty('border', 0);
	var width = adElement.offsetWidth;
	var height = adElement.offsetHeight;
	//alert(width + ", " +height);
	var fillerDiv = document.createElement("div");
	fillerDiv.style.width = width + 'px';
	fillerDiv.style.height = height + 'px';
	fillerDiv.style.background = "purple";

	for (var i in tags) {
		if ((width == tags[i].width) && (height == tags[i].height)) {
			var tagImage = document.createElement('img');
			tagImage.src = tags[i].tag;

			adElement.parentNode.replaceChild(tagImage, adElement);
			returnString += width + ", " + height + " - ";
		}
	}

	//adElement.parentNode.replaceChild(fillerDiv, adElement);
}