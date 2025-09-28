package com.disaster.emergency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.disaster.emergency"})
public class EmergencyPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmergencyPlatformApplication.class, args);
    }
}
