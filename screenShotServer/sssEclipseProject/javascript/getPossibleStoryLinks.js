/**
* This getPossibleStoryLinks script retrieves all of the possible links (<a>) from the page
* and returns detailed information on each.
*
* If a container element ID is provided, only links from within that element are returned.
* If a class name is provided, only links from within elements with the given class name are returned.
* 
* The script is designed to be run by and return a JSON response to Selenium.
*/

const MAXPARENTSEARCHHEIGHT = 10;

//The exception container info to use for getting anchors
//This is inserted below by the StoryLinkRetriever
let containerID = "";
let containerClassName = "";
//INSERT EXCEPTIONS//


//Scroll the window 1000 pixels down twice in order to make sure all anchors are loaded
//Scroll back up to get correct element locations
//Note: Might not be necessary since switched from PhantomJS to Chrome Headless
window.scrollBy(0,1000);
window.scrollBy(0,1000);
window.scrollBy(0,-2000);


/*******************************************************************************************/
/*********************************** Get Links from Page ***********************************/
/*******************************************************************************************/

//If no specific container or class name exception was provided, get all links/anchors
let anchorElements;
if ((containerClassName == '') && (containerID == '')){
	anchorElements = document.getElementsByTagName('a');
}

//If a specific container element ID was provided, get the links/anchors from only within it
else if (containerID != ''){
	let currentElement = document.getElementById(containerID);
	if (currentElement != null){
		anchorElements = currentElement.getElementsByTagName('a');
	}
}

//If a specific container class name was provided, get the links/anchors from only within matching elements
//NEEDS CLEAN UP 
//CONSIDER USING "SET"
else if (containerClassName != ''){
	
	//LIKELY UNNEEDED
	let anchorElementList = [];

	//NEEDS DOCUMENTATION AND REAL VARIABLE NAMES
	function arrayUnique(array) {
	    let a = array.concat();
	    for(let i=0; i<a.length; ++i) {
	        for(let j=i+1; j<a.length; ++j) {
	            try{
	                if(a[i].getAttribute('href') === a[j].getAttribute('href'))
	                    a.splice(j--, 1);
	           }
	            catch(err){}
	        }
	    }

	    return a;
	}

	//Loop through the elements with the matching class name and store the unique links
	let currentElements = document.getElementsByClassName(containerClassName);
	for (let curIndex = 0; curIndex < currentElements.length; curIndex++) {
	    
	    let tempAnchorElements = currentElements[curIndex].getElementsByTagName('a');
	    let ca = arrayUnique(Array.prototype.slice.call(tempAnchorElements).concat(Array.prototype.slice.call(anchorElementList)));

	    anchorElementList = ca;

	}
	anchorElements = anchorElementList;
}

/*******************************************************************************************/
/*********************************** Format Found Links ************************************/
/*******************************************************************************************/

//Loop through the anchor elements and store the detailed information on each
let anchorInfoList = [];
if (anchorElements != null){
	for (let curIndex = 0; curIndex < anchorElements.length; curIndex++) {

		//Store the element's basic attributes 
		let currentAnchorInfo = {};
		currentAnchorInfo['id'] = anchorElements[curIndex].getAttribute('id');
		currentAnchorInfo['href'] = anchorElements[curIndex].getAttribute('href');
		currentAnchorInfo['name'] = anchorElements[curIndex].getAttribute('name');
		currentAnchorInfo['onclick'] = anchorElements[curIndex].getAttribute('onclick');
		currentAnchorInfo['text'] = anchorElements[curIndex].text.replace(/(\r\n|\n|\r)/gm,"").trim();
		currentAnchorInfo['style'] = anchorElements[curIndex].getAttribute('style');
		currentAnchorInfo['title'] = anchorElements[curIndex].getAttribute('title');
		
		//Store the element's class. If none exist, grab the first available parent class within reasonable crawl.
		let currentClass = anchorElements[curIndex].getAttribute('class');
		let currentParentHeight = 0;
		let currentParentNode = anchorElements[curIndex].parentNode;
		while ((!currentClass) && (currentParentHeight < MAXPARENTSEARCHHEIGHT)) {
			
			try{
				currentClass = currentParentNode.getAttribute('class');
			}
			catch(err){

			}
			if ((currentParentNode) && (currentParentNode.parentNode)){
				currentParentNode = currentParentNode.parentNode;
			}
			++currentParentHeight;
		}
		
		//If a class was found, add it to the info object. Otherwise, leave an empty string.
		if (currentClass) {currentAnchorInfo['className'] = currentClass;}
		else {currentAnchorInfo['className'] = "";}
		
		//Determine the anchor's location and size then add it to the info object
		let box = anchorElements[curIndex].getBoundingClientRect();
		let body = document.body;
		let docElem = document.documentElement;
		let scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop;
		let scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft;
		let clientTop = docElem.clientTop || body.clientTop || 0;
		let clientLeft = docElem.clientLeft || body.clientLeft || 0;
		let top  = box.top +  scrollTop - clientTop;
		let left = box.left + scrollLeft - clientLeft;
		let currWidth = box.right - left;
		let currHeight = box.bottom - box.top;
		currentAnchorInfo['yPosition'] = Math.round(top);
		currentAnchorInfo['xPosition'] = Math.round(left);
		currentAnchorInfo['width'] = Math.round(currWidth);
		currentAnchorInfo['height'] = Math.round(currHeight);
		
		//Finaly, add the info to the main list
		anchorInfoList[curIndex] = currentAnchorInfo;
			
	}
	//Convert the anchor info list into JSON and return it
	return JSON.stringify(anchorInfoList);
}

else{
	return "FAILURE - Unable to find any anchors for the request";
}
