//Create a driver webpage and set the viewport  
var WINDOWWIDTH = 1024;
var WINDOWHEIGHT = 768;
var page = require('webpage').create();
page.viewportSize = { width: WINDOWWIDTH, height: WINDOWHEIGHT };

//Grab the passed target URL to grab stories from
var system = require('system');
var targetURL = system.args[1];

//Try to connect to and open the target URL
page.open(targetURL, function(status) {

	//If the connection failed, notify and end execution	
	if (status !== 'success') {
		console.log('Unable to access network');
	} else {
	
		//Inject the function to grab the formatted anchor objects
		var anchorListJSON = page.evaluate(function() {
		
			//Grab all anchor elements from the page
			var anchorElements = document.getElementsByTagName('a');
			
			//Loop through each elements and grab its own attributes
			var anchorInfoList = [];
			for (var i = 0; i < anchorElements.length; i++) {
				try {
					//Scroll the window 1000 pixels down
					window.scrollBy(0,1000);
					
					//Store the element's basic attributes 
					var curAnchorInfo = {};
					curAnchorInfo['href'] = anchorElements[i].getAttribute('href');
					curAnchorInfo['text'] = anchorElements[i].text.replace(/(\r\n|\n|\r)/gm,"").trim();
					curAnchorInfo['name'] = anchorElements[i].getAttribute('name');
					curAnchorInfo['style'] = anchorElements[i].getAttribute('style');
					curAnchorInfo['id'] = anchorElements[i].getAttribute('id');
					curAnchorInfo['title'] = anchorElements[i].getAttribute('title');
					curAnchorInfo['onclick'] = anchorElements[i].getAttribute('onclick');
					
					//Store the element's class. If none exist, grab the first available parent class.
					var loopCount = 0;
					var maxParentSearch = 10;
					var curClass = anchorElements[i].getAttribute('class');
					var curParentNode = anchorElements[i].parentNode;
					while ((!curClass) && (loopCount < 10)){
						curClass = curParentNode.getAttribute('class');;
						curParentNode = curParentNode.parentNode;
						++loopCount;
					}
					
					//If a class was found, add it to the info object. Otherwise, leave an empty string.
					if (curClass) {curAnchorInfo['class'] = curClass;}
					else {curAnchorInfo['class'] = "";}
					
					//Determine the anchor's location and add it to the info object
					var box = anchorElements[i].getBoundingClientRect();
					var body = document.body;
					var docElem = document.documentElement;
					var scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop;
					var scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft;
					var clientTop = docElem.clientTop || body.clientTop || 0;
					var clientLeft = docElem.clientLeft || body.clientLeft || 0;
					var top  = box.top +  scrollTop - clientTop;
					var left = box.left + scrollLeft - clientLeft;
					curAnchorInfo['yPos'] = Math.round(top);
					curAnchorInfo['xPos'] = Math.round(left);

					//Because we need them later, add score and reasons holders
					curAnchorInfo['score'] = 0;
					curAnchorInfo['reasons'] = "";
					
					//Finaly, add the info to the main list
					anchorInfoList[i] = curAnchorInfo;
					
				} catch (err) { }
			}
			
			//Convert the anchor info list into JSON and return it
			return JSON.stringify(anchorInfoList);
		});
		
		//List of good names for classes to have
		var goodClassNames = [
			"feature",
			"headline",
			"story",
			"cell"
		];
		
		//List of bad names for classes to have
		var badClassNames = [
			"trending"
		];

		//Grab an image of the page for future comparison
		var curDate = new Date();
		var curTime = curDate.getTime(); 
		var screenShotName = targetURL.replace("http://", "").replace("https://", "").replace("/", "-").replace(":", "-").replace("\\", "-") + curTime + ".png";
		screenShotName = screenShotName.replace("/", "-");
		var screenShotDirectory = "pics/";
		page.render(screenShotDirectory + screenShotName);
		
		//Convert the anchor list JSON to an array
		var anchorList = {};
		anchorList = JSON.parse(anchorListJSON);

		//If the anchor points to a domain other than the target URL, remove it
		anchorList = removeLinksToOtherDomains(anchorList);
		
		//Give the anchors in the list scores based on their location on the page
		anchorList = ratePageLocation(anchorList, WINDOWWIDTH);
		
		//Modifies the anchors' scores based on their length
		anchorList = rateLength(anchorList);
		
		//Increases the scores of anchors whose path first part is the same as the target url
		anchorList = ratePathFirstPart(anchorList);
		
		//Decrease the scores of the anchors that have no text or contain only uppercase letters
		anchorList = rateAllCapsAndNoText(anchorList);
				
		//Get the class with the highest score
		var highestClass = getClassWithHighestScore(anchorList);
		
		//Get the anchor list that only have the top class
		var finalAnchorList = [];
		for (var listKey in anchorList) {
			if (anchorList[listKey]['class'] == highestClass) {
				finalAnchorList[finalAnchorList.length] = anchorList[listKey]; 
			}
		}

		printHTML(finalAnchorList, targetURL, screenShotDirectory + screenShotName);

	}
	
	//Completely terminate this running script
	phantom.exit();
});

