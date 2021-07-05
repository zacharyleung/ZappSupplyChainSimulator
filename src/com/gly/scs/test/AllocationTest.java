package com.gly.scs.test;

import java.util.Collection;
import java.util.LinkedList;
import com.gly.scs.domain.*;
import com.gly.scs.replen.Allocation;
import com.gly.scs.replen.Allocation.Order;

public class AllocationTest {
	public static void main(String[] args) {
		Facility f1 = new RetailFacility.Builder()
				.withId("f1")
				.withStartPeriod(0)
				.withEndPeriod(1)
				.build();
		Facility f2 = new RetailFacility.Builder()
				.withId("f2")
				.withStartPeriod(0)
				.withEndPeriod(1)
				.build();
		
		LinkedList<Order> orders = new LinkedList<>();
		orders.add(new Order(f1, 40));
		orders.add(new Order(f2, 60));

		int inventory = 50;
		
		System.out.println("Proportional allocation");
		print(new Allocation.Proportional().getAllocation(orders, inventory));
		System.out.println();
		
		System.out.println("FCFS allocation");
		print(new Allocation.Fcfs().getAllocation(orders, inventory));
		System.out.println();
	}
	
	private static void print(Collection<Order> alloc) {
		for (Order order : alloc) {
			System.out.println(order);
		}
	}
}
