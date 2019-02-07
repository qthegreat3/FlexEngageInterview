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

	/**
	 * <p>This method adds new metric names to the data store of metrics
	 * </p>
	 * @param metric is a String
	 * @return true is add is successful and false if metric already exists
	 * @throws IllegalArgumentException is metric is null or empty
	 * 
	 * Big O(constant) space is linear
	 */
	@Override
	public boolean addMetric(String metric) {

		if (metric == null || metric.isEmpty()) {
			throw new IllegalArgumentException("Metric cannot be null or empty.");
		}

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

	/**
	 * <p>This method returns all metric names created in the data store of metrics
	 * </p>
	 * @param metric is a String
	 * @return String array of containing all metric names 
	 * 
	 * Big O(n)
	 */
	@Override
	public String[] getAllMetrics() {
		Set<String> metricsSet = metricRepository.keySet();
		String[] metricArray = new String[metricsSet.size()];
		return metricsSet.toArray(metricArray);
	}

	/**
	 * <p>This method adds a data value to the list of data for a specified metric
	 * </p>
	 * @param metric is a String
	 * @param data is a double that will be added to list of data for metric
	 * @return true if add is successful and false if metric does not exist
	 * @throws IllegalArgumentException is metric is null
	 * 
	 * Big O(n): This add maintains the list of data as a sorted list.
	 * This is done using a binary search to find where to insert the new value.
	 * It is linear time because the other values in the list have to be shifted so that the 
	 * new value can be inserted in the proper place.
	 * 
	 * This makes for a slower add then is possible, but does allow for very fast calculation of median, min, and max of a given list.
	 * Reducing from nlog(n) time to constant for calculating these statistics.  Depending on use of the API, the tradeoff can be made.
	 * 
	 * If users are more using this to add a lot of values and won't use the statistics much, then I'd favor having adding of values be
	 * constant and have slower statistics calculations.
	 * 
	 * I assumed users would use this mainly to get statistics quickly, so I went with the slower add but with the fast stat calculations.
	 * 
	 * Space is linear
	 */
	@Override
	public boolean addDataToMetric(String metric, double data) {

		if (metric == null) {
			throw new IllegalArgumentException("Metric cannot be null");
		}
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

	/**
	 * <p>This method adds new metric names to the data store of metrics
	 * </p>
	 * @param metric is a String
	 * @return median of list of values for metric as a double.  If list has even amount of values
	 * then function returns the average between the 2 center values in the list. If list is empty, will return 0.
	 * @throws IllegalArgumentException is metric is null or is not in data store
	 * 
	 * Big O(constant)
	 */
	@Override
	public double getMedianOfMetric(String metric) {
		if (metric == null) {
			throw new IllegalArgumentException("Metric cannot be null");
		}

		if (!metricRepository.containsKey(metric)) {
			throw new IllegalArgumentException("Metric: " + metric + " does not exist.");
		}

		List<Double> metricsDataList = metricRepository.get(metric);

		if (metricsDataList.isEmpty()) {
			return 0.0;
		}

		return calculateMedian(metricsDataList);
	}

	private double calculateMedian(List<Double> theList) {
		double median;

		synchronized (theList) {

			int middleOfList = theList.size() / 2;

			boolean isEvenLengthList = (theList.size() % 2) == 0;

			if (isEvenLengthList) {
				median = (theList.get(middleOfList) + theList.get(middleOfList - 1)) / 2;
			} else {
				median = theList.get(middleOfList);
			}
		}

		return median;
	}

	/**
	 * <p>This method adds new metric names to the data store of metrics
	 * </p>
	 * @param metric is a String
	 * @return minimum of list of values for metric as a double. If list is empty, will return 0;
	 * @throws IllegalArgumentException is metric is null or is not in data store
	 * 
	 * Big O(constant)
	 */
	@Override
	public double getMinimumOfMetric(String metric) {
		if (metric == null) {
			throw new IllegalArgumentException("Metric cannot be null");
		}
		// if metric doesnt exist, throw error
		if (!metricRepository.containsKey(metric)) {
			throw new IllegalArgumentException("Metric: " + metric + " does not exist.");
		}

		List<Double> metricsDataList = metricRepository.get(metric);

		if(metricsDataList.isEmpty())
		{
			return 0.0;
		}
		
		return metricsDataList.get(0);
	}

	/**
	 * <p>This method adds new metric names to the data store of metrics
	 * </p>
	 * @param metric is a String
	 * @return maximum of list of values for metric as a double. If list is empty, will retun 0.
	 * @throws IllegalArgumentException is metric is null or is not in data store
	 * 
	 * Big O(constant)
	 */
	@Override
	public double getMaximumOfMetric(String metric) {
		if (metric == null) {
			throw new IllegalArgumentException("Metric cannot be null");
		}

		if (!metricRepository.containsKey(metric)) {
			throw new IllegalArgumentException("Metric: " + metric + " does not exist.");
		}

		List<Double> metricsDataList = metricRepository.get(metric);

		if (metricsDataList.isEmpty()) {
			return 0.0;
		}

		int endOfMetricsDataList = metricsDataList.size() - 1;

		return metricsDataList.get(endOfMetricsDataList);
	}

	/**
	 * <p>This method adds new metric names to the data store of metrics
	 * </p>
	 * @param metric is a String
	 * @return mean of list of values for metric as a double. If list is empty, will return 0.
	 * @throws IllegalArgumentException is metric is null or is not in data store
	 * 
	 * Big O(n)
	 */
	@Override
	public double getAverageOfMetric(String metric) {
		if (metric == null) {
			throw new IllegalArgumentException("Metric cannot be null");
		}

		if (!metricRepository.containsKey(metric)) {
			throw new IllegalArgumentException("Metric: " + metric + " does not exist.");
		}

		List<Double> metricsDataList = metricRepository.get(metric);

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

	/**
	 * <p>This method adds new metric names to the data store of metrics
	 * </p>
	 * @param metric is a String
	 * @return returns list of values stored for that metric
	 * @throws IllegalArgumentException is metric is null or is not in data store
	 * 
	 * Big O(constant)
	 */
	@Override
	public List<Double> getDataForMetric(String metric) {
		if (metric == null) {
			throw new IllegalArgumentException("Metric cannot be null");
		}

		if (!metricRepository.containsKey(metric)) {
			throw new IllegalArgumentException("Metric: " + metric + " does not exist.");
		}

		List<Double> metricsDataList = metricRepository.get(metric);

		return metricsDataList;
	}

}
