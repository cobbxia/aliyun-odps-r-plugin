# R-plugin to ODPS
 RODPS is an R extension to enable R to interact with ODPS system, also support other related algorithm packages.

## Details
	
	RODPS is a standard R package to interact with ODPS within R.
	- Support Window/Linux/Mac.
	- Load data from ODPS or write R data(dataframe) into ODPS system.
	- Convert some of the R models to SQL command.
	- The large data set can be processed by using the distributed algorithm.
	- The small data set can be processed directly in R.

Please make sure the environment variable RODPS_CONFIG is set, it's in the same format as used in odpscmd, this file is required when connecting to ODPS.

## Requirements & Guide

###  Run Dependence
- Java 6+
- R 1.80+

### Library Dependence
- rJava
- DBI
- RSQLite

### Guide
If you just want to use ROdps package, click [UserGuide](https://github.com/yaoweidong/aliyun-odps-r-plugin/blob/master/UserGuide.md) for detailed help.

If you want to modify the source code to make it better , please click [DeveloperGuide](https://github.com/yaoweidong/aliyun-odps-r-plugin/blob/master/DeveloperGuide.md) for help.
 	

## Authors && Contributors

- [Yao Weidong](https://github.com/yaoweidong)
- [Yang Hongbo](https://github.com/hongbosoftware)


## License

licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)
