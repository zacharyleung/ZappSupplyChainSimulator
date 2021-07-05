package com.gly.scs.opt;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class ShipmentArrivalPeriodsTest {

	@Test
	public void test() {
		ShipmentArrivalPeriods sap = new ShipmentArrivalPeriods();
		
		sap.put(0, 2);
		sap.put(0, 2);
		sap.put(0, 6);
		sap.put(0, 4);
		sap.put(1, 1);
		sap.put(1, 3);
		sap.put(1, 5);
		
		Iterator<Integer> itr;
		
		itr = sap.getShipmentArrivalPeriods(0).iterator();
		assertEquals(2, itr.next().intValue());
		assertEquals(4, itr.next().intValue());
		assertEquals(6, itr.next().intValue());

		itr = sap.getShipmentArrivalPeriods(1).iterator();
		assertEquals(1, itr.next().intValue());
		assertEquals(3, itr.next().intValue());
		assertEquals(5, itr.next().intValue());
	}

}
