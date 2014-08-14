cd ..
git pull
phpdoc -d frontend -t documentation/phpdoc
git add --all
git commit -m 'Updating Documentation'
git push

cp -r /root/SSDPI/documentation/phpdoc /var/www/phpdoc/.

