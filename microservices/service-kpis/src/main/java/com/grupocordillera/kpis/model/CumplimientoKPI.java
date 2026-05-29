package com.grupocordillera.kpis.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CUMPLIMIENTO")
public class CumplimientoKPI extends KPI {

    public CumplimientoKPI() {
        super();
    }

    public CumplimientoKPI(String nombre, String descripcion, String unidadMedida) {
        super(nombre, descripcion, unidadMedida);
    }

    @Override
    public boolean validarValor(double valor) {
        // Compliance metrics must be percentages between 0 and 100
        return valor >= 0.0 && valor <= 100.0;
    }
}
