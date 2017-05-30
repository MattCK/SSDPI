<?PHP
/**
* Test for Client class
*
* @package AdShotRunner
* @subpackage Tests
*/
/**
* File to setup all the path definitions
*/
require_once('../systemSetup.php');

use AdShotRunner\Clients\Client;

header("Content-Type: text/plain");

echo "Get client by ID(1): \n\n";

$client = Client::getClient(1);

print_r($client);

echo "Get client by account number(12345678): \n\n";

$client = Client::getClientByAccountNumber("12345678");

print_r($client);