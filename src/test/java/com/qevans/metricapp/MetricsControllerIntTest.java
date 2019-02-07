package com.qevans.metricapp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qevans.metricapp.dto.DataDTO;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Category({Unit.class})
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
		
		DataDTO data = new DataDTO();
		data.setValue(1.0);
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Post data to metric
		//Post null metricName
		mockMvc.perform(post(METRIC_URI + "/" + null).content(mapper.writeValueAsString(data)))		
		.andExpect(status().isUnsupportedMediaType());

		String valueString = mapper.writeValueAsString(data);
		
		//Post metric name doesnt exist
		String noExistMetric = "noExistMetric";
		mockMvc.perform(post(METRIC_URI + "/" + noExistMetric).content(valueString).contentType(MediaType.APPLICATION_JSON))		
		.andExpect(status().isBadRequest())
		.andExpect(content().string("Metric Name : " + noExistMetric + " does not exist."));
		
		DataDTO data2 = new DataDTO();
		data2.setValue(2.0);
		
		DataDTO data3 = new DataDTO();
		data3.setValue(3.0);
		
		//Post add data to Metric Name
		mockMvc.perform(post(METRIC_URI + "/" + metricName).content(mapper.writeValueAsString(data)).contentType(MediaType.APPLICATION_JSON))		
		.andExpect(status().isOk())
		.andExpect(content().string(containsString(Double.toString(data.getValue()))));

		//Post 2x data to Metric Name
		mockMvc.perform(post(METRIC_URI + "/" + metricName).content(mapper.writeValueAsString(data2)).contentType(MediaType.APPLICATION_JSON))		
		.andExpect(status().isOk())
		.andExpect(content().string(containsString(Double.toString(data.getValue()))))
		.andExpect(content().string(containsString(Double.toString(data2.getValue()))));
		
		mockMvc.perform(post(METRIC_URI + "/" + metricName).content(mapper.writeValueAsString(data3)).contentType(MediaType.APPLICATION_JSON))		
		.andExpect(status().isOk())
		.andExpect(content().string(containsString(Double.toString(data.getValue()))))
		.andExpect(content().string(containsString(Double.toString(data2.getValue()))))
		.andExpect(content().string(containsString(Double.toString(data3.getValue()))));

		//Get null metricName
		mockMvc.perform(get(METRIC_URI + "/" + null))		
		.andExpect(status().isBadRequest());

		//Get stat null 
		mockMvc.perform(get(METRIC_URI + "/" + metricName + "?"))		
		.andExpect(status().isBadRequest());		
		//Get stat empty
		mockMvc.perform(get(METRIC_URI + "/" + metricName + "?stat="))		
		.andExpect(status().isBadRequest());
		//Get stat mean
		
		double expectedMean = (data.getValue() + data2.getValue() + data3.getValue()) / 3;
		
		mockMvc.perform(get(METRIC_URI + "/" + metricName + "?stat=MeaN"))		
		.andExpect(status().isOk())
		.andExpect(content().string(Double.toString(expectedMean)));
		//Get stat median
		mockMvc.perform(get(METRIC_URI + "/" + metricName + "?stat=MedIan"))		
		.andExpect(status().isOk())
		.andExpect(content().string(Double.toString(data2.getValue())));
		//Get stat min
		mockMvc.perform(get(METRIC_URI + "/" + metricName + "?stat=MiN"))		
		.andExpect(status().isOk())
		.andExpect(content().string(Double.toString(data.getValue())));
		//Get stat max
		mockMvc.perform(get(METRIC_URI + "/" + metricName + "?stat=Max"))		
		.andExpect(status().isOk())
		.andExpect(content().string(Double.toString(data3.getValue())));
		//Get stat doesnt exist
		mockMvc.perform(get(METRIC_URI + "/" + metricName + "?stat=NotReal"))		
		.andExpect(status().isBadRequest())
		.andExpect(content().string("No Supported Statistic Requested. Please add ?stat=mean|median|min|max to url."));		
	}
	
	public static byte[] toByteArray(double value) {
	    byte[] bytes = new byte[8];
	    ByteBuffer.wrap(bytes).putDouble(value);
	    return bytes;
	}
}
