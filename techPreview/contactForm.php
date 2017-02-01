<?PHP
/**
* Main app page that controls creation of screenshots
*
* @package AdShotRunner
*/
/**
* File to define paths, setup dependencies, and connect to database
*/
require_once('systemSetup.php');

use AdShotRunner\System\ASRProperties;

?>

<?PHP include_once(BASEPATH . "header.php");?>

<div id="contactFormDiv" class="section"> 
	<form id="asrContactForm">
		<input type="hidden" id="contactUserID" name="contactUserID" value="<?PHP if (defined("USERID")) {echo USERID;}?>">
		<input type="hidden" id="contactJobID" name="contactJobID" value="<?PHP echo $_GET['jobID']?>">
		<table>
			<tr>
				<td>Name:</td>				
				<td><input id="contactName" name="contactName" type="text" value="<?PHP if (defined("USERFIRSTNAME")) {echo USERFIRSTNAME . " " . USERLASTNAME;}?>"></td>
			</tr>
			<tr>
				<td>Email:</td>				
				<td><input id="contactEmail" name="contactEmail" type="text" value="<?PHP if (defined("USEREMAIL")) {echo USEREMAIL;}?>"></td>
			</tr>
			<tr>
				<td>Purpose:</td>
				<td>
					<label><input id="contactIdeaRadio" type='radio' name='contactType' value='IDEA' onclick="contactForm.selectIdea()" checked>Idea</label>
					<label><input id="contactIssueRadio" type='radio' name='contactType' value='ISSUE' onclick="contactForm.selectIssue()">Issue</label>
				</td>
			</tr>
			<tr id="contactProblemRow" style="display:none">
				<td>Problem:</td>
				<td>
					<select id="contactProblem" name="contactProblem">
						<option value="NONECHOSEN" selected>Choose...</option>
						<option value="POWERPOINTBACKGROUND">Problem uploading PowerPoint background image</option>
						<option value="SECTIONS">Sections for site are incomplete or incorrect</option>
						<option value="CREATIVEUPLOAD">Problem uploading creative</option>
						<option value="CREATIVEIMAGE">Creative images are incorrect or fail to load</option>
						<option value="STORY">Problem with automatically chosen story </option>
						<option value="POWERPOINT">Problem with final PowerPoint</option>
						<option value="OTHER">Other...</option>
					</select>
				</td>
			</tr>
			<tr>
				<td>Description:</td>				
				<td><textarea id="contactDescription" name="contactDescription" rows="4"></textarea></td>
			</tr>
			<tr>
				<td>&nbsp;</td>				
				<td><input type="button" id="contactSendButton" value="Send" onclick="contactForm.submitForm()"></td>
			</tr>
		</table>
	</form>
	<div id="contactThankYouDiv" style="display:none;">
		Thank you for your feedback! It is very important to us.<br><br>

		We will respond within two business days, but usually much sooner.
	</div>
	<div id="contactFailureDiv" style="display:none;">
		Submission failed. Please try again later.<br><br>

		We apologize for this inconvenience. Your feedback is very importan to us. 
		In the meantime, please email us at: <a href="mailto:<?PHP echo ASRProperties::emailAddressContact()?>"><?PHP echo ASRProperties::emailAddressContact()?></a>
	</div>
</div>
