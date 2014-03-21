var page = require('webpage').create();
page.open('http://woot.com', function() {
  page.render('picload01.png');
  phantom.exit();
});
