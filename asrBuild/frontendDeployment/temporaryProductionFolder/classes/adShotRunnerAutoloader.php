<?php

$mapping = array(
    'AdShotRunner\Database\ASRDatabase' => __DIR__ . '/AdShotRunner/Database/ASRDatabase.php',
    'AdShotRunner\DFP\DFPCommunicator' => __DIR__ . '/AdShotRunner/DFP/DFPCommunicator.php',
    'AdShotRunner\Menu\MenuGrabber' => __DIR__ . '/AdShotRunner/Menu/MenuGrabber.php',
    'AdShotRunner\PhantomJS\PhantomJSCommunicator' => __DIR__ . '/AdShotRunner/PhantomJS/PhantomJSCommunicator.php',
    'AdShotRunner\PowerPoint\PowerPointBackground' => __DIR__ . '/AdShotRunner/PowerPoint/PowerPointBackground.php',
    'AdShotRunner\System\ASRProperties' => __DIR__ . '/AdShotRunner/System/ASRProperties.php',
    'AdShotRunner\Users\User' => __DIR__ . '/AdShotRunner/Users/User.php',
    'AdShotRunner\Utilities\EmailClient' => __DIR__ . '/AdShotRunner/Utilities/EmailClient.php',
    'AdShotRunner\Utilities\FileStorageClient' => __DIR__ . '/AdShotRunner/Utilities/FileStorageClient.php',
    'AdShotRunner\Utilities\MessageQueueClient' => __DIR__ . '/AdShotRunner/Utilities/MessageQueueClient.php',
    'AdShotRunner\Utilities\NotificationClient' => __DIR__ . '/AdShotRunner/Utilities/NotificationClient.php',
    'AdShotRunner\Utilities\WebPageCommunicator' => __DIR__ . '/AdShotRunner/Utilities/WebPageCommunicator.php',
);

spl_autoload_register(function ($class) use ($mapping) {
    if (isset($mapping[$class])) {
        require $mapping[$class];
    }
}, true);

