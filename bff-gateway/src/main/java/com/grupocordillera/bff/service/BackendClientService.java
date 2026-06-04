package com.grupocordillera.bff.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BackendClientService {

    private static final Logger log = LoggerFactory.getLogger(BackendClientService.class);

    @Autowired
    private RestTemplate restTemplate;

    // --- SERVICE RESULTS CLASS ---
    public static class ServiceResult {
        private final List<Map<String, Object>> data;
        private final String status;
        private final String message;

        public ServiceResult(List<Map<String, Object>> data, String status, String message) {
            this.data = data;
            this.status = status;
            this.message = message;
        }

        public List<Map<String, Object>> getData() { return data; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }

    // --- SERVICE AREAS CALLS ---
    @CircuitBreaker(name = "areasService", fallbackMethod = "fallbackAreas")
    public ServiceResult getAreas() {
        List<Map<String, Object>> data = restTemplate.getForObject("http://service-areas/api/areas", List.class);
        return new ServiceResult(data != null ? data : new ArrayList<>(), "OK", "Servicio operativo");
    }

    @CircuitBreaker(name = "areasService", fallbackMethod = "fallbackEquipos")
    public ServiceResult getEquipos() {
        List<Map<String, Object>> data = restTemplate.getForObject("http://service-areas/api/equipos", List.class);
        return new ServiceResult(data != null ? data : new ArrayList<>(), "OK", "Servicio operativo");
    }

    @CircuitBreaker(name = "areasService", fallbackMethod = "fallbackUsuarios")
    public ServiceResult getUsuarios() {
        List<Map<String, Object>> data = restTemplate.getForObject("http://service-areas/api/usuarios", List.class);
        return new ServiceResult(data != null ? data : new ArrayList<>(), "OK", "Servicio operativo");
    }

    // --- SERVICE KPIS CALLS ---
    @CircuitBreaker(name = "kpisService", fallbackMethod = "fallbackKpis")
    public ServiceResult getKpis() {
        List<Map<String, Object>> data = restTemplate.getForObject("http://service-kpis/api/kpis", List.class);
        return new ServiceResult(data != null ? data : new ArrayList<>(), "OK", "Servicio operativo");
    }

    @CircuitBreaker(name = "kpisService", fallbackMethod = "fallbackMediciones")
    public ServiceResult getMediciones() {
        List<Map<String, Object>> data = restTemplate.getForObject("http://service-kpis/api/mediciones", List.class);
        return new ServiceResult(data != null ? data : new ArrayList<>(), "OK", "Servicio operativo");
    }

    // --- SERVICE METAS CALLS ---
    @CircuitBreaker(name = "metasService", fallbackMethod = "fallbackMetas")
    public ServiceResult getMetas() {
        List<Map<String, Object>> data = restTemplate.getForObject("http://service-metas/api/metas", List.class);
        return new ServiceResult(data != null ? data : new ArrayList<>(), "OK", "Servicio operativo");
    }

    // --- FALLBACK METHODS ---
    public ServiceResult fallbackAreas(Throwable t) {
        log.error("[BFF-CB-FALLBACK] Disyuntor activo para service-areas (Áreas). Causa: {}", t.getMessage());
        return new ServiceResult(new ArrayList<>(), "DEGRADADO", "El servicio de Estructura Organizativa (Áreas) no está disponible.");
    }

    public ServiceResult fallbackEquipos(Throwable t) {
        log.error("[BFF-CB-FALLBACK] Disyuntor activo para service-areas (Equipos). Causa: {}", t.getMessage());
        return new ServiceResult(new ArrayList<>(), "DEGRADADO", "El servicio de Estructura Organizativa (Equipos) no está disponible.");
    }

    public ServiceResult fallbackUsuarios(Throwable t) {
        log.error("[BFF-CB-FALLBACK] Disyuntor activo para service-areas (Usuarios). Causa: {}", t.getMessage());
        return new ServiceResult(new ArrayList<>(), "DEGRADADO", "El servicio de usuarios no está disponible.");
    }

    public ServiceResult fallbackKpis(Throwable t) {
        log.error("[BFF-CB-FALLBACK] Disyuntor activo para service-kpis (KPIs). Causa: {}", t.getMessage());
        return new ServiceResult(new ArrayList<>(), "DEGRADADO", "El servicio de Gestión de KPIs (Catálogo) no está disponible.");
    }

    public ServiceResult fallbackMediciones(Throwable t) {
        log.error("[BFF-CB-FALLBACK] Disyuntor activo para service-kpis (Mediciones). Causa: {}", t.getMessage());
        return new ServiceResult(new ArrayList<>(), "DEGRADADO", "El servicio de Gestión de KPIs (Mediciones) no está disponible.");
    }

    public ServiceResult fallbackMetas(Throwable t) {
        log.error("[BFF-CB-FALLBACK] Disyuntor activo para service-metas (Metas). Causa: {}", t.getMessage());
        return new ServiceResult(new ArrayList<>(), "DEGRADADO", "El servicio de Metas Organizacionales no está disponible.");
    }
}
