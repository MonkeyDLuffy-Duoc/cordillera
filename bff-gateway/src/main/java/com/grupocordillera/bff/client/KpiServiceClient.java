package com.grupocordillera.bff.client;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Cliente de servicios simulado para conectar el BFF con el microservicio de KPIs.
 * Contiene mecanismos para inducir fallos artificiales y evaluar el comportamiento del Circuit Breaker.
 */
@Service
public class KpiServiceClient {

    private final Random random = new Random();

    /**
     * Obtiene el resumen de KPIs consolidado.
     * @param failIfTrue Si es verdadero, fuerza una excepción para probar el Circuit Breaker.
     * @return Mapa con la estructura de KPIs.
     */
    public Map<String, Object> getKpiSummary(boolean failIfTrue) {
        if (failIfTrue) {
            throw new RuntimeException("Error de comunicación HTTP: Conexión rechazada por el microservicio de KPIs (Simulado)");
        }

        // Datos de KPIs simulados para Grupo Cordillera
        Map<String, Object> kpiData = new HashMap<>();
        kpiData.put("ventasMensualesActuales", 85000000); // 85 millones
        kpiData.put("metaVentasMensuales", 100000000); // 100 millones
        kpiData.put("cumplimientoVentasPercent", 85.0);
        kpiData.put("estadoVentasKpi", "Amarillo"); // Alerta
        
        kpiData.put("quiebreStockProductos", 4.2); // 4.2% de productos rotos
        kpiData.put("estadoInventarioKpi", "Verde"); // Excelente
        
        kpiData.put("tiempoEntregaLogisticaDias", 3.8); // Meta es < 3 dias
        kpiData.put("estadoLogisticaKpi", "Rojo"); // Crítico
        
        kpiData.put("source", "Microservicio de KPIs en Tiempo Real (Operando OK)");

        return kpiData;
    }
}
