package com.grupocordillera.areas.controller;

import com.grupocordillera.areas.model.Usuario;
import com.grupocordillera.areas.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/{username}")
    public ResponseEntity<Usuario> getUsuarioByUsername(@PathVariable String username) {
        return usuarioRepository.findById(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUsuario(@RequestBody Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getPassword() == null || usuario.getNombreCompleto() == null || usuario.getRole() == null) {
            log.warn("[ERR-VAL-400] Intento de creación fallido: Faltan campos requeridos en el RequestBody.");
            return ResponseEntity.badRequest().body("Los campos 'username', 'password', 'nombreCompleto' y 'role' son requeridos.");
        }
        
        // Convert username to lowercase to normalize logins
        usuario.setUsername(usuario.getUsername().toLowerCase().trim());
        log.info("Petición recibida en service-areas para crear usuario @{}", usuario.getUsername());
        
        if (usuarioRepository.existsById(usuario.getUsername())) {
            log.warn("[ERR-VAL-400] Intento de creación fallido: El nombre de usuario @{} ya está registrado.", usuario.getUsername());
            return ResponseEntity.badRequest().body("El nombre de usuario ya está registrado.");
        }

        try {
            Usuario savedUsuario = usuarioRepository.save(usuario);
            log.info("Usuario @{} registrado exitosamente en MySQL (areas_db).", savedUsuario.getUsername());
            return ResponseEntity.ok(savedUsuario);
        } catch (Exception e) {
            log.error("[ERR-DB-500] Error al persistir el usuario @{} en base de datos: {}", usuario.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Error al persistir el usuario en base de datos.");
        }
    }

    @PutMapping("/{username}")
    public ResponseEntity<?> updateUsuario(@PathVariable String username, @RequestBody Usuario updatedUser) {
        log.info("Petición recibida en service-areas para actualizar usuario @{}", username);
        try {
            return usuarioRepository.findById(username.toLowerCase().trim())
                    .map(existingUser -> {
                        if (updatedUser.getNombreCompleto() != null) {
                            existingUser.setNombreCompleto(updatedUser.getNombreCompleto());
                        }
                        if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                            existingUser.setPassword(updatedUser.getPassword());
                        }
                        if (updatedUser.getRole() != null) {
                            existingUser.setRole(updatedUser.getRole());
                        }
                        existingUser.setAreaId(updatedUser.getAreaId());
                        existingUser.setEquipoId(updatedUser.getEquipoId());

                        try {
                            Usuario saved = usuarioRepository.save(existingUser);
                            log.info("Usuario @{} actualizado exitosamente en base de datos. Rol = {}", username, saved.getRole());
                            return ResponseEntity.ok(saved);
                        } catch (Exception e) {
                            log.error("[ERR-DB-500] Error al guardar la actualización del usuario @{} en base de datos: {}", username, e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    })
                    .orElseGet(() -> {
                        log.warn("[ERR-VAL-400] Fallo al actualizar: Usuario @{} no encontrado.", username);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("[ERR-DB-500] Error interno al buscar/actualizar el usuario @{} en base de datos: {}", username, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error interno en base de datos al actualizar usuario.");
        }
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteUsuario(@PathVariable String username) {
        log.info("Petición recibida en service-areas para eliminar usuario @{}", username);
        try {
            return usuarioRepository.findById(username.toLowerCase().trim())
                    .map(usuario -> {
                        try {
                            usuarioRepository.delete(usuario);
                            log.info("Usuario @{} eliminado exitosamente de MySQL (areas_db).", username);
                            return ResponseEntity.ok().build();
                        } catch (Exception e) {
                            log.error("[ERR-DB-500] Error al eliminar el usuario @{} de la base de datos: {}", username, e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    })
                    .orElseGet(() -> {
                        log.warn("[ERR-VAL-400] Fallo al eliminar: Usuario @{} no encontrado.", username);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("[ERR-DB-500] Error interno al buscar/eliminar el usuario @{} en base de datos: {}", username, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error interno en base de datos al eliminar usuario.");
        }
    }
}
