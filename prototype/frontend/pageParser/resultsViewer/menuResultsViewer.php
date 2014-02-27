<?PHP

//require_once('../menuGrabLib.php');

//$menuResultsFilename = dirname(__FILE__) . '/results/menuWordFrequencies-'"";
$menuResultsFilename = '../results/publisherMenus-1393379213.csv';

$row = 1;
$urlInfo = array();
if (($handle = fopen($menuResultsFilename, "r")) !== FALSE) {

    while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
	
		$curMenu = array();
        for ($c=0; $c < count($data); $c++) {
            $curMenu[] = $data[$c];
        }
        $urlInfo[$data[0]] = array('rank' => $data[1], 'menu' => $curMenu);
    }
	
    fclose($handle);
}


$outputString = "";
$index = 1;
foreach($urlInfo as $curURL => $curSiteInfo) {
	
	if ($curSiteInfo['rank'] == 0) {
	
		//Start by setting up the url
		$outputString .= "$index: <a href='http://" . $curURL . "' style='line-height:2'>" . $curURL . "</a><br>\n";
		++$index;
	}
	
}

echo $outputString;
