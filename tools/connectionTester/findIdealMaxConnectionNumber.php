<?PHP 
require_once('class.WebCommunicator.php');
Class WebCommunicatorWMaxConnections Extends WebPageCommunicator {
	
	public function setMaxConnections ($maxConnections) {
		parent::setMaxGroupSize($maxConnections);
	}
	/*public function getManyURLResponses($URLList){
		$urlResponse = parent::getManyURLGroupResponses($URLList);
		return $urlResponse;
	}*/
	
}

header("Content-Type: text/plain");

$publishers = getAssociativeArrayFromCSV('hugePublisherList.csv');
//print_r($publishers);
//print($argv[1]);
$incrementMaxConnectionsBy = 5; //increment  by
$startingConnectionNum = $incrementMaxConnectionsBy; //start
$topConnectionCeiling = 200; //end
$attemptNumMultiplier =3; //num of groups to try
if($argv[1] AND $argv[2] AND $argv[3] AND $argv[4]){
	//take in command line args
	if($argv[1]){$startingConnectionNum = $argv[1];} //start of range
	if($argv[2]){$topConnectionCeiling = $argv[2];} //end of range
	if($argv[3]){$incrementMaxConnectionsBy = $argv[3];} //increment by
	if($argv[4]){$attemptNumMultiplier = $argv[4];} //num of connection groups to try

	for ($curMaxConnections = $startingConnectionNum; $curMaxConnections < $topConnectionCeiling; $curMaxConnections = $curMaxConnections + $incrementMaxConnectionsBy) {
		print("----------------- " . $curMaxConnections . " ------------------------ \r\n");
		$webCommunicator = new WebCommunicatorWMaxConnections();
		//print("Created instance abbout to set max \r\n");
		$webCommunicator->setMaxConnections($curMaxConnections);
		//$maxConSetInClass = $webCommunicator->getMaxGroupSize();
		print("Set max connections to " . $webCommunicator->getMaxGroupSize() . " \r\n");
		$totalURLSToTry = $curMaxConnections * $attemptNumMultiplier;
		//if the publishers list isn't big enough for the current attempts to try
		//concatenate the list with it's self
		while ($totalURLSToTry > count($publishers)){
				$publishers = array_merge($publishers, $publishers);
		}
		$mtime = microtime(); 
		$mtime = explode(" ",$mtime); 
		$mtime = $mtime[1] + $mtime[0]; 
		$starttime = $mtime;
		print("attempting " . $totalURLSToTry . " connections \r\n");
		$webCommunicator->getManyURLResponses(array_slice($publishers, 0, $totalURLSToTry, true));
		$mtime = microtime(); 
		$mtime = explode(" ",$mtime); 
		$mtime = $mtime[1] + $mtime[0]; 
		$endtime = $mtime; 
		$totaltime = ($endtime - $starttime);
		print("Using " . $curMaxConnections . " connections took " . $totaltime . " seconds for " . $totalURLSToTry . " connections\r\n");
		print("averaging " . ($totaltime / $totalURLSToTry) . " seconds for each connection \r\n");
		print("|||||||||||||||| " . $curMaxConnections . " ||||||||||||||||||||||\r\n");
	}

}
Else {
	print("\r\n \r\n \r\nPlease provide arguments in the following form: \r\n");
	print("php thisfile.php StartOfRange EndOfRange IncrementBy ConnectionAttemptMultiplier \r\n");
	print("Ex: \r\n");
	print("php findIdealMaxConnectionNumber 5 100 5 3 \r\n");
	print("\r\n \r\n");
	print("Which would start with 5 simultaneous connections increment by 5 until it got to 100 \r\n");
	print("and try each increment 3 times. \r\n \r\n \r\n \r\n");

}

//Returns an associative array of the CSV file with the first column as keys and second as values
function getAssociativeArrayFromCSV($csvFile) {

	//Attempt to open the file
	$csvArray = array();
	if (($csvFileHandle = fopen($csvFile, "r")) !== FALSE) {
	
		while (($currentRowData = fgetcsv($csvFileHandle, 1000, ",")) !== FALSE) {
			$csvArray[$currentRowData[0]] = $currentRowData[1];
		}
		
		fclose($csvFileHandle);
	}
	
	//If file couldn't be opened, show error and exit
	else {echo "CSV couldn't be opened"; exit();}
	
	//Return the associative array of CSV data
	return $csvArray;
}

