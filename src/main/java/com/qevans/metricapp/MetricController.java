package com.qevans.metricapp;

import org.springframework.web.bind.annotation.RestController;

import com.qevans.metricapp.dto.DataDTO;
import com.qevans.metricapp.repository.IMetricsRepository;

import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.annotations.*;

@RestController
public class MetricController {

	@Autowired
	private IMetricsRepository metricsRepository;

    @ApiOperation(value = "Add a metric")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created metric"),
            @ApiResponse(code = 400, message = "Metric was null or empty"),
            @ApiResponse(code = 417, message = "Tried to add a duplicate metric"),            
    })
	@PostMapping("/metric")
	public ResponseEntity<String> index(@RequestBody String newMetric) {

		if(newMetric == null || newMetric.isEmpty())
		{
			return ResponseEntity.badRequest().body("Metric cannot be null or empty");
		}
		
		if (metricsRepository.addMetric(newMetric)) {
			return ResponseEntity.status(HttpStatus.CREATED).body(newMetric);
		}
		
		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Metric Already Exists.");
	}
    
    @ApiOperation(value = "Get list of all available metrics")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved metrics")            
    })
	@GetMapping("/metric")
	public ResponseEntity<String[]> getAllMetrics() {

		String[] metricsList = metricsRepository.getAllMetrics();

		return ResponseEntity.ok(metricsList);
	}

    @ApiOperation(value = "Add data to specified metric")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "added data successfully"),
            @ApiResponse(code = 400, message = "metric specified was null or did not exist")
    })
	@PostMapping("/metric/{metricName}")
	public ResponseEntity<?> addDataToMetric(@PathVariable String metricName, @RequestBody DataDTO data) {
		if(metricName == null)
		{
			return ResponseEntity.badRequest().body("Metric Name cannot be null.");
		}
		
		if (!metricsRepository.addDataToMetric(metricName, data.getValue())) {
			return ResponseEntity.badRequest().body("Metric Name : " + metricName + " does not exist.");
		}

		return ResponseEntity.ok().body(metricsRepository.getDataForMetric(metricName));
	}

    @ApiOperation(value = "Get statistic (mean, median, min, max) for given metric")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "got desired statistic for metric successfully"),
            @ApiResponse(code = 400, message = "metric specified was null or did not exist or statistic requested did not exist, was null, or was empty")
    })
	@GetMapping("/metric/{metricName}")
	public ResponseEntity<?> getMetricStatistic(@PathVariable String metricName,
			@RequestParam("stat") String requestedStatistic) {
		
		if (metricName == null || metricName.isEmpty())
		{
			ResponseEntity.badRequest().body("Metric Name cannot be null or empty");
		}
		
		//Handle if requestedStatistic is null or empty
		
		if(requestedStatistic == null || requestedStatistic.isEmpty())
		{
			try
			{
				ResponseEntity.ok(metricsRepository.getDataForMetric(metricName));
			}
			catch (IllegalArgumentException ex)
			{
				//log it
				ResponseEntity.badRequest().body(ex.getMessage());
			}			
		}
		
		double result = 0.0;
		
		try {
			if (requestedStatistic.equalsIgnoreCase("mean")) {
				result = metricsRepository.getAverageOfMetric(metricName);
			} else if (requestedStatistic.equalsIgnoreCase("median")) {
				result = metricsRepository.getMedianOfMetric(metricName);
			} else if (requestedStatistic.equalsIgnoreCase("min")) {
				result = metricsRepository.getMinimumOfMetric(metricName);
			} else if (requestedStatistic.equalsIgnoreCase("max")) {
				result = metricsRepository.getMaximumOfMetric(metricName);
			} else {
				return ResponseEntity.badRequest().body("No Supported Statistic Requested. Please add ?stat=mean|median|min|max to url.");
			}
		} catch (IllegalArgumentException ex) {
			System.out.println();
			return ResponseEntity.badRequest().body(ex.getMessage());
		}

		return ResponseEntity.ok(result);
	}
}