package com.grupocordillera.areas.repository;

import com.grupocordillera.areas.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class UsuarioRepositoryIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    public void testSaveAndFindUser() {
        Usuario user = new Usuario("testuser", "pass123", "Test User", "COLABORADOR", 1L, 2L);
        usuarioRepository.save(user);

        Optional<Usuario> found = usuarioRepository.findById("testuser");
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getNombreCompleto());
        assertEquals("pass123", found.get().getPassword());
        assertEquals("COLABORADOR", found.get().getRole());
        assertEquals(1L, found.get().getAreaId());
        assertEquals(2L, found.get().getEquipoId());
    }

    @Test
    public void testUpdateUser() {
        Usuario user = new Usuario("updateuser", "oldpass", "Update User", "COLABORADOR", 1L, 2L);
        usuarioRepository.save(user);

        Usuario savedUser = usuarioRepository.findById("updateuser").orElse(null);
        assertNotNull(savedUser);
        savedUser.setPassword("newpass");
        savedUser.setRole("ADMIN");
        usuarioRepository.save(savedUser);

        Usuario updatedUser = usuarioRepository.findById("updateuser").orElse(null);
        assertNotNull(updatedUser);
        assertEquals("newpass", updatedUser.getPassword());
        assertEquals("ADMIN", updatedUser.getRole());
    }

    @Test
    public void testDeleteUser() {
        Usuario user = new Usuario("deleteuser", "pass", "Delete User", "COLABORADOR", 1L, 2L);
        usuarioRepository.save(user);

        assertTrue(usuarioRepository.findById("deleteuser").isPresent());

        usuarioRepository.deleteById("deleteuser");

        assertFalse(usuarioRepository.findById("deleteuser").isPresent());
    }

    @Test
    public void testFindAllUsers() {
        usuarioRepository.deleteAll(); // clear any pre-loaded data

        Usuario u1 = new Usuario("u1", "pass", "User One", "ADMIN", null, null);
        Usuario u2 = new Usuario("u2", "pass", "User Two", "COLABORADOR", 1L, 2L);
        usuarioRepository.save(u1);
        usuarioRepository.save(u2);

        List<Usuario> users = usuarioRepository.findAll();
        assertEquals(2, users.size());
    }
}
