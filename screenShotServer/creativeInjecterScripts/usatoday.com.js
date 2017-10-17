allSelectors.push(new AdSelector(".partner-pushdown-ad", true).addSize(1320, 743));
allSelectors.push(new AdSelector(".partner-leavebehind", true).addSize(800, 40));
if (document.querySelector(".partner-gravity-ad") != null) {
	document.querySelector(".partner-gravity-ad").style.display = 'none'
}
if (document.getElementById("cards") != null) {
	document.getElementById("cards").style.marginTop = 'auto';
}