/**
* 	Remove anchors from list that point to other domains
**/
function removeLinksToOtherDomains(anchorListLocal){

	//Grab the domain of the target URL to use as a base
	try{
		var targetHost = parseURI(targetURL);
		var targetDomain = getDomain(targetHost);
	}
	
	//If the domain could not be determined, simply set it to an empty string for now
	catch(err){
		targetDomain = "";
	}
	
	//Check each anchors domain. If it doesn't match the target's, remove it from the list.
	for (var key in anchorListLocal) {
		var curAnchor = anchorListLocal[key];
		try {
		  if (curAnchor['href'] && (curAnchor['href'].substring(0,1) != '/')) {
				var curHost = parseURI(curAnchor['href']);
				var curDomain = getDomain(curHost);
			
				if(curDomain != targetDomain){
					delete anchorListLocal[key];
				}
			}
		}
		//If there was an error, delete the current anchor and set the domain to empty string to delete the rest
		catch(err1){
			delete anchorListLocal[key];
			curDomain = "";
		}	

	}

	return anchorListLocal;
}


/**
* 	Returns the host string of the passed URI
**/
function parseURI(url) {
	pathArray = url.split( '/' );
	urlHost = pathArray[2];
	return pathArray[2];
};

/**
* 	Returns the domain of the passed URL
**/
function getDomain(url){

	//List all the current possible TLDs (about to change very soon)
	var TLDs = ["ac", "ad", "ae", "aero", "af", "ag", "ai", "al", "am", "an", "ao", "aq", "ar", "arpa", "as", "asia", "at", "au", "aw", "ax", "az", "ba", "bb", "bd", "be", "bf", "bg", "bh", "bi", "biz", "bj", "bm", "bn", "bo", "br", "bs", "bt", "bv", "bw", "by", "bz", "ca", "cat", "cc", "cd", "cf", "cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "com", "coop", "cr", "cu", "cv", "cx", "cy", "cz", "de", "dj", "dk", "dm", "do", "dz", "ec", "edu", "ee", "eg", "er", "es", "et", "eu", "fi", "fj", "fk", "fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gg", "gh", "gi", "gl", "gm", "gn", "gov", "gp", "gq", "gr", "gs", "gt", "gu", "gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu", "id", "ie", "il", "im", "in", "info", "int", "io", "iq", "ir", "is", "it", "je", "jm", "jo", "jobs", "jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr", "kw", "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", "ls", "lt", "lu", "lv", "ly", "ma", "mc", "md", "me", "mg", "mh", "mil", "mk", "ml", "mm", "mn", "mo", "mobi", "mp", "mq", "mr", "ms", "mt", "mu", "museum", "mv", "mw", "mx", "my", "mz", "na", "name", "nc", "ne", "net", "nf", "ng", "ni", "nl", "no", "np", "nr", "nu", "nz", "om", "org", "pa", "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", "pro", "ps", "pt", "pw", "py", "qa", "re", "ro", "rs", "ru", "rw", "sa", "sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "sk", "sl", "sm", "sn", "so", "sr", "st", "su", "sv", "sy", "sz", "tc", "td", "tel", "tf", "tg", "th", "tj", "tk", "tl", "tm", "tn", "to", "tp", "tr", "travel", "tt", "tv", "tw", "tz", "ua", "ug", "uk", "us", "uy", "uz", "va", "vc", "ve", "vg", "vi", "vn", "vu", "wf", "ws", "xn--0zwm56d", "xn--11b5bs3a9aj6g", "xn--3e0b707e", "xn--45brj9c", "xn--80akhbyknj4f", "xn--90a3ac", "xn--9t4b11yi5a", "xn--clchc0ea0b2g2a9gcd", "xn--deba0ad", "xn--fiqs8s", "xn--fiqz9s", "xn--fpcrj9c3d", "xn--fzc2c9e2c", "xn--g6w251d", "xn--gecrj9c", "xn--h2brj9c", "xn--hgbk6aj7f53bba", "xn--hlcj6aya9esc7a", "xn--j6w193g", "xn--jxalpdlp", "xn--kgbechtv", "xn--kprw13d", "xn--kpry57d", "xn--lgbbat1ad8j", "xn--mgbaam7a8h", "xn--mgbayh7gpa", "xn--mgbbh1a71e", "xn--mgbc0a9azcg", "xn--mgberp4a5d4ar", "xn--o3cw4h", "xn--ogbpf8fl", "xn--p1ai", "xn--pgbs0dh", "xn--s9brj9c", "xn--wgbh1c", "xn--wgbl6a", "xn--xkc2al3hye2a", "xn--xkc2dl3a5ee0h", "xn--yfro4i67o", "xn--ygbi2ammx", "xn--zckzah", "xxx", "ye", "yt", "za", "zm", "zw"].join();
	
	//Split the URL into its individual parts
    var urlParts = url.split('.');
	
	//If a 'www' precedes the domain, remove it
	//There's even an exception for www.com which is currently squatted 
    if (urlParts[0] === 'www' && urlParts[1] !== 'com'){
        urlParts.shift();
    }
	
    var numberOfURLParts = urlParts.length;
    var partsIterator = numberOfURLParts;
    var minLength = urlParts[urlParts.length-1].length;
    var curPart;

    //Iterate backwards through the parts to determine the domain
    while(curPart = urlParts[--partsIterator]){
		if (partsIterator === 0 ||                  // 'asia.com' (last remaining must be the SLD)
			partsIterator < numberOfURLParts - 2 ||  	// TLDs only span 2 levels
			curPart.length < minLength || 					// 'www.cn.com' (valid TLD as second-level domain)
			TLDs.indexOf(curPart) < 0  						// officialy not a TLD
			){
				return curPart;
		}
    }
}

