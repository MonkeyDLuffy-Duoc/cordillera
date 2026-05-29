package com.grupocordillera.metas.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class KpiValidationService {

    private static final Logger log = LoggerFactory.getLogger(KpiValidationService.class);

    @Autowired
    private RestTemplate restTemplate;

    @CircuitBreaker(name = "kpiService", fallbackMethod = "fallbackValidateKpi")
    public boolean validateKpiExists(Long kpiId) {
        log.info("Iniciando validación de existencia para KPI ID: {}", kpiId);
        String kpiServiceUrl = "http://service-kpis/api/kpis/" + kpiId;
        
        // This will make the REST call
        restTemplate.getForObject(kpiServiceUrl, Object.class);
        log.info("Validación exitosa: El KPI ID {} existe y está activo.", kpiId);
        return true;
    }

    // Fallback method when service-kpis is offline or slow
    public boolean fallbackValidateKpi(Long kpiId, Throwable t) {
        log.error("[Circuit Breaker: kpiService] Activado fallback por falla en comunicación con 'service-kpis'. Causa: {}", t.getMessage());
        log.warn("[Circuit Breaker: kpiService] Degradación de servicio: Se autoriza la creación de la meta para KPI ID: {} bajo contingencia técnica.", kpiId);
        return true;
    }
}
