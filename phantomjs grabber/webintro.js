var page = require('webpage').create();
var system = require('system');
// this beautiful bit of code is for making c
//onsole messages in the browser come out the console log in the runtime
page.onConsoleMessage = function(msg) {
    console.log(msg);
};
//console.log('The default user agent is ' + page.settings.userAgent);
//page.settings.userAgent = 'SpecialAgent';
var windowWidth = 1024;
var windowHeight = 768;
page.viewportSize = { width: windowWidth, height: windowHeight };
var headURL = system.args[1];
page.open(headURL, function(status) {
  if (status !== 'success') {
    console.log('Unable to access network');
  } else {
    var anchorListString = page.evaluate(function() {
		var linkTags = document.getElementsByTagName('a');
		var anchorAttributeList = [];
		for (var i = 0; i < linkTags.length; i++) {
			try {
				window.scrollBy(0,1000);
				var linkProps = {};
				linkProps['href'] = linkTags[i].getAttribute('href');
				linkProps['text'] = linkTags[i].text.replace(/(\r\n|\n|\r)/gm,"").trim();
				linkProps['name'] = linkTags[i].getAttribute('name');
				linkProps['style'] = linkTags[i].getAttribute('style');
				linkProps['id'] = linkTags[i].getAttribute('id');
				linkProps['title'] = linkTags[i].getAttribute('title');
				linkProps['onclick'] = linkTags[i].getAttribute('onclick');
				var tempProp = linkTags[i].getAttribute('class');
				var tempTag = linkTags[i].parentNode;
				var count = 0;
				//console.log("something here");
				while ((!tempProp) && (count < 10)){
					tempProp = tempTag.getAttribute('class');;
					tempTag = tempTag.parentNode;
					count ++;

				}
				if(tempProp){
					linkProps['class'] = tempProp;
				}
				else{
					linkProps['class'] = "";
				}

				var box = linkTags[i].getBoundingClientRect();
				var body = document.body;
				var docElem = document.documentElement;
				var scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop;
				var scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft;
				var clientTop = docElem.clientTop || body.clientTop || 0;
				var clientLeft = docElem.clientLeft || body.clientLeft || 0;
				var top  = box.top +  scrollTop - clientTop;
				var left = box.left + scrollLeft - clientLeft;
				linkProps['yPos'] = Math.round(top);
				linkProps['xPos'] = Math.round(left);
				
				
				linkProps['score'] = 0;
				linkProps['reasons'] = "";
				anchorAttributeList[i] = linkProps;
			} catch (err) { }
		}
		return JSON.stringify(anchorAttributeList);
    });
	//console.log(anchorListString);
	var anchorList = {};
	anchorList = JSON.parse(anchorListString);
	
	anchorList = removeLinksToOtherDomains(anchorList);
    anchorList = ratePageLocation(anchorList, windowWidth);
	anchorList = rateLength(anchorList);
	anchorList = rateFirstPath(anchorList);
	
	
	//printObject(anchorList);
	
	for (var key in anchorList) {
    if (anchorList.hasOwnProperty(key)) {
	  //console.log("stuff here")
      var obj = anchorList[key];
	  console.log("'" + obj['score'] + "','" + obj['class'] + "','" + obj['href'] + "','" + obj['text'] + "','" + obj['reasons'] + "'");
	  //console.log("text: " + obj['text']);
     // for (var prop in obj) {
      //   if (obj.hasOwnProperty(prop)) {
       //     console.log(prop + " = " + obj[prop]);
        // }
       //}
	  }
	 }
	
	//var anchorsListString2 = JSON.stringify(anchors2);
	//console.log(anchorsListString2);
	//console.log('printout1');
    //console.log(anchors2.link0.href);
	
  }
  phantom.exit();
});
//grade links based on the length of their text
function rateLength(anchorListLocal){

for (var key in anchorListLocal) {
  var obj = anchorListLocal[key];
  //console.log("AnchorList Text: " + obj['text']);
  // if text is long
  if(obj['text'].length > 16){
	  //console.log("long-old score: " + obj['score']);
	  obj['score'] = obj['score'] + 11;
	  obj['reasons'] = obj['reasons'] + " : longString";
	  //console.log("long-new score: " + obj['score']);
  }
    // if text is def too short
    if(obj['text'].length < 10){
	  //console.log("short-old score: " + obj['score']);
	  obj['score'] = obj['score'] - 10;
	  obj['reasons'] = obj['reasons'] + " : shortString";
	  //console.log("short-new score: " + obj['score']);
  }
    //if there are at least two spaces
    if(countSpaces(obj['text']) >= 2 ){
	  //console.log("spaces-old score: " + obj['score']);
	  obj['score'] = obj['score'] + 10;
	  obj['reasons'] = obj['reasons'] + " : 2pSpaces";
	  //console.log("spaces-new score: " + obj['score']);
    }
   }

  return anchorListLocal;
}
//grade links based on location in page
function ratePageLocation(anchorListLocal, windowWidthLocal){
   for (var key in anchorListLocal) {
	  //--------------- near center?-----------
	  var obj = anchorListLocal[key];
	  var thirdPoint = Math.round(windowWidthLocal / 3);
	  var halfPoint = Math.round(windowWidth / 2);
	  var pixelsAwayFromThird = Math.abs(obj['xPos'] - thirdPoint);
	  var scoreMultiplier = ((halfPoint - pixelsAwayFromThird)/(halfPoint));
	  
	  if(scoreMultiplier >= .5){
		obj['score'] = obj['score'] + (1.0*(scoreMultiplier * 20));
		obj['reasons'] = obj['reasons'] + " : over50Multi";
	  }
	  else{
		obj['score'] = obj['score'] + (scoreMultiplier * 20);
		obj['reasons'] = obj['reasons'] + " : under50Multi";
	  }
	  obj['score'] = Math.round(obj['score']);
	  //------------------- too high on page?-------------
	  
	  if(obj['yPos'] <= 250){
		obj['score'] = obj['score'] - 11;
		obj['reasons'] = obj['reasons'] + " : tooHigh";
	  }
	  
	  
	  //console.log("Score = " + obj['score'] + "xpos:" + obj['xPos'] + " thirdPoint:" + thirdPoint + " multiplier:"  + scoreMultiplier);

  }

   return anchorListLocal;
}
//bonus rank for having same first folder
function rateFirstPath(anchorListLocal){
    try{
	var headFirstPath = getFirstPath(headURL);
	}
	catch(err){
	console.log("couldn't get headFirstPath");
	}
   for (var key in anchorListLocal) {
	  	 var obj = anchorListLocal[key];
		  try{
			var curFirstPath = getFirstPath(obj['href'])
			}
		  catch(err1){
			curDomain = "";
		  }
		  //console.log(curFirstPath + " : " + headFirstPath)
		  if(curFirstPath == headFirstPath){
				obj['score'] = obj['score'] + 12;
				obj['reasons'] = obj['reasons'] + " : samePath";
			
		  }
  } 

   return anchorListLocal;
}
//rate all capital letters
function removeLowerRatedDuplicates(anchorListLocal){

   for (var key in anchorListLocal) {
	  	 var obj = anchorListLocal[key];
		 var usedURLArray = [];
		 var matchedURL = false;
		 for(int i = 0; i < matchedURL.length; i++){
			if(obj['href'] == usedURLArray[i]){
				matchedURL = true;
			}
		 }
		 if(matchedURL == true){
			
		 }
		 

   }

   return anchorListLocal;
}
//reduce link count based on domain
function removeLinksToOtherDomains(anchorListLocal){
	//console.log("just inside removeLinks");
	try{
	var headHost = parseURI(headURL);
	//console.log("headhost" + headHost);
	var headDomain = getDomain(headHost);
	}
	catch(err){
	console.log("couldn't get headDomain");
	headDomain = "";
	}
	   for (var key in anchorListLocal) {
		  var obj = anchorListLocal[key];
		  try{
			var curHost = parseURI(obj['href'])
			//console.log("curhost" + curHost);
			var curDomain = getDomain(curHost)
			}
		  catch(err1){
			curDomain = "";
		  }
		  if((obj['href'] !== null) && (obj['href'].substring(0,1) != '/')){
			  //console.log("substring1: " + obj['href'].substring(0,1));
			  //console.log(curDomain + "  :  " + headDomain)
			  if(curDomain != headDomain){
				//console.log("insideMatchedDomain");
				//console.log("about to delete inside removeLinks");
				delete anchorListLocal[key];
			  }
		  }
		
		
	 }
	//console.log("about to return inside removeLinks");
	return anchorListLocal;
}



