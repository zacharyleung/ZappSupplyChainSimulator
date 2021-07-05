package com.gly.scs.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.gly.scs.domain.Report;

/**
 * An implementation of the <tt>AbstractReportRepository</tt> class
 * using a linked list.
 * 
 * @author zacharyleung
 *
 */
public class ReportRepository extends AbstractReportRepository {

	LinkedList<Entry> entries = new LinkedList<>();
	
	@Override
	public void addReport(Report report, String supplierFacility,
			int periodReceived) {
		entries.add(new Entry(report, supplierFacility, periodReceived));
	}

	@Override
	public Collection<Report> getReports(String supplierFacility, int period) {
		LinkedList<Report> outList = new LinkedList<>();

		Iterator<Entry> itr = entries.iterator();
		while (itr.hasNext()) {
			Entry entry = itr.next();
			// if the report arrives at the current period
			if (entry.periodReceived == period && 
					entry.supplierFacility.equals(supplierFacility)) {
				// remove the report from the regional facility report list
				itr.remove();
				outList.add(entry.report);
			}	
		}

		return outList;
	}

	private class Entry {
		private int periodReceived;
		private String supplierFacility;
		private Report report;
		
		private Entry(Report report, String supplierFacility,
				int periodReceived) {
			this.report = report;
			this.periodReceived = periodReceived;
			this.supplierFacility = supplierFacility;
		}
	}
	
}
