package com.gly.scs.sim;

public abstract class SupplyChainFactory {

	public abstract SupplyChain getSupplyChain(double supplyDemandRatio);

	/**
	 * The demandParameter should be between a number between 0 and 1.
	 *   - The value of 0 means that for each facility, the expected demand
	 *     is the same in each period.
	 *   - The value of 1 means that the demand pattern is exactly
	 *     identical to our existing malaria drug demand dataset.
	 */
	public abstract SupplyChain getSupplyChainWithDemandParameter(
			double supplyDemandRatio, double demandParameter);

	/**
	 * The accessParameter should be between a number between 0 and 1.
	 *   - The value of 0 means that for each facility is fully accessible
	 *     in each period.
	 *   - The value of 1 means that the accessibility pattern is exactly
	 *     identical to our existing malaria drug demand dataset.
	 */
	public abstract SupplyChain getSupplyChainWithAccessParameter(
			double supplyDemandRatio, double accessParameter);

	/**
	 * The parameter should be a number between 0 and 1.
	 * This parameter is used as both the access parameter and the
	 * demand parameter.
	 */
	public abstract SupplyChain getSupplyChainWithDemandAndAccessParameter(
			double supplyDemandRatio, double parameter);
}
