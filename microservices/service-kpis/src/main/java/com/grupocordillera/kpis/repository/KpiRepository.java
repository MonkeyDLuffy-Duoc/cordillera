package com.grupocordillera.kpis.repository;

import com.grupocordillera.kpis.model.KPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KpiRepository extends JpaRepository<KPI, Long> {
}
