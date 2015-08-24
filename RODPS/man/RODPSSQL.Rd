\name{rodps.sql}
\alias{RODPS.SQL}
\alias{rodps.sql}
\title{ Sql Command }
\usage{
rodps.sql( sql )
}
\description{
  run SQL command and return result(in data.frame type).
}
\arguments{
\item{ sql }{ sql command，ex. select/insert/etc.  }
} 
\author{
\email{yunyuan.zhangyy@alibaba-inc.com}
}
\examples{
## select the data of 'sales' in January ,and store the result in data.frame
\dontrun{ data <- rodps.sql('select * from sales where month=1')}
}
\seealso{
 \code{\link{RODPS}},
 \code{\link{rodps.table}},
 \code{\link{rodps.project}}
}
\keyword{programming}
