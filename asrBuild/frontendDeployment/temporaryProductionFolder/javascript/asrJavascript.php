var $jscomp={scope:{},findInternal:function(a,b,c){a instanceof String&&(a=String(a));for(var d=a.length,e=0;e<d;e++){var f=a[e];if(b.call(c,f,e,a))return{i:e,v:f}}return{i:-1,v:void 0}}};$jscomp.defineProperty="function"==typeof Object.defineProperties?Object.defineProperty:function(a,b,c){if(c.get||c.set)throw new TypeError("ES3 does not support getters and setters.");a!=Array.prototype&&a!=Object.prototype&&(a[b]=c.value)};
$jscomp.getGlobal=function(a){return"undefined"!=typeof window&&window===a?a:"undefined"!=typeof global&&null!=global?global:a};$jscomp.global=$jscomp.getGlobal(this);$jscomp.polyfill=function(a,b,c,d){if(b){c=$jscomp.global;a=a.split(".");for(d=0;d<a.length-1;d++){var e=a[d];e in c||(c[e]={});c=c[e]}a=a[a.length-1];d=c[a];b=b(d);b!=d&&null!=b&&$jscomp.defineProperty(c,a,{configurable:!0,writable:!0,value:b})}};
$jscomp.polyfill("Array.prototype.find",function(a){return a?a:function(a,c){return $jscomp.findInternal(this,a,c).v}},"es6-impl","es3");
var base={nodeFromID:function(a){return $("#"+a).get(0)},onReady:function(a){$(a)},show:function(a){$("#"+a).show()},hide:function(a){$("#"+a).hide()},isShown:function(a){return $("#"+a).is(":visible")},toggle:function(a){$("#"+a).toggle()},enable:function(a){$("#"+a).prop("disabled",!1)},disable:function(a){$("#"+a).prop("disabled",!0)},isEnabled:function(a){return!this.nodeFromID(a).disabled},check:function(a){$("#"+a).prop("checked",!0)},uncheck:function(a){$("#"+a).prop("checked",!1)},isChecked:function(a){return this.nodeFromID(a).checked},
focus:function(a){$("#"+a).focus()},select:function(a){$("#"+a).select()},focusAndSelect:function(a){this.focus(a);this.select(a)},clearForm:function(a){$("#"+a).find("input:text, input:password, input:file, select, textarea").val("");$("#"+a).find("input:radio, input:checkbox").removeAttr("checked").removeAttr("selected")},enableFormFields:function(a){$("#"+a+" :input").prop("disabled",!1)},disableFormFields:function(a){$("#"+a+" :input").prop("disabled",!0)},serializeForm:function(a){return $("#"+
a).serialize()},autoTab:function(a,b,c){this.nodeFromID(a).value.length>=b&&this.focusAndSelect(c)},onTabFocus:function(a,b,c){var d=a.which||0==a.which?a.which:a.keyCode,e=a.target?a.target:a.srcElement;if(b&&(a.altKey||9==d)&&!a.shiftKey)for(c=0;c<b.length;){curElementID=b[c];if(nodeFromID(curElementID)&&isShown(curElementID)&&isEnabled(curElementID)){focus(curElementID);if(document.activeElement==nodeFromID(curElementID))return!1;e.focus()}++c}else if(c&&(a.altKey||9==d)&&a.shiftKey)for(b=0;b<
c.length;){curElementID=c[b];if(nodeFromID(curElementID)&&isShown(curElementID)&&isEnabled(curElementID)){focus(curElementID);if(document.activeElement==nodeFromID(curElementID))return!1;e.focus()}++b}return!0},numberMask:function(a,b){var c=a.which||0==a.which?a.which:a.keyCode,d=a.target?a.target:a.srcElement;if(a.altKey||a.shiftKey&&33<=charKey&&126>=charKey)return!1;if(b&&46==c){if(-1<d.value.indexOf("."))return!1}else if(32<=c&&47>=c||58<=c&&126>=c||128<=c)return!1;return!0},dateMask:function(a){var b=
a.which||0==a.which?a.which:a.keyCode,c=a.target?a.target:a.srcElement,d=c.value?c.value:c.innerHTML.trim();if(a.altKey||a.shiftKey&&33<=charKey&&126>=charKey||10<=d.length&&8!=b&&127!=b&&0!=b&&!this.textIsSelected(c))return!1;if(47==b){if((a=d.match(/\//g))&&2<=a.length)return!1}else if(32<=b&&47>=b||58<=b&&126>=b||128<=b)return!1;return!0},dateFormat:function(a){var b=a.which||0==a.which?a.which:a.keyCode;a=a.target?a.target:a.srcElement;var c=a.value?a.value:a.innerHTML.trim();8!=b&&127!=b&&(c.match(/^\d{2}$/)&&
(a.value?a.value+="/":a.innerHTML+="/"),c.match(/^\d+\/\d{2}$/)&&(a.value?a.value+="/":a.innerHTML+="/"),b=c.split("/"),12<b[0]&&(a.value?a.value=a.value.replace(/^\d+/,"12"):a.innerHTML=a.innerHTML.replace(/^\d+/,"12")),1<b.length&&31<b[1]&&(a.value?a.value=a.value.replace(/\/\d+/,"/31"):a.innerHTML=a.innerHTML.replace(/\/\d+/,"/31")))},insertTodaysDate:function(a){(a.target?a.target:a.srcElement).value=base.getTodaysDate()},getTodaysDate:function(){var a=new Date,b=a.getMonth()+1,c=10>a.getDate()?
"0"+a.getDate():a.getDate(),b=10>b?"0"+b:b,a=a.getFullYear();return b+"/"+c+"/"+a},enableButtonOnTextInput:function(a,b){0<(a.target?a.target:a.srcElement).value.length?this.enable(b):this.disable(b)},textIsSelected:function(a){return document.selection&&"Text"==document.selection.type||null!=a.selectionStart&&null!=a.selectionEnd&&a.selectionStart!=a.selectionEnd?!0:!1},highlightNode:function(a,b,c,d){" undefined"===typeof b&&(b="A5DFE2");" undefined"===typeof c&&(c="FFFFFF");" undefined"===typeof d&&
(d=4E3);$("#"+a).css("background-color","#"+b).animate({backgroundColor:"#"+c},d)},highlightRow:function(a){this.highlightNode(a,"A5DFE2","FFFFFF",4E3)},getFilenameExtension:function(a){return 0<=a.lastIndexOf(".")?a.substr(a.lastIndexOf(".")+1).toLowerCase():""},asyncRequest:function(a,b,c,d,e,f){a={url:a,data:b,processData:e?!1:!0,success:c,type:f?"GET":"POST",dataType:"json"};0;e&&(a.contentType=!1);$.ajax(a).fail(function(a,b,c){d?d(b,c):(base.showMessage("There was difficulty communicating with the server.<br/><br/>Check your internet connection and try refreshing your browser.<br/><br/>If the problem persists, please contact us.",
"A Problem has Occurred"),console.log("An error occurred: "+b),console.log("Error Thrown: "+c))})},createAutocomplete:function(a,b,c,d,e,f,g){if(!a||!b)return!1;f||(f=3);g||(g=300);f={minLength:f,delay:g,source:function(a,d){$.post(b,c+"&query="+a.term,function(a){d(a)},"json").fail(function(a,b,c){base.showMessage("<strong>An error has occurred.</strong><br><br>"+("Error: "+c+"<br><br>")+("Response: "+a.responseText+"<br><br>"))})}};d&&(f.select=function(a,b){d(b.item)});e&&(f.change=function(a,
b){b.item||$(a.target).val("")});f.focus=function(b,c){if(c.item.value)return $("#"+a).val(c.item.value),!1};$.ui.autocomplete.prototype._renderMenu=function(a,b){var c=this,d="";$.each(b,function(b,e){e.category&&e.category!=d&&(a.append("<li class='ui-autocomplete-category'>"+e.category+"</li>"),d=e.category);c._renderItemData(a,e)})};$.ui.autocomplete.prototype._renderItem=function(a,b){return $("<li></li>").data("item.ui-autocomplete",b).append("<a>"+b.label+"</a>").appendTo(a)};$("#"+a).autocomplete(f)},
createDialog:function(a,b,c,d){if(!a)return!1;b={title:b,width:d,autoOpen:!1,modal:!0,minHeight:50};c||(b.closeOnEscape=!1,b.open=function(a,b){$(".ui-dialog-titlebar-close",$(this).parent()).hide()});$("#"+a).dialog(b);return displayControl={open:function(){$("#"+a).dialog("open")},close:function(){$("#"+a).dialog("close")}}},showMessage:function(a,b){0==$("#messageDialog").length&&($(document.body).append('\r\n\t\t\t\t<div id="messageDialog"> \r\n\t\t\t\t\t<div align="center"> \r\n\t\t\t\t\t\t<div id="messageDiv"></div> \r\n\t\t\t\t\t</div> \r\n\t\t\t\t</div>'),
this.messageDialog=this.createDialog("messageDialog",b,!0,600));this.nodeFromID("messageDiv").innerHTML=a;this.messageDialog.open()}},asr={tagImagesURL:"",powerPointBackgroundsURL:"",_domain:"",_getMenuURL:"getMenu.php",_getTagImagesURL:"getTagImages.php",_uploadTagImageURL:"uploadTagImage.php",_requestScreenshotsURL:"requestScreenshots.php",_uploadBackgroundImageURL:"uploadBackgroundImage.php",_storeTagTextURL:"storeTagText.php",_searchOrdersURL:"searchDFPOrders.php",_getOrderDataURL:"getOrderData.php",
_menuItems:[],_menuOptions:"",_imageLoadTimeout:3E3,_rowIndex:0,_queuedTags:[],_tagsBeingProcessed:0,_matchingOrders:[],_lineItems:[],_creatives:[],orders:{},checkCustomerCompletion:function(){asr.setGetScreenshotsButtonStatus()},getMenu:function(){base.disable("getMenuButton");base.disable("domain");var a=base.nodeFromID("domain").value.replace(/^https?\:\/\//i,""),b=a.split("/");0<b.length&&(a=b[0]);base.nodeFromID("domain").value=a;base.asyncRequest(asr._getMenuURL,"domain="+base.nodeFromID("domain").value,
function(a){if(a.success){asr._menuItems=a.data;a=0;asr._menuOptions="<option value='"+base.nodeFromID("domain").value+"/'>Main Page</option>";for(var b in asr._menuItems)asr._menuItems.hasOwnProperty(b)&&(asr._menuOptions+="<option value='"+asr._menuItems[b]+"'>"+b+"</option>",++a);asr._domain=base.nodeFromID("domain").value;base.hide("domainInputDiv");base.nodeFromID("campaignPagesHeader").innerHTML="Campaign Pages: <a href='http://"+asr._domain+"' target='_blank'>"+asr._domain+"</a>";base.show("domainNameDiv");
base.show("pagesTableDiv");2>=a&&(base.disable("addSiteSectionButton"),base.show("noMenuNotification"))}else alert(a.message);base.enable("getMenuButton");base.enable("domain")},function(a,b){asr.showErrorMessage("trying to get the site sections.");base.enable("getMenuButton");base.enable("domain")})},addMenuSectionRow:function(){var a=base.nodeFromID("pagesTable"),a=a.insertRow(a.rows.length);a.id="pageRow"+asr._rowIndex;rowCells="<td><select name='pages["+asr._rowIndex+"]'>"+asr._menuOptions+"</select></td>";
rowCells+="<td><label><input type='checkbox' name='findStory["+asr._rowIndex+"]' value='1'>Story</label></td>";rowCells+="<td><label><input type='checkbox' name='useMobile["+asr._rowIndex+"]' value='1'>Mobile</label></td>";rowCells+="<td><label><input type='radio' name='screenshotType["+asr._rowIndex+"]' value='all' checked>All Creative</label>";rowCells+="    <label><input type='radio' name='screenshotType["+asr._rowIndex+"]' value='individual'>Individual Creative Screenshots</label>";rowCells+=
"    <label><input type='radio' name='screenshotType["+asr._rowIndex+"]' value='none'>No Creative</label></td>";rowCells+="<td><input class='button-tiny' type='button' value='Remove' onClick='asr.deletePageRow("+asr._rowIndex+")'></td>";a.innerHTML=rowCells;asr._rowIndex+=1;asr.setGetScreenshotsButtonStatus()},addURLRow:function(){var a=base.nodeFromID("pagesTable"),a=a.insertRow(a.rows.length);a.id="pageRow"+asr._rowIndex;rowCells="<td colspan='2' class='pageURLTitle'>Page URL: ";rowCells+="<input type='text' name='pages["+
asr._rowIndex+"]'></td>";rowCells+="<td><label><input type='checkbox' name='useMobile["+asr._rowIndex+"]' value='1'>Mobile</label></td>";rowCells+="<td><label><input type='radio' name='screenshotType["+asr._rowIndex+"]' value='all' checked>All Creative</label>";rowCells+="    <label><input type='radio' name='screenshotType["+asr._rowIndex+"]' value='individual'>Individual Creative Screenshots</label>";rowCells+="    <label><input type='radio' name='screenshotType["+asr._rowIndex+"]' value='none'>No Creative</label></td>";
rowCells+="<td><input class='button-tiny' type='button' value='Remove' onClick='asr.deletePageRow("+asr._rowIndex+")'></td>";a.innerHTML=rowCells;asr._rowIndex+=1;asr.setGetScreenshotsButtonStatus()},deletePageRow:function(a){a=base.nodeFromID("pageRow"+a);a.parentNode.removeChild(a);asr.setGetScreenshotsButtonStatus()},getTagImages:function(){if(asr._queuedTags.length){for(var a={},b=0;b<asr._queuedTags.length;++b){var c=asr.getUUID();a[c]=asr._queuedTags[b];var d=$("<li class='ui-state-default' id='tagLI"+
c+"' />").html('<div class="queuedTagDiv"><img src="/images/waitingIcon.gif">Processing Image (May take a minute or two)</div>');$("#sortable").append(d);$("#sortable").sortable("refresh");++asr._tagsBeingProcessed;asr.loadTagImage(asr.tagImagesURL+c+".png","tagLI"+c)}base.disable("getTagImagesButton");base.asyncRequest(asr._getTagImagesURL,{tags:a},function(a){asr._queuedTags=[];base.nodeFromID("queuedTagCountSpan").innerHTML=0;console.log(a.data);base.nodeFromID("queuedTagDiv").className="yellowBackground"},
function(a,b){asr.showErrorMessage("trying to get tag images.")});asr.setGetScreenshotsButtonStatus()}},uploadTagImage:function(a){if(a){var b=asr.getUUID(),c=$("<li class='ui-state-default' id='tagLI"+b+"' />").html('<div class="queuedTagDiv">Queued...</div>');$("#sortable").append(c);$("#sortable").sortable("refresh");asr.loadTagImage(asr.tagImagesURL+b+".png","tagLI"+b);c=new FormData;c.append("imageID",b);c.append("image",a);base.asyncRequest(asr._uploadTagImageURL,c,function(a){console.log("Image uploaded")},
function(a,b){asr.showErrorMessage("trying to upload the tag image.")},!0);++asr._tagsBeingProcessed;asr.setGetScreenshotsButtonStatus()}},requestScreenshots:function(){for(var a=asr.getUUID(),b="",c=$("#sortable").sortable("toArray"),d=0;d<c.length;++d)var e=$("#"+c[d]+" img").attr("src").replace(/^https?\:\/\//i,""),b=b+("tagImages["+d+"]="+e+"&");base.asyncRequest(asr._requestScreenshotsURL,"jobID="+a+"&"+b+"&"+base.serializeForm("pagesForm"),function(b){b.success?window.location.href="/campaignResults.php?jobID="+
a:(console.log("in failure"),console.log(b.data))},function(a,b){asr.showErrorMessage("trying to request screenshots.")})},addTagsToQueue:function(a){asr._queuedTags=asr._queuedTags.concat(a);base.nodeFromID("queuedTagCountSpan").innerHTML=asr._queuedTags.length;0<asr._queuedTags.length&&base.enable("getTagImagesButton");asr.setGetScreenshotsButtonStatus();base.nodeFromID("queuedTagDiv").className="greenBackground"},storeTagText:function(a){base.asyncRequest(asr._storeTagTextURL,{tagText:a},function(a){console.log(a)},
function(a,c){console.log(a+": "+c)})},loadTagImage:function(a,b){var c=new Image;c.onload=function(){var d;d='<div class="tagImageRowDiv"><div class="tagImageInfoDiv">'+('<div class="tagDimensionsDiv">'+c.naturalWidth+"x"+c.naturalHeight+"</div>");d+='<div class="tagImageDiv"><img rowTag="" style="max-height: 120px;" src="'+a+'" /></div>';d=d+'</div><div class="deleteButtonDiv">'+("<input type='button' class='button-tiny' value='Remove' onClick='asr.deleteTagImageListItem(\""+b+"\")'>");d+="</div></div>";
$("#"+b).html(d);--asr._tagsBeingProcessed;asr.setGetScreenshotsButtonStatus()};c.onerror=function(){setTimeout(function(){asr.loadTagImage(a,b)},asr._imageLoadTimeout)};c.src=a},deleteTagImageListItem:function(a){a=base.nodeFromID(a);a.parentNode.removeChild(a)},filterOrders:function(){var a=base.nodeFromID("orderFilter").value,b="",c;for(c in asr.orders)if(asr.orders.hasOwnProperty(c)){var d=asr.orders[c].name;asr.orders[c].advertiserName&&(d+=" - "+asr.orders[c].advertiserName,asr.orders[c].agencyName&&
(d+=" ("+asr.orders[c].agencyName+")"));d+=" - "+c;if(""==a||-1!==d.toLowerCase().indexOf(a.toLowerCase()))b+="<option value='"+c+"'>"+d+"</option>"}base.nodeFromID("orderSelect").innerHTML=b;base.nodeFromID("orderNotesDiv").innerHTML=""},displayOrderNotes:function(){var a=base.nodeFromID("orderSelect").value;a&&(base.nodeFromID("orderNotesDiv").innerHTML=asr._matchingOrders[a].notes)},requestOrderData:function(){var a=base.nodeFromID("orderSelect").value;a&&(base.disable("getOrderDataButton"),base.asyncRequest(asr._getOrderDataURL,
"orderID="+a,function(b){base.enable("getOrderDataButton");base.disable("lineItemsButton");base.hide("tooManyCreativeDiv");if(b.success){asr._lineItems=b.data.lineItems;asr._creatives=b.data.creatives;b="";var c=0,d;for(d in asr._lineItems)if(asr._lineItems.hasOwnProperty(d)){var e=asr._lineItems[d],f="lineItemCheckBox_"+d;b+="<tr><td><input type='checkbox' id='"+f+"' value='"+d+"'></td>";b+="<td><label for='"+f+"'>"+e.name+"</label></td>";b+="<td><label for='"+f+"'>"+e.status+"</label></td>";b+=
"<td><label for='"+f+"'>"+e.creatives.length+"</label></td></tr>";++c}base.nodeFromID("lineItemsTableDiv").innerHTML='<table id="lineItemsTable" class="tablesorter"><thead><tr><th>&nbsp;</th><th>Name</th><th>Status</th><th>Creatives</th></tr></thead><tbody>'+b+"</tbody></table>";0<c&&$("#lineItemsTable").tablesorter({sortList:[[1,0]]});$("#lineItemsTable input:checkbox").click(asr.onLineItemSelection);lineItemsDialog.open();asr._matchingOrders[a].advertiserName&&(base.nodeFromID("customer").value=
asr._matchingOrders[a].advertiserName)}else console.log("in get order data failure"),console.log(b.data),base.enable("getOrderDataButton")},function(a,c){asr.showErrorMessage("trying to get the line items and creative.");console.log("in get order data failure");console.log(response.data);base.enable("getOrderDataButton")}))},searchOrders:function(){var a=base.nodeFromID("orderSearchTerm").value;3>a.length||(base.disable("orderSearchButton"),base.asyncRequest(asr._searchOrdersURL,"searchTerm="+a,function(a){base.enable("orderSearchButton");
if(a.success){asr._matchingOrders=a.data;a="";for(var b in asr._matchingOrders)if(asr._matchingOrders.hasOwnProperty(b)){var d=asr._matchingOrders[b].name;asr._matchingOrders[b].advertiserName&&(d+=" - "+asr._matchingOrders[b].advertiserName,asr._matchingOrders[b].agencyName&&(d+=" ("+asr._matchingOrders[b].agencyName+")"));d+=" - "+b;a+="<option value='"+b+"'>"+d+"</option>"}base.nodeFromID("orderSelect").innerHTML=a;base.nodeFromID("orderNotesDiv").innerHTML=""}else console.log("in search order data failure"),
console.log(a.data),base.enable("orderSearchButton")},function(a,c){asr.showErrorMessage("trying to search orders.");console.log("in search order data failure");console.log(response.data);base.enable("orderSearchButton")}))},useSelectedLineItems:function(){var a=[];$("#lineItemsTable input:checked").each(function(b){a.push($(this).val())});if(0!=a.length){var b=[];base.nodeFromID("lineItemsDiv").innerHTML="";for(var c in a){var d=asr._lineItems[a[c]];""!=base.nodeFromID("lineItemsDiv").innerHTML&&
(base.nodeFromID("lineItemsDiv").innerHTML+="<br><br>");base.nodeFromID("lineItemsDiv").innerHTML+="<strong>"+d.name+" ("+d.status+") </strong>";d.notes&&(base.nodeFromID("lineItemsDiv").innerHTML+=" - "+d.notes);for(var e in d.creatives){var f=d.creatives[e];0>b.indexOf(f)?b.push(f):console.log("Found: "+f)}}if(15>=b.length){for(var g in b)asr.addTagsToQueue(tagParser.getTags(asr._creatives[b[g]].tag));asr.getTagImages()}base.hide("dfpOrdersHeader");base.hide("dfpOrdersHelpIcon");base.hide("dfpOrdersDiv");
base.show("lineItemsHeader");base.show("lineItemsDiv");lineItemsDialog.close()}},selectAllLineItems:function(){$("#lineItemsTable input:checkbox").prop("checked",!0);asr.onLineItemSelection()},unselectAllLineItems:function(){$("#lineItemsTable input:checkbox").prop("checked",!1);asr.onLineItemSelection()},onLineItemSelection:function(){var a=[];$("#lineItemsTable input:checked").each(function(b){a.push($(this).val())});if(0==a.length)base.disable("lineItemsButton"),base.hide("tooManyCreativeDiv");
else{base.enable("lineItemsButton");var b=[],c;for(c in a){var d=asr._lineItems[a[c]],e;for(e in d.creatives){var f=d.creatives[e];0>b.indexOf(f)&&b.push(f)}}15<=b.length?base.show("tooManyCreativeDiv"):base.hide("tooManyCreativeDiv")}},uploadPowerPointBackground:function(){if(0==base.nodeFromID("newBackgroundTitle").value.length)alert("Enter a name for the PowerPoint");else if(0==base.nodeFromID("newBackgroundImage").value.length)alert("Choose an image for the background");else{base.disable("uploadBackgroundButton");
var a=new FormData;a.append("backgroundTitle",base.nodeFromID("newBackgroundTitle").value);a.append("backgroundFontColor",base.nodeFromID("newBackgroundFontColor").value.substring(1));a.append("backgroundImage",base.nodeFromID("newBackgroundImage").files[0]);base.asyncRequest(asr._uploadBackgroundImageURL,a,function(a){a.success?(a=a.data,base.nodeFromID("backgroundTitleDiv").innerHTML=a.title,base.nodeFromID("fontColorDiv").style.backgroundColor="#"+a.fontColor,base.nodeFromID("backgroundThumbnailImage").src=
asr.powerPointBackgroundsURL+"thumbnails/"+a.thumbnailFilename,base.nodeFromID("backgroundTitle").value=a.title,base.nodeFromID("backgroundFontColor").value=a.fontColor,base.nodeFromID("backgroundFilename").value=a.filename):alert(a.message);base.enable("uploadBackgroundButton");base.hide("uploadBackgroundDiv");base.show("changeBackgroundButtonDiv")},function(a,c){asr.showErrorMessage("trying to upload the PowerPoint background image.");base.enable("uploadBackgroundButton")},!0)}},setGetScreenshotsButtonStatus:function(){""!=
base.nodeFromID("customer").value&&0<base.nodeFromID("pagesTable").rows.length&&0==asr._tagsBeingProcessed&&0==asr._queuedTags.length?base.enable("getScreenshotsButton"):base.disable("getScreenshotsButton")},showErrorMessage:function(a){var b="";a&&(b=" while "+a);a="There was difficulty communicating with the server"+b+".<br/><br/>Check your internet connection and try refreshing your browser.<br/><br/>If the problem persists, please <a onclick='contactForm.reset(); contactFormDialog.open()'>Contact Us</a> us.";
base.showMessage(a,"A Problem has Occurred")},enableSubmitButtons:function(){base.enable("getMenuButton");base.enable("domain");base.enable("getTagImagesButton");base.enable("getOrderDataButton");base.enable("uploadBackgroundButton");base.disable("getScreenshotsButton")},getUUID:function(){var a=(new Date).getTime();window.performance&&"function"===typeof window.performance.now&&(a+=performance.now());return"xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g,function(b){var c=(a+16*Math.random())%
16|0;a=Math.floor(a/16);return("x"==b?c:c&3|8).toString(16)})}},tagParser={getTags:function(a){for(var b=/<(\w*)\b[^>]*>[\s\S]*?<\/\1>/gmi,c=[],d;null!==(d=b.exec(a));){var e=d[0];d=d[1].toLowerCase();var f=!1;"script"==d&&(f=tagParser.scriptHasSource(e));c.push({html:e,type:d,isSource:f})}for(b=/<img[^>]+src="?[^"\s]+"?\s*\/>/g;null!==(currentIMGTag=b.exec(a));)c.push({html:currentIMGTag[0],type:"img",isSource:!1});if(0==c.length)return[];var g,h,k,l,m;for(a=0;a<c.length;++a)switch(c[a].type){case "script":g=
!0;break;case "iframe":h=!0;break;case "noscript":k=!0;break;case "a":l=!0;break;case "img":m=!0}a=[];b="";for(e=0;e<c.length;++e)g?"script"==c[e].type&&(c[e].isSource?(b+=c[e].html,a.push(b),b=""):b+=c[e].html):h?"iframe"==c[e].type&&a.push(c[e].html):k?"noscript"==c[e].type&&a.push(c[e].html):l?"a"==c[e].type&&a.push(c[e].html):m&&"img"==c[e].type&&a.push(c[e].html);return a},scriptHasSource:function(a){var b=!1;-1<a.toLowerCase().indexOf("src=")&&(b=!0);-1<a.toLowerCase().indexOf("document.write")&&
(b=!0);return b},handleTagTextFileDrop:function(a){a.stopPropagation();a.preventDefault();base.nodeFromID(this.id).classList.remove("dropBoxHasDragOver");a=a.dataTransfer.files;if(0!=a.length)for(var b=0,c;c=a[b];b++){var d=new FileReader;c.type.match("text.*")?(d.onload=function(a){return function(a){asr.addTagsToQueue(tagParser.getTags(a.target.result));asr.storeTagText(a.target.result)}}(c),d.readAsText(c)):c.type.match("image.*")&&asr.uploadTagImage(c)}},handleTagZipFileDrop:function(a){a.stopPropagation();
a.preventDefault();base.nodeFromID(this.id).classList.remove("dropBoxHasDragOver");a=a.dataTransfer.files;0!=a.length&&(a=a[0],-1>=a.type.toLowerCase().indexOf("zip")||zip.createReader(new zip.BlobReader(a),function(a){a.getEntries(function(b){if(b.length)for(var c=0,e;e=b[c];c++)console.log(e),e.getData(new zip.TextWriter,function(b){asr.addTagsToQueue(tagParser.getTags(b));asr.storeTagText(b);a.close()})})}))},handleTagTextboxInput:function(){var a=base.nodeFromID("tagTextTextbox"),b=a.value;""!=
b&&(asr.addTagsToQueue(tagParser.getTags(b)),asr.storeTagText(b),a.value="")},handleDragOver:function(a){base.nodeFromID(this.id).classList.add("dropBoxHasDragOver");a.stopPropagation();a.preventDefault()},handleDragLeave:function(a){base.nodeFromID(this.id).classList.remove("dropBoxHasDragOver");a.stopPropagation();a.preventDefault()}},campaign={jobID:"",_campaignJobURL:"getCampaignJob.php",_QUEUETIMEOUT:5E3,getResults:function(){base.asyncRequest(campaign._campaignJobURL,"jobID="+campaign.jobID,
function(a){if(a.queued)setTimeout(campaign.getResults,campaign._QUEUETIMEOUT);else if(a.success){base.nodeFromID("customerSpan").innerHTML=a.customer;base.nodeFromID("domainSpan").innerHTML=a.domain;base.nodeFromID("dateSpan").innerHTML=a.date;base.nodeFromID("runtimeSpan").innerHTML=a.runtime;base.nodeFromID("powerPointLink").href=a.powerPointURL;var b="",c=0,d;for(d in a.screenshots)a.screenshots.hasOwnProperty(d)&&(b+="<tr><td><a href='"+a.screenshots[d]+"' target='_blank'>"+a.screenshots[d]+
"</a><br><br>",b+='<img style="max-width: 600px;" src="'+d+'" /></td></tr>',++c);base.nodeFromID("screenshotsTable").innerHTML=b;base.nodeFromID("screenshotCountSpan").innerHTML=c;base.hide("campaignSubmittedDiv");base.show("campaignResultsDiv")}else alert("Could not retrieve job data"),console.log("error: "+a)},function(){setTimeout(campaign.getResults,campaign._QUEUETIMEOUT)})}};
$(function(){$(document).tooltip({items:"[helpIcon], [rowTag]",content:function(){var a=$(this),b=a.attr("id");if(a.is("[rowTag]"))return"<img src='"+a.attr("src")+"'/>";if("dfpOrdersHelpIcon"==b)return"These are your active DFP orders.<br><br> \t\t\t\t\t\tUse the filter bar above the orders to narrow your results.";if("customerHelpIcon"==b)return"The customer name will appear in the final PowerPoint and in the campaign results email.";if("powerPointBackgroundHelpIcon"==b)return"Choose the background image and font color to be used in the finished PowerPoint.<br><br>\t\t\t\t\t\t<strong>Recommended image sizes: </strong>1280x720, 1920x1080<br><br>\t\t\t\t\t\tThe chosen font color should be easy to read against the background image.";
if("domainInputHelpIcon"==b)return"Enter the domain URL of the publisher.<br><br>(Ex: nytimes.com, chicagotribune.com)";if("addPagesHelpIcon"==b)return"Choose the pages for screenshots.<br><br> \t\t\t\t\t\t<strong>Add Site Section:</strong> Let's you choose a section from the site such as weather or news.<br><br>\t\t\t\t\t\t<strong>Add URL:</strong> Paste in the URL of an exact page.<br><br>\t\t\t\t\t\t<strong>Story:</strong> Automatically chooses a top story from the section.<br><br>\t\t\t\t\t\t<strong>Mobile:</strong> Returns a mobile screenshot.<br><br>\t\t\t\t\t\t<strong>Creative Type:</strong><br><br>\t\t\t\t\t\t<i>All Creative:</i> All possible creative is used on the page.<br><br>\t\t\t\t\t\t<i>Individual Creative:</i> A screenshot is taken for each individual creative.<br><br>\t\t\t\t\t\t<i>No Creative:</i> No creative is used on the page for the screenshot.";
if("creativeHelpIcon"==b)return"Add creative for the screenshots.<br><br>\t\t\t\t\t\tAfter text tags have been added, click <i>Get Tag Images</i> to turn them into images.<br><br>\t\t\t\t\t\t<strong>Copy and Paste:</strong> Copy tag text from an email or file and paste into the text box. \t\t\t\t\t\tClick <i>Add Tags</i> when finished.<br><br>\t\t\t\t\t\t<strong>Drop Text or Image File(s):</strong> Drag and drop text files or image files to quickly add them.<br><br>\t\t\t\t\t\t<strong>Drop a Zip File:</strong> Drag and drop a zip file of tag texts to quickly add them."}})});
var contactForm={_submitFormURL:"submitContactForm.php",selectIdea:function(){base.check("contactIdeaRadio");base.hide("contactProblemRow")},selectIssue:function(){base.check("contactIssueRadio");base.show("contactProblemRow")},submitForm:function(){""==base.nodeFromID("contactName").value.trim()?alert("Please enter your name."):""==base.nodeFromID("contactEmail").value.trim()?alert("Please enter your email."):base.isChecked("contactIssueRadio")&&"NONECHOSEN"==base.nodeFromID("contactProblem").value?
alert("Please select an issue from the Problem menu."):""==base.nodeFromID("contactDescription").value.trim()?alert("Please enter a description. Be thorough."):base.asyncRequest(contactForm._submitFormURL,base.serializeForm("asrContactForm"),function(a){a.success?(base.hide("asrContactForm"),base.show("contactThankYouDiv")):(alert(a.message),base.hide("asrContactForm"),base.show("contactFailureDiv"))})},reset:function(){base.hide("contactThankYouDiv");base.hide("contactFailureDiv");base.show("asrContactForm");
contactForm.selectIdea();base.nodeFromID("contactProblem").value="NONECHOSEN";base.nodeFromID("contactDescription").value=""}};
$(function(){$("#orderSearchTerm").keyup(function(a){13==a.which&&asr.searchOrders()});$("#orderSearchButton").click(asr.searchOrders);$("#orderSelect").change(asr.displayOrderNotes);$("#getOrderDataButton").click(asr.requestOrderData);$("#selectAllLineItemsButton").click(asr.selectAllLineItems);$("#unselectAllLineItemsButton").click(asr.unselectAllLineItems);$("#lineItemsButton").click(asr.useSelectedLineItems);$("#customer").on("input",asr.checkCustomerCompletion);$("#changeBackgroundButton").click(function(){base.hide("changeBackgroundButtonDiv");
base.show("uploadBackgroundDiv")});$("#uploadBackgroundButton").click(asr.uploadPowerPointBackground);$("#domain").keyup(function(a){13==a.which&&asr.getMenu()});$("#getMenuButton").click(asr.getMenu);$("#addSiteSectionButton").click(asr.addMenuSectionRow);$("#addURLButton").click(asr.addURLRow);var a=base.nodeFromID("textFileDropZone");a&&(a.addEventListener("dragover",tagParser.handleDragOver,!1),a.addEventListener("drop",tagParser.handleTagTextFileDrop,!1),a.addEventListener("dragleave",tagParser.handleDragLeave,
!1));if(a=base.nodeFromID("zipFileDropZone"))a.addEventListener("dragover",tagParser.handleDragOver,!1),a.addEventListener("drop",tagParser.handleTagZipFileDrop,!1),a.addEventListener("dragleave",tagParser.handleDragLeave,!1);$("#tagTextTextboxButton").click(tagParser.handleTagTextboxInput);$("#getTagImagesButton").click(asr.getTagImages);$("#getScreenshotsButton").click(asr.requestScreenshots);$(".contactIssueLink").click(function(){contactForm.reset();contactForm.selectIssue();contactFormDialog.open()});
$(".contactIdeaLink").click(function(){contactForm.reset();contactFormDialog.open()});$("#contactIdeaRadio").click(contactForm.selectIdea);$("#contactIssueRadio").click(contactForm.selectIssue);$("#contactSendButton").click(contactForm.submitForm)});
