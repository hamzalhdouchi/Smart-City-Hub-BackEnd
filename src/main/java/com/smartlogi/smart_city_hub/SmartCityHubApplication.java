package com.smartlogi.smart_city_hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartCityHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCityHubApplication.class, args);

        System.out.println("||========================================||");
        System.out.println("||      Server is working successfully    ||");
        System.out.println("||========================================||");
    }

}
