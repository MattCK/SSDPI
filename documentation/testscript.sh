cd ..
git pull

phpdoc -d frontend -t documentation/php
cp -r documentation/php/. /var/www/doc/php



git add --all
git commit -m 'Updating Documentation'
git push
