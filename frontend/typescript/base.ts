/**
* Contains general javascript functions used throughout the AdShotRunner system.
*
* @package AdShotRunner
* @subpackage JavaScript
*/
//Base object for all system functions and variables
namespace base {

	let messageDialog: {open: () => void, close: () => void};

	/**
	* Returns the node in the DOM with the passed ID. Returns NULL on failure.
	*
	* @param {String} nodeID  ID of the document element to retrieve
	* @return {HTMLElement}  Element in the document with the passed ID. NULL on failure.
	*/
	export function nodeFromID(nodeID: string) {
		return $('#' + nodeID).get(0);
	}
	
	/**
	* Executes the passed function when the DOM is fully ready/loaded.
	*
	* @param {function} launchFunction  Function to execute when the DOM is ready.
	* @return {HTMLElement}  Element in the document with the passed ID. NULL on failure.
	*/
	export function onReady(launchFunction: () => void) {
		$(launchFunction);
	}

	/**
	* Shows the node with the passed ID 
	*
	* @param {String} nodeID  ID of the node to show
	*/
	export function show(nodeID: string) {
		$('#' + nodeID).show();		
	}

	/**
	* Hides the node with the passed ID 
	*
	* @param {String} nodeID  ID of the document node to show
	*/
	export function hide(nodeID: string) {
		$('#' + nodeID).hide();		
	}

	/**
	* Returns TRUE if the display style of the HTMLElement in the DOM with the passed ID is set to '' (shown) and FALSE otherwise. 
	*
	* @param {String} nodeID  ID of the document element to retrieve
	* @return {HTMLElement}  TRUE if the display is '' (shown) and FALSE otherwise (hidden).
	*/
	export function isShown(nodeID: string) {
		return $('#' + nodeID).is(":visible");
	}

	/**
	* If the node with the passed ID is shown, it is hidden. If it is hidden, it is then shown. 
	*
	* @param {String} nodeID  ID of the node with the display to toggle
	*/
	export function toggle(nodeID: string){
		$('#' + nodeID).toggle();		
	}

	/**
	* Enables the node with the passed ID. 
	*
	* @param {String} nodeID  ID of the node to enable
	*/
	export function enable(nodeID: string) {
		$('#' + nodeID).prop("disabled", false);	
	}

	/**
	* Disables the node with the passed ID. 
	*
	* @param {String} nodeID  ID of the node to disable
	*/
	export function disable(nodeID: string) {
		$('#' + nodeID).prop("disabled", true);	
	}

	/**
	* Returns TRUE if the disabled property of the HTMLElement in the DOM with the passed ID is set to FALSE and returns TRUE otherwise. 
	*
	* @param {String} nodeID  ID of the document element to retrieve
	* @return {HTMLElement}  TRUE if the disabled property is FALSE and FALSE otherwise.
	*/
	export function isEnabled(nodeID: string) {
		return (!(<HTMLInputElement>nodeFromID(nodeID)).disabled);
	}

	/**
	* Checks the checkbox node with the passed ID. 
	*
	* @param {String} nodeID  ID of the checkbox node to check
	*/
	export function check(nodeID: string) {
		$('#' + nodeID).prop("checked", true);	
	}

	/**
	* Unchecks the checkbox node with the passed ID. 
	*
	* @param {String} nodeID  ID of the checkbox node to uncheck
	*/
	export function uncheck(nodeID: string) {
		$('#' + nodeID).prop("checked", false);	
	}

	/**
	* Returns TRUE if the checked property of the HTMLElement in the DOM with the passed ID is set to TRUE and returns FALSE otherwise. 
	*
	* @param {String} nodeID  ID of the document element to retrieve
	* @return {HTMLElement}  TRUE if the checked property is TRUE and FALSE otherwise.
	*/
	export function isChecked(nodeID: string){
		return (<HTMLInputElement>nodeFromID(nodeID)).checked;
	}

	/**
	* Puts the focus on the input node with the passed ID.  
	*
	* @param {String} nodeID  ID of the input node to focus upon
	*/
	export function focus(nodeID: string) {
		$('#' + nodeID).focus();
	}

	/**
	* Selects the node in the DOM with the passed ID.  
	*
	* @param {String} nodeID  ID of the document element to select
	*/
	export function select(nodeID: string) {
		$('#' + nodeID).select();
	}

	/**
	* Focuses upon and selects the HTMLInput element in the DOM with the passed ID.  
	*
	* Note: The reason we have to do both instead of just selecting is because IE is dumb.
	* NOTE: IE NOT USED. REPLACE IN CODE.
	*
	* @param {String} nodeID  ID of the document element to focus and select
	*/
	export function focusAndSelect(nodeID: string) {
		focus(nodeID);
		select(nodeID);
	}

