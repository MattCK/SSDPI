/**
*  nytimes.com login script
*/

let emailField = document.querySelector("input#userid");
let passwordField = document.querySelector("input#password1");
let submitButton = document.querySelector("button#submit-button");
if (emailField && passwordField && submitButton) {
	emailField.value = "matt@dangerouspenguins.com";
	passwordField.value = "bYZ18mybDO";
	submitButton.click();
}