<?php

$mapping = array(
    'AdShotRunner\Database\MySQLDatabase' => __DIR__ . '/AdShotRunner/Database/MySQLDatabase.php',
    'AdShotRunner\Menu\MenuGrabber' => __DIR__ . '/AdShotRunner/Menu/MenuGrabber.php',
    'AdShotRunner\Users\User' => __DIR__ . '/AdShotRunner/Users/User.php',
    'AdShotRunner\Utilities\EmailClient' => __DIR__ . '/AdShotRunner/Utilities/EmailClient.php',
    'AdShotRunner\Utilities\MessageQueueClient' => __DIR__ . '/AdShotRunner/Utilities/MessageQueueClient.php',
    'AdShotRunner\Utilities\NotificationClient' => __DIR__ . '/AdShotRunner/Utilities/NotificationClient.php',
    'AdShotRunner\Utilities\WebPageCommunicator' => __DIR__ . '/AdShotRunner/Utilities/WebPageCommunicator.php',
);

spl_autoload_register(function ($class) use ($mapping) {
    if (isset($mapping[$class])) {
        require $mapping[$class];
    }
}, true);

