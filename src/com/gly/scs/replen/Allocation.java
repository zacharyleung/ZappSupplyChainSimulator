package com.gly.scs.replen;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.gly.scs.domain.Facility;

/**
 * How to allocate a limited inventory based on orders.
 * @author znhleung
 *
 */
public abstract class Allocation {
	public static class Order {
		public final Facility customer;
		public final int quantity;

		public Order(Facility customer, int quantity) {
			this.customer = customer;
			this.quantity = quantity;
		}
		
		@Override
		public String toString() {
			return String.format("Order(%s,%d)",
					customer.getId(), quantity);
		}
	}

	private static int getTotalQuantity(Collection<Order> orders) {
		int sum = 0;
		for (Order order : orders) {
			sum += order.quantity;
		}
		return sum;
	}

	public Collection<Order> getAllocation(
			Collection<Order> orders, int inventory) {
		if (inventory >= getTotalQuantity(orders)) {
			return orders;
		} else {
			return myGetAllocation(orders, inventory);
		}
	}

	public abstract Collection<Order> myGetAllocation(
			Collection<Order> orders, int inventory);

	public static class Proportional extends Allocation {
		@Override
		public Collection<Order> myGetAllocation(
				Collection<Order> orders, int inventory) {
			double factor = (1.0 * inventory) / getTotalQuantity(orders);
			LinkedList<Order> result = new LinkedList<>();
			for (Order order : orders) {
				int q = (int) (factor * order.quantity);
				result.add(new Order(order.customer, q));
			}
			return result;
		}
	}

	public static class Fcfs extends Allocation {
		@Override
		public Collection<Order> myGetAllocation(
				Collection<Order> orders, int inventory) {
			// this variable keeps track of the inventory remaining
			int i = inventory;
			LinkedList<Order> result = new LinkedList<>();
			for (Order order : orders) {
				int q = Math.min(i, order.quantity);
				i -= q;
				if (q > 0) {
					result.add(new Order(order.customer, q));
				}
			}
			return result;
		}
	}
	
	public static class RandomFcfs extends Allocation {
		public static int seed = 0;
		
		@Override
		public Collection<Order> myGetAllocation(
				Collection<Order> orders, int inventory) {
			// randomly shuffle the orders
			LinkedList<Order> list = new LinkedList<>();
			list.addAll(orders);
			Collections.shuffle(list, new Random(seed)); 
			// increment the random seed
			seed++;
			// use Fcfs.getAllocation(.)
			return new Fcfs().getAllocation(list, inventory);
		}		
	}
}
