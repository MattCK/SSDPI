/**
* The findValue function searches all global window variables for the passed value. 
*
* If the search argument is found, the variable keys will be outputted to the console. 
* They are listed from inner container to outer container.
*
* Example: On fr.msn.com, a search for 728 returns:
*
* w = 728
* 0
* adsDivs
*
* which means the variable is in adsDivs[0].w
*/

/**
* USAGE: Set 'searchValue' to the value to search for, copy and paste this whole script into
* 	     the browser console, and hit enter.
*/

//Created undefined variables
if (typeof searchValue === 'undefined') {let searchValue;}
if (typeof objectToSearch === 'undefined') {let objectToSearch;}
if (typeof objectName === 'undefined') {let objectName;}
if (typeof outputContainingObject === 'undefined') {let outputContainingObject;}

//Set the object and value to search
objectToSearch = window;
objectName = "window";
searchValue = "ad-position-73";

//If TRUE, the containing object holding the search value and its key will be output to the console
outputContainingObject = true;

/**
* Recursively searches the window global variables for a specific value and, if found, outputs
* the keys.
*/
function findValue(searchValue) {

	//let propertyHistory = '';
	let cache = [];
	let recursiveFind = function(currentObject, currentHistory) {

		//If we have already seen this object, ignore it and return to prevent circular reference
	    if (typeof currentObject === 'object' && currentObject !== null) {
	        if (cache.indexOf(currentObject) !== -1) {
	            return;
	        }

	        //Store this object in the cache to prevent circular references to it
	        cache.push(currentObject);

	        //Loop through the object's properties, traversing them if necessary, while looking
	        //for a variable with the searchValue value
			for (var key in currentObject) {
				if (currentObject.hasOwnProperty(key)) {
					let value = currentObject[key];

		        	//Surround the calls in a try catch to prevent IFrame security issues
		        	try {

		        		//If the current value is an object, call this recursive function on it
					    if (typeof value == "object" ) {

							let found = recursiveFind(value, currentHistory + '["' + key + '"]')
					    	if (found && outputContainingObject) {
					    		console.log(value);
								// propertyHistory = '["' + key + '"]' + propertyHistory;
					    		// return true;
					    	};
							//If true is returned, output the current key
					    	// if (recursiveFind(value)) {
					    	// 	console.log(key);
							// 	propertyHistory = '["' + key + '"]' + propertyHistory;
					    	// 	return true;
					    	// };

					    }

					    //If the current value is a variable, compare it against the searchValue
					    //If they are equal, output the current key with the value and return true.
					    else {
					    	if (value == searchValue) {
								// console.log(key + " = " + value); 
								// propertyHistory = '["' + key + '"]' + propertyHistory;
								console.log(currentHistory + '["' + key + '"]');
					    		return true;
					    	}
					    }
		        	} catch(e) {}
		        }
	        }
		}
	}

	recursiveFind(objectToSearch, objectName);
	// console.log("Property List: " + propertyHistory);
}

console.log('');
console.log("-------------------- Recursive Search -------------------");
console.log("Searching: " + objectName);
console.log("For Value: " + searchValue);
console.log('');

findValue(searchValue);

console.log("---------------------------------------------------------");
