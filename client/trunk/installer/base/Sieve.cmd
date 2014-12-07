echo off

PATH=.\jre\bin;%PATH%

java -server -Xmx256M -jar "sieve/NabsReceptor.jar"
pause