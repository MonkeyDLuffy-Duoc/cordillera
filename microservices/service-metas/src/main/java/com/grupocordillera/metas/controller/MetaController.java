package com.grupocordillera.metas.controller;

import com.grupocordillera.metas.model.Meta;
import com.grupocordillera.metas.repository.MetaRepository;
import com.grupocordillera.metas.service.KpiValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
@RequestMapping("/api/metas")
public class MetaController {

    private static final Logger log = LoggerFactory.getLogger(MetaController.class);

    @Autowired
    private MetaRepository metaRepository;

    @Autowired
    private KpiValidationService kpiValidationService;

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
     * Protected by Resilience4j Circuit Breaker.
     */
    @PostMapping
    public ResponseEntity<?> createMeta(@RequestBody Meta meta) {
        log.info("Petición de negocio recibida para crear Meta: KPI ID = {}, Equipo ID = {}, Objetivo = {}", 
                 meta.getKpiId(), meta.getEquipoId(), meta.getValorObjetivo());

        if (meta.getKpiId() == null || meta.getEquipoId() == null || meta.getValorObjetivo() == null || meta.getFechaLimite() == null) {
            log.warn("[ERR-VAL-400] Fallo al crear Meta: Faltan campos requeridos en el RequestBody.");
            return ResponseEntity.badRequest().body("Los campos 'kpiId', 'equipoId', 'valorObjetivo' y 'fechaLimite' son requeridos.");
        }

        try {
            // Dynamic check protected by Resilience4j Circuit Breaker
            kpiValidationService.validateKpiExists(meta.getKpiId());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("[ERR-VAL-400] Validación fallida: El KPI ID {} no existe en la base de datos MySQL (kpis_db).", meta.getKpiId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error de validación: El KPI con ID " + meta.getKpiId() + " no existe.");
        } catch (Exception e) {
            log.error("[ERR-BFF-503] Excepción inesperada al validar la existencia del KPI: {}", e.getMessage());
        }

        try {
            Meta savedMeta = metaRepository.save(meta);
            log.info("Meta creada con éxito. Guardada con ID = {}, Estado inicial = {} en MySQL.", 
                     savedMeta.getId(), savedMeta.getEstado());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMeta);
        } catch (Exception e) {
            log.error("[ERR-DB-500] Error al guardar la meta en MySQL (metas_db). Detalles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error de persistencia.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeta(@PathVariable Long id, @RequestBody Meta metaDetails) {
        log.info("Petición de negocio recibida para actualizar Meta ID: {}", id);
        return metaRepository.findById(id)
                .map(meta -> {
                    try {
                        meta.setValorObjetivo(metaDetails.getValorObjetivo());
                        meta.setFechaLimite(metaDetails.getFechaLimite());
                        meta.setEstado(metaDetails.getEstado());
                        Meta updatedMeta = metaRepository.save(meta);
                        log.info("Meta ID {} actualizada exitosamente en base de datos. Nuevo Estado = {}", 
                                 id, updatedMeta.getEstado());
                        return ResponseEntity.ok(updatedMeta);
                    } catch (Exception e) {
                        log.error("[ERR-DB-500] Error al actualizar la meta ID {} en MySQL. Detalles: {}", id, e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar en base de datos.");
                    }
                })
                .orElseGet(() -> {
                    log.warn("[ERR-VAL-400] Intento de actualización fallido: Meta ID {} no existe.", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeta(@PathVariable Long id) {
        log.info("Petición de negocio recibida para eliminar Meta ID: {}", id);
        return metaRepository.findById(id)
                .map(meta -> {
                    try {
                        metaRepository.delete(meta);
                        log.info("Meta ID {} eliminada con éxito de la base de datos MySQL (metas_db).", id);
                        return ResponseEntity.ok().<Void>build();
                    } catch (Exception e) {
                        log.error("[ERR-DB-500] Error al eliminar la meta ID {} de MySQL. Detalles: {}", id, e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Void>build();
                    }
                })
                .orElseGet(() -> {
                    log.warn("[ERR-VAL-400] Intento de eliminación fallido: Meta ID {} no existe.", id);
                    return ResponseEntity.notFound().build();
                });
    }
}
