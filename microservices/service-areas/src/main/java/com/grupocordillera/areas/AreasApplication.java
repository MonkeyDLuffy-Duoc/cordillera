package com.grupocordillera.areas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AreasApplication {
    public static void main(String[] args) {
        SpringApplication.run(AreasApplication.class, args);
    }
}
