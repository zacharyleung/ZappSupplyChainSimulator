## exp07/process.R
##
## Read Java files from multiple input folders, and combine them.
## Then convert the output into input for pgfplots.

library(tidyverse)
library(stringr)
library(tools)

source('../CombineFolders.R')
source('../PrintHeader.R')
source('../MarginOfError.R')

# Set input_folders to all folders in the input_parent directory
input_parent = 'process-in'
input_folders = list.dirs(path = input_parent)
input_folders = input_folders[-1]
OUTPUT_FOLDER = 'process-out'

cat(sprintf('input folders = %s\n',
            paste(input_folders, collapse = ', ')))

inputFileExtension = 'csv'
outputFileExtension = 'csv'

# delete all files in the output folder
for (f in list.files(OUTPUT_FOLDER)) {
    theFile = paste(OUTPUT_FOLDER, f, sep = '/')
    cat(sprintf('Deleting %s...\n', theFile))
    file.remove(theFile)
}

min_reps = .Machine$double.xmax
max_rmoe = 0

# for each file in the input folder
for (f in list.files(input_folders[1])) {
    should_process = file_ext(f) == inputFileExtension

    # Don't process files with "delay1"
    #should_process = should_process && !str_detect(f, 'delay1')
    if (should_process) {
        x = CombineFiles(input_folders, f)
        cat('\n\n\n')
        cat(sprintf('%s\n', f))
        print(tbl_df(x))
        
        # convert data from long format to wide format
        # compute the means
        y = spread(x, key = Variable, value = Value)
        print(tbl_df(y))
        
        z = y %>%
            group_by(SupplyDemandRatio) %>%
            summarise(reps = n(),
                      inv_mean = mean(MeanInventoryLevel),
                      inv_rmoe = RelativeMarginOfError(MeanInventoryLevel),
                      serv_mean = mean(ServiceLevel),
                      serv_rmoe = RelativeMarginOfError(ServiceLevel),
                      gini_mean = mean(Gini),
                      gini_rmoe = RelativeMarginOfError(Gini),
                      minsl_mean = mean(MinServiceLevel),
                      minsl_rmoe = RelativeMarginOfError(MinServiceLevel),
                      maxmindiffsl_mean = mean(MaxMinDiffServiceLevel),
                      maxmindiffsl_rmoe = RelativeMarginOfError(MaxMinDiffServiceLevel),
                      wgtdstddev_serv_mean = mean(WeightedStdDevOfServiceLevel),
                      wgtdstddev_serv_rmoe = RelativeMarginOfError(WeightedStdDevOfServiceLevel),
                      stddev_serv_mean = mean(StdDevOfServiceLevel),
                      stddev_serv_rmoe = RelativeMarginOfError(StdDevOfServiceLevel))
        print(tbl_df(z))
        
        min_reps = min(min_reps, z$reps)
        max_rmoe = max(max_rmoe, z$inv_rmoe)
        max_rmoe = max(max_rmoe, z$serv_rmoe)
        max_rmoe = max(max_rmoe, z$stddev_serv_rmoe)
        
        outputFile = paste(OUTPUT_FOLDER, f, sep = '/')
        write.table(z, file = outputFile, sep = ',', quote = FALSE, row.names = FALSE)
    }
}

# Close all sinks
for(i in seq_len(sink.number())){
    sink(NULL)
}


cat(sprintf('number of replications = %d\n', min_reps))
cat(sprintf('max RMOE               = %.6f\n', max_rmoe))
sink(file.path(OUTPUT_FOLDER, 'out.txt'))
cat(sprintf('number of replications = %d\n', min_reps))
cat(sprintf('max RMOE               = %.6f\n', max_rmoe))
sink()


