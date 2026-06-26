# 🧪 Guía y Registro de Pruebas de la Plataforma KPI (Grupo Cordillera)

Este documento contiene la descripción técnica, clasificación y detalles de ejecución de las **30 pruebas automatizadas** del sistema. Las pruebas están divididas por nivel de granularidad tecnológica (**Unitarias**, **Integración** y **End-to-End**) y sub-clasificadas en **Camino Feliz (Happy Path)** y **Camino Infeliz (Casos de Borde, Errores y Vulnerabilidades)**.

---

## 📊 Resumen General de Pruebas (30 Tests)

| Categoría | Camino Feliz (Happy Path) | Camino Infeliz / Vulnerabilidades / Bugs | Total |
| :--- | :---: | :---: | :---: |
| **1. Pruebas Unitarias** | 7 | 1 | **8** |
| **2. Pruebas de Integración** | 10 | 7 | **17** |
| **3. Pruebas End-to-End (E2E)** | 3 | 2 | **5** |
| **Total** | **20** | **10** | **30** |

---

## 1. 🔍 Pruebas Unitarias (Unit Tests - 8 Tests)
Las pruebas unitarias evalúan la lógica interna de componentes y algoritmos aislados en memoria, sin requerir acceso a bases de datos, red, ni levantar el contexto completo de Spring Boot.

### 🟢 Camino Feliz (7 Tests)

#### A. Módulo: Catálogo de KPIs (`service-kpis`)
1.  **[`KpiFactoryTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/microservices/service-kpis/src/test/java/com/grupocordillera/kpis/factory/KpiFactoryTest.java) ➜ `testCreateFinancieroKpi`**
    *   **Explicación**: Comprueba que al solicitar la creación de un KPI de tipo `"FINANCIERO"` a la fábrica (`KpiFactory`), se instancie correctamente un objeto de la clase `FinancieroKPI` con su nombre, descripción y unidad de medida correspondientes.
2.  **[`KpiFactoryTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/microservices/service-kpis/src/test/java/com/grupocordillera/kpis/factory/KpiFactoryTest.java) ➜ `testCreateCumplimientoKpi`**
    *   **Explicación**: Valida el patrón Factory Method cuando se solicita un KPI de tipo `"CUMPLIMIENTO"`, verificando que se obtenga una instancia de `CumplimientoKPI` y que retenga sus propiedades de manera íntegra.
3.  **[`KpiFactoryTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/microservices/service-kpis/src/test/java/com/grupocordillera/kpis/factory/KpiFactoryTest.java) ➜ `testCreateOperacionalKpi`**
    *   **Explicación**: Asegura que la factoría cree un `OperacionalKPI` al pasar la cadena `"OPERACIONAL"`, comprobando la correcta asignación de campos descriptivos y de métricas.

#### B. Módulo: BFF Gateway Security (`bff-gateway`)
4.  **[`JwtUtilTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/security/JwtUtilTest.java) ➜ `testGenerateAndExtractToken`**
    *   **Explicación**: Valida el proceso criptográfico de generación de un token JWT usando una clave secreta HMAC-SHA y verifica que al extraer el nombre de usuario (subject) coincida exactamente con el ingresado.
5.  **[`JwtUtilTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/security/JwtUtilTest.java) ➜ `testExtractRole`**
    *   **Explicación**: Verifica que los Custom Claims inyectados dentro del JWT codifiquen de forma correcta el rol de seguridad del usuario (ej. `"JEFE_AREA"`) y que el helper lo extraiga adecuadamente.
6.  **[`JwtUtilTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/security/JwtUtilTest.java) ➜ `testExtractAreaIdAndEquipoId`**
    *   **Explicación**: Valida que los metadatos relacionales necesarios para los filtros de datos (`areaId` y `equipoId`) se empaqueten y recuperen de manera correcta en el payload cifrado del JWT.
