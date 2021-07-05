package com.gly.scs.sim;

import java.io.PrintStream;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Max;

import com.gly.scs.domain.*;
import com.gly.scs.leadtime.LeadTime;
import com.gly.scs.sched.AbstractShipmentSchedule;
import com.gly.util.MathUtils;
import com.gly.util.SparseIntArray;

/**
 * This class calculates summary statistics for one replication
 * of the Simulator, e.g., the service level, or the standard 
 * deviation of service levels across retail facilities.
 */
public class SimulationResults {
	private AbstractSimulator simulator;
	/** Data collection start period. */
	private int startPeriod;
	/** Data collection end period. */
	private int endPeriod;
	private int numberOfPeriods;

	/** Used in getRealizedSupplyDemandRatio(). */
	public static final int REALIZED_SUPPLY_DEMAND_RATIO_BUFFER = 8;

	public SimulationResults(AbstractSimulator simulator) {
		this.simulator = simulator;
		SimulationParameters params = simulator.simulationParameters; 
		startPeriod = params.getDataCollectionStartPeriod();
		endPeriod = params.getDataCollectionEndPeriod();
		numberOfPeriods = endPeriod - startPeriod; 
	}

	public void printTraceLongFormat(PrintStream out) {
		out.println("Facility,Period,Variable,Value");
		for (Facility facility : simulator.getFacilities()) {
			SparseIntArray shipmentSent = getShipmentsSent(facility);

			for (int t = startPeriod; t < endPeriod; ++t) {
				out.printf("%s,%d,%s,%d\n", facility.getId(), t,
						"InvBegin",
						facility.getBeginningOfPeriodInventory(t));
				out.printf("%s,%d,%s,%d\n", facility.getId(), t,
						"ShipmentSent",
						shipmentSent.get(t));
				out.printf("%s,%d,%s,%d\n", facility.getId(), t,
						"Demand",
						facility.getDemand(t));
				out.printf("%s,%d,%s,%d\n", facility.getId(), t,
						"UnmetDemand",
						facility.getUnmetDemand(t));
			}
		}
	}

	public void printTrace() {
		printTrace(System.out, startPeriod, endPeriod);
	}

	public void printTrace(PrintStream out,
			int simulationStartPeriod, int simulationEndPeriod) {
		out.println("SimulationResults.printTrace()");
		for (Facility facility : simulator.getFacilities()) {
			out.println("facility = " + facility.getId());
		}
	}

	/**
	 * Print the trace for the first retail facility.
	 * @param out
	 * @param simulationStartPeriod
	 * @param simulationEndPeriod
	 */
	public void printRetailTrace(PrintStream out) {
		Facility facility = simulator.facilityRepo.getRetailFacilities()
				.iterator().next();
		printTrace(out, facility, startPeriod, endPeriod);
	}

	public void printTrace(PrintStream out, Facility facility,
			int startPeriod, int endPeriod) {
		String[] headers = {"Period", "InvBegin", "ShipmentSent", "Demand",
		"UnmetDemand"};
		// number of spaces between two columns
		int n = 2;
		// compute the format string
		String formatString = "";
		for (String s : headers) {		
			formatString += String.format("%% %dd", s.length() + n);
		}
		formatString += "\n";

		// print header
		for (String s : headers) {
			for (int i = 0; i < n; ++i) {
				out.print(" ");
			}
			out.print(s);
		}
		out.println();

		SparseIntArray shipmentSent = getShipmentsSent(facility);

		for (int t = startPeriod; t < endPeriod; ++t) {
			out.printf(formatString,
					t,
					facility.getBeginningOfPeriodInventory(t),
					shipmentSent.get(t),
					facility.getDemand(t),
					facility.getUnmetDemand(t));
		}
	}

	public SparseIntArray getShipmentsSent(Facility facility) {
		Collection<Shipment> shipments = 
				simulator.shipmentRepo.getShipmentHistory(facility);
		SparseIntArray array = new SparseIntArray();
		for (Shipment shipment : shipments) {
			array.put(shipment.periodSent, shipment.quantity);
		}
		return array;
	}

	public int getUnmetDemand(String facilityId, int period) {
		return simulator.getFacility(facilityId).getUnmetDemand(period);
	}

	public int getShipment(String facilityId, int period) {
		return simulator.getFacility(facilityId).getShipment(period);
	}

	public long getTotalConsumption() {
		long sum = 0;
		for (Facility facility : simulator.facilityRepo.getRetailFacilities()) {
			sum += getTotalConsumption(facility);
		}
		return sum;		
	}

	public long getTotalConsumption(String facilityId) {
		return getTotalConsumption(simulator.getFacility(facilityId));
	}

	public long getTotalConsumption(Facility facility) {
		return getTotalConsumption(facility, startPeriod, endPeriod);
	}

