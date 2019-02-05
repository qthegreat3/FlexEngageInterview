package com.qevans.metricapp.repository;

import java.util.List;

public interface IMetricsRepository {

	public boolean addMetric(String metric);
	public String[] getAllMetrics();
	public boolean addDataToMetric(String metric, double data);
	public double getMedianOfMetric(String metric);
	public double getMinimumOfMetric(String metric);
	public double getMaximumOfMetric(String metric);
	public double getAverageOfMetric(String metric);
	public List<Double> getDataForMetric(String metric);
	
}
