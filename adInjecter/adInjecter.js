(function() {

  // Get all elements that have a style attribute
  //var elms = document.querySelectorAll("*[style]");
    
  var elms = document.body.getElementsByTagName("*");
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
	  if (floodOpacity == 0.9898) {
		//alert(floodOpacity);
		elm.style.setProperty('border', 0);
	  var width = elm.offsetWidth;
	  var height = elm.offsetHeight;
	  elm.innerHTML = '<div style="height: ' + height + 'px; width: ' + width + 'px; background-color: blue;"></div>';

		}
	  floods += floodOpacity + ',';
      //alert(floodOpacity);
      //alert(document.defaultView.getComputedStyle(elm,null).getPropertyValue('flood-opacity'));
      
      //floodStyle = document.defaultView.getComputedStyle(elm,null).getPropertyValue('flood-opacity');
      //if (floodStyle) {alert(floodStyle);}
  });
  //alert(floods);
})();