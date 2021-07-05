## exp06/compute.R
##
## Given the long form trace of multiple replications of each pack
## size, compute the mean number of Q4 stock-out days for a given set
## of facilities.
##
## Input files:
##  - Set of cross-docking facilities for which we have Q4 stock-out
##    results
##  - Long form trace from simulator in the files
##    <pack size> / <replication number>.csv
##
## To make the health facility names match, convert to lower case.
##
## Running time:
## |--------------------|--------------|--------------|
## |                      Replications   Running Time |
## |--------------------|--------------|--------------|
## | Dell Inspiron 3551 |           10 |         0:00 |
## | Dell Optiplex 2020 |           10 |         0:10 |
## | Dell Optiplex 2020 |          100 |         1:30 |
## | Dell Optiplex 2020 |         1000 |   30 minutes |
## |--------------------|--------------|--------------|

library(plyr)
library(tidyverse)
library(pracma)
library(binom)
library(reshape2)

source('CsvFile.R')

sink('temp.log')
tic()

## All health facilities
all_facilities_file <-'../../input/zambia/health-facilities.csv'
istock_facilities_file <-
    'facilities-istock-sss-scs.csv'
xdock_facilities_file <-
    'facilities-xdock-sss-scs.csv'

START_PERIOD <- 36
END_PERIOD <- 48
EPSILON <- 1e-6    # To avoid 0 / 0 errors, do x / (y + EPSILON)
IS_DEBUG_MODE <- FALSE
NUMBER_OF_REPLICATIONS <- 2000

## For all health facilities
#facility_set <- temp$Health.Facility

## For debugging purposes
#facility_set <- c('buli', 'chibale', 'lubushi')



# Read a trace file and compute the mean stockout days for a set of
# facilities.
ComputeMeanStockoutDays <- function(traceFile, facility_set) {
    temp <- read.table(traceFile,
                       header = TRUE, sep = ',', stringsAsFactor = FALSE)
    
    ## Convert facility names to lower case
    temp$Facility <- tolower(temp$Facility)
    facility_set <- tolower(facility_set)
    
    if (IS_DEBUG_MODE) {
        print(str(temp))
        cat('Facilities in the facility set\n')
        print(facility_set[facility_set %in% temp$Facility])
        cat('Facilities not in the facility set\n')
        print(facility_set[!(facility_set %in% temp$Facility)])
    }
    
    temp <- subset(temp, Period >= START_PERIOD & Period < END_PERIOD)
    temp <- subset(temp, Facility %in% facility_set)
    temp <- subset(temp, Variable %in% c('UnmetDemand', 'Demand'))
    
    ## Convert table into wide format
    temp <- dcast(temp, Facility + Period ~ Variable, value.var = 'Value')
    
    temp$stockoutDays <- with(temp, 365 / 48 * UnmetDemand / (Demand + EPSILON))
    
    ## Compute the number of days stocked out in Q4
    temp <- ddply(temp, .(Facility), summarize,
                  stockoutDaysQ4 = sum(stockoutDays))
    
    meanStockoutDays <- mean(temp$stockoutDaysQ4)
    i <- which.max(meanStockoutDays)
    x <- data.frame(
        replication = basename(traceFile),
        facilities = nrow(temp),
        meanStockoutDays = meanStockoutDays,
        maxStockoutFacility = temp$Facility[i],
        maxStockoutDays = max(meanStockoutDays))
    
    i <- as.numeric(gsub('.{3}$', '', basename(traceFile)))
    if (i %% 1000 == 0) {
        print(Sys.time())
        print(traceFile)
    }
    return(x)
}



MasterComputeMean <- function(theDirectory, facility_set) {
    TempReadFile <- function(theFile) {
        theFile <- file.path(theDirectory, theFile)
        ComputeMeanStockoutDays(theFile, facility_set)
    }
    
    the_files <- list.files(theDirectory, pattern = '*.csv')
    n <- min(length(the_files), NUMBER_OF_REPLICATIONS)
    the_files <- head(the_files, n)
    the_x <- lapply(as.list(the_files), TempReadFile)
    return(do.call(rbind, the_x))
}








