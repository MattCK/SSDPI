#!/bin/sh
# SCRIPT TO CREATE FRONTEND --PRODUCTION-- DEPLOYMENT ZIP

#Set the temporary build folder name
BUILD_FOLDER=temporaryProductionFolder

#Set the paths to the source folders
FRONTEND_FOLDER='../../frontend'
TECHPREVIEW_FOLDER='../../techPreview'

#Get the date and time. Both are used to name the final file.
DATE=`date +%Y-%m-%d`
TIME=`date +%H%M%S`
ZIP_FILENAME=productionDeployment-${DATE}-${TIME}.zip

#Name the temporary javascript file holding all uncompiled ASR javascript libraries
JS_FILENAME=uncompiledLibraries-${DATE}-${TIME}.js

#Name the properties files
PRODUCTION_PROPERTIES_FILE=asrProperties.ini.production
DEVELOPMENT_PROPERTIES_FILE=asrProperties.ini.development

#Output what is going to occur
echo --------- Create Frontend PRODUCTION Deployment Zip ---------
echo 
echo You are creating the ElasticBeanstalk deployment zip file
echo for the PRODUCTION environment.
echo
echo Final Filename: $ZIP_FILENAME
echo
echo ASR Properties: \'asrProperties.ini.production\' will 
echo '                 'be copied to \'asrProperties.ini\'
echo
echo Did you change asr.js so campaign results open in the same tab \(NOT a new tab\)\?
echo 

#Require a prompt to confirm moving forward
printf "Type 'soundsgood' to continue: "
read PROMPT_ANSWER
if [ "${PROMPT_ANSWER}" = soundsgood ]; then

	echo #extra space

	#Make the temporary build folder
	echo Making build folder...
	mkdir $BUILD_FOLDER

	#Copy the frontend and techpreview files into the temporary build folder
	echo Copying files...
	cp -a ${FRONTEND_FOLDER}/. ${BUILD_FOLDER}
	cp -a ${TECHPREVIEW_FOLDER}/. ${BUILD_FOLDER}

	#Copy the production properties to asrProperties.ini and delete the rest
	echo Setting properties file. Using: ${PRODUCTION_PROPERTIES_FILE}
	cp ${BUILD_FOLDER}/restricted/${PRODUCTION_PROPERTIES_FILE} ${BUILD_FOLDER}/restricted/asrProperties.ini
	rm ${BUILD_FOLDER}/restricted/${PRODUCTION_PROPERTIES_FILE}
	rm ${BUILD_FOLDER}/restricted/${DEVELOPMENT_PROPERTIES_FILE}

	#remove the development and testing tools
	#rm -rf ${BUILD_FOLDER}/tools

	#Place all uncompiled JS libraries in one file, compile it to the build folder, and remove the uncompiled file
	echo Compiling Javascript...
	php ${BUILD_FOLDER}/javascript/asrJavascript.php > ${JS_FILENAME}
	java -jar closure-compiler.jar -W QUIET --js ${JS_FILENAME} --js_output_file ${BUILD_FOLDER}/javascript/asrJavascript.php
	rm ${JS_FILENAME}

	#Place all the files in a zip
	echo Compressing files into zip. Filename: $ZIP_FILENAME
	cd ${BUILD_FOLDER}
	zip ${ZIP_FILENAME} -qr .
	mv ${ZIP_FILENAME} ../.
	cd ../

	#Remove the temporary folder
	echo Removing build folder...
	rm -rf ${BUILD_FOLDER}

	#State the file
	echo
	echo Done! 
	echo PRODUCTION Deployment File: $ZIP_FILENAME
	echo

#If they didn't type the correct prompt, end the build
else 
	echo
	echo Build cancelled!
	echo
fi