/**
* 	Scores each anchor based on its location on the target page
**/
function ratePageLocation(anchorListLocal, windowWidthLocal) {

	//Loop through each anchor and modify its score based on placement
	for (var key in anchorListLocal) {
		
		//Grab the anchor to work with
		var curAnchor = anchorListLocal[key];
		
		//Determine the one third of the page x coordinate and the anchors distance from it
		var oneThirdWidthPoint = Math.round(windowWidthLocal / 3);
		var pixelsFromOneThirdWidth = Math.abs(curAnchor['xPos'] - oneThirdWidthPoint);

		//Determines the point on the page used in scoring. The distance between the one-third point
		//and this point determines the ratio used. (ex: 2 equals half point on page)
		var REFERENCERATIO = 2;
		var referencePoint = Math.round(windowWidthLocal / REFERENCERATIO);
		
		//-------------------- HORIZONTAL SCORE MULTIPLER---------------------------
		var horizontalScoreMultiplier = ((referencePoint - pixelsFromOneThirdWidth) / referencePoint);
		
		//Set the anchor's score based on the multiplier
		var MAXSCOREMULTIPLIER = 20;
		var NOXPOSITIONSCOREREDUCTION  = -20;		
		if (curAnchor['xPos'] > 0) {
			curAnchor['score'] += horizontalScoreMultiplier * MAXSCOREMULTIPLIER;
			curAnchor['score'] = Math.round(curAnchor['score']);
		}
		else {
			curAnchor['score'] += NOXPOSITIONSCOREREDUCTION;
			curAnchor['reasons'] += " : noXPos";
		}

		//Set the reason to the multipler
		if (horizontalScoreMultiplier >= .5){curAnchor['reasons'] += " : over50Multi";}
		else {curAnchor['reasons'] += " : under50Multi";}
		
		
		//If the anchor is unreasonably high, reduce its score
		var TOPHEIGHTYPOSITION = 350;
		var LOWERHEIGHTYPOSITION = 475;
		var TOPHEIGHTSCOREREDUCTION = -11;
		var LOWERHEIGHTSCOREREDUCTION = -6;
		if (curAnchor['yPos'] <= TOPHEIGHTYPOSITION){
			curAnchor['score'] += TOPHEIGHTSCOREREDUCTION;
			curAnchor['reasons'] += " : tooHigh";
		}
		//------------------- high on page?-------------
		else if ((curAnchor['yPos'] > TOPHEIGHTYPOSITION) && (curAnchor['yPos'] < LOWERHEIGHTYPOSITION)){
			curAnchor['score'] += LOWERHEIGHTSCOREREDUCTION;
			curAnchor['reasons'] += " : prettyHigh";
		}
	}
	
	return anchorListLocal;
}

