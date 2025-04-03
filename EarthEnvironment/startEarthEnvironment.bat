@ECHO OFF

Set CP=%PRTI1516E_HOME%/lib/prticore.jar;./EarthEnvironment.jar;

java -cp "%CP%" se.pitch.earthenvironment.Main %1 %2 %3

pause