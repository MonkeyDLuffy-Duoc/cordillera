package com.grupocordillera.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Clase principal de inicio para el Backend For Frontend (BFF) - Grupo Cordillera.
 * Habilita el descubrimiento y registro automático con Eureka.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BffApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }
}