	/**
	* Clears the passed form and unchecks all buttons. Hidden, radio, and checkbox values are preserved. 
	*
	* @param {String} formID  ID of the form node to clear
	*/
	export function clearForm(formID: string) {
		$('#' + formID).find('input:text, input:password, input:file, select, textarea').val('');
		$('#' + formID).find('input:radio, input:checkbox').removeAttr('checked').removeAttr('selected');
	}

	/**
	* Enables all the fields in a form. 
	*
	* @param {String} formID  ID of the form element to enable fields
	*/
	export function enableFormFields(formID: string) {
		$('#' + formID + " :input").prop("disabled", false);
	}

	/**
	* Disables all the fields in a form. 
	*
	* @param {String} formID  ID of the form element to disable fields
	*/
	export function disableFormFields(formID: string) {
		$('#' + formID + ' :input').prop("disabled", true);
	}
	
	/**
	* Turns the form into a proper header string and returns it. 
	*
	* @param {String} formID  ID of the form element to serialize
	*/
	export function serializeForm(formID: string) {
		return $('#' + formID).serialize();
	}

	/**
	* Moves focus to the next element when the current element reaches a certain length. 
	*
	* @param {String} curElementID  ID of the current form element
	* @param {Number} maxLength  Maximum length the current element should reach
	* @param {String} nextElementID  ID of the next form element to focus upon
	*/
	export function autoTab(curElementID: string, maxLength: number, nextElementID: string) {
		if ((<HTMLInputElement>nodeFromID(curElementID)).value.length >= maxLength) {focusAndSelect(nextElementID);}
	}


	export function numberMask(evt: KeyboardEvent, allowDecimal: boolean) {
			
		//Get the char code and target from the keypress event
		let charCode = (evt.which || (evt.which == 0))  ? evt.which : evt.keyCode;
		let targetElement = (evt.target) ? evt.target : evt.srcElement;
		
		//If a alt is pressed ignore everything
		if (evt.altKey) {return false;}
		
		//If shift is pressed with a character key, ignore it
		if ((evt.shiftKey) && (charCode >= 33 && charCode <= 126)) {return false;}
			
		//If decimals are allowed and the key pressed is a '.', verify it is the first and only. If not return false
		if ((allowDecimal) && (charCode == 46)) {
			if ((<HTMLInputElement>targetElement).value.indexOf('.') > -1) {return false;}
		}
		
		//Otherwise, if the key pressed is not a number, return false
		else if ((charCode >= 32 && charCode <= 47) || (charCode >= 58 && charCode <= 126) || (charCode >= 128)) {
			return false;
		}
		
		//Otherwise return true
		return true;
	}