7.  **[`JwtUtilTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/security/JwtUtilTest.java) ➜ `testValidateTokenSuccess`**
    *   **Explicación**: Genera un token legítimo y comprueba que la función de validación devuelva `true` cuando se compara contra el nombre de usuario emisor y el token no ha expirado.

### 🔴 Camino Infeliz (1 Test)

#### A. Módulo: BFF Gateway Security (`bff-gateway`)
8.  **[`JwtUtilTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/security/JwtUtilTest.java) ➜ `testValidateTokenFailure`**
    *   **Explicación**: Valida el rechazo de tokens inválidos. Comprueba que el componente devuelva `false` si se intenta validar el token contra un nombre de usuario diferente o si el token ha sido manipulado, mitigando potenciales ataques de suplantación.

---

## 2. 🔌 Pruebas de Integración (Integration Tests - 17 Tests)
Evalúan la interacción y comunicación entre múltiples componentes del ecosistema, como controladores Spring, clientes REST (RestTemplate), bases de datos en memoria (H2) y sistemas de tolerancia a fallos (Circuit Breakers).

### 🟢 Camino Feliz (10 Tests)

#### A. Módulo: Persistencia en Áreas y Equipos (`service-areas`)
1.  **[`UsuarioRepositoryIntegrationTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/microservices/service-areas/src/test/java/com/grupocordillera/areas/repository/UsuarioRepositoryIntegrationTest.java) ➜ `testSaveAndFindUser`**
    *   **Explicación**: Comprueba la integración de Hibernate/JPA con H2 insertando un nuevo `Usuario` con sus parámetros y buscándolo por su clave primaria, validando la persistencia de los campos.
2.  **[`UsuarioRepositoryIntegrationTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/microservices/service-areas/src/test/java/com/grupocordillera/areas/repository/UsuarioRepositoryIntegrationTest.java) ➜ `testUpdateUser`**
    *   **Explicación**: Altera el password y el rol de un usuario existente, guardando los cambios en base de datos y verificando que la actualización se haya consolidado en el registro persistido.
3.  **[`UsuarioRepositoryIntegrationTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/microservices/service-areas/src/test/java/com/grupocordillera/areas/repository/UsuarioRepositoryIntegrationTest.java) ➜ `testDeleteUser`**
    *   **Explicación**: Crea un usuario en base de datos H2, confirma su existencia, procede con su eliminación física por ID y verifica que ya no se encuentre en las consultas.
4.  **[`UsuarioRepositoryIntegrationTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/microservices/service-areas/src/test/java/com/grupocordillera/areas/repository/UsuarioRepositoryIntegrationTest.java) ➜ `testFindAllUsers`**
    *   **Explicación**: Inserta múltiples usuarios de prueba y valida que el método `findAll()` de JPA retorne la colección completa con el conteo esperado.
5.  **[`UsuarioRepositoryIntegrationTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/microservices/service-areas/src/test/java/com/grupocordillera/areas/repository/UsuarioRepositoryIntegrationTest.java) ➜ `testSaveAndFindArea`**
    *   **Explicación**: Valida el almacenamiento y recuperación exitosa de entidades de tipo `Area` en la base de datos H2, asegurando que los campos descriptivos se persistan íntegramente.
6.  **[`UsuarioRepositoryIntegrationTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/microservices/service-areas/src/test/java/com/grupocordillera/areas/repository/UsuarioRepositoryIntegrationTest.java) ➜ `testSaveAndFindEquipo`**
    *   **Explicación**: Verifica que se guarden correctamente las entidades de tipo `Equipo` relacionándolas con su respectiva área y líder de equipo en la base de datos integrada.

