<?PHP

/**
* File to setup all the path definitions
*/
require_once('../systemSetup.php');

use AdShotRunner\Utilities\EmailClient;
use AdShotRunner\Utilities\MessageQueueClient;
use AdShotRunner\Utilities\NotificationClient;


$count = 1;
while (true) {
	echo "Sending message $count...";
	$response = MessageQueueClient::sendMessage(MessageQueueClient::TAGIMAGEREQUESTS, "Tag image request - $count");
	echo "Done!\n";
	++$count;
	sleep(5);
}