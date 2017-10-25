let madElement = document.getElementById("MAD");
if (madElement) {
	madElement.parentNode.removeChild(madElement);
}

if (document.getElementById('A728')) {
	document.getElementById('A728').style.left = '1px';
	document.getElementById('A728').style.top = '1px';
}