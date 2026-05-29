package com.grupocordillera.kpis.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("OPERACIONAL")
public class OperacionalKPI extends KPI {

    public OperacionalKPI() {
        super();
    }

    public OperacionalKPI(String nombre, String descripcion, String unidadMedida) {
        super(nombre, descripcion, unidadMedida);
    }

    @Override
    public boolean validarValor(double valor) {
        // Operational values (e.g. counts, hours, stock units) should be non-negative
        return valor >= 0.0;
    }
}
