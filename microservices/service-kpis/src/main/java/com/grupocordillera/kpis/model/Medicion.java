package com.grupocordillera.kpis.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "mediciones")
public class Medicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kpi_id", nullable = false)
    private Long kpiId;

    @Column(nullable = false)
    private Double valor;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "registrado_por")
    private String registradoPor; // Username (e.g. "jefe.ventas")

    // Constructors
    public Medicion() {}

    public Medicion(Long kpiId, Double valor, LocalDate fechaRegistro, String registradoPor) {
        this.kpiId = kpiId;
        this.valor = valor;
        this.fechaRegistro = fechaRegistro;
        this.registradoPor = registradoPor;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKpiId() {
        return kpiId;
    }

    public void setKpiId(Long kpiId) {
        this.kpiId = kpiId;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }
}
