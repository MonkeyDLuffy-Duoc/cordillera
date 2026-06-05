package com.grupocordillera.bff.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class BackendClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BackendClientService backendClientService;

    @Test
    public void testGetKpisSuccess() {
        List<Object> mockKpis = new ArrayList<>();
        Mockito.when(restTemplate.getForObject(eq("http://service-kpis/api/kpis"), eq(List.class)))
               .thenReturn(mockKpis);

        BackendClientService.ServiceResult result = backendClientService.getKpis();
        assertNotNull(result);
        assertEquals("OK", result.getStatus());
        assertEquals("Servicio operativo", result.getMessage());
    }

    @Test
    public void testGetKpisFallback() {
        BackendClientService.ServiceResult result = backendClientService.fallbackKpis(new RuntimeException("Error de red"));
        assertNotNull(result);
        assertEquals("DEGRADADO", result.getStatus());
        assertEquals("El servicio de Gestión de KPIs (Catálogo) no está disponible.", result.getMessage());
    }

    @Test
    public void testGetMetasSuccess() {
        List<Object> mockMetas = new ArrayList<>();
        Mockito.when(restTemplate.getForObject(eq("http://service-metas/api/metas"), eq(List.class)))
               .thenReturn(mockMetas);

        BackendClientService.ServiceResult result = backendClientService.getMetas();
        assertNotNull(result);
        assertEquals("OK", result.getStatus());
        assertEquals("Servicio operativo", result.getMessage());
    }

    @Test
    public void testGetMetasFallback() {
        BackendClientService.ServiceResult result = backendClientService.fallbackMetas(new RuntimeException("Error de red"));
        assertNotNull(result);
        assertEquals("DEGRADADO", result.getStatus());
        assertEquals("El servicio de Metas Organizacionales no está disponible.", result.getMessage());
    }
}
