package com.grupocordillera.areas.controller;

import com.grupocordillera.areas.model.Usuario;
import com.grupocordillera.areas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

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
            return ResponseEntity.badRequest().body("Los campos 'username', 'password', 'nombreCompleto' y 'role' son requeridos.");
        }
        
        // Convert username to lowercase to normalize logins
        usuario.setUsername(usuario.getUsername().toLowerCase().trim());
        
        if (usuarioRepository.existsById(usuario.getUsername())) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya está registrado.");
        }

        Usuario savedUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.ok(savedUsuario);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable String username) {
        return usuarioRepository.findById(username.toLowerCase().trim())
                .map(usuario -> {
                    usuarioRepository.delete(usuario);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
