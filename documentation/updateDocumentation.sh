cd ..
git pull
doxygen documentation/dox.config
git add --all
git commit -m 'Updating Documentation'
git push

cp -r documentation/html/* /var/www/doxygen/*
