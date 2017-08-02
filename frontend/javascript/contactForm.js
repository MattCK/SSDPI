"use strict";
/**
* Contains Javascript functions for the contact form interactions
*
* @package AdShotRunner
* @subpackage JavaScript
*/
var contactForm;
(function (contactForm) {
    const _submitFormURL = 'submitContactForm.php'; //URL of request page used to submit the form's data
    /**
    * Checks the 'Idea' radio button and hides the 'Problem' row
    */
    function selectIdea() {
        base.check("contactIdeaRadio");
        base.hide("contactProblemRow");
    }
    contactForm.selectIdea = selectIdea;
    /**
    * Checks the 'Issue' radio button and shows the 'Problem' row
    */
    function selectIssue() {
        base.check("contactIssueRadio");
        base.show("contactProblemRow");
    }
    contactForm.selectIssue = selectIssue;
    /**
    * Submits the contact form's information to the server
    *
    * On success, the user is shown the thank you for your feedback text
    */
    function submitForm() {
        //First, make sure all of the fields are filled out
        if (base.nodeFromID("contactName").value.trim() == "") {
            alert("Please enter your name.");
            return;
        }
        else if (base.nodeFromID("contactEmail").value.trim() == "") {
            alert("Please enter your email.");
            return;
        }
        else if (base.isChecked("contactIssueRadio") && (base.nodeFromID("contactProblem").value == "NONECHOSEN")) {
            alert("Please select an issue from the Problem menu.");
            return;
        }
        else if (base.nodeFromID("contactDescription").value.trim() == "") {
            alert("Please enter a description. Be thorough.");
            return;
        }
        //Create the callback function that will either show the feedback thanks text or error text
        let onSuccessCallback = function (response) {
            //If successful, hide the form and show the thank you text
            if (response.success) {
                base.hide("asrContactForm");
                base.show("contactThankYouDiv");
            }
            else {
                alert(response.message);
                base.hide("asrContactForm");
                base.show("contactFailureDiv");
            }
        };
        //If there was a problem connecting with the server, notify the user
        let onFailCallback = function (textStatus, errorThrown) {
            //Show the error message
            asr.showErrorMessage("trying to submit contact form.");
        };
        //Make the request
        base.asyncRequest(_submitFormURL, base.serializeForm('asrContactForm'), onSuccessCallback);
    }
    contactForm.submitForm = submitForm;
    /**
    * Resets the form to its original setup, showing the form with idea checked, the problem set to 'choose',
    * the description removed, and the response divs hidden
    */
    function reset() {
        base.hide("contactThankYouDiv");
        base.hide("contactFailureDiv");
        base.show("asrContactForm");
        contactForm.selectIdea();
        base.nodeFromID("contactProblem").value = "NONECHOSEN";
        base.nodeFromID("contactDescription").value = "";
    }
    contactForm.reset = reset;
})(contactForm || (contactForm = {}));
