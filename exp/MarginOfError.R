## Calculate the relative margin of error, which is defined as the
## half-width of the 95% confidence interval, divided by the mean
## estimate.

RelativeMarginOfError = function(x) {
    if (mean(x) < 0.01) {
        return(0)
    }
    
    half_width = 1.96 * sd(x) / sqrt(length(x))
    return(half_width / mean(x))
}


AbsoluteMarginOfError = function(x) {
    half_width = 1.96 * sd(x) / sqrt(length(x))
    return(half_width)
}


