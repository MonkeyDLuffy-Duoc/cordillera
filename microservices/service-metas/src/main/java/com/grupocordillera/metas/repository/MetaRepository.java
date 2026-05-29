package com.grupocordillera.metas.repository;

import com.grupocordillera.metas.model.Meta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaRepository extends JpaRepository<Meta, Long> {
    List<Meta> findByEquipoId(Long equipoId);
    List<Meta> findByKpiId(Long kpiId);
}
