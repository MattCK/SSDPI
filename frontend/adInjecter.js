(function() {

  // Get all elements that have a style attribute
  //var elms = document.querySelectorAll("*[style]");
	
	findAdsInDocument(document);
	
  /*var elms = document.body.getElementsByTagName("*");
	var floods = "";
  
  // Loop through them
  Array.prototype.forEach.call(elms, function(elm) {
    // Get the color value
    var clr = elm.style.color || "";

    // Remove all whitespace, make it all lower case
    clr = clr.replace(/\s/g, "").toLowerCase();

    // Switch on the possible values we know of
        //elm.style.color = "#444";
      elm.style.setProperty('color', 'red');
	  
	  var floodOpacity = document.defaultView.getComputedStyle(elm,null).getPropertyValue('flood-opacity');
	  if (floodOpacity == '0.9898') {
		//alert(floodOpacity + ": " + elm.nodeName);
		elm.style.setProperty('border', 0);
		elm.style.border = 0;
		  var width = elm.offsetWidth;
		  var height = elm.offsetHeight;
		  //elm.innerHTML = '<div style="height: ' + height + 'px; width: ' + width + 'px; background-color: blue;"></div>';
		var fillerDiv = document.createElement("div");
		fillerDiv.style.width = width + 'px';
		fillerDiv.style.height = height + 'px';
		fillerDiv.style.background = "purple";
		elm.parentNode.replaceChild(fillerDiv, elm);
		//elm.parentNode.removeChild(elm);
		}
	  floods += floodOpacity + ',';
      //alert(floodOpacity);
      //alert(document.defaultView.getComputedStyle(elm,null).getPropertyValue('flood-opacity'));
      
      //floodStyle = document.defaultView.getComputedStyle(elm,null).getPropertyValue('flood-opacity');
      //if (floodStyle) {alert(floodStyle);}
  });
  //alert(floods);*/
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
	adElement.parentNode.replaceChild(fillerDiv, adElement);
}