package com.gly.scs.zambia;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Scanner;

import com.gly.scs.domain.Topology;
import com.gly.scs.domain.Topology.Entry;
import com.gly.scs.domain.Topology.Entry.Builder;

/**
 * Class to read a Zambia topology file and parse it into a
 * {@link Topology} object.
 * 
 * Expected input file format:
 * 
 * <br />
 * <pre>
 * district,health facility
 * Texas,Dallas
 * Texas,Austin
 * California,San Francisco
 * </pre>
 * 
 * @author zacharyleung
 *
 */
public final class TopologyParser {
	
	private TopologyParser() {}
	
	public static Topology parse(File file) throws FileNotFoundException {
		try(Scanner scanner = new Scanner(file)) {
			LinkedList<Entry> entries = new LinkedList<>();
			
			// skip the header line
			scanner.nextLine();
			
			// read each data line
			while(scanner.hasNext()) {
				String line = scanner.nextLine();
				
				// check that the line has one and only one comma
				if (line.indexOf(",") != line.lastIndexOf(",")) {
					throw new InputMismatchException(
							"more than one comma in line " + line);
				}
				
				int i = line.indexOf(",");
				String regional = line.substring(0, i);
				String retail = line.substring(i + 1, line.length());
				entries.add(new Entry.Builder()
				.withRegionalFacilityId(regional)
				.withRetailFacilityId(retail)
				.build());
			}
			
			return new Topology(entries);
		}
	}
}