# Level 1: compute the mean stockout days for each size and replication
# of this supply chain configuration type
PackRepMean <- function(type_name, facility_set_file) {
    x <- read.table(facility_set_file,
                    header = TRUE, sep = ',', stringsAsFactor = FALSE)
    facility_set <- as.character(x$facility_scs)
    facility_set <- facility_set[facility_set != '']
    print(facility_set)
    
    the_lapply <- lapply(as.list(c(6, 12, 18, 24)), function(pack_size) {
        theDirectory <- sprintf('%s-%02d', type_name, pack_size)
        x <- MasterComputeMean(theDirectory, facility_set)
        x$pack_size <- pack_size
        return(x)
    })
    #do.call(rbind, the_lapply) %>% print
    y <- do.call(rbind, the_lapply) %>%
        mutate(type = type_name)
    return(y)
}


## The cross-docking facilities for which we have Q4 stock-out results

x <- PackRepMean(type_name = 'istock',
                 facility_set_file = istock_facilities_file)
y <- PackRepMean(type_name = 'xdock',
                 facility_set_file = xdock_facilities_file)
pack_rep_mean <- rbind(x, y)


stats <- ddply(pack_rep_mean, .(type, pack_size), summarize,
               replications = length(pack_size),
               mean = mean(meanStockoutDays),
               min = min(meanStockoutDays),
               max = max(meanStockoutDays),
               perc05 = quantile(meanStockoutDays, probs = 0.05),
               perc50 = quantile(meanStockoutDays, probs = 0.50),
               perc95 = quantile(meanStockoutDays, probs = 0.95))

print(stats)

write.table(stats, file = 'xdock.csv',
            quote = FALSE, row.names = FALSE, sep = ',')

cat('Print percentiles\n')
ComputePercentile <- function(the_type, the_pack_size, the_mean) {
    x <- pack_rep_mean %>%
        filter(type == the_type) %>%
        filter(pack_size == the_pack_size)
    the_percentile <- ecdf(x$meanStockoutDays)(the_mean)
    cat(sprintf('The percentile for pack size %02d and mean %.2f is %.2f\n',
                the_pack_size, the_mean, the_percentile))
    cat(sprintf('Number of facilities = %d\n', median(x$facilities)))
    trials <- nrow(x)
    successes <- nrow(x %>% filter(meanStockoutDays <= the_mean))
    z <- binom.confint(successes, trials, method = 'wilson')
    return(data.frame(type = the_type,
                      pack_size = the_pack_size,
                      reps = trials,
                      actual_mean = the_mean,
                      sim_mean = mean(x$meanStockoutDays),
                      sim_min = min(x$meanStockoutDays),
                      sim_max = max(x$meanStockoutDays),
                      percentile = the_percentile,
                      lower_wilson = z$lower, upper_wilson = z$upper))
}


x1 <- ComputePercentile('istock', 6, 12.62)
x2 <- ComputePercentile('istock', 12, 4.85)
x3 <- ComputePercentile('istock', 18, 7.56)
x4 <- ComputePercentile('istock', 24, 7.85)
x5 <- ComputePercentile('xdock', 6, 4.18)
x6 <- ComputePercentile('xdock', 12, 0.00)
x7 <- ComputePercentile('xdock', 18, 1.39)
x8 <- ComputePercentile('xdock', 24, 1.80)
x <- rbind(x1, x2, x3, x4, x5, x6, x7, x8)
x %>% select(type, pack_size, reps, percentile) %>% print

x <- x %>%
    mutate(sim_min = round(sim_min, 2)) %>%
    mutate(sim_max = round(sim_max, 2)) %>%
    mutate(sim_mean	= round(sim_mean,2)) %>%
    mutate(percentile = round(percentile, 4)) %>%
    mutate(lower_wilson = round(lower_wilson, 4)) %>%
    mutate(upper_wilson = round(upper_wilson, 4)) %>%
    select(type, pack_size, reps, sim_mean, actual_mean,
           sim_min, sim_max, everything())

print(x)
WriteCsvFile(x, file = 'compute.csv')

toc()
sink()

writeLines(readLines('temp.log'))
