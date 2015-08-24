# Guide for Developer
This page is used to show you how to download RODPS source code,and how to build for your own using.
If you just want to use ROdps package, click [UserGuide](https://github.com/yaoweidong/aliyun-odps-r-plugin/blob/master/UserGuide.md) for detailed help.

### Requirements

#### Compile and Run Environment
- Java 6+
- R 1.80+
- Maven

#### Library Dependence
- rJava
- DBI
- RSQLite

### Code Tree
```shell
.
|-- CHANGELOG.md
|-- License
|-- README.md
|-- RODPS
|   |-- DESCRIPTION
|   |-- NAMESPACE
|   |-- R
|   |   |-- init.R
|   |   |-- odps_advanced_function__naomit.R
|   |   |-- odps_advanced_function_head.R
|   |   |-- odps_advanced_function_hist.R
|   |   |-- odps_advanced_function_predict.R
|   |   |-- odps_advanced_function_str.R
|   |   |-- odps_advanced_function_summary.R
|   |   |-- odps_base_function.R
|   |   `-- odps_data_structure.R
|   |-- inst
|   |   `-- lib
|   `-- man
|       |-- RODPS.Rd
|       |-- RODPSPROJECT.Rd
|       |-- RODPSPROJECTCURRENT.Rd
|       |-- RODPSPROJECTUSE.Rd
|       |-- RODPSSQL.Rd
|       |-- RODPSTABLE.Rd
|       |-- RODPSTABLEDESC.Rd
|       |-- RODPSTABLEDROP.Rd
|       |-- RODPSTABLEEXIST.Rd
|       |-- RODPSTABLELIST.Rd
|       |-- RODPSTABLEPART.Rd
|       |-- RODPSTABLEREAD.Rd
|       |-- RODPSTABLEROWS.Rd
|       |-- RODPSTABLESIZE.Rd
|       `-- RODPSTABLEWRITE.Rd
|-- Rplots.pdf
|-- build
|   |-- RODPS_1.0.tar.gz
|   `-- RODPS_1.0.zip
|-- build.sh
|-- buildall.sh
|-- java
|   |-- check_style.xml
|   |-- eclipse-java-google-style.xml
|   |-- intellij-java-google-style.xml
|   |-- lib
|   |   `-- odpsconsole.jar
|   |-- pom.xml
|   |-- src
|   |   |-- main
|   |   `-- test
|   `-- target
`-- rodps_ut
    |-- rodpstest.R
    `-- testout
        `-- readme.txt

```
### Build
- Run `git clone git@gitlab.alibaba-inc.com:aliopensource/aliyun-openapi-java-sdk.git` to clone code from remote 
- `cd aliyun-odps-r-plugin`
- `./buildall.sh`
- Genetate `/build/RODPS_xxx.tar.gz` for Linux/Mac , `/build/RODPS_xxx.zip` for Windows


### Run UnitTest

#### Java UnitTest
- `cd aliyun-odps-r-plugin.git/java/src/test/resources` , configure the `odps_config.ini` 

```shell
project_name=my_project
access_id=*******************
access_key=*******************
end_point=http://service.odps.aliyun-inc.com/api
dt_end_point=http://dt.odps.aliyun-inc.com/

#access from internal network
end_point=http://service-corp.odps.aliyun-inc.com/api
dt_end_point=http://dt-corp.odps.aliyun-inc.com/

#sqlite temp path, default is /home/rodps_temp/
sqlite_temp=*******
```
- `cd aliyun-odps-r-plugin.git/java`
- Run `sudo mvn test`

#### R UnitTest
- set the folder `rodps_ut` as working directory through RStudio toolbar,or use the command `setwd("/AbsolutePath/rodps_ut")`
- `source("rodpstest.R",echo = TRUE)`,and the output can be found in `/testout` folder
