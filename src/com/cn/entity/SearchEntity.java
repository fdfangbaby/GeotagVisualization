package com.cn.entity;

import java.util.List;

public class SearchEntity {

	private String taxiId;
	private int clusterId;
	private List<String> comparedTaxiIds; 
	private int index;
	public int getClusterId() {
		return clusterId;
	}
	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getTaxiId() {
		return taxiId;
	}
	public void setTaxiId(String taxiId) {
		this.taxiId = taxiId;
	}
	public List<String> getComparedTaxiIds() {
		return comparedTaxiIds;
	}
	public void setComparedTaxiIds(List<String> comparedTaxiIds) {
		this.comparedTaxiIds = comparedTaxiIds;
	}
	
}
