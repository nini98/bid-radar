package com.bidradar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BidRadarBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BidRadarBackendApplication.class, args);
	}

}