#### B. Módulo: BFF Auth Controller Integration (`bff-gateway`)
7.  **[`AuthControllerIntegrationTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/controller/AuthControllerIntegrationTest.java) ➜ `testLoginSuccess`**
    *   **Explicación**: Realiza una petición POST simulada con `MockMvc` a `/api/auth/login` con credenciales válidas. Mockea la llamada al servicio externo de áreas y comprueba que se reciba un HTTP 200 y el token JWT correspondiente.

#### C. Módulo: BFF Gateway API Integration Clients (`bff-gateway`)
8.  **[`BackendClientServiceTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/service/BackendClientServiceTest.java) ➜ `testGetKpisSuccess`**
    *   **Explicación**: Mockea la respuesta HTTP de `service-kpis` y valida que el cliente REST del BFF procese correctamente la lista de KPIs del catálogo, retornando un estado `"OK"`.
9.  **[`BackendClientServiceTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/service/BackendClientServiceTest.java) ➜ `testGetMetasSuccess`**
    *   **Explicación**: Mockea la respuesta de `service-metas` y comprueba que el BFF Gateway reciba y serialice exitosamente la lista de metas organizacionales.

#### D. Módulo: Validación de KPIs en Metas (`service-metas`)
10. **[`KpiValidationServiceTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/microservices/service-metas/src/test/java/com/grupocordillera/metas/service/KpiValidationServiceTest.java) ➜ `testValidateKpiExistsSuccess`**
    *   **Explicación**: Simula que `service-kpis` responde satisfactoriamente indicando que el KPI existe. Valida que el servicio interno de metas retorne `true` habilitando el flujo ordinario de creación.

---

### 🔴 Camino Infeliz, Robustez y Resiliencia (7 Tests)

#### A. Módulo: Autenticación y Robustez en Login (`bff-gateway`)
11. **[`AuthControllerIntegrationTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/controller/AuthControllerIntegrationTest.java) ➜ `testLoginIncorrectPassword`**
    *   **Explicación**: Valida el caso de borde de contraseña errónea. Asegura que la petición al endpoint `/api/auth/login` sea denegada con un código HTTP 401 (Unauthorized) y un mensaje amigable al cliente.
12. **[`AuthControllerIntegrationTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/controller/AuthControllerIntegrationTest.java) ➜ `testLoginUserNotFound`**
    *   **Explicación**: Comprueba el comportamiento del login cuando el usuario no existe en la base de datos corporativa. Asegura que responda con HTTP 401 mitigando la enumeración de usuarios.
13. **[`AuthControllerIntegrationTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/controller/AuthControllerIntegrationTest.java) ➜ `testLoginMissingFields`**
    *   **Explicación**: Envía un JSON incompleto al endpoint de login (sin password). Evita un bug en tiempo de ejecución validando que la API rechace la petición con un HTTP 400 (Bad Request).
14. **[`AuthControllerIntegrationTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/controller/AuthControllerIntegrationTest.java) ➜ `testLoginAreasServiceUnavailable`**
    *   **Explicación**: Simula una desconexión total o timeout de `service-areas`. Verifica que el BFF no colapse y responda elegantemente con HTTP 503 (Service Unavailable) para proteger la experiencia del usuario final.

#### B. Módulo: Tolerancia a Fallos y Circuit Breakers (`bff-gateway`)
15. **[`BackendClientServiceTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/service/BackendClientServiceTest.java) ➜ `testGetKpisFallback`**
    *   **Explicación**: Simula un fallo de red hacia el microservicio de KPIs. Valida que el disyuntor (Circuit Breaker) intercepte el error y active el método Fallback, devolviendo un estado `"DEGRADADO"` en lugar de colapsar la agregación.
16. **[`BackendClientServiceTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/service/BackendClientServiceTest.java) ➜ `testGetMetasFallback`**
    *   **Explicación**: Valida el Circuit Breaker para el servicio de Metas. Simula una interrupción física y comprueba que el método fallback de resiliencia devuelva un estado degradado controlado.

