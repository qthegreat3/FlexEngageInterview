package com.qevans.metricapp.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class MetricsRepository implements IMetricsRepository {

	private Map<String, List<Double>> metricRepository;

	public MetricsRepository() {
		metricRepository = new ConcurrentHashMap<String, List<Double>>();
	}

	@Override
	public boolean addMetric(String metric) {

		// check if metric exists
		if (metricRepository.containsKey(metric)) {
			return false;
		}

		// create empty concurrent list
		List<Double> metricDataList = Collections.synchronizedList(new ArrayList<>());

		// add to map
		metricRepository.put(metric, metricDataList);

		return true;
	}

	@Override
	public String[] getAllMetrics() {
		// TODO Auto-generated method stub
		Set<String> metricsSet = metricRepository.keySet();
		String[] metricArray = new String[metricsSet.size()];
		return metricsSet.toArray(metricArray);
	}

	@Override
	public boolean addDataToMetric(String metric, double data) {
		// if metric not in map, don't do anything
		// else, if length is 0, insert
		if (!metricRepository.containsKey(metric)) {
			return false;
		}

		List<Double> metricsDataList = metricRepository.get(metric);
		// need to synchronize here
		synchronized (metricsDataList) {
			int insertionIndex = Collections.binarySearch(metricsDataList, data);

			if (insertionIndex < 0) {
				metricsDataList.add(~insertionIndex, data);
			} else {
				metricsDataList.add(insertionIndex, data);
			}
		}

		return true;
	}

	@Override
	public double getMedianOfMetric(String metric) {
		if (!metricRepository.containsKey(metric)) {
			throw new IllegalArgumentException("Metric: " + metric + " does not exist.");
		}

		List<Double> metricsDataList = metricRepository.get(metric);

		if (metricsDataList.isEmpty()) {
			throw new IllegalArgumentException("Cannot Calculate a median for an empty list.");
		}

		return calculateMedian(metricsDataList);
	}

	private double calculateMedian(List<Double> theList)
	{
		double median;
		
		synchronized(theList) {
			
		int middleOfList = theList.size() / 2;
		
		boolean isEvenLengthList = (theList.size() % 2) == 0;
		
		if (isEvenLengthList)
		{
		    median = (theList.get(middleOfList) + theList.get(middleOfList -1))/2;
		}
		else
		{
		    median = theList.get(middleOfList);
		}
		}
		
		 return median;
	}

	@Override
	public double getMinimumOfMetric(String metric) {
		// if metric doesnt exist, throw error
		if (!metricRepository.containsKey(metric)) {
			throw new IllegalArgumentException("Metric: " + metric + " does not exist.");
		}

		List<Double> metricsDataList = metricRepository.get(metric);

		return metricsDataList.get(0);
	}

	@Override
	public double getMaximumOfMetric(String metric) {
		// TODO Auto-generated method stub
		if (!metricRepository.containsKey(metric)) {
			throw new IllegalArgumentException("Metric: " + metric + " does not exist.");
		}

		List<Double> metricsDataList = metricRepository.get(metric);

		int endOfMetricsDataList = metricsDataList.size() - 1;

		return metricsDataList.get(endOfMetricsDataList);
	}

	@Override
	public double getAverageOfMetric(String metric) {
		if (!metricRepository.containsKey(metric)) {
			throw new IllegalArgumentException("Metric: " + metric + " does not exist.");
		}

		List<Double> metricsDataList = metricRepository.get(metric);

		if (metricsDataList.isEmpty()) {
			throw new IllegalArgumentException("Cannot Calculate a average for an empty list.");
		}

		return calculateAverage(metricsDataList);
	}

	private double calculateAverage(List<Double> theList) {
		Double sum = 0d;
		synchronized (theList) {
			if (!theList.isEmpty()) {
				for (Double value : theList) {
					sum += value;
				}
				return sum / theList.size();
			}
		}
		return sum;
	}

	@Override
	public List<Double> getDataForMetric(String metric) {
		if (!metricRepository.containsKey(metric)) {
			throw new IllegalArgumentException("Metric: " + metric + " does not exist.");
		}

		List<Double> metricsDataList = metricRepository.get(metric);
		
		return metricsDataList;
	}

}
