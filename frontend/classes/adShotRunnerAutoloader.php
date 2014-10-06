<?php

$mapping = array(
    'AdShotRunner\Database\MySQLDatabase' => __DIR__ . '/AdShotRunner/Database/MySQLDatabase.php',
    'AdShotRunner\Menu\MenuGrabber' => __DIR__ . '/AdShotRunner/Menu/MenuGrabber.php',
    'AdShotRunner\Utilities\WebPageCommunicator' => __DIR__ . '/AdShotRunner/Utilities/WebPageCommunicator.php',
);

spl_autoload_register(function ($class) use ($mapping) {
    if (isset($mapping[$class])) {
        require $mapping[$class];
    }
}, true);

