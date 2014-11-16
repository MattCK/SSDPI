<?PHP 

error_reporting(E_ERROR | E_PARSE);

require_once('../systemSetup.php');

use AdShotRunner\Menu\MenuGrabber;

header("Content-Type: text/plain");

$url = ($_GET['url']) ? $_GET['url'] : 'boston.com';

$menuGrabber = new MenuGrabber();
$domains = $menuGrabber->getDomainMenus([$url]);

print_r(array_shift($domains[$url])['items'][1]['url']);