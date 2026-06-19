package com.grupocordillera.bff.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    public void testGenerateAndExtractToken() {
        String username = "testuser";
        String role = "ADMIN";
        String nombreCompleto = "Test User Admin";
        Long areaId = 1L;
        Long equipoId = 2L;

        String token = jwtUtil.generateToken(username, role, nombreCompleto, areaId, equipoId);

        assertNotNull(token);
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    public void testExtractRole() {
        String username = "testuser";
        String role = "JEFE_AREA";
        String nombreCompleto = "Jefe de Area";
        Long areaId = 5L;
        Long equipoId = null;

        String token = jwtUtil.generateToken(username, role, nombreCompleto, areaId, equipoId);

        assertEquals(role, jwtUtil.extractRole(token));
    }

    @Test
    public void testExtractAreaIdAndEquipoId() {
        String username = "colaborador";
        String role = "COLABORADOR";
        String nombreCompleto = "Juan Perez";
        Long areaId = 2L;
        Long equipoId = 10L;

        String token = jwtUtil.generateToken(username, role, nombreCompleto, areaId, equipoId);

        assertEquals(areaId, jwtUtil.extractAreaId(token));
        assertEquals(equipoId, jwtUtil.extractEquipoId(token));
    }

    @Test
    public void testValidateTokenSuccess() {
        String username = "jefe.ventas";
        String role = "JEFE_AREA";
        String nombreCompleto = "Jefe Ventas";
        Long areaId = 1L;
        Long equipoId = 2L;

        String token = jwtUtil.generateToken(username, role, nombreCompleto, areaId, equipoId);

        assertTrue(jwtUtil.validateToken(token, username));
    }

    @Test
    public void testValidateTokenFailure() {
        String username = "jefe.ventas";
        String role = "JEFE_AREA";
        String nombreCompleto = "Jefe Ventas";
        Long areaId = 1L;
        Long equipoId = 2L;

        String token = jwtUtil.generateToken(username, role, nombreCompleto, areaId, equipoId);

        // Validation should fail for a different username
        assertFalse(jwtUtil.validateToken(token, "anotheruser"));
    }
}
