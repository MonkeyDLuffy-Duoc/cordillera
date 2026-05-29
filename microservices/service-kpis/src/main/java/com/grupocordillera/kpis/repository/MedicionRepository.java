package com.grupocordillera.kpis.repository;

import com.grupocordillera.kpis.model.Medicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicionRepository extends JpaRepository<Medicion, Long> {
    List<Medicion> findByKpiIdOrderByFechaRegistroAsc(Long kpiId);
    List<Medicion> findByKpiIdAndFechaRegistroBetweenOrderByFechaRegistroAsc(Long kpiId, LocalDate start, LocalDate end);
}
