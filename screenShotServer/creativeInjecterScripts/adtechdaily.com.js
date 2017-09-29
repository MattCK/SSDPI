allSelectors.push(new AdSelector("div.td-header-rec-wrap", true).addSize(728, 90));
allSelectors.push(new AdSelector("div#bsap_1282", true).addSize(300, 250));
allSelectors.push(new AdSelector("div#bsap_1280", true).addSize(300, 250));
if (document.documentElement.clientWidth <= 767) {

	setTimeout(function() { 
		if (document.querySelector(".td-header-rec-wrap img")) {

			document.querySelector(".td-header-rec-wrap img").style.width = "100%";
			document.querySelector(".td-header-rec-wrap img").style.height = "100%";
			document.querySelector(".td-header-rec-wrap img").style.margin = "10px auto";
		}
	}, 750);

}
