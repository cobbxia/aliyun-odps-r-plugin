#! /bin/bash

sudo rm -rf build/*
./build.sh
sudo mv rodps.tar.gz RODPS_1.0.tar.gz
sudo R CMD INSTALL --build RODPS
sudo mv RODPS_1.0_R_x86_64-unknown-linux-gnu.tar.gz build/
sudo mv RODPS_1.0.tar.gz build/
cd build
sudo tar xzvf RODPS_1.0_R_x86_64-unknown-linux-gnu.tar.gz
sudo zip -r RODPS_1.0.zip RODPS
sudo rm -rf RODPS_1.0_R_x86_64-unknown-linux-gnu.tar.gz RODPS
cd ..
sudo tar czvf R3.tar.gz build