	public long getTotalConsumption(Facility facility, int theStartPeriod, int theEndPeriod) {
		long sum = 0;
		for (int t = theStartPeriod; t < theEndPeriod; ++t) {
			sum += facility.getConsumption(t);
		}
		return sum;
	}

	public long getTotalDemand() {
		long sum = 0;
		for (Facility facility : simulator.facilityRepo.getRetailFacilities()) {
			sum += getTotalDemand(facility);
		}
		return sum;		
	}

	public long getTotalDemand(String facilityId) {
		return getTotalDemand(simulator.getFacility(facilityId));
	}


	public long getTotalDemand(Facility facility) {
		return getTotalDemand(facility, startPeriod, endPeriod);
	}

	public long getTotalDemand(Facility facility, int theStartPeriod, int theEndPeriod) {
		long sum = 0;
		for (int t = theStartPeriod; t < theEndPeriod; ++t) {
			sum += facility.getDemand(t);
		}
		return sum;
	}

	public double getMeanDemandPerPeriod(Facility facility) {
		return getTotalDemand(facility) / numberOfPeriods; 
	}

	/**
	 * The service level is defined as the satisfied demand divided by
	 * the total demand.
	 * @return
	 */
	public double getServiceLevel() {
		return MathUtils.doubleDivision(getTotalConsumption(), getTotalDemand());
	}

	public long getTotalRetailInventoryLevel() {
		long sum = 0;
		for (Facility facility : simulator.getFacilities()) {
			if (facility instanceof RetailFacility) {
				for (int t = startPeriod; t < endPeriod; ++t) {
					sum += facility.getInventory(t);
				}
			}
		}
		return sum;		
	}

	/**
	 * Calculate the mean inventory level at retail facilities, as the
	 * total inventory level divided by the total demand level. 
	 * @return
	 */
	public double getMeanRetailInventoryLevel() {
		return MathUtils.doubleDivision(getTotalRetailInventoryLevel(), 
				getTotalDemand());
	}

	public int getMaxInventoryLevel(Facility facility) {
		Max max = new Max();
		for (int t = startPeriod; t < endPeriod; ++t) {
			max.increment(facility.getInventory(t));
		}
		return (int) max.getResult();
	}

	public double getMeanMaxRetailInventoryLevel() {
		Mean mean = new Mean();
		for (Facility facility : simulator.facilityRepo.getRetailFacilities()) {
			mean.increment(
					getMaxInventoryLevel(facility) / getMeanDemandPerPeriod(facility));
		}
		return mean.getResult();
	}

	public double getServiceLevel(int theStartPeriod, int theEndPeriod) {
		long consumption = 0;
		long demand = 0;
		for (RetailFacility retailFacility : simulator.facilityRepo.getRetailFacilities()) {
			consumption += getTotalConsumption(retailFacility, theStartPeriod, theEndPeriod);
			demand += getTotalDemand(retailFacility, theStartPeriod, theEndPeriod);
		}
		return (1.0 * consumption) / demand;
	}

	public double getServiceLevel(Facility retailFacility) {
		long consumption = getTotalConsumption(retailFacility);
		long demand = getTotalDemand(retailFacility);
		double result = MathUtils.doubleDivision(consumption, demand);
		//System.out.printf("SimulationResults.getServiceLevel(%s)%n", retailFacility.getId());
		//System.out.printf("consumption = %d%n", consumption);
		//System.out.printf("demand = %d%n", demand);
		//System.out.printf("service level = %.4f%n", result);
		return result;
	}

	public double getServiceLevel(String retailFacilityId) {
		return getServiceLevel(simulator.facilityRepo.getFacility(retailFacilityId));
	}

	public double getStdDevOfServiceLevel() {
		SummaryStatistics stats = new SummaryStatistics();
		for (RetailFacility retailFacility : simulator.facilityRepo.getRetailFacilities()) {
			stats.addValue(getServiceLevel(retailFacility));
		}
		return stats.getStandardDeviation();
	}

	public double getWeightedStdDevOfServiceLevel() {
		double mu = getServiceLevel();
		double var = 0;
		double[] w = getArrayOfWeights();
		double[] sl = getArrayOfServiceLevel();
		for (int i = 0; i < w.length; ++i) {
			var += w[i] * Math.pow(sl[i] - mu, 2);
		}
		return Math.sqrt(var);
	}

	public double getGini() {
		double mu = getServiceLevel();
		double g = 0;
		double[] w = getArrayOfWeights();
		double[] sl = getArrayOfServiceLevel();
		for (int i = 0; i < w.length; ++i) {
			for (int j = 0; j < w.length; ++j) {
				g += 1 / (2 * mu) * w[i] * w[j] * Math.abs(sl[i] - sl[j]);
			}
		}
		return g;
	}

