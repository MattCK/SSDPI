/**
*  focus.de exception script
*
*  Removes Facebook/Subscribe overlay from article that appears on first navigation
*/

//Remove the article facebook/subscribe overlay
if (document.getElementById("likegate")) {
	document.getElementById("likegate").style.display = 'none';
}

if (document.getElementById("likegate-overlay-0")) {
	document.getElementById("likegate-overlay-0").style.display = 'none';
}