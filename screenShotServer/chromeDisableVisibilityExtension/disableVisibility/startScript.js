/**
* ---------------------------------------------------------------------------------------
* ------------------------------ startScript.js -----------------------------------
* ---------------------------------------------------------------------------------------
*	
* Injects disableVisibility.js into each frame.
*/

//Creates the script element for disableVisibility.js, inject into the page, then remove the script object.
var script = document.createElement('script');
script.setAttribute("type", "text/javascript");
script.setAttribute("src", chrome.extension.getURL('disableVisibility.js'));
(document.head||document.documentElement).insertBefore(script, (document.head||document.documentElement).firstChild);
script.remove();