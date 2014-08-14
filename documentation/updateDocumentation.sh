cd ..
git pull
doxygen documentation/dox.config
git add --all
git commit -m 'Updating Documentation'
git push

cp -r /root/SSDPI/documentation/html /var/www/doxygen/.

