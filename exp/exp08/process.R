## Exp08
## Convert the output from java into input for pgfplots

rm(list = ls())
require(tools)
require(reshape2)
require(tidyverse)

source('../CombineFolders.R')
source('../MarginOfError.R')
source('../PrintHeader.R')

# Delete rows where the service level is less than this value
MIN_SERVICE_LEVEL = 0.2
MAX_INVENTORY_LEVEL = 20
OUTPUT_FOLDER = 'process-out'

# Set input_folders to all folders in the input_parent directory
input_parent = 'process-in'
input_folders = list.dirs(path = input_parent)
input_folders = input_folders[-1]

inputFileExtension = 'csv'
outputFileExtension = 'csv'

min_reps = 1000000
max_rmoe = 0
max_sl_amoe = 0
max_il_amoe = 0
max_sdsl_amoe = 0
max_inv_decrease = 0

# The variable is TRUE if the inventory mean is increasing across
# all policies
inv_mean_is_increasing = TRUE

# delete all files in the output folder
for (f in list.files(OUTPUT_FOLDER)) {
    if (file_ext(f) == outputFileExtension) {
        theFile = paste(OUTPUT_FOLDER, f, sep = '/')
        cat(sprintf('Deleting %s...\n', theFile))
        file.remove(theFile)
    }
}

# for each file in the input folder
for (f in list.files(input_folders[1])) {
    print(f)
    if (file_ext(f) == inputFileExtension) {
        x = CombineFiles(input_folders, f)
        print(tbl_df(x))
        
        # convert data from long format to wide format
        # compute the means
        y = dcast(x,
                  parameter + random_seed ~ variable,
                  value.var = 'value')
        #print(tbl_df(y))
        
        z = y %>% 
            group_by(parameter) %>%
            summarise(
                reps = n(),
                inv_mean = mean(inventory_level),
                inv_amoe =
                    AbsoluteMarginOfError(inventory_level),
                inv_rmoe = 
                    RelativeMarginOfError(inventory_level),
                #min_inv = min(InventoryLevel),
                #max_inv = max(InventoryLevel),
                gini_mean = mean(gini),
                wsdsl_mean = mean(wgtdstddev_service_level),
                minsl_mean = mean(min_service_level),
                maxminsl_mean = mean(maxmin_service_level),
                serv_mean = mean(service_level),
                serv_amoe =
                    AbsoluteMarginOfError(service_level),
                serv_rmoe = 
                    RelativeMarginOfError(service_level),
                sdsl_mean = mean(stddev_service_level),
                sdsl_amoe = 
                    AbsoluteMarginOfError(stddev_service_level),
                sdsl_rmoe = 
                    RelativeMarginOfError(stddev_service_level))
        #print(tbl_df(z))
        
        # Remove rows where the mean service level is less than 30%
        z = z %>% filter(serv_mean >= MIN_SERVICE_LEVEL)
        # Remove rows where the mean inventory greater than 21
        z = z %>% filter(inv_mean <= MAX_INVENTORY_LEVEL)
        
        min_reps = min(min_reps, z$reps)
        max_sl_amoe = max(max_sl_amoe, z$serv_amoe)
        max_sdsl_amoe = max(max_sdsl_amoe, z$sdsl_amoe)
        max_il_amoe = max(max_il_amoe, z$inv_amoe)
        max_rmoe = max(max_rmoe, z$inv_rmoe)
        max_rmoe = max(max_rmoe, z$serv_rmoe)
        max_rmoe = max(max_rmoe, z$sdsl_rmoe)
        
        PrintHeader(f)
        cat(sprintf('max RMOE               = %.6f\n', max_rmoe))
        
        is_increasing = !is.unsorted(z$inv_mean)
        inv_mean_is_increasing = inv_mean_is_increasing & 
            is_increasing
        cat(sprintf('inventory level is increasing = %s\n',
                    is_increasing))
        
        temp = lead(z$inv_mean) - z$inv_mean
        cat(sprintf('inventory increasing = %.2e\n',
                    min(temp, na.rm = TRUE)))
        max_inv_decrease = max(max_inv_decrease, max(-temp, na.rm = TRUE))
        cat('\n\n\n')
        
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
cat(sprintf('max serv level AMOE    = %.6f\n', max_sl_amoe))
cat(sprintf('max sd serv level AMOE = %.6f\n', max_sdsl_amoe))
cat(sprintf('max inv level AMOE     = %.6f\n', max_il_amoe))
cat(sprintf('inventory mean is increasing across all policies = %s\n',
            inv_mean_is_increasing))
cat(sprintf('max inventory level decrease = %.4f\n', max_inv_decrease))



##---------------------------------------------------------------
## Write to file
##---------------------------------------------------------------

sink(file.path(OUTPUT_FOLDER, 'out.txt'))
cat(sprintf('number of replications = %d\n', min_reps))
cat(sprintf('max RMOE               = %.6f\n', max_rmoe))
cat(sprintf('max serv level AMOE    = %.6f\n', max_sl_amoe))
cat(sprintf('max sd serv level AMOE = %.6f\n', max_sdsl_amoe))
cat(sprintf('max inv level AMOE     = %.6f\n', max_il_amoe))
cat(sprintf('inventory mean is increasing across all policies = %s\n',
            inv_mean_is_increasing))
cat(sprintf('max inventory level decrease = %.4f\n', max_inv_decrease))

sink()


