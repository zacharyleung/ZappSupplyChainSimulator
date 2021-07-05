CombineFiles = function(the_folders, the_file) {
    z = data.frame()
    for (the_folder in the_folders) {
        x = file.path(the_folder, the_file)
        
        # Check if the file exists
        if (file.exists(x)) {
            #x = read.table(x, sep = ',', header = TRUE)
            x = read_csv(x, comment = '#')
            z = rbind(z, x)
        }
    }
    return(z)
}

