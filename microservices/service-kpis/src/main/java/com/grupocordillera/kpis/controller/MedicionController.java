package com.grupocordillera.kpis.controller;

import com.grupocordillera.kpis.model.KPI;
import com.grupocordillera.kpis.model.Medicion;
import com.grupocordillera.kpis.repository.KpiRepository;
import com.grupocordillera.kpis.repository.MedicionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/mediciones")
public class MedicionController {

    @Autowired
    private MedicionRepository medicionRepository;

    @Autowired
    private KpiRepository kpiRepository;

    @GetMapping
    public List<Medicion> getAllMediciones() {
        return medicionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicion> getMedicionById(@PathVariable Long id) {
        return medicionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/kpi/{kpiId}")
    public List<Medicion> getMedicionesByKpiId(@PathVariable Long kpiId) {
        return medicionRepository.findByKpiIdOrderByFechaRegistroAsc(kpiId);
    }

    @GetMapping("/kpi/{kpiId}/range")
    public List<Medicion> getMedicionesByKpiIdAndRange(
            @PathVariable Long kpiId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return medicionRepository.findByKpiIdAndFechaRegistroBetweenOrderByFechaRegistroAsc(kpiId, startDate, endDate);
    }

    /**
     * Registers a new measurement, utilizing the polymorphism validation of the target KPI.
     */
    @PostMapping
    public ResponseEntity<?> createMedicion(@RequestBody Medicion medicion) {
        if (medicion.getKpiId() == null || medicion.getValor() == null) {
            return ResponseEntity.badRequest().body("Los campos 'kpiId' y 'valor' son requeridos.");
        }

        // Fetch KPI to validate
        return kpiRepository.findById(medicion.getKpiId())
                .map(kpi -> {
                    // Polymorphic validation call
                    if (!kpi.validarValor(medicion.getValor())) {
                        return ResponseEntity.badRequest().body(
                            String.format("El valor %.2f no es válido para el KPI '%s' (tipo: %s, unidad: %s).",
                                medicion.getValor(), kpi.getNombre(), kpi.getTipo(), kpi.getUnidadMedida())
                        );
                    }

                    if (medicion.getFechaRegistro() == null) {
                        medicion.setFechaRegistro(LocalDate.now());
                    }

                    Medicion savedMedicion = medicionRepository.save(medicion);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedMedicion);
                })
                .orElse(ResponseEntity.badRequest().body("El KPI con ID " + medicion.getKpiId() + " no existe."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicion(@PathVariable Long id) {
        return medicionRepository.findById(id)
                .map(medicion -> {
                    medicionRepository.delete(medicion);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
