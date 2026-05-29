package com.grupocordillera.areas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private String role;

    private Long areaId;

    private Long equipoId;

    // Constructors
    public Usuario() {}

    public Usuario(String username, String password, String nombreCompleto, String role, Long areaId, Long equipoId) {
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.role = role;
        this.areaId = areaId;
        this.equipoId = equipoId;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getEquipoId() {
        return equipoId;
    }

    public void setEquipoId(Long equipoId) {
        this.equipoId = equipoId;
    }
}
