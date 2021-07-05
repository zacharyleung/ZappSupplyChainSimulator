package com.gly.scs.zambia;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gly.scs.domain.Topology.Entry;
import com.gly.scs.leadtime.ConstantSingleFacilityLeadTimeFactory;
import com.gly.scs.leadtime.SingleFacilityLeadTimeFactory;
import com.gly.scs.sched.RegionalOffsetEntry;

public final class ReplenishmentFileParser {

	private List<RegionalOffsetEntry> 
	regionalOffsetEntries = new LinkedList<>();

	private List<ConstantSingleFacilityLeadTimeFactory.Entry>
	primaryLeadTimeEntries = new LinkedList<>();
	
	private HashMap<String, Double> map = new HashMap<>();
	
	public ReplenishmentFileParser(File file) throws FileNotFoundException {
		Logger logger = LoggerFactory.getLogger(ReplenishmentFileParser.class);
	    logger.info("Reading file: " + file);
		
		String headerLine = 
				"district,delivery_group,primary_leadtime,mean_secondary_leadtime";
		try(Scanner scanner = new Scanner(file)) {
			String line;

			// check that the header line is what is expected
			line = scanner.nextLine();
			if (!line.equals(headerLine)) {
				System.out.println(line);
				throw new InputMismatchException("header of file does not match");
			}

			// read each data line
			while(scanner.hasNext()) {
				// parse the next line
				line = scanner.nextLine();
				Scanner s = new Scanner(line);
				s.useDelimiter(",");
				String regional = s.next();
				int offset = s.nextInt();
				int primaryLeadTime = s.nextInt();
				double secondaryLeadTime = s.nextDouble();

				// store the entries in data structures 
				regionalOffsetEntries.add(
						new RegionalOffsetEntry.Builder()
						.withOffset(offset)
						.withRegionalFacilityId(regional)
						.build());
				primaryLeadTimeEntries.add(
						new ConstantSingleFacilityLeadTimeFactory.Entry(
								regional, primaryLeadTime));
				map.put(regional, secondaryLeadTime);
				
				// close the scanner
				s.close();
			}
		}
	}

	public Collection<RegionalOffsetEntry> getRegionalOffsetEntries() {
		return regionalOffsetEntries;
	}

	public SingleFacilityLeadTimeFactory getPrimarySfltFactory() {
		return new ConstantSingleFacilityLeadTimeFactory(primaryLeadTimeEntries);
	}
	
	public double getMeanSecondaryLeadTime(String regionalFacilityId) {
		//System.out.println("ReplenishmentFileParser.getMeanSecondaryLeadTime()");
		//System.out.println(regionalFacilityId);
		return map.get(regionalFacilityId);
	}
	
}