	export function dateMask(evt: KeyboardEvent) {

		//Get the char code and target from the keypress event
		let charCode = (evt.which || (evt.which == 0))  ? evt.which : evt.keyCode;
		let targetElement = <HTMLInputElement>((evt.target) ? evt.target : evt.srcElement);
		let targetValue = (!targetElement.value) ? targetElement.innerHTML.trim() : targetElement.value;

		//If a alt is pressed ignore everything
		if (evt.altKey) {return false;}
		
		//If shift is pressed with a character key, ignore it
		if ((evt.shiftKey) && (charCode >= 33 && charCode <= 126)) {return false;}
		
		//If there are already 10 chars and the key pressed isn't backspace or delete or an arrow and nothing is selected, return false
		if ((targetValue.length >= 10) && (charCode != 8) && (charCode != 127) && (charCode != 0) && (!textIsSelected(targetElement))) {
			return false;
		}
		
		//If the key pressed is a '/', verify it is the first or second. If not return false
		if (charCode == 47) {
			let slashMatches = targetValue.match(/\//g);
			if (slashMatches && (slashMatches.length >= 2)) {return false;}
		}
		
		//Otherwise, if the key pressed is not a number or movement key, return false
		else if ((charCode >= 32 && charCode <= 47) || (charCode >= 58 && charCode <= 126) || (charCode >= 128)) {
			return false;
		}
		
		//Otherwise return true
		return true;
	}

	export function dateFormat(evt: KeyboardEvent) {

		//Get the char code and target from the keypress event
		let charCode = (evt.which || (evt.which == 0))  ? evt.which : evt.keyCode;
		let targetElement = <HTMLInputElement>((evt.target) ? evt.target : evt.srcElement);
		let targetValue = (!targetElement.value) ? targetElement.innerHTML.trim() : targetElement.value;

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
		let dateParts = targetValue.split('/');
		
		//If the first part is greater than 12, replace it with 12
		if (parseInt(dateParts[0]) > 12) {
			if (targetElement.value) {targetElement.value = targetElement.value.replace(/^\d+/, '12');}
			else {targetElement.innerHTML = targetElement.innerHTML.replace(/^\d+/, '12');}
		}

		//If the second part exists and is greater than 31, replace it with 31
		if ((dateParts.length > 1) && (parseInt(dateParts[1]) > 31)) {
			if (targetElement.value) {targetElement.value = targetElement.value.replace(/\/\d+/, '/31');}
			else {targetElement.innerHTML = targetElement.innerHTML.replace(/\/\d+/, '/31');}
		}
	}

	export function insertTodaysDate(evt: KeyboardEvent) {

		//Get the target from the keypress event
		let targetElement = (evt.target) ? evt.target : evt.srcElement;
		
		//Insert today's date into the target
		(<HTMLInputElement>targetElement).value = getTodaysDate();	
	}

	export function getTodaysDate() {

		//Format today's date
		let todaysDate = new Date();
		let incrementedMonth = todaysDate.getMonth() + 1;
		let currentDay = (todaysDate.getDate() < 10) ? "0" + todaysDate.getDate() : todaysDate.getDate();
		let currentMonth = (incrementedMonth < 10) ? "0" + incrementedMonth : incrementedMonth;
		let currentYear = todaysDate.getFullYear();
		
		//Add slashes and return it
		return currentMonth + '/' + currentDay + '/' + currentYear;	
	}

	export function enableButtonOnTextInput(evt: KeyboardEvent, buttonID: string) {
		
		//Get the target from the keypress event
		let targetElement = <HTMLInputElement>((evt.target) ? evt.target : evt.srcElement);
		
		//If there is at least one character, enable the button. Otherwise, disable it.
		if (targetElement.value.length > 0) {enable(buttonID);}
		else {disable(buttonID)};
	}

	export function textIsSelected(textElement: HTMLInputElement) {
		//If text is selected, return true
		// if ((document.selection) && (document.selection.type == "Text")) {return true;}
		if ((textElement.selectionStart != null) && (textElement.selectionEnd != null) && (textElement.selectionStart != textElement.selectionEnd)) {return true;}  
		
		//Otherwise, return false
		return false;
	}

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
	export function highlightNode(nodeID: string, startColor: string, endColor: string, duration: number) {

		//Set the starting colors and duration if not passed
		if (startColor == "") startColor = 'A5DFE2';
		if (endColor == "") endColor = 'FFFFFF';
		if (duration <= 0) duration = 4000;

		//Execute the highlight animation
		$('#' + nodeID).css('background-color', '#' + startColor).animate({
						backgroundColor: '#' + endColor,
					}, duration);
	}

	/**
	* Animates the background color of the table row with the passed ID teal to white over 4 seconds.
	*
	* @param {String} rowID  		ID of the table row to highlight
	*/
	export function highlightRow(rowID: string) {
		//Re-passes defaults in case base function defaults change
		highlightNode(rowID, 'A5DFE2', 'FFFFFF', 4000);
	}

	/**
	* Returns the extension of the filename.
	*
	* The extension is simply the text after the last '.' . This function returns it as lowercase.
	*
	* @param {String} filename  		Filename.
	* @return {String}  			Filename extension as lowercase
	*/
	export function getFilenameExtension(filename: string) {
		if (filename.lastIndexOf('.') >= 0) {return filename.substr(filename.lastIndexOf('.') + 1).toLowerCase();}
		else {return '';}
	}

	/**
	* Sends a request to the server and executes the callback function on the returned data. 
	* The basic usage uses POST unless otherwise flagged. The function expects a JSON response.
	* The on success callback takes a single argument, 'data', the returned data converted to a JSON object.
	* 
	*
	* @param {String} 	url  					URL to send the request to
	* @param {String}  	headers  				Post headers to send. This can be a string or an associated array with keys as names and values as data.
	* @param {Function} onSuccessFunction  		Function to call on successful request. Takes a single argument for the JSON returned from the server.
	* @param {Function} onFailFunction  		Function to call on a failed request. Returns two arguements: textStatus and errorThrown
	* @param {Boolean} 	useUnprocessedData  	Flags whether or not to process the data DEFAULT: false
	* @param {Boolean} 	useGET  				On true, the request is sent via GET. Otherwise, it is sent via POST (including when omitted).
	*/
	export function asyncRequest(url: string, headers: string | object, 
						   onSuccessFunction: JQuery.Ajax.SuccessCallback<any>, 
						   onFailFunction: (textStatus: string, errorThrown: string) => void = function(){}, 
						   useUnprocessedData = false, useGET = false) {
		
		//Set the GET/POST communication type based on the passed argument
		let communicationType = (useGET) ? 'GET' : 'POST';
					
		//Flag whether or not to use raw data. This is opposite what's expected.
		let processFlag = (useUnprocessedData) ? false : true;
		
		//Set the options
		let ajaxOptions : JQueryAjaxSettings = {
			url: url,
			data: headers,
			processData: processFlag,
			success: onSuccessFunction,
			type: communicationType,
			dataType: 'json',
		};
		
		//If unprocessed data needs to be sent, remove content type
		if (useUnprocessedData) {ajaxOptions.contentType = false;}
		
		//Setup and make the AJAX call
		$.ajax(ajaxOptions)
		 .fail(function(jqXHR, textStatus, errorThrown) {
		 	if (onFailFunction) {onFailFunction(textStatus, errorThrown);}
		 	else {
				let errorHTML = "There was difficulty communicating with the server.<br/><br/>";
				errorHTML += "Check your internet connection and try refreshing your browser.<br/><br/>";
				errorHTML += "If the problem persists, please contact us.";
				showMessage(errorHTML, "A Problem has Occurred");

				console.log("An error occurred: " + textStatus);
				console.log("Error Thrown: " + errorThrown);
			}
			//base.showMessage(errorHTML);
		});	
	}

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
	export function createAutocomplete(inputNodeID: string, url: string, headers: string, 
								 onSelectCallback: (item: JQueryUI.AutocompleteUIParams) => void, 
								 forceSelection: JQueryUI.AutocompleteEvent, 
								 minStringLength: number, timeDelay: number) {
		
		//Check the basic inputs: input node id and the url
		if (!inputNodeID || !url) {return false;}
		
		//If no minStringLength or timeDelay have been passed, set the defaults.
		if (!minStringLength) {minStringLength = 3;}
		if (!timeDelay) {timeDelay = 300;}
				
		//Define the basic options to pass to the autocomplete function
		let autoCompleteOptions : JQueryUI.AutocompleteOptions = {
			minLength: minStringLength,
			delay: timeDelay,
			source: function (request: any, response: any) {
				$.post(url, headers + '&query=' + request.term, function (data) {
					response(data);
				}, "json")
				 .fail(function(jqXHR, textStatus, errorThrown) {
					let errorHTML = '<strong>An error has occurred.</strong><br><br>';
					errorHTML += 'Error: ' + errorThrown + '<br><br>';
					errorHTML += 'Response: ' + jqXHR.responseText + '<br><br>';
					showMessage(errorHTML);
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
			return;
		}
		
		//Modify the appearance
		$.ui.autocomplete.prototype._renderMenu = function(ul: any, items: any) {
			let thisMenu = this,
			currentCategory = "";
			$.each(items, function(index, item) {
				if ((item.category) && (item.category != currentCategory)) {
					ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
					currentCategory = item.category;
				}
				thisMenu._renderItemData(ul, item);
			});
		}

		$.ui.autocomplete.prototype._renderItem = function(ul: any, item: any) {
			return $("<li></li>")
				 .data("item.ui-autocomplete", item)
				 .append('<a>' + item.label + '</a>')
				 .appendTo(ul);
		};

		
		//Execute the autocomplete
		$("#" + inputNodeID).autocomplete(autoCompleteOptions);	
		return;	
	}
	
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
	export function createDialog(nodeID: string, title: string, isClosable: boolean, width: number) :
							{open: () => void, close: () => void} {
		
		//Check the nodeID
		//if (!nodeID) {return false;}
				
		//Define the basic options to pass to the dialog function
		let dialogOptions: JQueryUI.DialogOptions = {
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
				$(".ui-dialog-titlebar-close", $(this).parent()).hide();
			};
		}
				
		//Execute the dialog
		$("#" + nodeID).dialog(dialogOptions);
		
		//Create the return control object
		return {
			open: function() {$("#" + nodeID).dialog("open");},
			close: function() {$("#" + nodeID).dialog("close");}
		}
	}

	/**
	* Displays a message to the current user. 
	* 
	* The message can be text or html. 
	*
	* @param {String} messageHTML  			HTML/Text to show in the message div
	* @param {String} dialogTitle  			Optional title to show in dialog top title bar
	*/
	export function showMessage(messageHTML: string, dialogTitle = "") {
		
		//If the message dialog does not exist, create it
		if ($('#messageDialog').length == 0) {
			
			//Insert message dialog div to the end of the document
			$(document.body).append('\
				<div id="messageDialog"> \
					<div align="center"> \
						<div id="messageDiv"></div> \
					</div> \
				</div>');

			//Turn div into dialog and store it
			messageDialog = createDialog('messageDialog', dialogTitle, true, 600);
		}
		
		//Insert the message HTML into the div
		nodeFromID('messageDiv').innerHTML = messageHTML;
		
		//Show the panel
		messageDialog.open();
	}
	
}

