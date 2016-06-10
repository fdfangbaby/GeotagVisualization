package com.cn.dao;

import java.util.List;
import java.util.Set;

import com.cn.entity.Taxi;

public interface TaxiDao {

	public List<Taxi> Search(String taxiId, String time);
	public List<Taxi> getAll();
	public Set<String> getAllTaxiId();
	public void Delete(Taxi taxi);
	public Set<String> GetTaxiIdByFuzzy(String fuzzyId);
}
