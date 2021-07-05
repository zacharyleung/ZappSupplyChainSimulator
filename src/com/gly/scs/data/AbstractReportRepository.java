package com.gly.scs.data;

import java.util.Collection;

import com.gly.scs.domain.Report;

public abstract class AbstractReportRepository {

	/**
	 * Add a new report to the report repository.
	 * The report will be received by the supplier facility
	 * <tt>supplierFacility</tt> at the period <tt>periodReceived</tt>.
	 * @param report
	 * @param supplierFacility
	 * @param periodReceived
	 */
	public abstract void addReport(Report report, String supplierFacility, 
			int periodReceived);
	
	/**
	 * Return the reports that are scheduled to arrive at the supplier
	 * facility <tt>supplierFacility</tt> and remove the reports from the
	 * repository.
	 * @param supplierFacility
	 * @param period
	 * @return
	 */
	public abstract Collection<Report> getReports(String supplierFacility,
			int period);
	
}
