## Exp10 -> process.R
## Convert the output from java into input for pgfplots.
##
## The parameter is_excluded determines whether the facility
## "Mphyanakunda" is excluded from the calculation of the 95% CIs
## for the service level and the total lead time.

library(tidyverse)
library(tools)

source('../CombineFolders.R')
source('../MarginOfError.R')
source('../PrintHeader.R')

# Construct a mapping from facility to delivery group
x = read_csv('../../input/zambia/facility-names.csv')
y = read_csv('../../input/zambia/replenishment.csv')
the_map = inner_join(x, y, by = 'district') %>%
    select(facility, delivery_group)
print(the_map)

# Set input_folders to all folders in the input_parent directory
input_parent = 'process-in'
input_folders = list.dirs(path = input_parent)
input_folders = input_folders[-1]

OUTPUT_FOLDER = 'process-out'

inputFileExtension = 'csv'
outputFileExtension = 'csv'

max_sl_rmoe  = 0
max_tlt_rmoe = 0
max_sl_amoe  = 0
max_tlt_amoe = 0
min_reps = .Machine$double.xmax

# Should the facility "Mphyanakunda" be excluded?
is_excluded = TRUE

is.nan.data.frame = function(x) {
    do.call(cbind, lapply(x, is.nan))
}

# delete all files in the output folder
for (f in list.files(OUTPUT_FOLDER)) {
    theFile = paste(OUTPUT_FOLDER, f, sep = '/')
    cat(sprintf('Deleting %s...\n', theFile))
    file.remove(theFile)
}

# for each file in the input folder
for (f in list.files(input_folders[1])) {
    print(f)
    if (file_ext(f) == inputFileExtension) {
        x = CombineFiles(input_folders, f)
        print(tbl_df(x))
        
        # Delete the facility "Mphyanakunda"
        if (is_excluded) {
            x = x %>% filter(facility != 'mphyanakunda')
        }
        
        # Replace lead times which are NaN by 48
        x[is.nan(x)] = 48
        
        y = x %>%
            group_by(facility) %>%
            summarise(reps = n(),
                      service_level_mean = mean(service_level, na.rm = TRUE),
                      service_level_amoe = 
                          AbsoluteMarginOfError(service_level),
                      service_level_rmoe = 
                          RelativeMarginOfError(service_level),
                      secondary_lead_time_mean = mean(secondary_lead_time),
                      secondary_lead_time_rmoe = 
                          RelativeMarginOfError(secondary_lead_time),
                      total_lead_time_mean = mean(total_lead_time),
                      total_lead_time_amoe = 
                          AbsoluteMarginOfError(total_lead_time),
                      total_lead_time_rmoe = 
                          RelativeMarginOfError(total_lead_time))
        
        # Delete the field "Facility"
        #y = y %>% select(-Facility)
        
        max_sl_rmoe  = max(max_sl_rmoe, y$service_level_rmoe)
        max_tlt_rmoe = max(max_tlt_rmoe, y$total_lead_time_rmoe)
        max_sl_amoe  = max(max_sl_amoe, y$service_level_amoe)
        max_tlt_amoe = max(max_tlt_amoe, y$total_lead_time_amoe)
        min_reps     = min(min_reps, y$reps)
        
        # Add the field "delivery group"
        y = inner_join(y, the_map, by = 'facility')

        # Add the field "weekly_demand_cv"
        temp = read_csv('weekly_demand_cv.csv')
        y = inner_join(y, temp, by = 'facility')

        # Add 1 to the "delivery_group" column
        y = y %>% mutate(delivery_group = 1 + delivery_group)
        # Change delivery group 4 to delivery group 3
        y = y %>% mutate(delivery_group =
            ifelse(delivery_group == 4, 3, delivery_group))

        print(summary(y))
        
        #cat(sprintf('max service level error = %.6f\n', max_sl_halfwidth))
        #cat(sprintf('max total lead time error = %.6f\n', max_tlt_halfwidth))
        print(tbl_df(y))
        outputFile = paste(OUTPUT_FOLDER, f, sep = '/')
        write.table(y, file = outputFile, sep = ',', quote = FALSE, row.names = FALSE)
    }
}

cat(sprintf('the facility "Mphyanakunda" is excluded = %s\n', is_excluded))
cat(sprintf('number of replications                  = %d\n', min_reps))
cat(sprintf('max service level RMOE                  = %.6f\n', max_sl_rmoe))
cat(sprintf('max service level AMOE                  = %.6f\n', max_sl_amoe))
cat(sprintf('max total lead time RMOE                = %.6f\n', max_tlt_rmoe))
cat(sprintf('max total lead time AMOE                = %.6f\n', max_tlt_amoe))
sink(file.path(OUTPUT_FOLDER, 'out.txt'))
cat(sprintf('the facility "Mphyanakunda" is excluded = %s\n', is_excluded))
cat(sprintf('number of replications                  = %d\n', min_reps))
cat(sprintf('max service level RMOE                  = %.6f\n', max_sl_rmoe))
cat(sprintf('max service level AMOE                  = %.6f\n', max_sl_amoe))
cat(sprintf('max total lead time RMOE                = %.6f\n', max_tlt_rmoe))
cat(sprintf('max total lead time AMOE                = %.6f\n', max_tlt_amoe))
sink()

