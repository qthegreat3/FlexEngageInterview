package com.qevans.metricapp.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class MetricsRepositoryTest {

	@Test
	public void addMetricTest() {
		MetricsRepository repo = new MetricsRepository();

		// add null
		try {
			repo.addMetric(null);
			fail();
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage().equals("Metric cannot be null or empty."));
		}
		// add empty
		try {
			repo.addMetric("");
			fail();
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage().equals("Metric cannot be null or empty."));
		}

		// regular add
		String addedMetric = "myMetric";
		boolean isAdded = repo.addMetric(addedMetric);
		assertTrue(isAdded);

		String[] metricList = repo.getAllMetrics();

		assertTrue(foundGivenMetric(metricList, addedMetric));

		// add duplicate
		isAdded = repo.addMetric(addedMetric);
		assertFalse(isAdded);
	}

	private boolean foundGivenMetric(String[] metricList, String metricToFind) {
		boolean foundMyMetric = false;
		for (String metric : metricList) {
			if (metric.equals(metricToFind)) {
				foundMyMetric = true;
			}
		}

		return foundMyMetric;
	}

	@Test
	public void addMultiThreadTest() {
		MetricsRepository repo = new MetricsRepository();
		String metric1 = "metric1";
		String metric2 = "metric2";
		String metric3 = "metric3";

		Runner run1 = new Runner(repo, metric1);
		Runner run2 = new Runner(repo, metric2);
		Runner run3 = new Runner(repo, metric3);
		Thread addThread1 = new Thread(run1);
		Thread addThread2 = new Thread(run2);
		Thread addThread3 = new Thread(run3);

		addThread1.start();
		addThread2.start();
		addThread3.start();

		while (!run1.isDone() || !run2.isDone() || !run3.isDone()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Gettting List Now!");
		String[] metricList = repo.getAllMetrics();

		assertTrue(foundGivenMetric(metricList, metric1) && foundGivenMetric(metricList, metric2)
				&& foundGivenMetric(metricList, metric3));

		System.out.println("Done!");
	}

	public class Runner implements Runnable {

		private MetricsRepository repo;
		private String myMetric;
		private boolean done = false;

		public Runner(MetricsRepository repo, String metric) {
			this.repo = repo;
			this.myMetric = metric;
		}

		public boolean isDone() {
			return done;
		}

		@Override
		public void run() {
			System.out.println("Adding MyMetric: " + myMetric + "...");
			repo.addMetric(myMetric);
			done = true;
		}
	}
	
	@Test
	public void addDataToMetricTest()
	{
		MetricsRepository repo = new MetricsRepository();
		
		double data = 1.2;
		double data2 = -1.3;
		double data3 = 1.25;
		double data4 = -1.3;
		double data5 = 0.1;
		
		String metric = "metric";
		try
		{
			repo.addDataToMetric(null, data);
			fail();
		}
		catch(IllegalArgumentException ex)
		{
			assertTrue(ex.getMessage().equals("Metric cannot be null"));
		}
		
		assertFalse(repo.addDataToMetric("", data));
		repo.addMetric(metric);

		try
		{
			repo.addDataToMetric(null, data);
			fail();
		}
		catch(IllegalArgumentException ex)
		{
			assertTrue(ex.getMessage().equals("Metric cannot be null"));
		}
		
		assertFalse(repo.addDataToMetric("", data));
		
		assertTrue(repo.addDataToMetric(metric, data));
		
		List<Double> metricDataList = repo.getDataForMetric(metric);
		
		assertTrue(metricDataList.contains(data));
		//is ordered after multiple adds
		repo.addDataToMetric(metric, data2);
		repo.addDataToMetric(metric, data3);
		repo.addDataToMetric(metric, data4);
		repo.addDataToMetric(metric, data5);
		
		metricDataList = repo.getDataForMetric(metric);
		
		printList(metricDataList);
		
		assertTrue(isOrderedList(metricDataList));		
	}
	
	@Test
	public void addDataToMetricMultithreadTest()
	{
		MetricsRepository repo = new MetricsRepository();
		
		double data = 1.2;
		double data2 = -1.3;
		double data3 = 1.25;
		double data4 = -1.3;
		double data5 = 0.1;
		
		String metric = "metric";
		
		repo.addMetric(metric);
		
		AddDataRunner run1 = new AddDataRunner(repo, data, metric);
		AddDataRunner run2 = new AddDataRunner(repo, data2, metric);
		AddDataRunner run3 = new AddDataRunner(repo, data3, metric);
		Thread addThread1 = new Thread(run1);
		Thread addThread2 = new Thread(run2);
		Thread addThread3 = new Thread(run3);

		addThread1.start();
		addThread2.start();
		addThread3.start();

		while (!run1.isDone() || !run2.isDone() || !run3.isDone()) {
			try {
				//System.out.println("Waiting for runs to finish...");
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		List<Double> metricDataList = repo.getDataForMetric(metric);
		
		printList(metricDataList);
		
		assertTrue(isOrderedList(metricDataList));
	}
	
	public class AddDataRunner implements Runnable {
		
		private MetricsRepository repo;
		private double dataToAdd;
		private String metric;
		private boolean done = false;
		
		public AddDataRunner(MetricsRepository repo, double dataToAdd, String metric)
		{
			this.repo = repo;
			this.dataToAdd = dataToAdd;
			this.metric = metric;
		}
		
		public boolean isDone()
		{
			return this.done;
		}
		
		@Override
		public void run() {
			System.out.println("Adding Data: " + dataToAdd);
			repo.addDataToMetric(metric, dataToAdd);
			done = true;
		}		
	}
	
	private void printList(List<Double> theList)
	{
		for(Double item : theList)
		{
			System.out.print(item + ",");
		}
	}
	
	private boolean isOrderedList(List<Double> theList)
	{
		if(theList.size() == 1 || theList.isEmpty())
		{
			return true;
		}
		
		for(int listIndex = 1; listIndex < theList.size(); listIndex++)
		{
			double item = theList.get(listIndex);
			for(int listOfNumbersToCheckIndex = 0; listOfNumbersToCheckIndex < listIndex; listOfNumbersToCheckIndex++)
			{
				if(theList.get(listOfNumbersToCheckIndex) > item)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Test
	public void getStatisticsTest()
	{
		MetricsRepository repo = new MetricsRepository();
		
		String metric = "metric";
		
		try
		{
			repo.getAverageOfMetric(null);
			fail();
		}
		catch (IllegalArgumentException ex)
		{
			assertTrue(ex.getMessage().equals("Metric cannot be null"));
		}
	
		try
		{
			repo.getMaximumOfMetric(null);
			fail();
		}
		catch (IllegalArgumentException ex)
		{
			assertTrue(ex.getMessage().equals("Metric cannot be null"));
		}
	
		try
		{
			repo.getMinimumOfMetric(null);
			fail();
		}
		catch (IllegalArgumentException ex)
		{
			assertTrue(ex.getMessage().equals("Metric cannot be null"));
		}
	
		try
		{
			repo.getMedianOfMetric(null);
			fail();
		}
		catch (IllegalArgumentException ex)
		{
			assertTrue(ex.getMessage().equals("Metric cannot be null"));
		}
	
		try
		{
			repo.getAverageOfMetric("");
			fail();
		}
		catch (IllegalArgumentException ex)
		{
			assertTrue(ex.getMessage().equals("Metric: " + "" + " does not exist."));
		}

		try
		{
			repo.getMedianOfMetric("");
			fail();
		}
		catch (IllegalArgumentException ex)
		{
			assertTrue(ex.getMessage().equals("Metric: " + "" + " does not exist."));
		}
		
		try
		{
			repo.getMinimumOfMetric("");
			fail();
		}
		catch (IllegalArgumentException ex)
		{
			assertTrue(ex.getMessage().equals("Metric: " + "" + " does not exist."));
		}
		
		try
		{
			repo.getMaximumOfMetric("");
			fail();
		}
		catch (IllegalArgumentException ex)
		{
			assertTrue(ex.getMessage().equals("Metric: " + "" + " does not exist."));
		}
		
		repo.addMetric(metric);
				
		double zero = 0.0;
		//case of 0 entries
		assertTrue(repo.getAverageOfMetric(metric) == zero);
		assertTrue(repo.getMedianOfMetric(metric) == zero);
		assertTrue(repo.getMinimumOfMetric(metric) == zero);
		assertTrue(repo.getMaximumOfMetric(metric) == zero);
		
		//case of 1 entry
		double data = 1.0;		
		
		repo.addDataToMetric(metric, data);
		
		assertTrue(repo.getAverageOfMetric(metric) == data);
		assertTrue(repo.getMedianOfMetric(metric) == data);
		assertTrue(repo.getMinimumOfMetric(metric) == data);
		assertTrue(repo.getMaximumOfMetric(metric) == data);
		
		double data1 = 5.1;
		
		//case of 2 positive
		repo.addDataToMetric(metric, data1);
		
		double sum = data + data1;
		double expectedAverage = sum /2;
		
		assertTrue(repo.getAverageOfMetric(metric) == expectedAverage);
		assertTrue(repo.getMedianOfMetric(metric) == expectedAverage);
		assertTrue(repo.getMinimumOfMetric(metric) == data);
		assertTrue(repo.getMaximumOfMetric(metric) == data1);		

		//case of 3 postive and negative
		double data2 = -1.4;		
		repo.addDataToMetric(metric, data2);
		
		sum = data + data1 + data2;
		expectedAverage = sum / 3;
		
		assertTrue(repo.getAverageOfMetric(metric) == expectedAverage);
		assertTrue(repo.getMedianOfMetric(metric) == data);
		assertTrue(repo.getMinimumOfMetric(metric) == data2);
		assertTrue(repo.getMaximumOfMetric(metric) == data1);
		//case of 3 all negative
		String negativeMetric = "negMetric";
		repo.addMetric(negativeMetric);
		
		double neg10 = -10d;
		double neg46pt5 = -46.6;
		double neg1pt5 = -1.5;
				
		repo.addDataToMetric(negativeMetric, neg10);
		repo.addDataToMetric(negativeMetric, neg46pt5);
		repo.addDataToMetric(negativeMetric, neg1pt5);

		sum = neg10 + neg46pt5 + neg1pt5;
		expectedAverage = sum / 3;
		
		assertTrue(repo.getAverageOfMetric(negativeMetric) == expectedAverage);
		assertTrue(repo.getMedianOfMetric(negativeMetric) == neg10);
		assertTrue(repo.getMinimumOfMetric(negativeMetric) == neg46pt5);
		assertTrue(repo.getMaximumOfMetric(negativeMetric) == neg1pt5);
		
		//case of 5 really big postive numbers
		String reallyBigMetric = "bigMetric";
		repo.addMetric(reallyBigMetric);
		
		double d12mil = 12000000d;
		double d52mil = 52000000d;
		double d82mil = 82000000d;
		double d132mil = 132000000d;
		double d72mil = 72000000d;
		
		repo.addDataToMetric(reallyBigMetric, d12mil);
		repo.addDataToMetric(reallyBigMetric, d52mil);
		repo.addDataToMetric(reallyBigMetric, d82mil);
		repo.addDataToMetric(reallyBigMetric, d132mil);
		repo.addDataToMetric(reallyBigMetric, d72mil);

		sum = d12mil + d52mil + d82mil + d132mil + d72mil;
		expectedAverage = sum / 5;
		
		assertTrue(repo.getAverageOfMetric(reallyBigMetric) == expectedAverage);
		assertTrue(repo.getMedianOfMetric(reallyBigMetric) == d72mil);
		assertTrue(repo.getMinimumOfMetric(reallyBigMetric) == d12mil);
		assertTrue(repo.getMaximumOfMetric(reallyBigMetric) == d132mil);
		
		//case of 5 really small positive numbers
		String reallySmallMetric = "smallMetric";
		repo.addMetric(reallySmallMetric);
		
		double tiny5 =          .000000000000000000005;
		double tiny1 =          .000000000000000000001;
		double tinytiny5 =      .00000000000000000000005;
		double small1 =         .0000000000000000001;
		double teenytinytiny8 = .00000000000000000000008;
		
		repo.addDataToMetric(reallySmallMetric, tiny5);
		repo.addDataToMetric(reallySmallMetric, tiny1);
		repo.addDataToMetric(reallySmallMetric, tinytiny5);
		repo.addDataToMetric(reallySmallMetric, small1);
		repo.addDataToMetric(reallySmallMetric, teenytinytiny8);
		
		sum = tiny5 + tiny1 + tinytiny5 + small1 + teenytinytiny8;
		expectedAverage = sum / 5;
		
		printList(repo.getDataForMetric(reallyBigMetric));
		
		assertTrue(repo.getAverageOfMetric(reallySmallMetric) == expectedAverage);
		assertTrue(repo.getMedianOfMetric(reallySmallMetric) == tiny1);
		assertTrue(repo.getMinimumOfMetric(reallySmallMetric) == tinytiny5);
		assertTrue(repo.getMaximumOfMetric(reallySmallMetric) == small1);
	}
}