#### C. Módulo: Resiliencia de Metas (`service-metas`)
17. **[`KpiValidationServiceTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/microservices/service-metas/src/test/java/com/grupocordillera/metas/service/KpiValidationServiceTest.java) ➜ `testValidateKpiExistsFallback`**
    *   **Explicación**: Simula una desconexión con el catálogo de KPIs durante la creación de metas. Verifica que la resiliencia optimista retorne `true` (fallback), permitiendo al usuario registrar la meta de negocio de forma autónoma.

---

## 🏆 3. Pruebas End-to-End (E2E Tests - 5 Tests)
Las pruebas End-to-End simulan el ciclo de vida completo de una solicitud HTTP entrante desde el exterior, evaluando los filtros de seguridad del gateway, el ruteo hacia microservicios y la agregación de datos consolidada.

### 🟢 Camino Feliz (3 Tests)

#### A. Módulo: Orquestador y API Gateway (`bff-gateway`)
1.  **[`BffGatewayE2eTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/controller/BffGatewayE2eTest.java) ➜ `testDashboardE2EAdminAccess`**
    *   **Explicación**: Envía una petición GET al dashboard del BFF con un token firmado con rol `"ADMIN"`. Comprueba que el BFF orqueste y consolide todos los microservicios, devolviendo las metas y el cumplimiento global sin restricciones (HTTP 200).
2.  **[`BffGatewayE2eTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/controller/BffGatewayE2eTest.java) ➜ `testDashboardE2ECollaboratorAccess`**
    *   **Explicación**: Realiza la consulta del dashboard con un token de `"COLABORADOR"` asociado a un equipo específico. Verifica la lógica de negocio jerárquica: el payload devuelto por el BFF debe contener únicamente las metas relativas al equipo del solicitante (ocultando datos de terceros).
3.  **[`BffGatewayE2eTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/controller/BffGatewayE2eTest.java) ➜ `testCreateMetaSuccess`**
    *   **Explicación**: Simula el envío de un payload POST de creación de meta por un `"ADMIN"`. Verifica que el BFF filtre los permisos, enrute exitosamente el objeto a `service-metas` y retorne un HTTP 201 (Created).

### 🔴 Camino Infeliz y Vulnerabilidades (2 Tests)

#### A. Módulo: Orquestador y API Gateway (`bff-gateway`)
4.  **[`BffGatewayE2eTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/controller/BffGatewayE2eTest.java) ➜ `testDashboardE2EUnauthorized`**
    *   **Vulnerabilidad Mitigada**: Acceso no autenticado / bypass de seguridad a la API del negocio.
    *   **Explicación**: Envía peticiones al dashboard sin cabecera `Authorization` o con firmas corruptas. Valida que el Gateway rechace la petición y bloquee el acceso retornando un código de error HTTP 403 (Forbidden) o HTTP 401.
5.  **[`BffGatewayE2eTest.java`](file:///c:/Users/User/OneDrive/Documentos/Pixelium_Repo/cordillera/bff-gateway/src/test/java/com/grupocordillera/bff/controller/BffGatewayE2eTest.java) ➜ `testCreateMetaForbidden`**
    *   **Vulnerabilidad Mitigada**: Escalada de privilegios horizontal y vertical (Bypass de RBAC).
    *   **Explicación**: Un usuario con rol `"COLABORADOR"` intenta enviar un POST para crear una nueva meta organizativa. Asegura que el filtro del BFF deniegue la operación a nivel de API Gateway devolviendo un HTTP 403 y previniendo la alteración no autorizada de metas organizacionales.

---

## 🚀 Cómo Ejecutar las Pruebas

Puedes ejecutar la suite completa (las 30 pruebas) de forma automatizada mediante Maven desde la raíz del proyecto:

```bash
mvn clean test
```

### Ejecución por módulos:
*   **BFF Gateway (19 Tests)**: `mvn -pl bff-gateway test`
*   **Gestión de KPIs (3 Tests)**: `mvn -pl microservices/service-kpis test`
*   **Metas Organizacionales (2 Tests)**: `mvn -pl microservices/service-metas test`
*   **Áreas y Equipos (6 Tests)**: `mvn -pl microservices/service-areas test`
