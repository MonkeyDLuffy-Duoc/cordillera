package com.grupocordillera.bff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupocordillera.bff.security.JwtUtil;
import com.grupocordillera.bff.service.BackendClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BffGatewayE2eTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private BackendClientService backendClientService;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Map<String, Object>> mockAreas;
    private List<Map<String, Object>> mockEquipos;
    private List<Map<String, Object>> mockKpis;
    private List<Map<String, Object>> mockMediciones;
    private List<Map<String, Object>> mockMetas;

    @BeforeEach
    public void setUp() {
        // Setup mock data representing typical backend responses
        mockAreas = new ArrayList<>();
        Map<String, Object> area1 = new HashMap<>();
        area1.put("id", 1L);
        area1.put("nombre", "Ventas");
        mockAreas.add(area1);

        mockEquipos = new ArrayList<>();
        Map<String, Object> eq1 = new HashMap<>();
        eq1.put("id", 1L);
        eq1.put("nombre", "Ventas Retail");
        eq1.put("areaId", 1L);
        mockEquipos.add(eq1);

        mockKpis = new ArrayList<>();
        Map<String, Object> kpi1 = new HashMap<>();
        kpi1.put("id", 1L);
        kpi1.put("nombre", "Volumen Ventas");
        kpi1.put("tipo", "FINANCIERO");
        kpi1.put("unidadMedida", "CLP");
        mockKpis.add(kpi1);

        mockMediciones = new ArrayList<>();
        Map<String, Object> med1 = new HashMap<>();
        med1.put("id", 1L);
        med1.put("kpiId", 1L);
        med1.put("valor", 100.0);
        med1.put("fechaRegistro", "2026-06-01T00:00:00Z");
        mockMediciones.add(med1);

        mockMetas = new ArrayList<>();
        Map<String, Object> meta1 = new HashMap<>();
        meta1.put("id", 1L);
        meta1.put("kpiId", 1L);
        meta1.put("equipoId", 1L);
        meta1.put("valorObjetivo", 120.0);
        mockMetas.add(meta1);

        // Setup common mocked client service calls
        Mockito.when(backendClientService.getAreas()).thenReturn(new BackendClientService.ServiceResult(mockAreas, "OK", "Servicio operativo"));
        Mockito.when(backendClientService.getEquipos()).thenReturn(new BackendClientService.ServiceResult(mockEquipos, "OK", "Servicio operativo"));
        Mockito.when(backendClientService.getKpis()).thenReturn(new BackendClientService.ServiceResult(mockKpis, "OK", "Servicio operativo"));
        Mockito.when(backendClientService.getMediciones()).thenReturn(new BackendClientService.ServiceResult(mockMediciones, "OK", "Servicio operativo"));
        Mockito.when(backendClientService.getMetas()).thenReturn(new BackendClientService.ServiceResult(mockMetas, "OK", "Servicio operativo"));
    }

    @Test
    public void testDashboardE2EAdminAccess() throws Exception {
        // 1. Generate token for Admin
        String token = jwtUtil.generateToken("admin", "ADMIN", "Administrador", null, null);

        // 2. Perform E2E GET Request on dashboard
        mockMvc.perform(get("/api/bff/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.areas").isArray())
                .andExpect(jsonPath("$.areas[0].nombre").value("Ventas"))
                .andExpect(jsonPath("$.metasReporte[0].kpiNombre").value("Volumen Ventas"))
                .andExpect(jsonPath("$.metasReporte[0].valorObjetivo").value(120.0))
                .andExpect(jsonPath("$.metasReporte[0].valorActual").value(100.0))
                // Compliance calculation: (100 / 120) * 100 = 83.33 -> 83.3
                .andExpect(jsonPath("$.metasReporte[0].cumplimiento").value(83.3))
                .andExpect(jsonPath("$.metasReporte[0].estado").value("EN_PROGRESO"));
    }

    @Test
    public void testDashboardE2ECollaboratorAccess() throws Exception {
        // 1. Generate token for Collaborator linked to Equipo 1 (Ventas Retail)
        String token = jwtUtil.generateToken("juan", "COLABORADOR", "Juan Colaborador", 1L, 1L);

        // 2. Perform E2E GET Request on dashboard
        mockMvc.perform(get("/api/bff/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("COLABORADOR"))
                .andExpect(jsonPath("$.username").value("juan"))
                .andExpect(jsonPath("$.equipos[0].id").value(1))
                .andExpect(jsonPath("$.metasReporte[0].equipoNombre").value("Ventas Retail"));
    }

    @Test
    public void testDashboardE2EUnauthorized() throws Exception {
        // Perform request with NO token header
        mockMvc.perform(get("/api/bff/dashboard"))
                .andExpect(status().isForbidden());

        // Perform request with corrupted/expired token header
        mockMvc.perform(get("/api/bff/dashboard")
                .header("Authorization", "Bearer corrupted.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateMetaSuccess() throws Exception {
        // 1. Generate token for Admin
        String token = jwtUtil.generateToken("admin", "ADMIN", "Administrador", null, null);

        Map<String, Object> metaRequest = new HashMap<>();
        metaRequest.put("kpiId", 1L);
        metaRequest.put("equipoId", 1L);
        metaRequest.put("valorObjetivo", 150.0);

        Map<String, Object> mockCreatedMeta = new HashMap<>();
        mockCreatedMeta.put("id", 2L);
        mockCreatedMeta.put("kpiId", 1L);
        mockCreatedMeta.put("equipoId", 1L);
        mockCreatedMeta.put("valorObjetivo", 150.0);

        Mockito.when(restTemplate.postForEntity(eq("http://service-metas/api/metas"), any(), eq(Object.class)))
               .thenReturn(new ResponseEntity<>(mockCreatedMeta, HttpStatus.CREATED));

        // 2. Perform POST Request
        mockMvc.perform(post("/api/bff/metas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.valorObjetivo").value(150.0));
    }

    @Test
    public void testCreateMetaForbidden() throws Exception {
        // 1. Generate token for Collaborator (Role COLABORADOR does not have access to write/create goals)
        String token = jwtUtil.generateToken("juan", "COLABORADOR", "Juan Colaborador", 1L, 1L);

        Map<String, Object> metaRequest = new HashMap<>();
        metaRequest.put("kpiId", 1L);
        metaRequest.put("equipoId", 1L);
        metaRequest.put("valorObjetivo", 150.0);

        // 2. Perform POST Request and expect 403 Forbidden
        mockMvc.perform(post("/api/bff/metas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acceso denegado: Solo administradores o jefes de área pueden crear metas."));
    }
}