/**
* 	Scores each anchor based on its text length
**/
function rateLength(anchorListLocal){
	
	//Define the constants used
	var LONGLENGTH = 16;
	var SHORTLENGTH = 9;
	var LONGSCORE = 11;
	var SHORTSCOREREDUCTION = -10;
	var SPACESCORE = 10;
	
	//Loop through each anchor and modify the scores as necessary
	for (var key in anchorListLocal) {
	
		var curAnchor = anchorListLocal[key];
		
		//If the text is long enough, increase the score
		if (curAnchor['text'].length > LONGLENGTH){
			curAnchor['score'] += LONGSCORE;
			curAnchor['reasons'] += " : longString";
		}
		
		//If the text is too short, decrease the score
		else if (curAnchor['text'].length <= SHORTLENGTH){
			curAnchor['score'] += SHORTSCOREREDUCTION;
			curAnchor['reasons'] += " : shortString";
		}
		
		//If there are at least two spaces, increase the score
		if (countSpaces(curAnchor['text']) >= 2 ) {
			curAnchor['score'] += SPACESCORE;
			curAnchor['reasons'] += " : 2pSpaces";
		}
	}

	return anchorListLocal;
}


/**
* 	Returns the number of spaces in a passed string
**/
function countSpaces(text){
	return text.split(/\s/).length - 1;
}

/**
* 	Increases the anchors who share the same path first part as the target url
**/
function ratePathFirstPart(anchorListLocal){
	
	//Get the first part/folder in the target URL's path
	var targetPathFirstPart = getURLPathFirstPart(targetURL);
	
	//Loop through the anchors. If their link has the same first 
	//path part as the target's, increment their score
	for (var key in anchorListLocal) {
	
		//Get the first path part of the current anchor
		var curAnchor = anchorListLocal[key];
		if (curAnchor['href']) {
			var curPathFirstPart = getURLPathFirstPart(curAnchor['href']);
		}
		
		//If the first path parts are the same, increase the score
		var SAMEPATHPARTSCORE = 7;
		if (curPathFirstPart == targetPathFirstPart) {
			curAnchor['score'] += SAMEPATHPARTSCORE;
			curAnchor['reasons'] += " : samePath";
		}
	} 
	
	return anchorListLocal;
}

/**
* 	Returns the path's first part of the passed url
**/
function getURLPathFirstPart(url){

	//Separte the URL into parts
	var urlParts = url.split( '/' );
	
	//If there is no protocol, just a path, return the second part
	if((url != null) && url.substring(0,1) == '/'){
		return urlParts[1];
	}
	
	//Otherwise return the 4th part, after the domain
	else{
		return urlParts[3]
	}
}

