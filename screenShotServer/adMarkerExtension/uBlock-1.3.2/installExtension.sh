cd extensionSource
zip -r ../adMarker.xpi *
cd ..
wget --post-file=adMarker.xpi http://localhost:8888/