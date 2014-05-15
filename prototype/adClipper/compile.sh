rm adClipper.jar
cd adclipper
javac -encoding ISO-8859-1 -cp "../lib/*" AdClipper.java
cd ..
jar -cmf manifest2.mf adClipper.jar adclipper/AdClipper.class
rm adclipper/*.class
