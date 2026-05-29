package com.grupocordillera.areas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipos")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "area_id", nullable = false)
    private Long areaId;

    @Column(name = "lider_id")
    private String liderId; // Reference to the username of the team leader (e.g. "jefe.ventas")

    // Constructors
    public Equipo() {}

    public Equipo(String nombre, Long areaId, String liderId) {
        this.nombre = nombre;
        this.areaId = areaId;
        this.liderId = liderId;
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

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getLiderId() {
        return liderId;
    }

    public void setLiderId(String liderId) {
        this.liderId = liderId;
    }
}
