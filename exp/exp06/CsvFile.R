## Read and write csv files.

ReadCsvFile <- function(file) {
    read.table(file,
               sep = ',', header = TRUE, quote = '',
               stringsAsFactors = FALSE)
}

WriteCsvFile <- function(x, file, col.names = TRUE) {
    write.table(x,
                file,
                col.names = col.names,
                sep = ',', quote = FALSE, row.names = FALSE)
}

WriteMatrix <- function(x, file) {
    write.table(x = x, file = file,
        row.names = FALSE, col.names = FALSE, sep = ',')
}

ReadMatrix <- function(file) {
    A <- read.table(file,
               sep = ',', header = FALSE, quote = '')
    as.matrix(A)
}

