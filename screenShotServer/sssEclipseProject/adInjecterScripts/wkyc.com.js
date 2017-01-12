/**
*  wkyc.com exception script
*
*  On desktop Chrome, the top banner ad often is not displayed at all or is displayed 
*  as a much larger ad size. When the latter occurs, the image is replaced by the
*  Ad Injecter but is stretched to the larger size.
*
*  This script replaces the entire ad div element, and its children, with a dummy
*  holder div with the correct margin and 728x90 size. A second 728x90 div
*  is placed inside of it with the flood-opacity set so it will be replaced
*  by the ad injecter.
*
*  The div used on the page has the id 'layout-column_column-1'.
*/

let found728x90 = false;
let found320x50 = false;
//Check to see if a 728x90 or 320x50 tag has been passed by the AdShotter
for (tagIndex in tags) {

    let currentTag = tags[tagIndex];
    if ((currentTag.width == 728) && (currentTag.height == 90)) {
    	found728x90= true;
    }
    if ((currentTag.width == 320) && (currentTag.height == 50)) {
    	found320x50= true;
    }
}
//Get the div holding the banner ad
//this selector works for mobile and desktop
let bannerAdDiv = document.querySelector("#layout-column_column-1 div.portlet-body");

//Run exception on desktop browsers (no mobile)
if ((!navigator.userAgent.toLowerCase().includes("mobile"))) {

	//If a 728x90 tag has been passed, replace the header ad with a 728x90 div
	//and then place a second div inside of the first for the ad injecter to
	// replace
	if (found728x90) {

		//remove all of the elements children
		while (bannerAdDiv.firstChild) {
			bannerAdDiv.removeChild(bannerAdDiv.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		bannerAdDiv.style.visibility = 'visible';
		bannerAdDiv.style.width = '728px';
		bannerAdDiv.style.height = '90px';
		bannerAdDiv.style.margin = "0 auto";
		bannerAdDiv.style.marginBottom = "10px";

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '728px';
		adFillerDiv.style.height = '90px';
		bannerAdDiv.appendChild(adFillerDiv);
	}
}
//Run exception on mobile
if ((navigator.userAgent.toLowerCase().includes("mobile"))) {

	//If a 320x50 tag has been passed, replace the header ad with a 320x50 div
	//and then place a second div inside of the first for the ad injecter to
	// replace
	if (found320x50) {

		//remove all of the elements children
		while (bannerAdDiv.firstChild) {
			bannerAdDiv.removeChild(bannerAdDiv.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		bannerAdDiv.style.visibility = 'visible';
		bannerAdDiv.style.width = '320px';
		bannerAdDiv.style.height = '50px';
		bannerAdDiv.style.margin = "0 auto";
		bannerAdDiv.style.marginBottom = "10px";
		bannerAdDiv.style.martinTop = "10px"

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '320px';
		adFillerDiv.style.height = '50px';
		bannerAdDiv.appendChild(adFillerDiv);
	}
}

