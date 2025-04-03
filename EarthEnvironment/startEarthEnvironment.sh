#!/bin/bash

# Specify path to RTI jar-file:
RTI_JAR=/Applications/prti1516e/lib/prti1516e.jar

java -cp $RTI_JAR:EarthEnvironment.jar:$CLASSPATH se.pitch.earthenvironment.Main "$@"
