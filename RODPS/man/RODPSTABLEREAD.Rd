\name{rodps.table.load}
\alias{RODPS.TABLE.LOAD}
\alias{rodps.table.load}
\title{ Read Table }
\usage{
rodps.table.load( full.tablename,limit=-1,memsize=10737518240, colsep="\t", rowsep="\n")
}
\description{
 read data from ODPS and store in R.
}
\arguments{
\item{ full.tablename }{ table name , in format 'ProjectName.TableName',or 'TableName'(using current project) }.
\item{ limit }{ limit the rows to read,'-1' for not limit }
\item{ memsize }{ Maximum data capacity }
\item{ colsep }{ Separator of Column,default is "\\t" }
\item{ colsep }{ Separator of Row,default is "\\n" }
} 
\author{
\email{yunyuan.zhangyy@alibaba-inc.com}
}
\examples{
## load data from ‘sales’ in first 100 rows
\dontrun{ x<-rodps.table.load('sales',limit=100) }
}
\seealso{
 \code{\link{rodps.table.desc}},
 \code{\link{rodps.table.drop}},
 \code{\link{rodps.table.exist}},
 \code{\link{rodps.table.partitions}},
 \code{\link{rodps.table.list}},
 \code{\link{rodps.table.rows}},
 \code{\link{rodps.table.size}},
 \code{\link{rodps.table.read}},
 \code{\link{rodps.table.wrtie}}
}
\keyword{programming}
