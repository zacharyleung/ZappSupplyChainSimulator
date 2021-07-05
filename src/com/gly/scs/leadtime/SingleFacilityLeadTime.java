package com.gly.scs.leadtime;

import java.util.NoSuchElementException;

import com.gly.random.IntegerRandomVariable;

/**
 * Abstract single facility lead time class.
 * @author zacharyleung
 *
 */
public abstract class SingleFacilityLeadTime {

	abstract int getLeadTime(int t) throws NoSuchElementException;
	
	abstract IntegerRandomVariable getLeadTimeRandomVariable(int t);

	abstract double getAccessibility(int t);
	
}
