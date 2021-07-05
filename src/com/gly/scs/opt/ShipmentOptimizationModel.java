package com.gly.scs.opt;

import gurobi.*;

import java.io.File;
import java.util.*;

import com.gly.random.*;
import com.gly.util.*;

/**
 * One supplier facility
 * Multiple retail facilities
 * 
 * We have received a report from each retail facility,
 * with the report created at the beginning of period T0[r].
 * 
 * The current period is denoted by T1.
 * T2 is the last date for which we have information
 * forecastHorizon
 *
 * You can set the number of Gurobi threads by changing the contents
 * of the file SupplyChainSimulator/gurobi-threads.txt.
 * 
 * @author zacharyleung
 *
 */
public class ShipmentOptimizationModel {
	public static Collection<ShipmentDecision> getShipmentDecisions(
			Inputs inputs, Options options) throws Exception {
		ShipmentOptimizationModel som = 
				new ShipmentOptimizationModel(inputs, options);
		return som.getShipmentDecisions();
	}

	boolean shouldPrint = false;
	boolean shouldWriteModel = false;

	private Inputs inputs;
	private Options options;

	// Cost for every unit of inventory sent
	private double shipmentCost = 0.001;

	// Gurobi decision variables
	private SparseArray<GRBVar> is;
	private SparseArray<GRBVar> js;
	private Sparse2DArray<GRBVar> cr;
	private Sparse2DArray<GRBVar> ir;
	private Sparse2DArray<GRBVar> jr;
	private Sparse2DArray<GRBVar> ur;
	private Sparse2DArray<GRBVar> xr;
	private ShipmentArrivalPeriods shipmentArrivalPeriods;

	private ShipmentOptimizationModel(Inputs inputs, Options options) {
		this.inputs = inputs;
		this.options = options;
	}

	private Collection<ShipmentDecision> getShipmentDecisions()
			throws Exception {
		try {
			initialize();
			checkStates();
			declareDecisionVariables();
			addObjective();
			addSupplierInventoryConstraints();
			addRetailInventoryConstraints();
			addConsumptionUnmetDemandConstraints();
			addUnmetDemandLowerBounds();
			addShipmentConstraints();
		} catch (Exception e) {
			System.out.printf("forecastHorizon = %d%n", options.forecastHorizon);
			for (int r = 0; r < R; ++r) {
				System.out.printf("T0[%d] = %d%n", r, T0[r]);
			}		
			System.out.printf("T1 = %d%n", T1);
			System.out.printf("T2 = %d%n", T2);
			throw e;
		}

		// GUROBI: optimize model
		model.getEnv().set(GRB.IntParam.OutputFlag, 0);
		model.getEnv().set(GRB.IntParam.Threads, getNumberOfThreads());
		model.optimize();

		//System.out.printf("Number of threads = %d%n", getNumberOfThreads());

		// GUROBI: write model to file
		if (shouldWriteModel) {
			String file = String.format("temp/%d.lp", T1);
			model.write(file);
		}

		try {
			is.get(T1).get(GRB.DoubleAttr.X);
		} catch (Exception e) {
			model.computeIIS();
			model.write("som.ilp");
			throw new IllegalArgumentException("Infeasible model");
		}

		Collection<ShipmentDecision> shipmentDecisions = recoverSolution();

		// GUROBI: dispose of model and environment
		model.dispose();
		env.dispose();

		return shipmentDecisions;
	}

	private int getNumberOfThreads() {
		try (Scanner sc = new Scanner(new File("gurobi-threads.txt"))) {
			return sc.nextInt();
		} catch (Exception e) {
			return 1;
		}
	}

	// Gurobi environment and model
	private GRBEnv env;
	private GRBModel model;

	/** Number of retail facilities */
	private int R;

	private int[] T0;
	private int T1;
	private int T2;

	/**
	 * because the retail states have different start periods,
	 * we set the stop period of the optimization model as follows.
	 * @throws Exception
	 */
	private void initialize() throws Exception {
		env = new GRBEnv("clair.log");
		model = new GRBModel(env);

		R = inputs.retailStates.length;
		T1 = inputs.supplierState.getPeriod();
		T0 = new int[R];
		T2 = T1 + options.forecastHorizon;
		// because the retail states have different start periods,
		// we set the stop period of the optimization model as follows
		for (int r = 0; r < R; ++r) {
			T0[r] = inputs.retailStates[r].getPeriod();
			T2 = Math.min(T2, T0[r] + options.forecastHorizon);
		}

		shipmentArrivalPeriods = new ShipmentArrivalPeriods();
	}

