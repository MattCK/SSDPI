allSelectors.push(new AdSelector(".portlet-layout .partner-pushdown-ad", true).addSize(1320, 743));
allSelectors.push(new AdSelector(".portlet-layout .partner-leavebehind", true).addSize(800, 40));

//Not hiding sometimes on the node. Unable to replicate reliably.
if (document.querySelector(".portlet-layout .partner-pushdown-ad")) {
	document.querySelector(".portlet-layout .partner-pushdown-ad").style.display = "none";
}

if (document.querySelector(".partner-gravity-ad") != null) {
	document.querySelector(".partner-gravity-ad").style.display = 'none'
}
if (document.getElementById("cards") != null) {
	document.getElementById("cards").style.marginTop = 'auto';
}
if (document.querySelector("#main.container")) {
	document.querySelector("#main.container").style.paddingTop = "0px";
}