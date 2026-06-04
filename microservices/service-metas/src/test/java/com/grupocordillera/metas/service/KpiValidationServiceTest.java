package com.grupocordillera.metas.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class KpiValidationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KpiValidationService kpiValidationService;

    @Test
    public void testValidateKpiExistsSuccess() {
        Mockito.when(restTemplate.getForObject(eq("http://service-kpis/api/kpis/1"), eq(Object.class)))
               .thenReturn(new Object());

        boolean result = kpiValidationService.validateKpiExists(1L);
        assertTrue(result);
    }

    @Test
    public void testValidateKpiExistsFallback() {
        // Direct validation of the fallback behavior and its returned value
        boolean result = kpiValidationService.fallbackValidateKpi(1L, new RuntimeException("Simulacion de Caida de KPIs"));
        assertTrue(result);
    }
}
