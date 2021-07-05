PrintHeader <- function(the_string) {
    cat('\n\n\n')

    cat('%%')
    for (i in 1:76) {
        cat('-')
    }
    cat('%%')
    cat('\n')

    m <- floor((76 - nchar(the_string)) / 2)
    n <- ceiling((76 - nchar(the_string)) / 2)
    cat('%%')
    for (i in 1:m) {
        cat(' ')
    }
    cat(the_string)
    for (i in 1:n) {
        cat(' ')
    }
    cat('%%')
    cat('\n')

    cat('%%')
    for (i in 1:76) {
        cat('-')
    }
    cat('%%')
    cat('\n')
}

