\name{rodps.table.drop}
\alias{RODPS.TABLE.DROP}
\alias{rodps.table.drop}
\title{ Drop Table }
\usage{
rodps.table.drop( full.tablename )
}
\description{
 delete table if exist.
}
\arguments{
\item{ full.tablename }{ tablename, in format 'ProjectName.TableName' or 'TableName'(using current project).}
} 
\author{
\email{yunyuan.zhangyy@alibaba-inc.com}
}
\examples{
## delete 'sales_backup' in current Project
\dontrun{rodps.table.drop('sales_backup')}
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
