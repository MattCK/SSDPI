allSelectors.push(new AdSelector("div#crown9x1_frame", true).addSize(1920, 520));
if (document.documentElement.clientWidth <= 767) {
	if(ElementInfo.yPosition(document.querySelector("div.pusher.headroom.hr-top")) == 0){
		document.querySelector("div.pusher.headroom.hr-top").style.marginTop = "40px";

	}
}
