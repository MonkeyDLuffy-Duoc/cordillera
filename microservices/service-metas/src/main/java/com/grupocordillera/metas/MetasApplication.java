package com.grupocordillera.metas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MetasApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetasApplication.class, args);
    }
}
