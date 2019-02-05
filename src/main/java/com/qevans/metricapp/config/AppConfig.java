package com.qevans.metricapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.qevans.metricapp.repository.MetricsRepository;

@Configuration
public class AppConfig {

	@Bean
	public MetricsRepository metricsRepository()
	{
		return new MetricsRepository();
	}
	
}
