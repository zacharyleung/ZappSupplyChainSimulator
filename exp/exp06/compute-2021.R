


library(tidyverse)
library(pracma)

tic()

START_PERIOD = 36
END_PERIOD = 48
EPSILON = 1e-6    # To avoid 0 / 0 errors, do x / (y + EPSILON)
NUMBER_OF_REPLICATIONS = 500

# For debugging
#NUMBER_OF_REPLICATIONS = 10 

REFERENCE_FACILITIES =
    c('chibale', 'chifunda', 'chikwa', 'chingi', 'chiwoma', 'chozi',
      'kakoma', 'kapolyo', 'lundu', 'mapamba', 'mulumbi', 'nakonde',
      'ngoli', 'ntatumbila', 'shem', 'sitwe', 'tembwe')

ReadPackSize = function(pack_size) {
    type_names = c('istock', 'xdock')
    input_folders = sprintf('%s-%02d', type_names, pack_size)
    print(input_folders)
    
    x = tibble()
    for (input_folder in input_folders) {
        the_files = list.files(input_folder, pattern = '*.csv')
        #print(the_files)
        for (the_file in the_files[1:NUMBER_OF_REPLICATIONS]) {
            y = read_csv(file.path(input_folder, the_file), col_types = cols())
            y = y %>%
                filter(Period >= START_PERIOD & Period < END_PERIOD) %>%
                filter(Facility %in% REFERENCE_FACILITIES)
            y = y %>% 
                pivot_wider(names_from = Variable, values_from = Value)
            y = y %>%
                mutate(stockout_days = 365 / 48 * UnmetDemand / (Demand + EPSILON))
            
            z = y %>%
                rename(facility = Facility) %>%
                group_by(facility) %>%
                summarize(soq4 = sum(stockout_days)) %>%
                mutate(input_folder = input_folder, file = the_file)
            #print(z)
            
            # Combine into the larger data frame
            x = bind_rows(x, z)
        }
    }
    x = x %>%
        arrange(facility) 
    return(x)
}


x = ReadPackSize(6)
print(x)

summary_table = x %>%
    group_by(facility) %>%
    summarise(min = min(soq4),
              perc025 = quantile(soq4, 0.025),
              perc250 = quantile(soq4, 0.250),
              mean = mean(soq4),
              perc750 = quantile(soq4, 0.750),
              perc975 = quantile(soq4, 0.975),
              max = max(soq4))
write_csv(summary_table, 'compute-2021-pack06.csv')

toc()
