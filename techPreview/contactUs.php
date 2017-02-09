<?PHP
/**
* Displays campaign results
*
* @package AdShotRunner
*/
/**
* File to define paths, setup dependencies, and connect to database
*/
require_once('systemSetup.php');

?>

<?PHP include_once(BASEPATH . "header.php");?>

<body>
	
<div id="header">
	<div id="title">
		<h1>AdShotRunner&trade;: Tech Preview</h1> 
	</div>
</div>

<div id="mainContent">

	<h2>Contact Us</h2>
	<?PHP include("contactForm.php");?>
</div>

</body>
</html>

<script>

//Reset the contact form in case the user pressed the refresh button
contactForm.reset();

</script>
