package com.gly.scs.domain;

public interface ReportProcessor {

	/**
	 * Return the number of periods of history to include in a report.
	 * @return
	 */
	public abstract int getReportHistoryPeriods();
	
	/**
	 * Return the number of periods of demand forecast to include in
	 * a report.
	 * @return
	 */
	public abstract int getReportForecastPeriods();

}
