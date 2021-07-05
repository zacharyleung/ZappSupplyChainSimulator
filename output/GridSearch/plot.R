require(ggplot2)
require(pracma)

tic()

the_df <- read.table('java.csv', sep = ',', header = TRUE)

the_df$name <- sprintf('%02.0f-%.2f',
       the_df$unmet_demand_cost,
       the_df$shipment_lead_time_percentile)

print(the_df)

ggplot(the_df, aes(x = service_level, y = inventory_level, label = name)) +
        geom_point(size = 1) +
        geom_text(size = 1, aes(label = name, hjust = 1.3)) +
        coord_cartesian(xlim = c(0.7, 1.0))

ggsave('grid-search.png', width = 5, height = 5)

toc()


