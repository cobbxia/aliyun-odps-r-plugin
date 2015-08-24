# Guide for User
This page is used to give you first sense of what is RODPS , and also show you how to install and use it. If you want to modify the source code to make it better , please click [DeveloperGuide](https://github.com/yaoweidong/aliyun-odps-r-plugin/blob/master/DeveloperGuide.md) for help.


### Details
	
	RODPS is a standard R package to interact with ODPS within R.
	- Support Window/Linux/Mac.
	- Load data from ODPS or write R data(dataframe) into ODPS system.
	- Convert some of the R models to SQL command.
	- The large data set can be processed by using the distributed algorithm.
	- The small data set can be processed directly in R.
### Download ROdps package
- access ROdps gitlab page [http://gitlab.alibaba-inc.com/aliopensource/aliyun-odps-r-plugin/tree/zhuying/build](http://gitlab.alibaba-inc.com/aliopensource/aliyun-odps-r-plugin/tree/zhuying/build)
- download `RODPS_xxx.tar.gz` for Linux/Mac , `RODPS_xxx.zip` for Windows

### How to install and load
- create `odps_config.ini` and configure it

```shell
project_name=my_project
access_id=*******************
access_key=*******************
end_point=http://service.odps.aliyun-inc.com/api
dt_end_point=http://dt.odps.aliyun-inc.com/

#access from internal network
end_point=http://service-corp.odps.aliyun-inc.com/api
dt_end_point=http://dt-corp.odps.aliyun-inc.com/

#set path for log4j.properties,or don't configure for using our default setting
log4j_properties=
```
- set `odps_config` path as environment variable `RODPS_CONFIG` or manually init in R after load RODPS using `rodps.init('/AbsolutePath/odps_config.ini')`
- start RStudio or other R client
- run `Sys.getenv('RODPS_CONFIG')` to check whether `RODPS_CONFIG` is correctly set
- install rJava and RSQLite `install.packages(c('rJava','DBI','RSQLite'))`
- install the RODPS_xxx.xxx through RStudio toolbar, or use the command `install.packages("/AbsolutePath/RODPS_xxx.xxx",repos = NULL, type="source")`
- `library('RODPS')` to load this R package

### How to get help
- Run `help('RODPS')` and will show the index of help document
- To get the description of the function `rodps.xxx.xxx` , run `help('rodps.xxx.xxx')`

```shell
#help document about how to use sql command
>help('rodps.sql')
#help document about how to operate project
>help('rodps.project')
#help document about how to operate table
>help('rodps.table')
```

### Example
#### sql
	#submmit sql command
	rodps.sql('list tables')

#### table list
	#show the table list in current project
	>rodps.table.list()

#### table read
	#read data from ODPS and store in R
	>tbl1 <- rodps.table.read("tbl1")
	>d <- head(tbl1)

#### table write
	#write dataframe into ODPS
	#modify the columns name of "iris" to store in ODPS system
	>names(iris) <- gsub("\\.","_",names(iris))
	
	#write "iris" data into ODPS
	>rodps.table.write(iris,'iris')

#### table sample
	#random sampling data from table by raws or probability
	
	#sample by raws
	>rodps.table.sample.srs('tbl1','small_tbl1', 100 )

	#sample by probability
	>rodps.table.sample.srs('tbl1','small_tbl1',0.1 )

#### table hist
	#draw histogram of the table in ODPS
	rodps.table.hist(tblname='iris',colname='species',col=rainbow(10),main="Hist of species in IRIS dataset",freq=F)

#### modle
	#Modeling TEST with the data set "iris"	
	#Build a classification tree model
	>library(rpart)
	>fit <- rpart(Species~., data=iris)
	
	#Converting the model to SQL command and execute
	>rodps.predict.rpart(fit, srctbl='iris',tgttbl='iris_p')
	
	#Check the target table
	>d <- rodps.table.read('iris_p')
 	

### Authors && Contributors

- [Yang Hongbo](https://github.com/hongbosoftware)
- [Yao Weidong](https://github.com/yaoweidong)

### License

licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)
