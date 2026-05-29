package com.grupocordillera.areas.controller;

import com.grupocordillera.areas.model.Equipo;
import com.grupocordillera.areas.repository.EquipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    @Autowired
    private EquipoRepository equipoRepository;

    @GetMapping
    public List<Equipo> getAllEquipos() {
        return equipoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipo> getEquipoById(@PathVariable Long id) {
        return equipoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/area/{areaId}")
    public List<Equipo> getEquiposByAreaId(@PathVariable Long areaId) {
        return equipoRepository.findByAreaId(areaId);
    }

    @GetMapping("/lider/{liderId}")
    public List<Equipo> getEquiposByLiderId(@PathVariable String liderId) {
        return equipoRepository.findByLiderId(liderId);
    }

    @PostMapping
    public Equipo createEquipo(@RequestBody Equipo equipo) {
        return equipoRepository.save(equipo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipo> updateEquipo(@PathVariable Long id, @RequestBody Equipo equipoDetails) {
        return equipoRepository.findById(id)
                .map(equipo -> {
                    equipo.setNombre(equipoDetails.getNombre());
                    equipo.setAreaId(equipoDetails.getAreaId());
                    equipo.setLiderId(equipoDetails.getLiderId());
                    Equipo updatedEquipo = equipoRepository.save(equipo);
                    return ResponseEntity.ok(updatedEquipo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipo(@PathVariable Long id) {
        return equipoRepository.findById(id)
                .map(equipo -> {
                    equipoRepository.delete(equipo);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
