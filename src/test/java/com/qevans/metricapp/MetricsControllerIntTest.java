package com.qevans.metricapp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.*;

import junit.framework.TestCase;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MetricsControllerIntTest {

	@Autowired
	private MetricController metricController;
	
	private MockMvc mockMvc;
	
	@Before
	public void setUp()
	{
		this.mockMvc = MockMvcBuilders.standaloneSetup(metricController).build();
	}
	
	private final String METRIC_URI = "/metric";
		
	
	@Test
	public void getAndPostMetricTest() throws Exception
	{
		//Get metric with nothing there
		mockMvc.perform(get(METRIC_URI))		
		.andExpect(status().isOk())
		.andExpect(content().string("[]"));
		
		//Post null metric
		mockMvc.perform(post(METRIC_URI))		
		.andExpect(status().isBadRequest());
		
		//Post empty metric
		mockMvc.perform(post(METRIC_URI).content(""))		
		.andExpect(status().isBadRequest());		
		//Post special char metric
		String specialCharMetricName = "/!@#$%.";
		
		mockMvc.perform(post(METRIC_URI).content(specialCharMetricName))		
		.andExpect(status().isCreated())
		.andExpect(content().string(specialCharMetricName));
		
		//Post regular metric
		String metricName = "metric";
		mockMvc.perform(post(METRIC_URI).content(metricName))		
		.andExpect(status().isCreated())
		.andExpect(content().string(metricName));
		
		//Post duplicate metric
		mockMvc.perform(post(METRIC_URI).content(metricName))		
		.andExpect(status().isExpectationFailed())
		.andExpect(content().string("Metric Already Exists."));
		
		//Get metric with Entries
		mockMvc.perform(get(METRIC_URI))		
		.andExpect(status().isOk())
		.andExpect(content().string(containsString(specialCharMetricName)))
		.andExpect(content().string(containsString(metricName)));
		
		//Post data to metric
		//Post null metricName
		//Post empty metricName
		//Post metric name doesnt exist
		//Post add data to Metric Name
		//Post 2x data to Metric Name
		
		//Get null metricName
		//Get empty Metric Name
		//Get stat null 
		//Get stat empty
		//Get stat mean
		//Get stat median
		//Get stat min
		//Get stat max
	}
}
