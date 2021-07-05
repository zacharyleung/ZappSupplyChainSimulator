package com.gly.scs.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.gly.scs.domain.Facility;
import com.gly.scs.domain.RegionalFacility;
import com.gly.scs.domain.RetailFacility;

public class FacilityRepository {
	
	private List<Facility> facilities;
	
	public FacilityRepository(List<Facility> facilities) {
		this.facilities = new ArrayList<>(facilities);
	}
	
	public List<Facility> getAll() {
		return facilities;
	}
	
	public Facility getFacility(String facilityId) {
		for (Facility facility : facilities) {
			if (facility.getId().equals(facilityId)) {
				return facility;
			}
		}
		throw new IllegalArgumentException(
				String.format("facility ID = %s does not exist!", 
						facilityId));
	}
	
	public List<RetailFacility> getRetailFacilities() {
		return getFacilities(RetailFacility.class);
	}

	public List<RegionalFacility> getRegionalFacilities() {
		return getFacilities(RegionalFacility.class);
	}

	/**
	 * Return all facilities of the specified class.
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> getFacilities(Class<T> clazz) {
		LinkedList<T> list = new LinkedList<T>();
		for (Facility f : facilities) {
			if (clazz.isInstance(f)) {
				list.add((T) f);
			}
		}
		return list;
	}	

}
