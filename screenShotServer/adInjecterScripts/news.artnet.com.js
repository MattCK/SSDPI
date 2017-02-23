/**
*  news.artnet.com exception script
*
*  On desktop Chrome, the top banner ad often is not displayed at all. 
*
*  This script replaces the entire ad div element, and its children, with a dummy
*  holder div with the correct margin and 970x250 size. A second 970x250 div
*  is placed inside of it with the flood-opacity set so it will be replaced
*  by the ad injecter.
*
*/

//-------------------------- Testing Tags (Comment out before upload!!!)
/*
tags = [
	{id: '28577acb-9fbe-4861-a0ef-9d1a7397b4c9', tag: 'https://s3.amazonaws.com/asr-images/fillers/nsfiller-994x250.jpg', placement: 0, width: 994, height: 250},
	{id: 'ab4ec323-f91b-4578-a6c8-f57e5fca5c87', tag: 'https://s3.amazonaws.com/asr-images/fillers/nsfiller-300x480.jpg', placement: 0, width: 300, height: 480},
	{id: 'b4cce6c3-d68c-4cb4-b50c-6c567e0d3789', tag: 'https://s3.amazonaws.com/asr-images/fillers/nsfiller-970x250.jpg', placement: 0, width: 970, height: 250},
	{id: '312e383f-314e-4ba2-85f0-5f6937990fa6', tag: 'https://s3.amazonaws.com/asr-images/fillers/nsfiller-300x600.jpg', placement: 0, width: 300, height: 600}
];//*/

//Check to see if a 728x90 or 320x50 tag has been passed by the AdShotter
let found970x250 = false;
let found320x50 = false;
let found300x250 = false;
let found300x600 = false;
let found994x250 = false;
let found300x480 = false

for (tagIndex in tags) {

    let currentTag = tags[tagIndex];
    if ((currentTag.width == 970) && (currentTag.height == 250)) {
    	found970x250 = true;
    }
    if ((currentTag.width == 320) && (currentTag.height == 50)) {
    	found320x50 = true;
    }
    if ((currentTag.width == 994) && (currentTag.height == 250)) {
    	found994x250 = true;
    }
    if ((currentTag.width == 300) && (currentTag.height == 480)) {
    	found300x480 = true;
    }
    if ((currentTag.width == 300) && (currentTag.height == 250)) {
    	found300x250 = true;
    }

}

//Get the div holding the banner ad
//this selector works for mobile and desktop

let bannerAdDiv = document.querySelector("section div ul li div.artnet-ads-ad.widget-1.widget-odd.widget");
let largeMobileAd = document.querySelector("div.embedded-ad.visible-sm.visible-xs.ad-loaded");


//Run exception on desktop browsers (no mobile)
if ((!navigator.userAgent.toLowerCase().includes("mobile"))) {

	//////////////////////////// Banner Ad Desktop ///////////////////////////////

	//If a 970x250 tag has been passed, replace the header ad with a 728x90 div
	//and then place a second div inside of the first for the ad injecter to
	// replace

	if (found994x250) {

		//remove all of the elements children
		while (bannerAdDiv.firstChild) {
			bannerAdDiv.removeChild(bannerAdDiv.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		bannerAdDiv.style.visibility = 'visible';
		bannerAdDiv.style.width = '994px';
		bannerAdDiv.style.height = '250px';
		bannerAdDiv.style.margin = "0 auto";
		bannerAdDiv.style.marginBottom = "10px";

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '994px';
		adFillerDiv.style.height = '250px';
		bannerAdDiv.appendChild(adFillerDiv);
	}

	if (found970x250) {

		//remove all of the elements children
		while (bannerAdDiv.firstChild) {
			bannerAdDiv.removeChild(bannerAdDiv.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		bannerAdDiv.style.visibility = 'visible';
		bannerAdDiv.style.width = '970px';
		bannerAdDiv.style.height = '250px';
		bannerAdDiv.style.margin = "0 auto";
		bannerAdDiv.style.marginBottom = "10px";

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '970px';
		adFillerDiv.style.height = '250px';
		bannerAdDiv.appendChild(adFillerDiv);
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

	//////////////////////////// large mobile Ad Desktop ///////////////////////////////

	//If there is a side column, a 300x250 tag, BUT NOT a 300x480, 
	//replace the ad element with a filler
	if ((largeMobileAd) && (found300x250) && (!found300x480)) {

		console.log("Column and 300x50");
		//remove all of the elements children
		while (largeMobileAd.firstChild) {
			largeMobileAd.removeChild(largeMobileAd.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		largeMobileAd.style.visibility = 'visible';
		largeMobileAd.style.width = '300px';
		largeMobileAd.style.height = '250px';
		largeMobileAd.style.margin = "0 auto";
		//largeMobileAd.style.marginLeft = "20px";
		largeMobileAd.parentElement.style.marginLeft = "20px";
		//largeMobileAd.style.marginBottom = "10px";

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '300px';
		adFillerDiv.style.height = '250px';
		largeMobileAd.appendChild(adFillerDiv);
	}

	//If there is a side column, a 300x480 tag, BUT NOT a 300x250, 
	//replace the ad element with a filler
	else if ((largeMobileAd) && (found300x480) && (!found300x250)) {

		//remove all of the elements children
		while (largeMobileAd.firstChild) {
			largeMobileAd.removeChild(largeMobileAd.firstChild);
		}

		//Set the banner ads size, margin, and visibility so that it is centered
		//and placed correctly.
		largeMobileAd.style.visibility = 'visible';
		largeMobileAd.style.width = '300px';
		largeMobileAd.style.height = '480px';
		largeMobileAd.style.margin = "0 auto";
		//largeMobileAd.style.marginLeft = "20px";
		//largeMobileAd.parentElement.style.marginLeft = "20px";
		//largeMobileAd.style.marginBottom = "10px";

		//Create the ad filler div which will be replaced by the ad injecter
		//and add it inside the banner ad div
		let adFillerDiv =  document.createElement('div');
		adFillerDiv.style.floodOpacity = "0.9898";
		adFillerDiv.style.width = '300px';
		adFillerDiv.style.height = '480px';
		largeMobileAd.appendChild(adFillerDiv);
	}
}

