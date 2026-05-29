package com.grupocordillera.kpis.controller;

import com.grupocordillera.kpis.factory.KpiFactory;
import com.grupocordillera.kpis.model.KPI;
import com.grupocordillera.kpis.repository.KpiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kpis")
public class KpiController {

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
        try {
            String tipo = request.get("tipo");
            String nombre = request.get("nombre");
            String descripcion = request.get("descripcion");
            String unidadMedida = request.get("unidadMedida");

            if (tipo == null || nombre == null || unidadMedida == null) {
                return ResponseEntity.badRequest().body("Los campos 'tipo', 'nombre' y 'unidadMedida' son requeridos.");
            }

            // Factory Method Pattern Usage
            KPI kpi = KpiFactory.createKpi(tipo, nombre, descripcion, unidadMedida);
            KPI savedKpi = kpiRepository.save(kpi);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedKpi);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el KPI: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateKpi(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return kpiRepository.findById(id)
                .map(kpi -> {
                    kpi.setNombre(request.get("nombre"));
                    kpi.setDescripcion(request.get("descripcion"));
                    kpi.setUnidadMedida(request.get("unidadMedida"));
                    KPI updated = kpiRepository.save(kpi);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKpi(@PathVariable Long id) {
        return kpiRepository.findById(id)
                .map(kpi -> {
                    kpiRepository.delete(kpi);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
