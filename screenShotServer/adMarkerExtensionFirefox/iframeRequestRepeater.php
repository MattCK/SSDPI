
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<title>IFrame Request Repeater</title>
	<script
		src="https://code.jquery.com/jquery-3.0.0.min.js"
		integrity="sha256-JmvOoLtYsmqlsWxa7mDSLMwa6dZ9rrIdtrrVYRnDRH0="
		crossorigin="anonymous">
	</script>
</head>

<script>


function makeRequest() {
	$.get( "iframeImage.jpg" );

	setTimeout(function() {
		makeRequest();
	}, 3000);
}

makeRequest();

</script>

<body>

Makes a GET request every 3 seconds

</body>