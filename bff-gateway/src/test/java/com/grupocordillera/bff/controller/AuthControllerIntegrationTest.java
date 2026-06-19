package com.grupocordillera.bff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupocordillera.bff.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // bypass Spring Security filters for this endpoint integration test
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testLoginSuccess() throws Exception {
        Map<String, Object> mockDbUser = new HashMap<>();
        mockDbUser.put("username", "admin");
        mockDbUser.put("password", "admin123");
        mockDbUser.put("role", "ADMIN");
        mockDbUser.put("nombreCompleto", "Administrador");
        mockDbUser.put("areaId", null);
        mockDbUser.put("equipoId", null);

        Mockito.when(restTemplate.getForObject(eq("http://service-areas/api/usuarios/admin"), eq(Map.class)))
               .thenReturn(mockDbUser);

        Mockito.when(jwtUtil.generateToken(anyString(), anyString(), anyString(), any(), any()))
               .thenReturn("mocked-jwt-token");

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "admin123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.nombreCompleto").value("Administrador"));
    }

    @Test
    public void testLoginIncorrectPassword() throws Exception {
        Map<String, Object> mockDbUser = new HashMap<>();
        mockDbUser.put("username", "admin");
        mockDbUser.put("password", "admin123"); // correct password
        mockDbUser.put("role", "ADMIN");
        mockDbUser.put("nombreCompleto", "Administrador");

        Mockito.when(restTemplate.getForObject(eq("http://service-areas/api/usuarios/admin"), eq(Map.class)))
               .thenReturn(mockDbUser);

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "wrongpass"); // incorrect password

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales incorrectas: Contraseña inválida."));
    }

    @Test
    public void testLoginUserNotFound() throws Exception {
        Mockito.when(restTemplate.getForObject(eq("http://service-areas/api/usuarios/nonexistent"), eq(Map.class)))
               .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", org.springframework.http.HttpHeaders.EMPTY, new byte[0], java.nio.charset.StandardCharsets.UTF_8));

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "nonexistent");
        loginRequest.put("password", "pass123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales incorrectas: El usuario no existe."));
    }

    @Test
    public void testLoginAreasServiceUnavailable() throws Exception {
        Mockito.when(restTemplate.getForObject(eq("http://service-areas/api/usuarios/admin"), eq(Map.class)))
               .thenThrow(new RuntimeException("Connection Timeout"));

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "admin123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("No se pudo conectar al servicio de autenticación en este momento. Inténtelo más tarde."));
    }

    @Test
    public void testLoginMissingFields() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        // missing password

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Los campos 'username' y 'password' son requeridos."));
    }
}
