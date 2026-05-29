package com.grupocordillera.metas.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "metas")
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kpi_id", nullable = false)
    private Long kpiId;

    @Column(name = "equipo_id", nullable = false)
    private Long equipoId;

    @Column(name = "valor_objetivo", nullable = false)
    private Double valorObjetivo;

    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @Column(nullable = false)
    private String estado = "ACTIVA"; // ACTIVA, CUMPLIDA, NO_CUMPLIDA

    // Constructors
    public Meta() {}

    public Meta(Long kpiId, Long equipoId, Double valorObjetivo, LocalDate fechaLimite, String estado) {
        this.kpiId = kpiId;
        this.equipoId = equipoId;
        this.valorObjetivo = valorObjetivo;
        this.fechaLimite = fechaLimite;
        this.estado = estado;
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

    public Long getEquipoId() {
        return equipoId;
    }

    public void setEquipoId(Long equipoId) {
        this.equipoId = equipoId;
    }

    public Double getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(Double valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
