package com.gly.util;

import java.io.File;

import com.google.common.io.Files;

public class FileUtils {
	
	/**
	 * Delete CSV files in the folder. This function does not recursively
	 * deletes all sub-folders and files under them.
	 */
	public static void deleteCsvFiles(File dir) {
		// If the file is not a directory, throw an exception
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a directory");
		}
		
		for (File file: dir.listFiles()) {
			String extension = Files.getFileExtension(file.getName());
			if (extension.equals("csv")) {
				file.delete();
			}
		}
	}
	
}
