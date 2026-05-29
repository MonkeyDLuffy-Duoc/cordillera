# Catálogo e Índice de Errores: Plataforma de Microservicios (Grupo Cordillera)

Este documento sirve como el **Índice de Errores (Error Code Index)** oficial del sistema. Cada traza de log generada por el BFF Gateway o los microservicios de negocio cuenta con un código único estructurado como `[ERR-CATEGORÍA-CÓDIGO]` para agilizar la observabilidad, el diagnóstico de fallas distribuidas y el mantenimiento del software.

---

## Catálogo de Códigos de Error

| Código de Error | Categoría | Severidad | Descripción del Error | Contingencia o Acción Recomendada |
| :--- | :--- | :--- | :--- | :--- |
| **`[ERR-SEC-401]`** | Seguridad | ALTA | Credenciales incorrectas, usuario inexistente o contraseña inválida en el login. | Registrar intento fallido y denegar la emisión del token JWT. |
| **`[ERR-SEC-403]`** | Seguridad | MEDIA | Acceso denegado debido a fallas en la validación de privilegios RBAC (Forbidden). | Bloquear la operación a nivel de red y retornar código `403` al cliente. |
| **`[ERR-VAL-400]`** | Validación | BAJA | Parámetros obligatorios faltantes o inconsistencias en los datos del RequestBody. | Retornar código `400 Bad Request` indicando la validación fallida de negocio. |
| **`[ERR-BFF-501]`** | Conectividad | CRÍTICA | Falla al consumir el microservicio de Estructura Organizativa (`service-areas`). | Retornar lista vacía de Áreas/Equipos en el BFF para garantizar la resiliencia. |
| **`[ERR-BFF-502]`** | Conectividad | CRÍTICA | Falla al consumir el microservicio de Metas Organizacionales (`service-metas`). | Retornar lista vacía de Metas en el BFF para evitar el colapso del dashboard. |
| **`[ERR-BFF-503]`** | Conectividad | CRÍTICA | Falla al consumir el microservicio de Gestión de KPIs (`service-kpis`). | Retornar lista vacía de KPIs y mediciones, aislando la falla de KPIs. |
| **`[ERR-BFF-504]`** | Orquestación | ALTA | Excepción interna del BFF al consolidar o calcular las métricas agregadas del dashboard. | Retornar código `500` con advertencia controlada para evitar caídas silenciosas. |
| **`[ERR-CB-503]`** | Resiliencia | CRÍTICA | Disyuntor (Circuit Breaker) activado en `service-metas` por indisponibilidad de `service-kpis`. | Activar **Fallback**: permitir la creación de la meta bajo contingencia y emitir alerta. |
| **`[ERR-DB-500]`** | Base de Datos | ALTA | Error sintáctico, de integridad o restricción en la persistencia local de MySQL. | Capturar excepción JPA, impedir el registro duplicado o inconsistente y retornar error. |

---

## Clasificación por Niveles de Severidad

1. **`CRÍTICA`**: Pérdida de conectividad con un microservicio de negocio. Requiere intervención inmediata o que se activen los mecanismos de tolerancia a fallos del BFF / Circuit Breakers para operar de forma degradada.
2. **`ALTA`**: Fallos que impiden la persistencia de datos (JPA/MySQL) o errores lógicos internos. Afectan funcionalidades clave del negocio.
3. **`MEDIA`**: Intentos de intrusión, accesos a rutas restringidas sin privilegios o problemas de expiración de sesión.
4. **`BAJA`**: Errores menores de usuario al completar formularios (validación de campos requeridos). Se corrigen corrigiendo la entrada de datos.
