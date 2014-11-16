var returnString = "";

var tags = [];
//INSERT TAGS OBJECT//


return (function() {

	//Scroll the window 1000 pixels down in order to make sure all anchors are loaded
	//window.scrollBy(0,1000);
	//window.scrollBy(0,0);

	if (document.body == null) {return "No document body found";}

	findAdsInDocument(document);

	return returnString;
})();

function findAdsInDocument(curDocument) {

	if (curDocument.body == null) {returnString += "-No document body found-"; return;}

	//Get all the tags in the passed document
	//returnString += "**New Iteration**";
	var nodes = curDocument.body.getElementsByTagName("*");
	
	//Loop through for each element
	Array.prototype.forEach.call(nodes, function(curNode) {
	
		//If the flood value is set correctly, mark the element
		var floodOpacity = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('flood-opacity');
		if (floodOpacity == '0.9898') {
			markAd(curNode);
		}
		//else {
			var zIndex = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('z-index');
			var top = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('top');
			var left = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('left');

			var width = curNode.offsetWidth;
			var height = curNode.offsetHeight;

			//Remove fixed dimension px
			top = top.substring(0, top.length - 2);
			left = left.substring(0, left.length - 2);

			if (zIndex > 1) {
				//if ((top > 0) || (left > 0)) {
				var display = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('display');
				var position = document.defaultView.getComputedStyle(curNode,null).getPropertyValue('position');
				//if ((position == 'fixed') || (position == 'absolute')) {
				if ((position == 'fixed')) {
					returnString += "^^" + top + ", " + left + "^^";
					if ((top > 0) || (left > 0)) {
						curNode.style.display = 'none';
					}
					if (((width > 900) && (height > 300)) ||
						((width > 400) && (height > 700))) {
						curNode.style.display = 'none';
					}
				}
			}
		//}
		
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

	//adElement.parentNode.replaceChild(fillerDiv, adElement);

	if ((width > 15) && (height > 15)) {
		returnString += "- " + width + ", " + height + " ";
	}

	//If really big, hide it
	if ((width > 750) && (height > 401)) {
		returnString += "&&Hiding: " + width + ", " + height + " &&";
		adElement.style.display = 'none';

		largeParent = adElement.parentNode;
		var largeWidth = largeParent.offsetWidth;
		var largeHeight = largeParent.offsetHeight;
		returnString += "&&Grandparent: " + largeWidth + ", " + largeHeight + " &&";
		if ((largeWidth <= width) && (largeHeight <= height)) {
			largeParent.style.display = 'none';
		}
	}


	for (var i in tags) {
		if ((width == tags[i].width) && (height == tags[i].height)) {
			var tagImage = document.createElement('img');
			tagImage.src = tags[i].tag;
			tagImage.style.floodOpacity = "0.9898";

			//adElement.innerHTML = "";
			while (adElement.firstChild) {
			    adElement.removeChild(adElement.firstChild);
			}
			adElement.innerHTML = "";
			//adElement.parentNode.remove(adElement);
			//adElement.parentNode.appendChild(tagImage);
			var adElementParent = adElement.parentNode;

			adElement.parentNode.replaceChild(tagImage, adElement);
			returnString += "(" + width + ", " + height + ") ";

			//Change parent width/height if need be
			var parentWidth = adElementParent.offsetWidth;
			var parentHeight = adElementParent.offsetHeight;
			if ((parentWidth < 5) || (parentHeight < 5)) {
				adElementParent.style.width = tags[i].width;
				adElementParent.style.height = tags[i].height;	

			}
		}
	}

	//adElement.parentNode.replaceChild(fillerDiv, adElement);
}

function sleep(milliseconds) {
  var start = new Date().getTime();
  for (var i = 0; i < 1e7; i++) {
    if ((new Date().getTime() - start) > milliseconds){
      break;
    }
  }
}