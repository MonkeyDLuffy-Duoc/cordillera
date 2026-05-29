package com.grupocordillera.areas.repository;

import com.grupocordillera.areas.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    List<Equipo> findByAreaId(Long areaId);
    List<Equipo> findByLiderId(String liderId);
}
