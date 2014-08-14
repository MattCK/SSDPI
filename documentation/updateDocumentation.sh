cd ..
git pull
doxygen documentation/dox.config
git add --all
git commit -m 'Updating Documentation'
git push

cp -r html/* /var/www/doxygen/*