	private void checkStates() throws Exception {
		// check that the accessibility and demand forecast
		// is defined for [T0[r], T2)
		for (int r = 0; r < R; ++r) {
			for (int t = T0[r]; t < T2; ++t) {
				inputs.retailStates[r].getDemandForecast(t);
				inputs.retailStates[r].getAccessibility(t);
			}
		}
		if (options.sltType == ShipmentLeadTimeType.CONSERVATIVE) {
			if (options.shipmentLeadTimePercentile <= 0 ||
					options.shipmentLeadTimePercentile >= 1) {
				throw new IllegalArgumentException(
						"shipmentLeadTimePercentile should be in (0,1)");
			}
		}
	}

	private void declareDecisionVariables() throws GRBException {
		// GUROBI: create decision variables
		is = new SparseArray<>();
		js = new SparseArray<>();
		for (int t = T1 - 1; t <= T2; ++t) {
			is.put(t, model.addVar(0, GRB.INFINITY, 0.0,
					GRB.CONTINUOUS, String.format("is[%d]", t)));
			js.put(t, model.addVar(0, GRB.INFINITY, 0.0,
					GRB.CONTINUOUS, String.format("js[%d]", t)));
		}
		cr = new Sparse2DArray<>();
		ir = new Sparse2DArray<>();
		jr = new Sparse2DArray<>();
		ur = new Sparse2DArray<>();
		xr = new Sparse2DArray<>();
		for (int r = 0; r < R; ++r) {
			for (int t = T0[r] - 1; t <= T2; ++t) {
				cr.put(r, t, newVar(String.format("cr[%d][%d]", r, t)));
				ir.put(r, t, newVar(String.format("ir[%d][%d]", r, t)));
				jr.put(r, t, newVar(String.format("jr[%d][%d]", r, t)));
				ur.put(r, t, newVar(String.format("ur[%d][%d]", r, t)));
				xr.put(r, t, newVar(String.format("xr[%d][%d]", r, t)));
			}
		}		
		// GUROBI: Integrate new variables
		model.update();
	}

