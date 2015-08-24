#! /bin/bash

d=`date +%Y%m%d" "%H:%M:%S`
basepath=$(cd "$(dirname "$0")";pwd)
javasrc=$basepath/java
jarpath=$basepath/java/target
libpath=$basepath/RODPS/inst/lib
RPackageName="rodps.tar.gz"
Log4jPath=$basepath/java/src/main/java
RODPSPath=$basepath/RODPS


echo "Start build R package in $basepath at $d"

#mvn package
cd $javasrc
mvn clean
mvn package -DskipTests
cd $basepath

#copy rodps.jar&log4j.properties to /RODPS/inst/java
sudo cp $jarpath/*.jar $libpath
sudo cp $Log4jPath/log4j.properties $libpath

#tar R odps package
sudo tar zcvf $RPackageName RODPS


echo "Generate $RPackageName $basepath at $d"
