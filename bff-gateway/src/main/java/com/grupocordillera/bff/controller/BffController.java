package com.grupocordillera.bff.controller;

import com.grupocordillera.bff.client.KpiServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST del BFF que actúa como agregador de APIs.
 * Implementa tolerancia a fallos mediante el patrón Circuit Breaker de Resilience4j.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class BffController {

    @Autowired
    private KpiServiceClient kpiServiceClient;

    /**
     * Obtiene las métricas consolidadas para el frontend de Grupo Cordillera.
     * La anotación @CircuitBreaker intercepta esta llamada y la evalúa contra las políticas del application.yml.
     * 
     * @param fail Parámetro para forzar un fallo HTTP y demostrar el Circuit Breaker.
     * @return Respuesta HTTP con el resumen de métricas.
     */
    @GetMapping("/metrics")
    @CircuitBreaker(name = "kpiService", fallbackMethod = "getKpiSummaryFallback")
    public ResponseEntity<Map<String, Object>> getMetrics(@RequestParam(defaultValue = "false") boolean fail) {
        // Llama al microservicio (simulado) de KPIs
        Map<String, Object> metrics = kpiServiceClient.getKpiSummary(fail);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Método Fallback de Contingencia.
     * Se ejecuta automáticamente cuando:
     *   1. El microservicio de KPIs lanza una excepción (circuito cerrado pero fallando).
     *   2. El circuito está ABIERTO (se bloquea la llamada directa y se desvía inmediatamente aquí).
     * 
     * Nota académica: La firma del método fallback debe coincidir exactamente con el método original
     * y recibir un parámetro Throwable al final que capture la causa de la falla.
     */
    public ResponseEntity<Map<String, Object>> getKpiSummaryFallback(boolean fail, Throwable t) {
        Map<String, Object> fallbackData = new HashMap<>();
        
        // Datos de contingencia históricos de la última hora (Simulando caché Redis)
        fallbackData.put("ventasMensualesActuales", 84900000); 
        fallbackData.put("metaVentasMensuales", 100000000);
        fallbackData.put("cumplimientoVentasPercent", 84.9);
        fallbackData.put("estadoVentasKpi", "Amarillo");
        
        fallbackData.put("quiebreStockProductos", 4.3);
        fallbackData.put("estadoInventarioKpi", "Verde");
        
        fallbackData.put("tiempoEntregaLogisticaDias", 3.9);
        fallbackData.put("estadoLogisticaKpi", "Rojo");

        // Mensaje de alerta para informar en la UI de la caída
        fallbackData.put("source", "Caché de Contingencia (BFF) - El microservicio de KPIs no responde.");
        fallbackData.put("errorCause", t.getMessage());
        fallbackData.put("circuitBreakerTriggered", true);

        return ResponseEntity.ok(fallbackData);
    }
}
