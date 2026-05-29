package com.grupocordillera.areas.controller;

import com.grupocordillera.areas.model.Area;
import com.grupocordillera.areas.repository.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/areas")
public class AreaController {

    @Autowired
    private AreaRepository areaRepository;

    @GetMapping
    public List<Area> getAllAreas() {
        return areaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Area> getAreaById(@PathVariable Long id) {
        return areaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Area createArea(@RequestBody Area area) {
        return areaRepository.save(area);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Area> updateArea(@PathVariable Long id, @RequestBody Area areaDetails) {
        return areaRepository.findById(id)
                .map(area -> {
                    area.setNombre(areaDetails.getNombre());
                    area.setDescripcion(areaDetails.getDescripcion());
                    Area updatedArea = areaRepository.save(area);
                    return ResponseEntity.ok(updatedArea);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArea(@PathVariable Long id) {
        return areaRepository.findById(id)
                .map(area -> {
                    areaRepository.delete(area);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
