package com.grupocordillera.kpis.model;

import jakarta.persistence.*;

@Entity
@Table(name = "kpis")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_kpi", discriminatorType = DiscriminatorType.STRING)
public abstract class KPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "unidad_medida", nullable = false)
    private String unidadMedida;

    // Abstract validation method (to be implemented by concrete classes - Factory Method Pattern context)
    public abstract boolean validarValor(double valor);

    // Constructors
    public KPI() {}

    public KPI(String nombre, String descripcion, String unidadMedida) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.unidadMedida = unidadMedida;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    @Transient
    public String getTipo() {
        DiscriminatorValue val = this.getClass().getAnnotation(DiscriminatorValue.class);
        return val == null ? null : val.value();
    }
}
