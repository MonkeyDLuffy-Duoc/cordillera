package com.grupocordillera.bff.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bff")
public class BffController {

    private static final Logger log = LoggerFactory.getLogger(BffController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private com.grupocordillera.bff.service.BackendClientService backendClientService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        Long areaId = (Long) request.getAttribute("areaId");
        Long equipoId = (Long) request.getAttribute("equipoId");

        log.info("BFF orquestando consolidado de Dashboard para @{} [Rol: {}, Área: {}, Equipo: {}]", 
                 username, role, areaId != null ? areaId : "Global", equipoId != null ? equipoId : "Global");

        // 1. Inter-service calls with Resilience4j Circuit Breakers
        com.grupocordillera.bff.service.BackendClientService.ServiceResult areasResult = backendClientService.getAreas();
        com.grupocordillera.bff.service.BackendClientService.ServiceResult equiposResult = backendClientService.getEquipos();
        com.grupocordillera.bff.service.BackendClientService.ServiceResult kpisResult = backendClientService.getKpis();
        com.grupocordillera.bff.service.BackendClientService.ServiceResult medicionesResult = backendClientService.getMediciones();
        com.grupocordillera.bff.service.BackendClientService.ServiceResult metasResult = backendClientService.getMetas();

        List<Map<String, Object>> allAreas = areasResult.getData();
        List<Map<String, Object>> allEquipos = equiposResult.getData();
        List<Map<String, Object>> allKpis = kpisResult.getData();
        List<Map<String, Object>> allMediciones = medicionesResult.getData();
        List<Map<String, Object>> allMetas = metasResult.getData();

        // Track statuses to return in the JSON response
        Map<String, Map<String, String>> servicesStatus = new HashMap<>();

        Map<String, String> areasStatus = new HashMap<>();
        areasStatus.put("status", areasResult.getStatus());
        areasStatus.put("message", areasResult.getMessage());
        servicesStatus.put("service-areas", areasStatus);

        Map<String, String> kpisStatus = new HashMap<>();
        kpisStatus.put("status", kpisResult.getStatus());
        kpisStatus.put("message", kpisResult.getMessage());
        servicesStatus.put("service-kpis", kpisStatus);

        Map<String, String> metasStatus = new HashMap<>();
        metasStatus.put("status", metasResult.getStatus());
        metasStatus.put("message", metasResult.getMessage());
        servicesStatus.put("service-metas", metasStatus);

        try {
            // 2. Perform role-based filtering (Security by design at BFF layer)
            List<Map<String, Object>> filteredAreas = allAreas;
            List<Map<String, Object>> filteredEquipos = allEquipos;

            if ("JEFE_AREA".equals(role) && areaId != null) {
                // Filter only to the jefe's area and teams
                filteredAreas = allAreas.stream()
                        .filter(a -> areaId.equals(Long.valueOf(a.get("id").toString())))
                        .collect(Collectors.toList());
                filteredEquipos = allEquipos.stream()
                        .filter(eq -> areaId.equals(Long.valueOf(eq.get("areaId").toString())))
                        .collect(Collectors.toList());
            } else if ("COLABORADOR".equals(role) && equipoId != null) {
                // Filter only to the collaborator's team and their area
                filteredEquipos = allEquipos.stream()
                        .filter(eq -> equipoId.equals(Long.valueOf(eq.get("id").toString())))
                        .collect(Collectors.toList());
                Long colabAreaId = filteredEquipos.isEmpty() ? null : Long.valueOf(filteredEquipos.get(0).get("areaId").toString());
                filteredAreas = allAreas.stream()
                        .filter(a -> colabAreaId != null && colabAreaId.equals(Long.valueOf(a.get("id").toString())))
                        .collect(Collectors.toList());
            }

            // Get list of active equipo IDs and area IDs after filtering
            Set<Long> allowedEquipoIds = filteredEquipos.stream()
                    .map(eq -> Long.valueOf(eq.get("id").toString()))
                    .collect(Collectors.toSet());

            // 3. Orquestate Metas, KPIs and latest measurements
            List<Map<String, Object>> dashboardMetas = new ArrayList<>();
            for (Map<String, Object> meta : allMetas) {
                Long mEquipoId = Long.valueOf(meta.get("equipoId").toString());
                Long mKpiId = Long.valueOf(meta.get("kpiId").toString());

                // Filter goals by team scope
                if (!"ADMIN".equals(role) && !"GERENTE".equals(role) && !allowedEquipoIds.contains(mEquipoId)) {
                    continue;
                }

                // Match with KPI
                Map<String, Object> matchedKpi = allKpis.stream()
                        .filter(k -> mKpiId.equals(Long.valueOf(k.get("id").toString())))
                        .findFirst()
                        .orElse(null);

                if (matchedKpi == null) continue;

                // Match with measurements for this KPI
                List<Map<String, Object>> kpiMediciones = allMediciones.stream()
                        .filter(med -> mKpiId.equals(Long.valueOf(med.get("kpiId").toString())))
                        .sorted(Comparator.comparing(m -> m.get("fechaRegistro").toString()))
                        .collect(Collectors.toList());

                // Calculate latest measurement and goal compliance
                Double valorActual = 0.0;
                String fechaActual = "Sin mediciones";
                if (!kpiMediciones.isEmpty()) {
                    Map<String, Object> latestMed = kpiMediciones.get(kpiMediciones.size() - 1);
                    valorActual = Double.valueOf(latestMed.get("valor").toString());
                    fechaActual = latestMed.get("fechaRegistro").toString();
                }

                Double valorObjetivo = Double.valueOf(meta.get("valorObjetivo").toString());
                Double cumplimiento = 0.0;

                // Compliance logic: time averages are better if lower, standard percentages/finances are better if higher
                String kpiNombre = matchedKpi.get("nombre").toString();
                if (kpiNombre.contains("Tiempo")) {
                    cumplimiento = valorActual <= valorObjetivo ? 100.0 : (valorActual == 0.0 ? 0.0 : (valorObjetivo / valorActual) * 100.0);
                } else {
                    cumplimiento = valorObjetivo == 0.0 ? 0.0 : (valorActual / valorObjetivo) * 100.0;
                }
                // Cap compliance between 0 and 100 for display
                cumplimiento = Math.max(0.0, Math.min(100.0, cumplimiento));

                // Find corresponding team name
                Map<String, Object> matchedEquipo = allEquipos.stream()
                        .filter(eq -> mEquipoId.equals(Long.valueOf(eq.get("id").toString())))
                        .findFirst()
                        .orElse(null);
                String equipoNombre = matchedEquipo != null ? matchedEquipo.get("nombre").toString() : "Equipo General";

                Map<String, Object> metaData = new HashMap<>();
                metaData.put("id", meta.get("id"));
                metaData.put("kpiNombre", kpiNombre);
                metaData.put("kpiTipo", matchedKpi.get("tipo"));
                metaData.put("unidadMedida", matchedKpi.get("unidadMedida"));
                metaData.put("equipoNombre", equipoNombre);
                metaData.put("valorObjetivo", valorObjetivo);
                metaData.put("valorActual", valorActual);
                metaData.put("fechaUltimaMedicion", fechaActual);
                metaData.put("cumplimiento", Math.round(cumplimiento * 10.0) / 10.0); // round to 1 decimal
                metaData.put("estado", complianceStatus(cumplimiento));

                dashboardMetas.add(metaData);
            }

            // 4. Construct final aggregated object response
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("username", username);
            dashboard.put("role", role);
            dashboard.put("areas", filteredAreas);
            dashboard.put("equipos", filteredEquipos);
            dashboard.put("kpis", allKpis);
            dashboard.put("metasReporte", dashboardMetas);
            dashboard.put("medicionesHistoricas", allMediciones);
            dashboard.put("servicesStatus", servicesStatus);

            log.info("BFF consolidó con éxito el Dashboard para @{}.", username);
            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            log.error("[ERR-BFF-504] Error en BFF al consolidar el Dashboard para @{}: {}", username, e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error en BFF al consolidar el Dashboard: " + e.getMessage());
        }
    }

    @PostMapping("/metas")
    public ResponseEntity<?> createMeta(@RequestBody Map<String, Object> metaRequest, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        log.info("BFF recibiendo petición de @{} [Rol: {}] para CREAR meta: {}", username, role, metaRequest);

        if (!"ADMIN".equals(role) && !"JEFE_AREA".equals(role)) {
            log.warn("[ERR-SEC-403] Acceso denegado: El usuario @{} [Rol: {}] intentó crear una meta sin permisos.", username, role);
            return ResponseEntity.status(403).body("Acceso denegado: Solo administradores o jefes de área pueden crear metas.");
        }
        
        try {
            ResponseEntity<?> response = restTemplate.postForEntity("http://service-metas/api/metas", metaRequest, Object.class);
            log.info("BFF redirigió creación de meta con éxito. Status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warn("[ERR-VAL-400] Fallo en servicio metas al crear: {}", e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[ERR-BFF-502] Error en BFF al crear meta: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error en BFF al crear la meta: " + e.getMessage());
        }
    }

    @PutMapping("/metas/{id}")
    public ResponseEntity<?> updateMeta(@PathVariable Long id, @RequestBody Map<String, Object> metaRequest, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        log.info("BFF recibiendo petición de @{} [Rol: {}] para EDITAR meta ID {}: {}", username, role, id, metaRequest);

        if (!"ADMIN".equals(role) && !"JEFE_AREA".equals(role)) {
            log.warn("[ERR-SEC-403] Acceso denegado: El usuario @{} [Rol: {}] intentó editar la meta ID {} sin permisos.", username, role, id);
            return ResponseEntity.status(403).body("Acceso denegado: Solo administradores o jefes de área pueden editar metas.");
        }
        
        try {
            org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(metaRequest);
            ResponseEntity<?> response = restTemplate.exchange(
                "http://service-metas/api/metas/" + id,
                org.springframework.http.HttpMethod.PUT,
                entity,
                Object.class
            );
            log.info("BFF redirigió edición de meta ID {} con éxito. Status: {}", id, response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warn("[ERR-VAL-400] Fallo en servicio metas al editar ID {}: {}", id, e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[ERR-BFF-502] Error en BFF al actualizar meta ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(500).body("Error en BFF al actualizar la meta: " + e.getMessage());
        }
    }

    @DeleteMapping("/metas/{id}")
    public ResponseEntity<?> deleteMeta(@PathVariable Long id, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        log.info("BFF recibiendo petición de @{} [Rol: {}] para ELIMINAR meta ID {}", username, role, id);

        if (!"ADMIN".equals(role) && !"JEFE_AREA".equals(role)) {
            log.warn("[ERR-SEC-403] Acceso denegado: El usuario @{} [Rol: {}] intentó eliminar la meta ID {} sin permisos.", username, role, id);
            return ResponseEntity.status(403).body("Acceso denegado: Solo administradores o jefes de área pueden eliminar metas.");
        }
        
        try {
            ResponseEntity<?> response = restTemplate.exchange(
                "http://service-metas/api/metas/" + id,
                org.springframework.http.HttpMethod.DELETE,
                null,
                Void.class
            );
            log.info("BFF redirigió eliminación de meta ID {} con éxito. Status: {}", id, response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).build();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warn("[ERR-VAL-400] Fallo en servicio metas al eliminar ID {}: {}", id, e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[ERR-BFF-502] Error en BFF al eliminar meta ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(500).body("Error en BFF al eliminar la meta: " + e.getMessage());
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> getAllUsuarios(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        log.info("BFF recibiendo petición de @{} [Rol: {}] para LISTAR usuarios MySQL.", username, role);

        if (!"ADMIN".equals(role)) {
            log.warn("[ERR-SEC-403] Acceso denegado: El usuario @{} [Rol: {}] intentó listar usuarios sin ser ADMIN.", username, role);
            return ResponseEntity.status(403).body("Acceso denegado: Solo el administrador puede gestionar usuarios.");
        }
        try {
            com.grupocordillera.bff.service.BackendClientService.ServiceResult result = backendClientService.getUsuarios();
            if ("DEGRADADO".equals(result.getStatus())) {
                log.warn("[BFF-WARN] Intento de listar usuarios pero el servicio service-areas está DEGRADADO.");
                return ResponseEntity.status(503).body(result.getMessage());
            }
            log.info("BFF obtuvo exitosamente la lista de usuarios. Total: {}", result.getData().size());
            return ResponseEntity.ok(result.getData());
        } catch (Exception e) {
            log.error("[ERR-BFF-501] Error en BFF al listar usuarios: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error al obtener usuarios: " + e.getMessage());
        }
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> createUsuario(@RequestBody Map<String, Object> userRequest, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        log.info("BFF recibiendo petición de @{} [Rol: {}] para CREAR nuevo usuario: {}", 
                 username, role, userRequest.get("username"));

        if (!"ADMIN".equals(role)) {
            log.warn("[ERR-SEC-403] Acceso denegado: El usuario @{} [Rol: {}] intentó crear un usuario sin ser ADMIN.", username, role);
            return ResponseEntity.status(403).body("Acceso denegado: Solo el administrador puede crear usuarios.");
        }
        try {
            ResponseEntity<?> response = restTemplate.postForEntity("http://service-areas/api/usuarios", userRequest, Object.class);
            log.info("BFF redirigió registro de usuario con éxito. Status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warn("[ERR-VAL-400] Fallo en servicio áreas al crear usuario: {}", e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[ERR-BFF-501] Error en BFF al crear usuario: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error al crear usuario: " + e.getMessage());
        }
    }

    @PutMapping("/usuarios/{username}")
    public ResponseEntity<?> updateUsuario(@PathVariable String username, @RequestBody Map<String, Object> userRequest, HttpServletRequest request) {
        String bffAdmin = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        log.info("BFF recibiendo petición de @{} [Rol: {}] para EDITAR usuario @{}: {}", 
                 bffAdmin, role, username, userRequest);

        if (!"ADMIN".equals(role)) {
            log.warn("[ERR-SEC-403] Acceso denegado: El usuario @{} [Rol: {}] intentó editar al usuario @{} sin ser ADMIN.", bffAdmin, role, username);
            return ResponseEntity.status(403).body("Acceso denegado: Solo el administrador puede editar usuarios.");
        }
        try {
            org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(userRequest);
            ResponseEntity<?> response = restTemplate.exchange(
                "http://service-areas/api/usuarios/" + username,
                org.springframework.http.HttpMethod.PUT,
                entity,
                Object.class
            );
            log.info("BFF redirigió edición de usuario @{} con éxito. Status: {}", username, response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warn("[ERR-VAL-400] Fallo en servicio áreas al actualizar usuario @{}: {}", username, e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[ERR-BFF-501] Error en BFF al actualizar usuario @{}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    @DeleteMapping("/usuarios/{username}")
    public ResponseEntity<?> deleteUsuario(@PathVariable String username, HttpServletRequest request) {
        String bffAdmin = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        log.info("BFF recibiendo petición de @{} [Rol: {}] para ELIMINAR usuario @{}", bffAdmin, role, username);

        if (!"ADMIN".equals(role)) {
            log.warn("[ERR-SEC-403] Acceso denegado: El usuario @{} [Rol: {}] intentó eliminar al usuario @{} sin ser ADMIN.", bffAdmin, role, username);
            return ResponseEntity.status(403).body("Acceso denegado: Solo el administrador puede eliminar usuarios.");
        }
        try {
            restTemplate.delete("http://service-areas/api/usuarios/" + username);
            log.info("BFF redirigió eliminación de usuario @{} con éxito.", username);
            return ResponseEntity.ok().body("{\"message\": \"Usuario eliminado con éxito.\"}");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warn("[ERR-VAL-400] Fallo en servicio áreas al eliminar usuario @{}: {}", username, e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[ERR-BFF-501] Error en BFF al eliminar usuario @{}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body("Error al eliminar usuario: " + e.getMessage());
        }
    }

    private String complianceStatus(double compliance) {
        if (compliance >= 95.0) {
            return "CUMPLIDA";
        } else if (compliance >= 75.0) {
            return "EN_PROGRESO";
        } else {
            return "CRITICA";
        }
    }
}
