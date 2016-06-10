package com.cn.entity;

import java.util.List;

public class CompareEntity {

	private List<List<Taxi>> fragment;
	
	private double[][] similarity;

	public double[][] getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double[][] similarity) {
		this.similarity = similarity;
	}

	public List<List<Taxi>> getFragment() {
		return fragment;
	}

	public void setFragment(List<List<Taxi>> fragment) {
		this.fragment = fragment;
	}
	
}
