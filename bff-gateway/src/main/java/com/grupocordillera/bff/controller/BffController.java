package com.grupocordillera.bff.controller;

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

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        Long areaId = (Long) request.getAttribute("areaId");
        Long equipoId = (Long) request.getAttribute("equipoId");

        try {
            // 1. Inter-service calls to pull raw data from microservices
            List<Map<String, Object>> allAreas = restTemplate.getForObject("http://service-areas/api/areas", List.class);
            List<Map<String, Object>> allEquipos = restTemplate.getForObject("http://service-areas/api/equipos", List.class);
            List<Map<String, Object>> allKpis = restTemplate.getForObject("http://service-kpis/api/kpis", List.class);
            List<Map<String, Object>> allMediciones = restTemplate.getForObject("http://service-kpis/api/mediciones", List.class);
            List<Map<String, Object>> allMetas = restTemplate.getForObject("http://service-metas/api/metas", List.class);

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
                    cumplimiento = valorActual <= valorObjetivo ? 100.0 : (valorObjetivo / valorActual) * 100.0;
                } else {
                    cumplimiento = (valorActual / valorObjetivo) * 100.0;
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

            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error en BFF al consolidar el Dashboard: " + e.getMessage());
        }
    }

    @PostMapping("/metas")
    public ResponseEntity<?> createMeta(@RequestBody Map<String, Object> metaRequest, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role) && !"JEFE_AREA".equals(role)) {
            return ResponseEntity.status(403).body("Acceso denegado: Solo administradores o jefes de área pueden crear metas.");
        }
        
        try {
            ResponseEntity<?> response = restTemplate.postForEntity("http://service-metas/api/metas", metaRequest, Object.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en BFF al crear la meta: " + e.getMessage());
        }
    }

    @PutMapping("/metas/{id}")
    public ResponseEntity<?> updateMeta(@PathVariable Long id, @RequestBody Map<String, Object> metaRequest, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role) && !"JEFE_AREA".equals(role)) {
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
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en BFF al actualizar la meta: " + e.getMessage());
        }
    }

    @DeleteMapping("/metas/{id}")
    public ResponseEntity<?> deleteMeta(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role) && !"JEFE_AREA".equals(role)) {
            return ResponseEntity.status(403).body("Acceso denegado: Solo administradores o jefes de área pueden eliminar metas.");
        }
        
        try {
            ResponseEntity<?> response = restTemplate.exchange(
                "http://service-metas/api/metas/" + id,
                org.springframework.http.HttpMethod.DELETE,
                null,
                Void.class
            );
            return ResponseEntity.status(response.getStatusCode()).build();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en BFF al eliminar la meta: " + e.getMessage());
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> getAllUsuarios(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Acceso denegado: Solo el administrador puede gestionar usuarios.");
        }
        try {
            List<?> usuarios = restTemplate.getForObject("http://service-areas/api/usuarios", List.class);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener usuarios: " + e.getMessage());
        }
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> createUsuario(@RequestBody Map<String, Object> userRequest, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Acceso denegado: Solo el administrador puede crear usuarios.");
        }
        try {
            ResponseEntity<?> response = restTemplate.postForEntity("http://service-areas/api/usuarios", userRequest, Object.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al crear usuario: " + e.getMessage());
        }
    }

    @PutMapping("/usuarios/{username}")
    public ResponseEntity<?> updateUsuario(@PathVariable String username, @RequestBody Map<String, Object> userRequest, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
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
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    @DeleteMapping("/usuarios/{username}")
    public ResponseEntity<?> deleteUsuario(@PathVariable String username, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Acceso denegado: Solo el administrador puede eliminar usuarios.");
        }
        try {
            restTemplate.delete("http://service-areas/api/usuarios/" + username);
            return ResponseEntity.ok().body("{\"message\": \"Usuario eliminado con éxito.\"}");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
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
