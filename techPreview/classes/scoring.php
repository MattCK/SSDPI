<?PHP

/**
* Contains the class for retrieving user bracket scores
*
* @package bracket
* @subpackage Classes
*/

/**
* The Scoring class determines a user's score basing their bracket against the master bracket. This can be
* done for an individual user or all users.
*/
class Scoring {

	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Methods ------------------------------------
	//---------------------------------------------------------------------------------------	
	/**
	* Returns the score of the user with the passed ID.
	*
	* @param int $userID  	ID of the user to score
	* @return number  		Score of the user. NULL on failure.
	*/
	static public function getUserScore($userID) {
		
		//Verify the ID and that it's not less than 1. If not, return NULL.
		if ((!$userID) || (!is_numeric($userID)) || ($userID < 1)) {return NULL;}
		
		//Get the info from the game picks table and put them in an array for easy access
		$getPicksQuery = "SELECT * FROM gamepicks WHERE GP_USR_id = $userID";
		$getPicksResult = dbQuery($getPicksQuery);
		$picksList = array();
		while ($curPickInfo = mysql_fetch_array($getPicksResult)) {
			$picksList[$curPickInfo['GP_BR_id']] =  $curPickInfo['GP_TM_id'];
		}
		
		//Get the master branches from the database and store it as well
		$getBranchesQuery = "SELECT * FROM branches";
		$getBranchesResult = dbQuery($getBranchesQuery);
		$branchList = array();
		while ($curBranchInfo = mysql_fetch_array($getBranchesResult)) {
			$branchList[$curBranchInfo['BR_id']] =  array('index' => $curBranchInfo['BR_index'],
														  'isBase' => $curBranchInfo['BR_isBase'],
														  'teamID' => $curBranchInfo['BR_TM_id']);
		}
		
		//Return the final score
		return self::getScore($picksList, $branchList);
	}
		
	/**
	* Returns all scores of the users who have bought in.
	*
	* @return Array  		Scores of the pool users. ('user id' => ('score', 'name')
	*/
	static public function getPoolScores() {
				
		//Get the info from the game picks table 
		$getPicksQuery = "	SELECT *
							FROM gamepicks
							LEFT JOIN users ON (GP_USR_id = USR_id)
							WHERE USR_transactionData != ''
							ORDER BY USR_firstName ASC";
		$getPicksResult = dbQuery($getPicksQuery);
		
		//Add the picks to an array of the users and store the user full names
		$poolPicksLists = array();
		$userFullNames = array();
		while ($curPickInfo = mysql_fetch_array($getPicksResult)) {
			
			//If this is the first pick for the user, create its array
			if (!$poolPicksLists[$curPickInfo['GP_USR_id']]) {$poolPicksLists[$curPickInfo['GP_USR_id']] = array();}
			
			//Add the branch and pick to the current users list
			$poolPicksLists[$curPickInfo['GP_USR_id']][$curPickInfo['GP_BR_id']] = $curPickInfo['GP_TM_id'];
			
			//If not already, store the user full name
			$userFullNames[$curPickInfo['GP_USR_id']] = $curPickInfo['USR_firstName'] . ' ' . $curPickInfo['USR_lastName'];
		}
		
		//Get the master branches from the database and store it as well
		$getBranchesQuery = "SELECT * FROM branches";
		$getBranchesResult = dbQuery($getBranchesQuery);
		$branchList = array();
		while ($curBranchInfo = mysql_fetch_array($getBranchesResult)) {
			$branchList[$curBranchInfo['BR_id']] =  array('index' => $curBranchInfo['BR_index'],
														  'isBase' => $curBranchInfo['BR_isBase'],
														  'teamID' => $curBranchInfo['BR_TM_id']);
		}
		
		//Loop through each user and get their scores
		$poolScoresArray = array();
		$scoreSortArray = array();
		foreach ($poolPicksLists as $curUserID => $curPicksList) {
			$poolScoresArray[$curUserID] = array('score' => self::getScore($curPicksList, $branchList),
												 'name' => $userFullNames[$curUserID]);
			$scoreSortArray[$curUserID] = $poolScoresArray[$curUserID]['score'];
		}
		
		//Sort the results by score
		arsort($scoreSortArray);
		$finalScoresArray = array();
		foreach ($scoreSortArray as $curBranchID => $curScore) {
			$finalScoresArray[$curBranchID] = $poolScoresArray[$curBranchID];
		}
		
		//Return the final pool scores
		return $finalScoresArray;
	}

	/**
	* Returns the score based on the picks and master branches passed
	*
	* @param Array $picksList  		Associative array of user game picks ('branch id' => 'team pick id')
	* @param Array $branchList  	Associative array of master branches ('branch id' => ('index', 'isBase', 'teamID')
	* @return number  				Score of the cumulative picks. NULL on failure.
	*/
	static private function getScore($picksList, $branchList) {
		
		//Get the final score by looping through the picks and comparing them to the master branches
		$finalScore = 0;
		foreach ($picksList as $curPickBranchID => $curPickTeamID) {
			$curMasterBranch = $branchList[$curPickBranchID];
			if ((!$curMasterBranch['isBase']) && ($curMasterBranch['teamID'] != 0) && ($curPickTeamID == $curMasterBranch['teamID'])) {
			
				//Get the branch score
				$curscoringLevel = floor(log($curMasterBranch['index'], 2));
				$curScore = self::$scoringLevels[$curscoringLevel];
				
				//Add the score to the current user score
				$finalScore += $curScore;
			}
		}

		//Return the score
		return $finalScore;
	}


	//---------------------------------------------------------------------------------------
	//----------------------------------- Static Accessors ----------------------------------
	//---------------------------------------------------------------------------------------
	//********************************* Public Accessors ************************************
	/**
	* Returns current scoring levels being used. The key refers to LEVEL of the tree (NOT ROUND)
	*
	* @return Array  Scoring levels
	*/
	static public function getScoringLevels(){
		return $this->scoringLevels;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------ Variables ----------------------------------------
	//---------------------------------------------------------------------------------------	
	//******************************** Private Variables ************************************
	/**
	* @var Array  Associative array of score based on LEVEL of the tree (NOT ROUND)
	*/
	private static $scoringLevels = array(	'0' => 12,
											'1' => 10,
											'2' => 8,
											'3' => 6,
											'4' => 4,
											'5' => 2);




	
			
}
