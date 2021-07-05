

library(tidyverse)
library(stringr)

OUTPUT_FOLDER = 'R/trace'

CleanFacility = function(x) {
    x = str_replace_all(x, ' ', '_')
    x = str_replace_all(x, '\\(', '')
    x = str_replace_all(x, '\\)', '')
    return(x)    
}

#FacilityToFileName = function(x) {
#    x = sprintf('%s.csv', x)
#    x = file.path(OUTPUT_FOLDER, x)
#    return(x)
#}



x = read_csv('java/trace.txt')
colnames(x) = str_to_lower(colnames(x))
x = x %>% spread(variable, value)
colnames(x) = str_to_lower(colnames(x))
print(x)

# Arrange the facilities by increasing service level
x %>%
    group_by(facility) %>%
    summarise(service_level = 1 - sum(unmetdemand) / sum(demand)) %>%
    arrange(service_level) %>%
    print




##-----------------------------------------------------------------
## Compute the service level for each (facility, year)
## Write the output to a file
##-----------------------------------------------------------------

y = x %>%
    mutate(year = floor(period / 48)) %>%
    filter(!str_detect(facility, "dho")) %>%
    mutate(facility = CleanFacility(facility)) %>%
    group_by(facility, year) %>%
    summarise(service_level = 1 - sum(unmetdemand) / sum(demand))
print(y)
z = y %>%
    select(facility, year, service_level) %>%
    spread(facility, service_level)
print(z)
write_csv(z, 'R/trace.csv')

g = ggplot(data = y, aes(x = year, y = service_level, group = facility)) + 
    geom_line()
    #geom_line(position=position_jitter(w=0.02, h=0.02))
print(g)
ggsave('trace.png')

stop()

facility_set = unique(x$facility)
for (the_facility in facility_set) {
    out_file = FacilityToFileName(the_facility)
    z = x %>% filter(facility == the_facility)
    z = z %>%
        mutate(year = floor(period / 48)) %>%
        group_by(year) %>%
        summarise(service_level = 1 - sum(unmetdemand) / sum(demand))
    #print(z)
    write_csv(z, out_file)
}
