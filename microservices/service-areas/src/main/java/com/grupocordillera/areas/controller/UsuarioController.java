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
            log.warn("Intento de creación fallido: Faltan campos requeridos en el RequestBody.");
            return ResponseEntity.badRequest().body("Los campos 'username', 'password', 'nombreCompleto' y 'role' son requeridos.");
        }
        
        // Convert username to lowercase to normalize logins
        usuario.setUsername(usuario.getUsername().toLowerCase().trim());
        log.info("Petición recibida en service-areas para crear usuario @{}", usuario.getUsername());
        
        if (usuarioRepository.existsById(usuario.getUsername())) {
            log.warn("Intento de creación fallido: El nombre de usuario @{} ya está registrado.", usuario.getUsername());
            return ResponseEntity.badRequest().body("El nombre de usuario ya está registrado.");
        }

        Usuario savedUsuario = usuarioRepository.save(usuario);
        log.info("Usuario @{} registrado exitosamente en MySQL (areas_db).", savedUsuario.getUsername());
        return ResponseEntity.ok(savedUsuario);
    }

    @PutMapping("/{username}")
    public ResponseEntity<?> updateUsuario(@PathVariable String username, @RequestBody Usuario updatedUser) {
        log.info("Petición recibida en service-areas para actualizar usuario @{}", username);
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

                    Usuario saved = usuarioRepository.save(existingUser);
                    log.info("Usuario @{} actualizado exitosamente en base de datos. Rol = {}", username, saved.getRole());
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> {
                    log.warn("Fallo al actualizar: Usuario @{} no encontrado.", username);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable String username) {
        log.info("Petición recibida en service-areas para eliminar usuario @{}", username);
        return usuarioRepository.findById(username.toLowerCase().trim())
                .map(usuario -> {
                    usuarioRepository.delete(usuario);
                    log.info("Usuario @{} eliminado exitosamente de MySQL (areas_db).", username);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> {
                    log.warn("Fallo al eliminar: Usuario @{} no encontrado.", username);
                    return ResponseEntity.notFound().build();
                });
    }
}
