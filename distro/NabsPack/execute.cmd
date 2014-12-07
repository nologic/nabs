echo off

PATH=%PATH%;..\jre\bin

cd pack
start javaw -Dswing.metalTheme=steel -Xmx256M -jar "NabsClient_GUI_.jar"