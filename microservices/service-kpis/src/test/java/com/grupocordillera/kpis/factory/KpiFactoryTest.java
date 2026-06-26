package com.grupocordillera.kpis.factory;

import com.grupocordillera.kpis.model.CumplimientoKPI;
import com.grupocordillera.kpis.model.FinancieroKPI;
import com.grupocordillera.kpis.model.KPI;
import com.grupocordillera.kpis.model.OperacionalKPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KpiFactoryTest {

    @Test
    public void testCreateFinancieroKpi() {
        KPI kpi = KpiFactory.createKpi("FINANCIERO", "Margen EBITDA", "Margen operacional", "%");
        assertNotNull(kpi);
        assertTrue(kpi instanceof FinancieroKPI);
        assertEquals("Margen EBITDA", kpi.getNombre());
        assertEquals("Margen operacional", kpi.getDescripcion());
        assertEquals("%", kpi.getUnidadMedida());
    }

    @Test
    public void testCreateCumplimientoKpi() {
        KPI kpi = KpiFactory.createKpi("CUMPLIMIENTO", "Capacitacion Anual", "Horas aprobadas", "Horas");
        assertNotNull(kpi);
        assertTrue(kpi instanceof CumplimientoKPI);
        assertEquals("Capacitacion Anual", kpi.getNombre());
        assertEquals("Horas aprobadas", kpi.getDescripcion());
        assertEquals("Horas", kpi.getUnidadMedida());
    }

    @Test
    public void testCreateOperacionalKpi() {
        KPI kpi = KpiFactory.createKpi("OPERACIONAL", "Disponibilidad Servidores", "SLA servidores", "%");
        assertNotNull(kpi);
        assertTrue(kpi instanceof OperacionalKPI);
        assertEquals("Disponibilidad Servidores", kpi.getNombre());
        assertEquals("SLA servidores", kpi.getDescripcion());
        assertEquals("%", kpi.getUnidadMedida());
    }

}