/**
* 	If an anchor's text has no text or is all caps, decrease its score
**/
function rateAllCapsAndNoText(anchorListLocal){

	//Loop through each element and decrement its score if necessary
	for (var key in anchorListLocal) {

		var curAnchor = anchorListLocal[key];
		
		//If the anchor has no text, reduce its score
		var NOTEXTSCOREREDUCTION = -12;
		var ALLUPPERCASESCOREREDUCTION = -12;
		if (curAnchor['text'] == ""){
			curAnchor['score'] = curAnchor['score'] - NOTEXTSCOREREDUCTION;
			curAnchor['reasons'] = curAnchor['reasons'] + " : emptyText";
		}
		else {
			if (isUpperCase(curAnchor['text'])){
				curAnchor['score'] = curAnchor['score'] - ALLUPPERCASESCOREREDUCTION;
				curAnchor['reasons'] = curAnchor['reasons'] + " : allCaps";
			}
		}
	}

	return anchorListLocal;
}

/**
* 	Returns true if the passed text is uppercase, otherwise false
**/
function isUpperCase(text) {
    return text === text.toUpperCase();
}

/**
* 	Returns the class of the anchor list with the highest score.
*	Score is determined as the sum of the scores of anchors with the class divided by the amount of anchors
**/
function getClassWithHighestScore(anchorListLocal){
	
	//Loop through each element and add its class to the scores
	var classScores = [];
	for (var key in anchorListLocal) {
		var curAnchor = anchorListLocal[key];
		
		//Loop through the classes already processed and see if the anchor's class matches any
		var classMatched = false;
		var classScoresCounter = 0;
		while((!classMatched) && (classScoresCounter < classScores.length)){
			if(classScores[classScoresCounter]['className'] == curAnchor['class']){
				classMatched = true;
			}
			else {
				classScoresCounter++;
			}
		}
		
		//If the class matched, increase that class' score and count number
		if (classMatched){
			classScores[classScoresCounter]['score'] += curAnchor['score'];
			classScores[classScoresCounter]['count'] ++;
		}
		
		//Otherwise, add the class to the array
		else{
			classScores[classScores.length] = { className: curAnchor['class'], score: curAnchor['score'], count: 1};
		}
	}
	
	//Determine the averages and calculate the highest ranked class
	for(var i = 0; i < classScores.length; i++){
		if(classScores[i]['count'] >= 4){
			classScores[i]['score'] = classScores[i].score/classScores[i].count;
		}
		else{
			classScores[i]['score'] = (classScores[i].score/classScores[i].count)/2;
		}
	}
	
	//Sort the array by reverse score to get heighest class
	classScores.sort(function(a,b) { return b.score - a.score } );
	
	//Return the highest scored class name
	return classScores[0]['className'];
}

/**
* 	Sends the viewable interface HTML to standard out
**/
function printHTML(anchorListLocal, url, shotPath){
	var htmlOut = '<head><meta charset="utf-8" /><style>table, th,td{ border-collapse:collapse; border:1px solid black; } th, td{ padding:5px; }</style></head><body><table><tr> <td>  <p>Site Image: [[siteURL]]</p></td><td><p>Site Links</p> </td> </tr> <tr><td><img src="[[picPath]]" alt="SitePic" width="800" ></td><td>';
	htmlOut = htmlOut.replace('[[siteURL]]', url);
	htmlOut = htmlOut.replace('[[picPath]]', shotPath);
	htmlOut = htmlOut + "\n";
	for (var key in anchorListLocal) {
    if (anchorListLocal.hasOwnProperty(key)) {
      var obj = anchorListLocal[key];
	  htmlOut = htmlOut + "<b>" +  obj['text'] + "</b></br>";
	  htmlOut = htmlOut + obj['href'] + "</br>";
	  htmlOut = htmlOut + obj['reasons'] + "</br>";
	  htmlOut = htmlOut + "\n";
	  htmlOut = htmlOut + "<b>" +  obj['score'] + "</b></br>|: " + obj['class'] + " Location: " + obj['xPos'] + "x" + obj['yPos'] + "</br>";
	  htmlOut = htmlOut + "\n";
	  htmlOut = htmlOut + "<hr noshade>";
	  htmlOut = htmlOut + "\n";
	  }
	 }
	 htmlOut = htmlOut + '</td></tr></table></body>';
	 console.log(htmlOut);
}