	/**
	 * The realized supply/demand ratio is defined as the total
	 * inventory divided by the total demand.
	 * 
	 * The total inventory is the sum of the initial inventory levels
	 * at all facilities at the simulation start period, plus all the
	 * future shipments that arrive at the national facility in time
	 * for them to trickle down to retail facilities.
	 * 
	 * The total demand is the total demand at the retail facilities
	 * over the simulation horizon.
	 * @return
	 */
	public double getRealizedSupplyDemandRatio() {
		long totalInventory = 0;
		// compute the total initial inventory levels
		for (Facility facility : simulator.facilityRepo.getAll()) {
			totalInventory += facility.getInventory(startPeriod);
		}
		// for cross-docking simulations, the regional facilities have
		// zero inventory so the above code is correct in that case
		// as well

		// add the shipments scheduled to arrive at the national facility
		int lastPeriod = endPeriod - REALIZED_SUPPLY_DEMAND_RATIO_BUFFER;
		for (int t = startPeriod; t < lastPeriod; ++t) {
			totalInventory += 
					simulator.nationalReplenishmentSchedule.getQuantity(t);
		}

		return MathUtils.doubleDivision(totalInventory, getTotalDemand());
	}

	public double getRealizedMeanSecondaryLeadTime(Facility facility) {
		AbstractShipmentSchedule shipmentSchedule =
				simulator.getRetailShipmentSchedule();
		LeadTime leadTime = simulator.getRetailLeadTime();
		String facilityId = facility.getId();
		Mean mean = new Mean();
		for (int t = startPeriod; t < endPeriod; ++t) {
			if (shipmentSchedule.isShipmentPeriod(facilityId, t)) {
				// if the lead time is finite
				if (leadTime.hasLeadTime(facilityId, t)) { 
					int l = leadTime.getSecondaryLeadTime(facilityId, t);
					mean.increment(l);
				}
			}
		}
		return mean.getResult();
	}

	public double getRealizedMeanTotalLeadTime(Facility facility) {
		AbstractShipmentSchedule shipmentSchedule =
				simulator.getRetailShipmentSchedule();
		LeadTime leadTime = simulator.getRetailLeadTime();
		String facilityId = facility.getId();
		Mean mean = new Mean();
		for (int t = startPeriod; t < endPeriod; ++t) {
			if (shipmentSchedule.isShipmentPeriod(facilityId, t)) {
				// if the lead time is finite
				if (leadTime.hasLeadTime(facilityId, t)) { 
					mean.increment(leadTime.getLeadTime(facilityId, t));
				}
			}
		}
		return mean.getResult();
	}

	public double getRealizedMeanSecondaryLeadTime(String retailFacilityId) {
		return getRealizedMeanSecondaryLeadTime(
				simulator.facilityRepo.getFacility(retailFacilityId));
	}

	public double getRealizedMeanTotalLeadTime(String retailFacilityId) {
		return getRealizedMeanTotalLeadTime(
				simulator.facilityRepo.getFacility(retailFacilityId));
	}

	/** Return the minimum service level at a facility. */
	public double getMinServiceLevel() {
		double[] sl = getArrayOfServiceLevel();
		return MathUtils.getMin(sl);
	}

	public double getMaxMinDiffServiceLevel() {
		double[] sl = getArrayOfServiceLevel();
		return MathUtils.getMax(sl) - MathUtils.getMin(sl);
	}

	public Long[] getArrayOfTotalConsumption() {
		LinkedList<Long> list = new LinkedList<>();
		for (Facility facility : simulator.facilityRepo.getRetailFacilities()) {
			list.addLast(getTotalConsumption(facility));
		}
		return list.toArray(new Long[list.size()]);
	}

	public Long[] getArrayOfTotalDemand() {
		LinkedList<Long> list = new LinkedList<>();
		for (Facility facility : simulator.facilityRepo.getRetailFacilities()) {
			list.addLast(getTotalDemand(facility));
		}
		return list.toArray(new Long[list.size()]);
	}

	public double[] getArrayOfServiceLevel() {
		Long[] con = getArrayOfTotalConsumption();
		Long[] dem = getArrayOfTotalDemand();
		double[] result = new double[con.length];
		for (int i = 0; i < con.length; ++i) {
			result[i] = (1.0 * con[i]) / dem[i];
		}
		return result;
	}

	/**
	 * The weight of each facility = facility demand / total demand
	 * across all facilities.
	 */
	public double[] getArrayOfWeights() {
		Long[] dem = getArrayOfTotalDemand();
		double total = getTotalDemand();
		double[] result = new double[dem.length];
		for (int i = 0; i < dem.length; ++i) {
			result[i] = dem[i] / total;
		}
		return result;
	}

}
