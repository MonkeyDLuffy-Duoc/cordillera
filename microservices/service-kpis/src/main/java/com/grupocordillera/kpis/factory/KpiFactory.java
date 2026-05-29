package com.grupocordillera.kpis.factory;

import com.grupocordillera.kpis.model.*;

public class KpiFactory {

    /**
     * Factory Method to instantiate specific KPI types.
     *
     * @param tipo The type of KPI (FINANCIERO, CUMPLIMIENTO, OPERACIONAL)
     * @param nombre The name of the KPI
     * @param descripcion The description of the KPI
     * @param unidadMedida The unit of measurement (e.g., CLP, %, Unidades, Horas)
     * @return A concrete KPI subclass instance
     * @throws IllegalArgumentException if the type is unknown
     */
    public static KPI createKpi(String tipo, String nombre, String descripcion, String unidadMedida) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de KPI no puede ser nulo.");
        }

        switch (tipo.toUpperCase()) {
            case "FINANCIERO":
                return new FinancieroKPI(nombre, descripcion, unidadMedida);
            case "CUMPLIMIENTO":
                return new CumplimientoKPI(nombre, descripcion, unidadMedida);
            case "OPERACIONAL":
                return new OperacionalKPI(nombre, descripcion, unidadMedida);
            default:
                throw new IllegalArgumentException("Tipo de KPI desconocido: " + tipo);
        }
    }
}
