package com.cn.service;

import java.util.List;
import java.util.Set;

import com.cn.entity.Taxi;

public interface TaxiService {

	public List<Taxi> search(String taxiId, String time);
	public List<Taxi> getAll();
	public Set<String> getAllTaxiId( );
	public void transform(List<Taxi> list);
	public void GenerateTurningPoints(List<String> taxiIds);
	public List<List<Taxi>> GetFragment(List<String> list);
	public double[][] GetSimilarity(List<List<Taxi>> trajectory);
	public double getHausdorffDistance(List<Taxi> t1,List<Taxi> t2);
	public void DeleteDuplicateTaxi(List<String> id);
}
