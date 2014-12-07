echo off

PATH=.\jre\bin;%PATH%

start javaw -Dswing.metalTheme=steel -Xmx256M -jar "console/NabsClient_GUI_.jar"

exit
