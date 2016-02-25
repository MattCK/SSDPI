/**
* Contains general javascript functions used throughout the AdShotRunner system.
*
* @package AdShotRunner
* @subpackage JavaScript
*/

//Base object for all system functions and variables
var g = {

	/**
	* Returns the node in the DOM with the passed ID. Returns NULL on failure.
	*
	* @param {String} nodeID  ID of the document element to retrieve
	* @return {HTMLElement}  Element in the document with the passed ID. NULL on failure.
	*/
	nodeFromID: function(nodeID) {
		return $('#' + nodeID).get(0);
	},
	
	/**
	* Executes the passed function when the DOM is fully ready/loaded.
	*
	* @param {function} launchFunction  Function to execute when the DOM is ready.
	* @return {HTMLElement}  Element in the document with the passed ID. NULL on failure.
	*/
	onReady: function(launchFunction) {
		$(launchFunction);
	},

	/**
	* Shows the node with the passed ID 
	*
	* @param {String} nodeID  ID of the node to show
	*/
	show: function(nodeID) {
		$('#' + nodeID).show();		
	},

	/**
	* Hides the node with the passed ID 
	*
	* @param {String} nodeID  ID of the document node to show
	*/
	hide: function(nodeID) {
		$('#' + nodeID).hide();		
	},

	/**
	* Returns TRUE if the display style of the HTMLElement in the DOM with the passed ID is set to '' (shown) and FALSE otherwise. 
	*
	* @param {String} nodeID  ID of the document element to retrieve
	* @return {HTMLElement}  TRUE if the display is '' (shown) and FALSE otherwise (hidden).
	*/
	isShown: function(nodeID) {
		return $('#' + nodeID).is(":visible");
	},

	/**
	* If the node with the passed ID is shown, it is hidden. If it is hidden, it is then shown. 
	*
	* @param {String} nodeID  ID of the node with the display to toggle
	*/
	toggle: function(nodeID){
		$('#' + nodeID).toggle();		
	},

	/**
	* Enables the node with the passed ID. 
	*
	* @param {String} nodeID  ID of the node to enable
	*/
	enable: function(nodeID) {
		$('#' + nodeID).prop("disabled", false);	
	},

	/**
	* Disables the node with the passed ID. 
	*
	* @param {String} nodeID  ID of the node to disable
	*/
	disable: function(nodeID) {
		$('#' + nodeID).prop("disabled", true);	
	},

	/**
	* Returns TRUE if the disabled property of the HTMLElement in the DOM with the passed ID is set to FALSE and returns TRUE otherwise. 
	*
	* @param {String} nodeID  ID of the document element to retrieve
	* @return {HTMLElement}  TRUE if the disabled property is FALSE and FALSE otherwise.
	*/
	isEnabled: function(nodeID) {
		return (!this.nodeFromID(nodeID).disabled);
	},

	/**
	* Checks the checkbox node with the passed ID. 
	*
	* @param {String} nodeID  ID of the checkbox node to check
	*/
	check: function(nodeID) {
		$('#' + nodeID).prop("checked", true);	
	},

	/**
	* Unchecks the checkbox node with the passed ID. 
	*
	* @param {String} nodeID  ID of the checkbox node to uncheck
	*/
	uncheck: function(nodeID) {
		$('#' + nodeID).prop("checked", false);	
	},

	/**
	* Returns TRUE if the checked property of the HTMLElement in the DOM with the passed ID is set to TRUE and returns FALSE otherwise. 
	*
	* @param {String} nodeID  ID of the document element to retrieve
	* @return {HTMLElement}  TRUE if the checked property is TRUE and FALSE otherwise.
	*/
	isChecked: function(nodeID){
		return this.nodeFromID(nodeID).checked;
	},

	/**
	* Puts the focus on the input node with the passed ID.  
	*
	* @param {String} nodeID  ID of the input node to focus upon
	*/
	focus: function(nodeID) {
		$('#' + nodeID).focus();
	},

	/**
	* Selects the node in the DOM with the passed ID.  
	*
	* @param {String} nodeID  ID of the document element to select
	*/
	select: function(nodeID) {
		$('#' + nodeID).select();
	},

	/**
	* Focuses upon and selects the HTMLInput element in the DOM with the passed ID.  
	*
	* Note: The reason we have to do both instead of just selecting is because IE is dumb.
	* NOTE: IE NOT USED. REPLACE IN CODE.
	*
	* @param {String} nodeID  ID of the document element to focus and select
	*/
	focusAndSelect: function(nodeID) {
		this.focus(nodeID);
		this.select(nodeID);
	},

	/**
	* Clears the passed form and unchecks all buttons. Hidden, radio, and checkbox values are preserved. 
	*
	* @param {String} formID  ID of the form node to clear
	*/
	clearForm: function(formID) {
		$('#' + formID).find('input:text, input:password, input:file, select, textarea').val('');
		$('#' + formID).find('input:radio, input:checkbox').removeAttr('checked').removeAttr('selected');
	},

	/**
	* Enables all the fields in a form. 
	*
	* @param {String} formID  ID of the form element to enable fields
	*/
	enableFormFields: function(formID) {
		$('#' + formID + ' :input').prop("disabled", false);
	},

	/**
	* Disables all the fields in a form. 
	*
	* @param {String} formID  ID of the form element to disable fields
	*/
	disableFormFields: function(formID) {
		$('#' + formID + ' :input').prop("disabled", true);
	},
	
	/**
	* Turns the form into a proper header string and returns it. 
	*
	* @param {String} formID  ID of the form element to serialize
	*/
	serializeForm: function(formID) {
		return $('#' + formID).serialize();
	},

	/**
	* Moves focus to the next element when the current element reaches a certain length. 
	*
	* @param {String} curElementID  ID of the current form element
	* @param {Number} maxLength  Maximum length the current element should reach
	* @param {String} nextElementID  ID of the next form element to focus upon
	*/
	autoTab: function(curElementID, maxLength, nextElementID) {
		if (this.nodeFromID(curElementID).value.length >= maxLength) {this.focusAndSelect(nextElementID);}
	},

	/**
	* FIX THIS DESCRIPTION
	* When tab is pressed, the function attempts to move focus to the first element ID passed.
	* If unable to do to the element not being displayed or disabled, it attempts to focus
	* on the second element ID if one was passed. 
	*
	* @param {Event} evt  							Standard javascript event object from the call
	* @param {Array} tabElementPossibilities  		IDs of first element to attempt to focus upon
	* @param {Array} tabShiftElementPossibilities  	IDs of second element to attempt to focus upon
	*/
	onTabFocus: function(evt, tabElementPossibilities, tabShiftElementPossibilities) {
		
		//Get the charCode and target passed
		var charCode = (evt.which || (evt.which == 0))  ? evt.which : evt.keyCode;
		var targetElement = (evt.target) ? evt.target : evt.srcElement;

		//If alt is pressed but not shift, try to focus on the possibilities
		if ((tabElementPossibilities) && ((evt.altKey) || (charCode == 9)) && (!evt.shiftKey)) {
			var curIndex = 0;
			while (curIndex < tabElementPossibilities.length) {
				curElementID = tabElementPossibilities[curIndex];
				if ((nodeFromID(curElementID)) && (isShown(curElementID)) && (isEnabled(curElementID))) {
					focus(curElementID);
					//If the element is now the focus, return false. Otherwise, unable to process so set the focus back to the target.
					if (document.activeElement == nodeFromID(curElementID)) {return false;}
					else {targetElement.focus();}
				}
				++curIndex;
			}
		}

		//If alt is pressed with shift, try to focus on those possibilities
		else if ((tabShiftElementPossibilities) && ((evt.altKey) || (charCode == 9)) && (evt.shiftKey)) {
			var curIndex = 0;
			while (curIndex < tabShiftElementPossibilities.length) {
				curElementID = tabShiftElementPossibilities[curIndex];
				if ((nodeFromID(curElementID)) && (isShown(curElementID)) && (isEnabled(curElementID))) {
					focus(curElementID);
					//If the element is now the focus, return false. Otherwise, unable to process so set the focus back to the target.
					if (document.activeElement == nodeFromID(curElementID)) {return false;}
					else {targetElement.focus();}
				}
				++curIndex;
			}
		}

		//Otherwise, just return true so the field can keep doing its thing
		return true;
	},

	numberMask: function(evt, allowDecimal) {
			
		//Get the char code and target from the keypress event
		var charCode = (evt.which || (evt.which == 0))  ? evt.which : evt.keyCode;
		var targetElement = (evt.target) ? evt.target : evt.srcElement;
		
		//If a alt is pressed ignore everything
		if (evt.altKey) {return false;}
		
		//If shift is pressed with a character key, ignore it
		if ((evt.shiftKey) && (charKey >= 33 && charKey <= 126)) {return false;}
			
		//If decimals are allowed and the key pressed is a '.', verify it is the first and only. If not return false
		if ((allowDecimal) && (charCode == 46)) {
			if (targetElement.value.indexOf('.') > -1) {return false;}
		}
		
		//Otherwise, if the key pressed is not a number, return false
		else if ((charCode >= 32 && charCode <= 47) || (charCode >= 58 && charCode <= 126) || (charCode >= 128)) {
			return false;
		}
		
		//Otherwise return true
		return true;
	},

	dateMask: function(evt) {

		//Get the char code and target from the keypress event
		var charCode = (evt.which || (evt.which == 0))  ? evt.which : evt.keyCode;
		var targetElement = (evt.target) ? evt.target : evt.srcElement;
		var targetValue = (!targetElement.value) ? targetElement.innerHTML.trim() : targetElement.value;

		//If a alt is pressed ignore everything
		if (evt.altKey) {return false;}
		
		//If shift is pressed with a character key, ignore it
		if ((evt.shiftKey) && (charKey >= 33 && charKey <= 126)) {return false;}
		
		//If there are already 10 chars and the key pressed isn't backspace or delete or an arrow and nothing is selected, return false
		if ((targetValue.length >= 10) && (charCode != 8) && (charCode != 127) && (charCode != 0) && (!this.textIsSelected(targetElement))) {
			return false;
		}
		
		//If the key pressed is a '/', verify it is the first or second. If not return false
		if (charCode == 47) {
			var slashMatches = targetValue.match(/\//g);
			if (slashMatches && (slashMatches.length >= 2)) {return false;}
		}
		
		//Otherwise, if the key pressed is not a number or movement key, return false
		else if ((charCode >= 32 && charCode <= 47) || (charCode >= 58 && charCode <= 126) || (charCode >= 128)) {
			return false;
		}
		
		//Otherwise return true
		return true;
	},

	dateFormat: function(evt) {

		//Get the char code and target from the keypress event
		var charCode = (evt.which || (evt.which == 0))  ? evt.which : evt.keyCode;
		var targetElement = (evt.target) ? evt.target : evt.srcElement;
		var targetValue = (!targetElement.value) ? targetElement.innerHTML.trim() : targetElement.value;

		//If delete or backspace was pressed, do nothing for now.
		if ((charCode == 8) || (charCode == 127)) {return;}

		//If there are two digits as the value, add a slash
		if (targetValue.match(/^\d{2}$/)) {
			if (targetElement.value) {targetElement.value += '/';}
			else {targetElement.innerHTML += '/';}
		}

		//If there are two digits after a first slash, add a slash
		if (targetValue.match(/^\d+\/\d{2}$/)) {
			if (targetElement.value) {targetElement.value += '/';}
			else {targetElement.innerHTML += '/';}
		}
		
		//Split the date into its parts
		var dateParts = targetValue.split('/');
		
		//If the first part is greater than 12, replace it with 12
		if (dateParts[0] > 12) {
			if (targetElement.value) {targetElement.value = targetElement.value.replace(/^\d+/, '12');}
			else {targetElement.innerHTML = targetElement.innerHTML.replace(/^\d+/, '12');}
		}

		//If the second part exists and is greater than 31, replace it with 31
		if ((dateParts.length > 1) && (dateParts[1] > 31)) {
			if (targetElement.value) {targetElement.value = targetElement.value.replace(/\/\d+/, '/31');}
			else {targetElement.innerHTML = targetElement.innerHTML.replace(/\/\d+/, '/31');}
		}
	},

	insertTodaysDate: function(evt) {

		//Get the target from the keypress event
		var targetElement = (evt.target) ? evt.target : evt.srcElement;
		
		//Insert today's date into the target
		targetElement.value = g.getTodaysDate();	
	},

	getTodaysDate: function() {

		//Format today's date
		var todaysDate = new Date();
		var incrementedMonth = todaysDate.getMonth() + 1;
		var currentDay = (todaysDate.getDate() < 10) ? "0" + todaysDate.getDate() : todaysDate.getDate();
		var currentMonth = (incrementedMonth < 10) ? "0" + incrementedMonth : incrementedMonth;
		var currentYear = todaysDate.getFullYear();
		
		//Add slashes and return it
		return currentMonth + '/' + currentDay + '/' + currentYear;	
	},

	enableButtonOnTextInput: function(evt, buttonID) {
		
		//Get the target from the keypress event
		var targetElement = (evt.target) ? evt.target : evt.srcElement;
		
		//If there is at least one character, enable the button. Otherwise, disable it.
		if (targetElement.value.length > 0) {this.enable(buttonID);}
		else {this.disable(buttonID)};
	},

	textIsSelected: function(textElement) {
		//If text is selected, return true
		if ((document.selection) && (document.selection.type == "Text")) {return true;}
		else if ((textElement.selectionStart != null) && (textElement.selectionEnd != null) && (textElement.selectionStart != textElement.selectionEnd)) {return true;}  
		
		//Otherwise, return false
		return false;
	},

	/**
	* Animates the background color of the node with the passed ID from the beginning color to the final color
	* over the passed duration.
	*
	* The default animates from teal to white over 4 seconds.
	*
	* @param {String} nodeID  		ID of the node to highlight
	* @param {String} startColor  	Hex code of starting color (DEFAULT: A5DFE2)
	* @param {String} endColor  	Hex code of ending color (DEFAULT: FFFFFF)
	* @param {int} 	  duration  	Duration to animate in miliseconds (DEFAULT: 4000)
	*/
	highlightNode: function(nodeID, startColor, endColor, duration) {

		//Set the starting colors and duration if not passed
		if (typeof(startColor) ===' undefined') startColor = 'A5DFE2';
		if (typeof(endColor) ===' undefined') endColor = 'FFFFFF';
		if (typeof(duration) ===' undefined') duration = 4000;

		//Execute the highlight animation
		$('#' + nodeID).css('background-color', '#' + startColor).animate({
						backgroundColor: '#' + endColor,
					}, duration);
	},

	/**
	* Animates the background color of the table row with the passed ID teal to white over 4 seconds.
	*
	* @param {String} rowID  		ID of the table row to highlight
	*/
	highlightRow: function(rowID) {
		//Re-passes defaults in case base function defaults change
		this.highlightNode(rowID, 'A5DFE2', 'FFFFFF', 4000);
	},

	/**
	* Returns the extension of the filename.
	*
	* The extension is simply the text after the last '.' . This function returns it as lowercase.
	*
	* @param {String} filename  		Filename.
	* @return {String}  			Filename extension as lowercase
	*/
	getFilenameExtension: function(filename) {
		if (filename.lastIndexOf('.') >= 0) {return filename.substr(filename.lastIndexOf('.') + 1).toLowerCase();}
		else {return '';}
	},

	/**
	* Sends a request to the server and executes the callback function on the returned data. 
	* The basic usage uses POST unless otherwise flagged. The function expects a JSON response.
	* The callback takes a single argument, 'data', the returned data converted to a JSON object.
	* 
	*
	* @param {String} url  					URL to send the request to
	* @param {String} headers  				Post headers to send. This can be a string or an associated array with keys as names and values as data.
	* @param {Function} callbackFunction  	Function to call on request return. Takes a single argument for the data returned from the server.
	* @param {Boolean} useUnprocessedData  	Flags whether or not to process the data DEFAULT: false
	* @param {Boolean} useGET  				On true, the request is sent via GET. Otherwise, it is sent via POST (including when omitted).
	*/
	asyncRequest: function(url, headers, callbackFunction, useUnprocessedData, useGET) {
		
		//Set the GET/POST communication type based on the passed argument
		var communicationType = (useGET) ? 'GET' : 'POST';
					
		//Flag whether or not to use raw data. This is opposite what's expected.
		var processFlag = (useUnprocessedData) ? false : true;
		var contentTypeFlag = (useUnprocessedData) ? false : null;
		
		//Set the options
		var ajaxOptions = {
			url: url,
			data: headers,
			processData: processFlag,
			success: callbackFunction,
			type: communicationType,
			dataType: 'json',
		};
		
		//If unprocessed data needs to be sent, remove content type
		if (useUnprocessedData) {ajaxOptions.contentType = false;}
		
		//Setup and make the AJAX call
		$.ajax(ajaxOptions)
		 .fail(function( jqXHR, textStatus, errorThrown ) {
			var errorHTML = '<strong>An error has occurred.</strong><br><br>';
			errorHTML += 'Error: ' + errorThrown + '<br><br>';
			errorHTML += 'Response: ' + jqXHR.responseText + '<br><br>';
			g.showMessage(errorHTML);
		});	
	},

	/**
	* Adds autosuggest and autocomplete functionality to the passed text input node. The request is sent via POST method. The current
	* query is sent under the header variable as 'query'. Additional headers can be appended to the sent request.
	*
	* If a 'category' member is attached to the autosuggested item, categories will be displayed.
	*
	* @param {String} inputNodeID			Node ID of text input field to attach autosuggest to
	* @param {String} url  					URL to request suggestions from
	* @param {String} headers  				Extra headers to send with query header. These must be in string form.
	* @param {Function} onSelectCallback  	Function to call when an item is selected. It takes a single paramter: the item data.
	* @param {Number} minStringLength  		Minimum length of string before autosuggest begins. DEFAULT: 3
	* @param {Number} timeDelay  			Time to wait after typing ends before firing the autocomplete function. DEFAULT: 300 (in miliseconds)
	*/
	createAutocomplete: function(inputNodeID, url, headers, onSelectCallback, forceSelection, minStringLength, timeDelay) {
		
		//Check the basic inputs: input node id and the url
		if (!inputNodeID || !url) {return false;}
		
		//If no minStringLength or timeDelay have been passed, set the defaults.
		if (!minStringLength) {minStringLength = 3;}
		if (!timeDelay) {timeDelay = 300;}
				
		//Define the basic options to pass to the autocomplete function
		var autoCompleteOptions = {
			minLength: minStringLength,
			delay: timeDelay,
			source: function (request, response) {
				$.post(url, headers + '&query=' + request.term, function (data) {
					response(data);
				}, "json")
				 .fail(function(jqXHR, textStatus, errorThrown) {
					var errorHTML = '<strong>An error has occurred.</strong><br><br>';
					errorHTML += 'Error: ' + errorThrown + '<br><br>';
					errorHTML += 'Response: ' + jqXHR.responseText + '<br><br>';
					g.showMessage(errorHTML);
				 });
			}	
		};
		
		//If an onSelectCallback was passed, add that to the options
		if (onSelectCallback) {
			autoCompleteOptions.select = function (event, ui) {
				onSelectCallback(ui.item);
			};
		}
		
		//If an forceSelection was passed, add that to the options
		if (forceSelection) {
			autoCompleteOptions.change = function( event, ui ) {
				if (!ui.item){
					$(event.target).val("");
				}
			}
		}

		//Create the focus event
		autoCompleteOptions.focus = function( event, ui ) {
			if (ui.item.value) {
				$("#" + inputNodeID).val(ui.item.value);
				return false;
			}
		}
		
		//Modify the appearance
		$.ui.autocomplete.prototype._renderMenu = function(ul, items) {
			var thisMenu = this,
			currentCategory = "";
			$.each(items, function(index, item) {
				if ((item.category) && (item.category != currentCategory)) {
					ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
					currentCategory = item.category;
				}
				thisMenu._renderItemData(ul, item);
			});
		}

		$.ui.autocomplete.prototype._renderItem = function(ul, item) {
			return $("<li></li>")
				 .data("item.ui-autocomplete", item)
				 .append('<a>' + item.label + '</a>')
				 .appendTo(ul);
		};

		
		//Execute the autocomplete
		$("#" + inputNodeID).autocomplete(autoCompleteOptions);		
	},
	
	/**
	* Turns the passed node into a dialog.
	*
	* Returns an object with the member functions 'show' and 'hide' which display or hide the dialog.
	*
	* @param {String} nodeID				ID of node to turn into dialog
	* @param {String} title  				Title of dialog
	* @param {String} isClosable  			Determines whether or not there is an 'x' button in the dialog corner to close the dialog
	* @param {Function} width  				Set width of the dialog
	* @return {Object}			  			Object with two functions, 'show' and 'hide', that respectively display and hide the dialog
	*/
	createDialog: function(nodeID, title, isClosable, width) {
		
		//Check the nodeID
		if (!nodeID) {return false;}
				
		//Define the basic options to pass to the dialog function
		var dialogOptions = {
			title: title,
			width: width,
			autoOpen: false,
			modal: true,
			minHeight: 50
		};
		
		//If an isClosable was passed, add that to the options
		if (!isClosable) {
			dialogOptions.closeOnEscape = false;
			dialogOptions.open = function (event, ui) {
				//$(".ui-dialog-titlebar-close", ui.dialog).hide();
				//$(".ui-dialog-titlebar").hide();
				$(".ui-dialog-titlebar-close", $(this).parent()).hide();
			};
		}
				
		//Execute the dialog
		$("#" + nodeID).dialog(dialogOptions);
		
		//Create the return control object
		return displayControl = {
			open: function() {$("#" + nodeID).dialog("open");},
			close: function() {$("#" + nodeID).dialog("close");}
		}
	},

	/**
	* Displays a message to the current user. 
	* 
	* The message can be text or html. If no button one label is specified, "Continue" is used for the text. If a second button label is passed, the second
	* button is shown. Either button closes the dialog and executes the respective callback, if one was passed.
	*
	* @param {String} messageHTML  			HTML/Text to show in the message div
	* @param {String} buttonOneLabel  		Label to set the first button to
	* @param {String} buttonOneCallback  	Function to call after the first button is pressed
	* @param {String} buttonTwoLabel  		Label to set the second button to
	* @param {String} buttonTwoCallback  	Function to call after the second button is pressed
	*/
	showMessage: function(messageHTML, buttonOneLabel, buttonOneCallback, buttonTwoLabel, buttonTwoCallback) {
		
		//If the message dialog does not exist, create it
		if ($('#messageDialog').length == 0) {
			
			//Insert message dialog div to the end of the document
			$(document.body).append('\
				<div id="messageDialog" style="display:none;background:#FFCFCF;padding:5px;font-size:9px;"> \
					<div align="center"> \
						<div id="messageDiv" style="font-weight:bolder">.</div> \
						<br> \
						<input id="messageButtonOne" type="button" value="Continue" onclick="g.messageDialog.close()"> \
						<input id="messageButtonTwo" type="button" value="Cancel" onclick="g.messageDialog.close()" style="margin-left:50px;"> \
					</div> \
				</div>');
			
			//Turn div into dialog and store it
			this.messageDialog = this.createDialog('messageDialog', null, false, 450);
		}
		
		//If a button one label was not passed, set it to 'Continue'
		if (!buttonOneLabel) {buttonOneLabel = 'Continue';}

		//Insert the message HTML into the div
		this.nodeFromID('messageDiv').innerHTML = messageHTML;
		
		//Setup the first button
		this.nodeFromID('messageButtonOne').value = buttonOneLabel;
		this.nodeFromID('messageButtonOne').onclick = function() {
			g.messageDialog.close();
			if (buttonOneCallback) {setTimeout(buttonOneCallback, 350);}
		}
		
		//If a second button label was passed, setup the second button and show it
		if (buttonTwoLabel) {
			this.nodeFromID('messageButtonTwo').value = buttonTwoLabel;
			this.nodeFromID('messageButtonTwo').onclick = function() {
				g.messageDialog.close();
				if (buttonTwoCallback) {setTimeout(buttonTwoCallback, 350);}
			}
			this.show('messageButtonTwo');
		}
		
		//Otherwise, hide the second button
		else {this.hide('messageButtonTwo');}

		//Show the panel
		this.messageDialog.open();
		
		//Set the focus on the first button
		this.focus('messageButtonOne');
	},
	

	/**
	* Returns the city/state of the zipcode if found and places them in the passed callback function.
	*
	* @param {String} zipcode  					Zipcode to lookup
	* @param {Function} parentCallback  		Callback method to call on success. Needs two parameters (city, state) which are filled with their respective info or empty strings if no info was found.
	*/
	getZipcodeInfo: function(zipcode, parentCallback) {

		//If there are at least 5 characters in the  zipcode, request the info from the server
		if (zipcode.length >= 5) {
			
			//Create the callback function that sets the loading or shows any error messages
			var callback = function(response) {
				
				//If successful, put the info (if available), in the parent callback function
				if (response.success) {
					parentCallback(response.data.city, response.data.state);
				}
				
				//If failure, for now do nothing. 
				else {
					//g.showMessage(response.message, null, function() {focusAndSelect(response.focus);});
				}
			}
				
			//Make the calculate loading request
			this.asyncRequest('requests/generalRequests.php', 'request=sendZipcodeInfo&zipcode=' + zipcode.substr(0,5), callback);	
		}	
	},
};