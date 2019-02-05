package com.qevans.metricapp;

import org.springframework.web.bind.annotation.RestController;

import com.qevans.metricapp.repository.IMetricsRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class MetricController {

	@Autowired
	private IMetricsRepository metricsRepository;

	@PostMapping("/metric")
	public String index(@RequestBody String newMetric) {

		// Validation???

		metricsRepository.addMetric(newMetric);

		return newMetric;
	}

	@GetMapping("/metric")
	public ResponseEntity<String[]> getAllMetrics() {

		String[] metricsList = metricsRepository.getAllMetrics();

		return ResponseEntity.ok(metricsList);
	}

	@PostMapping("/metric/{metricName}")
	public ResponseEntity<?> addDataToMetric(@PathVariable String metricName, @RequestBody double data) {
		if (!metricsRepository.addDataToMetric(metricName, data)) {
			return ResponseEntity.badRequest().body("Metric Name : " + metricName + " does not exist.");
		}

		return ResponseEntity.ok().body(metricsRepository.getDataForMetric(metricName));
	}

	@GetMapping("/metric/{metricName}")
	public ResponseEntity<?> getMetricStatistic(@PathVariable String metricName,
			@RequestParam("stat") String requestedStatistic) {
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
				return ResponseEntity.badRequest().body("No Supported Statistic Requested.");
			}
		} catch (IllegalArgumentException ex) {
			System.out.println();
			return ResponseEntity.badRequest().body(ex.getMessage());
		}

		return ResponseEntity.ok(result);
	}
}