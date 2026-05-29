package com.grupocordillera.kpis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class KpisApplication {
    public static void main(String[] args) {
        SpringApplication.run(KpisApplication.class, args);
    }
}
