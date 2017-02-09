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

//-------------------------- Testing Tags (Comment out before upload!!!)
/*
tags = [
	{id: '28577acb-9fbe-4861-a0ef-9d1a7397b4c9', tag: 'http://s3.amazonaws.com/asr-tagimages/07809e6b-9f3a-42aa-8fe0-6ba0adb102d0.png', placement: 0, width: 728, height: 90},
	{id: 'ab4ec323-f91b-4578-a6c8-f57e5fca5c87', tag: 'http://s3.amazonaws.com/asr-tagimages/f050eb7d-9a0c-4781-849c-bf34629e5695.png', placement: 0, width: 300, height: 250},
	//{id: 'b722d748-dd25-493e-93c5-6fc1991f6392', tag: 'http://s3.amazonaws.com/asr-tagimages/074df31b-25d1-4a19-9b42-c8b8ab780738.png', placement: 0, width: 300, height: 50},
	//{id: 'b4cce6c3-d68c-4cb4-b50c-6c567e0d3789', tag: 'http://s3.amazonaws.com/asr-tagimages/59b1ba0b-cf8a-4295-b578-fecefd91e907.png', placement: 0, width: 320, height: 50},
	//{id: '312e383f-314e-4ba2-85f0-5f6937990fa6', tag: 'http://s3.amazonaws.com/asr-tagimages/aa0a39ab-1abb-48a3-a2c2-458ae0b54c4f.png', placement: 0, width: 300, height: 600}
];//*/

//Check to see if a 728x90 or 320x50 tag has been passed by the AdShotter
let found728x90 = false;
let found320x50 = false;
let found300x250 = false;
let found300x600 = false;
for (tagIndex in tags) {

    let currentTag = tags[tagIndex];
    if ((currentTag.width == 728) && (currentTag.height == 90)) {
    	found728x90 = true;
    }
    if ((currentTag.width == 320) && (currentTag.height == 50)) {
    	found320x50 = true;
    }
    if ((currentTag.width == 300) && (currentTag.height == 250)) {
    	found300x250 = true;
    }
    if ((currentTag.width == 300) && (currentTag.height == 600)) {
    	found300x600 = true;
    }
}

//Get the div holding the banner ad
//this selector works for mobile and desktop
let bannerAdDiv = document.querySelector("#layout-column_column-1 div.portlet-body");

//Get the desktop right column ad if it exists
let columnAd = document.querySelector("#layout-column_column-3 .mod-wrapper.ad-300");

//See if this is a desktop story article
let isStoryArticle = (document.querySelector(".story-utility-bar")) ? true: false;

//Run exception on desktop browsers (no mobile)
if ((!navigator.userAgent.toLowerCase().includes("mobile"))) {

	//////////////////////////// Banner Ad Desktop ///////////////////////////////

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

	//Otherwise, hide the 728x90 in case it doesn't load correctly
	else {
		bannerAdDiv.parentElement.parentElement.parentElement.style.display = 'none';
	}

	//////////////////////////// Column Ad Desktop ///////////////////////////////

	//If there is a side column, a 300x250 tag, BUT NOT a 300x600, 
	//replace the ad element with a filler
	if ((columnAd) && (found300x250) && (!found300x600)) {

		console.log("Column and 300x50");
		//remove all of the elements children
		while (columnAd.firstChild) {
			columnAd.removeChild(columnAd.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		columnAd.style.visibility = 'visible';
		columnAd.style.width = '300px';
		columnAd.style.height = '250px';
		columnAd.style.margin = "0 auto";
		columnAd.style.marginLeft = "20px";
		columnAd.parentElement.style.marginLeft = "20px";
		//columnAd.style.marginBottom = "10px";

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '300px';
		adFillerDiv.style.height = '250px';
		columnAd.appendChild(adFillerDiv);
	}

	//If there is a side column, a 300x600 tag, BUT NOT a 300x250, 
	//replace the ad element with a filler
	else if ((columnAd) && (found300x600) && (!found300x250)) {

		//remove all of the elements children
		while (columnAd.firstChild) {
			columnAd.removeChild(columnAd.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		columnAd.style.visibility = 'visible';
		columnAd.style.width = '300px';
		columnAd.style.height = '600px';
		columnAd.style.margin = "0 auto";
		columnAd.style.marginLeft = "20px";
		columnAd.parentElement.style.marginLeft = "20px";
		//columnAd.style.marginBottom = "10px";

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '300px';
		adFillerDiv.style.height = '600px';
		columnAd.appendChild(adFillerDiv);
	}
}

//Run exception on mobile
if ((navigator.userAgent.toLowerCase().includes("mobile"))) {

	//If a 320x50 tag has been passed, replace the header ad with a 320x50 div
	//and then place a second div inside of the first for the ad injecter to
	// replace
	if (found320x50) {

		//Remove all of the elements children
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

