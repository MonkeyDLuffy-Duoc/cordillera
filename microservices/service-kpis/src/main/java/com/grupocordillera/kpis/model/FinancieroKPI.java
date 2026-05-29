package com.grupocordillera.kpis.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FINANCIERO")
public class FinancieroKPI extends KPI {

    public FinancieroKPI() {
        super();
    }

    public FinancieroKPI(String nombre, String descripcion, String unidadMedida) {
        super(nombre, descripcion, unidadMedida);
    }

    @Override
    public boolean validarValor(double valor) {
        // Financial values can be high or low but generally should be non-negative
        return valor >= 0.0;
    }
}
