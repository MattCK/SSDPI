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

?>

<?PHP include_once(BASEPATH . "header.php");?>

<script>

let asr = {

	_domain: '',
	_getMenuURL: 'getMenu.php',
	_menuItems: [],
	_menuOptions: "",
	_rowIndex: 0,

	getMenu: function() {

		//Create the callback function that will show the table
		let callback = function(response) {
			
			//If successful, clear and hide the plan form, show the new table, and highlight the new table row
			if (response.success) {
				
				//Store the menu items and create the menu options
				//console.log(response.data);
				asr._menuItems = response.data;
				asr._menuOptions = "<option value='/'>Main</option>";
				for (let menuTitle in asr._menuItems) {
					if (asr._menuItems.hasOwnProperty(menuTitle)) {
						asr._menuOptions += "<option value='" + asr._menuItems[menuTitle] + "'>" + menuTitle + "</option>";
					}
				}	
				//console.log(asr._menuOptions);

				//Store the domain and hide the input fields
				asr._domain = base.nodeFromID('adSiteDomain').value;
				base.hide("domainInputDiv");
				base.nodeFromID("domainNameDiv").innerHTML = asr._domain;
				base.show("domainNameDiv");
				base.show("pagesTableDiv");
			}
						
			//If failure, show us the message returned from the server and focus on the selected element if returned. Also, re-enable the submit button.
			else {
				alert(response.message);
			}
		}
		
		//Make the request
		base.asyncRequest(asr._getMenuURL, 'domain=' + base.nodeFromID('adSiteDomain').value, callback);
	},

	addPageRow: function() {

		let pageTable = base.nodeFromID("pagesTable");
		let newRow =  pageTable.insertRow(pageTable.rows.length);

		newRow.id = "pageRow" + asr._rowIndex;

		rowCells = "<td><select name='pageMenuItem[" + asr._rowIndex + "]'>" + asr._menuOptions + "</select></td>";
		rowCells += "<td><select name='pageStoryType[" + asr._rowIndex + "]'><option value='front'>Front Page</option><option value='find'>Find me a story</option></select></td>";
		rowCells += "<td><input type='checkbox' name='onlyScreenshot[" + asr._rowIndex + "]' value='1'>Take screenshot without inserting tags</td>";
		rowCells += "<td><input type='button' value='Delete' onClick='asr.deletePageRow(" + asr._rowIndex + ")'></td>";
		newRow.innerHTML = rowCells;

		asr._rowIndex += 1;

		base.enable("addTagsButton");
	},

	addURLRow: function() {

		let pageTable = base.nodeFromID("pagesTable");
		let newRow =  pageTable.insertRow(pageTable.rows.length);

		newRow.id = "pageRow" + asr._rowIndex;

		rowCells = "<td>Page URL: </td>";
		rowCells += "<td><input type='text' name='pageURL[" + asr._rowIndex + "]'></td>";
		rowCells += "<td><input type='checkbox' name='onlyScreenshot[" + asr._rowIndex + "]' value='1'>Take screenshot without inserting tags</td>";
		rowCells += "<td><input type='button' value='Delete' onClick='asr.deletePageRow(" + asr._rowIndex + ")'></td>";
		newRow.innerHTML = rowCells;

		asr._rowIndex += 1;

		base.enable("addTagsButton");
	},

	deletePageRow: function(rowID) {
		let rowToDelete = base.nodeFromID("pageRow" + rowID);
		rowToDelete.parentNode.removeChild(rowToDelete);

		if (base.nodeFromID("pagesTable").rows.length == 0) {base.disable("addTagsButton");}
	},

	toggleDivs: function() {
    	var $inner = $("#inner");

	    // See which <divs> should be animated in/out.
	    if ($inner.position().left == 0) {
	        $inner.animate({
	            left: "-960px"
	        });
	    }
	    else {
	        $inner.animate({
	            left: "0px"
	        });
	    }
	},
}

</script>

<body>
	<!--div id="container">
		<div id="inner">
			<div id="pageSelectionDiv">
				<div id="domainInputDiv">
					Site Domain: <input id="adSiteDomain" type="text">
					<input type="button" value="Go!" onclick="asr.getMenu()">
				</div>
				<div id="domainNameDiv"></div>
				<div id="pagesTableDiv" style="display: none;">
					<input type="button" value="Add page" onclick="asr.addPageRow()">
					<input type="button" value="Add URL" onclick="asr.addURLRow()"><br>
					<table id="pagesTable"></table>
				</div>
			</div>
			<div id="tagUploadDiv">asdfasdfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff</div>
		</div>
	</div-->
<div id="container">
    <div id="inner">
        <div id="home">
			<div id="domainInputDiv">
				Site Domain: <input id="adSiteDomain" type="text">
				<input type="button" value="Go!" onclick="asr.getMenu()">
				<input id="addTagsButton" type="button" value="Skip" onclick="asr.toggleDivs()">
			</div>
			<div id="domainNameDiv"></div>
			<div id="pagesTableDiv" style="display: none;">
				<input type="button" value="Add page" onclick="asr.addPageRow()">
				<input type="button" value="Add URL" onclick="asr.addURLRow()"><br>
				<table id="pagesTable"></table><br><br>
				<input id="addTagsButton" type="button" value="Add Tags" onclick="asr.toggleDivs()" disabled>
			</div>
        </div>
        <div id="member-home">
        	Enter Tag Script: <br><br>
        	<textarea rows="10" cols="100"></textarea><br><br>
			<input type="button" value="Back to Page Selection" onclick="asr.toggleDivs()">
        </div>
    </div> 
</div>
<input type="button" value="toggle" onclick="toggleDivs()"-->
</body>
</html>
