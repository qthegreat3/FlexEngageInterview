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
}
