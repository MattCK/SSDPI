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

/**
* Verify the session is valid and set the session constants
*/
require_once(RESTRICTEDPATH . 'validateSession.php');

use AdShotRunner\DFP\DFPCommunicator;

?>

<?PHP include_once(BASEPATH . "header.php");?>

<div id="contactFormDiv" class="section"> 
	<form id="asrContactForm">
		<input type="hidden" id="contactUserID" name="contactUserID" value="<?PHP echo USERID?>">
		<input type="hidden" id="contactJobID" name="contactJobID" value="<?PHP echo $_GET['jobID']?>">
		<table>
			<tr>
				<td>Name:</td>				
				<td><input id="contactName" name="contactName" type="text" value="<?PHP echo USERFIRSTNAME . " " . USERLASTNAME?>"></td>
			</tr>
			<tr>
				<td>Email:</td>				
				<td><input id="contactEmail" name="contactEmail" type="text" value="<?PHP echo USEREMAIL?>"></td>
			</tr>
			<tr>
				<td>Purpose:</td>
				<td>
					<label><input type='radio' name='contactType' value='IDEA' checked>Idea</label>
					<label><input type='radio' name='contactType' value='ISSUE'>Issue</label>
				</td>
			</tr>
			<tr>
				<td>Problem:</td>
				<td>
					<select id="contactProblem" name="contactProblem">
						<option value="NONECHOSEN" selected>Choose...</option>
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
				<td><textarea rows="4"></textarea></td>
			</tr>
			<tr>
				<td>&nbsp;</td>				
				<td><input type="button" id="contactSendButton" value="Send"></td>
			</tr>
		</table>
	</form>
</div>