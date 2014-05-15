rm adInjecter.jar
cd adinjecter
javac -encoding ISO-8859-1 -cp "../lib/*" AdInjecter.java
cd ..
jar -cmf manifest2.mf adInjecter.jar adinjecter/AdInjecter.class
rm adinjecter/*.class
