package com.grupocordillera.kpis.controller;

import com.grupocordillera.kpis.factory.KpiFactory;
import com.grupocordillera.kpis.model.KPI;
import com.grupocordillera.kpis.repository.KpiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kpis")
public class KpiController {

    private static final Logger log = LoggerFactory.getLogger(KpiController.class);

    @Autowired
    private KpiRepository kpiRepository;

    @GetMapping
    public List<KPI> getAllKpis() {
        return kpiRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<KPI> getKpiById(@PathVariable Long id) {
        return kpiRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a KPI using the Factory Method pattern.
     */
    @PostMapping
    public ResponseEntity<?> createKpi(@RequestBody Map<String, String> request) {
        String tipo = request.get("tipo");
        String nombre = request.get("nombre");
        String descripcion = request.get("descripcion");
        String unidadMedida = request.get("unidadMedida");

        log.info("Petición recibida en service-kpis para crear KPI: Tipo = {}, Nombre = {}", tipo, nombre);

        try {
            if (tipo == null || nombre == null || unidadMedida == null) {
                log.warn("Fallo al crear KPI: Faltan campos requeridos en el RequestBody.");
                return ResponseEntity.badRequest().body("Los campos 'tipo', 'nombre' y 'unidadMedida' son requeridos.");
            }

            // Factory Method Pattern Usage
            KPI kpi = KpiFactory.createKpi(tipo, nombre, descripcion, unidadMedida);
            KPI savedKpi = kpiRepository.save(kpi);

            log.info("KPI fabricado e instanciado exitosamente. Guardado con ID = {}, Clase Polimórfica = {}", 
                     savedKpi.getId(), savedKpi.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedKpi);
        } catch (IllegalArgumentException e) {
            log.warn("Fallo al fabricar KPI: Parámetros inválidos para Factory Method. Detalles: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Excepción inesperada al crear KPI de tipo {}: {}", tipo, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el KPI: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateKpi(@PathVariable Long id, @RequestBody Map<String, String> request) {
        log.info("Petición recibida en service-kpis para actualizar KPI ID: {}", id);
        return kpiRepository.findById(id)
                .map(kpi -> {
                    kpi.setNombre(request.get("nombre"));
                    kpi.setDescripcion(request.get("descripcion"));
                    kpi.setUnidadMedida(request.get("unidadMedida"));
                    KPI updated = kpiRepository.save(kpi);
                    log.info("KPI ID {} actualizado con éxito en MySQL (kpis_db).", id);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> {
                    log.warn("Fallo al actualizar: KPI ID {} no encontrado.", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKpi(@PathVariable Long id) {
        log.info("Petición recibida en service-kpis para eliminar KPI ID: {}", id);
        return kpiRepository.findById(id)
                .map(kpi -> {
                    kpiRepository.delete(kpi);
                    log.info("KPI ID {} eliminado exitosamente de MySQL (kpis_db).", id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> {
                    log.warn("Fallo al eliminar: KPI ID {} no encontrado.", id);
                    return ResponseEntity.notFound().build();
                });
    }
}