	private GRBVar newVar(String name) throws GRBException {
		return model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, name);
	}

	private void addObjective() throws GRBException {
		GRBLinExpr obj = new GRBLinExpr();
		for (int r = 0; r < R; ++r) {
			for (int t = T0[r]; t < T2; ++t) {
				double accessibility = inputs.retailStates[r].getAccessibility(t);
				double hc = 0;
				switch(options.holdingCostType) {
				case CONSTANT:
					hc = 1;
					break;
				case FACILITY_ACCESSIBILITY:
					hc = accessibility;
					break;
				}
				hc = Math.max(hc, options.minHoldingCost);
				obj.addTerm(hc, jr.get(r, t));
				obj.addTerm(options.unmetDemandCost, ur.get(r, t));
			}
		}

		// Add a small penalty for every unit of inventory sent
		for (int r = 0; r < R; ++r) {
			for (int t = T0[r]; t < T2; ++t) {
				obj.addTerm(shipmentCost, xr.get(r, t));
			}
		}
		model.setObjective(obj, GRB.MINIMIZE);
	}

	/**
	 * Add constraints regarding the inventory level at the national facility.
	 * @throws GRBException
	 */
	private void addSupplierInventoryConstraints() throws GRBException {
		// initial inventory constraint
		// j^S_T1-1 = I^S
		model.addConstr(js.get(T1 - 1),
				GRB.EQUAL,
				inputs.supplierState.getInventoryLevel(),
				"j^S_T1 - 1 = I^S");

		// j^S_t = i^S_t - sum_r x^r_t 
		for (int t = T1; t < T2; ++t) {
			GRBLinExpr rhs = new GRBLinExpr();
			rhs.addTerm(1.0, is.get(t));
			for (int r = 0; r < R; ++r) {
				rhs.addTerm(-1.0, xr.get(r, t));
			}
			model.addConstr(rhs,
					GRB.EQUAL,
					js.get(t),
					String.format("j^S_t = i^S_t - sum_r x^r_t [%d]", t));			
		}


		// i^S_t = j^S_t-1 + X^s_t
		SparseArray<GRBLinExpr> rhs = new SparseArray<>();
		for (int t = T1; t < T2; ++t) {
			rhs.put(t, new GRBLinExpr());
			rhs.get(t).addTerm(1.0, js.get(t - 1));
		}
		for (IncomingShipment shipment : inputs.supplierState.getIncomingShipments()) {
			int t = shipment.getPeriodArrival();
			addConstantConditionally(rhs, t, shipment.getQuantity());
		}
		for (int t = T1; t < T2; ++t) {
			model.addConstr(rhs.get(t),
					GRB.EQUAL,
					is.get(t),
					String.format("i^S_t = j^S_t-1 + X^s_t [%d]", t));
		}
	}

	private void addRetailInventoryConstraints() throws Exception {
		for (int r = 0; r < R; ++r) {
			// initial inventory constraint
			model.addConstr(jr.get(r, T0[r] - 1),
					GRB.EQUAL,
					inputs.retailStates[r].getInventoryLevel(),
					"j^r_T0" + r);

			// j^r_t = i^r_t - c^r_t
			for (int u = T0[r]; u < T2; ++u) {
				GRBLinExpr expr = new GRBLinExpr();
				expr.addTerm(1.0, ir.get(r, u));
				expr.addTerm(-1.0, cr.get(r, u));
				model.addConstr(expr,
						GRB.EQUAL,
						jr.get(r, u),
						String.format("j^r_t = i^r_t - c^r_t [%d][%d]", r, u));
			}

			// lhs = j^r_t-1 + sum 1_{L^r_s = t - s} x^r_s
			SparseArray<GRBLinExpr> lhs = new SparseArray<>();
			for (int u = T0[r]; u <= T2; ++u) {
				lhs.put(u, new GRBLinExpr());
				lhs.get(u).addTerm(1.0, jr.get(r, u - 1));
			}

			// for each incoming shipment, make it arrive as soon as possible
			for (IncomingShipment shipment : inputs.retailStates[r].getIncomingShipments()) {
				int u = shipment.getPeriodSent();

				int leadTime = 0;
				switch(options.sltType) {
				case ACTUAL:
					leadTime = shipment.getRealizedLeadTime();
					break;
				case CONSERVATIVE:
					// in the case make the shipment arrive at the
					// earliest possible period
					IntegerRandomVariable ltrv = 
					shipment.getLeadTimeRandomVariable();
					leadTime = ltrv.inverseCumulativeProbability(
							1 - options.shipmentLeadTimePercentile);
					break;
				}
				int v = u + leadTime;

				// w = earliest possible period when shipment could arrive
				// given that it has not arrived yet by T0[r] when the report
				// was submitted
				int w = Math.max(T0[r], v);
				// if the shipment arrives within the planning horizon,
				// then make it arrive in period w
				addConstantConditionally(lhs, w, shipment.getQuantity());
				//				if (shouldPrint) {
				//					System.out.println("Past shipment {");
				//					System.out.printf("  facility = %d\n", r);
				//					System.out.printf("  quantity = %d\n", shipment.quantity);
				//					System.out.printf("  period sent = %d\n", u);
				//					System.out.printf("  earliest possible period received = %d\n", w);
				//					System.out.println("}");
				//				}

				// add w as a shipment arrival period
				shipmentArrivalPeriods.put(r, w);
			}

			// print the shipment opportunities
			//			for (ShipmentOpportunity so : inputs.retailStates[r].getShipmentOpportunities()) {
			//				System.out.println(so);
			//			}

			// add shipment quantities arriving from future shipments
			Iterator<ShipmentOpportunity> itr = 
					inputs.retailStates[r].getShipmentOpportunities().iterator();

			// for first shipment, make it arrive at the earliest possible period
			if (itr.hasNext()) {
				ShipmentOpportunity opp = itr.next();
				int u = opp.getPeriod();

				int leadTime = 0;
				switch(options.sltType) {
				case ACTUAL:
					leadTime = opp.getRealizedLeadTime();
					break;
				case CONSERVATIVE:
					// in the case make the shipment arrive at the
					// an early period
					IntegerRandomVariable ltrv = opp.getLeadTimeRandomVariable(); 
					leadTime = ltrv.inverseCumulativeProbability(
							1 - options.shipmentLeadTimePercentile);
					break;
				}
				int v = u + leadTime;

				addTermConditionally(lhs, v, 1.0, xr.get(r, u));

				// add v as a shipment arrival period
				shipmentArrivalPeriods.put(r, v);
			}

			// for subsequent shipments, make them arrive at the with the
			// lead time equal to the percentile of the lead time distribution 
			while (itr.hasNext()) {
				ShipmentOpportunity opp = itr.next();
				int u = opp.getPeriod();

				// if the current shipment opportunity is beyond the
				// planning horizon, then don't plan for this shipment
				// opportunity
				if (u >= T2) { continue; }

				int leadTime = 0;
				switch(options.sltType) {
				case ACTUAL:
					leadTime = opp.getRealizedLeadTime();
					break;
				case CONSERVATIVE:
					// in the case make the shipment arrive at the
					// an early period
					IntegerRandomVariable ltrv = opp.getLeadTimeRandomVariable(); 
					leadTime = ltrv.inverseCumulativeProbability(
							options.shipmentLeadTimePercentile);
					break;
				}
				int v = u + leadTime;

				addTermConditionally(lhs, v, 1.0, xr.get(r, u));

				// add v as a shipment arrival period
				shipmentArrivalPeriods.put(r, v);
			}

			// add constraints to the model
			for (int u = T0[r]; u <= T2; ++u) {
				model.addConstr(lhs.get(u),
						GRB.EQUAL,
						ir.get(r, u),
						"i^r_t = j^r_t-1 + sum 1_{L^r_s = t - s} x^r_s" + r + " " + u);
			}			
		}
	}

	/**
	 * Add constraints of the type: c^r_t = D^r_t - u^r_t
	 * @throws Exception
	 */
	private void addConsumptionUnmetDemandConstraints() throws Exception {
		for (int r = 0; r < R; ++r) {
			for (int t = T0[r]; t < T2; ++t) {
				RetailState retailState = inputs.retailStates[r];
				AbstractRandomVariable demand = retailState.getDemandForecast(t);

				// c^r_t = D^r_t - u^r_t
				double d = 0;
				switch (options.udlbType) {
				case ACTUAL:
					d = retailState.getRealizedDemand(t);
					break;

				default:
					d = demand.getMean();
				}
				GRBLinExpr rhs = new GRBLinExpr();
				rhs.addConstant(d);
				rhs.addTerm(-1.0, ur.get(r, t));
				model.addConstr(cr.get(r, t),
						GRB.EQUAL,
						rhs,
						String.format("c^r_t = D^r_t - u^r_t [%d][%d]", r, t));
			}
		}
	}

	/**
	 * Add unmet demand lower bounds: u^r_t >= a i^r_t + b
	 * @throws Exception
	 */
	private void addUnmetDemandLowerBounds() throws Exception {
		switch (options.udlbType) {
		case ACTUAL:
			addUnmetDemandLowerBoundsActual();
			break;
		case MEAN:
			addUnmetDemandLowerBoundsMean();
			break;
		case SINGLE_PERIOD:
			addUnmetDemandLowerBoundsSingle();
			break;
		case MULTI_PERIOD:
			addUnmetDemandLowerBoundsMulti();
			break;
		}
	}

	private void addUnmetDemandLowerBoundsActual() throws Exception {
		for (int r = 0; r < R; ++r) {
			for (int t = T0[r]; t < T2; ++t) {
				RetailState retailState = inputs.retailStates[r];
				GRBLinExpr rhs = new GRBLinExpr();
				rhs.addConstant(retailState.getRealizedDemand(t));
				rhs.addTerm(-1.0, ir.get(r, t));
				model.addConstr(ur.get(r, t),
						GRB.GREATER_EQUAL,
						rhs,
						String.format("u^r_t >= a i^r_t + b [%d][%d]", r, t));
			}
		}
	}

	private void addUnmetDemandLowerBoundsMean() throws Exception {
		for (int r = 0; r < R; ++r) {
			for (int t = T0[r]; t < T2; ++t) {
				RetailState retailState = inputs.retailStates[r];
				AbstractRandomVariable demand = retailState.getDemandForecast(t);
				GRBLinExpr rhs = new GRBLinExpr();
				rhs.addConstant(demand.getMean());
				rhs.addTerm(-1.0, ir.get(r, t));
				model.addConstr(ur.get(r, t),
						GRB.GREATER_EQUAL,
						rhs,
						String.format("u^r_t >= a i^r_t + b [%d][%d]", r, t));

			}
		}
	}

	private void addUnmetDemandLowerBoundsSingle() throws Exception {
		for (int r = 0; r < R; ++r) {
			for (int t = T0[r]; t < T2; ++t) {
				RetailState retailState = inputs.retailStates[r];
				AbstractRandomVariable demand = retailState.getDemandForecast(t);

				for (UnmetDemandLowerBound udlb : 
					options.udlbFactory.computeTangents(demand)) {
					GRBLinExpr rhs = new GRBLinExpr();
					rhs.addConstant(udlb.intercept);
					rhs.addTerm(udlb.slope, ir.get(r, t));
					model.addConstr(ur.get(r, t),
							GRB.GREATER_EQUAL,
							rhs,
							String.format("u^r_t >= a i^r_t + b [%d][%d]", r, t));
				}
			}
		}
	}

	private void addUnmetDemandLowerBoundsMulti() throws Exception {
		for (int r = 0; r < R; ++r) {
			RetailState retailState = inputs.retailStates[r];

			// add the report creation period as the period to start
			// computing unmet demand lower bounds
			shipmentArrivalPeriods.put(r, T0[r]);

			SortedSet<Integer> saps = setMaximum(
					shipmentArrivalPeriods.getShipmentArrivalPeriods(r), T2);
			Iterator<Integer> itr = saps.iterator();
			int t = itr.next();
			// add unmet demand lower bounds for periods [t, u)
			while (itr.hasNext()) {
				int u = itr.next();
				//System.out.printf("t = %d, u = %d%n", t, u);

				NormalRandomVariable demand = arvToNormal(
						retailState.getDemandForecast(t));
				for (int i = t + 1; i < u; ++i) {
					demand = demand.add(arvToNormal(
							retailState.getDemandForecast(i)));
				}

				for (UnmetDemandLowerBound udlb : 
					options.udlbFactory.computeTangents(demand)) {
					GRBLinExpr rhs = new GRBLinExpr();
					rhs.addConstant(udlb.intercept);
					rhs.addTerm(udlb.slope, ir.get(r, t));
					GRBLinExpr lhs = new GRBLinExpr();
					for (int i = t; i < u; ++i) {
						lhs.addTerm(1.0, ur.get(r, i));
					}
					model.addConstr(lhs,
							GRB.GREATER_EQUAL,
							rhs,
							String.format("u^r_t >= a i^r_t + b [%d][%d,%d]", r, t, u));
				}

				// update t and u
				t = u;
			}
		}
	}

	/**
	 * Return a sorted set whose maximum element is less than or equal
	 * to the specified max.
	 * @param input
	 * @param max
	 * @return
	 */
	private SortedSet<Integer> setMaximum(SortedSet<Integer> input, int max) {
		TreeSet<Integer> output = new TreeSet<>();
		input.add(Integer.MAX_VALUE);
		for (int i : input) {			
			if (i <= max) {
				output.add(i);
			} else {
				output.add(max);
				break;
			}
		}
		return output;
	}

	/**
	 * Convert an abstract random variable to a normal random variable
	 * with the same mean and standard deviation.
	 * @param arv
	 * @return
	 */
	private NormalRandomVariable arvToNormal(AbstractRandomVariable arv) {
		return new NormalRandomVariable.Builder()
				.withMean(arv.getMean())
				.withStandardDeviation(arv.getStdDev())
				.build();
	}

	/**
	 * If the supplier does not have a shipment opportunity to retail
	 * facility r in period t, then set x^r_t = 0.
	 * @throws Exception
	 */
	private void addShipmentConstraints() throws Exception {
		for (int r = 0; r < R; ++r) {
			for (int u = T0[r]; u < T2; ++u) {
				if (!inputs.retailStates[r].hasShipmentOpportunity(u)) {
					model.addConstr(xr.get(r, u),
							GRB.EQUAL,
							0.0,
							String.format("x^r_t = 0 [%d][%d]", r, u));					
				}
			}
		}
	}

	/**
	 * Conver the value of the variables into shipment decisions.
	 * @return
	 * @throws Exception
	 */
	private Collection<ShipmentDecision> recoverSolution() throws Exception {
		LinkedList<ShipmentDecision> shipmentDecisions = new LinkedList<>();
		boolean shouldPrint = false;
		for (int r = 0; r < R; ++r) {
			for (int t = T1; t <= T2; ++t) {
				//System.out.printf("xr.get(%d,  %d)%n", r, t);
				double q = xr.get(r, t).get(GRB.DoubleAttr.X);
				if (q > 0.001) {
					if (shouldPrint) {
						System.out.printf("x[%d][%d] = %.1f\n", r, t, q);
					}
					if (t == T1) {
						shipmentDecisions.add(new ShipmentDecision.Builder()
								.withRetailId(r)
								.withPeriod(t)
								.withQuantity((int) q)
								.build());
					}
				}
			}
		}

		// print the value of the variables
		for (int r = 0; r < R; ++r) {
			for (int t = T0[r]; t <= T2; ++t) {
				//				System.out.printf("r = %d, t = %d, i = %.0f, j = %.0f, u = %.0f%n", 
				//						r, t,
				//						ir.get(r, t).get(GRB.DoubleAttr.X),
				//						jr.get(r, t).get(GRB.DoubleAttr.X),
				//						ur.get(r, t).get(GRB.DoubleAttr.X));
			}
		}

		return shipmentDecisions;
	}

	private void addTermConditionally(SparseArray<GRBLinExpr> array, int t, 
			double coeff, GRBVar var) {
		if (t < T2) {
			array.get(t).addTerm(coeff, var);
		}
	}

	private void addConstantConditionally(SparseArray<GRBLinExpr> array,
			int t, double value) {
		if (t < T2) {
			array.get(t).addConstant(value);
		}		
	}

	public static class Inputs {
		private SupplierState supplierState;
		private RetailState[] retailStates;

		private Inputs(Builder builder) {
			this.supplierState = builder.supplierState;
			this.retailStates = builder.retailStates;
		}

		public static class Builder {
			private SupplierState supplierState;
			private RetailState[] retailStates;

			public Builder withRetailStates(RetailState[] retailStates) {
				this.retailStates = retailStates;
				return this;
			}

			public Builder withSupplierState(SupplierState supplierState) {
				this.supplierState = supplierState;
				return this;
			}

			public Inputs build() {
				return new Inputs(this);
			}
		}
	}

	public static class Options {
		private final int forecastHorizon;
		private final HoldingCostType holdingCostType;
		private final double minHoldingCost;
		private final double unmetDemandCost;
		private final double shipmentLeadTimePercentile;
		private final UnmetDemandLowerBoundFactory udlbFactory;
		private final UnmetDemandLowerBoundType udlbType;
		private final ShipmentLeadTimeType sltType;

		public Options(Builder builder) {
			forecastHorizon = builder.forecastHorizon;
			holdingCostType = builder.holdingCostType;
			minHoldingCost = builder.minHoldingCost;
			unmetDemandCost = builder.unmetDemandCost;
			shipmentLeadTimePercentile = builder.shipmentLeadTimePercentile;
			udlbFactory = builder.udlbFactory;
			udlbType = builder.udlbType;
			sltType = builder.sltType;
		}

		public static class Builder {
			private int forecastHorizon;
			private HoldingCostType holdingCostType;
			private double minHoldingCost = 0.01;
			private double unmetDemandCost;
			private double shipmentLeadTimePercentile;
			private UnmetDemandLowerBoundFactory udlbFactory;
			private UnmetDemandLowerBoundType udlbType;
			private ShipmentLeadTimeType sltType;

			public Builder withForecastHorizon(int forecastHorizon) {
				this.forecastHorizon = forecastHorizon;
				return this;
			}

			public Builder withHoldingCostType(HoldingCostType holdingCostType) {
				this.holdingCostType = holdingCostType;
				return this;
			}

			public Builder withUnmetDemandCost(double unmetDemandCost) {
				this.unmetDemandCost = unmetDemandCost;
				return this;
			}

			public Builder withShipmentLeadTimeType(ShipmentLeadTimeType sltType) {
				this.sltType = sltType;
				return this;
			}

			public Builder withShipmentLeadTimePercentile(double shipmentLeadTimePercentile) {
				this.shipmentLeadTimePercentile = shipmentLeadTimePercentile;
				return this;
			}

			public Builder withUdlbFactory(UnmetDemandLowerBoundFactory udlbFactory) {
				this.udlbFactory = udlbFactory;
				return this;
			}

			public Builder withUdlbType(UnmetDemandLowerBoundType udlbType) {
				this.udlbType = udlbType;
				return this;
			}

			public Options build() {
				return new Options(this);
			}
		}
	}
}
