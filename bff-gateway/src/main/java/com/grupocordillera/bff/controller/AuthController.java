package com.grupocordillera.bff.controller;

import com.grupocordillera.bff.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("Los campos 'username' y 'password' son requeridos.");
        }

        // Simulating predefined accounts for quick demonstration
        String role = null;
        String nombreCompleto = null;
        Long areaId = null;
        Long equipoId = null;

        if ("admin".equals(username) && "admin123".equals(password)) {
            role = "ADMIN";
            nombreCompleto = "Administrador del Sistema";
        } else if ("gerente".equals(username) && "gerente123".equals(password)) {
            role = "GERENTE";
            nombreCompleto = "Gerente Comercial";
        } else if ("jefe.ventas".equals(username) && "jefe123".equals(password)) {
            role = "JEFE_AREA";
            nombreCompleto = "Juan Pablo Rivera - Jefe de Ventas";
            areaId = 1L; // Ventas y Comercial Area
        } else if ("juan.ventas".equals(username) && "colab123".equals(password)) {
            role = "COLABORADOR";
            nombreCompleto = "Nayaret Rivas - Colaboradora Ventas";
            areaId = 1L;
            equipoId = 1L; // Ventas Norte Team
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas.");
        }

        // Generate JWT Token
        String token = jwtUtil.generateToken(username, role, nombreCompleto, areaId, equipoId);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("role", role);
        response.put("nombreCompleto", nombreCompleto);
        response.put("areaId", areaId);
        response.put("equipoId", equipoId);

        return ResponseEntity.ok(response);
    }
}