function countSpaces(countMe){
	var numOfSpaces = countMe.split(/\s/).length - 1;
	return numOfSpaces;
}


function getDomain(url){
//console.log("just starting getDomain");
var TLDs = ["ac", "ad", "ae", "aero", "af", "ag", "ai", "al", "am", "an", "ao", "aq", "ar", "arpa", "as", "asia", "at", "au", "aw", "ax", "az", "ba", "bb", "bd", "be", "bf", "bg", "bh", "bi", "biz", "bj", "bm", "bn", "bo", "br", "bs", "bt", "bv", "bw", "by", "bz", "ca", "cat", "cc", "cd", "cf", "cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "com", "coop", "cr", "cu", "cv", "cx", "cy", "cz", "de", "dj", "dk", "dm", "do", "dz", "ec", "edu", "ee", "eg", "er", "es", "et", "eu", "fi", "fj", "fk", "fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gg", "gh", "gi", "gl", "gm", "gn", "gov", "gp", "gq", "gr", "gs", "gt", "gu", "gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu", "id", "ie", "il", "im", "in", "info", "int", "io", "iq", "ir", "is", "it", "je", "jm", "jo", "jobs", "jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr", "kw", "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", "ls", "lt", "lu", "lv", "ly", "ma", "mc", "md", "me", "mg", "mh", "mil", "mk", "ml", "mm", "mn", "mo", "mobi", "mp", "mq", "mr", "ms", "mt", "mu", "museum", "mv", "mw", "mx", "my", "mz", "na", "name", "nc", "ne", "net", "nf", "ng", "ni", "nl", "no", "np", "nr", "nu", "nz", "om", "org", "pa", "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", "pro", "ps", "pt", "pw", "py", "qa", "re", "ro", "rs", "ru", "rw", "sa", "sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "sk", "sl", "sm", "sn", "so", "sr", "st", "su", "sv", "sy", "sz", "tc", "td", "tel", "tf", "tg", "th", "tj", "tk", "tl", "tm", "tn", "to", "tp", "tr", "travel", "tt", "tv", "tw", "tz", "ua", "ug", "uk", "us", "uy", "uz", "va", "vc", "ve", "vg", "vi", "vn", "vu", "wf", "ws", "xn--0zwm56d", "xn--11b5bs3a9aj6g", "xn--3e0b707e", "xn--45brj9c", "xn--80akhbyknj4f", "xn--90a3ac", "xn--9t4b11yi5a", "xn--clchc0ea0b2g2a9gcd", "xn--deba0ad", "xn--fiqs8s", "xn--fiqz9s", "xn--fpcrj9c3d", "xn--fzc2c9e2c", "xn--g6w251d", "xn--gecrj9c", "xn--h2brj9c", "xn--hgbk6aj7f53bba", "xn--hlcj6aya9esc7a", "xn--j6w193g", "xn--jxalpdlp", "xn--kgbechtv", "xn--kprw13d", "xn--kpry57d", "xn--lgbbat1ad8j", "xn--mgbaam7a8h", "xn--mgbayh7gpa", "xn--mgbbh1a71e", "xn--mgbc0a9azcg", "xn--mgberp4a5d4ar", "xn--o3cw4h", "xn--ogbpf8fl", "xn--p1ai", "xn--pgbs0dh", "xn--s9brj9c", "xn--wgbh1c", "xn--wgbl6a", "xn--xkc2al3hye2a", "xn--xkc2dl3a5ee0h", "xn--yfro4i67o", "xn--ygbi2ammx", "xn--zckzah", "xxx", "ye", "yt", "za", "zm", "zw"].join();
	//console.log("about to do the split in getDomain");
    var parts = url.split('.');
	//console.log("about to do the if in getDomain");
    if (parts[0] === 'www' && parts[1] !== 'com'){
		//console.log("about to do the shift getDomain");
        parts.shift()
    }
    var ln = parts.length
      , i = ln
      , minLength = parts[parts.length-1].length
      , part

    // iterate backwards
	//console.log("about to do the while getDomain");
    while(part = parts[--i]){
		//console.log("inside the while getDomain");
        // stop when we find a non-TLD part
        if (i === 0                    // 'asia.com' (last remaining must be the SLD)
            || i < ln-2                // TLDs only span 2 levels
            || part.length < minLength // 'www.cn.com' (valid TLD as second-level domain)
            || TLDs.indexOf(part) < 0  // officialy not a TLD
        ){
			//console.log("about to return in getDomain");
            return part
        }
    }
}

function parseURI (url) {
//console.log("parseuri");
pathArray = url.split( '/' );
protocol = pathArray[0];
host = pathArray[2];
url = protocol + '://' + host;

	return url;
};
function getFirstPath(url){
	var pathArray = url.split( '/' );
	var retPath = '';
	if((url != null) && url.substring(0,1) == '/'){
		retPath = pathArray[1];
	//protocol = pathArray[0];
	//host = pathArray[2];
	//url = protocol + '://' + host;
	}
	else{
		retPath = pathArray[3]
	}
	return retPath;
}



