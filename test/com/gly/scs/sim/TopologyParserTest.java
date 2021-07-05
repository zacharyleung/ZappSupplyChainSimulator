package com.gly.scs.sim;

import static org.junit.Assert.*;

import org.junit.Test;

import com.gly.scs.domain.Topology;
import com.gly.scs.zambia.TopologyParser;

public class TopologyParserTest {

	@Test
	public void test() {
		try {
			String file = "test/com/gly/scs/sim/RetailToRegionalParserTest.txt";
			Topology retailToRegional = TopologyParser.parse(file);
			System.out.println(retailToRegional);
			assertEquals("Texas", retailToRegional.getRegional("Dallas"));
			assertEquals("Texas", retailToRegional.getRegional("Austin"));
			assertEquals("California", retailToRegional.getRegional("San Francisco"));
		} catch (Exception e) {
			System.out.println(e);
			fail("Exception!");
		}
	}

}
