package com.gly.scs.zambia;


import java.io.*;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.gly.scs.demand.MmfeLevelsSingleFacilityDemandFactory;
import com.gly.scs.demand.MmfeLevelsSingleFacilityDemandFactory.Entry;
import com.gly.util.MathUtils;
import com.gly.scs.demand.MmfeLevelsSingleFacilityDemandFactory.Builder;


/**
 * Parse the demand means input file and create a 
 * 
 * The input file should have the following format:
 * 
 * <br />
 * <pre>
 * district,health facility,0,1,2,...,47
 * chama dho,buli,10,20,30,...
 * </pre>
 *
 * For each health facility, for each of 48 periods, we have the mean
 * demand.
 * 
 * @author zacleung
 *
 */
public final class MmfeDemandParser {

	public static final double DEFAULT_DEMAND_PARAMETER = 1;

	private MmfeDemandParser() {}

	/**
	 * The demandParameter should be between a number between 0 and 1.
	 *   - The value of 0 means that for each facility, the expected demand
	 *     is the same in each period.
	 *   - The value of 1 means that the demand pattern is exactly
	 *     identical to our existing malaria drug demand dataset.
	 */
	public static MmfeLevelsSingleFacilityDemandFactory parse(
			File file, double demandParameter) 
					throws FileNotFoundException {
		int numberOfPeriods = 48;

		LinkedList<MmfeLevelsSingleFacilityDemandFactory.Entry>
		entries = new LinkedList<>();

		try (Scanner scanner = new Scanner(file)) {
			String line;

			// skip the header line
			scanner.nextLine();

			// read each data line
			while(scanner.hasNext()) {
				// Parse the next line
				line = scanner.nextLine();
				Scanner s = new Scanner(line);
				s.useDelimiter(",");

				// Read the retail facility name
				String retail = s.next();

				// Read the mean demand values
				double[] a = new double[numberOfPeriods];
				for (int i = 0; i < numberOfPeriods; ++i) {
					a[i] = s.nextDouble();
				}

				// Apply the effect of the demand parameter
				a = MathUtils.applyConvexCombinationToMean(a, demandParameter);
				
				// Add the demand array for this retail facility into our list
				entries.add(new Entry(retail, a));

				// close the scanner
				s.close();
			}
		} catch (Exception e) {
			System.out.println("Cannot read file: " + file);
			throw(e);
		}

		double[] standardDeviation = getStandardDeviation();

		return new Builder()
				.withEntries(entries)
				.withStandardDeviation(standardDeviation)
				.withVarianceProportion(new double[]{1})
				.build();
	}

	/**
	 * Return the standard deviation for the multiplicative MMFE demand
	 * model, which was calibrated based on the Heath and Jackson 1994
	 * paper, and the Zambia demand estimation based on stock cards.
	 * 
	 * In Heath and Jackson (1994), 44%, 30%, 18% and 7% of demand
	 * variability is resolved 3, 2, 1 months before and during the
	 * month of sales respectively.
	 * The Zambia demand estimation based on stock cards estimates
	 * a weekly coefficient of variation of demand of 0.5.
	 * @return
	 */
	public static double[] getStandardDeviation() {
		// coefficient of variation of weekly demand
		double cv = 0.5;
		// percentage of variability resolved
		double[] povr = {1, 1.5, 2, 2.5, 3, 4, 5, 6,
				6.5, 7, 7.5, 8, 9, 10, 11, 16};

		// Given that X is a lognormal random variable
		// X ~ log normal(mu, sigma)
		// with coefficient of variation equal to 0.5,
		// what is sigma?
		// d_t = mean of d_t * X
		double sigma = Math.sqrt(Math.log(cv*cv + 1));

		//System.out.println("MmfeDemandParser.getStandardDeviation()");
		//System.out.printf("sigma = %.3f%n", sigma);

		double[] sd = new double[povr.length];
		for (int i = 0; i < sd.length; ++i) {
			sd[i] = Math.sqrt(povr[i] / 100 * sigma * sigma);
		}

		return sd;
	}


}
