package com.grupocordillera.metas.controller;

import com.grupocordillera.metas.model.Meta;
import com.grupocordillera.metas.repository.MetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/metas")
public class MetaController {

    @Autowired
    private MetaRepository metaRepository;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public List<Meta> getAllMetas() {
        return metaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meta> getMetaById(@PathVariable Long id) {
        return metaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/equipo/{equipoId}")
    public List<Meta> getMetasByEquipoId(@PathVariable Long equipoId) {
        return metaRepository.findByEquipoId(equipoId);
    }

    /**
     * Creates a Goal (Meta), validating that the associated KPI exists via inter-service call to service-kpis.
     */
    @PostMapping
    public ResponseEntity<?> createMeta(@RequestBody Meta meta) {
        if (meta.getKpiId() == null || meta.getEquipoId() == null || meta.getValorObjetivo() == null || meta.getFechaLimite() == null) {
            return ResponseEntity.badRequest().body("Los campos 'kpiId', 'equipoId', 'valorObjetivo' y 'fechaLimite' son requeridos.");
        }

        // Inter-service call to check KPI existence
        String kpiServiceUrl = "http://service-kpis/api/kpis/" + meta.getKpiId();
        try {
            ResponseEntity<?> response = restTemplate.getForEntity(kpiServiceUrl, Object.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El KPI con ID " + meta.getKpiId() + " no está activo o disponible.");
            }
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error de validación: El KPI con ID " + meta.getKpiId() + " no existe.");
        } catch (Exception e) {
            // Fallback in case KPI service is down (Circuit Breaker logic simulated/graceful fallback)
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo validar el KPI porque el servicio de KPIs no está disponible en este momento. Inténtelo más tarde.");
        }

        Meta savedMeta = metaRepository.save(meta);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMeta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meta> updateMeta(@PathVariable Long id, @RequestBody Meta metaDetails) {
        return metaRepository.findById(id)
                .map(meta -> {
                    meta.setValorObjetivo(metaDetails.getValorObjetivo());
                    meta.setFechaLimite(metaDetails.getFechaLimite());
                    meta.setEstado(metaDetails.getEstado());
                    Meta updatedMeta = metaRepository.save(meta);
                    return ResponseEntity.ok(updatedMeta);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeta(@PathVariable Long id) {
        return metaRepository.findById(id)
                .map(meta -> {
                    metaRepository.delete(meta);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
