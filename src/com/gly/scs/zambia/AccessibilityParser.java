package com.gly.scs.zambia;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import com.gly.scs.leadtime.ConstantSingleFacilityLeadTimeFactory;
import com.gly.scs.sched.RegionalOffsetEntry;

/**
 * Parse the accessibility input file.
 * 
 * The input file should have the following format:
 * 
 * <br />
 * <pre>
 * health facility,0,1,2,...,47
 * buli,0,0.2,0.5,...
 * </pre>
 *
 * For each health facility, we have the accessibility probabilities
 * for each of 48 periods.  The accessibility probability is a number
 * in [0,1].
 * @author zacleung
 *
 */
public class AccessibilityParser {

	HashMap<String, double[]> map = new HashMap<>();
	
	public AccessibilityParser(File file) throws FileNotFoundException {
		int numberOfPeriods = 48;
		int numberOfMonths = 12;
		int numberOfPeriodsInMonth = numberOfPeriods / numberOfMonths;
		try(Scanner scanner = new Scanner(file)) {
			
			// skip the header line
			scanner.nextLine();
			
			// read each data line
			while(scanner.hasNext()) {
				// parse the next line
				String line = scanner.nextLine();
				Scanner s = new Scanner(line);
				s.useDelimiter(",");

				String retail = s.next();
				double[] a = new double[numberOfPeriods];
				for (int i = 0; i < numberOfMonths; ++i) {
					double d = s.nextDouble();
					for (int j = 0; j < numberOfPeriodsInMonth; ++j) {
						a[4*i + j] = d;
					}
				}
				map.put(retail, a);
				
				// close the scanner
				s.close();
			}
		}
	}
	
	public double[] getAccessibility(String retailFacilityId) {
		return map.get(retailFacilityId);
	}
	
}
