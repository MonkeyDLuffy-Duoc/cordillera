package com.grupocordillera.bff.controller;

import com.grupocordillera.bff.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            log.warn("Fallo de login: Petición incompleta sin credenciales.");
            return ResponseEntity.badRequest().body("Los campos 'username' y 'password' son requeridos.");
        }

        username = username.toLowerCase().trim();
        log.info("Intento de inicio de sesión recibido para usuario: {}", username);

        // Query service-areas for the user dynamically
        String serviceAreasUrl = "http://service-areas/api/usuarios/" + username;
        Map<String, Object> dbUser = null;
        try {
            dbUser = restTemplate.getForObject(serviceAreasUrl, Map.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.warn("Fallo de login: El usuario @{} no existe en MySQL (areas_db).", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas: El usuario no existe.");
        } catch (Exception e) {
            log.error("Fallo de comunicación: No se pudo conectar con 'service-areas' para verificar a @{}. Motivo: {}", 
                      username, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo conectar al servicio de autenticación en este momento. Inténtelo más tarde.");
        }

        if (dbUser == null) {
            log.warn("Fallo de login: Registro de usuario @{} nulo en el servicio de áreas.", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas.");
        }

        // Validate plain password
        String dbPassword = (String) dbUser.get("password");
        if (!password.equals(dbPassword)) {
            log.warn("Fallo de login: Contraseña incorrecta para el usuario @{}.", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas: Contraseña inválida.");
        }

        // Extract credentials
        String role = (String) dbUser.get("role");
        String nombreCompleto = (String) dbUser.get("nombreCompleto");
        
        Long areaId = dbUser.get("areaId") != null ? Long.valueOf(dbUser.get("areaId").toString()) : null;
        Long equipoId = dbUser.get("equipoId") != null ? Long.valueOf(dbUser.get("equipoId").toString()) : null;

        // Generate JWT Token
        String token = jwtUtil.generateToken(username, role, nombreCompleto, areaId, equipoId);
        log.info("Login exitoso. Token JWT generado para el usuario @{} con rol: {} (Área: {}, Equipo: {}).", 
                 username, role, areaId != null ? areaId : "Global", equipoId != null ? equipoId : "Ninguno");

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
