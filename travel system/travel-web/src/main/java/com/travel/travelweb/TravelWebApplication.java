package com.travel.travelweb;

import com.travel.travelweb.config.AliyunSmsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AliyunSmsProperties.class)
public class TravelWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelWebApplication.class, args);
    }
}
