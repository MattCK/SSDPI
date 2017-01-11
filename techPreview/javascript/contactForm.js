/**
* Contains Javascript functions for the contact form interactions
*
* @package AdShotRunner
* @subpackage JavaScript
*/

var contactForm = {

	_submitFormURL: 'submitContactForm.php',		//URL of request page used to submit the form's data

	/**
	* Checks the 'Idea' radio button and hides the 'Prolem' row
	*/
	selectIdea: function() {
		base.check("contactIdeaRadio");
		base.hide("contactProblemRow");
	},

	/**
	* Checks the 'Issue' radio button and shows the 'Prolem' row
	*/
	selectIssue: function() {
		base.check("contactIssueRadio");
		base.show("contactProblemRow");
	},

	/**
	* Submits the contact form's information to the server
	*
	* On success, the user is shown the thank you for your feedback text
	*/
	submitForm: function() {

		//First, make sure all of the fields are filled out
		if (base.nodeFromID("contactName").value.trim() == "") {
			alert("Please enter your name."); return;
		}
		else if (base.nodeFromID("contactEmail").value.trim() == "") {
			alert("Please enter your email."); return;
		}
		else if (base.isChecked("contactIssueRadio") && (base.nodeFromID("contactProblem").value == "NONECHOSEN")) {
			alert("Please select an issue from the Problem menu."); return;
		}
		else if (base.nodeFromID("contactDescription").value.trim() == "") {
			alert("Please enter a description. Be thorough."); return;
		}

		//Create the callback function that will either show the feedback thanks text or error text
		let callback = function(response) {
			
			//If successful, hide the form and show the thank you text
			if (response.success) {
				base.hide("asrContactForm");
				base.show("contactThankYouDiv");
			}
						
			//If failure, hide the form and show the failure text
			else {
				base.hide("asrContactForm");
				base.show("contactFailureDiv");
			}
		}
		
		//Make the request
		base.asyncRequest(contactForm._submitFormURL, base.serializeForm('asrContactForm'), callback);
	},

	/**
	* Resets the form to its original setup, showing the form with idea checked, the problem set to 'choose',
	* the description removed, and the response divs hidden
	*/
	reset: function() {
		base.hide("contactThankYouDiv");
		base.hide("contactFailureDiv");
		base.show("asrContactForm");
		contactForm.selectIdea();
		base.nodeFromID("contactProblem").value = "NONECHOSEN";
		base.nodeFromID("contactDescription").value = "";
	},


}