/*****************************************ANTIQUATED******************************************/


//print to csv
function printCSV(anchorListLocal){
  for (var key in anchorListLocal) {
    if (anchorListLocal.hasOwnProperty(key)) {
      var obj = anchorListLocal[key];
	  console.log("'" + obj['score'] + "','" + obj['class'] + "','" + obj['href'] + "','" + obj['text'] + "','" + obj['reasons'] + "','" + obj['xPos'] + "x" + obj['yPos'] + "'");

	  }
	 }
}
//rate classname in class ranking
function rateClassNames(anchorListLocal, goodClassNamesLocal, badClassNamesLocal){
  for (var key in anchorListLocal) {
    if (anchorListLocal.hasOwnProperty(key)) {
      var obj = anchorListLocal[key];
	  //console.log("ahhhhWTF?");
		for(var classCount in goodClassNamesLocal){
			//console.log("inside goodclasslist");
			if(goodClassNamesLocal.hasOwnProperty(classCount))
			{
				var classObj = goodClassNamesLocal[classCount]
				//console.log("ok inside the goodclasname: " + classObj + " : " + obj['className']);
				var curGoodListName = classObj.toLowerCase();
				var curClassName = obj['className'].toLowerCase();
				if (curClassName.indexOf(curGoodListName) != -1){
				//console.log("B good class names matched! " + obj['className'] + "" + obj['score']);
				obj['score'] += 10;
				//console.log("A class names matched! " + obj['className'] + "" + obj['score']);
				}
			}
		}
		for(var classCount in badClassNamesLocal){
			//console.log("inside badclasslist");
			if(badClassNamesLocal.hasOwnProperty(classCount))
			{
				var classObj = badClassNamesLocal[classCount]
				//console.log("ok inside the badclasname: " + classObj + " : " + obj['className']);
				var curBadListName = classObj.toLowerCase();
				var curClassName = obj['className'].toLowerCase();
				if (curClassName.indexOf(curBadListName) != -1){
				//console.log("B bad class names matched! " + obj['className'] + "" + obj['score']);
				obj['score'] -= 5;
				//console.log("A class names matched! " + obj['className'] + "" + obj['score']);
				}
			}
		}

	  }
	 }
  return anchorListLocal;
}



//
function returnClassAt(anchorListLocal, classRankingsLocal, classIndex){
if(classIndex < anchorListLocal.length){
	/*for(var i = 0; i < classRankingsLocal.length; i++){
		console.log("classname: " + classRankingsLocal[i]['className']);
		console.log("classRank: " + classRankingsLocal[i]['score']);
	}*/
	var className = classRankingsLocal[classIndex]['className']
	var returnList = [];
	var returnListIndex = 0;
	for (var key in anchorListLocal) {
		var curAnchor = anchorListLocal[key];
		if(className == curAnchor['class']){
			returnList[returnListIndex] = curAnchor;
			returnListIndex++;
			//console.log("matched class - " + curAnchor['class']);
		}
		else{
			//console.log(className + " - didn't match class - " + curAnchor['class']);
		}
	}
	   
   return returnList;
 }
 
 else{
	return false;
 }
}

//insertion sort
function insertionSort(items) {

    var len     = items.length;     // number of items in the array
    var value;                      // the value currently being compared
    var i;                          // index into unsorted section
    var j;                          // index into sorted section
    
    for (i=0; i < len; i++) {
    
        // store the current value because it may shift later
        value = items[i];
        //console.log('logme: ' + value);
        /*
         * Whenever the value in the sorted section is greater than the value
         * in the unsorted section, shift all items in the sorted section over
         * by one. This creates space in which to insert the value.
         */
        for (j=i-1; j > -1 && items[j]['score'] < value['score']; j--) {
            items[j+1] = items[j];
        }

        items[j+1] = value;
    }
    
    return items;
}


