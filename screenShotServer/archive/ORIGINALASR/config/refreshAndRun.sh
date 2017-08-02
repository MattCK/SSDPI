sshpass -p "bypickingthefadedblue" scp -P 44 sshcode@71.194.60.34:screenShotServer/sssEclipseProject/bin/adshotrunner/\{*.class,*.js\} .

java -cp .:../../lib/*:../ "$1"

#java -cp .:../lib/* adshotrunner.AdShotTester
#java -cp .:bin/:lib/* adshotrunner.AdShotTester