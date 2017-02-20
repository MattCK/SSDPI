<?PHP
	
	//A timestamp is placed after the ASR resources to force a refresh each page load.
	//Since the resources are so small, this has a trivial effect on page load.
	//It allows us to make live changes to the system without the possibility of an error
	//changing a "version" field.
	$timestamp = time();
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<title>AdShotRunner&trade;: Tech Preview</title>
	<!--link rel="shortcut icon" href="favicon.ico" -->
	<link href="<?PHP echo CSSURL;?>jquery-ui.min.css" rel="stylesheet" type="text/css">
	<link href="<?PHP echo CSSURL;?>jquery-ui.structure.min.css" rel="stylesheet" type="text/css">
	<link href="<?PHP echo CSSURL;?>jquery-ui.min.css" rel="stylesheet" type="text/css">
	<link href="<?PHP echo CSSURL;?>theme.css" rel="stylesheet" type="text/css">
	<link href="<?PHP echo CSSURL;?>tabs.css" rel="stylesheet" type="text/css">
	<link href="<?PHP echo CSSURL;?>spectrum.css" rel="stylesheet" type="text/css">
	<link href="<?PHP echo CSSURL;?>adshotrunner.css<?PHP echo "?" . $timestamp?>" rel="stylesheet" type="text/css">
	<script src="<?PHP echo JAVASCRIPTURL;?>jquery-2.1.4.min.js" language="javascript" type="text/javascript"></script>
	<script src="<?PHP echo JAVASCRIPTURL;?>jquery-ui-1.11.4.min.js" language="javascript" type="text/javascript"></script>	
	<script src="<?PHP echo JAVASCRIPTURL;?>spectrum.js" language="javascript" type="text/javascript"></script>	
	<script src="<?PHP echo JAVASCRIPTURL;?>base.js<?PHP echo "?" . $timestamp?>" language="javascript" type="text/javascript"></script>	
	<script src="<?PHP echo JAVASCRIPTURL;?>zip.js" language="javascript" type="text/javascript"></script>	
	<script src="<?PHP echo JAVASCRIPTURL;?>asr.js<?PHP echo "?" . $timestamp?>" language="javascript" type="text/javascript"></script>	
	<script src="<?PHP echo JAVASCRIPTURL;?>tagParser.js<?PHP echo "?" . $timestamp?>" language="javascript" type="text/javascript"></script>
	<script src="<?PHP echo JAVASCRIPTURL;?>campaign.js<?PHP echo "?" . $timestamp?>" language="javascript" type="text/javascript"></script>
	<script src="<?PHP echo JAVASCRIPTURL;?>tooltips.js<?PHP echo "?" . $timestamp?>" language="javascript" type="text/javascript"></script>
	<script src="<?PHP echo JAVASCRIPTURL;?>contactForm.js<?PHP echo "?" . $timestamp?>" language="javascript" type="text/javascript"></script>
	<script>zip.workerScriptsPath = "<?PHP echo JAVASCRIPTURL;?>";</script>

	<script>

		//Load up the error image checkbox. If there is a connection error and the error message box appears,
		//it will be missing the checkbox "x" since it cannot otherwise load the image file.
		let tagImage = new Image(); tagImage.src = "https://development.adshotrunner.com/css/images/ui-icons_454545_256x240.png";

	</script>
</head>

